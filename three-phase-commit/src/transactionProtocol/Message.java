package transactionProtocol;

import java.io.Serializable;

public class Message implements Serializable {
	
	private static final long serialVersionUID = -2723845472583508382L;

	public enum MessageType{
		VOTE_REQ,
		YES,
		PRE_COMMIT,
		ACK,
		COMMIT,
		NO,
		ABORT,
		INITIATE }
	
	private final MessageType type;
	private final String source;
	private final String dest;
	private final long timestamp;
	private final Request request;
	//private Class<? extends Request> requestType;
	
	public Message(MessageType type, String source, String dest, long timestamp, Request r){
		this.type = type;
		this.source = source;
		this.dest = dest;
		this.timestamp = timestamp;
		this.request = r;
		//this.requestType = requestType;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public MessageType getType() {
		return type;
	}

	public String getSource() {
		return source;
	}

	public String getDest() {
		return dest;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Request getRequest() {
		return request;
	}
	
}
