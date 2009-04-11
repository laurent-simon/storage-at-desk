package edu.virginia.cs.storagedesk.storageserver;


import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


public class LogoutRequest implements IRequest {
	
	private static Logger logger = Logger.getLogger(LogoutRequest.class);
		
	public int immediate;
	public int reason;
	public int tag;
	public int cid;
	public int cmdSN;
	public int expStatSN;
	
	public boolean decap(byte[] header) throws IOException {
		logger.trace("Decap login request");
		
		if (Util.isNotEqual(logger, "OpCode", Util.getiSCSIOpCode(header[0]), ISCSI.LOGOUT_CMD)) {
			return false;
		}
		
		immediate	= ((header[1]&0x40) != 0) ? 1 : 0;      	// Immediate
		reason 		= header[1]&0x07;                       	// Reason
		tag			= Util.decapInt(header, 16, 4);				// Task Tag
		cid		 	= Util.decapInt(header, 20, 2);				// CID
		cmdSN		= Util.decapInt(header, 24, 4);				// CmdSN
		expStatSN	= Util.decapInt(header, 28, 4);				// ExpStatSN

		Util.isNotEqual(logger, "Byte 0, bit 0", (header[0]>>7), 0);
		Util.isNotEqual(logger, "Byte 1, bit 0", (header[1]>>7), 1);
		Util.isNotEqual(logger, "Bytes 2", header[2], 0);
		Util.isNotEqual(logger, "Bytes 3", header[3], 0);
		Util.isNotEqual(logger, "Bytes 4-7", header[4], 0);
		Util.isNotEqual(logger, "Bytes 8-11", header[8], 0);
		Util.isNotEqual(logger, "Bytes 12-13", header[12], 0);
		Util.isNotEqual(logger, "Bytes 22-23", header[22], 0);
		Util.isNotEqual(logger, "Bytes 32-35", header[32], 0);
		Util.isNotEqual(logger, "Bytes 36-39", header[36], 0);
		Util.isNotEqual(logger, "Bytes 40-43", header[40], 0);
		Util.isNotEqual(logger, "Bytes 44-47", header[44], 0);
		
		logger.trace("Immediate:         " + immediate);
		logger.trace("Reason:          	 " + reason);
		logger.trace("Initiator Task Tag:" + tag);
		logger.trace("CID:               " + cid);
		logger.trace("CmdSN:             " + cmdSN);
		logger.trace("ExpStatSN:         " + expStatSN);

		return true;
	}
}
