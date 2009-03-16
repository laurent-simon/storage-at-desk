package edu.virginia.cs.storagedesk.common;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.storageserver.ScsiDataRequest;
import edu.virginia.cs.storagedesk.storageserver.ScsiReadyToTransferResponse;
import edu.virginia.cs.storagedesk.storageserver.ScsiRequest;
import edu.virginia.cs.storagedesk.storageserver.Session;


/**
 * @author hh4z
 *
 */
public class Disk {
	private static Logger logger = Logger.getLogger(Disk.class.toString());
	
	private int type = ISCSI.DISK_TYPE;
	
	private long numLUNs 	= ISCSI.DEFAULT_DISK_NUM_LUNS;
	private long numBlocks 	= ISCSI.DEFAULT_DISK_NUM_BLOCKS;
	private long blockSize 	= ISCSI.DEFAULT_DISK_BLOCK_SIZE;
	
	private byte[] id = new byte[ISCSI.SCSI_ID_LENGTH];
	
	private long lun;
	private long offset;
	private long numBytes;
	
	public Disk() {			
		if ((blockSize != 512) &&
			(blockSize != 1024) &&
			(blockSize != 2048) &&
			(blockSize != 4096)) {
			logger.error("Invalid block size " + blockSize + ". Choose one of 512, 1024, 2048, 4096.");
		}
		
//		need to check diskNumLuns < disk_max_luns
		
		logger.debug("Disk: " + numLUNs + " LUNs (" + numBlocks + " blocks " + 
				+ blockSize + " byte/block)");
		
		System.arraycopy(("iSCSI").getBytes(), 0, id, 0, 5);
	}
	
	public byte[] read(long position, int length) throws IOException {
		return null;
	}
	
	public boolean write(byte[] bytes, long position) throws IOException {
		return false;
	}
	
	public final ScsiRequest executeCommand(Session session, ScsiRequest scsiRequest) throws IOException {
		
		int lun = scsiRequest.lun>>32;
		
		int lba;
		int len;
		
		byte[] responseData = new byte[ISCSI.BUFFER_SIZE];
		int responseDataLength = (scsiRequest.cdb[4] & 0xff);
		
		// Return no device equivalent for LUN requested beyond available LUN
		if (lun > this.getNumLUNs()) {
			responseData = new byte[responseDataLength];
			responseData[0] = 0x1f;					// device type
			responseData[0] |= 0x60;				// peripheral qualifier
			scsiRequest.toDiskData = responseData;
			scsiRequest.fromDisk = 1;
			scsiRequest.length = responseDataLength + 1;
			scsiRequest.status = 0;
			return scsiRequest;
		}
		
		logger.debug("SCSI op 0x" + Integer.toString(scsiRequest.cdb[0] & 0xff,16) + " (LUN " + lun + ")");
		
		switch (scsiRequest.cdb[0] & 0xff) {
		
		case ISCSI.TEST_UNIT_READY:
			logger.debug("TEST UNIT READY");
			scsiRequest.status = 0;
			scsiRequest.length = 0;
			break;
			
		case ISCSI.INQUIRY:
			logger.debug("INQUIRY");
			responseData = new byte[responseDataLength];
			
			if ((scsiRequest.cdb[1] & 0x03) == 0) {
				responseData[0] = 0;                              // Peripheral Device Type
				//data[1] |= 0x80;                        // Removable Bit
				//data[2] |= 0x02;                          // ANSI-approved version
				responseData[2] |= 0x04;                          // SPC-2 complicance
				//data[3] |= 0x80;                        // AENC 
				//data[3] |= 0x40;                        // TrmIOP 
				//data[3] |= 0x20;                        // NormACA 
				responseData[3] |= 0x42;
				responseData[4] |= 59;
				responseData[7] |= 0x02;
				//data[7] |= 0x80;                        // Relative addressing 
//				data[7] |= 0x40;                          // WBus32 
//				data[7] |= 0x20;                          // WBus16
				//data[7] |= 0x10;                        // Sync 
				//data[7] |= 0x08;                        // Linked Commands
				//data[7] |= 0x04;                        // TransDis 
				//data[7] |= 0x02;                        // Tagged Command Queueing
				//data[7] |= 0x01;                        // SftRe
				System.arraycopy(("UVACS").getBytes(), 0, responseData, 8, ("UVACS").length());     // Vendor
				System.arraycopy(("iSCSI").getBytes(), 0, responseData, 16, ("iSCSI").length() );    		// Product ID
				responseData[32] = ISCSI.VERSION;    // Product Revision
			} else if ((scsiRequest.cdb[1] & 0x02) != 0) {
				responseData[1] |= 0x01;
				responseData[5] = 0;
			} else if ((scsiRequest.cdb[1] & 0x01) != 0) {
				// EVPD bit set
				if (scsiRequest.cdb[2] == 0x0) {
					responseDataLength = 7;
					responseData = new byte[responseDataLength];
					responseData[1] = 0x0;
					responseData[3] = 3;
					responseData[4] = 0x0;
					responseData[5] |= 0x80;
					responseData[6] |= 0x83;					
				} else if (scsiRequest.cdb[2] == 0x80) {
					responseDataLength = 8;
					responseData = new byte[responseDataLength];
					responseData[1] |= 0x80;
					responseData[3] = 4;
				} else if (scsiRequest.cdb[2] == 0x83) {
					responseDataLength = 32;
					responseData = new byte[responseDataLength];
					responseData[1] |= 0x83;
					responseData[3] = ISCSI.SCSI_ID_LENGTH + 4;
					responseData[4] = 0x01;
					responseData[5] = 0x01;
					responseData[7] = ISCSI.SCSI_ID_LENGTH;
					System.arraycopy(this.getId(), 0, responseData, 8, ISCSI.SCSI_ID_LENGTH);
				}
			} 
			
			scsiRequest.fromDiskData = new ByteBuffer[1];
			scsiRequest.fromDiskData[0] = ByteBuffer.allocate(responseData.length);
			scsiRequest.fromDiskData[0].put(responseData);
			scsiRequest.fromDisk = 1;
			scsiRequest.length = responseDataLength;
			scsiRequest.status = 0;
			break;
			
		case ISCSI.READ_CAPACITY:
			logger.debug("READ CAPACITY");
			responseData = new byte[8];
			logger.debug("Num of blocks " + this.getNumBlocks() + ", after shift 32 bit " + ((this.getNumBlocks() - 1) >> 32));
			if (((this.getNumBlocks() - 1) >> 32) > 0) {
				logger.debug("Set 0xFFFFFFFFh");
				responseData[0] = responseData[1] = responseData[2] = responseData[3] = -1;  //0xFFh
			} else {
				System.arraycopy(Util.intToByte4((int) this.getNumBlocks()-1), 0, responseData, 0, 4); // Max LBA
			}
			System.arraycopy(Util.intToByte4((int) this.getBlockSize()), 0, responseData, 4, 4); // Block Size
			scsiRequest.fromDiskData = new ByteBuffer[1];
			scsiRequest.fromDiskData[0] = ByteBuffer.allocate(responseData.length);
			scsiRequest.fromDiskData[0].put(responseData);
			scsiRequest.fromDisk = 1;
			scsiRequest.status = 0;
			scsiRequest.length = 8;
			break;
			
		case ISCSI.WRITE_6:
			logger.debug("WRITE 6");
			
			responseData = new byte[4];
			System.arraycopy(scsiRequest.cdb, 2, responseData, 0, 4);
			lba = Util.byte4ToInt(responseData);
			lba &= 0x001fffff;
			len = scsiRequest.cdb[4] & 0xff;
			if (len == 0) len = 256;
			
			logger.debug("Write 6 (lba " + lba + ", len " + len + " blocks)");
			
			ScsiRequest old = scsiRequest;
			
			scsiRequest = executeWriteCommand(session, scsiRequest, lba, len, lun);
			if (scsiRequest == null) {
				logger.error("Disk write failed");
				scsiRequest = old;
				scsiRequest.status = 0x01;
			}
			scsiRequest.length = 0; 
			break;
		
		case ISCSI.WRITE_10:
			logger.debug("WRITE 10");
			
			responseData = new byte[4];
			System.arraycopy(scsiRequest.cdb, 2, responseData, 0, 4);
			lba = Util.byte4ToInt(responseData);
			responseData = new byte[2];
			System.arraycopy(scsiRequest.cdb, 7, responseData, 0, 2);
			len = Util.byte4ToInt(responseData);
			
			logger.debug("Write 10 (lba " + lba + ", len " + len + " blocks)");
			
			old = scsiRequest;
			
			scsiRequest = executeWriteCommand(session, scsiRequest, lba, len, lun);
			if (scsiRequest == null) {
				logger.error("Disk write failed");
				scsiRequest = old;
				scsiRequest.status = 0x01;
			}
			
			break;
			
		case ISCSI.READ_6:
			logger.debug("READ 6");
			responseData = new byte[4];
			System.arraycopy(scsiRequest.cdb, 2, responseData, 0, 4);
			lba = Util.byte4ToInt(responseData);
			lba &= 0x001fffff;
			len = scsiRequest.cdb[4] & 0xff;
			if (len == 0) len = 256;
			
			logger.debug("Read 6 (lba " + lba + ", len " + len + " blocks)");
			
			old = scsiRequest;
			
			scsiRequest = executeReadCommand(scsiRequest, lba, len, lun);
			if (scsiRequest == null) {
				logger.error("Disk read failed");
				scsiRequest = old;
				scsiRequest.status = 0x01;
			}
			scsiRequest.fromDisk = 1; 
			
			break;
			
		case ISCSI.READ_10:
			logger.debug("READ 10");
			responseData = new byte[4];
			System.arraycopy(scsiRequest.cdb, 2, responseData, 0, 4);
			lba = Util.byte4ToInt(responseData);
			responseData = new byte[2];
			System.arraycopy(scsiRequest.cdb, 7, responseData, 0, 2);
			len = Util.byte4ToInt(responseData);
			
			logger.debug("Read 10 (lba " + lba + ", len " + len + " blocks)");
			
			old = scsiRequest;
			
			scsiRequest = executeReadCommand(scsiRequest, lba, len, lun);
			if (scsiRequest == null) {
				logger.error("Disk read failed");
				scsiRequest = old;
				scsiRequest.status = 0x01;
			}
			scsiRequest.fromDisk = 1; 
			
			break;
			
		case ISCSI.MODE_SENSE:
			logger.debug("MODE SENSE");			
			
			if ((scsiRequest.cdb[1] & 0x8) != 0) {
				responseData = new byte[4];
				responseData[3] = 0;
			} else {
				responseData = new byte[12];
				responseData[3] = 8;
				System.arraycopy(Util.intToByte4((int) this.getNumBlocks()), 0, responseData, 4, 4);
				System.arraycopy(Util.intToByte4((int) this.getBlockSize()), 1, responseData, 9, 3);
			}

			switch (scsiRequest.cdb[2] & 0x3f) {
			case 0x0:
				break;
			case 0x2:
				responseData = Util.appendByteArray(responseData, insertDisconnectModePage());
				break;
			case 0x3:
				responseData = Util.appendByteArray(responseData, insertFormatDeviceModePage());
				break;
			case 0x4:
				responseData = Util.appendByteArray(responseData, insertGeometryModePage());
				break;
			case 0x8:
				responseData = Util.appendByteArray(responseData, insertCachingModePage());
				break;
			case 0xa:
				responseData = Util.appendByteArray(responseData, insertControlModePage());
				break;
			case 0x1c:
				responseData = Util.appendByteArray(responseData, insertExceptionsControlModePage());
				break;
			case 0x3f:
				if (scsiRequest.expTransferLength == 192) { 
					responseData = Util.appendByteArray(responseData, insertDisconnectModePage());
					responseData = Util.appendByteArray(responseData, insertFormatDeviceModePage());
					responseData = Util.appendByteArray(responseData, insertGeometryModePage());
					responseData = Util.appendByteArray(responseData, insertCachingModePage());
					responseData = Util.appendByteArray(responseData, insertControlModePage());
					responseData = Util.appendByteArray(responseData, insertExceptionsControlModePage());
				}
				break;
			default:
				logger.error("Wrong code in Mode Sense");
			}

			responseData[0] = Util.intToByte4(responseData.length - 1)[3];
			
			scsiRequest.fromDiskData = new ByteBuffer[1];
			scsiRequest.fromDiskData[0] = ByteBuffer.allocate(responseData.length);
			scsiRequest.fromDiskData[0].put(responseData);
			scsiRequest.fromDisk = 1; 
			
			scsiRequest.length = responseData.length;
			scsiRequest.status = 0; 

			break;

		  case ISCSI.REPORT_LUNS:
			logger.debug("REPORT LUNS");
			responseData = new byte[scsiRequest.expTransferLength];
			System.arraycopy(Util.intToByte4((int) this.getNumLUNs()*8), 0, responseData, 0, 4);  // lun list length
			for (int i=0; i<this.getNumLUNs(); i++) {
			    responseData[8+8*i] = 0x0;    // single level addressing method 
			    responseData[8+8*i+1] |= i;    // lun
			}
			scsiRequest.status = 0x0;
			scsiRequest.length = scsiRequest.expTransferLength;
			scsiRequest.fromDisk = 1;
			scsiRequest.fromDiskData = new ByteBuffer[1];
			scsiRequest.fromDiskData[0] = ByteBuffer.allocate(responseData.length);
			scsiRequest.fromDiskData[0].put(responseData);
			break;

		   case ISCSI.SET_WINDOW:
			logger.debug("SET WINDOW");
			break;	

//		    case 0x26:
//			PRINT("0x%x: DEVICE_EXIT\n", args->tag);
//			done = 1;
//			args->status = 0;
//			args->length = 0;
//			break;
			
		   case ISCSI.SERVICE_ACTION_IN:
			   logger.debug("READ CAPACITY 16");
			   responseData = new byte[32];			   
			   System.arraycopy(Util.longToByte8(this.getNumBlocks()-1), 0, responseData, 0, 8); // Max LBA
			   System.arraycopy(Util.intToByte4((int) this.getBlockSize()), 0, responseData, 8, 4); // Block Size
			   scsiRequest.fromDiskData = new ByteBuffer[1];
			   scsiRequest.fromDiskData[0] = ByteBuffer.allocate(responseData.length);
			   scsiRequest.fromDiskData[0].put(responseData);
			   scsiRequest.fromDisk = 1;
			   scsiRequest.status = 0;
			   scsiRequest.length = 8;
			   break;
			   
		   default:
			   logger.error("Unknow Op " + (int) scsiRequest.cdb[0]);
		   break;	
		}
		
		return scsiRequest;
	}

	public final ScsiRequest executeReadCommand(ScsiRequest scsiRequest, 
											int lba, 
											int len, 
											int lun) 
									throws IOException {
										
		this.setLun(lun);
		this.setOffset(lba * this.getBlockSize());
		this.setNumBytes(len * this.getBlockSize());
		
		if (len == 0) {
			logger.debug("Read length is 0");
			return null;
		}
		
		if ((lba > (this.getNumBlocks() - 1)) || ((lba + len) > this.getNumBlocks())) {
			logger.error("Attempt to read beyond end of media");
			logger.error("Max lba " + (this.getNumBlocks() - 1) + ", request lba " + lba + ", len " + len);
			return null;
		}
		
		int count = (int) this.getNumBytes() / ISCSI.BUFFER_SIZE;
		if (this.getNumBytes() % ISCSI.BUFFER_SIZE != 0) count++;
		scsiRequest.fromDiskData = new ByteBuffer[count];		
		for (int i = 0; i < ((this.getNumBytes() % ISCSI.BUFFER_SIZE != 0) ? count-1: count); i++)
			scsiRequest.fromDiskData[i] = ByteBuffer.allocate(ISCSI.BUFFER_SIZE);
		if  (this.getNumBytes() % ISCSI.BUFFER_SIZE != 0) 
			scsiRequest.fromDiskData[count-1] = ByteBuffer.allocate((int) this.getNumBytes() % ISCSI.BUFFER_SIZE);
		
		int i = 0;
		long pos = this.getLun() * this.getNumBlocks() * this.getBlockSize() + this.getOffset();
		do {		
			scsiRequest.fromDiskData[i].put(read(pos, 
					(int) (((this.getNumBytes() % ISCSI.BUFFER_SIZE != 0) && (i == count - 1)) ? (this.getNumBytes() % ISCSI.BUFFER_SIZE) : ISCSI.BUFFER_SIZE)));
			pos += ISCSI.BUFFER_SIZE;
			i++;
		} while (i < count);
		
		scsiRequest.length = (int) this.getNumBytes();
		
		scsiRequest.status = 0;
		
		logger.debug("Disk read from LUN " + lun + ", offset " + this.getOffset() + ", " + this.getNumBytes() + " bytes");
		
		return scsiRequest;
	}
	
	public final ScsiRequest executeWriteCommand(Session session, 
											ScsiRequest scsiRequest, 
											int lba, 
											int len, 
											int lun) 
									throws IOException {
		this.setOffset(lba * this.getBlockSize());
		this.setNumBytes(len * this.getBlockSize());
		
		logger.debug("Disk: writing " + this.getNumBytes() + " bytes at byte " + this.getOffset());		
		
		if (transferData(session, scsiRequest, len))
			scsiRequest.status = 0;
		
		return scsiRequest;
	}
	
	public final boolean transferData(Session session, 
								ScsiRequest scsiRequest, 
								int len) 
					throws IOException {		
		ScsiDataRequest dataRequest = new ScsiDataRequest();
		
		ByteBuffer immediateData = ByteBuffer.allocate(scsiRequest.length);
		
		byte[] response;
		
		/*
		 * Read immediate data
		 */
		if (scsiRequest.length > 0 &&
				session.params.get("ImmediateData").valueList.containsKey("Yes")) {
			logger.debug("To read immediate data of " + immediateData.capacity() + " bytes");
			if (session.params.get("MaxRecvDataSegmentLength").valueList.containsKey("0")) {
				logger.error("MaxRecvDataSegmentLength should not be zero");
			}
			int readCount = 0;
			while (immediateData.hasRemaining()) {
				readCount += session.socketChannel.read(immediateData);
			}
			
			if (readCount != scsiRequest.length) {
				logger.error("Read immediate read failed, read " + readCount + " != " + scsiRequest.length);
			} else {
				logger.debug("Read " + scsiRequest.length + " bytes immediate write data");
				
//				Do nothing with immediate data for now
						
//				logger.debug("Write " + scsiRequest.length + " bytes immediate data");
			}
		}
		
		/*
		 * Read iSCSI data PDUs
		 */
		if (scsiRequest.toDiskLength < scsiRequest.expTransferLength) {
			boolean r2t = false;
			do {
				
//				Send R2T 
				
				if ((r2t == false) && 
						((session.params.get("InitialR2T").valueList.containsKey("Yes")) ||
								((!session.params.get("FirstBurstLength").valueList.containsKey("0")) &&
										(scsiRequest.toDiskLength >= 
											Integer.parseInt(session.params.get("FirstBurstLength").valueList.values().toArray()[0].toString()) *
											ISCSI.FIRST_BURST_UNITS)))) {
					
					logger.debug("To send R2T");					    	
					
					ScsiReadyToTransferResponse r2tResponse = new ScsiReadyToTransferResponse();
					
					if ((!session.params.get("FirstBurstLength").valueList.containsKey("0")) &&
							(scsiRequest.toDiskLength >= 
								Integer.parseInt(session.params.get("FirstBurstLength").valueList.values().toArray()[0].toString()) *
								ISCSI.FIRST_BURST_UNITS)) {
						logger.error("Initiator exceeded first burst");
						return false;
					}
					
					r2tResponse.tag = scsiRequest.tag;
					r2tResponse.expCmdSN = session.expCmdSN;
					r2tResponse.maxCmdSN = session.maxCmdSN;
					r2tResponse.statSN = session.statSN;
					r2tResponse.length = scsiRequest.expTransferLength - scsiRequest.fromDiskLength;
					r2tResponse.offset = scsiRequest.fromDiskLength;
					
					byte[] b = r2tResponse.encap();
					response = Util.addPadding(b, ISCSI.MSG_BYTE_ALIGN);
					
					logger.debug("Sending R2T RESPONSE: " + response.toString());
					
					ByteBuffer responseBuffer = ByteBuffer.wrap(response);
					while (responseBuffer.hasRemaining()) {
						session.socketChannel.write(responseBuffer);
					}
					
					logger.debug("Sending R2T RESPONSE OK");
					
					r2t = true;					
				}
				
//				Read iSCSI data PDU
//				logger.debug("Reading iscsi data pdus");
				dataRequest = readDataPDU(session);
				if (dataRequest == null) {
					logger.error("Read data PDU failed");
					return false;
				}
				
				logger.debug("Read data PDU (offset " + dataRequest.offset + ", length " + dataRequest.length + ")");
				
//				Check args
				
				
//				Read
				ByteBuffer data = ByteBuffer.allocate(dataRequest.length);
				
				while (data.hasRemaining()) {
					session.socketChannel.read(data);
				}
				
				session.disk.write(data.array(), this.getLun() * this.getNumBlocks() * this.getBlockSize() + this.getOffset());
				
				scsiRequest.toDiskLength += dataRequest.length;
				
//				logger.debug("Received " + scsiRequest.toDiskLength + " bytes, expected " + scsiRequest.expTransferLength + " bytes");
				
				logger.debug("Successfully tranferred " + data.capacity() + " bytes");
					
			} while (scsiRequest.toDiskLength < scsiRequest.expTransferLength);
		}
		
		logger.debug("Successfully tranferred " + scsiRequest.toDiskLength + " bytes write data");
		
		return true;	
	}
	
	public final ScsiDataRequest readDataPDU(Session session) throws IOException {
		ByteBuffer requestHeader = ByteBuffer.allocate(ISCSI.Header_SIZE);
		ScsiDataRequest request = new ScsiDataRequest();
		
		int count = 0;
		
		while (requestHeader.hasRemaining()) {
			count += session.socketChannel.read(requestHeader);
		}
		
		if (count != ISCSI.Header_SIZE) {										
			logger.error("Read incorrect header of data PDU");
			return null;
		} else if (logger.isTraceEnabled()){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.reset();
			baos.write(requestHeader.array());								
			logger.trace("IRequest header: " + baos.toString());
		}
		
		request.decap(requestHeader.array());
		
		return request;
	}
	
	public static byte[] insertDisconnectModePage()
	{
		char[] disconnect = {0x02, 0x0e, 0x80, 0x80, 0x00, 0x0a, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		
		byte[] data = new byte[16];
		for (int i = 0; i < data.length; i++) 
			data[i] |= disconnect[i];
		return data;
	}
	
	public static byte[] insertCachingModePage()
	{
		char caching[] = {0x08, 0x12, 0x14, 0x00, 0xff, 0xff, 0x00, 0x00,
				0xff, 0xff, 0xff, 0xff, 0x80, 0x14, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00};
		
		byte[] data = new byte[20];
		for (int i = 0; i < data.length; i++) 
			data[i] |= caching[i];
		return data;
	}
	
	public static byte[] insertControlModePage()
	{
		char ctrl[] = {0x0a, 0x0a, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x02, 0x4b};
		
		byte[] data = new byte[12];
		for (int i = 0; i < data.length; i++) 
			data[i] |= ctrl[i];
		return data;
	}
	
	public static byte[] insertExceptionsControlModePage()
	{
		char iec[] = {0x1c, 0xa, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00, 0x00};
		
		byte[] data = new byte[12];
		for (int i = 0; i < data.length; i++) 
			data[i] |= iec[i];
		return data;
	}
	
	public static byte[] insertFormatDeviceModePage()
	{
		char format[] = {0x03, 0x16, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x01, 0x00, 0x02, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00};
		byte[] data = new byte[24];
		for (int i = 0; i < data.length; i++) 
			data[i] |= format[i];
		return data;
	}
	
	public static byte[] insertGeometryModePage()
	{
		char geo[] = {0x04, 0x16, 0x00, 0x00, 0x01, 0x40, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x3a, 0x98, 0x00, 0x00};
		
		byte[] data = new byte[24];
		for (int i = 0; i < data.length; i++) 
			data[i] |= geo[i];
		return data;
	}
	
	
	public long getBlockSize() {
		return blockSize;
	}
	
	
	public void setBlockSize(long blockSize) {
		this.blockSize = blockSize;
	}
	
	
	public byte[] getId() {
		return id;
	}
	
	
	public void setId(byte[] id) {
		this.id = id;
	}
	
	
	public long getLun() {
		return lun;
	}
	
	
	public void setLun(long lun) {
		this.lun = lun;
	}
	
	
	public long getNumBlocks() {
		return numBlocks;
	}
	
	
	public void setNumBlocks(long numBlocks) {
		this.numBlocks = numBlocks;
	}
	
	
	public long getNumBytes() {
		return numBytes;
	}
	
	
	public void setNumBytes(long numBytes) {
		this.numBytes = numBytes;
	}
	
	
	public long getNumLUNs() {
		return numLUNs;
	}
	
	
	public void setNumLUNs(long numLUNs) {
		this.numLUNs = numLUNs;
	}
	
	
	public long getOffset() {
		return offset;
	}
	
	
	public void setOffset(long offset) {
		this.offset = offset;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
