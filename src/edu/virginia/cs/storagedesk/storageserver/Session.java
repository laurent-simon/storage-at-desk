package edu.virginia.cs.storagedesk.storageserver;

import java.nio.channels.SocketChannel;
import java.util.Hashtable;

import edu.virginia.cs.storagedesk.common.Disk;

public class Session{
	
//	private static Logger logger = Logger.getLogger(Session.class);
	
	private static final long serialVersionUID = 0;
	
	public SocketChannel socketChannel;
	
	public int state;
	public String targetName;
	public String targetAddress;
	public Disk disk;
	
	public int id;
	public int cid;
	public int statSN; 
	public int expCmdSN;
	public int maxCmdSN;
	public int dataSN;
	
	public byte[] buffer;
	
	public boolean isUsePhaseCollapsedRead;
	public boolean isFullFeature;
	public boolean isLoggedIn;
	public boolean isLoginStarted;
	public long isid;
	public int tsih;
	
	public Hashtable<String, Parameter> params = new Hashtable<String, Parameter>();	
}
