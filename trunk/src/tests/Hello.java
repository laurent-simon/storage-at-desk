package tests;

/*****
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

public class Hello implements WrapperListener {
	static int i = 0;
	
	public Hello() {
		System.out.println("Hello World!");
		new HelloThread().start();
	}
	
	public class HelloThread extends Thread {

	    public void run() {
	    	while(true) {
	    		try {
					Thread.sleep(10000);
					i++;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		System.out.println("Hello from a thread. counts " + i);
	    	}
	    }
	}

	 public Integer start( String[] args )
	    {
	        System.out.println( "start()" );
	        
	        return null;
	    }
	    
	    public int stop( int exitCode )
	    {
	        System.out.println( "stop(" + exitCode + ")" );
	        
	        return exitCode;
	    }
	    
	    public void controlEvent( int event )
	    {
	        System.out.println( "controlEvent(" + event + ")" );
	        
	        if ( ( event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT )
	            && WrapperManager.isLaunchedAsService() )
	        {
	            System.out.println( "  Ignoring logoff event" );
	            // Ignore
	        }
	        else
	        {
	            WrapperManager.stop( 0 );
	        }
	    }
	    
	/**
	 * @param args
	 *
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println( " Init..." );
		
		WrapperManager.start(new Hello(), args);
	}

}

*/