package transactionProtocol;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import logger.Logger;
import logger.TransactionLogger;

public abstract class Participant<R extends Request> {

	// general information regarding a process
	private String uid;
	private boolean isCoordinator;
	private Logger logger;
	private int ranking; // used for election protocol
	// TODO: we need a better way of handling this
	//       it is likely each participant will need
	//       somesort of innerstate.  might be easier
	//       to begina a participant at some given state
	// TODO: can different protocols put participants in
	//       different states? how do we genercise that?
	private String defaultVote; 
	

	// sockets for message passing
	private Map<String, InetSocketAddress> addressBook;
	private InetSocketAddress address;
	private ServerSocket inbox;
	
	// sockets for heartbeat monitoring
//	private Map<String, InetSocketAddress> heartBook;
	private InetSocketAddress heartAddress;

	// handles for protocols
	private Protocol commitProtocol;
	private Protocol terminationProtocol;
//	private Protocol electionProtocol;

//	public Participant(String uid, InetAddress ipAddress, int port,
//			String logFile) throws IOException {
//		this(uid, new InetSocketAddress(ipAddress, port), logFile,
//				new HashMap<String, InetSocketAddress>());
//	}
//
//	public Participant(String uid, InetAddress ipAddress, int port,
//			String logFile, String configFile) throws IOException {
//		this(uid, new InetSocketAddress(ipAddress, port), logFile,
//				createAddressBook(configFile));
//	}
//
//	public Participant(String uid, InetAddress ipAddress, int port,
//			String logFile, Map<String, InetSocketAddress> addressBook)
//			throws IOException {
//		this(
//				uid,
//				new InetSocketAddress(ipAddress, port),
//				logFile,
//				(addressBook == null) ? new HashMap<String, InetSocketAddress>()
//						: addressBook);
//	}

	
	public Participant(String uid, int ranking, String defaultVote,
			InetSocketAddress address, InetSocketAddress heartAddress,
			Map<String, InetSocketAddress> addressBook,
			Map<String, InetSocketAddress> heartBook, String logFile)
			throws IOException {
		this.uid = uid;
		this.isCoordinator = false;
		this.logger = new TransactionLogger(logFile, true);
		this.ranking = ranking;
		this.defaultVote = defaultVote;
		
		this.address = address;
		this.addressBook = (addressBook == null) ? new HashMap<String, InetSocketAddress>()
				: addressBook;
		this.heartAddress = heartAddress;
//		this.heartBook = (heartBook == null) ? new HashMap<String, InetSocketAddress>()
//				: heartBook;
		
		this.inbox = new ServerSocket(this.address.getPort());
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
		sb.append("\n  heartAddress=" + this.heartAddress);
		sb.append("\n}");
		
		return sb.toString();
	}
	
//	public Participant(String uid, InetSocketAddress address, String logFile)
//			throws IOException {
//		this(uid, address, logFile, new HashMap<String, InetSocketAddress>());
//	}
//
//	public Participant(String uid, InetSocketAddress address, String logFile,
//			String configFile) throws IOException {
//		this(uid, address, logFile, createAddressBook(configFile));
//	}
//
//	public Participant(String uid, InetSocketAddress address, String logFile,
//			Map<String, InetSocketAddress> addressBook) throws IOException {
//		this.uid = uid;
//		this.isCoordinator = false;
//		this.logger = new TransactionLog(logFile, true);
//		this.addressBook = (addressBook == null) ? new HashMap<String, InetSocketAddress>()
//				: addressBook;
//		this.address = address;
//		this.inbox = new ServerSocket(this.address.getPort());
//	}

	//
	// general participant methods
	//

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

	//
	// methods for changing state
	//
	
	//public abstract void start();
	public abstract void abort();
	public abstract Vote castVote(R r);
	public abstract void commit();

	//
	// methods for getting/setting protocol handles
	//

	public Protocol getCommitProtocol() {
		return this.commitProtocol;
	}

	public void setCommitProtocol(Protocol p) {
		this.commitProtocol = p;
		this.commitProtocol.start(this);
	}

	public Protocol getTerminationProtocol() {
		return this.terminationProtocol;
	}

	public void setTerminationProtocol(Protocol p) {
		this.terminationProtocol = p;
	}

	//
	// methods for sending/receiving data
	//

	public void setAddressBook(Map<String, InetSocketAddress> addressBook) {
		this.addressBook = addressBook;
	}

	public void broadcastMessage(Message m) {
		for (InetSocketAddress isa : this.addressBook.values()) {
			sendMessage(isa, m);
		}
	}

	public void sendMessage(InetSocketAddress address, Message m) {
		try {
			Socket server = new Socket(address.getAddress(), address.getPort());
			Message.writeObject(server.getOutputStream(), m);
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	//TODO: add to interface if we need this.
//	public Map<String, Message> receiveFromAll(int timeout, Protocol timeoutProtocol) {
//		// we don't want to receive anything from ourselves.
//		// TODO: this is only necessary if we exist in our own addressBook
//		Set<String> keys = this.addressBook.keySet();
//		keys.remove(this.uid);
//
//		Map<String, Message> result = new HashMap<String, Message>();
//
//		// set a timeout
//		try {
//			this.inbox.setSoTimeout(timeout);
//			for (String recipient : keys) {
//				try {
//					Socket client = this.inbox.accept();
//					BufferedReader in = new BufferedReader(
//							new InputStreamReader(client.getInputStream()));
//
//					result.put(recipient, Message.valueOf(in.readLine()));
//					in.close();
//					client.close();
//				} catch (SocketTimeoutException e) {
//					timeoutProtocol.start(this);
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return result;
//	}

	public Message receiveMessage(int timeout) throws MessageTimeoutException {
		Message m = null;
		
		try {
			this.inbox.setSoTimeout(timeout);
			Socket client = this.inbox.accept();
			m = Message.readObject(client.getInputStream());
			client.close();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return m;
	}

	//
	// private methods
	//

//	private static Map<String, InetSocketAddress> createAddressBook(
//			String configFile) throws IOException {
//		HashMap<String, InetSocketAddress> result = new HashMap<String, InetSocketAddress>();
//		BufferedReader f = new BufferedReader(new FileReader(configFile));
//
//		String s = f.readLine();
//		while (s != null) {
//			String[] is = s.split("::");
//			result.put(is[0], new InetSocketAddress(is[1], Integer
//					.parseInt(is[2])));
//			s = f.readLine();
//		}
//
//		f.close();
//
//		return result;
//	}

}
