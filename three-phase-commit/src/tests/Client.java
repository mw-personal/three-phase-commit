package tests;

import transactionProtocol.Participant;
import transactionProtocol.Request;
import transactionProtocol.TransactionManager;

public class Client<R extends Request, P extends Participant<R>> implements Runnable {

	private TransactionManager<R,P> manager;
	private R request;
	private long clientId;
	
	public Client(TransactionManager<R,P> tm, R request, long id){
		this.manager = tm;
		this.request = request;
		this.clientId = id;
	}
	
	public void run() {
		System.out.println(clientId + " is sending request to TransactionManager");
		manager.sendRequest(request);
	}

}
