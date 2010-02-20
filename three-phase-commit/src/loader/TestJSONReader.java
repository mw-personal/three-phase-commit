package loader;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import atomicCommit.Participant;


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
		
		
		final String TEST_FILE = "testparticipantconfig.txt";
		
		try {
			ParticipantConfiguration.generateParticipantConfigurationFile(5, 8080, TEST_FILE);
			List<Participant> addressBook = ParticipantConfiguration.readParticipantConfiguration(TEST_FILE);
			
			for(Participant p : addressBook) {
				System.out.println(p);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
