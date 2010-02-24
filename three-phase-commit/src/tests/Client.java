package tests;

import transactionProtocol.Participant;
import transactionProtocol.Request;
import transactionProtocol.TransactionManager;

public class Client implements Runnable {

	private TransactionManager<? extends Participant<? extends Request>> manager;
	private Request request;
	private long clientId;
	
	public Client(TransactionManager<? extends Participant<? extends Request>> tm, Request request, long id){
		this.manager = tm;
		this.request = request;
		this.clientId = id;
	}
	
	public void run() {
		System.out.println(clientId + " is sending request to TransactionManager");
		manager.sendRequest(request);
	}

}
