package tests;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;


public class RunServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			java.rmi.registry.LocateRegistry.createRegistry(1099);
		} catch (Exception e) {
			
		}
		
		try {
			Server s = new Server();
			Naming.rebind("Server", s);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Server ready");

	}

}
