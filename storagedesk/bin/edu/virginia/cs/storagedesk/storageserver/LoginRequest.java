package edu.virginia.cs.storagedesk.storageserver;


import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


/**
 * @author hh4z
 *
 */
public class LoginRequest implements IRequest {
	
	private static Logger logger = Logger.getLogger(LoginRequest.class);
	
	public int transit;
	public int cont;
	public int csg;
	public int nsg;
	public int maxVersion;
	public int minVersion;
	public int lengthAHS;
	public int length;
	public long isid;
	public int tsih;
	public int tag;
	public int cid;
	public int cmdSN;
	public int expStatSN;
	
	public boolean decap(byte[] header) throws IOException {
		
		logger.trace("Decap login request");
		
		if (Util.isNotEqual(logger, "OpCode", Util.getiSCSIOpCode(header[0]), ISCSI.LOGIN_CMD)) {
			return false;
		}
		
		transit		= ((header[1]& 0x80) != 0) ? 1 : 0;         // Transit
		cont 		= ((header[1]&0x40) != 0) ? 1 : 0;          // Continue
		csg 		= (header[1]&0x0c)>>2;                  	// CSG
		nsg 		= header[1]&0x03;                       	// NSG
		maxVersion	= header[2];                            	// Version-Max 
		minVersion	= header[3];                            	// Version-Min 
		lengthAHS 	= header[4];                            	// TotalAHSLength		
		length		= Util.decapInt(header, 5, 3) & 0x00ffffff; // Length
		isid		= Util.decapLong(header, 8, 6);				// ISID
		tsih		= Util.decapInt(header, 14, 2);				// TSIH
		tag			= Util.decapInt(header, 16, 4);				// Task Tag
		cid		 	= Util.decapInt(header, 20, 2);				// CID
		cmdSN		= Util.decapInt(header, 24, 4);				// CmdSN
		expStatSN	= Util.decapInt(header, 28, 4);				// ExpStatSN

		Util.isNotEqual(logger, "Byte 1, bits 2-3", ((header[1]&0x30)>>4), 0);
		Util.isNotEqual(logger, "Bytes 22-23", header[22], 0);
		Util.isNotEqual(logger, "Bytes 32-35", header[32], 0);
		Util.isNotEqual(logger, "Bytes 36-39", header[36], 0);
		Util.isNotEqual(logger, "Bytes 40-43", header[40], 0);
		Util.isNotEqual(logger, "Bytes 44-47", header[44], 0);
		
		logger.trace("Transit:           " + transit);
		logger.trace("Continue:          " + cont);
		logger.trace("CSG:               " + csg);
		logger.trace("NSG:               " + nsg);
		logger.trace("Version_min:       " + minVersion);
		logger.trace("Version_max:       " + maxVersion);
		logger.trace("TotalAHSLength:    " + lengthAHS);
		logger.trace("DataSegmentLength: " + length);
		logger.trace("ISID:              " + isid);
		logger.trace("TSIH:              " + tsih);
		logger.trace("Initiator Task Tag:" + tag);
		logger.trace("CID:               " + cid);
		logger.trace("CmdSN:             " + cmdSN);
		logger.trace("ExpStatSN:         " + expStatSN);

		return true;
	}
}
