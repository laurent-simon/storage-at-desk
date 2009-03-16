package tests;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.DataBlock;
import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.storageserver.VirtualDisk;

public class VirtualDiskWriteBuffer implements Runnable{
	
	private static Logger logger = Logger.getLogger(VirtualDiskWriteBuffer.class);

	public static ArrayBlockingQueue<DataBlock> queue;
	
	private static VirtualDisk virtualDisk;
	
	public VirtualDiskWriteBuffer(VirtualDisk vd) {
		virtualDisk = vd;
		queue = new ArrayBlockingQueue<DataBlock>(ISCSI.DISK_WRITE_BUFFER_SIZE, true);
	}
	
	public byte[] flushBlocks(long start, long end) {
		byte[] b = null;
		for (Iterator i = queue.iterator(); i.hasNext();) {
			DataBlock db = (DataBlock) i.next();
			if ((start >= db.getPosition() &&
				 start <= db.getPosition() + db.getBytes().length) ||
				(end >= db.getPosition() &&
				 end <= db.getPosition() + db.getBytes().length)){
				if (start == db.getPosition() &&
					end == db.getPosition() + db.getBytes().length) {
					b = db.getBytes();
				} else {
//					virtualDisk.writeToDisk(db.getBytes(), db.getPosition());
//					queue.remove(db);
				}
				logger.error("Read one block " + 
						db.getPosition() + "-" + (db.getPosition() + db.getBytes().length) +
						" (read from " + start + " to " + end + ")");

			}
		}
		return b;
	}
	
	public void add(DataBlock db){
		try {
			if (queue.remainingCapacity() == 0)
				remove();
			queue.put(db);
			logger.error("Add the data block to the queue (size " + queue.size() + ")");
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
	}
	
	public void remove() {
		DataBlock db = queue.poll();
		if (db != null) {
//			virtualDisk.writeToDisk(db.getBytes(), db.getPosition());
		}
	}
	
	public void flush() {
		logger.debug("To flush the queue (size " + queue.size() + ")");
		while (queue.isEmpty() == false) {
			remove();
		}
		logger.error("Flushed the queue (size " + queue.size() + ")");
	}
	
	public void run(){
		for (;;) {
			if (queue.isEmpty() == false) {
				remove();				
				logger.error("Write one block from the queue (size " + queue.size() + ")");
			} else {
//				logger.error("buffer is empty");
				try {
					Thread.sleep(0, 1);
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				}
			}			
		}
	}
}
