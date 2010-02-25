package applications.banking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import threePhaseCommit.ThreePhaseCommitParticipant;
import transactionProtocol.Participant;
import transactionProtocol.Vote;


public class BankingParticipant extends ThreePhaseCommitParticipant<BankingRequest> {

	private Map<String, Double> accounts;

	public BankingParticipant(String uid, int ranking, String defaultVote,
			InetSocketAddress address,
			Set<Participant<BankingRequest>> participants, String logFile)
			throws IOException {
		super(uid, ranking, defaultVote, address, participants, logFile);
		this.accounts = new HashMap<String, Double>();
	}
	
	@Override
	public void abort(BankingRequest r) {
		// TODO Auto-generated method stub
	}

	@Override
	public Vote castVote(BankingRequest r) {
		// this does not actually perform the action!
		boolean hasAccount = accounts.containsKey(r.getAccountName());
		
		switch(r.getType()) {
		case CREATE:
			return Vote.valueOf(!hasAccount && r.getAmount() >= 0);
		case DELETE:
			return Vote.valueOf(hasAccount);
		case DEPOSIT:
			return Vote.valueOf(hasAccount && r.getAmount() >= 0);
		case WITHDRAW:
			return Vote.valueOf(hasAccount &&
					r.getAmount() >= 0 &&
					accounts.get(r.getAccountName()) >= r.getAmount());
		// TODO: this should return our default vote
		default: return Vote.NO;
		}
	}

	@Override
	public void commit(BankingRequest r) {
		switch(r.getType()){
		case CREATE:
			accounts.put(r.getAccountName(), r.getAmount());
			return;
		case DELETE:
			accounts.remove(r.getAccountName());
			return;
		case DEPOSIT:
			accounts.put(r.getAccountName(), r.getAmount());
			return;
		case WITHDRAW:
			accounts.put(r.getAccountName(), 
					accounts.get(r.getAccountName()) - r.getAmount());
			return;
		}
	}	
	
	@Override
	public String toString() {
		String s = super.toString();
		return s + "BANKING!";
	}
	
}
