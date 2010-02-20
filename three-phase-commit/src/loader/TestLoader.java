package loader;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import transactionProtocol.Message;
import transactionProtocol.MessageTimeoutException;

import applications.banking.BankingParticipant;
import applications.banking.BankingRequest;
import applications.banking.BankingRequest.BankingRequestType;
import applications.calendar.CalendarParticipant;

public class TestLoader {

	public static void main(String[] args) throws InterruptedException {
		final String TEST_FILE = "testparticipantconfig.txt";
		
		try {
			ParticipantConfiguration.generateParticipantConfigurationFile(2,
					8080, TEST_FILE);
			
			List<BankingParticipant> addressBook = ParticipantConfiguration.readParticipantConfiguration(BankingParticipant.class, TEST_FILE);

			BankingParticipant bp1 = addressBook.get(0);
			BankingParticipant bp2 = addressBook.get(1);
			
			bp1.sendMessage(bp2.getAddress(), new Message(Message.MessageType.ACK, bp1.getUid(), bp2.getUid(), System.currentTimeMillis(), 
					new BankingRequest(BankingRequestType.CREATE, "jose", 100.0)));
			System.out.println(bp2.receiveMessage(0));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessageTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
//		final ParticipantLauncher<BankingParticipant> pl = 
//			new ParticipantLauncher<BankingParticipant>(BankingParticipant.class, TEST_FILE);
		
//		final ParticipantLauncher<CalendarParticipant> pl2 = 
//			new ParticipantLauncher<CalendarParticipant>(CalendarParticipant.class, TEST_FILE);		
//		
//		pl.start();
//		Thread.sleep(1000);
//		pl.stop();
		
//		pl2.start();
//		Thread.sleep(1000);
//		pl2.stop();
	}
}
