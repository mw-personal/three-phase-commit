package loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import transactionProtocol.Participant;
import transactionProtocol.Request;


public class ParticipantConfiguration<P extends Participant<? extends Request>> {
	
	// expected JSON tags!
	public static final String PARTICIPANTS = "participants";
	public static final String UID = "uid";
	public static final String INBOX_IP = "inbox-ip";
	public static final String INBOX_PORT = "inbox-port";
	public static final String HEART_IP = "heartbeat-ip";
	public static final String HEART_PORT = "heartbeat-port";
	public static final String RANKING = "ranking";
	public static final String DEFAULT_VOTE = "default-vote";
	public static final String LOG_FILE = "log-file";
	public static final String POINT_TO_FAIL = "point-to-fail";
		
	// writer
	public static void generateParticipantConfigurationFile(int numParticipants, int port, String filePath) throws IOException {
		generateParticipantConfigurationFile(numParticipants, port, new File(filePath));
	}
	
	public static void generateParticipantConfigurationFile(int numParticipants, int port, File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		
		fw.write(generateParticipantConfiguration(numParticipants, port));
		fw.close();
	}

	private JSONObject jobj;
	private Class<P> type;
	
	public ParticipantConfiguration(Class<P> type, String configFile) throws IOException, JSONException {
		this(type, new File(configFile));
	}
	
	public ParticipantConfiguration(Class<P> type, File configFile) throws IOException, JSONException {
		// read entire file
		FileInputStream fis = new FileInputStream(configFile);
		byte[] b = new byte[(int) configFile.length()];
		fis.read(b);
		fis.close();
		
		// create json object from file
		this.jobj = new JSONObject(new String(b));
		this.type = type;
	}
	
	// reader	
	public List<P> getParticipants() throws JSONException, UnknownHostException {
		JSONArray jarray = this.jobj.getJSONArray(PARTICIPANTS);
		
		ArrayList<P> result = new ArrayList<P>(jarray.length());
		Map<String, InetSocketAddress> addressBook = new HashMap<String, InetSocketAddress>();
		
		for(int i = 0; i < jarray.length(); i++) {
			JSONObject j = jarray.getJSONObject(i);
			
			String uid = j.getString(UID);
			InetSocketAddress address = new InetSocketAddress(
					InetAddress.getByName(j.getString(INBOX_IP)),
					j.getInt(INBOX_PORT));
			InetSocketAddress heartAddress = new InetSocketAddress(
					InetAddress.getByName(j.getString(HEART_IP)),
					j.getInt(HEART_PORT));
			int ranking = j.getInt(RANKING);
			String defaultVote = j.getString(DEFAULT_VOTE);
			String logFile = j.getString(LOG_FILE);
			
			P p;
			try {
				p = type.getDeclaredConstructor(String.class, int.class,
						String.class, InetSocketAddress.class,
						InetSocketAddress.class, Map.class, Map.class,
						String.class).newInstance(uid, ranking, defaultVote,
						address, heartAddress, null, null, logFile);
				result.add(p);
				addressBook.put(uid, address);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// we've generated a list of all participants, but we need to give
		// each participant an address book
		for (P p : result) {
			p.setAddressBook(new HashMap<String, InetSocketAddress>(addressBook));
		}
		
		return result;
	}
	
	public Map<String, String> getPointsToFail() throws JSONException {
		JSONArray jarray = this.jobj.getJSONArray(PARTICIPANTS);
		
		Map<String, String> result = new HashMap<String, String>();
		
		for(int i = 0; i < jarray.length(); i++) {
			JSONObject j = jarray.getJSONObject(i);
			
			String uid = j.getString(UID);
			String pointToFail = j.getString(POINT_TO_FAIL);
			result.put(uid, pointToFail);
		}
		
		return result;
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
				jobj.put(RANKING, -1);
				jobj.put(DEFAULT_VOTE, "?");
				jobj.put(LOG_FILE, "logs/mylogfile" + i + ".txt");
				jobj.put(POINT_TO_FAIL, "");
				
				jarray.put(jobj);
			}
			
			return ((new JSONObject()).put(PARTICIPANTS, jarray)).toString(4);
		} catch (JSONException e) {
			e.printStackTrace();
			return "Error: writing JSON for pariticipant configuration file!";
		}
	}
	
}
