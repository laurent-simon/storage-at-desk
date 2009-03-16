package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


public class TextRequest implements IRequest {
	
	private static Logger logger = Logger.getLogger(TextRequest.class);
			
	public int immediate;
	public int finalBit;
	public int cont;
	public int length;
	public long lun;
	public int tag;
	public int transferTag;
	public int cmdSN;
	public int expStatSN;
	
	public boolean decap(byte[] header) throws IOException {
		
		logger.trace("Decap text command request");
		
		if (Util.isNotEqual(logger, "OpCode", Util.getiSCSIOpCode(header[0]), ISCSI.TEXT_CMD)) {
			return false;
		}
		
		immediate	= ((header[0]& 0x40)!= 0)? 1 : 0;      		// Immediate bit
		finalBit	= ((header[1]&0x80) != 0)? 1 : 0;			// Final bit
		cont 		= ((header[1]&0x40) != 0)? 1 : 0;       	// Continue bit
		length		= Util.decapInt(header, 5, 3) & 0x00ffffff; // Length
		lun			= Util.decapLong(header, 8, 8);				// LUN
		tag			= Util.decapInt(header, 16, 4);				// Task Tag
		transferTag = Util.decapInt(header, 20, 4);				// Transfer Tag
		cmdSN		= Util.decapInt(header, 24, 4);				// CmdSN
		expStatSN	= Util.decapInt(header, 28, 4);				// ExpStatSN
		
		Util.isNotEqual(logger, "Byte 1, bits 2-7", ((header[1]&0x00)>>4), 0);
		Util.isNotEqual(logger, "Bytes 2", header[2], 0);
		Util.isNotEqual(logger, "Bytes 3", header[3], 0);
		Util.isNotEqual(logger, "Bytes 4", header[4], 0);
		Util.isNotEqual(logger, "Bytes 8-11", header[8], 0);
		Util.isNotEqual(logger, "Bytes 12-15", header[12], 0);
		Util.isNotEqual(logger, "Bytes 32-35", header[32], 0);
		Util.isNotEqual(logger, "Bytes 36-39", header[36], 0);
		Util.isNotEqual(logger, "Bytes 40-43", header[40], 0);
		Util.isNotEqual(logger, "Bytes 44-47", header[44], 0);
		
		logger.trace("Immediate:         " + immediate);
		logger.trace("Final:             " + finalBit);
		logger.trace("Continue:          " + cont);
		logger.trace("DataSegmentLength: " + length);
		logger.trace("LUN:               " + lun);
		logger.trace("Initiator Task Tag:" + tag);
		logger.trace("Transfer Tag:      " + transferTag);
		logger.trace("CmdSN:             " + cmdSN);
		logger.trace("ExpStatSN:         " + expStatSN);
		
		return true;
	}
}
