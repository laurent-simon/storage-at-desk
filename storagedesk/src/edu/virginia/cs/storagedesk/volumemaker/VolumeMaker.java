package edu.virginia.cs.storagedesk.volumemaker;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.virginia.cs.storagedesk.common.Util;
import edu.virginia.cs.storagedesk.database.Volume;
import edu.virginia.cs.storagedesk.volumecontroller.IVolumeController;

public class VolumeMaker {
	private static Logger logger = Logger.getLogger(VolumeMaker.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// setup logging
		System.setProperty("logfile.name", "VolumeMaker.log");
		PropertyConfigurator.configure("storage@desk.log4j.properties");
		logger.info("loaded logging properties");
		
		// replication level
		int numCopies = 1;
		
		// these values are used by ISCSI.java
		// let the user input this stuff into the form
		// and they should also include it in their own config
		// for their StorageServer
		// NOTE: numLUNs * numBlocks * blockSize = total disk size
		long numLUNs = 1;
		long numBlocks = 200000;
		long blockSize = 1024;	// static: 1KB
		
		String targetName = "iqn.edu.virginia.cs.storagedesk:disk3";
		
		// volume controller access information
		String ipAddress = "192.168.5.5";
		int port = 1099;
		
		// this value should be a system wide value and probably
		// hardcoded into Config.java
		long volumeChunkSize = 536870912;  // static: 512MB
		
		// This block is used to parse command line input
		try {
			for( int i = 0; i < args.length; i++ ) {
				if( args[i].equals("-numCp ") ) {
					i++;
					numCopies = Integer.valueOf(args[i]);
				} else if( args[i].equals("-LUN") ) {
					i++;
					numLUNs = Long.valueOf(args[i]);
				} else if( args[i].equals("-numBlocks") ) {
					i++;
					numBlocks = Long.valueOf(args[i]);
				} else if( args[i].equals("-targetName") ) {
					i++;
					targetName = args[i];
				} else if( args[i].equals("-ip") ) {
					i++;
					ipAddress = args[i];
				}  else if( args[i].equals("-port") ) {
					i++;
					port = Integer.parseInt(args[i]);
				} 
			}
		} catch(Exception e) {
			System.out.println("Invalid input");
			return;
		}
		
		Volume volume = new Volume(targetName,
				numCopies,
				numLUNs,
				numBlocks,
				blockSize,
				volumeChunkSize);

		boolean isNewVolume = true;
		try {
			// Ask the Volume Controller to register the machine
			Registry controllerRegistry = LocateRegistry.getRegistry(ipAddress, port);
			IVolumeController volumeController = (IVolumeController) controllerRegistry.lookup("VolumeController");

			// If this is a new volume
			// need to setup a bunch of things
			// 1. create a version file for the volume
			// 2. create a journal directory
			// 3. create a version file for each virtual chunk
			isNewVolume = volumeController.isNewVolume(volume);
			//volumeController.isNewVolume(volume);
			volume.setId(volumeController.registerVolume(volume));
			if (volume.getId() > -1) {
				//logger.info("Volume registration OK and id is " + volume.getId());
				System.out.println("Volume registration OK and id is " + volume.getId());
			} else {
				//logger.info("Volume registration failed");
				System.out.println("Volume registration failed");
				return;
			}
			
			// Assign or retrieve mappings from the Volume Controller
			try {
				volume = volumeController.assignMapping(volume);
				System.out.println("Volume mappings successfully assigned");
			} catch (RemoteException e) {
				System.out.println(e.getCause());
			}
			
			// not needed (exception above takes care of this
			/*
			Mapping[][] mappings = volume.getMappings();

			if (mappings.length != numCopies) {
				//logger.error("SHOULD HAVE " + numCopies + " COPIES");
				System.out.println("SHOULD HAVE " + numCopies + " COPIES");
				return;	
			}
			*/
			
		} catch (Exception e) {
			//logger.error(Util.getStackTrace(e));
			System.out.println(Util.getStackTrace(e));
		}
	}

}
