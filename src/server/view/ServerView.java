package server.view;
import client.net.ClientConnection;

public class ServerView implements Runnable {
	public void start() {
        new ClientConnection();
        new Thread(this).start();
    }
	
	@Override
	public void run() {
		try {
				//
		}
		catch(Exception ex) {
			println("Exception: "+ex.getMessage());
		}
	}
	
	//Thread safe standard output
	public synchronized void println(String output) {
		System.out.println(output);
	}
}
