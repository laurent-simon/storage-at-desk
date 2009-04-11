package edu.virginia.cs.storagedesk.common;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

public class FileDisk extends Disk {	
	private static Logger logger = Logger.getLogger(FileDisk.class.toString());

	private int id;
	private FileChannel data;
	static byte[] b;
	ByteBuffer bytes = null;
	private String fileName;
//	private NativeFile nativeFile = new NativeFile();

//	private static ConcurrentLinkedQueue<Work> queue = new ConcurrentLinkedQueue<Work>();
	
	public FileDisk(int i, String path, String prefix, long size) {
		super();
		id = i;
		try {
			fileName =  path + File.separator + prefix + "-" + id + ".dat";// "\\temp\\StorageDesk-0.dat"; // +
			logger.debug("File name is " + fileName);
			File file = new File(fileName);
			if (file.exists() == false) {
				file.createNewFile();
				logger.info("Create a new file");
			}
			if (file.length() != size) {
				RandomAccessFile f = new RandomAccessFile(file, "rw");
				f.setLength(size);
				f.close();
				logger.info("Set the file to size " + size);
			}	
			
			if (System.getProperty("os.name").substring(0, 3).toLowerCase().compareTo("win") != 0) {
				RandomAccessFile aFile = new RandomAccessFile(fileName, "rwd");
				data = aFile.getChannel();
			}

			logger.info("FileDisk for Chunk [" + i + "] inits");
		} catch (Exception e) {
			logger.error(e.getMessage());
		} 		
	}

	public byte[] read(long position, int length) {
		logger.debug("Reading " + length + " bytes from Disk (LUN " + this.getLun() + 
				", offset " + position + ", " + length + " bytes)");
		try {
			// byte [] result = new byte[length];
			bytes = ByteBuffer.allocate(length);
/*			if (System.getProperty("os.name").substring(0, 3).toLowerCase().compareTo("win") == 0) {
				logger.debug("Windows Native File Functions");
				result = nativeFile.read(fileName, position, length);
				return result;
			} else {  */
				int numBytes = data.read(bytes, position);
				logger.info("Read " + numBytes + " bytes");		
			//}
		} catch (IOException ex) {
			logger.error("Read IOException happened at position " + position + ", length " + length);
			ex.printStackTrace();
		}
		return bytes.array();
	}

	public boolean write(byte[] bytes, long position) {
		logger.info("Writing " + bytes.length + " bytes to Disk (LUN " + this.getLun() + 
				", offset " + position + ", " + bytes.length + " bytes)");
		try {
/*			if (System.getProperty("os.name").substring(0, 3).toLowerCase().compareTo("win") == 0) {
				logger.debug("Windows Native File Functions");
				nativeFile.write(fileName, bytes, position);
			} else {  */
				int numBytes = data.write(ByteBuffer.wrap(bytes), position);
				data.force(true);
				logger.info("Write " + numBytes + " bytes");
			// }
			logger.debug("Finished write "); // + count);
		} catch (IOException ex) {
			logger.error("Write IOException happened at position " + position);
			ex.printStackTrace();
		}
		return true;
	}

}
