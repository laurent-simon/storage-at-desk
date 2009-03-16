package edu.virginia.cs.storagedesk.database;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.log4j.Logger;

public class Mapping implements Externalizable {
	
	private static Logger logger = Logger.getLogger(Volume.class);
	
	private static final long serialVersionUID = 0;
	
	private long volumeID;
	private int virtualChunkID;
	private int replicaID;
	private String machineID = "";
	private int physcialChunkID;	
	private String ip = "";
		
	public Mapping() {
		
	}
		
	public Mapping(long vid, int rid, int vcid, String mid, int pcid, String ip) {
		this.volumeID = vid;
		this.replicaID = rid;
		this.virtualChunkID = vcid;		
		this.machineID = mid;
		this.physcialChunkID = pcid;
		this.ip = ip;
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.volumeID 	 = in.readLong();
		this.virtualChunkID = in.readInt();
		this.replicaID   = in.readInt();
		this.machineID 	 = in.readUTF();
		this.physcialChunkID = in.readInt();
		this.ip = in.readUTF();
		logger.trace("Mapping: Read external completes");
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {		
		out.writeLong(this.volumeID);
		out.writeInt(this.virtualChunkID);
		out.writeInt(this.replicaID);
		out.writeUTF(this.machineID);
		out.writeInt(this.physcialChunkID);
		out.writeUTF(this.ip);
		logger.trace("Mapping: Write external completes");
	}
		
	public int getReplicaID() {
		return replicaID;
	}

	public String getMachineID() {
		return machineID;
	}

	public String getIp() {
		return ip;
	}

	public int getPhyscialChunkID() {
		return physcialChunkID;
	}

	public int getVirtualChunkID() {
		return virtualChunkID;
	}

	public long getVolumeID() {
		return volumeID;
	}
}
