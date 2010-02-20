package transactionProtocol;

public class RunnableParticipant implements Runnable {

	Participant participant;
	
	public RunnableParticipant(Participant p){
		this.participant = p;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			participant.start();
		}
	}

}
