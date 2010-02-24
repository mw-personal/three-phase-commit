package applications.banking;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import transactionProtocol.Message;
import transactionProtocol.Request;

public class BankingRequest implements Request {

	private static final long serialVersionUID = -4778078068961838085L;

	// address and inbox for process making request
	private InetSocketAddress address; 
	private ServerSocket inbox;
	
	public enum BankingRequestType {
		CREATE,
		DELETE,
		WITHDRAW,
		DEPOSIT
	}

	private BankingRequestType type;
	private String accountName;
	private double amount;
	
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
		
	public String toString() {
		return "{" + type + ":" + accountName + ":" + amount + "}";
	}

	public InetSocketAddress getAddress() {
		// TODO Auto-generated method stub
		return address;
	}

	public ServerSocket getServer() {
		// TODO Auto-generated method stub
		return inbox;
	}
	
}
