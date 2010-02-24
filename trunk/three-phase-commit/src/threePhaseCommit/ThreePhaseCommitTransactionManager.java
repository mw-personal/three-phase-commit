package threePhaseCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import transactionProtocol.*;
import transactionProtocol.Message.MessageType;

public class ThreePhaseCommitTransactionManager<R extends Request, P extends Participant<R>>
		extends TransactionManager<R, P> {

	public static int TIMEOUT = 1000;
	
	public ThreePhaseCommitTransactionManager(Class<P> type, String config,
			int port) throws IOException, JSONException {
		super(type, config, port);
	}

	// Needed for termination protocol
	enum State{
		ABORTED,
		COMMITTED,
		COMMITTABLE,
		UNCERTAIN
	};
	
	@Override
	public Protocol<R> getCommitProtocol() {		
		return new Protocol<R>() {
			
			private static final int INFINITE_TIMEOUT = 0;
			
			// Log types
			private static final String ABORT = "ABORT";
			private static final String COMMIT = "COMMIT";
			private static final String YES = "YES";
			private static final String NO = "NO";
			private static final String START = "START-3PC";
			
			// participant points to fail
			private static final String P_FAIL_BEFORE_INIT = "Participant-Fail-Before-Init";
			private static final String P_FAIL_BEFORE_VOTE_REQ = "Participant-Fail-Before-Vote-Req";
			private static final String P_FAIL_AFTER_VOTE_BEFORE_SEND = "Participant-Fail-After-Vote-Before-Send";
			private static final String P_FAIL_AFTER_VOTE_AFTER_SEND = "Participant-Fail-After-Vote-After-Send";
			private static final String P_FAIL_AFTER_ACK= "Participant-Fail-After-Ack";
			private static final String P_FAIL_AFTER_COMMIT = "Participant-Fail-After-Commit";
			private static final String P_FAIL_AFTER_ABORT = "Participant-Fail-After-Abort";
			
			// coordinator points to fail
			private static final String C_FAIL_BEFORE_INIT = "Coordinator-Fail-Before-Init";
			private static final String C_FAIL_AFTER_VOTE_REQ = "Coordinator-Fail-After-Vote-Req";
			private static final String C_FAIL_AFTER_PRE_COMMIT = "Coordinator-Fail-After-Pre-Commit";
			private static final String C_FAIL_AFTER_COMMIT_BEFORE_SEND = "Coordinator-Fail-After-Commit-Before-Send";
			private static final String C_FAIL_AFTER_COMMIT_AFTER_SEND = "Coordinator-Fail-After-Commit-After-Send";
			private static final String C_FAIL_AFTER_ABORT_BEFORE_SEND = "Coordinator-Fail-After-Abort-Before-Send";
			private static final String C_FAIL_AFTER_ABORT_AFTER_SEND = "Coordinator-Fail-After-Abort-Before-Send";
			
			
			private State state = State.ABORTED;
			
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
			protected void coordinator(Participant<R> p) {
				Message<R> message = null;
				ParticipantThread<R, P> thread = ((ParticipantThread<R, P>) Thread.currentThread());
				List<String> yesVotes;
				boolean decision;
				long upSize;
				
				try{
					while(true){
						/**
						 * Wait for initialization
						 */
						try{
							if( thread.isInterrupted(C_FAIL_BEFORE_INIT)){
								throw new InterruptedException();
							}
							message = p.receiveMessage(INFINITE_TIMEOUT);
							if(message != null && (message.getType().equals(Message.MessageType.INITIATE))){
								p.getLog().log(START);
								
							} else{
								// TODO: received message other than INITIATE
								// Could possibly be a message from heartbeat monitor
							}
							
							p.broadcastMessage(Message.MessageType.VOTE_REQ, message.getRequest());
							
							if(thread.isInterrupted(C_FAIL_AFTER_VOTE_REQ)){
								throw new InterruptedException();
							}
							
							/**
							 * Wait for VOTES from participants
							 */
							
							decision = true;
							yesVotes = new ArrayList<String>();
							upSize = p.getUpList().size();
							while(yesVotes.size() < upSize){
								message = p.receiveMessage(TIMEOUT);
								if( message != null && message.getType().equals(Message.MessageType.YES)){
									yesVotes.add(message.getSource());
								} else if( message != null && message.getType().equals(Message.MessageType.NO)){
									decision = false;
									return;
								} else if( message != null && message.getType().equals(Message.MessageType.ABORT)){
									decision = false;
									return;
								} else{
									//  TODO: received message other than VOTE or ABORT
									//  Could possibly be a message from heartbeat monitor
								}
							}
							
							if(decision){
								/** 
								 * Send Pre-Commit
								 */
								p.broadcastMessage(Message.MessageType.PRE_COMMIT, message.getRequest());
								yesVotes = new ArrayList<String>();
								while(yesVotes.size() < p.getUpList().size()){
									message = p.receiveMessage(TIMEOUT);
									if( message != null && message.getType().equals(Message.MessageType.ACK)){
										// TODO:  Finish pre-commit and commit section
									}
								}
							}
						
						} catch(MessageTimeoutException e){
							p.getLog().log(ABORT);
							p.broadcastMessage(Message.MessageType.ABORT, message.getRequest());
							continue;
						} catch(ClassCastException e){
							e.printStackTrace();
							p.getLog().log(ABORT);
							if(thread.isInterrupted(C_FAIL_AFTER_ABORT_BEFORE_SEND)){
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
			
			/**
			 * Participant's commit protocol
			 * @param p
			 */
			protected void participant(Participant<R> p) {
				Message<R> message;
				Vote vote;
				ParticipantThread<R, P> thread = ((ParticipantThread<R, P>) Thread.currentThread());
				
				try{
					while(true){
						/**
						 * Wait for initialization
						 */
						try{
							if( thread.isInterrupted(P_FAIL_BEFORE_INIT)){
								throw new InterruptedException();
							}
							message = p.receiveMessage(INFINITE_TIMEOUT);
							if(message != null && (message.getType().equals(Message.MessageType.INITIATE))){
								p.getLog().log(START);
								/**
								 * Wait for Vote-Req
								 */
								if( thread.isInterrupted(P_FAIL_BEFORE_VOTE_REQ)){
									throw new InterruptedException();
								}
								message = p.receiveMessage(TIMEOUT);
								if( message != null && (message.getType().equals(Message.MessageType.VOTE_REQ))){
									//skip
								} else if(message != null && (message.getType().equals(Message.MessageType.ABORT))){
									state = State.ABORTED;
									p.getLog().log(ABORT);
									if(thread.isInterrupted(P_FAIL_AFTER_ABORT)){
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
								state = State.ABORTED;
								p.getLog().log(ABORT);
								if(thread.isInterrupted(P_FAIL_AFTER_ABORT)){
									throw new InterruptedException();
								}
								// continue to next transaction
								continue;
							} else{
								// TODO: received message other than INITIATE, VOTE-REQ, or ABORT
								// Could possibly be a message from heartbeat monitor or election protocol message
							}
							
							/**
							 * Choose vote
							 */
							vote = p.castVote(message.getRequest());
							if (vote.equals(Vote.YES)){
								p.getLog().log(YES);
								state = State.UNCERTAIN;
								if(thread.isInterrupted(P_FAIL_AFTER_VOTE_BEFORE_SEND)){
									throw new InterruptedException();
								}
								p.sendMessage(p.getCoordintar().getUid(), MessageType.YES, message.getRequest());
							} else if(vote.equals(Vote.NO)){
								p.getLog().log(NO);
								if(thread.isInterrupted(P_FAIL_AFTER_VOTE_BEFORE_SEND)){
									throw new InterruptedException();
								}
								p.sendMessage(p.getCoordintar().getUid(), MessageType.NO, message.getRequest());
								state = State.ABORTED;
								p.getLog().log(ABORT);
								if(thread.isInterrupted(P_FAIL_AFTER_ABORT)){
									throw new InterruptedException();
								}
								// continue to next transaction
								continue;
							} else{
								state = State.ABORTED;
								p.getLog().log(ABORT);
								if(thread.isInterrupted(P_FAIL_AFTER_ABORT)){
									throw new InterruptedException();
								}
								// continue to next transaction
								continue;
							} 
							
							if(thread.isInterrupted(P_FAIL_AFTER_VOTE_AFTER_SEND)){
								throw new InterruptedException();
							}
							
							/**
							 * Wait for Pre-Commit from Coordinator
							 */
							
							message = p.receiveMessage(TIMEOUT);
							if(message != null && message.getType().equals(MessageType.PRE_COMMIT)){
								state = State.COMMITTABLE;
								p.sendMessage(p.getCoordintar().getUid(), Message.MessageType.ACK, message.getRequest());
								if(thread.isInterrupted(P_FAIL_AFTER_ACK)){
									throw new InterruptedException();
								}
							} else if(message != null && message.getType().equals(MessageType.ABORT)){
								state = state.ABORTED;
								p.getLog().log(ABORT);
								if(thread.isInterrupted(P_FAIL_AFTER_ABORT)){
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
								state = State.COMMITTED;
								p.getLog().log(COMMIT);
								if(thread.isInterrupted(P_FAIL_AFTER_COMMIT)){
									throw new InterruptedException();
								}
							} else if(message != null && message.getType().equals(Message.MessageType.ABORT)){
								state = State.ABORTED;
								p.getLog().log(ABORT);
								if(thread.isInterrupted(P_FAIL_AFTER_ABORT)){
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
							if(thread.isInterrupted(P_FAIL_AFTER_ABORT)){
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
