package edu.virginia.cs.storagedesk.common;


/**
 * @author hh4z
 *
 */
public class ISCSI {
  
	
	public final static int VERSION 		= 0;
	
	public static int PORT   				= 3260; 
	
	public final static int Header_SIZE 	= 48;
	public final static int CDB_SIZE		= 16;	// commonly used CDBs
	public final static int MAX_AHS_SIZE	= 128;
	
	public final static int BUFFER_SIZE 	= 65536; //8192;
	
	public final static int MSG_BYTE_ALIGN  = 4;
	
	public final static int DEFAULT_IMMEDIATE_DATE	= 1;
	public final static int DEFAULT_INITIAL_R2T		= 1;
	public final static boolean DEFAULT_USE_PHASE_COLLAPSED_READ = false;
	public final static int MAX_BURST_UNITS			= 512;
	public final static int FIRST_BURST_UNITS		= 512;
	public final static int DATA_PDU_LENGTH_UNITS	= 512;
	
	/*
	 * Disk
	 */
	public static int DISK_TYPE						= 0;
	public final static int RAM_DISK				= 0;
	public final static int FILE_DISK				= 1;
	public final static int VIRTUAL_RAM_DISK		= 2;
	public final static int VIRTUAL_FILE_DISK		= 3;
	
	public final static int SCSI_ID_LENGTH			= 24;
	
	public static long DEFAULT_DISK_NUM_LUNS 		= 1;
	public static long DEFAULT_DISK_BLOCK_SIZE 		= 512; 		// in bytes
	public static long DEFAULT_DISK_NUM_BLOCKS 		= 2048*100; //16384; // 2048 * 50; // 50M
	public final static int DISK_MAX_LUNS			= 4;
	
	public static boolean DISK_WRITE_BUFFER			= true;
	public static int     DISK_WRITE_BUFFER_SIZE	= 1024;

	/*
	 * Target
	 */
	
	public final static int TARGET_INITIALIZING 	= 1;
	public final static int TARGET_INITIALIZED		= 2;
	public final static int TARGET_SHUTTING_DOWN	= 3;
	public final static int TARGET_SHUT_DOWN		= 4;
	
	public static int TARGET_MAX_SESSIONS			= 10;
	public final static int TARGET_MAX_QUEUE		= 20;
//	public final static int TARGET_MAX_IOV_LEN		= 32;
	public final static int TARGET_MAX_IMMEDIATE	= 65536;	
	

	/*
	 * Login Phase
	 */
	public final static int LOGIN_STATUS_SUCCESS          = 0;
	public final static int LOGIN_STATUS_REDIRECTION      = 1;
	public final static int LOGIN_STATUS_INITIATOR_ERROR  = 2;
	public final static int LOGIN_STATUS_TARGET_ERROR     = 3;
	public final static int LOGIN_STAGE_SECURITY          = 0;
	public final static int LOGIN_STAGE_NEGOTIATE         = 1;
	public final static int LOGIN_STAGE_FULL_FEATURE      = 3;


	/*
	 * Logout Phase
	 */
	public final static int LOGOUT_CLOSE_SESSION      = 0;
	public final static int LOGOUT_CLOSE_CONNECTION   = 1;
	public final static int LOGOUT_CLOSE_RECOVERY     = 2;
	public final static int LOGOUT_STATUS_SUCCESS     = 0;
	public final static int LOGOUT_STATUS_NO_CID      = 1;
	public final static int LOGOUT_STATUS_NO_RECOVERY = 2;
	public final static int LOGOUT_STATUS_FAILURE     = 3;

	/*
	 * Task Command 
	 */
	public final static int TASK_CMD_ABORT_TASK         = 1;
	public final static int TASK_CMD_ABORT_TASK_SET     = 2;
	public final static int TASK_CMD_CLEAR_ACA          = 3;
	public final static int TASK_CMD_CLEAR_TASK_SET     = 4;
	public final static int TASK_CMD_LOGICAL_UNIT_RESET = 5;
	public final static int TASK_CMD_TARGET_WARM_RESET  = 6;
	public final static int TASK_CMD_TARGET_COLD_RESET  = 7;
	public final static int TASK_CMD_TARGET_REASSIGN    = 8;
	
	public final static int TASK_RSP_FUNCTION_COMPLETE  = 0;
	public final static int TASK_RSP_NO_SUCH_TASK       = 1;
	public final static int TASK_RSP_NO_SUCH_LUN        = 2;
	public final static int TASK_RSP_STILL_ALLEGIANT    = 3;
	public final static int TASK_RSP_NO_FAILOVER        = 4;
	public final static int TASK_RSP_NO_SUPPORT	  	    = 5;
	public final static int TASK_RSP_AUTHORIZED_FAILED  = 6;
	public final static int TASK_RSP_REJECTED           = 255;
	public final static int TASK_QUAL_FUNCTION_EXECUTED = 0;
	public final static int TASK_QUAL_NOT_AUTHORIZED    = 1;
	
	/*
	 * OpCodes
	 */
	
	/* 
	 * Client to Server Message Opcode values 
	 */
	public final static int NOP_OUT				= 0x00;
	public final static int SCSI_CMD			= 0x01;
	public final static int TASK_CMD			= 0x02;
	public final static int LOGIN_CMD			= 0x03;
	public final static int TEXT_CMD			= 0x04;
	public final static int SCSI_DATA			= 0x05;
	public final static int LOGOUT_CMD			= 0x06;
	public final static int SNACK_CMD			= 0x10;

	/* 
	 * Server to Client Message Opcode values 
	 */
	public final static int NOP_IN				= 0x20;
	public final static int SCSI_RSP			= 0x21;
	public final static int TASK_RSP			= 0x22;
	public final static int LOGIN_RSP			= 0x23;
	public final static int TEXT_RSP			= 0x24;
	public final static int SCSI_DATA_RSP		= 0x25;
	public final static int LOGOUT_RSP			= 0x26;
	public final static int R2T_RSP				= 0x31;
	public final static int ASYNC_EVENT			= 0x32;
	public final static int REJECT_MSG			= 0x3f;
	
	/*
	 * Parameter
	 */
	public final static int PARAM_MAX_LEN 			 = 256;
	
	public final static int PARAM_TYPE_DECLARATIVE    = 1;
	public final static int PARAM_TYPE_DECLARE_MULTI  = 2; // for TargetName and TargetAddress
	public final static int PARAM_TYPE_NUMERICAL      = 3;
	public final static int PARAM_TYPE_NUMERICAL_Z    = 4;  // zero represents no limit
	public final static int PARAM_TYPE_BINARY_OR      = 5;
	public final static int PARAM_TYPE_BINARY_AND     = 6;
	public final static int PARAM_TYPE_LIST           = 7;
	
	/*
	 * SCSI
	 */
	public static final byte TEST_UNIT_READY       = 0x00;
	public static final byte REZERO_UNIT           = 0x01;
	public static final byte REQUEST_SENSE         = 0x03;
	public static final byte FORMAT_UNIT           = 0x04;
	public static final byte READ_BLOCK_LIMITS     = 0x05;
	public static final byte REASSIGN_BLOCKS       = 0x07;
	public static final byte READ_6                = 0x08;
	public static final byte WRITE_6               = 0x0a;
	public static final byte SEEK_6                = 0x0b;
	public static final byte READ_REVERSE          = 0x0f;
	public static final byte WRITE_FILEMARKS       = 0x10;
	public static final byte SPACE                 = 0x11;
	public static final byte INQUIRY               = 0x12;
	public static final byte RECOVER_BUFFERED_DATA = 0x14;
	public static final byte MODE_SELECT           = 0x15;
	public static final byte RESERVE               = 0x16;
	public static final byte RELEASE               = 0x17;
	public static final byte COPY                  = 0x18;
	public static final byte ERASE                 = 0x19;
	public static final byte MODE_SENSE            = 0x1a;
	public static final byte START_STOP            = 0x1b;
	public static final byte RECEIVE_DIAGNOSTIC    = 0x1c;
	public static final byte SEND_DIAGNOSTIC       = 0x1d;
	public static final byte ALLOW_MEDIUM_REMOVAL  = 0x1e;

	public static final byte SET_WINDOW            = 0x24;
	public static final byte READ_CAPACITY         = 0x25;
	public static final byte READ_10               = 0x28;
	public static final byte WRITE_10              = 0x2a;
	public static final byte SEEK_10               = 0x2b;
	public static final byte WRITE_VERIFY          = 0x2e;
	public static final byte VERIFY                = 0x2f;
	public static final byte SEARCH_HIGH           = 0x30;
	public static final byte SEARCH_EQUAL          = 0x31;
	public static final byte SEARCH_LOW            = 0x32;
	public static final byte SET_LIMITS            = 0x33;
	
	public static final short SERVICE_ACTION_IN	   = 0x9e;
	
	public static final short REPORT_LUNS		   = 0xa0;
	
	
}
