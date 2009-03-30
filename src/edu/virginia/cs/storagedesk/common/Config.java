package edu.virginia.cs.storagedesk.common;

public class Config {
	
	public static String	DEBUG_LEVEL		= "INFO";   
	
	/*
	 * Storage Machine
	 */
	public static String	STORAGEMACHINE_PATH				= "\temp";
	public static String 	STORAGEMACHINE_FILE_PREFIX		= "StorageDesk";
	
	public static int		STORAGEMACHINE_NUM_CHUNKS		= 1;
	public static long		STORAGEMACHINE_CHUNK_SIZE		= 1024000000; // 1G
	
	public static long		STORAGEMACHINE_HEARTBEAT_TIME	= 600000; // 10 minutes

	/*
	 * Database
	 */
	public static String 	DB_USER_NAME 					= "storage@desk";
	public static String 	DB_PASSWORD 					= "sdisgood";
	public static String  	DB_HOST							= "queen.cs.virginia.edu";
	public static String	DB_NAME							= "storage@desk";
	public static String 	DB_URL 							= "jdbc:mysql://" + DB_HOST + "/" + DB_NAME;
	
	/*
	 * Volume Controller
	 */
	public static String VOLUMECONTROLLER_IP_ADDRESS  		= "128.143.69.86";
	
	/*
	 * Socket port
	 */
	public static int 	SD_SOCKET_PORT					= 1099;
	
	/*
	 * Target
	 */
	public static String TARGET_NAME   = "iqn.edu.virginia.cs.storagedesk:disk1";
	
	/*
	 * Volume
	 */
	public static int	VOLUME_NUM_COPIES			= 1;
	public static long 	VOLUME_CHUNK_SIZE			= 1024000000;
	
	/*
	 * ISCSI Server
	 */
	public static boolean ISCSISERVER_JOURNALING	= false;
	public static String  ISCSISERVER_PATH			= ".";
}
