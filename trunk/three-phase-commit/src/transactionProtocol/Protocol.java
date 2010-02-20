package transactionProtocol;

public interface Protocol {
	public void start(Participant<? extends Request> p);
}
