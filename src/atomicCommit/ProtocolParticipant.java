package atomicCommit;

import java.net.InetSocketAddress;

import logger.Logger;

public interface ProtocolParticipant {
	// methods for sending/receiving data
	public void broadcastMessage(Message m);
	public void sendMessage(InetSocketAddress a, Message m);
	public Message receiveMessage(int timeout) throws MessageTimeoutException;
	
	// general methods for each pariticipant
	public boolean isCoordinator();
	public Vote castVote();
	public void commit();
	public void abort();
	
	// method for getting log
	public Logger getLog();
	
	// methods for protocol handles
	public void setCommitProtocol(Protocol p);
	public void setTerminationProtocol(Protocol p);
	public Protocol getCommitProtocol();
	public Protocol getTerminationProtocol();
}
