package transactionProtocol;

public interface TransactionProtocol {
	public Protocol getCommitProtocol();
	public Protocol getTerminationProtocol();
}
