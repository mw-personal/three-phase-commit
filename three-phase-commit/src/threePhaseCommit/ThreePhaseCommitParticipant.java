package threePhaseCommit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import logger.Logger;
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
	private static final String P_FAIL_AFTER_VOTE_REQ = "Participant-Fail-After-Vote-Req";
	private static final String P_FAIL_AFTER_VOTE_BEFORE_SEND = "Participant-Fail-After-Vote-Before-Send";
	private static final String P_FAIL_AFTER_VOTE_AFTER_SEND = "Participant-Fail-After-Vote-After-Send";
	private static final String P_FAIL_BEFORE_ACK = "Participant-Fail-Before-Ack";
	private static final String P_FAIL_AFTER_ACK = "Participant-Fail-After-Ack";
	private static final String P_FAIL_AFTER_COMMIT = "Participant-Fail-After-Commit";
	private static final String P_FAIL_AFTER_ABORT = "Participant-Fail-After-Abort";

	// Coordinator points to fail
	private static final String C_FAIL_BEFORE_INIT = "Coordinator-Fail-Before-Init";
	private static final String C_FAIL_BEFORE_VOTE_REQ = "Coordinator-Fail-Before-Vote-Req";
	private static final String C_FAIL_AFTER_VOTE_REQ = "Coordinator-Fail-After-Vote-Req";
	private static final String C_FAIL_BEFORE_PRE_COMMIT = "Coordinator-Fail-Before-Pre-Commit";
	private static final String C_FAIL_AFTER_PRE_COMMIT = "Coordinator-Fail-After-Pre-Commit";
	private static final String C_FAIL_AFTER_COMMIT_BEFORE_SEND = "Coordinator-Fail-After-Commit-Before-Send";
	private static final String C_FAIL_AFTER_COMMIT_AFTER_SEND = "Coordinator-Fail-After-Commit-After-Send";
	private static final String C_FAIL_AFTER_ABORT_BEFORE_SEND = "Coordinator-Fail-After-Abort-Before-Send";
	private static final String C_FAIL_AFTER_ABORT_AFTER_SEND = "Coordinator-Fail-After-Abort-Before-Send";

	// Participant termination protocol points to fail
	private static final String T_P_FAIL_BEFORE_STATE_REQ = "T-Participant-Fail-Before-State-Req";
	private static final String T_P_FAIL_AFTER_SEND_STATE = "T-Participant-Fail-After-Send-State";
	private static final String T_P_FAIL_AFTER_ACK = "T-Participant-Fail-After-Ack";
	
	// Coordinator termination protocol points to fail
	private static final String T_C_FAIL_BEFORE_STATE_REQ = "T-Coordinator-Fail-Before-State-Request";
	private static final String T_C_FAIL_AFTER_STATE_REQ = "T-Coordinator-Fail-After-State-Request";
	private static final String T_C_FAIL_AFTER_PRE_COMMIT = "T-Coordinator-Fail-After-Pre-Commit";
	
	
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
	protected void startTerminationProtocol(R request) {
		if (isCoordinator()) {
			startCoordinatorTerminationProtocol(request);
		} else {
			startParticipantTerminationProtocol(request);
		}
	}

	@SuppressWarnings("unchecked")
	private void startCoordinatorCommitProtocol() {
		Message<R> message = null;
		ParticipantThread<R, ThreePhaseCommitParticipant<R>> thread = 
			((ParticipantThread<R, ThreePhaseCommitParticipant<R>>) Thread.currentThread());
		Set<Participant<R>> yesVotes;
		boolean decision;
		Logger log = this.getLog();
		R request = null;

		try {
			intial_state: while (true) {
				
				log.log("WAITING FOR INTIALIZE");

				// wait for initialization
				if (thread.isInterrupted(C_FAIL_BEFORE_INIT)) {
					throw new InterruptedException();
				}

				try {
					message = this.receiveMessage(INFINITE_TIMEOUT);
				} catch (MessageTimeoutException e) { }

				MessageType mtype = message.getType();
				if (mtype == MessageType.INITIATE) {
					log.log(START);
					request = message.getRequest();
				} else if (mtype == MessageType.FAIL) {
					this.handleFailedProcess(message.getSource());
					continue intial_state;
				} else if (mtype == MessageType.ALIVE) {
					this.handleResurrectedProcess(message.getSource());
					continue intial_state;
				} else {
					continue intial_state;
				}

				if (thread.isInterrupted(C_FAIL_BEFORE_VOTE_REQ)) {
					throw new InterruptedException();
				}

				// send out vote-req
				this.broadcastMessage(Message.MessageType.VOTE_REQ, request);

				if (thread.isInterrupted(C_FAIL_AFTER_VOTE_REQ)) {
					throw new InterruptedException();
				}

				decision = true;
				yesVotes = new HashSet<Participant<R>>();

				int upSize = this.getUpList().size();
				int votesReceived = upSize - 1;

				while (votesReceived != 0) {
					try {
						message = this.receiveMessage(TIMEOUT);
					} catch (MessageTimeoutException e) {
						decision = false;
						votesReceived--;
					}

					mtype = message.getType();
					if (mtype == MessageType.YES) {
						yesVotes.add(findParticipant(message.getSource()));
						votesReceived--;
					} else if (mtype == MessageType.NO) {
						decision = false;
						votesReceived--;
					} else if (mtype == MessageType.FAIL) {
						this.handleFailedProcess(message.getSource());
					} else if (mtype == MessageType.ALIVE) {
						this.handleResurrectedProcess(message.getSource());
					}
				}

				if (decision && this.castVote(request) == Vote.YES) {
					// send precommit

					if (thread.isInterrupted(C_FAIL_BEFORE_PRE_COMMIT)) {
						throw new InterruptedException();
					}

					this.broadcastMessage(Message.MessageType.PRE_COMMIT, request);

					if (thread.isInterrupted(C_FAIL_AFTER_PRE_COMMIT)) {
						throw new InterruptedException();
					}

					yesVotes = new HashSet<Participant<R>>();
					upSize = this.getUpList().size();
					votesReceived = upSize - 1; // here votesReceived acts as
												// "acksReceived"

					while (votesReceived != 0) {
						try {
							message = receiveMessage(TIMEOUT);
						} catch (MessageTimeoutException e) {
						}

						mtype = message.getType();
						if (mtype == MessageType.ACK) {
							yesVotes.add(findParticipant(message.getSource()));
							votesReceived--;
						} else if (mtype == MessageType.FAIL) {
							handleFailedProcess(message.getSource());
							votesReceived--; // we don't care about failed
												// participants
						} else if (mtype == MessageType.ALIVE) {
							handleResurrectedProcess(message.getSource());
						}
					}

					// commit
					log.log(COMMIT);
					this.commit(request);

					// notify the tm
					this.sendMessage(this.getManagerAddress().getHostName(),
							this.getManagerAddress(),
							Message.MessageType.COMMIT, request);

					if (thread.isInterrupted(C_FAIL_AFTER_COMMIT_BEFORE_SEND)) {
						throw new InterruptedException();
					}

					this.broadcastMessage(yesVotes, Message.MessageType.COMMIT, request);

					if (thread.isInterrupted(C_FAIL_AFTER_COMMIT_AFTER_SEND)) {
						throw new InterruptedException();
					}
				}

				else {
					// decision is Abort
					if (thread.isInterrupted(C_FAIL_AFTER_ABORT_BEFORE_SEND)) {
						throw new InterruptedException();
					}

					this.coordinatorAbort(yesVotes, request);

					if (thread.isInterrupted(C_FAIL_AFTER_ABORT_AFTER_SEND)) {
						throw new InterruptedException();
					}
				}
			}
		} catch (InterruptedException e) {
			System.out.println("INTERRUPTED THIS THREAD!");
			this.broadcastMessage(Message.MessageType.FAIL, null);
			return;
		}
	}

	@SuppressWarnings("unchecked")
	public void startParticipantCommitProtocol() {
		Message<R> message = null;
		Logger log = getLog();
		R request = null;

		ParticipantThread<R, ThreePhaseCommitParticipant<R>> thread = 
			((ParticipantThread<R, ThreePhaseCommitParticipant<R>>) Thread.currentThread());

		try {

			initial_state: while (true) {
				
				log.log("WAITING FOR INTIALIZE");

				if (thread.isInterrupted(P_FAIL_BEFORE_INIT)) {
					throw new InterruptedException();
				}

				// we sit and wait for an intiate protocol message...forever.
				try {
					message = this.receiveMessage(INFINITE_TIMEOUT);
				} catch (MessageTimeoutException e) {
					continue initial_state;
				}

				MessageType mtype = message.getType();
				if (mtype == MessageType.INITIATE) {
					request = message.getRequest();
					// wait for vote-request
					if (thread.isInterrupted(P_FAIL_BEFORE_VOTE_REQ)) { 
						throw new InterruptedException();
					}

					try {
						// spin until we receive a vote-req
						while (true) {
							message = this.receiveMessage(TIMEOUT);

							mtype = message.getType();
							if (mtype == MessageType.ALIVE) {
								this.handleResurrectedProcess(message.getSource());
							} else if (mtype == MessageType.FAIL) {
								this.handleFailedProcess(message.getSource());
							} else if (mtype == MessageType.VOTE_REQ) {
								break;
							} else if (mtype == MessageType.UR_ELECTED) {
								// TODO: omg what to do here.
								this.removeCoordinatorFromUpList();
								this.setCurrentCoordinator(this);
								this.startCoordinatorTerminationProtocol(request);
								continue initial_state;
							}
						}
					} catch (MessageTimeoutException e) {
						log.log(ABORT);
						this.state = State.ABORTED;
						this.abort(message.getRequest());

						// return to initialstate
						continue initial_state;
					}
				} else if (mtype == MessageType.VOTE_REQ) {
					// nothing to do here!
					request = message.getRequest();
				} else if (mtype == MessageType.FAIL) {
					this.handleFailedProcess(message.getSource());
					log.log(message.toString());
					continue initial_state;
				} else if (mtype == MessageType.ALIVE) {
					this.handleResurrectedProcess(message.getSource());
					log.log(message.toString());
					continue initial_state;
				} else if (mtype == MessageType.UR_ELECTED) {
					// TODO: omg what to do here?!
					this.removeCoordinatorFromUpList();
					this.setCurrentCoordinator(this);
					this.startCoordinatorTerminationProtocol(request);
					continue initial_state;
				} else {
					log.log(message.toString());
					continue initial_state;
				}

				// cast our votes!
				if (this.castVote(request).equals(Vote.YES)) {

					if (thread.isInterrupted(P_FAIL_AFTER_VOTE_REQ)) {
						throw new InterruptedException();
					}
					
					log.log(YES);
					this.state = State.UNCERTAIN;

					if (thread.isInterrupted(P_FAIL_AFTER_VOTE_BEFORE_SEND)) {
						throw new InterruptedException();
					}
					
					// send yes to coordinator
					this.sendMessage(getCurrentCoordinator().getUid(),
							MessageType.YES, request);

					if (thread.isInterrupted(P_FAIL_AFTER_VOTE_AFTER_SEND)) {
						throw new InterruptedException();
					}

					// wait for Pre-Commit from Coordinator
					try {
						// spin until we receive a vote-req
						while (true) {
							message = receiveMessage(TIMEOUT);

							mtype = message.getType();
							if (mtype == MessageType.ALIVE) {
								this.handleResurrectedProcess(message
										.getSource());
							} else if (mtype == MessageType.FAIL) {
								this.handleFailedProcess(message.getSource());
							} else if (mtype == MessageType.ABORT) {
								log.log(ABORT);
								this.state = State.ABORTED;
								this.abort(request);
								continue initial_state;
							} else if (mtype == MessageType.UR_ELECTED) {
								// TODO: omg what to do here?!
								this.removeCoordinatorFromUpList();
								this.setCurrentCoordinator(this);
								this.startCoordinatorTerminationProtocol(request);
								continue initial_state;
							} else if (mtype == MessageType.PRE_COMMIT) {
								this.state = State.COMMITTABLE;

								if (thread.isInterrupted(P_FAIL_BEFORE_ACK)) {
									throw new InterruptedException();
								}

								// send ack to coordinator
								this.sendMessage(getCurrentCoordinator()
										.getUid(), Message.MessageType.ACK,
										request);

								if (thread.isInterrupted(P_FAIL_AFTER_ACK)) {
									throw new InterruptedException();
								}

								// wait for Commit from Coordinator
								try {
									// spin until we receive a commit
									while (true) {
										message = receiveMessage(TIMEOUT);

										mtype = message.getType();
										if (mtype == MessageType.ALIVE) {
											this.handleResurrectedProcess(message.getSource());
										} else if (mtype == MessageType.FAIL) {
											this.handleFailedProcess(message
													.getSource());
										} else if (mtype == MessageType.COMMIT) {
											log.log(COMMIT);
											this.state = State.COMMITTED;
											this.commit(request);

											if (thread.isInterrupted(P_FAIL_AFTER_COMMIT)) {
												throw new InterruptedException();
											}

											// commited, now back to initialize!
											continue initial_state;
										} else if (mtype == MessageType.UR_ELECTED) {
											// TODO: omg what to do here?!
											this.removeCoordinatorFromUpList();
											this.setCurrentCoordinator(this);
											this.startCoordinatorTerminationProtocol(request);
											continue initial_state;
										}
									}
								} catch (MessageTimeoutException e) {
									this.removeCoordinatorFromUpList();
									this.startElectionProtocol(request);
									if(this.getCurrentCoordinator().getUid().equals(this.getUid())){
										this.startCoordinatorTerminationProtocol(request);
									} else{
										this.startParticipantTerminationProtocol(request);
									}
									continue initial_state;
								}
							}
						}
					} catch (MessageTimeoutException e) {
						this.removeCoordinatorFromUpList();
						this.startElectionProtocol(request);
						if(this.getCurrentCoordinator().getUid().equals(this.getUid())){
							this.startCoordinatorTerminationProtocol(request);
						} else{
							this.startParticipantTerminationProtocol(request);
						}
						continue initial_state;
					}
				}

				// vote is no :(
				else {
					if (thread.isInterrupted(P_FAIL_AFTER_VOTE_BEFORE_SEND)) {
						throw new InterruptedException();
					}

					// send no to coordinator
					this.sendMessage(getCurrentCoordinator().getUid(),
							MessageType.NO, request);

					if (thread.isInterrupted(P_FAIL_AFTER_VOTE_AFTER_SEND)) {
						throw new InterruptedException();
					}

					log.log(ABORT);
					this.state = State.ABORTED;
					this.abort(request);

					if (thread.isInterrupted(P_FAIL_AFTER_ABORT)) {
						throw new InterruptedException();
					}
				}
			}
		}

		catch (InterruptedException e) {
			// TODO close p's socket and kill p's heartbeat monitor
			// upon return the thread's run() method will finish in turn killing
			// the thread
			System.out.println("INTERRUPTED THIS THREAD!");
			this.broadcastMessage(Message.MessageType.FAIL, null);
			return;
		}
	}

	private void startCoordinatorTerminationProtocol(R request) {
		Message<R> message = null;
		final Logger log = this.getLog();

		ParticipantThread<R, ThreePhaseCommitParticipant<R>> thread = 
			((ParticipantThread<R, ThreePhaseCommitParticipant<R>>) Thread.currentThread());
		
		try {
			
			if (thread.isInterrupted(T_C_FAIL_BEFORE_STATE_REQ)) {
				throw new InterruptedException();
			}
			
			// request state from all processes
			this.broadcastMessage(MessageType.STATE_REQ, request);
			
			if(thread.isInterrupted(T_C_FAIL_AFTER_STATE_REQ)){
				throw new InterruptedException();
			}
			
			int upSize = this.getUpList().size();
			int stateReportsReceived = upSize - 1;
			
			boolean isAborted = false;
			boolean isCommitted = false;
			boolean isCommittable = false;

			Set<Participant<R>> uncertainParticipants = 
				new HashSet<Participant<R>>();
			
			MessageType mtype = null;
			while (stateReportsReceived != 0) {
				try {
					message = this.receiveMessage(TIMEOUT);
				} catch (MessageTimeoutException e) { 
					stateReportsReceived--;
				}

				mtype = message.getType();
				if (mtype == MessageType.ABORTED) {
					isAborted = true;
					stateReportsReceived--;
				} else if (mtype == MessageType.COMMITTED) {
					isCommitted = true;
					stateReportsReceived--;
				} else if (mtype == MessageType.UNCERTAIN) {
					uncertainParticipants.add(
							findParticipant(message.getSource()));
					stateReportsReceived--;
				} else if (mtype == MessageType.COMMITTABLE) {
					isCommittable = true;
					stateReportsReceived --;
				}
				
				// TODO; handle other kinds of messages here
			}
						
			// TR1
			if (this.state == State.ABORTED || isAborted) {
				// TODO: may write ABORT twice
				log.log(ABORT);

				this.broadcastMessage(MessageType.ABORT, request);
			}

			// TR2
			else if (this.state == State.COMMITTED || isCommitted) {
				// TODO: may write COMMIT twice
				log.log(COMMIT);

				this.broadcastMessage(MessageType.COMMIT, request);					
			}

			// TR3
			else if (this.state == State.UNCERTAIN && !isCommittable) {
				log.log(ABORT);
				this.state = State.ABORTED;

				this.broadcastMessage(MessageType.ABORT, request);										
			}

			// TR4
			else {
				// send precommits to all uncertain
				this.broadcastMessage(uncertainParticipants, MessageType.PRE_COMMIT, request);
				
				if(thread.isInterrupted(T_C_FAIL_AFTER_PRE_COMMIT)){
					throw new InterruptedException();
				}
				
				// wait for acks
				upSize = this.getUpList().size();
				int acksReceived = upSize - 1;

				while (acksReceived != 0) {
					try {
						message = receiveMessage(TIMEOUT);
					} catch (MessageTimeoutException e) {
						acksReceived--;
					}

					mtype = message.getType();
					if (mtype == MessageType.ACK) {
						acksReceived--;
					}
				}
				
				log.log(COMMIT);
				this.state = State.COMMITTED;
				
				this.broadcastMessage(MessageType.COMMIT, request);				
			}
			
			// notify the TM of our action
			this.sendMessage(this.getManagerAddress().getHostName(),
					this.getManagerAddress(),
					(this.state == State.COMMITTED) ? 
							MessageType.COMMIT : MessageType.ABORT, 
					request);
		}
		
		catch (Exception e) {

		}
	}

	private void startParticipantTerminationProtocol(R request) {
		Message<R> message = null;
		MessageType mType = null;
		MessageType stateType = null;
		boolean processed = false;
		
		ParticipantThread<R, ThreePhaseCommitParticipant<R>> thread = 
			((ParticipantThread<R, ThreePhaseCommitParticipant<R>>) Thread.currentThread());
		
		/**
		 * Wait for STATE-REQ
		 */
		try{
			if (thread.isInterrupted(T_P_FAIL_BEFORE_STATE_REQ)) {
				throw new InterruptedException();
			}
			// Spin until receiving State_Request from coordinator
			while(!processed){
				try{
					message = this.receiveMessage(TIMEOUT);
				} catch(MessageTimeoutException e){
					this.removeCoordinatorFromUpList();
					this.startElectionProtocol(request);
					if(this.getCurrentCoordinator().getUid().equals(this.getUid())){
						this.startCoordinatorTerminationProtocol(request);
					} else{
						this.startParticipantTerminationProtocol(request);
					}
					return;
				}
				mType = message.getType();
				if(mType.equals(MessageType.STATE_REQ)){
					switch(this.state){
						case ABORTED: 		stateType = MessageType.ABORTED;
											break;
						case COMMITTABLE:	stateType = MessageType.COMMITTABLE;
											break;
						case COMMITTED:		stateType = MessageType.COMMITTED;
											break;
						case UNCERTAIN:		stateType = MessageType.UNCERTAIN;
					}
					this.sendMessage(this.getCurrentCoordinator().getUid(), stateType, request);
					processed = true;
				} else if (mType == MessageType.FAIL) {
					this.handleFailedProcess(message.getSource());
				} else if(mType == MessageType.UR_ELECTED){
					this.removeCoordinatorFromUpList();
					this.setCurrentCoordinator(this);
					this.startCoordinatorTerminationProtocol(request);
					return;
				} else if (mType == MessageType.ALIVE){
					this.handleResurrectedProcess(message.getSource());
				} else{
					// TODO:  Any other cases to handle?
				}
			}
			
			if (thread.isInterrupted(T_P_FAIL_AFTER_SEND_STATE)) {
				throw new InterruptedException();
			}
			
			/**
			 * Wait for response from coordinator
			 */
			processed = false;
			while(!processed){
				try{
					message = this.receiveMessage(TIMEOUT);
				} catch(MessageTimeoutException e){
					this.removeCoordinatorFromUpList();
					this.startElectionProtocol(request);
					if(this.getCurrentCoordinator().getUid().equals(this.getUid())){
						this.startCoordinatorTerminationProtocol(request);
					} else{
						this.startParticipantTerminationProtocol(request);
					}
					return;
				}
				mType = message.getType();
				if(mType == MessageType.ABORT){
					this.getLog().log(ABORT);
					return;
				} else if(mType == MessageType.COMMIT){
					this.getLog().log(COMMIT);
					return;
				} else if(mType == MessageType.FAIL){
					this.handleFailedProcess(message.getSource());
				} else if(mType == MessageType.ALIVE){
					this.handleResurrectedProcess(message.getSource());
				} else if(mType == MessageType.UR_ELECTED){
					this.removeCoordinatorFromUpList();
					this.setCurrentCoordinator(this);
					this.startCoordinatorTerminationProtocol(request);
					return;
				} else if(mType == MessageType.PRE_COMMIT){
					this.sendMessage(this.getCurrentCoordinator().getUid(), MessageType.ACK, request);
					processed = true;
				} else{
					// TODO:  Any other cases to handle?
				}
			}
			
			if(thread.isInterrupted(T_P_FAIL_AFTER_ACK)){
				throw new InterruptedException();
			}
			
			/** 
			 * Pre-Commit
			 */
			processed = false;
			while(!processed){
				try{
					message = this.receiveMessage(TIMEOUT);
				} catch(MessageTimeoutException e){
					this.removeCoordinatorFromUpList();
					this.startElectionProtocol(request);
					if(this.getCurrentCoordinator().getUid().equals(this.getUid())){
						this.startCoordinatorTerminationProtocol(request);
					} else{
						this.startParticipantTerminationProtocol(request);
					}
					return;
				}
				
				mType = message.getType();
				if(mType == MessageType.COMMIT){
					this.getLog().log(COMMIT);
					processed = true;
				} else if(mType == MessageType.FAIL){
					this.handleFailedProcess(message.getSource());
				} else if(mType == MessageType.ALIVE){
					this.handleResurrectedProcess(message.getSource());
				} else if(mType == MessageType.UR_ELECTED){
					this.removeCoordinatorFromUpList();
					this.setCurrentCoordinator(this);
					this.startCoordinatorTerminationProtocol(request);
					return;
				}
			}
			
		} catch(InterruptedException e){
			
		}
		
		
	}
	
	private void removeCoordinatorFromUpList(){
		if(this.getCurrentCoordinator().getUid() != this.getUid())
			this.getUpList().remove(this.getCurrentCoordinator());
	}
	
	protected void startRecoveryFromFailure() {
		// TODO Auto-generated method stub
		
	}

	private void startElectionProtocol(R request) {
		Participant<R> newCoordinator = this.getUpList().first();
		this.setCurrentCoordinator(newCoordinator);
		
		this.sendMessage(this.getCurrentCoordinator().getUid(),
				MessageType.UR_ELECTED, request);
	}

	private void handleResurrectedProcess(String uid) {
		Participant<R> resurrectedParticipant = findParticipant(uid);
		if (resurrectedParticipant == null) {
			throw new IllegalStateException();
		}

		// TODO: what to do here?!
	}

	private void handleFailedProcess(String uid) {
		Participant<R> failedParticipant = findParticipant(uid);
		if (failedParticipant == null) {
			throw new IllegalStateException();
		}

		this.getUpList().remove(failedParticipant);
	}

	private void coordinatorAbort(Set<Participant<R>> peeps, R request) {
		this.getLog().log(ABORT);
		this.state = State.ABORTED;
		this.abort(request);

		this.sendMessage(this.getManagerAddress().getHostName(), this
				.getManagerAddress(), Message.MessageType.ABORT, request);

		this.broadcastMessage(peeps, Message.MessageType.ABORT, request);
	}

	private Participant<R> findParticipant(String uid) {
		Participant<R> par = null;
		for (Participant<R> p : this.getParticipants()) {
			if (p.getUid().equals(uid)) {
				par = p;
				break;
			}
		}

		return par;
	}
}
