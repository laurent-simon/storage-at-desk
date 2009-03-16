// Will be removed

package edu.virginia.cs.storagedesk.common;

import static java.util.logging.Logger.global;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ArrayBlockingQueue;

public class RamWriteBuffer implements Runnable{
	
	public ArrayBlockingQueue<DataBlock> queue;
	
	private static ByteBuffer data;
	
	public RamWriteBuffer(ByteBuffer bb) {
		data = bb;
		queue = new ArrayBlockingQueue<DataBlock>(100, true);
	}
	
	public void add(DataBlock db){
		try {
			queue.put(db);
			global.severe("Add the data block to the queue (size " + queue.size() + ")");
		} catch (InterruptedException e) {
			global.severe(e.getMessage());
		}
	}
	
	public void flush() {
		global.severe("To flush the queue (size " + queue.size() + ")");
		while (queue.isEmpty() == false);
		global.severe("Flushed the queue (size " + queue.size() + ")");
	}
	
	public void run() {
		for (;;) {
			if (queue.isEmpty() == false) {
				DataBlock db = queue.poll();
				data.position((int) db.getPosition());
				data.put(db.getBytes());
				global.severe("Write one block from the queue (size " + queue.size() + ")");
			} else {
//				global.severe("buffer is empty");
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}
	}
}
