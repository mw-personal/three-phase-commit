package logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LogParser {

	private File logFile;
	private ArrayList<String> fileLines;
	
	public LogParser(String logFile) throws IOException {
		this(new File(logFile));
	}
	
	public LogParser(File logFile) throws IOException {
		this.logFile = logFile;
		final BufferedReader fileReader = new BufferedReader(
				new FileReader(this.logFile));
		this.fileLines = new ArrayList<String>();
		
		String currentLine = fileReader.readLine();
		while (currentLine != null) {
			this.fileLines.add(0, currentLine);
			currentLine = fileReader.readLine();
		}
	}
}
