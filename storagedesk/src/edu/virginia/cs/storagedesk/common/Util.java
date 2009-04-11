package edu.virginia.cs.storagedesk.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;

import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import org.apache.log4j.*;

public class Util {
//	private static Logger logger = Logger.getLogger(Util.class);
	
	public static byte[] addPadding(byte[] a, int b) {
		int length = a.length;
		int i = length % (b);
		if (i != 0) {
			length += b - i;
		}
		byte[] data = new byte[length];
		System.arraycopy(a, 0, data, 0, a.length);
		if (i != 0) {
			for (int j = b - i; j > 0; j--) {
				data[length - j] = '\u0000';
			}
		}
		return data;
	}
	
	public static byte[] appendByteArray(byte[] a, byte[] b) {
		byte[] z = new byte[a.length + b.length];
        System.arraycopy(a, 0, z, 0, a.length);
        System.arraycopy(b, 0, z, a.length, b.length);
        return z;
	}
	
	public int getSCSIOpCode(byte b) {
		String opCode = Integer.toHexString(b);
		opCode = opCode.substring(opCode.length() - 2);
		return Integer.parseInt(opCode);
	}
	
	public static int getiSCSIOpCode(byte b) {
		return (int) b & 0x3F;
	}
	
	public static int byte4ToInt(byte[] b) {
		int value = 0;
		for (int i = 0; i < b.length; i++) {
			int shift = (b.length - 1 - i) * 8;
			value += (b[i] & 0x000000FF) << shift;
		}
		return value;
	}
	
	public static byte[] intToByte4(int i) {
		byte[] b = new byte[4];
		b[0] = (byte) ((i >> 24) & 0x000000FF);
		b[1] = (byte) ((i >> 16) & 0x000000FF);
		b[2] = (byte) ((i >> 8)  & 0x000000FF);
		b[3] = (byte) ( i        & 0x00FF);
		return b;
	}
	
	public static byte[] longToByte8 (long l) throws IOException {		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeLong(l);
		dos.flush();
		return bos.toByteArray();
	}
	
	public static long byte8ToLong(byte[] bytes) throws IOException {
		return (new BigInteger(bytes)).longValue();     
	}
	
	public static int decapInt(byte[] bytes, int start, int len){
		byte[] temp = new byte[len];
		System.arraycopy(bytes, start, temp, 0, len);
		return byte4ToInt(temp);
	}
	
	public static long decapLong(byte[] bytes, int start, int len) throws IOException {
		byte[] temp = new byte[len];
		System.arraycopy(bytes, start, temp, 0, len);
		return byte8ToLong(temp);
	}
	
	public static boolean isNotEqual(Logger logger, String name, int v1, int v2) {
		if (v1 != v2) {
			logger.error("Bad " + name + ": got " + v1 + " but expected " + v2);
			return true;
		} else {
			return false;
		}
	}
	
	public static void setProperties(Logger logger) throws InvalidPropertiesFormatException, IOException {
		
		Properties properties = new Properties();
	    FileInputStream fis = new FileInputStream("storage@desk.properties.xml");
	    properties.loadFromXML(fis);
	    
	    for (Enumeration<Object> keys = properties.keys(); keys.hasMoreElements(); ) {
	    	String key = (String) keys.nextElement();
	    	logger.info(key + " = " + properties.getProperty(key));
	    	
	    	
	    	if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.VOLUMECONTROLLER_IP_ADDRESS") == 0) {
	    		Config.VOLUMECONTROLLER_IP_ADDRESS = properties.getProperty(key);
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.SD_SOCKET_PORT") == 0) {
	    		Config.SD_SOCKET_PORT = Integer.parseInt(properties.getProperty(key));
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.DISK_TYPE") == 0) {
	    		ISCSI.DISK_TYPE = Integer.parseInt(properties.getProperty(key)); // always set to VirtualDisk, only used by StorageServer
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.DEFAULT_DISK_NUM_LUNS") == 0) {
	    		ISCSI.DEFAULT_DISK_NUM_LUNS = Long.parseLong(properties.getProperty(key)); // only used by VirtualDisk (StorageServer)
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.DEFAULT_DISK_BLOCK_SIZE") == 0) {
	    		ISCSI.DEFAULT_DISK_BLOCK_SIZE = Long.parseLong(properties.getProperty(key)); // only used by VirtualDisk (StorageServer)
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.DEFAULT_DISK_NUM_BLOCKS") == 0) {
	    		ISCSI.DEFAULT_DISK_NUM_BLOCKS = Long.parseLong(properties.getProperty(key)); // only used by VirtualDisk (StorageServer)
	    	} 
	    	
	    	// Storage Machine
	    	else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.STORAGEMACHINE_PATH") == 0) {
	    		Config.STORAGEMACHINE_PATH = properties.getProperty(key); // used mainly by StorageMachine, but reference exists in StorageServer (only needed if not using VirtualDisk)
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.STORAGEMACHINE_FILE_PREFIX") == 0) {
	    		Config.STORAGEMACHINE_FILE_PREFIX = properties.getProperty(key); // used mainly by StorageMachine, but reference exists in StorageServer (only needed if not using VirtualDisk)
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.STORAGEMACHINE_NUM_CHUNKS") == 0) {
	    		Config.STORAGEMACHINE_NUM_CHUNKS = Integer.parseInt(properties.getProperty(key)); // used mainly by StorageMachine, but reference exists in StorageServer (only needed if not using VirtualDisk)
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.STORAGEMACHINE_CHUNK_SIZE") == 0) {
	    		Config.STORAGEMACHINE_CHUNK_SIZE = Long.parseLong(properties.getProperty(key)); // used mainly by StorageMachine, but reference exists in StorageServer (only needed if not using VirtualDisk)
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.STORAGEMACHINE_HEARTBEAT_TIME") == 0) {
	    		Config.STORAGEMACHINE_HEARTBEAT_TIME = Long.parseLong(properties.getProperty(key)); // only used by StorageMachine
	    	} 
	    	
	    	else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.TARGET_NAME") == 0) {
	    		Config.TARGET_NAME = properties.getProperty(key); // used by StorageServer (also referenced in database, default volume name)
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.VOLUME_CHUNK_SIZE") == 0) {
	    		Config.VOLUME_CHUNK_SIZE = Long.parseLong(properties.getProperty(key)); // used by VirtualDisk (StorageServer) when creating new volume
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.VOLUME_NUM_COPIES") == 0) {
	    		Config.VOLUME_NUM_COPIES = Integer.parseInt(properties.getProperty(key)); // used by StorageServer, creating new volume
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.TARGET_MAX_SESSIONS") == 0) {
	    		ISCSI.TARGET_MAX_SESSIONS = Integer.parseInt(properties.getProperty(key)); // used by StorageServer
	    	} 
	    	
	    	// ISCSI Server
	    	else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.ISCSISERVER_JOURNALING") == 0) {
	    		Config.ISCSISERVER_JOURNALING = Boolean.parseBoolean(properties.getProperty(key)); // never used
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.ISCSISERVER_PATH") == 0) {
	    		Config.ISCSISERVER_PATH = properties.getProperty(key); // never used
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.ISCSI_PORT") == 0) {
	    		ISCSI.PORT = Integer.parseInt(properties.getProperty(key)); // used by StorageServer
	    	}
	    	
	    	// REMOVE LATER
	    	else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.DISK_WRITE_BUFFER") == 0) {
	    		ISCSI.DISK_WRITE_BUFFER = Boolean.parseBoolean(properties.getProperty(key)); // never used
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.DISK_WRITE_BUFFER_SIZE") == 0) {
	    		ISCSI.DISK_WRITE_BUFFER_SIZE = Integer.parseInt(properties.getProperty(key)); // used in FileWriteBuffer (which is never actually used) and test for VirtualDiskWriteBuffer
	    	}
	    	
	    	// DATABASE INFO (only used by Volume Controller)
	    	else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.DB_USER_NAME") == 0) {
	    		Config.DB_USER_NAME = properties.getProperty(key);
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.DB_PASSWORD") == 0) {
	    		Config.DB_PASSWORD = properties.getProperty(key);
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.DB_HOST") == 0) {
	    		Config.DB_HOST = properties.getProperty(key);
	    		Config.DB_URL = "jdbc:mysql://" + Config.DB_HOST + "/" + Config.DB_NAME;
	    	} else if (key.compareToIgnoreCase("edu.virginia.cs.storagedesk.DB_NAME") == 0) {
	    		Config.DB_NAME = properties.getProperty(key);
	    		Config.DB_URL = "jdbc:mysql://" + Config.DB_HOST + "/" + Config.DB_NAME;
	    	}
	    	
	    	else {
	    		logger.error("Undefined property: " + key);
	    	}
	    }
	}
	
	// this function is never called and seemingly useless
	// all communication is done over RMI over (the default port) 1099
	// change Config.SD_SOCKET_PORT = 10000
	// if this function proves usefull at some point
	public static int getSDPortNumber(int volumeID, int replicaID, boolean read) {
		int port = Config.SD_SOCKET_PORT;
		port += volumeID*100 + replicaID*10 + (read?0:1);
		return port;
	}
	
	public static String getStackTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}
}
