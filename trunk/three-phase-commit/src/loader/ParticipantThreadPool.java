package loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.HashMap;

import org.json.JSONException;

import transactionProtocol.*;

public class ParticipantThreadPool<P extends Participant<? extends Request>> {
	
	private Map<String, P> participantMap;
	private Map<String, Thread> participantThreads;
	private List<String> failedParticipants;
	
	public ParticipantThreadPool(Class<P> type, String configFile) throws IOException, JSONException {
		this(type, new File(configFile));
	}
	
	public ParticipantThreadPool(Class<P> type, File configFile) throws IOException, JSONException {
		List<P> peeps = ParticipantConfiguration
				.<P>readParticipantConfiguration(type, configFile);
		this.participantMap = new HashMap<String, P>();
		this.participantThreads = new HashMap<String, Thread>();
		this.failedParticipants = new ArrayList<String>();

		for (P p : peeps) {
			this.participantMap.put(p.getUid(), p);
			this.participantThreads
					.put(p.getUid(), new ParticipantThread<P>(p));
		}
	}
	
	/**
	 * Start RunnableParticipant threads
	 */
	public void start() {
		for(Thread t : this.participantThreads.values()) { 
			t.start();
		}
	}
	
	public boolean startParticipant(String uid) {
		P participant = this.participantMap.get(uid);
		if (participant == null) {
			return false;
		}
				
		ParticipantThread<P> pt = new ParticipantThread<P>(participant);
		this.failedParticipants.remove(uid);
		this.participantThreads.put(uid, pt);
		pt.start();
		
		return true;
	}
	
	public boolean stopParticipant(String uid) {
		Thread t = this.participantThreads.remove(uid);
		if (t == null) {
			return false;
		}
		
		t.interrupt();
		this.failedParticipants.add(uid);
		return true;
	}
	
	public List<String> getFailedParticipants() {
		return this.failedParticipants;
	}
	
//	public HashMap<String, Future> getThreads(){
//		return threads;
//	}
	
	public List<P> getParticipants(){
		return new ArrayList<P>(this.participantMap.values());
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
	
	
	public void stop() {
		this.executorService.shutdownNow();
	}

}
