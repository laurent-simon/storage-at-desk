package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


public class TaskRequest implements IRequest {

	private static Logger logger = Logger.getLogger(TaskRequest.class);

	int immediate;
	int function;
	int lun;
	int tag;
	int referenceTag;	    
	int cmdSN;
	int expStatSN;
	int refCmdSN;
	int expDataSN;

	public boolean decap(byte[] header) throws IOException {
		logger.trace("Decap task command request");

		if (Util.isNotEqual(logger, "OpCode", Util.getiSCSIOpCode(header[0]), ISCSI.TASK_CMD)) {
			return false;
		}

		immediate	= ((header[0]& 0x40) == 0x40)? 1 : 0;      		// Immediate bit
		function	= header[1]&0x0f;							// Function
		lun			= Util.decapInt(header, 8, 4);				// LUN
		tag			= Util.decapInt(header, 16, 4);				// Task Tag
		referenceTag = Util.decapInt(header, 20, 4);			// Reference Tag
		cmdSN		= Util.decapInt(header, 24, 4);				// CmdSN
		expStatSN	= Util.decapInt(header, 28, 4);				// ExpStatSN
		refCmdSN	= Util.decapInt(header, 32, 4);				// RefCmdSN
		expDataSN	= Util.decapInt(header, 36, 4);				// ExpDataSN

		// Later
//		Util.isNotEqual(global, "Byte 1, bits 3-4", (header[1]&0x18), 0);
//		Util.isNotEqual(global, "Bytes 2", header[2], 0);
//		Util.isNotEqual(global, "Bytes 3", header[3], 0);

		logger.trace("Immediate:         " + immediate);
		logger.trace("Function:          " + function);
		logger.trace("LUN:               " + lun);
		logger.trace("Initiator Task Tag:" + tag);
		logger.trace("Reference Tag:	 " + referenceTag);
		logger.trace("CmdSN:             " + cmdSN);
		logger.trace("ExpStatSN:         " + expStatSN);
		logger.trace("RefCmdSN:          " + refCmdSN);
		logger.trace("ExpDataSN:         " + expDataSN);

		return true;
	}
}
