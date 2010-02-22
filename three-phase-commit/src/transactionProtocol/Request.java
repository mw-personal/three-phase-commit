package transactionProtocol;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;


public interface Request extends Serializable {
	// ServerSocket and address of process making Request (outside world)
	public ServerSocket getServer();
	public InetSocketAddress getAddress();
}
