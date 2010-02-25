package tests;

import transactionProtocol.TransactionManager;
import applications.banking.BankingParticipant;
import applications.banking.BankingRequest;

public class BankingClient implements Client<BankingRequest, BankingParticipant> {

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
