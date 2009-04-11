package edu.virginia.cs.storagedesk.volumecontroller;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.virginia.cs.storagedesk.database.Machine;
import edu.virginia.cs.storagedesk.database.Volume;

public interface IVolumeController extends Remote {
	public boolean registerMachine(Machine machine) throws RemoteException;
	public int registerVolume(Volume volume) throws RemoteException;
	public Volume  assignMapping(Volume volume) throws RemoteException;
	
	public boolean isNewVolume(Volume volume) throws RemoteException;
	
	public boolean machineHeartbeat(String id, String ip) throws RemoteException;
}
