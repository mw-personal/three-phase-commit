package transactionProtocol;

import java.io.Serializable;

public class Message<R extends Request> implements Serializable {
	
	private static final long serialVersionUID = -2723845472583508382L;

	public enum MessageType {
		VOTE_REQ,
		YES,
		PRE_COMMIT,
		ACK,
		COMMIT,
		NO,
		ABORT,
		INITIATE,
		FAIL,
		ALIVE,
		UR_ELECTED,
		STATE_REQ,
		ABORTED,
		COMMITTED,
		UNCERTAIN,
		COMMITTABLE
	}
	
	private MessageType type;
	private String source;
	private String dest;
	private long timestamp;
	private R request;
	
	public Message(MessageType type, String source, String dest, long timestamp, R r){
		this.type = type;
		this.source = source;
		this.dest = dest;
		this.timestamp = timestamp;
		this.request = r;
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

	public R getRequest() {
		return request;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\n  " + type);
		sb.append(",\n  " + source);
		sb.append(",\n  " + dest);
		sb.append(",\n  " + timestamp);
		sb.append(",\n  " + request);
		sb.append("\n}");
		
		return sb.toString();
	}
}
