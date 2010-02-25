package tests;

import transactionProtocol.Participant;
import transactionProtocol.Request;
import transactionProtocol.TransactionManager;

public interface Client<R extends Request, P extends Participant<R>> extends Runnable {
	
	public void run();
	
}
