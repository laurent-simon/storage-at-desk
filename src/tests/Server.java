package tests;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.virginia.cs.storagedesk.common.NativeFile;



public class Server extends UnicastRemoteObject implements IServer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	FileChannel data;
	long length = Double.doubleToLongBits(Math.pow(2, 30));
	String fileName = "data";
	
	private NativeFile nativeFile = new NativeFile();
	
	public Server() throws RemoteException {
		try {
			File file = new File(fileName);
			if (file.exists() == false) {
				file.createNewFile();
			}
			RandomAccessFile f = new RandomAccessFile(fileName, "rws");
			length = 1000000000;
			f.setLength(length);
			f.close();
//			data = f.getChannel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public long write(byte[] bytes, long position) throws RemoteException {
		// TODO Auto-generated method stub
//		position = Math.abs((new Random()).nextLong()) % (length);
//		try {
//			data.position(position);
//			position += data.write(ByteBuffer.wrap(bytes));
//			data.force(true);
//			System.out.println("Write " + bytes.length + " bytes at " + position);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		try {
		System.out.println("about to write");
		nativeFile.write(fileName, bytes, 0);
		System.out.println("Write " + bytes.length + " bytes at " + position);
		byte[] results = nativeFile.read(fileName, 0, bytes.length);
	    boolean diff = false;
	    for (int i = 0; i < bytes.length; i++) {
//	    	System.out.println(results[i]);
	    	if (bytes[i] != results[i]) {
	    		System.out.println("wrong result at " + i + " : " + bytes[i] + " - " + results[i]);
	    		diff = true;
	    		break;
	    	}
	    }
	    if (diff) {
	    	System.out.println("diff ");
	    } else {
	    	System.out.println("OK ");
	    }}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return position;
	}

}
