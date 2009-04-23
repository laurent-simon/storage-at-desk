package edu.virginia.cs.storagedesk.storageserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.Disk;
import edu.virginia.cs.storagedesk.common.FileDisk;
import edu.virginia.cs.storagedesk.common.ISCSI;
import edu.virginia.cs.storagedesk.common.Util;

public class TargetThread implements Runnable {
	
	private static Logger logger = Logger.getLogger(TargetThread.class);
	
	private Session session;
	
	public TargetThread(SocketChannel socketChannel, int s, String name, Disk d, int diskType) throws IOException {
		this.session = new Session();
		
		session.id = 0;
		session.socketChannel = socketChannel;
		logger.info("Connection accepted on port " + socketChannel.socket().getLocalPort() + 
				    "(local IP " + socketChannel.socket().getLocalSocketAddress().toString() + 
				    ", remote IP " + socketChannel.socket().getRemoteSocketAddress().toString() + ")");

		session.isFullFeature = false;
		session.isLoggedIn = false;
		session.isLoginStarted = false;
		session.state = s;
		session.targetName = name;
		session.targetAddress = socketChannel.socket().getLocalAddress().getHostAddress() + ":" +  
								socketChannel.socket().getLocalPort() + ",1";
		
		switch (diskType) {
		case ISCSI.RAM_DISK:			
//			session.disk = (RamDisk) d;
			break;
		
		case ISCSI.FILE_DISK:
			session.disk = (FileDisk) d;
			break;
			
		case ISCSI.VIRTUAL_RAM_DISK:
		case ISCSI.VIRTUAL_FILE_DISK:
			session.disk = (VirtualDisk) d;
			break;
		case ISCSI.VIRTUAL_FILE_DISK_NJ:
			session.disk = (VirtualDiskNJ) d;
			break;
			
		default:
			logger.error("Wrong disk type");
			break;
		}
		session.disk.setType(diskType);
	}
	
	public void run() {
		// Read and service request
		try {
			logger.debug("Target thread runs");				
			
//			PARAM
			session.params.put("AuthMethod", new Parameter(ISCSI.PARAM_TYPE_LIST, "AuthMethod", "None", "None"));
			session.params.put("TargetPortalGroupTag", new Parameter(ISCSI.PARAM_TYPE_DECLARATIVE, "TargetPortalGroupTag", "1", "1"));
			session.params.put("HeaderDigest", new Parameter(ISCSI.PARAM_TYPE_LIST, "HeaderDigest", "None", "None"));
			session.params.put("DataDigest", new Parameter(ISCSI.PARAM_TYPE_LIST, "DataDigest", "None", "None"));
			session.params.put("MaxConnections", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL, "MaxConnections", "1", "1"));
			session.params.put("SendTargets", new Parameter(ISCSI.PARAM_TYPE_DECLARATIVE, "SendTargets", "", ""));
			session.params.put("TargetName", new Parameter(ISCSI.PARAM_TYPE_DECLARATIVE, "TargetName", "", ""));
			session.params.put("InitiatorName", new Parameter(ISCSI.PARAM_TYPE_DECLARATIVE, "InitiatorName", "", ""));
			session.params.put("TargetAlias", new Parameter(ISCSI.PARAM_TYPE_DECLARATIVE, "TargetAlias", "", ""));
			session.params.put("InitiatorAlias", new Parameter(ISCSI.PARAM_TYPE_DECLARATIVE, "InitiatorAlias", "", ""));
			session.params.put("TargetAddress", new Parameter(ISCSI.PARAM_TYPE_DECLARATIVE, "TargetAddress", "", ""));
			session.params.put("InitialR2T", new Parameter(ISCSI.PARAM_TYPE_BINARY_OR, "InitialR2T", "Yes", "Yes,No"));
			session.params.put("OFMarker", new Parameter(ISCSI.PARAM_TYPE_BINARY_AND, "OFMarker", "No", "Yes,No"));
			session.params.put("IFMarker", new Parameter(ISCSI.PARAM_TYPE_BINARY_AND, "IFMarker", "No", "Yes,No"));
			session.params.put("OFMarkInt", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL_Z, "OFMarkInt", "1", "65536"));
			session.params.put("IFMarkInt", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL_Z, "IFMarkInt", "1", "65536"));
			session.params.put("ImmediateData", new Parameter(ISCSI.PARAM_TYPE_BINARY_AND, "ImmediateData", "Yes", "Yes,No"));
			session.params.put("MaxRecvDataSegmentLength", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL_Z, "MaxRecvDataSegmentLength", "65536", "1677215"));
			session.params.put("MaxBurstLength", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL_Z, "MaxBurstLength", "262144", "1677215"));
			session.params.put("FirstBurstLength", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL_Z, "FirstBurstLength", "65536", "1677215"));
			session.params.put("DefaultTime2Wait", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL, "DefaultTime2Wait", "2", "2"));
			session.params.put("DefaultTime2Retain", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL, "DefaultTime2Retain", "20", "20"));
			session.params.put("MaxOutstandingR2T", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL, "MaxOutstandingR2T", "1", "1"));
			session.params.put("DataPDUInOrder", new Parameter(ISCSI.PARAM_TYPE_BINARY_OR, "DataPDUInOrder", "Yes", "Yes,No"));
			session.params.put("DataSequenceInOrder", new Parameter(ISCSI.PARAM_TYPE_BINARY_OR, "DataSequenceInOrder", "Yes", "Yes,No"));
//			Windows initiator needs ErrorRecoveryLevel 2
			session.params.put("ErrorRecoveryLevel", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL, "ErrorRecoveryLevel", "0", "2"));
//			Some Linux iSCSI initiators do not support ErrorRecoveryLevel 2
//			session.params.put("ErrorRecoveryLevel", new Parameter(ISCSI.PARAM_TYPE_NUMERICAL, "ErrorRecoveryLevel", "0", "0"));
			session.params.put("SessionType", new Parameter(ISCSI.PARAM_TYPE_DECLARATIVE, "SessionType", "Normal", "Normal,Discovery"));
			
			session.isUsePhaseCollapsedRead = ISCSI.DEFAULT_USE_PHASE_COLLAPSED_READ;
			
			ByteBuffer header = ByteBuffer.allocate(ISCSI.Header_SIZE);
			
			while (session.state != ISCSI.TARGET_SHUT_DOWN) {
				
				header = ByteBuffer.allocate(ISCSI.Header_SIZE);
				
				logger.debug("Ready to read from initiator");
							
				while (session.socketChannel.isConnected() && header.hasRemaining()) {
					session.socketChannel.read(header);
				}
				
				if (session.socketChannel.isConnected() == false) {
					close();
					logger.debug("Socket closed");
					return;
				}
				
				if (logger.isTraceEnabled()) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					baos.reset();
					baos.write(header.array());								
					logger.trace("IRequest header: " + baos.toString());
				}
				
				logger.debug("ISCSI op code 0x" + Util.getiSCSIOpCode(header.array()[0]));
				if (execute(header.array()) == false) {
					logger.error("execution failed");
					break;
				}
				logger.debug("ISCSI op code 0x" + Util.getiSCSIOpCode(header.array()[0]) + " complete");
				if (Util.getiSCSIOpCode(header.array()[0]) == ISCSI.LOGOUT_CMD) {
					logger.debug("Logout received, ending thread");
					break;
				}
			}
			
			close();
			logger.debug("Session ended");
		}
		catch (IOException ex) {
			close();
		}
	}
	
	private void close() {
		if (session.socketChannel.isOpen()) {
			try { session.socketChannel.close(); } catch (IOException exc) {}
		}
	}
	
	public boolean execute(byte[] header) throws IOException {
		int opCode = Util.getiSCSIOpCode(header[0]);
		
		if (session.isFullFeature == false &&
				((opCode != ISCSI.LOGIN_CMD) && 
						(opCode != ISCSI.LOGOUT_CMD))) {
			logger.error("ISCSI op 0x" + opCode + " attempted before full feature");
			//
			// Rejetct
			//
			return false;
		}
		
		switch(opCode) {
		case ISCSI.TASK_CMD:
			logger.debug("TASK command");
			executeTaskCommand(header);
			break;
			
		case ISCSI.NOP_OUT:
			logger.debug("NOP OUT command");
			executeNopOutCommand(header);
			break;
			
		case ISCSI.LOGIN_CMD:
			logger.debug("LOGIN command");
			executeLoginCommand(header);
			break;
		
		case ISCSI.TEXT_CMD:
			logger.debug("TEXT command");
			executeTextCommand(header);
			break;
		
		case ISCSI.LOGOUT_CMD:
			logger.debug("LOGOUT command");
			executeLogoutCommand(header);
			break;
		
		case ISCSI.SCSI_CMD:
			logger.debug("SCSI command");
			executeSCSICommand(header);
			break;
			
		case ISCSI.SCSI_DATA:
			logger.debug("SCSI Data");
			executeSCSIDataCommand(header);
			break;
		
		default:
			//
			// Rejetct
			//
			logger.error("Unknow Opcode " + opCode);
			break;
		}
		
		return true;
	}

	private boolean executeTaskCommand(byte[] header) throws IOException {
		TaskRequest taskRequest	= new TaskRequest();
		TaskResponse taskResponse	= new TaskResponse();
				
		byte[] response;
		byte[] responseHeader;	    	
				
		if (taskRequest.decap(header) == false) {
			logger.error("ISCSI text command decap failed");
			return false;
		} 
		
		if (taskRequest.cmdSN != session.expCmdSN) {
			logger.error("Expected CmdSN " + session.expCmdSN + ", got " + taskRequest.cmdSN + ". Ignoring for now");
			session.expCmdSN = taskRequest.cmdSN;
		}
		
		session.maxCmdSN++;
		
		taskResponse.response = ISCSI.TASK_RSP_FUNCTION_COMPLETE;
		
		switch(taskRequest.function) {
		case (ISCSI.TASK_CMD_ABORT_TASK):
			logger.debug("TASK CMD ABORT TASK");
			break;
		case (ISCSI.TASK_CMD_ABORT_TASK_SET):
			logger.debug("TASK CMD ABORT TASK SET");
			break;
		case (ISCSI.TASK_CMD_CLEAR_ACA):
			logger.debug("TASK CMD CLEAR ACA");
			break;
		case (ISCSI.TASK_CMD_CLEAR_TASK_SET):
			logger.debug("TASK CMD CLEAR TASK SET");
			break;
		case (ISCSI.TASK_CMD_LOGICAL_UNIT_RESET):
			logger.debug("TASK CMD LOGICAL UNIT RESET");
			break;
		case (ISCSI.TASK_CMD_TARGET_WARM_RESET):
			logger.debug("TASK CMD TARGET WARM RESET");
			break;
		case (ISCSI.TASK_CMD_TARGET_COLD_RESET):
			logger.debug("TASK CMD TARGET COLD RESET");
			break;
		case (ISCSI.TASK_CMD_TARGET_REASSIGN):
			logger.debug("ISCSI TASK CMD TARGET REASSIGN");
			break;
		default:
			logger.error("Unknow task function");
			taskResponse.response = ISCSI.TASK_RSP_REJECTED;
		}
		
		taskResponse.tag = taskRequest.tag;
		taskResponse.statSN = ++session.statSN;
		taskResponse.expCmdSN = session.expCmdSN;
		taskResponse.maxCmdSN = session.maxCmdSN;
		
		responseHeader = taskResponse.encap();	
		response = Util.addPadding(responseHeader, ISCSI.MSG_BYTE_ALIGN);
		
		logger.debug("Sending TASK RESPONSE: " + response.toString());
		
		ByteBuffer responseBuffer = ByteBuffer.wrap(response);
		while (responseBuffer.hasRemaining()) {
			session.socketChannel.write(responseBuffer);
		}
				
		logger.debug("Sending TASK RESPONSE OK");
		
		return true;
	}
	
	private boolean executeNopOutCommand(byte[] header) throws IOException {
		NopOutRequest nopOutRequest	= new NopOutRequest();
		NopInResponse nopInResponse	= new NopInResponse();
				
		byte[] response;
		byte[] responseHeader;	
		ByteBuffer pingData = ByteBuffer.allocate(1);
				
		if (nopOutRequest.decap(header) == false) {
			logger.error("ISCSI nopin command decap failed");
			return false;
		} 
		
		if (nopOutRequest.cmdSN != session.expCmdSN) {
			logger.error("Expected CmdSN " + session.expCmdSN + ", got " + nopOutRequest.cmdSN + ". Ignoring for now");
			session.expCmdSN = nopOutRequest.cmdSN;
		}
		
		if (nopOutRequest.length > 0) {
			pingData = ByteBuffer.allocate(nopOutRequest.length);
			while (pingData.hasRemaining()) {
				session.socketChannel.read(pingData);
			}
			if (logger.isDebugEnabled()) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();	
				baos.write(pingData.array());								
				logger.debug("PING data: " + baos.toString());
			}
			
			// finish reading padding zeros	
			readPadding(nopOutRequest.length);
		} else {
			logger.debug("No ping payload");
		}
		
		if (nopOutRequest.tag != 0xffffffff) {
			nopInResponse.length = nopOutRequest.length;
			nopInResponse.lun = nopOutRequest.lun;
			nopInResponse.tag = nopOutRequest.tag;
			nopInResponse.transferTag = 0xffffffff;
			nopInResponse.statSN = session.statSN++;
			nopInResponse.expCmdSN = ++session.expCmdSN;
			nopInResponse.maxCmdSN = ++session.maxCmdSN;
			
			responseHeader = nopInResponse.encap();	
			response = Util.appendByteArray(responseHeader, pingData.array());				
			response = Util.addPadding(response, ISCSI.MSG_BYTE_ALIGN);

			logger.debug("Sending NOPIN RESPONSE: " + response.toString());
			
			ByteBuffer responseBuffer = ByteBuffer.wrap(response);
			while (responseBuffer.hasRemaining()) {
				session.socketChannel.write(responseBuffer);
			}
			
			logger.debug("Sending NOPIN RESPONSE OK");
		} else {
			logger.debug("SendNO NOPIN RESPONSE");
		}
		
		return true;
	}
	
	private boolean executeLoginCommand(byte[] header) throws IOException {
		LoginRequest loginRequest	= new LoginRequest();
		LoginResponse loginResponse	= new LoginResponse();
		
		ByteBuffer requestData;
		byte[] response;
		byte[] responseHeader;	    	
		byte[] responseData = new byte[1];
		
		loginResponse.statusClass = ISCSI.LOGIN_STATUS_INITIATOR_ERROR;
		
		// 
		// should check the version number
		// ignore for now
		
		if (loginRequest.decap(header) == false) {
			logger.error("ISCSI login command decap failed");	    		
		} else if (session.isLoggedIn) {
			logger.error("Duplicate login attempts");
		} else if (loginRequest.cont != 0) {
			logger.error("Bad command continue. Expected 0.");
		} else if (loginRequest.tsih != 0) {
			logger.error("Bad command tsih. Expected 0.");
		} else {				
			if (loginRequest.length > 0) {
				requestData = ByteBuffer.allocate(loginRequest.length);
				while (requestData.hasRemaining()) {
					session.socketChannel.read(requestData);
				}
				if (logger.isDebugEnabled()) {										
					ByteArrayOutputStream baos = new ByteArrayOutputStream();	
					baos.write(requestData.array());								
					logger.debug("IRequest data: " + baos.toString());
				}
				
				// finish reading padding zeros	
				readPadding(loginRequest.length);
				
				// Parse incoming parameters
				// return response data
				responseData = parseParameter(requestData.array(), false);
				
				if(session.isLoginStarted == false) {
					responseData = Util.appendByteArray(("TargetPortalGroupTag=1" + String.valueOf('\u0000')).getBytes(), responseData);
					loginResponse.length = responseData.length;
				}
				
				// DO NOT KNOW WHY???
				// Parse the outgoing offer					
//				if (responseData.length > 0) {
//				parseParameter(params, responseData, true);
//				}
				
				if (session.isLoginStarted == false) {
					session.isLoginStarted = true;
				}
			}
			
			// For now, accept whatever the initiator's current and next states are
			// And le are always ready to transitition to that state.
			loginResponse.csg = loginRequest.csg;
			loginResponse.nsg = loginRequest.nsg;
			loginResponse.transit = 1;
			
			if (loginRequest.transit != 0 && (loginRequest.nsg == ISCSI.LOGIN_STAGE_FULL_FEATURE)) {
				logger.debug("Transitioning to ISCSI.LOGIN_STAGE_FULL_FEATURE");
				boolean condition = false;
				// Check post condition
				if (session.params.containsKey("InitiatorName") == false ||
						((Parameter) session.params.get("InitiatorName")).valueList.size() == 0) {
					logger.error("InitiatorName not specified");
				} else if (session.params.containsKey("SessionType") == false || 
						((Parameter) session.params.get("SessionType")).valueList.size() == 0){
					logger.error("SessionType not specified");
				} else if (session.params.containsKey("SessionType") && 
						((Parameter) session.params.get("SessionType")).valueList.get("Normal") != null){
//					if (params.containsKey("TargetName") == false || 
//							((Parameter) params.get("TargetName")).valueList.size() == 0){
//						logger.error("TargetName not specified");
//					} else if (params.containsKey("TargetName") &&
//							((Parameter) params.get("TargetName")).valueList.get(targetName) != null){
//						logger.error("Bad Targetname " + ((Parameter) params.get("TargetName")).valueList.get(targetName));
//					} else {
						condition = true;
//					}
				} 
								
				if (condition) {
					session.expCmdSN = loginRequest.cmdSN;
					session.maxCmdSN = loginRequest.cmdSN;
					session.cid = loginRequest.cid;
					session.isid = loginRequest.isid;
					session.tsih = session.id + 1;
					session.isFullFeature = true;
					session.isLoggedIn = true;
					if (session.params.containsKey("SessionType") && 
							((Parameter) session.params.get("SessionType")).valueList.get("Discovery") != null){
						((Parameter) session.params.get("MaxConnections")).valueList.put("1", "1");				
					}
				}
			}
			
			// No errors
			loginResponse.statusClass = 0;
			loginResponse.statusDetail = 0;
			loginResponse.length = responseData.length;
		}
		
		// Send response
		
		session.expCmdSN = loginRequest.cmdSN;
		session.maxCmdSN = loginRequest.cmdSN;
		loginResponse.isid = loginRequest.isid;
		loginResponse.statSN = loginRequest.expStatSN;
		loginResponse.tag = loginRequest.tag;
		loginResponse.cont = loginRequest.cont;
		loginResponse.expCmdSN = session.expCmdSN;
		loginResponse.maxCmdSN = session.maxCmdSN;	
		
		if (loginResponse.statusClass == 0) {
			if (loginResponse.transit != 0 && loginResponse.nsg == ISCSI.LOGIN_STAGE_FULL_FEATURE) {
				loginResponse.maxVersion = ISCSI.VERSION;
				loginResponse.activeVersion = ISCSI.VERSION;
				loginResponse.statSN = session.statSN++;
				loginResponse.tsih = session.tsih;
			}
		}
		
		responseHeader = loginResponse.encap();	
		response = Util.appendByteArray(responseHeader, responseData);				
		response = Util.addPadding(response, ISCSI.MSG_BYTE_ALIGN);
		
		logger.debug("Sending LOGIN RESPONSE: " + response.toString());
		
		ByteBuffer responseBuffer = ByteBuffer.wrap(response);
		while (responseBuffer.hasRemaining()) {
			session.socketChannel.write(responseBuffer);
		}
		
		logger.debug("Sending LOGIN RESPONSE OK");
		
		if (loginResponse.statusClass != 0) {
			return false;
		}
		
		if (loginRequest.transit != 0 && (loginRequest.nsg == ISCSI.LOGIN_STAGE_FULL_FEATURE)) {
			logger.debug("LOGIN SUCCESSFUL");				
		}
		
		return true;
	}
	
	private boolean executeTextCommand(byte[] header) throws IOException {
		TextRequest textRequest	= new TextRequest();
		TextResponse textResponse = new TextResponse();
		
		byte[] response;
		byte[] responseHeader;	    	
		ByteBuffer requestData = ByteBuffer.allocate(1);
		byte[] responseData = new byte[1];
		
		if (textRequest.decap(header) == false) {
			logger.error("ISCSI text command decap failed");
			return false;
		} 
		
		if (!Util.isNotEqual(logger, "Final", textRequest.finalBit, 1) &&
				!Util.isNotEqual(logger, "Continue", textRequest.cont, 0) &&
				!Util.isNotEqual(logger, "Transfer Tag", textRequest.transferTag, 0xffffffff) &&
				!Util.isNotEqual(logger, "CmdSN", textRequest.cmdSN, session.expCmdSN)) {
			
			session.statSN = textRequest.expStatSN;
//			Util.isNotEqual(global, "ExpStatSN", textRequest.expStatSN, session.statSN);
			
			session.expCmdSN++;
			
			// Read text parameters
			if (textRequest.length > 0) {
				requestData = ByteBuffer.allocate(textRequest.length);
				while (requestData.hasRemaining()) {
					session.socketChannel.read(requestData);
				}
				if (logger.isDebugEnabled()) {										
					ByteArrayOutputStream baos = new ByteArrayOutputStream();	
					baos.write(requestData.array());								
					logger.debug("IRequest data: " + baos.toString());
				}
				
				// finish reading padding zeros		
				readPadding(textRequest.length);
								
				// Parse incoming parameters
				// return response data
				responseData = parseParameter(requestData.array(), false);
				
				if (session.params.containsKey("SendTargets") == false ||
						((Parameter) session.params.get("SendTargets")).valueList.size() == 0) {
					logger.error("SendTarget not specified");
					return false;
				}
				
				Parameter p = (Parameter) session.params.get("SendTargets");
				
				if (p.isRxOffer) {
					responseData = Util.appendByteArray(("TargetAddress=" + session.targetAddress + String.valueOf('\u0000')).getBytes(), responseData);
					responseData = Util.appendByteArray(("TargetName=" + session.targetName + String.valueOf('\u0000')).getBytes(), responseData);					
					textResponse.length = responseData.length;
					p.isRxOffer = false;
				}
				
				// parse outgoing offer
				
			}
			
			// Send response
			
			textResponse.finalBit = textRequest.finalBit;
			textResponse.cont = 0;
			textResponse.length = responseData.length;
			textResponse.lun = textRequest.lun;
			textResponse.tag = textRequest.tag;
			
			if (textResponse.finalBit != 0) {
				textResponse.transferTag = 0xffffffff;
			} else {
				textResponse.transferTag = 0x1234;
			}				
			textResponse.statSN = session.statSN++;
			textResponse.expCmdSN = session.expCmdSN;
			textResponse.maxCmdSN = session.maxCmdSN;			
								
			responseHeader = textResponse.encap();	
			response = Util.appendByteArray(responseHeader, responseData);				
			response = Util.addPadding(response, ISCSI.MSG_BYTE_ALIGN);
			
			logger.debug("Sending TEXT RESPONSE: " + response.toString());
			
			ByteBuffer responseBuffer = ByteBuffer.wrap(response);
			while (responseBuffer.hasRemaining()) {
				session.socketChannel.write(responseBuffer);
			}
			
			logger.debug("Sending TEXT RESPONSE OK");
		}
		
		return true;
	}
	
	private boolean executeLogoutCommand(byte[] header) throws IOException {
		LogoutRequest logoutRequest	= new LogoutRequest();
		LogoutResponse logoutResponse = new LogoutResponse();
		
		byte[] response;
		
		if (logoutRequest.decap(header) == false) {
			logger.error("ISCSI text command decap failed");
			return false;
		} 
		
		session.statSN = logoutRequest.expStatSN;
				
		if (Util.isNotEqual(logger, "Reason", logoutRequest.reason, ISCSI.LOGOUT_CLOSE_SESSION)) {
			return false;
		}
		
		logoutResponse.tag = logoutRequest.tag;
		logoutResponse.statSN = session.statSN;
		logoutResponse.expCmdSN = session.expCmdSN;
		logoutResponse.maxCmdSN = session.maxCmdSN;
		
		response = logoutResponse.encap();						
		response = Util.addPadding(response, ISCSI.MSG_BYTE_ALIGN);
		
		logger.debug("Sending LOGOUT RESPONSE: " + response.toString());
		
		ByteBuffer responseBuffer = ByteBuffer.wrap(response);
		while (responseBuffer.hasRemaining()) {
			session.socketChannel.write(responseBuffer);
		}
		
		logger.debug("Sending LOGOUT RESPONSE OK");
		
		session.isLoggedIn = false;
		
		return true;
	}
	
	private boolean executeSCSICommand(byte[] header) throws IOException {
		ScsiRequest scsiRequest	= new ScsiRequest();
		ScsiResponse scsiResponse = new ScsiResponse();
		
		byte[] response;
		byte[] responseHeader;	    	
		byte[] responseData = new byte[1];
		
		if (scsiRequest.decap(header) == false) {
			logger.error("ISCSI scsi command decap failed");
			return false;
		} 
		
		logger.debug("SCSI Command (CmdSN " + scsiRequest.cmdSN + ", op 0x" + 
					Integer.toString(scsiRequest.cdb[0] & 0xff,16) + ")");
		
		
		scsiRequest.attr = 0; 
		
		// Read AHS
		// Will add this part later
		if (scsiRequest.ahsLength > 0) {
			logger.debug("Need to read AHS, ignoring for now");
		} else {
			logger.debug("No AHS to read");
			scsiRequest.ahs = null;
		}
		
		// Check numbering
		if (scsiRequest.cmdSN != session.expCmdSN) {
			logger.error("Expected CmdSN " + session.expCmdSN + ", got " + scsiRequest.cmdSN + ". " + 
						"Ignoring and resetting expections");
			session.expCmdSN = scsiRequest.cmdSN;
		}
					
		// Check transfer lengths
		// Add later
		
		if (isParameterExisted("MaxBurstLength", "0") &&
			(scsiRequest.expTransferLength > 
				Integer.parseInt(session.params.get("MaxBurstLength").valueList.get(0)) * ISCSI.MAX_BURST_UNITS)) {
			logger.error("SCSI request transfer length is greater than MaxBurstLength");
			return false;
		}
		
		if (isParameterExisted("FirstBurstLength", "0") &&
			(scsiRequest.length > 
				Integer.parseInt(session.params.get("FirstBurstLength").valueList.get(0)) * ISCSI.FIRST_BURST_UNITS)) {
			logger.error("SCSI request length is greater than FirstBurstLength");
			return false;
		}
		
		if (isParameterExisted("MaxRecvDataSegmentLength", "0") &&
			(scsiRequest.length > 
				Integer.parseInt(session.params.get("MaxRecvDataSegmentLength").valueList.get(0)) * ISCSI.MAX_BURST_UNITS)) {
			logger.error("SCSI request length is greater than MaxRecvDataSegmentLength");
			return false;
		}
		
		session.expCmdSN++;
		session.maxCmdSN++;		
		
		// Execute cdb
		
		scsiRequest = session.disk.executeCommand(session, scsiRequest);
		
		session.statSN++;
		
		// Send input data
		
		if (scsiRequest.status == 0 && scsiRequest.fromDisk == 1) {	
			int transferLength;
			int offset = 0;
//			int offsetInc;
			
			ScsiDataResponse data = new ScsiDataResponse();
			
			if (scsiRequest.toDisk == 1) {
				logger.debug("Sending " + scsiRequest.bidirectionTransferLength + " bytes" +
								" bi-directional input data");
				transferLength = scsiRequest.bidirectionTransferLength;
			} else {
				transferLength = scsiRequest.expTransferLength;
			}
			
			logger.debug("Sending " + transferLength + " bytes input data as separate PDUs");
			
//			if (isParameterExisted("MaxRecvDataSegmentLength", "0")) {
//				offsetInc = ((Integer) params.get("MaxRecvDataSegmentLength").valueList.values().toArray()[0]).intValue()
//							* ISCSI.DATA_PDU_LENGTH_UNITS;
//			} else {
//				offsetInc = transferLength;
//			}

			for (int bufferIndex = 0; bufferIndex < scsiRequest.fromDiskData.length; bufferIndex++) {
				if (isParameterExisted("MaxRecvDataSegmentLength", "0")) {
					data.length = Math.min(transferLength - offset, 
								((Integer) session.params.get("MaxRecvDataSegmentLength").valueList.values().toArray()[0]).intValue()
								* ISCSI.DATA_PDU_LENGTH_UNITS);
				} else {
					data.length = scsiRequest.fromDiskData[bufferIndex].array().length;
				}
				
				logger.debug("Sending read data PDU (offset " + offset + 
												  ", length " + data.length + 
												  ", transfer len " + transferLength + ")");
				
				if (offset +  data.length == transferLength) {
					data.finalBit = 1;
					if (session.isUsePhaseCollapsedRead) {
//						data.status = 1;
						data.status = scsiRequest.status;
						data.statSN = session.statSN++;
						logger.debug("Status " + data.status + " collapsed into last data PDU");
					} else {
						logger.debug("Not collapsing status with last data PDU");
					}
				} else if (offset + data.length > transferLength) {
					logger.error("offset + data.length > transferLength");
					return false;
				}
				
				data.tag = scsiRequest.tag;
				data.statSN = scsiRequest.expStatSN;
				data.expCmdSN = session.expCmdSN;
				data.maxCmdSN = session.maxCmdSN;
				data.dataSN = session.dataSN++;
				data.offset = offset;
				data.transferTag = 0xffffffff;
				
				if (scsiRequest.expTransferLength < scsiRequest.length) {
					data.overflow = 1;
					data.residualCount = data.length - scsiRequest.expTransferLength;					
				} else if (scsiRequest.expTransferLength > scsiRequest.length) {
					data.underflow = 1;
					data.residualCount = scsiRequest.expTransferLength - data.length;
				} 
				
				responseHeader = data.encap();	
				responseData = new byte[data.length];
				
				logger.debug("Disk data array number is " + bufferIndex + ", length is " + data.length);
				
				System.arraycopy(scsiRequest.fromDiskData[bufferIndex].array(), 0, responseData, 0, data.length);
				response = Util.appendByteArray(responseHeader, responseData);	
				logger.debug("data length " + scsiRequest.length + " response len " + response.length);
				response = Util.addPadding(response, ISCSI.MSG_BYTE_ALIGN);
				logger.debug("after padding response len " + response.length);
				
				logger.debug("Sending READ DATA RESPONSE: " + response.toString());
				
				ByteBuffer responseBuffer = ByteBuffer.wrap(response);
				while (responseBuffer.hasRemaining()) {
					session.socketChannel.write(responseBuffer);
				}
				
				logger.debug("Sending READ DATA RESPONSE OK");
				
				offset += data.length;
			} // end of for loop
		}
		
		/*
		 * Send a response PDU
		 */
		
		if ((scsiRequest.fromDisk != 1) && 
				(session.isUsePhaseCollapsedRead == false || 
				scsiRequest.length == 0 ||
				scsiRequest.status != 0)) {
			logger.debug("Sending SCSI response PDU");
			scsiResponse.length = (scsiRequest.status != 0) ? scsiRequest.length: 0;
			scsiResponse.tag = scsiRequest.tag;
			scsiResponse.statSN = session.statSN++;
			scsiResponse.expCmdSN = session.expCmdSN;
			scsiResponse.maxCmdSN = session.maxCmdSN;
			scsiResponse.expDataSN = (scsiRequest.status == 0 && scsiRequest.fromDisk != 0)? session.dataSN: 0;
			scsiResponse.response = 0x00;
			scsiResponse.status = scsiRequest.status;
			
			responseHeader = scsiResponse.encap();	
			logger.debug("scsi response encap completes");
			if (scsiResponse.length != 0) {
				response = Util.appendByteArray(responseHeader, responseData);				
				response = Util.addPadding(response, ISCSI.MSG_BYTE_ALIGN);
			} else {
				response = Util.addPadding(responseHeader, ISCSI.MSG_BYTE_ALIGN);
			}
			
			logger.debug("Sending SCSI RESPONSE: " + response.toString());
			
			ByteBuffer responseBuffer = ByteBuffer.wrap(response);
			while (responseBuffer.hasRemaining()) {
				session.socketChannel.write(responseBuffer);
			}
			
			logger.debug("Sending SCSI RESPONSE OK");
			
			if (scsiRequest.toDisk != 0) {
				
			}
			
			
		}		
	
		logger.debug("Complete SCSI command");
		
		return true;
	}
	
	private boolean executeSCSIDataCommand(byte[] header) throws IOException {
		ScsiDataRequest dataRequest	= new ScsiDataRequest();
		
		if (dataRequest.decap(header) == false) {
			logger.error("SCSI Data command decap failed");
			return false;
		} 
		
		logger.debug("NEED TO WORK ON SCSI DATA COMMAND");
		
		return true;
	}
	
	
	private byte[] parseParameter(byte[] requestData, boolean outgoing) throws IOException {
			logger.debug("Parsing " + requestData.length + " bytes of text parameters " + (outgoing?"outgoing":"incoming"));
			StringBuffer responseData = new StringBuffer();
			int responseLength = -1;
			if (outgoing == false) {
				responseLength = 0;
			}			
			
			logger.debug("Parameters negotiated");
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();			
			baos.write(requestData);								
			String[] pairs = baos.toString().split(String.valueOf('\u0000'));
			
			boolean negotiate = false;
			
	//		process each pair
			for (int i = 0; i < pairs.length; i++) {
				logger.debug("parameter pair: " + pairs[i]);
				negotiate = false;
	//			Extract <key>=<value> token from the pair
				String[] result = pairs[i].split("[=,]");
	//			for (int j = 0; j < result.length; j++)
	//			logger.debug(result[j]);				
				if (result.length < 2) {
					logger.error("Delimiter = not found in the pair");	
					continue;
				} 
				
				String key = result[0];
				String value = result[1];
				
	//			Find key in param list
				Parameter param = (Parameter) session.params.get(key);
				if ( param == null) {
					logger.error("Ignore key: " + key);		
					continue;
				} 						
				if (value.length() > ISCSI.PARAM_MAX_LEN) {
					logger.error("Exceed the length of the parameter: " + pairs[i]);
					continue;
				} 		
				// We're sending|receiving an offer|answer 
				if (outgoing) {
					if (param.isRxOffer) {   
						param.isTxAnswer = true;  // sending an answer
						param.txAnswer = value; 
						logger.debug("Sending answer " + param.key + "=" + param.txAnswer + 
								" for offer " + param.rxOffer); 
						negotiate = true;
					} else {
						param.isTxOffer = true;   // sending an offer
						param.isRxOffer = false;   // reset
						param.txOffer = value; 
						logger.debug("Sending offer " + param.key + "=" + param.txOffer);  
						if ((param.type == ISCSI.PARAM_TYPE_DECLARATIVE)||
								(param.type == ISCSI.PARAM_TYPE_DECLARE_MULTI)) {
							negotiate = true;
						}									
					}
				} else {
					if (param.isTxOffer) {   
						param.isRxAnswer = true;  // received an answer
						param.isTxOffer = false;   // reset
						param.rxAnswer = value; 
						logger.debug("Received answer " + param.key + "=" + param.txAnswer + 
								" for offer " + param.txOffer); 
						negotiate = true;
					} else {
						param.isRxOffer = true;   // received an offer
						param.rxOffer = value; 
						logger.debug("Received offer " + param.key + "=" + param.rxOffer);  
						
						// Answer the offer if it is an inquiry or the type is not DECLARATIVE									
						if ((param.rxOffer.toString().compareTo("?") != 0) && 
								((param.type == ISCSI.PARAM_TYPE_DECLARATIVE)||
										(param.type == ISCSI.PARAM_TYPE_DECLARE_MULTI))) {
							negotiate = true;
						} 
					}
				}
				
				if (negotiate == false) {
					// Answer
					
					// Answer with current value if this is an inquiry (<key>=?)
					if (value.compareTo("?") == 0) {
						logger.debug("Got inquiry for param " + param.key);
						if (!param.valueList.isEmpty() &&
								(((String) param.valueList.get(0)).compareTo("") != 0)) {		
							param.txAnswer = (String) param.valueList.get(0);												
						} else {
							logger.error("param " + param.key + " has NULL value list");
							param.txAnswer = "";
						}											
					} else {
						// Generate answer according to the parameter type
						switch(param.type) {
						
						case ISCSI.PARAM_TYPE_BINARY_AND:
						case ISCSI.PARAM_TYPE_BINARY_OR:
							if ((value.compareToIgnoreCase("yes") != 0) &&
									(value.compareToIgnoreCase("No")!= 0)) {
								logger.error(value + " is not a valid binary value");
								param.txAnswer = "NotUnderstood";										          
							} else if (param.validValues.size() > 1) {
								param.txAnswer = value;        // we accept both yes and no, so answer w/ their offer
							} else {
								param.txAnswer = param.validValues.get(0); // answer with the only value we support
							}
							
							// temporary hack
							if (param.key.compareToIgnoreCase("InitialR2T") == 0) {
								param.txAnswer = "Yes";
							}
							
							break;
							
						case ISCSI.PARAM_TYPE_LIST:										    	  
							// Find the first valid offer that we support
							String[] rxOffers = param.rxOffer.split(String.valueOf(','));
							if (param.validValues.size() == 0) {
								logger.error("Parameter valid list empty. Answering with first in offer list.");
								param.txAnswer = rxOffers[0];
							} else {
								for (int j = 0; j < rxOffers.length; j++) {
									if (param.validValues.containsKey(rxOffers[j])) {									
										param.txAnswer = rxOffers[j];
									} else {
										logger.error(rxOffers[j] + "is not a valid offer (must choose from " + 
												param.validValues + ")");	
										param.txAnswer = "NotUnderstood";
									} 
									break;										    			  
								}
							}
							break;
							
						case ISCSI.PARAM_TYPE_NUMERICAL_Z:
						case ISCSI.PARAM_TYPE_NUMERICAL:
							int offer = Integer.parseInt(param.rxOffer);
							int max = Integer.parseInt(param.validValues.values().toArray()[0].toString());
							int answer;
							
							if (param.type == ISCSI.PARAM_TYPE_NUMERICAL_Z) {
								if (max == 0) {
									answer = offer;       // we support anything, so return whatever they offered
								} else if (offer == 0) {
									answer = max;         // return only what we can support
								} else if (offer > max) {
									answer = max;         // we are the lower of the two
								} else {
									answer = offer;       // they are the lower of the two
								}
							} else {
//								if (offer > max) {
									answer = max;         // we are the lower of the two
//								} else {
//									answer = offer;       // they are the lower of the two
//								}
							}
							param.txAnswer = String.valueOf(answer);
							break;
							
						default:
							break;
						}
						responseData.append(param.key + "=" + param.txAnswer + String.valueOf('\u0000'));
						responseLength = responseData.length();
						logger.debug("Answering " + responseData);							
					}
				} else {
					// negotiate
					switch (param.type) {
					case ISCSI.PARAM_TYPE_DECLARE_MULTI:
					case ISCSI.PARAM_TYPE_DECLARATIVE:
						if (outgoing) {
							param.negotiated = param.txOffer;
						} else {
							param.negotiated = param.rxOffer;
						}
						break;
						
					case ISCSI.PARAM_TYPE_BINARY_AND:
					case ISCSI.PARAM_TYPE_BINARY_OR:
						String val1, val2;
						if (outgoing) {
							val1 = param.rxOffer;
							val2 = param.txAnswer;
						} else {
							val1 = param.rxAnswer;
							val2 = param.txOffer;
						}
						if (param.type == ISCSI.PARAM_TYPE_BINARY_OR) {
							if (val1.compareToIgnoreCase("Yes") == 0 || 
									val2.compareToIgnoreCase("Yes") == 0) {
								param.negotiated = "Yes";
							} else {
								param.negotiated = "No";
							}
						} else {
							if (val1.compareToIgnoreCase("Yes") == 0 && 
									val2.compareToIgnoreCase("Yes") == 0) {
								param.negotiated = "Yes";
							} else {
								param.negotiated = "No";
							}
						}
						break;
						
					case ISCSI.PARAM_TYPE_NUMERICAL_Z:
					case ISCSI.PARAM_TYPE_NUMERICAL:									
						int v1, v2, negotiatedValue;
						if (outgoing) {
							val1 = param.rxOffer;
							val2 = param.txAnswer;
						} else {
							val1 = param.rxAnswer;
							val2 = param.txOffer;
						}
						v1 = Integer.parseInt(val1);
						v2 = Integer.parseInt(val2);
						if (param.type == ISCSI.PARAM_TYPE_NUMERICAL_Z) {
							if (v1 == 0) {
								negotiatedValue = v2;
							} else if (v2 == 0) {
								negotiatedValue = v1;
							} else if (v1 > v2) {
								negotiatedValue = v2;
							} else {
								negotiatedValue = v1;
							}
						} else {
							if (v1 > v2) {
								negotiatedValue = v2;
							} else {
								negotiatedValue = v1;
							}
						}
						param.negotiated = String.valueOf(negotiatedValue);
						break;
						
					case ISCSI.PARAM_TYPE_LIST:
						if (outgoing) {
							if (param.isTxOffer) {
								logger.error("Should not be here"); // error - sending an offer
								return null;
							} else if (param.isTxAnswer) {
								val1 = param.txAnswer; 		// Sending an swer
							} else {
								logger.error("Unexpected error");
								return null;
							}
						} else {
							if (param.isRxOffer) {
								logger.error("Should not be here"); // error - received an offer
								return null;
							} else if (param.isRxAnswer) {
								val1 = param.rxAnswer; 		// receving an swer
							} else {
								logger.error("Unexpected error");
								return null;
							}
						}
						// Make sure incoming or outgoing answer is valid
						String[] rxOffers = param.rxOffer.split(String.valueOf(','));
						if (param.validValues.size() == 0) {
							logger.error("Parameter valid list empty.");
							return null;
						} else {
							boolean match = false;							
							for (int j = 0; j < rxOffers.length; j++) {
								if (val1.compareToIgnoreCase(rxOffers[j]) == 0) {
									match = true;
									param.negotiated = val1;
									break;
								}
							}
							if (match == false) {
								logger.error(val1 + "is not a valid offer (must choose from " + 
										param.validValues + ")");	
								return null;
							}									
						}
						break;
						
					default:
						logger.error("Should not be here.");
					break;
					}
					logger.debug("negtoiated " + param.key + "=" + param.negotiated);
					
					// For inquires, we do not commit the value.
					if (param.isTxOffer && param.txOffer != null && param.txOffer.compareToIgnoreCase("?") == 0) {
						logger.debug("Sending an inquiry for " + param.key);
					} else if (param.isRxOffer && param.rxOffer != null && param.rxOffer.compareToIgnoreCase("?") == 0) {
						logger.debug("Received an inquiry for " + param.key);
					} else if (param.isTxAnswer && param.rxOffer != null && param.rxOffer.compareToIgnoreCase("?") == 0) {
						logger.debug("Answeringing an inquiry for " + param.key);
					} else if (param.isRxAnswer && param.txOffer != null && param.txOffer.compareToIgnoreCase("?") == 0) {
						logger.debug("Received an answer for inquiry on " + param.key);
					} else {
						logger.debug("Automatically committing " + param.key + "=" + param.negotiated);
						
						if (param.reset) {
							logger.debug("Deleting value list for " + param.key);
							param.valueList = null;
							param.reset = false;
						}
						// Seems strange here
						if (param.valueList != null) {
							if (param.type == ISCSI.PARAM_TYPE_DECLARE_MULTI) {
								param.valueList.put(param.negotiated, param.negotiated);
							} else {
								param.valueList.put(param.negotiated, param.negotiated);
							}
						} else {
							param.valueList.put(param.negotiated, param.negotiated);
						}
					}
				}	// answer or negotiate								
			}
			if (outgoing == false) {
				logger.debug("Generated " + responseLength + " bytes response");
			}
			return responseData.toString().getBytes();
		}

	private boolean isParameterExisted(String key, String value) {
		if (session.params.containsKey(key)) {			
			if (session.params.get(key).valueList.containsKey(value)) {
				return true;				
			} else {
				return false;
			}
		} else {
			logger.debug("Key " + key + " not found in parameter list");
			return false;
		}
	}

	private void readPadding(int length) throws IOException{
		//	 finish reading padding zeros					
		if ((ISCSI.Header_SIZE + length) % ISCSI.MSG_BYTE_ALIGN > 0) {
			int count = ISCSI.MSG_BYTE_ALIGN - (ISCSI.Header_SIZE + length) % ISCSI.MSG_BYTE_ALIGN;
			logger.debug("Padding count: " + count);
			ByteBuffer padding = ByteBuffer.allocate(count);
			while (padding.hasRemaining()) {
				session.socketChannel.read(padding);
			}
		}
	}
	
}
