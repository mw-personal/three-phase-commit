package loader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;
import java.util.HashMap;

import org.json.JSONException;

import transactionProtocol.*;

public class ParticipantLauncher<P extends Participant<? extends Request>> {
	
	private List<P> peeps;
	private ExecutorService executorService;
	private HashMap<String, Future> threads;
	
	public ParticipantLauncher(Class<P> type, String configFile) {
		this(type, new File(configFile));
	}
	
	public ParticipantLauncher(Class<P> type, File configFile) {
		try {
			this.peeps = ParticipantConfiguration.<P>
				readParticipantConfiguration(type, configFile);
			threads = new HashMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.executorService = Executors.newCachedThreadPool();
	}
	
	/**
	 * Start RunnableParticipant threads
	 */
	public void start() {
		for(final P p : this.peeps){ 
			threads.put(p.getUid(), this.executorService.submit(new RunnableParticipant(p)));
		}
	}
	
	public boolean stopParticipant(String uid){
		return threads.get(uid).cancel(true);
	}
	
	/*
	public void start() {
		for (final P p : this.peeps) {
			this.executorService.submit(new Runnable() {
				public void run() {
					System.out.println("Thread " + Thread.currentThread().getId() + 
							":\n" + p + "\n====================");
				}
			});
		}
	}*/
	
	public void killParticipant(){
		
	}
	
	public void stop() {
		this.executorService.shutdownNow();
	}

}
