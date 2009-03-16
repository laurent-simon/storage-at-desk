package edu.virginia.cs.storagedesk.storageserver;


import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;

public class ScsiRequest implements IRequest{
	
	private static Logger logger = Logger.getLogger(ScsiRequest.class);
	
	private static final long serialVersionUID = 0;
	
	public int immediate;
	public int finalBit;
	public int fromDisk;
	public int toDisk;
	public int attr;
//	Need this to support CDBs longer than 16 bytes
	public int ahsLength;
	public int length;
	public int lun;
	public int tag;
	public int expTransferLength;
	public int bidirectionTransferLength;
	public int cmdSN;
	public int expStatSN;
	public byte[] cdb = new byte[ISCSI.CDB_SIZE];	    
	public byte[] ahs = new byte[ISCSI.MAX_AHS_SIZE];
	public byte[] toDiskData = new byte[ISCSI.BUFFER_SIZE];
	public int toDiskLength;
	public ByteBuffer fromDiskData[];
	public int fromDiskLength;
	public int status;
	
	public boolean decap(byte[] header) throws IOException {
		logger.debug("Decap scsi command request");
		
		if (Util.isNotEqual(logger, "OpCode", Util.getiSCSIOpCode(header[0]), ISCSI.SCSI_CMD)) {
			return false;
		}
		
		immediate	= ((header[0]& 0x40)!= 0)? 1 : 0;      		// Immediate bit
		finalBit	= ((header[1]&0x80) != 0)? 1 : 0;			// Final bit
		fromDisk 	= ((header[1]&0x40) != 0)? 1 : 0;       	// Input bit
		toDisk 		= ((header[1]&0x20) != 0)? 1 : 0;       	// Output bit
		attr		= header[1]&0x07;							// ATTR
		ahsLength	= header[4];								// AHS Length
		length		= Util.decapInt(header, 5, 3) & 0x00ffffff; // Length
		lun			= Util.decapInt(header, 8, 4);				// LUN
		tag			= Util.decapInt(header, 16, 4);				// Task Tag
		expTransferLength = Util.decapInt(header, 20, 4);			// Transfer Length
		cmdSN		= Util.decapInt(header, 24, 4);				// CmdSN
		expStatSN	= Util.decapInt(header, 28, 4);				// ExpStatSN
		System.arraycopy(header, 32, cdb, 0, ISCSI.CDB_SIZE);	// CDB
		
		Util.isNotEqual(logger, "Byte 1, bits 3-4", (header[1]&0x18), 0);
		Util.isNotEqual(logger, "Bytes 2", header[2], 0);
		Util.isNotEqual(logger, "Bytes 3", header[3], 0);
		
		logger.trace("Immediate:         " + immediate);
		logger.trace("Final:             " + finalBit);
		logger.trace("Input:	         " + fromDisk);
		logger.trace("Output:	         " + toDisk);
		logger.trace("DataSegmentLength: " + length);
		logger.trace("LUN:               " + lun);
		logger.trace("Initiator Task Tag:" + tag);
		logger.trace("Transfer Length:   " + expTransferLength);
		logger.trace("CmdSN:             " + cmdSN);
		logger.trace("ExpStatSN:         " + expStatSN);
		logger.trace("CDB:		         " + cdb.toString());
		
		logger.debug("decap finishes");
		
		return true;
	}
}
