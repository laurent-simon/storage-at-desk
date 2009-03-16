package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;

public class NopInResponse implements IResponse {
		
	public int length;
	public long lun;
	public int tag;
	public int transferTag;
	public int statSN;
	public int expCmdSN;
	public int maxCmdSN;
	
	public byte[] encap() throws IOException {
		byte[] header = new byte[ISCSI.Header_SIZE]; 
		
		header[0] |= 0x00| ISCSI.NOP_IN;
		header[1] |= 0x80;
		
		System.arraycopy(Util.intToByte4(length), 0, header, 4, 4);	
		System.arraycopy(Util.longToByte8(lun), 0, header, 8, 8);	
		System.arraycopy(Util.intToByte4(tag), 0, header, 16, 4);          	// Tag
		System.arraycopy(Util.intToByte4(transferTag), 0, header, 20, 4);	 
		System.arraycopy(Util.intToByte4(statSN), 0, header, 24, 4);      
		System.arraycopy(Util.intToByte4(expCmdSN), 0, header, 28, 4);     	
		System.arraycopy(Util.intToByte4(maxCmdSN), 0, header, 32, 4);     	
		
		return header;
	}
}
