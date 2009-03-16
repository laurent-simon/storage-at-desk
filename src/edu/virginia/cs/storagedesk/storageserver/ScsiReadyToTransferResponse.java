package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


public class ScsiReadyToTransferResponse implements IResponse {

	private static Logger logger = Logger.getLogger(ScsiReadyToTransferResponse.class);
			
	public int ahsLength;
	public int lun;
	public int tag;
	public int transferTag;
	public int statSN;	
	public int expCmdSN;
	public int maxCmdSN;
	public int r2tSN;
	public int offset;
	public int length;
	
	public byte[] encap() throws IOException {
		logger.debug("encap starts");
		
		byte[] header = new byte[ISCSI.Header_SIZE]; 
		
		logger.trace("DataSegmentLength: 		" + length);
		logger.trace("AHSLength: 				" + ahsLength);
		logger.trace("Initiator Task Tag:		" + tag);
		logger.trace("Transfer Tag:				" + transferTag);
		logger.trace("StatSN:			        " + statSN);
		logger.trace("ExpCmdSN:             	" + expCmdSN);
		logger.trace("MaxCmdSN:       		    " + maxCmdSN);
		logger.trace("R2TSN:	           		" + r2tSN);
		logger.trace("Buffer Offset:	 		" + offset);
				
		header[0] |= 0x00| ISCSI.R2T_RSP;
		header[1] |= 0x80;		
		System.arraycopy(Util.intToByte4(ahsLength), 0, header, 4, 4);
		System.arraycopy(Util.intToByte4(lun), 0, header, 9, 2);
		System.arraycopy(Util.intToByte4(tag), 0, header, 16, 4);          	// Tag
		System.arraycopy(Util.intToByte4(transferTag), 0, header, 20, 4);          	// Tag
		System.arraycopy(Util.intToByte4(statSN), 0, header, 24, 4);	 
		System.arraycopy(Util.intToByte4(expCmdSN), 0, header, 28, 4);      // ExpCmdSN
		System.arraycopy(Util.intToByte4(maxCmdSN), 0, header, 32, 4);     	// MaxCmdSN
		System.arraycopy(Util.intToByte4(r2tSN), 0, header, 36, 4);
		System.arraycopy(Util.intToByte4(offset), 0, header, 40, 4);
		System.arraycopy(Util.intToByte4(length), 0, header, 44, 4);	
		
		logger.debug("encap ends");
		
		return header;
	}

}
