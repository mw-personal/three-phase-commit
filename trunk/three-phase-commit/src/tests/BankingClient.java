package tests;

import applications.banking.BankingParticipant;
import applications.banking.BankingRequest;
import transactionProtocol.Participant;
import transactionProtocol.Request;
import transactionProtocol.TransactionManager;

public class BankingClient implements Client {

	private TransactionManager<BankingRequest, BankingParticipant> manager;
	private BankingRequest request;
	private long clientId;
	
	public BankingClient(TransactionManager<BankingRequest, BankingParticipant> manager,
			BankingRequest request, long clientId){
		this.manager = manager;
		this.request = request;
		this.clientId = clientId;
	}
	
	public void run(){
		System.out.println(clientId + ": " + request.getType());
		boolean outcome = manager.sendRequest(request);
		if(outcome){
			System.out.println("Committed!!");
		} else{
			System.out.println("Aborted...");
		}
	}
	
}
