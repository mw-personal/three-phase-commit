package transactionProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;

public class Message<R extends Request> implements Serializable {
	
	private static final long serialVersionUID = -2723845472583508382L;

	public enum MessageType{
		VOTE_REQ,
		YES,
		PRE_COMMIT,
		ACK,
		COMMIT,
		NO,
		ABORT,
		INITIATE,
		}
	
	private MessageType type;
	private String source;
	private String dest;
	private long timestamp;
	private R request;
	//private Class<? extends Request> requestType;
	
	public Message(MessageType type, String source, String dest, long timestamp, R r){
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
	
	public static void writeObject(OutputStream stream, Message m) throws IOException {
		ObjectOutputStream oos = 
			new ObjectOutputStream(stream);
		oos.writeObject(m);
		oos.close();
	}
	
	public Message<R> readObject(InputStream stream) throws IOException, ClassNotFoundException {
		ObjectInputStream ois =
			new ObjectInputStream(stream);
		Object obj = ois.readObject();
		ois.close();
		
		if(obj != null && obj instanceof Message) {
			return (Message<R>) obj;
		} else {
			throw new ClassNotFoundException("Message.readObject: Objec read from stream was not of type Message");
		}
	}
	
}
