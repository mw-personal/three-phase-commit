package threePhaseCommit;

import java.net.InetSocketAddress;
import java.util.List;

import transactionProtocol.Message;
import transactionProtocol.MessageTimeoutException;
import transactionProtocol.Participant;
import transactionProtocol.Protocol;
import transactionProtocol.TransactionProtocol;
import transactionProtocol.Vote;


public class ThreePhaseCommitProtocol implements TransactionProtocol {
	
	protected static final int TIMEOUT = 0;

	public InetSocketAddress coordinator;
	public List<InetSocketAddress> participants;

	public InetSocketAddress getCoordinator() {
		return this.coordinator;
	}

	public List<InetSocketAddress> getParticiapnts() {
		return this.participants;
	}

	public void startTransaction() {
		return;
	}

	public Protocol getCommitProtocol() {
		return new Protocol() {

			public void start(Participant p) {
				if (p.isCoordinator()) {
					coordinator(p);
				} else {
					participant(p);
				}
			}

			protected void coordinator(Participant p) {
				// write start-3PC to log
				p.getLog().log("START-3PC");

				// send VOTE-REQ to all participants
				p.broadcastMessage(Message.VOTE_REQ);

				// wait for vote messages from all participants
				try {
					// TODO: need to receive message from everyone
					p.receiveMessage(TIMEOUT);
				}
				// on timeout, abort transaction and notify
				catch (MessageTimeoutException e) {
					// TODO: this is a possible error.
					// TODO: p needs to call p.abort();
					p.getLog().log("ABORT");
					// TODO: this can be improved!
					p.broadcastMessage(Message.ABORT);
					return;
				}

				// if all messages were yes, and i vote yes, then begin
				Vote v = p.castVote();
				if (v == Vote.YES) {
					// send PRE-COMMIT to all participants
					p.broadcastMessage(Message.PRE_COMMIT);
					try {
						// TODO need to receive ack from all participants
						p.receiveMessage(TIMEOUT);
					} catch (MessageTimeoutException e) {
						// swallow exception
					}

					// write commit to log
					p.getLog().log("COMMIT");
					p.broadcastMessage(Message.COMMIT);
				} else {
					// TODO: this is a possible error.
					// TODO: p needs to call p.abort();
					p.getLog().log("ABORT");
					// TODO: this can be improved!
					p.broadcastMessage(Message.ABORT);
					return;
				}
			}

			protected void participant(Participant p) {
				//
				// wait for VOTE-REQ from coordinator
				try {
					// TODO: should we check that we received the
					// correct mesage?
					p.receiveMessage(TIMEOUT);
				}
				// on timeout, log ABORT and return
				catch (MessageTimeoutException e) {
					// TODO: this is a possible error.
					// TODO: p needs to call p.abort();
					p.getLog().log("ABORT");
					return;
				}

				Vote v = p.castVote();
				if (v == Vote.YES) {
					// log YES, and send vote to coordinator
					p.getLog().log("YES");
					p.sendMessage(getCoordinator(), Message.YES);

					// wait for response from coordinator
					Message m = null;
					try {
						m = p.receiveMessage(TIMEOUT);
						// TODO: should we check that we received
						// the correct message?
					}
					// on timeout
					catch (MessageTimeoutException e) {
						// begin election
						// if elected then act as coordinator
						// begin termination protocol
						// else
						// begin termination protocol as participant
						// return
					}
					
					if (m == Message.PRE_COMMIT) {
						// send ACK to coordinator
						p.sendMessage(getCoordinator(), Message.ACK);
						// wait for COMMMIT from coordinator
						try {
							p.receiveMessage(TIMEOUT);
						}
						// on timeout
						catch (MessageTimeoutException e) {
							// begin election
							// if elected then act as coordinator
							// begin termination protocol
							// else
							// begin termination protocol as participant
						}
						
						// log COMMIT
						p.getLog().log("COMMIT");
					}
					
					// abort was received from the coordinator
					else {
						// log ABORT
						p.getLog().log("ABORT");
					}
				}
				
				// if myvote is NO then
				else {
					// send NO to coordinator
					p.sendMessage(getCoordinator(), Message.NO);
					// write ABORT
					p.getLog().log("ABORT");
				}
			}
		};
	}

	public Protocol getTerminationProtocol() {
		return new Protocol() {

			public void start(Participant p) {
				// TODO Auto-generated method stub

			}
		};
	}
}
