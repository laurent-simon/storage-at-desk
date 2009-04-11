package edu.virginia.cs.storagedesk.common;

import static java.util.logging.Logger.global;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ArrayBlockingQueue;

public class FileWriteBuffer implements Runnable{
	
	public ArrayBlockingQueue<DataBlock> queue;
	
	private static FileChannel data;
	
	public FileWriteBuffer(FileChannel bb) {
		data = bb;
		queue = new ArrayBlockingQueue<DataBlock>(ISCSI.DISK_WRITE_BUFFER_SIZE, true);
	}
	
	public void add(DataBlock db){
		try {
			queue.put(db);
			global.severe("Add the data block to the queue (size " + queue.size() + ")");
		} catch (InterruptedException e) {
			global.severe(e.getMessage());
		}
	}
	
	public void remove() {
		DataBlock db = queue.poll();
		if (db != null) {
			try {
				data.position((int) db.getPosition());
				data.write(ByteBuffer.wrap(db.getBytes()));
			} catch (IOException e) {
				global.severe(e.getMessage());
			}
		}
	}
	
	public void flush() {
		global.info("To flush the queue (size " + queue.size() + ")");
		while (queue.isEmpty() == false) {
			remove();
		}
		global.severe("Flushed the queue (size " + queue.size() + ")");
	}
	
	public void run(){
		for (;;) {
			if (queue.isEmpty() == false) {
				remove();				
				global.severe("Write one block from the queue (size " + queue.size() + ")");
			} else {
//				global.severe("buffer is empty");
				try {
					Thread.sleep(0, 1);
				} catch (InterruptedException e) {
					global.severe(e.getMessage());
				}
			}			
		}
	}
}
