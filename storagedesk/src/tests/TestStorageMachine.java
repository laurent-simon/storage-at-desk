package tests;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import edu.virginia.cs.storagedesk.storagemachine.IStorageMachine;

public class TestStorageMachine {

    private TestStorageMachine() {}

    public static void main(String[] args) {

	String host = (args.length < 1) ? null : args[0];
	try {
	    Registry registry = LocateRegistry.getRegistry(host);
	    IStorageMachine stub = (IStorageMachine) registry.lookup("StorageMachine");
	    stub.init();
//	    String response = stub.sayHello();
//	    System.out.println("response: " + response);
	} catch (Exception e) {
	    System.err.println("Client exception: " + e.toString());
	    e.printStackTrace();
	}
    }
}