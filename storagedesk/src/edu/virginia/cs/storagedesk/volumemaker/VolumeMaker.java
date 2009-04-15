package edu.virginia.cs.storagedesk.volumemaker;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import edu.virginia.cs.storagedesk.common.Config;
import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;
import edu.virginia.cs.storagedesk.database.Mapping;
import edu.virginia.cs.storagedesk.database.Volume;
import edu.virginia.cs.storagedesk.volumecontroller.IVolumeController;

public class VolumeMaker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// parse input
		int numCopies = 2;
		long numLUNs = 1;
		long numBlocks = 100000;
		long blockSize = 1024;
		String targetName = "iqn.edu.virginia.cs.storagedesk:disk2";
		String ipAddress = "172.27.44.116";
		int port = 1099;
		
		
		
		
		Volume volume = new Volume(targetName,
				numCopies,
				numLUNs,
				numBlocks,
				blockSize,
				(int) Math.ceil((ISCSI.DEFAULT_DISK_NUM_LUNS *
						ISCSI.DEFAULT_DISK_BLOCK_SIZE *
						ISCSI.DEFAULT_DISK_NUM_BLOCKS)/
						Config.VOLUME_CHUNK_SIZE),
						Config.VOLUME_CHUNK_SIZE);

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
			volume = volumeController.assignMapping(volume);				
			Mapping[][] mappings = volume.getMappings();

			if (mappings.length != numCopies) {
				//logger.error("SHOULD HAVE " + numCopies + " COPIES");
				System.out.println("SHOULD HAVE " + numCopies + " COPIES");
				return;	
			}
			
		} catch (Exception e) {
			//logger.error(Util.getStackTrace(e));
			System.out.println(Util.getStackTrace(e));
		}
	}

}
