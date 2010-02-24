package transactionProtocol;


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
public interface TransactionManagerTwo<P extends Participant<? extends Request>> {
	

	public void initParticipants();
	
	/**
	 * API for outside world to send a request to the DS.
	 * @param request
	 */
	public void sendRequest(Request request);

}