package loader;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import transactionProtocol.Message;
import transactionProtocol.MessageTimeoutException;
import transactionProtocol.Participant;
import transactionProtocol.Protocol;
import transactionProtocol.Request;
import transactionProtocol.Message.MessageType;

import applications.banking.BankingParticipant;
import applications.banking.BankingRequest;
import applications.banking.BankingRequest.BankingRequestType;
import applications.calendar.CalendarParticipant;

public class TestLoader {

	public static void main(String[] args) throws IOException, JSONException, InterruptedException {
		final String configFile = "testparticipantconfig.txt";
		
		ParticipantConfiguration.generateParticipantConfigurationFile(4, 8090, configFile);
			
		ParticipantThreadPool<BankingParticipant> ptp = 
			new ParticipantThreadPool<BankingParticipant>(BankingParticipant.class, configFile);
		
		for (BankingParticipant bp : ptp.getParticipants()) {
			bp.setCommitProtocol(new Protocol() {
				public void start(Participant<? extends Request> p) {
					try {
						// send a broadcast to all participants
						p.broadcastMessage(MessageType.ACK, null);
						
						// wait to receive a message from all participants
						for (int i = 0; i < 3; i++) {
							try {
								if (Thread.currentThread().isInterrupted()) {
									throw new InterruptedException();
								}
								Message m = p.receiveMessage(1000);
								System.out.println(p.getUid() + " received the following message:\n" + m);
							} catch (MessageTimeoutException e) {
								e.printStackTrace();
							}
						}
					} catch (InterruptedException e) {
						System.out.println(p.getUid() + " has been interrupted and is now quitting!");
					}
				}
			});
		}
		
		ptp.start();
		ptp.stopParticipant(ptp.getParticipants().get(0).getUid());
		Thread.sleep(5000);
		Thread.sleep(5000);
		
		ptp.startParticipant(ptp.getParticipants().get(0).getUid());
		Thread.sleep(5000);
		ptp.stop();
		Thread.sleep(1000);
	}
}
