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
		System.out.println("WHATS IS HAPPENING!!!");
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#start()
	 */
	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
		participant.start();
	}

	
	
}
