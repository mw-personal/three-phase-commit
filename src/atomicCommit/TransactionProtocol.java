package atomicCommit;

import java.net.InetSocketAddress;
import java.util.List;

public interface TransactionProtocol {
	public void startTransaction();
	
	public List<InetSocketAddress> getParticiapnts();
	public InetSocketAddress getCoordinator();
	
	
	
	
	public Protocol getCommitProtocol();
	public Protocol getTerminationProtocol();
}
