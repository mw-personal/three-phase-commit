package transactionProtocol;

public class ParticipantThread<P extends Participant<? extends Request>> extends Thread {

	private P participant;
	
	public ParticipantThread (P participant) {
		this.participant = participant;
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
