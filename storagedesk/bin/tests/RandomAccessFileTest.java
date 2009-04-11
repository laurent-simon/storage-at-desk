package tests;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class RandomAccessFileTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File srcDir = new File(args[0]);
		File dstDir = new File(args[1]);

		File[] srcFiles = srcDir.listFiles();

		for (File f: srcFiles) {
			if (f.isDirectory()) 
				continue;
			
			try {
				// Create channel on the source
				FileChannel srcChannel = new FileInputStream(f).getChannel();

				// Create channel on the destination
				FileChannel dstChannel = new FileOutputStream(dstDir.getAbsolutePath() + 
															  System.getProperty("file.separator") +
															  f.getName()).getChannel();

				// Copy file contents from source to destination
//				dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
				
				ByteBuffer buffer = ByteBuffer.allocate(1024);
		        int nr = 0;
		        srcChannel.position(nr);
		        nr += srcChannel.read(buffer);
		        while(nr < f.length() ){
		        	buffer.flip();
		            dstChannel.write(buffer);
		            buffer.clear();
		            nr += srcChannel.read(buffer);
		        }

				// Close the channels
				srcChannel.close();
				dstChannel.close();
			} catch (IOException e) {
				
			}
		}
	}

}
