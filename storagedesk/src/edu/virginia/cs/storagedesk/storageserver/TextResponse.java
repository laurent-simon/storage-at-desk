package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


public class TextResponse implements IResponse {
	private static Logger logger = Logger.getLogger(TextResponse.class);
	
	public int finalBit;
	public int cont;
	public int length;
	public long lun;
	public int tag;
	public int transferTag;
	public int statSN;
	public int expCmdSN;
	public int maxCmdSN;
	
	public byte[] encap() throws IOException {
		
		byte[] header = new byte[ISCSI.Header_SIZE]; 
		
		logger.trace("Final:             " + finalBit);
		logger.trace("Continue:          " + cont);
		logger.trace("DataSegmentLength: " + length);
		logger.trace("LUN:               " + lun);
		logger.trace("Initiator Task Tag:" + tag);
		logger.trace("Transfer Tag:      " + transferTag);
		logger.trace("CmdSN:             " + statSN);
		logger.trace("ExpCmdSN:          " + expCmdSN);
		logger.trace("maxStatSN:         " + maxCmdSN);
		
		header[0] |= 0x00|ISCSI.TEXT_RSP;                           		// Opcode 
		if (finalBit != 0) header[1] |= 0x80;								// Final Bit 
		if (cont != 0) header[1] |= 0x40;                       			// Continue Bit
		System.arraycopy(Util.intToByte4(length), 0, header, 4, 4);			// Length
		System.arraycopy(Util.longToByte8(lun), 2, header, 8, 6);			// LUN
		System.arraycopy(Util.intToByte4(tag), 0, header, 16, 4);          	// Tag
		System.arraycopy(Util.intToByte4(transferTag), 0, header, 20, 4);	// Transfer Tag 
		System.arraycopy(Util.intToByte4(statSN), 0, header, 24, 4);        // StatSN
		System.arraycopy(Util.intToByte4(expCmdSN), 0, header, 28, 4);      // ExpCmdSN
		System.arraycopy(Util.intToByte4(maxCmdSN), 0, header, 32, 4);     	// MaxCmdSN
		
		return header;
	}
}
