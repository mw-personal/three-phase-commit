package loader;

import applications.banking.BankingParticipant;
import applications.calendar.CalendarParticipant;

public class TestLoader {

	public static void main(String[] args) throws InterruptedException {
		final String TEST_FILE = "testparticipantconfig.txt";
		
//		try {
//			ParticipantConfiguration.generateParticipantConfigurationFile(5, 8080, TEST_FILE);
//			List<Participant> addressBook = ParticipantConfiguration.readParticipantConfiguration(TEST_FILE);
//			
//			for(Participant p : addressBook) {
//				System.out.println(p);
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		final ParticipantLauncher<BankingParticipant> pl = 
			new ParticipantLauncher<BankingParticipant>(BankingParticipant.class, TEST_FILE);
		
//		final ParticipantLauncher<CalendarParticipant> pl2 = 
//			new ParticipantLauncher<CalendarParticipant>(CalendarParticipant.class, TEST_FILE);		
//		
		pl.start();
		Thread.sleep(1000);
		pl.stop();
		
//		pl2.start();
//		Thread.sleep(1000);
//		pl2.stop();
	}
}
