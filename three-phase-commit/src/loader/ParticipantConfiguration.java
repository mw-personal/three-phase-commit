package loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import atomicCommit.Participant;

public class ParticipantConfiguration {
	
	// expected JSON tags!
	public static final String PARTICIPANTS = "participants";
	public static final String UID = "uid";
	public static final String INBOX_IP = "inbox-ip";
	public static final String INBOX_PORT = "inbox-port";
	public static final String HEART_IP = "heartbeat-ip";
	public static final String HEART_PORT = "heartbeat-port";
	public static final String PRIORITY = "priority";
	public static final String DEFAULT_VOTE = "default-vote";
	public static final String LOG_FILE = "log-file";
	
	// reader
	public static List<Participant> readParticipantConfiguration(String configFile) throws IOException, JSONException {
		return readParticipantConfiguration(new File(configFile));
	}
	
	// writer
	public static void generateParticipantConfigurationFile(int numParticipants, int port, String filePath) throws IOException {
		generateParticipantConfigurationFile(numParticipants, port, new File(filePath));
	}
	
	public static List<Participant> readParticipantConfiguration(File configFile) throws IOException, JSONException {
		// read entire file
		FileInputStream fis = new FileInputStream(configFile);
		byte[] b = new byte[(int) configFile.length()];
		fis.read(b);
		fis.close();
		
		// create json object from file
		JSONObject jobj = new JSONObject(new String(b));
		JSONArray jarray = jobj.getJSONArray(PARTICIPANTS);
		
		for(int i = 0; i < jarray.length(); i++) {
			JSONObject j = jarray.getJSONObject(i);
			
			String uid = j.getString(UID);
			InetSocketAddress inboxAddress = new InetSocketAddress(
					InetAddress.getByName(j.getString(INBOX_IP)),
					j.getInt(INBOX_PORT));
			InetSocketAddress heartAddress = new InetSocketAddress(
					InetAddress.getByName(j.getString(HEART_IP)),
					j.getInt(HEART_PORT));
			int priority = j.getInt(PRIORITY);
			String defaultVote = j.getString(DEFAULT_VOTE);
			String logFile = j.getString(LOG_FILE);
			
			//Participant p = new Participant(j.getString(ID), address, "mylogfile");
		}
		
		return null;
	}
		
	public static void generateParticipantConfigurationFile(int numParticipants, int port, File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		
		fw.write(generateParticipantConfiguration(numParticipants, port));
		fw.close();
	}
	
	private static String generateParticipantConfiguration(int numParticipants, int port) {	
		final JSONArray jarray = new JSONArray();
		String ipAddress;
		int currentPort = port;
		
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			ipAddress = "0.0.0.0";
		}
		
		try {
			for (int i = 0; i < numParticipants; i++) {
				JSONObject jobj = new JSONObject();
				jobj.put(UID, UUID.randomUUID().toString());
				jobj.put(INBOX_IP, ipAddress);
				jobj.put(INBOX_PORT, currentPort++);
				jobj.put(HEART_IP, ipAddress);
				jobj.put(HEART_PORT, currentPort++);
				jobj.put(PRIORITY, -1);
				jobj.put(DEFAULT_VOTE, "?");
				jobj.put(LOG_FILE, "mylogfile" + i + ".txt");
				
				jarray.put(jobj);
			}
			
			return ((new JSONObject()).put(PARTICIPANTS, jarray)).toString(4);
		} catch (JSONException e) {
			e.printStackTrace();
			return "Error: writing JSON for pariticipant configuration file!";
		}
	}
	
}
