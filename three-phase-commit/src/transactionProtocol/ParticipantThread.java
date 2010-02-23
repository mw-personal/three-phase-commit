package transactionProtocol;

public class ParticipantThread<P extends Participant<? extends Request>> extends Thread {

	private P participant;
	private String pointToFail;
	
	public ParticipantThread (P participant, String pointToFail) {
		this.participant = participant;
		this.pointToFail = pointToFail;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#interrupt()
	 */
	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		super.interrupt();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#isInterrupted()
	 */
	@Override
	public boolean isInterrupted() {
		// TODO Auto-generated method stub
		return super.isInterrupted();
	}
	
	public boolean isInterrupted(String s) {
		return (s == null) ? isInterrupted() : ((s.equals(this.pointToFail)) ? true : isInterrupted());
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#start()
	 */
	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		participant.startCommitProtocol();
	}
	
	

	
	
}
