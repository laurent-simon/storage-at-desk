package edu.virginia.cs.storagedesk.common;

public class DataBlock {
	private long position;
	private byte[] bytes;
	
	public DataBlock(long p, byte[] b) {
		this.position = p;
		this.bytes = b;
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	public long getPosition() {
		return position;
	}
	public void setPosition(long position) {
		this.position = position;
	} 
	
}
