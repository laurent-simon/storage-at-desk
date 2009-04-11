package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


public class ScsiDataResponse implements IResponse {
	
	private static Logger logger = Logger.getLogger(ScsiDataResponse.class);
	
	public int finalBit;
	public int ack;
	public int overflow;
	public int underflow;
	
	public int s;
	public int status;
	public int length;
	public int lun;
	public int tag;
	public int transferTag;
	public int statSN;	
	public int expCmdSN;
	public int maxCmdSN;
	public int dataSN;
	public int offset;
	public int residualCount;
	
	public byte[] encap() throws IOException {
		byte[] header = new byte[ISCSI.Header_SIZE]; 
		
		logger.trace("Final:					" + finalBit);
		logger.trace("Acknowledge:				" + ack);
		logger.trace("Overflow:					" + overflow);
		logger.trace("Underflow:				" + underflow);
		logger.trace("S bit:					" + s);
		logger.trace("SCSI Status:				" + status);
		logger.trace("DataSegmentLength: 		" + length);
		logger.trace("Initiator Task Tag:		" + tag);
		logger.trace("Transfer Tag:				" + transferTag);
		logger.trace("StatSN:			        " + statSN);
		logger.trace("ExpCmdSN:             	" + expCmdSN);
		logger.trace("MaxCmdSN:       		    " + maxCmdSN);
		logger.trace("DataSN:           		" + dataSN);
		logger.trace("Buffer Offset:	 		" + offset);
		logger.trace("Residual Count:      		" + residualCount);
		
		header[0] |= 0x00| ISCSI.SCSI_DATA_RSP;
		if (finalBit != 0) header[1] |= 0x80;
		if (ack != 0) header[1] |= 0x40;		
		if (overflow != 0) header[1] |= 0x04;
		if (underflow != 0) header[1] |= 0x02;
//		Windows initiator needs to ingore s
//		if (s != 0) {
			header[1] |= 0x01;
			System.arraycopy(Util.intToByte4(status), 0, header, 3, 1);
//		}
		System.arraycopy(Util.intToByte4(length), 0, header, 4, 4);
		System.arraycopy(Util.intToByte4(lun), 0, header, 9, 2);
		System.arraycopy(Util.intToByte4(tag), 0, header, 16, 4);          	// Tag
		System.arraycopy(Util.intToByte4(transferTag), 0, header, 20, 4);          	// Tag
		System.arraycopy(Util.intToByte4(statSN), 0, header, 24, 4);	 
		System.arraycopy(Util.intToByte4(expCmdSN), 0, header, 28, 4);      // ExpCmdSN
		System.arraycopy(Util.intToByte4(maxCmdSN), 0, header, 32, 4);     	// MaxCmdSN
		System.arraycopy(Util.intToByte4(dataSN), 0, header, 36, 4);
		System.arraycopy(Util.intToByte4(offset), 0, header, 40, 4);
		if (s != 0)
			System.arraycopy(Util.intToByte4(residualCount), 0, header, 44, 4);	
		
		return header;
	}
}
