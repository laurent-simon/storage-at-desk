package edu.virginia.cs.storagedesk.volumecontroller;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.TreeMap;

import edu.virginia.cs.storagedesk.common.Config;
import edu.virginia.cs.storagedesk.common.Util;
import edu.virginia.cs.storagedesk.database.Machine;
import edu.virginia.cs.storagedesk.database.Volume;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class VolumeController implements IVolumeController{
	
	private static Logger logger = Logger.getLogger(VolumeController.class);
	
	private static Connection conn;
	
	private TreeMap<String, Machine> machines = new TreeMap<String, Machine>();
//	private TreeMap<Integer, Volume> volumes = new TreeMap<Integer, Volume>();
	
	public VolumeController() {

		try {
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (Config.DB_URL, Config.DB_USER_NAME, Config.DB_PASSWORD);
			if (conn == null) {
				logger.error("NULL connection");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 

		logger.debug("Starting the controller");
	}
	
	public boolean registerMachine(Machine machine) throws RemoteException {
		logger.info("Registering a machine");

		if (machine.inDatabase(conn) == false) {
			machine.insert(conn);
			if (machine.inDatabase(conn)) {
				logger.debug("Machine registration finishes");
			} else {
				logger.error("Machine registration failed");
				return false;
			}
		}

		if (machines.containsKey(machine.getId()) == false) {
			machines.put(machine.getId(), machine);
		}
		return true;
	}
	
	public boolean isNewVolume(Volume volume) throws RemoteException {
		logger.info("Is this a new volume?");
		int id = 0;
		id = volume.inDatabase(conn);
		if ( id == -1) {
			return true;
		} else {
			return false;
		}
	}
		
	public int registerVolume(Volume volume) throws RemoteException {
		logger.info("Registering a volume");
		int id = 0;
		id = volume.inDatabase(conn);
		if ( id == -1) {
			id = volume.insert(conn);
		}

//		if (volumes.containsKey(new Integer(id)) == false) {
//			volumes.put(new Integer(id), volume);
//		}		
		return id;
	}
	
	// Retrieves machine information from the database
	// Evenly distributes the volume on the machines	
	public Volume assignMapping(Volume volume) throws RemoteException {
		logger.info("Getting a mapping for a volume");
		if (volume.existedMapping(conn) == false) {
			if (volume.assignMapping(conn) == false) {
				throw new RemoteException("Cannot achieve mappings");
			}
		}
		return volume;
	}
	
	public boolean machineHeartbeat(String id, String ip) throws RemoteException {
		Machine m = new Machine(id, ip);
		return m.heartbeat(conn);
	}
		
	public static void main(String args[]) {	
		try {
//			 Load properties
			
			InetAddress addr = InetAddress.getLocalHost();
			// Get IP Address
			String ip = addr.getHostAddress();			
			// Get hostname
			String hostname = addr.getHostName();
			System.setProperty("logfile.name", "VolumeController."+hostname+"."+ip+".log");
			
			PropertyConfigurator.configure("storage@desk.log4j.properties");
			
			Util.setProperties(logger);

			try {
				logger.info("Starting RMI Registry");
				java.rmi.registry.LocateRegistry.createRegistry(Config.SD_SOCKET_PORT);
			} catch (Exception e) {
				logger.info("RMI Registry Already Running");
			}
			
			VolumeController controller = new VolumeController();
			IVolumeController stub = (IVolumeController) UnicastRemoteObject.exportObject(controller, 0);
						
			Registry registry = LocateRegistry.getRegistry(Config.VOLUMECONTROLLER_IP_ADDRESS, Config.SD_SOCKET_PORT);
			registry.rebind("VolumeController", stub);
			
			logger.info("Volume Controller starts");
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}
}
