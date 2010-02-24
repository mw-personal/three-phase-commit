package transactionProtocol;

public interface Protocol<R extends Request> {
	public void start(Participant<R> p);
}
