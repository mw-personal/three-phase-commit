package threePhaseCommit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import threePhaseCommit.ThreePhaseCommitTransactionManager.State;
import transactionProtocol.Participant;
import transactionProtocol.Request;

public abstract class ThreePhaseCommitParticipant<R extends Request> extends Participant<R> {

	
	// Needed for termination protocol
	enum State{
		ABORTED,
		COMMITTED,
		COMMITTABLE,
		UNCERTAIN
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
	private static final String P_FAIL_AFTER_ACK= "Participant-Fail-After-Ack";
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
	
	
	private State state = State.ABORTED;
	
	public ThreePhaseCommitParticipant(String uid, int ranking,
			String defaultVote, InetSocketAddress address,
			Map<String, InetSocketAddress> addressBook, String logFile)
			throws IOException {
		super(uid, ranking, defaultVote, address, addressBook, logFile);
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
		
	}
	
	public void startParticipantCommitProtocol() {
		
	}

	public void startCoordinatorTerminationProtocol() {
		
	}
	
	public void startParticipantTerminationProtocol() {
		
	}

}
