package edu.virginia.cs.storagedesk.storagemachine;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IStorageMachine extends Remote {
	
	public boolean init() throws RemoteException;
	
	public boolean isAlive() throws RemoteException;
	
//	methods for read/write bytes via RMI
	public byte[] read(int chunkID, long position, int length) throws RemoteException;
	public boolean write(int chunkID, byte[] bytes, long position) throws RemoteException;
	
}
