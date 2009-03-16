package edu.virginia.cs.storagedesk.storageserver;


import java.io.IOException;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;


/**
 * @author hh4z
 *
 */
public class LoginResponse implements IResponse {

	private static Logger logger = Logger.getLogger(LoginRequest.class);
			
	public int transit;
	public int cont;
	public int csg;
	public int nsg;
	public int maxVersion;
	public int activeVersion;
	public int lengthAHS;
	public int length;
	public long isid;
	public int tsih;
	public int tag;
	public int statSN;
	public int expCmdSN;
	public int maxCmdSN;
	public int statusClass;
	public int statusDetail;
	
	public byte[] encap() throws IOException {
		
		byte[] header = new byte[ISCSI.Header_SIZE]; 
		
		logger.trace("Encap login response");
		logger.trace("Transit:           " + transit);
		logger.trace("Continue:          " + cont);
		logger.trace("CSG:               " + csg);
		logger.trace("NSG:               " + nsg);
		logger.trace("Version_min:       " + maxVersion);
		logger.trace("Version_max:       " + activeVersion);
		logger.trace("TotalAHSLength:    " + lengthAHS);
		logger.trace("DataSegmentLength: " + length);
		logger.trace("ISID:              " + isid);
		logger.trace("TSIH:              " + tsih);
		logger.trace("Task Tag:          " + tag);
		logger.trace("StatSN:            " + statSN);
		logger.trace("ExpCmdSN:          " + expCmdSN);
		logger.trace("MaxCmdSN:          " + maxCmdSN);
		logger.trace("StatusClass:       " + statusClass);
		logger.trace("StatusDetail:      " + statusDetail);
		
		header[0] |= 0x00|ISCSI.LOGIN_RSP;                           	// Opcode 
		if (transit != 0) header[1] |= 0x80;                     		// Transit 
		if (cont != 0) header[1] |= 0x40;                       		// Continue
		header[1] |= ((csg)<<2)&0x0c;                      				// CSG
		header[1] |= (nsg)&0x03;                           				// NSG
		header[2] |= (maxVersion) & 0x0f;                           	// Version-max
		header[3] |= (activeVersion) & 0x0f;                        	// Version-active
		header[4] |= (lengthAHS) & 0x0f;                             	// TotalAHSLength
		System.arraycopy(Util.intToByte4(length), 0, header, 4, 4);		// Length
		System.arraycopy(Util.longToByte8(isid), 2, header, 8, 6);		// ISID
		System.arraycopy(Util.intToByte4(tsih), 2, header, 14, 2);  	// TSIH
		System.arraycopy(Util.intToByte4(tag), 0, header, 16, 4);       // Tag 
		System.arraycopy(Util.intToByte4(statSN), 0, header, 24, 4);    // StatRn
		System.arraycopy(Util.intToByte4(expCmdSN), 0, header, 28, 4);  // ExpCmdSN
		System.arraycopy(Util.intToByte4(maxCmdSN), 0, header, 32, 4);  // MaxCmdSN
		header[36] |= (statusClass) & 0x0f;                         	// Status-Class
		header[37] |= (statusDetail) & 0x0f;                        	// Status-Detail
		
		return header;
	}
}
