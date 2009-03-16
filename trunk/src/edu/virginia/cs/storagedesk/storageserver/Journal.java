package edu.virginia.cs.storagedesk.storageserver;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Journal {
	private static Logger logger = Logger.getLogger(Journal.class);
	
	private ArrayList<Long> queue = new ArrayList<Long>();
	
	public Journal(String jdataPath) {
		init(jdataPath);
	}
	
	public synchronized void init(String jdataPath) {
		File jdata = new File(jdataPath);
		File[] vers = jdata.listFiles(filter);
		
		for (File f : vers) {
			Long ver = Long.parseLong(f.getName().substring(0, f.getName().indexOf(".")));
			queue.add(ver);
			logger.debug("Add " + ver + " to the journal queue");
		}
	}
	
	public synchronized int size() {
//		logger.debug("The size of the journal queue is " + queue.size());
		return queue.size();
	}
	
	public synchronized Long[] get() {
		logger.debug("To get the queue");
		Long[] q = new Long[queue.size()];
		for (int i = 0; i < q.length; i++) {
			q[i] = queue.get(i);
		}
		logger.debug("get returns");
		return q;
	}
	
	public synchronized boolean add(Long entry) {
		logger.debug("Add " + entry + " to the journal queue");
		queue.add(entry);
		return true;
	}
	
	public synchronized boolean remove(Long entry) {
		logger.debug("Remove " + entry + " from the journal queue");
		queue.remove(entry);
		return true;
	}
	
	FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".aux");
        }
    };
}
