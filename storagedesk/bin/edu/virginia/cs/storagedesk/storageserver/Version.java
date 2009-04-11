package edu.virginia.cs.storagedesk.storageserver;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.Util;

public class Version {
	private static Logger logger = Logger.getLogger(Version.class);
	
	private long number;	
	
	FileChannel channel;
	
	String filename;
	
    public Version(String name) {
    	filename = name;
		File file = new File(filename);
		if (file.exists() == false) {
			logger.error("Version file " + filename + " does not exist");
			return;
		}	    	
	}
	
	public synchronized long getVersionNumber() {
		try {
			File file = new File(filename);
			if (file.length() == 0) {
				logger.debug("Have yet set a version");
				return -1;
			}
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			number = raf.readLong();
			raf.close();
			logger.debug("read from file: " + number);
		} catch (IOException e) {
			logger.error(Util.getStackTrace(e));
			number = -1;
		} 
		return number;
	}
	
	public synchronized void setVersionNumber(Long newVer) {
		try {
			RandomAccessFile raf = new RandomAccessFile(new File(filename), "rw");
			raf.writeLong(newVer);
			raf.close();
		} catch (IOException e) {
			logger.error(Util.getStackTrace(e));
		} 
	}
	
	public synchronized long increment() {
		long num = -1;
		try {
			RandomAccessFile raf = new RandomAccessFile(new File(filename), "rw");
			num = raf.readLong();
			num++;
			raf.seek(0);
			raf.writeLong(num);
			raf.close();			
		} catch (IOException e) {
			logger.error(Util.getStackTrace(e));
		} 
		return num;
	}
}
