package threePhaseCommit;

import java.io.IOException;

import org.json.JSONException;

import applications.banking.BankingParticipant;

import transactionProtocol.*;
import transactionProtocol.Message.MessageType;

public class ThreePhaseCommitTransactionManager<R extends Request, P extends Participant<R>>
		extends TransactionManager<R, P> {

	public static int TIMEOUT = 1000;
	
	public ThreePhaseCommitTransactionManager(Class<P> type, String config,
			int port) throws IOException, JSONException {
		super(type, config, port);
	}

	@Override
	public Protocol getCommitProtocol() {		
		return new Protocol<R>() {
			
			private static final int INFINITE_TIMEOUT = 0;
			
			// Log types
			private static final String ABORT = "ABORT";
			private static final String COMMIT = "COMMIT";
			private static final String YES = "YES";
			private static final String NO = "NO";
			private static final String START = "START-3PC";
			
			// points to fail
			private static final String FAIL_BEFORE_INIT = "Fail-Before-Init";
			private static final String FAIL_BEFORE_VOTE_REQ = "Fail-Before-Vote-Req";
			private static final String FAIL_AFTER_VOTE_BEFORE_SEND = "Fail-After-Vote-Before-Send";
			private static final String FAIL_AFTER_VOTE_AFTER_SEND = "Fail-After-Vote-After-Send";
			private static final String FAIL_AFTER_ACK= "Fail-After-Ack";
			private static final String FAIL_AFTER_COMMIT = "Fail-After-Commit";
			private static final String FAIL_AFTER_ABORT = "Fail-After-Abort";
			
			Request r;
			
			public void start(Participant<R> p) {
				if (p.isCoordinator()) {
					coordinator(p);
				} else {
					participant(p);
				}
			}

			/**
			 * Coordinator's commit protocol
			 * @param p
			 */
			protected void coordinator(Participant<? extends Request> p) {
				
			}
			
			/**
			 * Participant's commit protocol
			 * @param p
			 */
			protected void participant(Participant<R> p) {
				Message<R> message;
				Vote vote;
				Message<R> send;
				ParticipantThread<R, P> thread = ((ParticipantThread<R, P>) Thread.currentThread());
				
				try{
					while(true){
						/**
						 * Wait for initialization
						 */
						try{
							if( thread.isInterrupted(FAIL_BEFORE_INIT)){
								throw new InterruptedException();
							}
							message = p.receiveMessage(INFINITE_TIMEOUT);
							if(message != null && (message.getType().equals(Message.MessageType.INITIATE))){
								p.getLog().log(START);
								/**
								 * Wait for Vote-Req
								 */
								if( thread.isInterrupted(FAIL_BEFORE_VOTE_REQ)){
									throw new InterruptedException();
								}
								message = p.receiveMessage(TIMEOUT);
								if( message != null && (message.getType().equals(Message.MessageType.VOTE_REQ))){
									//skip
								} else if(message != null && (message.getType().equals(Message.MessageType.ABORT))){
									p.getLog().log(ABORT);
									if(thread.isInterrupted(FAIL_AFTER_ABORT)){
										throw new InterruptedException();
									}
									// continue to next transaction
									continue;
								} else{
									// TODO: received message other than VOTE-REQ, or ABORT
									// Could possibly be a message from heartbeat monitor or election protocol message
								}
							} else if(message != null && (message.getType().equals(Message.MessageType.VOTE_REQ))){
								// skip
							} else if(message != null && (message.getType().equals(Message.MessageType.ABORT))){
								p.getLog().log(ABORT);
								if(thread.isInterrupted(FAIL_AFTER_ABORT)){
									throw new InterruptedException();
								}
								// continue to next transaction
								continue;
							} else{
								// TODO: received message other than INITIATE, VOTE-REQ, or ABORT
								// Could possibly be a message from heartbeat monitor or election protocol message
							}
							
							vote = p.castVote(message.getRequest());
							if (vote.equals(Vote.YES)){
								p.getLog().log(YES);
								if(thread.isInterrupted(FAIL_AFTER_VOTE_BEFORE_SEND)){
									throw new InterruptedException();
								}
								p.sendMessage(p.getCoordintar().getUid(), MessageType.YES, message.getRequest());
							} else if(vote.equals(Vote.NO)){
								p.getLog().log(NO);
								if(thread.isInterrupted(FAIL_AFTER_VOTE_BEFORE_SEND)){
									throw new InterruptedException();
								}
								p.sendMessage(p.getCoordintar().getUid(), MessageType.NO, message.getRequest());
								
							} else{
								p.getLog().log(ABORT);
								if(thread.isInterrupted(FAIL_AFTER_ABORT)){
									throw new InterruptedException();
								}
								// continue to next transaction
								continue;
							} 
							
							if(thread.isInterrupted(FAIL_AFTER_VOTE_AFTER_SEND)){
								throw new InterruptedException();
							}
							
							/**
							 * Wait for Pre-Commit from Coordinator
							 */
							
							message = p.receiveMessage(TIMEOUT);
							if(message != null && message.getType().equals(MessageType.PRE_COMMIT)){
								p.sendMessage(p.getCoordintar().getUid(), Message.MessageType.ACK, message.getRequest());
								if(thread.isInterrupted(FAIL_AFTER_ACK)){
									throw new InterruptedException();
								}
							} else if(message != null && message.getType().equals(MessageType.ABORT)){
								p.getLog().log(ABORT);
								if(thread.isInterrupted(FAIL_AFTER_ABORT)){
									throw new InterruptedException();
								}
								// continue to next transaction
								continue;
							} else{
								// TODO: received message other than PRE-COMMIT, or ABORT
								// Could possibly be a message from heartbeat monitor or election protocol message
							}
							
							/**
							 * Wait for Commit from Coordinator
							 */
							
							message = p.receiveMessage(TIMEOUT);
							if(message != null && message.getType().equals(Message.MessageType.COMMIT)){
								p.getLog().log(COMMIT);
								if(thread.isInterrupted(FAIL_AFTER_COMMIT)){
									throw new InterruptedException();
								}
							} else if(message != null && message.getType().equals(Message.MessageType.ABORT)){
								p.getLog().log(ABORT);
								if(thread.isInterrupted(FAIL_AFTER_ABORT)){
									throw new InterruptedException();
								}
								// continue to next transaction
								continue;
							} else {
								// TODO: received message other than COMMIT, or ABORT
								// Could possibly be a message from heartbeat monitor or election protocol message
							}
						} catch(MessageTimeoutException e){
							// assert(coordinator has died!)
							p.getLog().log(ABORT);
							// TODO: begin election protocol
							continue;
						} catch(ClassCastException e){
							e.printStackTrace();
							p.getLog().log(ABORT);
							if(thread.isInterrupted(FAIL_AFTER_ABORT)){
								throw new InterruptedException();
							}
							continue;
						}
					}
				} catch(InterruptedException e){
					// TODO close p's socket and kill p's heartbeat monitor
					// upon return the thread's run() method will finish in turn killing the thread
					return;
				}
			}
		};
	}

	@Override
	public Protocol getTerminationProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

}
