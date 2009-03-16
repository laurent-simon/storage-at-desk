package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


public class ScsiResponse implements IResponse {
	
	private static Logger logger = Logger.getLogger(ScsiResponse.class);
	
	public int bidirectionalOverflow;
	public int bidirectionalUnderflow;
	public int overflow;
	public int underflow;
	
	public int response;
	public int status;
	public int length;
	public int ahsLength;
	public int tag;
	public int statSN;
	public int expCmdSN;
	public int maxCmdSN;
	public int expDataSN;
	public int bidirectionalResidualCount;
	public int basicResidualCount;
	
	public byte[] encap() throws IOException {
		byte[] header = new byte[ISCSI.Header_SIZE]; 
		logger.debug("encap starts");
		
		logger.trace("Bidirectional Overflow:	" + bidirectionalOverflow);
		logger.trace("Bidirectional Underflow:	" + bidirectionalUnderflow);
		logger.trace("Overflow:					" + overflow);
		logger.trace("Underflow:				" + underflow);
		logger.trace("iSCSI Response:			" + response);
		logger.trace("SCSI Status:				" + status);
		logger.trace("DataSegmentLength: 		" + length);
		logger.trace("Initiator Task Tag:		" + tag);
		logger.trace("StatSN:			        " + statSN);
		logger.trace("ExpCmdSN:             	" + expCmdSN);
		logger.trace("MaxCmdSN:       		    " + maxCmdSN);
		logger.trace("ExpDataSN:           		" + expDataSN);
		logger.trace("Bidi Residual Count: 		" + bidirectionalResidualCount);
		logger.trace("Residual Count:      		" + basicResidualCount);
		
		header[0] |= 0x00| ISCSI.SCSI_RSP;
		header[1] |= 0x80;
		if (bidirectionalOverflow != 0) header[1] |= 0x10;
		if (bidirectionalUnderflow != 0) header[1] |= 0x08;
		if (overflow != 0) header[1] |= 0x04;
		if (underflow != 0) header[1] |= 0x02;
		System.arraycopy(Util.intToByte4(response), 0, header, 2, 1);
		System.arraycopy(Util.intToByte4(status), 0, header, 3, 1);
		System.arraycopy(Util.intToByte4(ahsLength), 0, header, 4, 1);	
		System.arraycopy(Util.intToByte4(length), 0, header, 5, 3);	
		System.arraycopy(Util.intToByte4(tag), 0, header, 16, 4);          	// Tag
		System.arraycopy(Util.intToByte4(statSN), 0, header, 24, 4);	 
		System.arraycopy(Util.intToByte4(expCmdSN), 0, header, 28, 4);      // ExpCmdSN
		System.arraycopy(Util.intToByte4(maxCmdSN), 0, header, 32, 4);     	// MaxCmdSN
		System.arraycopy(Util.intToByte4(expDataSN), 0, header, 36, 4);
		System.arraycopy(Util.intToByte4(bidirectionalResidualCount), 0, header, 40, 4);
		System.arraycopy(Util.intToByte4(basicResidualCount), 0, header, 44, 4);	
		logger.debug("encap ends");
		return header;
	}
}
