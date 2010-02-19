package loader;

import java.io.IOException;

import json.JSONException;

public class TestJSONReader {

	public static void main(String[] args) {
		try {
			ParticipantConfig.readParticipantConfig("participantconfig.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
