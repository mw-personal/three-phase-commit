package transactionProtocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.json.JSONException;

import com.sun.tools.internal.xjc.reader.Messages;

import loader.*;

import applications.banking.BankingParticipant;

/**
 * The TransactionManager is responsible for initiating the RunnableParticipant threads
 * as well as communicating between the outside world and the Distributed System.
 * 
 * The TransactionManager speaks to paticipants and the outside world through TCP, however the coordinator and the outside
 * world speak to the TransactionManager directly (using sendMessage() and sendReply())
 * @author cjlax26
 *
 * @param <P>
 */
public abstract class TransactionManager<P extends Participant<? extends Request>> {
	
	public static final String MANAGER = "MANAGER";
	
	// private P coordinator;
	private List<P> addressBook;
	private ParticipantThreadPool<P> launcher;
	private ServerSocket inbox;

	public TransactionManager(Class<P> type, String config, int port) throws IOException, JSONException {
		// this.coordinator = // run election protocol(addressBook); 
		this.launcher = new ParticipantThreadPool<P>(type, config);
		this.addressBook = this.launcher.getParticipants();
		this.inbox = new ServerSocket(port);
		
		// set protocols for participants
		for (P p : this.addressBook) {
			p.setCommitProtocol(getCommitProtocol());
			p.setTerminationProtocol(getTerminationProtocol());
		}
	}
	
	public abstract Protocol getCommitProtocol();
	public abstract Protocol getTerminationProtocol();
	
	public void initParticipants() {		
		this.launcher.start();
	}
	
	/**
	 * API for outside world to send a request to the DS.
	 * @param request
	 */
	public synchronized void sendRequest(Request request){
		try{
			List<P> participants = launcher.getParticipants();
			Message init;
			Socket server;
			// Send Initialize message to each particpant
			for(final P p : participants){
				init = new Message(Message.MessageType.INITIATE, this.MANAGER, p.getUid(), System.currentTimeMillis(), request);
				server = new Socket(p.getAddress().getAddress(), p.getAddress().getPort());
				Message.writeObject(server.getOutputStream(), init);
			}
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
//	/**
//	 * Send reply back to outside world
//	 */
//	public void sendReply(Reply reply){
//		try{
//			Socket client = reply.getServer().accept();
//			reply.writeObject(client.getOutputStream());
//			client.close();
//		} catch(Exception e){
//			e.printStackTrace();
//		}
//	}
}