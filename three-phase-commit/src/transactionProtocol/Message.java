package transactionProtocol;

public enum Message {
	VOTE_REQ,
	YES,
	PRE_COMMIT,
	ACK,
	COMMIT,
	NO,
	ABORT;
}
