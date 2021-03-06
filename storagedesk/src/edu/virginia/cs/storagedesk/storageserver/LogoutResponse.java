package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


public class LogoutResponse implements IResponse{

	private static Logger logger = Logger.getLogger(LogoutResponse.class);
	
	public int response;
	public int length;
	public int tag;
	public int statSN;
	public int expCmdSN;
	public int maxCmdSN;
	public int time2Wait;
	public int time2Retain;
	
	public byte[] encap() throws IOException {
		byte[] header = new byte[ISCSI.Header_SIZE]; 
		
		logger.trace("Encap login response");
		logger.trace("IResponse:          " + response);
		logger.trace("DataSegmentLength: " + length);
		logger.trace("Task Tag:          " + tag);
		logger.trace("StatSN:            " + statSN);
		logger.trace("ExpCmdSN:          " + expCmdSN);
		logger.trace("MaxCmdSN:          " + maxCmdSN);
		logger.trace("Time2Wait:         " + time2Wait);
		logger.trace("Time2Retain:       " + time2Retain);
		
		header[0] |= 0x00|ISCSI.LOGOUT_RSP;                           		// Opcode 
		header[1] |= 0x80;                     								// Reserved 		
		header[2] |= response;                           					// IResponse
		System.arraycopy(Util.intToByte4(length), 0, header, 4, 4);			// Length		
		System.arraycopy(Util.intToByte4(tag), 0, header, 16, 4);          	// Tag 
		System.arraycopy(Util.intToByte4(statSN), 0, header, 24, 4);        // StatRn
		System.arraycopy(Util.intToByte4(expCmdSN), 0, header, 28, 4);      // ExpCmdSN
		System.arraycopy(Util.intToByte4(maxCmdSN), 0, header, 32, 4);     	// MaxCmdSN
		System.arraycopy(Util.intToByte4(time2Wait), 0, header, 40, 2);     // Time2Wait
		System.arraycopy(Util.intToByte4(time2Retain), 0, header, 42, 2);  	// Time2Retain
		
		return header;
	}
}
