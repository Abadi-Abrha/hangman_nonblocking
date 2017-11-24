package client.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;

import client.view.ClientView;

public class ClientConnection implements Runnable {
	private static BufferedReader userInputReader;
	
	private static SocketChannel sChannel;
	private static Selector selector;
	private static int port = 8989;
	private static ClientView view;
	private static boolean stop;
	
	@Override
	public void run() {
		view = new ClientView();
		stop = false;
		try {
			initiateConnection();
			initiateSelector();
			
			userInputReader = new BufferedReader(new InputStreamReader(System.in));//Should be nio
			while(true) {
				if(selector.select() <= 0) continue;
				
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				while(keys.hasNext()) {
					SelectionKey key = (SelectionKey) keys.next();
					keys.remove();
					
					if(key.isConnectable()) {
						if(!processConncetion(key)) {
							stop = true; break; 
						}
					}
					if(key.isReadable()) { view.print(processRead(key)); }
					if(key.isWritable()) {  
						if(processWrite(key)) {
							stop = true; break; 
						}
					}
				}
				if(stop) break;
			}
			view.print("Goodbye!");
			sChannel.close();
			sChannel.keyFor(selector).cancel();
			selector.close();
		}
		catch(Exception ex) {
			view.println(ex.getMessage());
		}
	}
	private static void initiateConnection() throws Exception {		 
		sChannel = SocketChannel.open();
		sChannel.configureBlocking(false);
		sChannel.connect(new InetSocketAddress("localhost", port));
	}
	private static void initiateSelector() throws Exception {
		selector = Selector.open();
		int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
		sChannel.register(selector, operations);
	}
	private static boolean processConncetion(SelectionKey key) throws Exception {
		SocketChannel channel = (SocketChannel) key.channel();
		while(channel.isConnectionPending()) {
			channel.finishConnect();
		}
		view.print(startupMessage());
		key.interestOps(SelectionKey.OP_WRITE);
		return true;
	}
	private String processRead(SelectionKey key) throws Exception {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		channel.read(buffer);
		
		buffer.flip();
		Charset charset = Charset.forName("UTF-8");
		CharsetDecoder decoder = charset.newDecoder();
		CharBuffer charBuffer = decoder.decode(buffer);
		buffer.clear();
		
		key.interestOps(SelectionKey.OP_WRITE);
		
		return charBuffer.toString();
	}
	private static boolean processWrite(SelectionKey key) throws Exception {
        String msg = userInputReader.readLine(); 
        if(msg.length() != 0) {
        	SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
            channel.write(buffer);
    		key.interestOps(SelectionKey.OP_READ);
            if (msg.equals("0")) return true;
            return false;
        }
        key.interestOps(SelectionKey.OP_WRITE);
    	return false;
	}
	private static String startupMessage() {
		String message = "Welcome to hangman game!\n\n"+
					"Game rules:\n"+
					". Only guess the whole word or one letter at a time,\n"+
					". Part of the word is considered as a word,\n"+
					". Any time you submit \'0\', game will quit,\n"+
					". No more attemts implies game over,\n"+
					"-----------------------------------------\n"+
					"Use '1' - to Start the game. '0' to quit.\n"+
					"$ ";
		return message;
	}
}
