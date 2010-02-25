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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import logger.Logger;
import logger.TransactionLogger;
import transactionProtocol.Message.MessageType;

public abstract class Participant<R extends Request> {

	// general information regarding a process
	private String uid;
	private boolean isCoordinator;
	private Logger logger;
	private int ranking; // used for election protocol
	private Participant<R> coordinator;
	private String defaultVote; 
	private Set<Participant<R>> upList;
	
	// sockets for message passing
	private Map<String, InetSocketAddress> addressBook;
	private InetSocketAddress address;
	private ServerSocket inbox;
	
	public Participant(String uid, int ranking, String defaultVote,
			InetSocketAddress address,
			Map<String, InetSocketAddress> addressBook, String logFile)
			throws IOException {
		this.uid = uid;
		this.isCoordinator = false;
		this.logger = new TransactionLogger(logFile, true);
		this.ranking = ranking;
		this.defaultVote = defaultVote;
		
		this.address = address;
		this.addressBook = (addressBook == null) ? new HashMap<String, InetSocketAddress>()
				: addressBook;
		if (this.addressBook.containsKey(this.getUid())) {
			this.addressBook.remove(this.getUid());
		}
		
		this.inbox = new ServerSocket(this.address.getPort());
		
		// Must be set by ParticipantThreadPool
		this.upList = null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.uid);
		sb.append("\n{");
		sb.append("\n  isCoordinator=" + this.isCoordinator);
		sb.append("\n  logfile=" + this.logger.getFilePath());
		sb.append("\n  ranking=" + this.ranking);
		sb.append("\n  defaultVote=" + this.defaultVote);
		sb.append("\n  address=" + this.address);
		sb.append("\n}");
		
		return sb.toString();
	}
	
	//
	// general participant methods
	//

	public void setUpList(Set<Participant<R>> list){
		this.upList = list;
	}
	
	public void setLog(Logger logger) {
		this.logger = logger;
	}
	
	public Logger getLog() {
		return this.logger;
	}

	public InetSocketAddress getAddress() {
		return this.address;
	}

	public String getUid() {
		return this.uid;
	}

	public boolean isCoordinator() {
		return this.isCoordinator;
	}

	public void setCoordinator(boolean isCoordinator) {
		this.isCoordinator = isCoordinator;
	}
	
	public void setAddressBook(Map<String, InetSocketAddress> addressBook) {
		this.addressBook = addressBook;
		if (this.addressBook.containsKey(this.getUid())) {
			this.addressBook.remove(this.getUid());
		}
	}
	
	public int getRanking(){
		return this.ranking;
	}
	
	//
	// methods for changing state
	//
	
	public abstract void abort();
	public abstract Vote castVote(R r);
	public abstract void commit();
	public abstract void startCommitProtocol();
	public abstract void startTerminationProtocol();

	//
	// methods for sending/receiving data
	//

	public void broadcastMessage(MessageType messageType, R request) {
		for (Participant<R> p : this.upList) {
			sendMessage(p.getUid(), messageType, request);
		}
	}

	public void sendMessage(String uid, MessageType messageType, R request) {
		sendMessage(uid, this.addressBook.get(uid), messageType, request);
	}
	
	public void sendMessage(String uid, InetSocketAddress address, MessageType messageType, R request) {
		try {
			Socket server = new Socket(address.getAddress(), address.getPort());
			Message.writeObject(server.getOutputStream(), 
					new Message(messageType, getUid(), uid, System.currentTimeMillis(), request));
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
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
	
	private void writeObject(OutputStream stream, Message m) throws IOException {
		ObjectOutputStream oos = 
			new ObjectOutputStream(stream);
		oos.writeObject(m);
		oos.close();
	}
	
	private Message<R> readObject(InputStream stream) throws IOException, ClassNotFoundException {
		ObjectInputStream ois =
			new ObjectInputStream(stream);
		Object obj = ois.readObject();
		ois.close();
		
		if(obj != null && obj instanceof Message) {
			return (Message<R>) obj;
		} else {
			throw new ClassNotFoundException("Message.readObject: Objec read from stream was not of type Message");
		}
	}
	
	public Participant<R> getCoordintar(){
		return coordinator;
	}
	
	public Set<Participant<R>> getUpList(){
		return upList;
	}

}
