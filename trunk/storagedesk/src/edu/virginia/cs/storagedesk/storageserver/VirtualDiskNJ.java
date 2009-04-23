package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.Config;
import edu.virginia.cs.storagedesk.common.Disk;
import edu.virginia.cs.storagedesk.common.Util;
import edu.virginia.cs.storagedesk.database.Mapping;
import edu.virginia.cs.storagedesk.database.Volume;
import edu.virginia.cs.storagedesk.storagemachine.IStorageMachine;
import edu.virginia.cs.storagedesk.volumecontroller.IVolumeController;

public class VirtualDiskNJ extends Disk {
	
	// log4j logger
	private static Logger logger = Logger.getLogger(VirtualDiskNJ.class);
	
	// Volume Controller
	private Registry controllerRegistry;
	private IVolumeController volumeController;
	
	// volume data structure
	private Volume volume;
	
	// volume chunk mappings
	// [replica_id][chunk_id]
	private Mapping[][] mappings;
	
	// Storage Machines
	private Map<String, IStorageMachine> storageMachines = new ConcurrentHashMap<String, IStorageMachine>();
	private Map<String, Registry> machineRegistry = new ConcurrentHashMap<String, Registry>();
	private Map<Integer, Vector<Replica>> replicaStatus = new ConcurrentHashMap<Integer, Vector<Replica>>();
	
	public VirtualDiskNJ(int numCopies) {
		super();
		
		// initialize volume data structure
		this.volume = new Volume(Config.TARGET_NAME,
								numCopies,
								this.getNumLUNs(),
								this.getNumBlocks(),
								this.getBlockSize(),
								Config.VOLUME_CHUNK_SIZE);
		// get mappings
		try {
			// Init VolumeController RMI communication
			controllerRegistry = LocateRegistry.getRegistry(Config.VOLUMECONTROLLER_IP_ADDRESS, Config.SD_SOCKET_PORT);
			volumeController = (IVolumeController) controllerRegistry.lookup("VolumeController");
			
			// check if volume exists
			if (volumeController.isNewVolume(volume)){
				logger.error("Trying to create a new volume, use admin console instead.");
				return;
			}
			
			// find out volume ID
			volume.setId(volumeController.registerVolume(volume));
			
			// check if valid
			if (volume.getId() > -1) {
				logger.info("Volume registration OK and id is " + volume.getId());
			} else {
				logger.info("Volume registration failed");
				return;
			}	

			// Retrieve mappings from the Volume Controller				
			this.volume = volumeController.assignMapping(volume);	// only assigns if existedMapping fails			
			this.mappings = volume.getMappings();					// [replica_id][chunk_id]
			
			// Correct number of replicas?
			if (mappings.length != numCopies) {
				logger.error("SHOULD HAVE " + numCopies + " COPIES");
				return;
			}
			
		} catch (Exception e) {
			logger.error(Util.getStackTrace(e));
		}
		
		// get storage machine stubs
		// initialize machines
		// initialize replica status
		
		// init replicas list
		for (int chunk = 0; chunk < this.volume.getNumChunks(); chunk++) {
			// key is physical chunk id
			// value is list of replicas for that chunk
			// [virtualchunk_id][list of replicas]
			this.replicaStatus.put(new Integer(chunk), new Vector<Replica>() );
		}
		
		// for each replica
		for (int replica = 0; replica < mappings.length; replica++) {
			
			// Ask each chunk to initialize itself
			for (int i = 0; i < mappings[replica].length; i++) {
				
				logger.info("Init for replica " + replica + " chunk " + i);
				Mapping mapping = mappings[replica][i];
				
				try {
					// check if machine already exists
					if (machineRegistry.containsKey(mapping.getMachineID()) == false){
						// Init StorageMachine RMI communication
						logger.info("Getting the registry of the machine (id " + mapping.getMachineID() + ") ip " + mapping.getIp());
						Registry mReg = LocateRegistry.getRegistry(mapping.getIp(), Config.SD_SOCKET_PORT);
						machineRegistry.put(mapping.getMachineID(), mReg); 
					} else {
						logger.info("Machine Registry for "+ mapping.getMachineID() + " already exists.");
					}
					
					if (storageMachines.containsKey(mapping.getMachineID()) == false) {
						// Init machineStub
						logger.info("Looking up the stub of the machine (id " + mapping.getMachineID() + ") ip " + mapping.getIp());
						IStorageMachine machineStub = (IStorageMachine) machineRegistry.get(mapping.getMachineID()).lookup(mapping.getMachineID());
						
						// add mapping
						logger.info("Got the stub of the machine (id " + mapping.getMachineID() + ") ip " + mapping.getIp());
						storageMachines.put(mapping.getMachineID(), machineStub);
						
						// Init Storage Machine
						logger.info("Init machine (id " + mapping.getMachineID() + ")");
						machineStub.init();
						
						logger.info("Machine ready (id " + mapping.getMachineID() + ")");
					} else {
						logger.info("Machine Stub for "+ mapping.getMachineID() + " already exists.");
					}
					
					// Add to replicas list
					Replica r = new Replica(mapping);
					this.replicaStatus.get(new Integer(mapping.getVirtualChunkID())).add( r );
					
				} catch (Exception e) {
					// machine down?
					logger.error(Util.getStackTrace(e));
				}
				
			// end chunk for loop
			}
		// end replica for loop
		}
	// end constructor 
	}
	
	// override Disk read
	// called magically by scsi execute command
	public byte[] read(long position, int length) throws IOException {
		// for each replica
			// check if readable
		// pick random one able to read from
			// read
			// write through to those who cant read
		
		logger.debug("Read " + length + " bytes at " + position);
		
		byte[] bytes = new byte[1];
		
		// read may span multiple chunks
		// buffer result and get from chunks sequentially
		int virtualChunk = (int) Math.floor(position/ (double)this.volume.getChunkSize());
		bytes = new byte[length];
		int numBytesLeft = length;
		int count = 0;
		// int cursor = (int) (position - virtualChunk * this.volume.getChunkSize());
		// chunk size can be a long so cursor might still be long
		long cursor = position - virtualChunk * this.volume.getChunkSize();
		
		// loop over result array
		while (numBytesLeft > 0) {
			// long numBytesAdjusted = numBytesLeft;
			int numBytesAdjusted = numBytesLeft;
			
			if ((cursor + numBytesLeft) > this.volume.getChunkSize()) {
				numBytesAdjusted = (int) (this.volume.getChunkSize() - cursor);
			}
			
			byte[] result = new byte[1];
			
			// for each replica, see if readable
			//TODO: might need to synchronize this
			Iterator<Replica> itr = this.replicaStatus.get(new Integer(virtualChunk)).iterator();
			Vector<Replica> valid = new Vector<Replica>();
			Vector<Replica> invalid = new Vector<Replica>();
			while (itr.hasNext()) {
				Replica rep = itr.next();
				if (rep.read(cursor, numBytesAdjusted)) {
					valid.add(rep);
				} else {
					invalid.add(rep);
				}
			}
			
			//TODO: might make this synchronized
			Iterator<Replica> validItr = valid.iterator();
			while (validItr.hasNext()) {
				
				Replica rep = validItr.next();
				IStorageMachine machineStub = storageMachines.get(rep.getMachineID());
				
				logger.debug("Read for replica " + rep.getReplicaID() + " virtual chunk " + virtualChunk + " from machine (id " + 
						rep.getMachineID() + ") at " + cursor);

				boolean readOK = true;
				
				try {
					result= machineStub.read(rep.getPhysicalChunkID(),
							cursor,
							(int) numBytesAdjusted);		
				} catch (ConnectException e) {
					logger.error("Unable to connect to the storage machine: " + rep.getMachineID());
					readOK = false;
				} catch (RemoteException e) {
					logger.error("Unable to read from replica " + + rep.getReplicaID());
					logger.error(Util.getStackTrace(e));
					readOK = false;
				} 

				if (readOK == false) {
					// read not successful
					// continue loop
				} else {
					// read was successful
					// add code here to write to all invalid
					// copy to total result
					System.arraycopy(result, 
							0,
							bytes,
							count,
							result.length);

					logger.debug("Read " + result.length  + " bytes from replica " + + rep.getReplicaID() + ", machine (id " + 
							rep.getMachineID() + ") at " + cursor);
					
					break;
				}
			}

			// update cursor to read
			count += result.length;
			numBytesLeft -= result.length;
			if (numBytesLeft > 0) {
				virtualChunk++;
				cursor = 0;
			}

		} // end of the while loop

		return bytes;
	}
	
	// override Disk write
	// called magically by scsi execute write command
	public boolean write(byte[] bytes, long position) throws IOException {
		// for each replica
			// write
			// update replica information to be current if write succeeded
		
		logger.debug("Write to DISK " + bytes.length + " bytes at " + position);

		boolean success = true;
			
		int virtualChunk = (int) Math.floor(position/ (double)this.volume.getChunkSize());
		int numBytesLeft = bytes.length;
		int count = 0;
		long cursor = position - virtualChunk * this.volume.getChunkSize();

		while (numBytesLeft > 0) {
			long numBytesAdjusted = numBytesLeft;
			if ((cursor + numBytesLeft) > this.volume.getChunkSize()) {
				numBytesAdjusted = this.volume.getChunkSize() - cursor;
			} 
			
			//TODO: might need to synchronize this
			boolean writeOK = false;
			Iterator<Replica> itr = this.replicaStatus.get(new Integer(virtualChunk)).iterator();
			while (itr.hasNext()) {
				Replica rep = itr.next();
				
				IStorageMachine machineStub = storageMachines.get(rep.getMachineID());
				
				logger.debug("Write for replica " + rep.getReplicaID() + " virtual chunk " + virtualChunk + " from machine (id " + 
						rep.getMachineID() + ") at " + cursor);

				byte[] portion = new byte[(int) numBytesAdjusted];
				System.arraycopy(bytes, 
						count,
						portion,
						0,
						(int) numBytesAdjusted);

				try {
					machineStub.write(rep.getPhysicalChunkID(),
							portion,
							cursor);
					writeOK = true;
					rep.write(cursor, portion.length);
					
				} catch (ConnectException e) {
					logger.error("Unable to connect to the storage machine");
					logger.error(Util.getStackTrace(e));		
				} catch (RemoteException e) {
					logger.error("Unable to write the storage machine");
					logger.error(Util.getStackTrace(e));
				}

				logger.debug("Write " + numBytesAdjusted  + " bytes from machine (id " + 
						rep.getMachineID() + ") at " + cursor);
			}
			
			if (writeOK == false) {
				success = false;
			}
			
			count += numBytesAdjusted;
			numBytesLeft -= numBytesAdjusted;

			if (numBytesLeft > 0) {
				virtualChunk++;
				cursor = 0;
			}			
		} // end of the while loop

		logger.debug("Write ends");
		return success;
	}
	
// end class
}
