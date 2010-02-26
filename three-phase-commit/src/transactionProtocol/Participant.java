package transactionProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import logger.Logger;
import logger.TransactionLogger;
import transactionProtocol.Message.MessageType;

public abstract class Participant<R extends Request> {
	
	public enum ExecutionState { READY, STARTED, FAILED, RECOVERING }
	
	// general information regarding a process
	private String uid;
	private Logger logger;
	private int ranking;
	private Participant<R> currentCoordinator;
	private String defaultVote; 
	private SortedSet<Participant<R>> upList;
	private Map<String, Participant<R>> participants;
	private ExecutionState executionState;
	
	// sockets for message passing
	private InetSocketAddress address;
	private ServerSocket inbox;
	private InetSocketAddress managerAddress;
	
	public Participant(String uid, int ranking, String defaultVote,
			InetSocketAddress address, 
			Set<Participant<R>> participants,
			String logFile) throws IOException {
		this.uid = uid;
		this.logger = new TransactionLogger(logFile, true);
		this.ranking = ranking;
		this.currentCoordinator = null;
		this.defaultVote = defaultVote;

		// address for TCP messaging
		this.address = address;
		this.inbox = new ServerSocket(this.address.getPort());
		this.setManagerAddress(null);
		
		// populate participants map
		if (participants != null) {
			this.setParticipants(participants);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.uid);
		sb.append("\n{");
		sb.append("\n  logfile=" + this.logger.getFilePath());
		sb.append("\n  ranking=" + this.ranking);
		sb.append("\n  defaultVote=" + this.defaultVote);
		sb.append("\n  address=" + this.address);
		sb.append("\n  participants={");
		for (Participant<R> p : this.participants.values()) {
			sb.append("\n      " + p.uid + ":" + p.getRanking());
		}
		sb.append("\n  }");
		sb.append("\n}");
		
		return sb.toString();
	}
	
	//
	// general participant methods
	//
	
	public void setParticipants(Set<Participant<R>> participants) {
		this.participants = new HashMap<String, Participant<R>>();
		this.upList = new TreeSet<Participant<R>>(new ParticipantComparator<R, Participant<R>>());
		
		// TODO: we must be in our own uplist!
		for (Participant<R> p : participants) {
			this.participants.put(p.getUid(), p);
			this.upList.add(p);
		}
	}
	
	public Set<Participant<R>> getParticipants() {
		return new HashSet<Participant<R>>(this.participants.values());
	}

	public void setUpList(SortedSet<Participant<R>> list){
		if (list.contains(this)) {
			list.remove(this);
		}
		this.upList = list;
	}
	
	public SortedSet<Participant<R>> getUpList() {
		return this.upList;
	}
	
	public void setLog(Logger logger) {
		this.logger = logger;
	}
	
	public Logger getLog() {
		return this.logger;
	}
	
	protected ExecutionState getExecutionState() {
		return this.executionState;
	}
	
	protected void setExecutionState(ExecutionState es) {
		this.executionState = es;
	}
	
	public String getDefaultVote() {
		return this.defaultVote;
	}

	public InetSocketAddress getAddress() {
		return this.address;
	}

	public String getUid() {
		return this.uid;
	}
	
	public int getRanking() {
		return this.ranking;
	}

	public boolean isCoordinator() {
		return this.currentCoordinator == this;
	}
	
	public void setCurrentCoordinator(Participant<R> newCoordinator) {
		this.currentCoordinator = newCoordinator;
	}
	
	public Participant<R> getCurrentCoordinator() {
		return this.currentCoordinator;
	}
			
	//
	// methods for changing state
	//
	
	public abstract void abort(R r);
	public abstract Vote castVote(R r);
	public abstract void commit(R r);
	public abstract void startCommitProtocol();
	protected abstract void startTerminationProtocol(R r);

	//
	// methods for sending/receiving data
	//

	
	public void broadcastMessage(MessageType messageType, R request) {
		this.broadcastMessage(this.getUpList(), messageType, request);
	}
	
	public void broadcastMessage(Set<Participant<R>> recipients, MessageType messageType, R request) {
		for (Participant<R> p : recipients) {
			sendMessage(p.getUid(), messageType, request);
		}
	}

	public boolean sendMessage(String uid, MessageType messageType, R request) {
		if (!this.participants.containsKey(uid)) {
			return false;
		}
		
		return sendMessage(uid, this.participants.get(uid).getAddress(), messageType, request);
	}
	
	public boolean sendMessage(String uid, InetSocketAddress address, MessageType messageType, R request) {
		try {
			Socket server = new Socket(address.getAddress(), address.getPort());
			writeObject(server.getOutputStream(), 
					new Message<R>(messageType, getUid(), uid, System.currentTimeMillis(),request));
			server.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public Message<R> receiveMessage(int timeout) throws MessageTimeoutException {
		Message<R> m = null;
		
		try {
			this.inbox.setSoTimeout(timeout);
			Socket client = this.inbox.accept();
			m = readObject(client.getInputStream());
			client.close();
		} catch (SocketTimeoutException e) {
			throw new MessageTimeoutException();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return m;
	}
	
	protected void resetInboxSocket() throws IOException {
		this.inbox.close();
		this.inbox = new ServerSocket(this.address.getPort());
	}
	
	private void writeObject(OutputStream stream, Message<R> m) throws IOException {
		ObjectOutputStream oos = 
			new ObjectOutputStream(stream);
		oos.writeObject(m);
		oos.close();
	}
	
	@SuppressWarnings("unchecked")
	private Message<R> readObject(InputStream stream) throws IOException, ClassNotFoundException {
		ObjectInputStream ois =
			new ObjectInputStream(stream);
		Object obj = ois.readObject();
		ois.close();
		
		if(obj != null && obj instanceof Message<?>) {
			return (Message<R>) obj;
		} else {
			throw new ClassNotFoundException("Message.readObject: Objec read from stream was not of type Message");
		}
	}

	public void setManagerAddress(InetSocketAddress managerAddress) {
		this.managerAddress = managerAddress;
	}

	public InetSocketAddress getManagerAddress() {
		return managerAddress;
	}
}
