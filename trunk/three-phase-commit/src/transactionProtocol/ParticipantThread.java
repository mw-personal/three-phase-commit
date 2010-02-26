package transactionProtocol;

import transactionProtocol.Participant.ExecutionState;

public class ParticipantThread<R extends Request, P extends Participant<R>> extends Thread {

	private P participant;
	private String pointToFail;
	
	public ParticipantThread (P participant, String pointToFail) {
		this.participant = participant;
		this.pointToFail = pointToFail;
	}

	public boolean isInterrupted(String s) {
		boolean toInterrupt = (s == null) ? isInterrupted() : ((s.equals(this.pointToFail)) ? true : isInterrupted());
		if (toInterrupt) {
			participant.setExecutionState(ExecutionState.FAILED);
		}
			
		return toInterrupt;
	}
	
	@Override
	public void run() {
		super.run();
		
		switch(participant.getExecutionState()) {
		case FAILED:
			participant.setExecutionState(ExecutionState.RECOVERING);
			//participant.startRecoveryProtocol();
		case READY:
			participant.setExecutionState(ExecutionState.STARTED);
			participant.startCommitProtocol();
			break;
		}
	}
}
