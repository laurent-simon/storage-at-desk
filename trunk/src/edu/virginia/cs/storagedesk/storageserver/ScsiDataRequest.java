package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


public class ScsiDataRequest implements IRequest {
	private static Logger logger = Logger.getLogger(ScsiDataRequest.class);
	
	public int finalBit;
	public int length;
	public int lun;
	public int tag;
	public int transferTag;
	public int expStatSN;	
	public int dataSN;
	public int offset;
	
	public boolean decap(byte[] header) throws IOException {	
		if (Util.isNotEqual(logger, "OpCode", Util.getiSCSIOpCode(header[0]), ISCSI.SCSI_DATA)) {
			return false;
		}
				
		finalBit	= header[1]&0x80;							// Final bit
		length		= Util.decapInt(header, 5, 3) & 0x00ffffff; // Length
		lun			= Util.decapInt(header, 8, 4);				// LUN
		tag			= Util.decapInt(header, 16, 4);				// Task Tag
		transferTag = Util.decapInt(header, 20, 4);				// Transfer Tag		
		expStatSN	= Util.decapInt(header, 28, 4);				// ExpStatSN
		dataSN	    = Util.decapInt(header, 36, 4);				// DataSN
		offset  	= Util.decapInt(header, 40, 4);				// Buffer Offset
		
		
//		Util.isNotEqual(global, "Byte 1, bits 3-4", (header[1]&0x18), 0);
//		Util.isNotEqual(global, "Bytes 2", header[2], 0);
//		Util.isNotEqual(global, "Bytes 3", header[3], 0);
				
		logger.trace("Final:             " + finalBit);
		logger.trace("DataSegmentLength: " + length);
		logger.trace("LUN:               " + lun);
		logger.trace("Initiator Task Tag:" + tag);
		logger.trace("Transfer Tag:      " + transferTag);		
		logger.trace("ExpStatSN:         " + expStatSN);
		logger.trace("DataSN:	         " + dataSN);
		logger.trace("Offset:	         " + offset);
		
		return true;
	}

}
