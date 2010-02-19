package atomicCommit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import logger.Logger;
import logger.TransactionLog;

public class Participant implements ProtocolParticipant {

	// general information regarding a process
	private String uid;
	private boolean isCoordinator;
	private TransactionLog logger;

	// sockets for message passing
	private Map<String, InetSocketAddress> addressBook;
	private InetSocketAddress address;
	private ServerSocket inbox;

	// handles for protocols
	private Protocol commitProtocol;
	private Protocol terminationProtocol;
	private Protocol electionProtocol;

	public Participant(String uid, InetAddress ipAddress, int port,
			String logFile) throws IOException {
		this(uid, new InetSocketAddress(ipAddress, port), logFile,
				new HashMap<String, InetSocketAddress>());
	}

	public Participant(String uid, InetAddress ipAddress, int port,
			String logFile, String configFile) throws IOException {
		this(uid, new InetSocketAddress(ipAddress, port), logFile,
				createAddressBook(configFile));
	}

	public Participant(String uid, InetAddress ipAddress, int port,
			String logFile, Map<String, InetSocketAddress> addressBook)
			throws IOException {
		this(
				uid,
				new InetSocketAddress(ipAddress, port),
				logFile,
				(addressBook == null) ? new HashMap<String, InetSocketAddress>()
						: addressBook);
	}

	public Participant(String uid, InetSocketAddress address, String logFile)
			throws IOException {
		this(uid, address, logFile, new HashMap<String, InetSocketAddress>());
	}

	public Participant(String uid, InetSocketAddress address, String logFile,
			String configFile) throws IOException {
		this(uid, address, logFile, createAddressBook(configFile));
	}

	public Participant(String uid, InetSocketAddress address, String logFile,
			Map<String, InetSocketAddress> addressBook) throws IOException {
		this.uid = uid;
		this.isCoordinator = false;
		this.logger = new TransactionLog(logFile, true);
		this.addressBook = (addressBook == null) ? new HashMap<String, InetSocketAddress>()
				: addressBook;
		this.address = address;
		this.inbox = new ServerSocket(this.address.getPort());
	}

	//
	// general participant methods
	//

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

	public void setCoordinator(boolean t) {
		this.isCoordinator = t;
	}

	public void abort() {
		// TODO Auto-generated method stub

	}

	public Vote castVote() {
		// TODO Auto-generated method stub
		return null;
	}

	public void commit() {
		// TODO Auto-generated method stub

	}

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
			PrintWriter out = new PrintWriter(server.getOutputStream());

			// write uuid::message
			out.write(m.name());
			out.close();
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
		String data = null;
		try {
			this.inbox.setSoTimeout(timeout);
			Socket client = this.inbox.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(client
					.getInputStream()));

			data = in.readLine();
			in.close();
			client.close();
		} catch (SocketTimeoutException e) {
			throw new MessageTimeoutException(e);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (data != null) {
			return Message.valueOf(data);
		} else {
			return null;
		}
	}

	//
	// private methods
	//

	private static Map<String, InetSocketAddress> createAddressBook(
			String configFile) throws IOException {
		HashMap<String, InetSocketAddress> result = new HashMap<String, InetSocketAddress>();
		BufferedReader f = new BufferedReader(new FileReader(configFile));

		String s = f.readLine();
		while (s != null) {
			String[] is = s.split("::");
			result.put(is[0], new InetSocketAddress(is[1], Integer
					.parseInt(is[2])));
			s = f.readLine();
		}

		f.close();

		return result;
	}

}
