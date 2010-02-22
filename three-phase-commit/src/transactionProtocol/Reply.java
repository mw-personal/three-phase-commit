package transactionProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * A Reply is an object written to the outside world from the TransactionManager
 * via the coordinator on completion of a 3PC protocol.
 * @author cjlax26
 *
 */
public interface Reply {

	public ServerSocket getServer();
	public InetSocketAddress getAddress();
	
	public Reply readObject(InputStream in) throws IOException, ClassNotFoundException;
	public void writeObject(OutputStream out) throws IOException;
	
}
