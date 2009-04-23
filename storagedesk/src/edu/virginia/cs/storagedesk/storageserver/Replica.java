package edu.virginia.cs.storagedesk.storageserver;

import edu.virginia.cs.storagedesk.database.Mapping;

public class Replica {
	private String machineID;
	private int replicaID; // not needed?
	private int virtualChunkID;	// volume chunk id
	private int physicalChunkID; // storagemachine chunk id
	private long volumeID;
	
	// need
	// ISCSI.DEFAULT_DISK_BLOCK_SIZE
	
	// volumeId/virtualChunkId/machineId-physicalChunkId.dat
	
	public Replica(Mapping map) {
		super();
		this.machineID = map.getMachineID();
		this.replicaID = map.getReplicaID();
		this.virtualChunkID = map.getVirtualChunkID();
		this.physicalChunkID = map.getPhyscialChunkID();
		this.volumeID = map.getVolumeID();
	}
	
	public boolean read(long cursor, int length) {
		return true;
	}
	
	public void write(long cursor, int length) {
		// write 1's to file
	}

	// Getters
	
	public String getMachineID() {
		return machineID;
	}

	public int getReplicaID() {
		return replicaID;
	}

	public int getVirtualChunkID() {
		return virtualChunkID;
	}

	public int getPhysicalChunkID() {
		return physicalChunkID;
	}
	public long getVolumeId() {
		return volumeID;
	}
}
