package transactionProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import loader.ParticipantThreadPool;

import org.json.JSONException;

import threePhaseCommit.ThreePhaseCommitParticipant;

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
public class TransactionManager<R extends Request, P extends Participant<R>>{
	
	public static final String MANAGER = "MANAGER";

	private ParticipantThreadPool<R,P> launcher;
	private InetSocketAddress address;
	private ServerSocket inbox;

	public TransactionManager(Class<P> type, String config, InetSocketAddress address) throws IOException, JSONException {
		// this.coordinator = // run election protocol(addressBook); 
		this.launcher = new ParticipantThreadPool<R,P>(type, config);
		this.inbox = new ServerSocket(address.getPort());
		this.address = address;
	}
	
	public TransactionManager(Class<P> type, ParticipantThreadPool<R, P> threadpool, InetSocketAddress address) throws IOException {
		this.launcher = threadpool;
		this.inbox = new ServerSocket(address.getPort());
		this.address = address;
	}
		
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		this.inbox.close();
		super.finalize();
	}

	public boolean assertEqualState() {
		return this.launcher.assertEqualState();
	}
	
	public void initParticipants() {
		SortedSet<P> sortedParticipants = new TreeSet<P>(new ParticipantComparator<R, P>());
		sortedParticipants.addAll(launcher.getParticipants());
		
		P coordinator = sortedParticipants.first();
		
		System.out.println("TransactionManager: The coordinator is " + coordinator.getUid());
		
		Set<P> participants = launcher.getParticipants();
		for(final P p: participants){
			p.setManagerAddress(this.address);
			p.setCurrentCoordinator(coordinator);
		}
		
		this.launcher.start();
	}

	public synchronized boolean sendRequest(R request) {
		System.out.println("TransactionManager: client request, " + request);
		try{
			Set<P> participants = launcher.getParticipants();
			Message<R> init;
			Socket server;
			
			// Send Initialize message to each particpant
			for(final P p : participants){
				init = new Message<R>(Message.MessageType.INITIATE, MANAGER, p.getUid(), System.currentTimeMillis(), request);
				server = new Socket(p.getAddress().getAddress(), p.getAddress().getPort());
				writeObject(server.getOutputStream(), init);
				server.close();
			}
								
			boolean result = receiveMessage().getType() == Message.MessageType.COMMIT;
			
			try {
				Thread.sleep(ThreePhaseCommitParticipant.TIMEOUT+1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("TransactionManager: coordinator decided to " + ((result) ? "COMMIT" : "ABORT") + " " + request);
			return result;
		} catch(IOException e){
			e.printStackTrace();
			return false;
		}
	}
	
	public Message<R> receiveMessage() {
		Message<R> m = null;
		
		try {
			Socket client = this.inbox.accept();
			m = readObject(client.getInputStream());
			client.close();
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
}