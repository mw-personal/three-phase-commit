package applications.banking;

import transactionProtocol.Request;

public class BankingRequest implements Request {

	private static final long serialVersionUID = -4778078068961838085L;

	public enum BankingRequestType {
		CREATE,
		DELETE,
		WITHDRAW,
		DEPOSIT
	}

	private final BankingRequestType type;
	private final String accountName;
	private final double amount;
	
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
