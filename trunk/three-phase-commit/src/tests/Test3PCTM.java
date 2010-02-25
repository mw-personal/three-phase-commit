package tests;

import java.io.IOException;
import java.net.InetSocketAddress;

import loader.ParticipantConfiguration;

import org.json.JSONException;

import transactionProtocol.TransactionManager;

import applications.banking.BankingParticipant;
import applications.banking.BankingRequest;

public class Test3PCTM {

	public static void main(String[] args) throws IOException, JSONException {
		final String configFile = "testparticipantconfig.txt";
		final int numParticipants = 4;
		
		ParticipantConfiguration.generateParticipantConfigurationFile(numParticipants, 8090, configFile);
		
		TransactionManager<BankingRequest, BankingParticipant> tm = 
			new TransactionManager<BankingRequest, BankingParticipant>(
				BankingParticipant.class, configFile, new InetSocketAddress(8080));

		tm.initParticipants();
		
//		Client[] clients = new Client[numParticipants];
//		for(int i = 0; i < clients.length; ++i){
//			clients[i] = new Client(tm, null, i);
//			new Thread(clients[i]).start();
//		}
		
		System.out.println("client1: is creating an account with $200");
		BankingRequest request = new BankingRequest(BankingRequest.BankingRequestType.CREATE,
										"client1", 200);
		BankingClient client1 = new BankingClient(tm, request, 1);
		new Thread(client1).start();
		
		
		// create client threads, passing tpctm, which call
		// tpctm.sendRequest(null);
		// tpctm.sendRequest(null);
		// tpctm.sendRequest(null);
		// tpctm.sendRequest(null);
		// tpctm.sendRequest(null);
		// tpctm.sendRequest(null);
		// tpctm.sendRequest(null);
		// tpctm.sendRequest(null);
	}

}
