package applications.banking;

import transactionProtocol.Request;

public class BankingRequest implements Request {
	public enum BankingRequestType {
		CREATE,
		DELETE,
		WITHDRAW,
		DEPOSIT
	}

	private final BankingRequestType type;
	private final String accountName;
	private final double amount;
	
	public BankingRequest(String req) {
		// TODO: parse from string
		this(BankingRequestType.CREATE, "", 0);	
	}
	
	public BankingRequest(BankingRequestType type, String accountName, double amount) {
		this.type = type;
		this.accountName = accountName;
		this.amount = amount;
	}

	public BankingRequestType getType() {
		return type;
	}

	public String getAccountName() {
		return accountName;
	}

	public double getAmount() {
		return amount;
	}
}
