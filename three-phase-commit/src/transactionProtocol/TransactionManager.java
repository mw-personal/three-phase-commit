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
import java.util.List;
import java.util.Set;

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
public abstract class TransactionManager<R extends Request, 
	P extends Participant<R>> implements TransactionProtocol{
	
	private static final int INFINITE_TIMEOUT = 0;
	public static final String MANAGER = "MANAGER";
	
	// private P coordinator;
	private Set<P> addressBook;
	private ParticipantThreadPool<R,P> launcher;
	private InetSocketAddress address;
	private ServerSocket inbox;

	public TransactionManager(Class<P> type, String config, int port, InetSocketAddress address) throws IOException, JSONException {
		// this.coordinator = // run election protocol(addressBook); 
		this.launcher = new ParticipantThreadPool<R,P>(type, config);
		this.addressBook = this.launcher.getParticipants();
		this.inbox = new ServerSocket(port);
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
	public synchronized boolean sendRequest(R request){
		try{
			Set<P> participants = launcher.getParticipants();
			Message<R> init;
			Socket server;
			
			// Send Initialize message to each particpant
			for(final P p : participants){
				init = new Message<R>(Message.MessageType.INITIATE, this.MANAGER, p.getUid(), System.currentTimeMillis(), request);
				server = new Socket(p.getAddress().getAddress(), p.getAddress().getPort());
				writeObject(server.getOutputStream(), init);
			}
			
			Message<R> m = receiveMessage(INFINITE_TIMEOUT);
			if(m.getType().equals(Message.MessageType.COMMIT)){
				return true;
			} 
			
		} catch(IOException e){
			e.printStackTrace();
		} catch(MessageTimeoutException e){
		}
		return false;
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