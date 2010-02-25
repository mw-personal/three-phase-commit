package loader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import transactionProtocol.Participant;
import transactionProtocol.ParticipantThread;
import transactionProtocol.Request;

public class ParticipantThreadPool<R extends Request, P extends Participant<R>> {
	
	private boolean started;
	private Map<String, String> pointsToFail;
	private Map<String, P> participantMap;
	private Map<String, Thread> participantThreads;
	private Set<String> failedParticipants;
	
	public ParticipantThreadPool(Class<P> type, String configFile) throws IOException, JSONException {
		this(type, new File(configFile));
	}
	
	public ParticipantThreadPool(Class<P> type, File configFile) throws IOException, JSONException {
		ParticipantConfiguration<R, P> pc = new ParticipantConfiguration<R, P>(type, configFile);
		
		Set<P> peeps = pc.getParticipants();
		this.pointsToFail = pc.getPointsToFail();
		this.participantMap = new HashMap<String, P>();
		this.participantThreads = new HashMap<String, Thread>();
		this.failedParticipants = new HashSet<String>();

		
		for (P p : peeps) {
			this.participantMap.put(p.getUid(), p);
			this.participantThreads
					.put(p.getUid(), new ParticipantThread<R, P>(p, this.pointsToFail.get(p.getUid())));
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
				
		ParticipantThread<R,P> pt = new ParticipantThread<R,P>(participant, this.pointsToFail.get(participant.getUid()));
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
	
	public Set<String> getFailedParticipants() {
		return this.failedParticipants;
	}
		
	public Set<P> getParticipants(){
		return new HashSet<P>(this.participantMap.values());
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
