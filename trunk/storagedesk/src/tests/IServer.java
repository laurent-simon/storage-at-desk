package tests;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {
	public long write(byte[] bytes, long position) throws RemoteException;
}
