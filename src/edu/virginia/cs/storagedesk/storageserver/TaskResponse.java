package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


public class TaskResponse implements IResponse {
		
	private static Logger logger = Logger.getLogger(TaskResponse.class);
	
	public int response;
	public int length;
	public int tag;
	public int statSN;
	public int expCmdSN;
	public int maxCmdSN;
	
	public byte[] encap() throws IOException {
		byte[] header = new byte[ISCSI.Header_SIZE]; 
		
		logger.trace("iSCSI Response:			" + response);
		logger.trace("DataSegmentLength: 		" + length);
		logger.trace("Initiator Task Tag:		" + tag);
		logger.trace("StatSN:			        " + statSN);
		logger.trace("ExpCmdSN:             	" + expCmdSN);
		logger.trace("MaxCmdSN:       		    " + maxCmdSN);
		
		header[0] |= 0x00| ISCSI.TASK_RSP;
		header[1] |= 0x80;
		System.arraycopy(Util.intToByte4(response), 0, header, 2, 1);
		System.arraycopy(Util.intToByte4(length), 0, header, 5, 3);	
		System.arraycopy(Util.intToByte4(tag), 0, header, 16, 4);          	// Tag
		System.arraycopy(Util.intToByte4(statSN), 0, header, 24, 4);	 
		System.arraycopy(Util.intToByte4(expCmdSN), 0, header, 28, 4);      // ExpCmdSN
		System.arraycopy(Util.intToByte4(maxCmdSN), 0, header, 32, 4);     	// MaxCmdSN
		
		return header;
	}
}
