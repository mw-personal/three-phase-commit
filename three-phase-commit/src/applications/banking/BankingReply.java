package applications.banking;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import transactionProtocol.Reply;

public class BankingReply implements Reply{

	
	private InetSocketAddress address; 
	private ServerSocket inbox;
	private TransactionOutcome outcome;
	
	public enum TransactionOutcome {
		SUCCESS,
		FAILURE
	}
	
	@Override
	public InetSocketAddress getAddress() {
		// TODO Auto-generated method stub
		return address;
	}

	@Override
	public ServerSocket getServer() {
		// TODO Auto-generated method stub
		return inbox;
	}
	
	public void writeObject(OutputStream stream) throws IOException {
		ObjectOutputStream oos = 
			new ObjectOutputStream(stream);
		oos.writeObject(this);
		oos.close();
	}
	
	public Reply readObject(InputStream stream) throws IOException, ClassNotFoundException {
		ObjectInputStream ois =
			new ObjectInputStream(stream);
		Object obj = ois.readObject();
		ois.close();
		
		if(obj != null && obj instanceof Reply) {
			return (Reply) obj;
		} else {
			throw new ClassNotFoundException("Reply.readObject: Object read from stream was not of type Reply");
		}
	}
	
}
