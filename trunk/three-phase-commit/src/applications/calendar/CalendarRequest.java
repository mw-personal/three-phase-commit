package applications.calendar;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import transactionProtocol.Message;
import transactionProtocol.Request;

public class CalendarRequest implements Request {

	private static final long serialVersionUID = -4788203264363451388L;
	// address and inbox for process making request
	private InetSocketAddress address; 
	private ServerSocket inbox;
	
	
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
	
}
