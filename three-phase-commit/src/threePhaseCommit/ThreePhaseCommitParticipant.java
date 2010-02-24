package threePhaseCommit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import transactionProtocol.Participant;
import transactionProtocol.Request;

public abstract class ThreePhaseCommitParticipant<R extends Request> extends Participant<R> {

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
