package tests;

import java.io.IOException;

import loader.ParticipantConfiguration;

import org.json.JSONException;

import threePhaseCommit.ThreePhaseCommitTransactionManager;

import applications.banking.BankingParticipant;
import applications.banking.BankingRequest;

public class Test3PCTM {

	public static void main(String[] args) throws IOException, JSONException {
		final String configFile = "testparticipantconfig.txt";
		final int numParticipants = 4;
		
		ParticipantConfiguration.generateParticipantConfigurationFile(numParticipants, 8090, configFile);
		
		ThreePhaseCommitTransactionManager<BankingRequest, BankingParticipant> tpctm = 
			new ThreePhaseCommitTransactionManager<BankingRequest, BankingParticipant>(
				BankingParticipant.class, configFile, 8080);

		tpctm.initParticipants();
		
		Client[] clients = new Client[numParticipants];
		for(int i = 0; i < clients.length; ++i){
			clients[i] = new Client(tpctm, null, i);
			new Thread(clients[i]).start();
		}
		
		
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
