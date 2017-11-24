package server.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;

import server.controller.HangmanController;
import server.model.HangmanModel;
import server.view.ServerView;

public class HangmanServer  implements Runnable {
	private static int port = 8989;
	private static InetAddress serverIPAddress;
	private static ServerSocketChannel ssChannel;
	private static Selector selector;
	private static HangmanController game;
	private static ServerView view;
	private static String message;
	
	public HangmanServer() {
		game = new HangmanController();
		view = new ServerView();
	}
	public void start() {
        new Thread(this).start();
	}
	public void run() {
		game = new HangmanController();
		try {
			initiateConnection();
			initiateSelector();
			view.println("Hangman server is running...");
			
			//Handle communication with non-blocking
			while(true) {
				//if (selector.select() <= 0) { continue; }
				selector.selectNow();
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				while(keys.hasNext()) {
					SelectionKey key = keys.next();
					keys.remove();
					
					if(!key.isValid()) continue;
					if(key.isAcceptable()) {processConnection(key);}
					else if(key.isReadable()) {
						String msg = processRead(key);
						if(handleGame(msg, key)) {
							key.interestOps(SelectionKey.OP_WRITE);
						}
					}
					else if(key.isWritable()) {processWrite(key);}
				}
			}
		}
		catch(Exception ex) {
			view.println("Exception: " + ex.getMessage());
		}
	}
	private void initiateConnection() throws Exception {
		serverIPAddress = InetAddress.getByName("localhost");
		ssChannel = ServerSocketChannel.open();
		ssChannel.configureBlocking(false);
		ssChannel.socket().bind(new InetSocketAddress(serverIPAddress, port));
	}
	private void initiateSelector() throws Exception {
		selector= Selector.open();
		ssChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
	private static void processConnection(SelectionKey key) throws Exception {
		ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
		SocketChannel sChannel = (SocketChannel) ssChannel.accept();
		sChannel.configureBlocking(false);
		HangmanModel model = new HangmanModel();
		model.setPlaying(false);
		sChannel.register(key.selector(), SelectionKey.OP_READ, model);// ByteBuffer.allocate(1024));
		view.println("A client is connected!");
	}
	private static String processRead(SelectionKey key) throws Exception {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		channel.read(buffer);
		
		buffer.flip();
		Charset charset = Charset.forName("UTF-8");
		CharsetDecoder decoder = charset.newDecoder();
		CharBuffer charBuffer = decoder.decode(buffer);
		buffer.clear();
		return charBuffer.toString();
	}
	private static void processWrite(SelectionKey key) throws Exception {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.flip();
		
		HangmanModel model = (HangmanModel) key.attachment();
		message = model.getMessage();
		channel.write(Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap(message)));
		if(buffer.hasRemaining()) buffer.compact();
		else buffer.clear();
		key.interestOps(SelectionKey.OP_READ);
	}
	private static boolean handleGame(String msg, SelectionKey key) throws Exception{	
		message = "";
		HangmanModel model = (HangmanModel) key.attachment();
		boolean playing = model.getPlaying();
		if(msg.equals("0")) {
			view.println("Player opted-out!");
			key.channel().close();
			key.cancel();
			if(key.channel().isRegistered()) {
				key.channel().keyFor(selector).cancel();
			}
			return false;
		}
		else if(msg.equals("1") && !playing) {
			model = game.instantiateTheGame();
			String str = model.getMessage();
			for (int i=0;i<str.length();i++)
				message +="_";
			message += "___\n" + str;
			playing = true;
		}
		else if(msg.length() >= 1 && playing) {
			model = game.onGame(model, msg);
			message = model.getMessage();
			if(message.contains("Success!") || message.contains("Game over!")) {
				playing = false;
			}
		}
		else{
			message = "Please enter a valid key, try again. $ ";
		}
		model.setPlaying(playing);
		model.setMessage(message);
		key.attach(model);
		return true;
	}
}
