package edu.virginia.cs.storagedesk.storagemachine;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import edu.virginia.cs.storagedesk.common.FileDisk;
import edu.virginia.cs.storagedesk.common.Config;
import edu.virginia.cs.storagedesk.common.Util;
import edu.virginia.cs.storagedesk.database.Machine;
import edu.virginia.cs.storagedesk.volumecontroller.IVolumeController;

public class StorageMachine implements IStorageMachine, WrapperListener{ 
	private static Logger logger = Logger.getLogger(StorageMachine.class);
	
	static 
	  {
		 if (System.getProperty("os.name").substring(0, 3).toLowerCase().compareTo("win") == 0) {
			System.loadLibrary("nativefile");
		 	logger.debug("loaded dll");
		 }
	  }
	
//	private Registry controllerRegistry;
	private  IVolumeController volumeController;
	private Machine machine; // all meta data
		
	private FileDisk[] chunks;
	

	// 1. Check if it has registered in the database
	// 2. If not, register itself by calling the Volume Controller
	// 3. Bind the stub in the RMI registry
	
	public StorageMachine() {
 
	}
	
	public boolean isAlive() throws RemoteException {
		return true;
	}
	
	public boolean init() throws RemoteException {
		chunks = new FileDisk[machine.getNumChunks()];
		for (int i = 0; i < machine.getNumChunks(); i++) {
			chunks[i] = new FileDisk(i,
									 this.machine.getPath(),
									 Config.STORAGEMACHINE_FILE_PREFIX,
									 this.machine.getChunkSize());
		}

		return true;
	}
	
	
	
//	methods for read/write bytes via RMI
	public byte[] read(int chunkID, long position, int length) throws RemoteException {
		return chunks[chunkID].read(position, length);
	}
	
	public boolean write(int chunkID, byte[] bytes, long position) throws RemoteException {
		return chunks[chunkID].write(bytes, position);
	}
	
	public Integer start( String[] args )
    {
        System.out.println( "start()" );
        
        task();
        
        return null;
    }
    
    public int stop( int exitCode )
    {
        System.out.println( "stop(" + exitCode + ")" );
        
        return exitCode;
    }
    
    public void controlEvent( int event )
    {
        System.out.println( "controlEvent(" + event + ")" );
        
        if ( ( event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT )
            && WrapperManager.isLaunchedAsService() )
        {
            System.out.println( "  Ignoring logoff event" );
            // Ignore
        }
        else
        {
            WrapperManager.stop( 0 );
        }
    }
	
	public static void main(String args[]) {	
		boolean service = true;
		
		if (service) {
			System.out.println( " Init..." );		
			WrapperManager.start(new StorageMachine(), args);
		} else {
			task();
		}
	}
	
	private static void task() {
		StorageMachine sm = new StorageMachine();

		try {
			InetAddress addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress();			
			String hostname = addr.getHostName();
			System.setProperty("logfile.name", "StorageMachine."+
												hostname + "." +
												ip + 
												".log");
			
			PropertyConfigurator.configure("storage@desk.log4j.properties");
			
			try {
				logger.info("Starting RMI Registry");
				java.rmi.registry.LocateRegistry.createRegistry(1099);
			} catch (Exception e) {
				logger.info("RMI Registry Already Running");
			}	
			
//			 Load properties
			Util.setProperties(logger);
			
//			 Assume no two machines have the same hostname
			sm.machine = new Machine(UUID.nameUUIDFromBytes(hostname.getBytes()).toString(), 
								  hostname,
								  ip,
								  Config.STORAGEMACHINE_PATH,
								  Config.STORAGEMACHINE_NUM_CHUNKS,
								  Config.STORAGEMACHINE_CHUNK_SIZE);
			
			IStorageMachine stub = (IStorageMachine) UnicastRemoteObject.exportObject(sm, 0);
			
			// Ask the Volume Controller to register the machine
			Registry volumeRegistry = LocateRegistry.getRegistry(Config.VOLUMECONTROLLER_IP_ADDRESS); 
			if (volumeRegistry == null) {
				logger.error("Cannot get the controller registry");
				return ;
			}
			sm.volumeController = (IVolumeController) volumeRegistry.lookup("VolumeController");
			
			if (sm.volumeController.registerMachine(sm.machine)) {
				logger.info("Machine registration OK");
			} else {
				logger.error("Machine registration failed");
				return ;
			}
			
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(sm.machine.getId(), stub);
			
			Thread t = new Thread(sm.new Heartbeat());
		    t.start();
			
			logger.info("Storage machine starts");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class Heartbeat implements Runnable {
		
		public void run() {
			try {
				while (true) {
					volumeController.machineHeartbeat(machine.getId());
					Thread.sleep(Config.STORAGEMACHINE_HEARTBEAT_TIME);
				}
			} catch (InterruptedException e) {
			
			} catch (RemoteException e) {
				
			}
		}
	}
}
