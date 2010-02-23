package transactionProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import loader.ParticipantThreadPool;

import org.json.JSONException;

public class TransactionManagerFactory {
	
	private static TransactionManagerFactory factory;
	
	private HashMap<Class<? extends Participant<? extends Request>>, TransactionManager<? extends Participant<? extends Request>>> managers;
	
	private TransactionManagerFactory(){
		
	}
	
	public static TransactionManagerFactory getTransactionManagerFactory(){
		return (factory == null) ? new TransactionManagerFactory() : factory;
	}
	
	public <P extends Participant<? extends Request>> TransactionManager<P> getTransactionManager(Class<P> type, String config, int port){
		
		TransactionManager<P> manager;
		if( managers.get(type) == null){
			try {
				manager = new TransactionManagerImp<P>(type, config, port);
				managers.put(type, manager);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
		 
	}
	
	
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
	private class TransactionManagerImp<P extends Participant<? extends Request>> implements TransactionManager<P> {
		
		public static final String MANAGER = "MANAGER";
		
		// private P coordinator;
		private List<P> addressBook;
		private ParticipantThreadPool<P> launcher;
		private ServerSocket inbox;

		private TransactionManagerImp(Class<P> type, String config, int port) throws IOException, JSONException {
			// this.coordinator = // run election protocol(addressBook); 
			this.launcher = new ParticipantThreadPool<P>(type, config);
			this.addressBook = this.launcher.getParticipants();
			this.inbox = new ServerSocket(port);
		}
		
		public void initParticipants() {
			launcher.start();
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
		
//		/**
//		 * Send reply back to outside world
//		 */
//		public void sendReply(Reply reply){
//			try{
//				Socket client = reply.getServer().accept();
//				reply.writeObject(client.getOutputStream());
//				client.close();
//			} catch(Exception e){
//				e.printStackTrace();
//			}
//		}
	}
	
}
