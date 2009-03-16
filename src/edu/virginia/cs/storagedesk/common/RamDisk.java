// Will be removed

package edu.virginia.cs.storagedesk.common;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.storagemachine.IStorageMachine;

public class RamDisk extends Disk { //implements IStorageMachine {
	private static Logger logger = Logger.getLogger(RamDisk.class);
	
	private ByteBuffer data;
	
	private RamWriteBuffer writeBuffer;
		
//	public boolean init(int volumeID) {
//		if (super.init(volumeID)) {
//			if (data == null) {
//				data = ByteBuffer.allocate((int) (this.getNumLUNs() * this.getNumBlocks() * this.getBlockSize()));
//			}
//			
//			writeBuffer = new RamWriteBuffer(data);
//			new Thread(writeBuffer).start();
//			
//			return true;
//		} else {
//			return false;
//		}
//	}
	
	public void readTask(long position, int length) {
		
	}
	
	public void writeTask(long position, int length) {

	}
	
	public byte[] read(long position, int length) {
		logger.debug("Reading " + length + " bytes from Disk (LUN " + this.getLun() + 
				 	", offset " + position + ", " + length + " bytes)");
		
		byte[] bytes = new byte[length];
		
//		writeBuffer.flush();
		
		data.position((int) position);		
		data.get(bytes);
		logger.debug("Reading completes");
		return bytes;
	}
	
	public boolean write(byte[] bytes, long position) {
		logger.debug("Writing " + bytes.length + " bytes to Disk (LUN " + this.getLun() + 
				 ", offset " + position + ", " + bytes.length + " bytes)");
		
		data.position((int) position);
		data.put(bytes);
//		
//		writeBuffer.add(new DataBlock(position, bytes));
//		
//		this.setOffset(this.getOffset() + bytes.length);
		return true;
	}
	
	public boolean writeToDisk(long position, int length) {
		return true;
	}
}
