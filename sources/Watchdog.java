import java.util.Timer;
import java.util.TimerTask;

public final class Watchdog{
	
	private final Timer timer;
	private final TimerTask task;
	
	private final long interval;
	
	private boolean running = false;
	
	private volatile long lastActivity;
	
	public Watchdog(long interval, long maxTimeout, Runnable callback){
		this.interval = interval;
		timer = new Timer();
		task = new TimerTask(){
			@Override
			public void run(){
				if(System.currentTimeMillis() - lastActivity > maxTimeout){
					if(callback != null) callback.run();
				}
			}
		};
	}
	
	public void start(){
		if(!running){
			running = true;
			kick();
			timer.scheduleAtFixedRate(task, 0, interval);
		}
	}
	
	public void stop(){
		if(running){
			task.cancel();
			timer.purge();
			timer.cancel();
		}
	}
	
	public void kick(){
		lastActivity = System.currentTimeMillis();
	}
}