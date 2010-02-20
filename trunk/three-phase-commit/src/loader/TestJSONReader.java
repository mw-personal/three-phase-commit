package loader;

import java.io.IOException;


public class TestJSONReader {

	public static void main(String[] args) {
//		try {
//			ParticipantConfiguration.readParticipantConfig("participantconfig.txt");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		try {
			ParticipantConfiguration.generateParticipantConfigurationFile(5, 8080, "testparticipantconfig.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
