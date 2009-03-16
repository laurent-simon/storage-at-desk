package edu.virginia.cs.storagedesk.common;

public class NativeFile {

	public native boolean write(String filename, byte[] data, long pos);
	public native byte[] read(String filename, long pos, int len);
	
//	static 
//	  {
//		 if (System.getProperty("os.name").substring(0, 3).toLowerCase().compareTo("win") == 0) {
//			System.loadLibrary("nativefile");
//		 	System.out.println("loaded dll");
//		 }
//	  }

}
