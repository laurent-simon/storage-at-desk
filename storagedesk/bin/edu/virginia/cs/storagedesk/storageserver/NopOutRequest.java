package edu.virginia.cs.storagedesk.storageserver;

import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;

public class NopOutRequest implements IRequest {

	private static Logger logger = Logger.getLogger(NopOutRequest.class);
			
	public int immediate;
	public int length;
	public long lun;
	public int tag;
	public int transferTag;
	public int cmdSN;
	public int expStatSN;
	
	public boolean decap(byte[] header) throws IOException {
		
		logger.debug("Decap NOPOUT request");
		
		if (Util.isNotEqual(logger, "OpCode", Util.getiSCSIOpCode(header[0]), ISCSI.NOP_OUT)) {
			return false;
		}
		
		immediate   = ((header[0] & 0x40) == 0x40) ? 1: 0;
		length		= Util.decapInt(header, 4, 4); // Length
		logger.debug("length = " + length);
		lun			= Util.decapLong(header, 8, 8);				
		tag			= Util.decapInt(header, 16, 4);				// Task Tag
		transferTag	= Util.decapInt(header, 20, 4);				
		cmdSN		= Util.decapInt(header, 24, 4);				
		expStatSN	= Util.decapInt(header, 28, 4);	

		return true;
	}
	
	
}
