package threePhaseCommit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

import logger.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import transactionProtocol.Message;
import transactionProtocol.MessageTimeoutException;
import threePhaseCommit.ThreePhaseCommitTransactionManager.State;
import transactionProtocol.Message;
import transactionProtocol.MessageTimeoutException;
import transactionProtocol.Participant;
import transactionProtocol.ParticipantThread;
import transactionProtocol.Request;
import transactionProtocol.Vote;
import transactionProtocol.Message.MessageType;

public abstract class ThreePhaseCommitParticipant<R extends Request> extends Participant<R> {

	// Needed for termination protocol
	enum State {
		ABORTED, COMMITTED, COMMITTABLE, UNCERTAIN
	};

	// Timeouts
	public static int TIMEOUT = 1000;
	private static final int INFINITE_TIMEOUT = 0;

	// Log types
	private static final String ABORT = "ABORT";
	private static final String COMMIT = "COMMIT";
	private static final String YES = "YES";
	private static final String NO = "NO";
	private static final String START = "START-3PC";

	// Participant points to fail
	private static final String P_FAIL_BEFORE_INIT = "Participant-Fail-Before-Init";
	private static final String P_FAIL_BEFORE_VOTE_REQ = "Participant-Fail-Before-Vote-Req";
	private static final String P_FAIL_AFTER_VOTE_BEFORE_SEND = "Participant-Fail-After-Vote-Before-Send";
	private static final String P_FAIL_AFTER_VOTE_AFTER_SEND = "Participant-Fail-After-Vote-After-Send";
	private static final String P_FAIL_BEFORE_ACK = "Participant-Fail-Before-Ack";
	private static final String P_FAIL_AFTER_ACK = "Participant-Fail-After-Ack";
	private static final String P_FAIL_AFTER_COMMIT = "Participant-Fail-After-Commit";
	private static final String P_FAIL_AFTER_ABORT = "Participant-Fail-After-Abort";

	// Coordinator points to fail
	private static final String C_FAIL_BEFORE_INIT = "Coordinator-Fail-Before-Init";
	private static final String C_FAIL_AFTER_VOTE_REQ = "Coordinator-Fail-After-Vote-Req";
	private static final String C_FAIL_AFTER_PRE_COMMIT = "Coordinator-Fail-After-Pre-Commit";
	private static final String C_FAIL_AFTER_COMMIT_BEFORE_SEND = "Coordinator-Fail-After-Commit-Before-Send";
	private static final String C_FAIL_AFTER_COMMIT_AFTER_SEND = "Coordinator-Fail-After-Commit-After-Send";
	private static final String C_FAIL_AFTER_ABORT_BEFORE_SEND = "Coordinator-Fail-After-Abort-Before-Send";
	private static final String C_FAIL_AFTER_ABORT_AFTER_SEND = "Coordinator-Fail-After-Abort-Before-Send";

	private State state;
	
	public ThreePhaseCommitParticipant(String uid, int ranking,
			String defaultVote, InetSocketAddress address,
			Set<Participant<R>> participants, String logFile)
			throws IOException {
		super(uid, ranking, defaultVote, address, participants, logFile);
		this.state = State.ABORTED;
	}


	@Override
	public void startCommitProtocol() {
		if (isCoordinator()) {
			startCoordinatorCommitProtocol();
		} else {
			startParticipantCommitProtocol();
		}
	}

	@Override
	public void startTerminationProtocol() {
		if (isCoordinator()) {
			startCoordinatorTerminationProtocol();
		} else {
			startParticipantTerminationProtocol();
		}
	}

	public void startCoordinatorCommitProtocol() {

		Message<R> message = null;
		ParticipantThread<R, ThreePhaseCommitParticipant<R>> thread = ((ParticipantThread<R, ThreePhaseCommitParticipant<R>>) Thread.currentThread());
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
					try{
						message = receiveMessage(INFINITE_TIMEOUT);
					} catch(MessageTimeoutException e){
						// swallow
					}
					
					if(message == null){
						continue;
					}
					switch(message.getType()){
						case VOTE_REQ: continue;
						case YES: continue;
						case PRE_COMMIT: continue;
						case ACK: continue;
						case COMMIT: continue;
						case NO: continue;
						case ABORT: continue;
						case INITIATE: getLog().log(START);
										break;
						case FAIL: handleFailureMessage(message.getSource());
									continue;
						case ALIVE:	handleAliveMessage(message.getSource());
									continue;
					}
					
					// Initialized.  send vote req to all participants
					this.broadcastMessage(Message.MessageType.VOTE_REQ, message.getRequest());
					if(thread.isInterrupted(C_FAIL_AFTER_VOTE_REQ)){
						throw new InterruptedException();
					}
					
					/**
					 * Wait for VOTES from participants
					 */
					
					decision = true;
					yesVotes = new ArrayList<String>();
					upSize = getUpList().size();
					while(yesVotes.size() < upSize){
						try{
							message = receiveMessage(TIMEOUT);
						} catch(MessageTimeoutException e){
							decision = false;
							return;
						}
						switch(message.getType()){
							case VOTE_REQ:	continue;
							case YES: 	yesVotes.add(message.getSource());
										continue;
							case PRE_COMMIT:	continue;
							case ACK:	continue;
							case COMMIT:	continue;
							case NO: 	decision = false;
										return;
							case ABORT:	continue;
							case INITIATE:	continue;
							case FAIL:	handleFailureMessage(message.getSource());
										continue;
							case ALIVE:	handleAliveMessage(message.getSource());
										continue;
						}
					}
					
					if(decision){
						/** 
						 * Send Pre-Commit
						 */
						
						broadcastMessage(Message.MessageType.PRE_COMMIT, message.getRequest());
						if(thread.isInterrupted(C_FAIL_AFTER_PRE_COMMIT)){
							throw new InterruptedException();
						}
						
						yesVotes = new ArrayList<String>();
						upSize = this.getUpList().size();
						while(yesVotes.size() < upSize){
							try{
								message = receiveMessage(TIMEOUT);
							} catch(MessageTimeoutException e){
								coordinatorAbort(message);
								return;
							}
							switch(message.getType()){
							case VOTE_REQ:	continue;
							case YES:	continue;
							case PRE_COMMIT:	continue;
							case ACK:	yesVotes.add(message.getSource());
							case COMMIT:	continue;
							case NO: 	continue;
							case ABORT:	continue;
							case INITIATE:	continue;
							case FAIL:	handleFailureMessage(message.getSource());
										continue;
							case ALIVE:	handleAliveMessage(message.getSource());
										continue;
							}
						}
					} else{
						// Decision is Abort
						coordinatorAbort(message);
					}
				} catch(ClassCastException e){
					e.printStackTrace();
					getLog().log(ABORT);
					if(thread.isInterrupted(C_FAIL_AFTER_ABORT_BEFORE_SEND)){
						throw new InterruptedException();
					}
					continue;
				}
			}
		} catch(InterruptedException e){
			this.broadcastMessage(Message.MessageType.FAIL, message.getRequest());
			return;
		}
	}

	@SuppressWarnings("unchecked")
	public void startParticipantCommitProtocol() {
		Message<R> message;
		Logger log = getLog();

		ParticipantThread<R, ThreePhaseCommitParticipant<R>> thread 
			= ((ParticipantThread<R, ThreePhaseCommitParticipant<R>>) Thread.currentThread());

		try {
			// this while loop can be interrupted 
			while (true) {

				if (thread.isInterrupted(P_FAIL_BEFORE_INIT)) {
					throw new InterruptedException();
				}

				// we sit and wait for an intiate protocol message...forever.
				try {
					System.out.println(getUid() + " is waiting for an intialize message");
					message = receiveMessage(INFINITE_TIMEOUT);
					System.out.println(getUid() + " received an intialize message");
				} catch (MessageTimeoutException e) { continue; }

				switch(message.getType()) {
				
				// on an intiate we wait for vote-request
				case INITIATE:
					// wait for vote-request
					if (thread.isInterrupted(P_FAIL_BEFORE_VOTE_REQ)) {
						throw new InterruptedException();
					}
					
					try {
						// spin until we receive a vote-req
						while (true) {
							message = receiveMessage(TIMEOUT);
							switch(message.getType()) {
							case ALIVE: 
								handleResurrectedProcess(message.getSource());
								continue;
							case FAIL: 
								handleFailedProcess(message.getSource());
								continue;
							case VOTE_REQ: 
								break;
							default: 
								continue;
							}							
							break;
						}
					} catch (MessageTimeoutException e) {
						log.log(ABORT);
						state = State.ABORTED;
						continue;
					}					
				
					break;
					
				// if we receive a vote-req before an intiate, we break
				// into the voting process
				case VOTE_REQ: 
					break;
				// handle failed processes
				case FAIL: 
					handleFailedProcess(message.getSource());
					continue;
				// handle newly resurrected processes
				case ALIVE:
					handleResurrectedProcess(message.getSource());
					continue;
				// everything else is ignored
				default: 
					continue;
				}

				// cast our votes!
				if (castVote(message.getRequest()).equals(Vote.YES)) {
					log.log(YES);
					state = State.UNCERTAIN;

					if (thread.isInterrupted(P_FAIL_AFTER_VOTE_BEFORE_SEND)) {
						throw new InterruptedException();
					}

					// send yes to coordinator
					sendMessage(getCurrentCoordinator().getUid(),
							MessageType.YES, message.getRequest());

					if (thread.isInterrupted(P_FAIL_AFTER_VOTE_AFTER_SEND)) {
						throw new InterruptedException();
					}

					// wait for Pre-Commit from Coordinator
					try {
						// spin until we receive a vote-req
						while (true) {
							message = receiveMessage(TIMEOUT);
							switch(message.getType()) {
							case ALIVE: 
								handleResurrectedProcess(message.getSource());
								continue;
							case FAIL: 
								handleFailedProcess(message.getSource());
								continue;
							case PRE_COMMIT: 
								state = State.COMMITTABLE;

								if (thread.isInterrupted(P_FAIL_BEFORE_ACK)) {
									throw new InterruptedException();
								}

								// send ack to coordinator
								sendMessage(getCurrentCoordinator().getUid(),
										Message.MessageType.ACK, message.getRequest());

								if (thread.isInterrupted(P_FAIL_AFTER_ACK)) {
									throw new InterruptedException();
								}

								// wait for Commit from Coordinator
								try {
									// spin until we receive a commit 
									while (true) {
										message = receiveMessage(TIMEOUT);
										switch(message.getType()) {
										case ALIVE: 
											handleResurrectedProcess(message.getSource());
											continue;
										case FAIL: 
											handleFailedProcess(message.getSource());
											continue;
										case COMMIT: 
											log.log(COMMIT);
											state = State.COMMITTED;

											if (thread.isInterrupted(P_FAIL_AFTER_COMMIT)) {
												throw new InterruptedException();
											}
											
											break;
										default: 
											continue;
										}
										
										break;
									}
								} catch (MessageTimeoutException e) {
									// initiate election protocol
									// if elected then invoke coordinators algorithm
									// else invoke participants algorithm
									// return
								}
								
								break;
							case ABORT:
								log.log(ABORT);
								state = State.ABORTED;
								continue;
							default:
								continue;
							}
							
							break;
						}
					} catch (MessageTimeoutException e) {
						// initiate election protocol
						// if elected then invoke coordinators algorithm
						// else invoke participants algorithm
						// return
					} 	
				} 

				// vote is no :(
				else {						
					if (thread.isInterrupted(P_FAIL_AFTER_VOTE_BEFORE_SEND)) {
						throw new InterruptedException();
					}

					// send no to coordinator
					sendMessage(getCurrentCoordinator().getUid(),
							MessageType.NO, message.getRequest());
					
					if (thread.isInterrupted(P_FAIL_AFTER_VOTE_AFTER_SEND)) {
						throw new InterruptedException();
					}

					log.log(ABORT);
					state = State.ABORTED;

					if (thread.isInterrupted(P_FAIL_AFTER_ABORT)) {
						throw new InterruptedException();
					}

					// continue to next transaction
					continue;
				}
			}
		} 
		
		catch (InterruptedException e) {
			// TODO close p's socket and kill p's heartbeat monitor
			// upon return the thread's run() method will finish in turn killing
			// the thread
			return;
		}
	}

	public void startCoordinatorTerminationProtocol() {

	}

	public void startParticipantTerminationProtocol() {

	}
	
	private void handleResurrectedProcess(String uid) {
		
	}
	
	private void handleFailedProcess(String uid) {
		
	}
	
	private void handleFailureMessage(String uid){
		
	}
	
	private void handleAbortMessage(String uid){
		
	}
	
	private void handleAliveMessage(String uid){

	}
	
	private void coordinatorAbort(Message<R> message){
		this.sendMessage(this.getUid(), Message.MessageType.ABORT, message.getRequest());
	}

}
