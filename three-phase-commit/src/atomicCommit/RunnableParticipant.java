package atomicCommit;

import java.util.concurrent.Callable;

public class RunnableParticipant implement Runnable {

	private Participant p;

	public Boolean call() throws Exception {
		// TODO Auto-generated method stub
		p.start();
		return false;
	}
	
	
	
}
