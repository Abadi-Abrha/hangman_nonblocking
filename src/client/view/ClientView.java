package client.view;

import client.net.ClientConnection;

public class ClientView implements Runnable {
	public static final String PROMPT = "$ ";
	private ClientConnection server;
	
    public void start() {
        server = new ClientConnection();
        new Thread(this).start();
    }
	@Override
	public void run() {
		try {
			new Thread(server).start();
		}
		catch(Exception ex) {
			println("Exception: "+ex.getMessage());
		}
	}
	
	//Thread safe standard output
	public synchronized void print(String output) {
		System.out.print(output);
	}
	public synchronized void println(String output) {
		System.out.println(output);
	}
}
