package edu.virginia.cs.storagedesk.storageserver;

import edu.virginia.cs.storagedesk.common.Disk;
import edu.virginia.cs.storagedesk.common.FileDisk;
import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Config;
import edu.virginia.cs.storagedesk.common.Util;

import java.util.concurrent.*;
import java.net.*;
import java.nio.channels.*;
import java.io.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author hh4z
 *
 */
public class StorageServer {
	/*
	 * Private
	 */
	private static Logger logger = Logger.getLogger(StorageServer.class);

	private int state = ISCSI.TARGET_SHUT_DOWN;
	private String targetName = Config.TARGET_NAME;

	private Disk disk;
	private int diskType;

	private ServerSocketChannel serverSocketChannel;
	private ExecutorService threadPool;

	public StorageServer (String name, int port, int poolSize, int type, int numCopies) throws IOException {

		this.targetName = name;
		this.diskType = type;

		switch (this.diskType) {
		case ISCSI.RAM_DISK:
			logger.info("RAM disk");
//			disk = new RamDisk();
			break;

		case ISCSI.FILE_DISK:
			logger.info("File disk");
			disk = new FileDisk(0,
								Config.STORAGEMACHINE_PATH,
								Config.STORAGEMACHINE_FILE_PREFIX,
								Config.STORAGEMACHINE_NUM_CHUNKS *  Config.STORAGEMACHINE_CHUNK_SIZE);
			break;

		case ISCSI.VIRTUAL_RAM_DISK:
		case ISCSI.VIRTUAL_FILE_DISK:
			logger.info("Virtual disk");
			disk = new VirtualDisk(numCopies);
			break;

		default:
			logger.error("Wrong disk type");
		break;
		}

		if ((state == ISCSI.TARGET_INITIALIZING) ||
				(state == ISCSI.TARGET_INITIALIZED)) {
			logger.error("duplicate target init attempted");
			System.exit(-1);
		}
		state = ISCSI.TARGET_INITIALIZING;
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(ISCSI.PORT));
		threadPool = Executors.newFixedThreadPool(ISCSI.TARGET_MAX_SESSIONS);
		logger.info("Target: " + targetName);
		state = ISCSI.TARGET_INITIALIZED;	
	}

	public void serve() {
		try {
			while (true) {
				SocketChannel sc = serverSocketChannel.accept();
				sc.configureBlocking(true);
				threadPool.execute(new TargetThread(sc, state, targetName, disk, diskType));
			}
		} catch (IOException ex) {

		}
	}

	public static void main(String[] args) throws IOException {

		InetAddress addr = InetAddress.getLocalHost();
		// Get IP Address
		String ip = addr.getHostAddress();			
		// Get hostname
		String hostname = addr.getHostName();
		System.setProperty("logfile.name", "StorageServer."+
				hostname + "." +
				ip + ".log");

		PropertyConfigurator.configure("storage@desk.log4j.properties");

//		Load properties
		Util.setProperties(logger);

		StorageServer target = new StorageServer(Config.TARGET_NAME, ISCSI.PORT, ISCSI.TARGET_MAX_SESSIONS, ISCSI.DISK_TYPE, Config.VOLUME_NUM_COPIES);
		target.serve();
	}
}
