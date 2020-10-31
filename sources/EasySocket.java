import java.io.IOException;
import java.net.Socket;

import java.io.IOException;
import java.net.Socket;

public abstract class EasySocket extends Thread{
	
	private final Socket socket;
	
	private final boolean hasWatchdog;
	private Watchdog watchdog;
	private final int watchdogInterval;
	private final int timeout;
	
	private static final int DEFAULT_TIMEOUT = 60_000;

	protected long bytesSent;
	protected long bytesReceived;

	private final Callback callback;
	
	protected volatile boolean hadTimeout;
	
	private static final double MAX_PACKET_SIZE = 4_096_000;

	protected EasySocket(Socket socket){
		this(socket, 0, DEFAULT_TIMEOUT, null);
	}
	
	protected EasySocket(Socket socket, int watchdogInterval, int timeout){
		this(socket, watchdogInterval, timeout, null);
	}
	
	protected EasySocket(Socket socket, int watchdogInterval, int timeout, Callback callback){
		this.bytesSent = 0L;
		this.bytesReceived = 0L;
		this.hadTimeout = false;
		this.socket = socket;
		this.watchdogInterval = watchdogInterval;
		this.hasWatchdog = watchdogInterval > 0;
		this.timeout = timeout;
		this.callback = callback;
	}
	
	@Override
	public synchronized void start(){
		super.start();
		if(hasWatchdog){
			watchdog = new Watchdog(watchdogInterval, timeout, this::timeout);
			watchdog.start();
		}
	}
	
	protected final void timeout(){
		hadTimeout = true;
		update(Update.State.TIMEOUT);
		close();
	}
	
	protected final void stopWatchdog(){
		if(watchdog != null) watchdog.stop();
	}
	
	protected final void update(Update.State state){
		if(callback != null) callback.update(new Update(bytesSent, bytesReceived, state));
	}
	
	protected final void kick(){
		if(watchdog != null) watchdog.kick();
	}

	protected final void send(byte[]... bytes) throws IOException{
		for(byte[] array : bytes){
			send(array);
		}
	}
	
	protected final void send(byte[] bytes) throws IOException{
		if(bytes.length > MAX_PACKET_SIZE){
			int subPayloads = (int)Math.ceil(bytes.length / MAX_PACKET_SIZE);
			
			int totalSent = 0;
			
			for(int i = 0; i < subPayloads; i++){
				byte[] nextPayload = new byte[(int)Math.min(bytes.length - totalSent, MAX_PACKET_SIZE)];
				System.arraycopy(bytes, (int)(i * MAX_PACKET_SIZE), nextPayload, 0, nextPayload.length);
				totalSent += nextPayload.length;
				send(nextPayload);
			}
		
		}else{
			socket.getOutputStream().write(bytes);
			socket.getOutputStream().flush();
			kick();
			bytesSent += bytes.length;
		}
	}
	
	protected final void send(byte b) throws IOException{
		socket.getOutputStream().write(b);
		kick();
		bytesSent++;
		socket.getOutputStream().flush();
	}
	
	protected final byte read() throws IOException{
		byte[] bytes = readNBytes(1);
		return bytes[0];
	}

	protected final byte[] readNBytes(int n) throws IOException{
		byte[] bytes = new byte[n];
		int read = 0;
		do{
			int available = socket.getInputStream().available();
			if(available > 0){
				int toRead = n - read;
				toRead = Math.min(available, toRead);
				int status = socket.getInputStream().read(bytes, read, toRead);
				if(status == -1){
					throw new IOException();
				}
				kick();
				bytesReceived += toRead;
				update(Update.State.UPDATE);
				read += toRead;
			}
		}while(read < n);
		return bytes;
	}

	protected final void close(){
		try{
			socket.close();
			stopWatchdog();
		}catch(IOException ignored){}
	}
}
