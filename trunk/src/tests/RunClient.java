package tests;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class RunClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Object o = Naming.lookup("Server");
			IServer serverStub = (IServer) o;

			File srcDir = new File(args[0]);
			File dstDir = new File(args[1]);

			File[] srcFiles = srcDir.listFiles();
			
			long position = 0;

			for (File f: srcFiles) {
				if (f.isDirectory()) 
					continue;
				
				try {
					// Create channel on the source
					FileChannel srcChannel = new FileInputStream(f).getChannel();
					
					String fileName = dstDir.getAbsolutePath() + 
					  System.getProperty("file.separator") +
					  f.getName(); 

					// Create channel on the destination
					FileChannel dstChannel = new FileOutputStream(fileName).getChannel();

					// Copy file contents from source to destination
//					dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
					
					System.out.println("Coping " + fileName);
					
					ByteBuffer buffer = ByteBuffer.allocate(65536);
			        int nr = 0;
			        srcChannel.position(nr);
			        nr = srcChannel.read(buffer);
			        while(nr > -1 ){
			        	buffer.flip();
			        	byte[] bytes = new byte[buffer.limit()];
			        	buffer.get(bytes);
//			            dstChannel.write(buffer);
			        	position = serverStub.write(bytes, position);
			
			            buffer.clear();
			            
			            nr = srcChannel.read(buffer);
			        }
			        
			        System.out.println("Done ");
					// Close the channels
					srcChannel.close();
					dstChannel.close();
				} catch (IOException e) {
					
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
