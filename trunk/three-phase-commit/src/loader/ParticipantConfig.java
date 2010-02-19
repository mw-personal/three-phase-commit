package loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import atomicCommit.Participant;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;

public class ParticipantConfig {
	
	public static final String PARTICIPANTS = "participants";
	public static final String ID = "id";
	public static final String INBOXIP = "inboxip";
	public static final String INBOXPORT = "inboxport";
	public static final String HEARTIP = "heartbeatip";
	public static final String HEARTPORT = "heartbeatport";
	public static final String PRIORITY = "priority";
	public static final String DEFAULTVOTE = "defaultvote";
	
	public static List<Participant> readParticipantConfig(String configFile) throws IOException, JSONException {
		return readParticipantConfig(new File(configFile));
	}
	
	public static List<Participant> readParticipantConfig(File configFile) throws IOException, JSONException {		
		FileInputStream fis = new FileInputStream(configFile);
		byte[] b = new byte[(int) configFile.length()];
		fis.read(b);
		fis.close();
		
		String jsonfile = new String(b);
		JSONObject jobj = new JSONObject(jsonfile);
		JSONArray jarray = jobj.getJSONArray(PARTICIPANTS);
		
		for(int i = 0; i < jarray.length(); i++) {
			JSONObject j = jarray.getJSONObject(i);
			System.out.println(j);
			///InetSocketAddress address = new InetSocketAddress(
			//		InetAddress.getByName(j.getString(INBOXIP)),
			//		j.getInt(INBOXPORT));
			//Participant p = new Participant(j.getString(ID), address, "mylogfile");
		}
		
		return null;
	}
	
}
