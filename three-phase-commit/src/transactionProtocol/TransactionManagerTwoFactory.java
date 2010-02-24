package transactionProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import loader.ParticipantThreadPool;

import org.json.JSONException;

public class TransactionManagerTwoFactory {
	
	private static TransactionManagerTwoFactory factory;
	
	private HashMap<Class<? extends Participant<? extends Request>>, TransactionManagerTwo<? extends Participant<? extends Request>>> managers;
	
	private TransactionManagerTwoFactory(){
		managers = new HashMap<Class<? extends Participant<? extends Request>>, TransactionManagerTwo<? extends Participant<? extends Request>>>();
	}
	
	public static TransactionManagerTwoFactory getTransactionManagerTwoFactory(){
		return (factory == null) ? new TransactionManagerTwoFactory() : factory;
	}
	
	public <P extends Participant<? extends Request>> TransactionManagerTwo<P> getTransactionManagerTwo(Class<P> type, String config, int port){
		
		TransactionManagerTwo<P> manager = null;
		if( managers.get(type) == null){
			try {
				manager = new TransactionManagerTwoImp<P>(type, config, port);
				managers.put(type, manager);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return manager;
		 
	}
	
	
	/**
	 * The TransactionManagerTwo is responsible for initiating the RunnableParticipant threads
	 * as well as communicating between the outside world and the Distributed System.
	 * 
	 * The TransactionManagerTwo speaks to paticipants and the outside world through TCP, however the coordinator and the outside
	 * world speak to the TransactionManagerTwo directly (using sendMessage() and sendReply())
	 * @author cjlax26
	 *
	 * @param <P>
	 */
	private class TransactionManagerTwoImp<P extends Participant<? extends Request>> implements TransactionManagerTwo<P> {
		
		public static final String MANAGER = "MANAGER";
		
		// private P coordinator;
		private List<P> addressBook;
		private ParticipantThreadPool<P> launcher;
		private ServerSocket inbox;

		private TransactionManagerTwoImp(Class<P> type, String config, int port) throws IOException, JSONException {
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
