package threePhaseCommit;

import java.io.IOException;

import org.json.JSONException;

import applications.banking.BankingParticipant;

public class Test3PCTM {

	public static void main(String[] args) throws IOException, JSONException {
		final String configFile = "testparticipantconfig.txt";

		ThreePhaseCommitTransactionManager<BankingParticipant> tpctm = 
			new ThreePhaseCommitTransactionManager<BankingParticipant>(
				BankingParticipant.class, configFile, 8080);

		tpctm.initParticipants();

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
