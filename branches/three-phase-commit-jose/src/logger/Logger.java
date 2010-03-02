package logger;

public interface Logger {
	public String getFilePath();
	public boolean log(String data);
	//public boolean clear();
	public boolean close();	
}
