package loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.json.JSONException;

import transactionProtocol.*;

public class ParticipantThreadPool<P extends Participant<? extends Request>> {
	
	private boolean started;
	private Map<String, String> pointsToFail;
	private Map<String, P> participantMap;
	private Map<String, Thread> participantThreads;
	private List<String> failedParticipants;
	
	public ParticipantThreadPool(Class<P> type, String configFile) throws IOException, JSONException {
		this(type, new File(configFile));
	}
	
	public ParticipantThreadPool(Class<P> type, File configFile) throws IOException, JSONException {
		ParticipantConfiguration<P> pc = new ParticipantConfiguration<P>(type, configFile);
		
		List<P> peeps = pc.getParticipants();
		this.pointsToFail = pc.getPointsToFail();
		this.participantMap = new HashMap<String, P>();
		this.participantThreads = new HashMap<String, Thread>();
		this.failedParticipants = new ArrayList<String>();

		
		for (P p : peeps) {
			this.participantMap.put(p.getUid(), p);
			this.participantThreads
					.put(p.getUid(), new ParticipantThread<P>(p, this.pointsToFail.get(p.getUid())));
		}
		this.started = false;
	}
	
	public void start() {
		if (this.started) {
			throw new IllegalStateException();
		}
		
		for(Thread t : this.participantThreads.values()) { 
			if (!t.isAlive())
				t.start();
		}
		this.started = true;
	}
	
	public boolean startParticipant(String uid) {
		Thread t = this.participantThreads.get(uid);
		if (t != null) {
			if (t.isAlive()) {
				return false;
			} else {
				t.start();
				return true;
			}
		}
		
		P participant = this.participantMap.get(uid);
		if (participant == null) {
			return false;
		}
				
		ParticipantThread<P> pt = new ParticipantThread<P>(participant, this.pointsToFail.get(participant.getUid()));
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
		
	public List<P> getParticipants(){
		return new ArrayList<P>(this.participantMap.values());
	}
	
	public void stop() {
		if (!this.started) {
			throw new IllegalStateException();
		}
		
		for(Thread t : this.participantThreads.values()) { 
			t.interrupt();
		}
	}
}
