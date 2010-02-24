package tests;

import java.io.IOException;

import loader.ParticipantConfiguration;
import loader.ParticipantThreadPool;

import org.json.JSONException;

import transactionProtocol.Message;
import transactionProtocol.MessageTimeoutException;
import transactionProtocol.Participant;
import transactionProtocol.ParticipantThread;
import transactionProtocol.Protocol;
import transactionProtocol.Request;
import transactionProtocol.Message.MessageType;
import applications.banking.BankingParticipant;

public class TestLoader {

	public static void main(String[] args) throws IOException, JSONException, InterruptedException {
		final String configFile = "testparticipantconfig.txt";
		
		ParticipantConfiguration.generateParticipantConfigurationFile(4, 8090, configFile);
			
		ParticipantThreadPool<BankingParticipant> ptp = 
			new ParticipantThreadPool<BankingParticipant>(BankingParticipant.class, configFile);
		
		for (BankingParticipant bp : ptp.getParticipants()) {
			bp.setCommitProtocol(new Protocol() {
				@SuppressWarnings("unchecked")
				public void start(Participant<? extends Request> p) {
					try {
						// send a broadcast to all participants
						p.broadcastMessage(MessageType.ACK, null);
						
						// wait to receive a message from all participants
						for (int i = 0; i < 3; i++) {
							try {
								if (((ParticipantThread<BankingParticipant>) Thread.currentThread()).isInterrupted("omg")) {
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
		
//		InetSocketAddress coordinator;
//		List<InetSocketAddress> addressBook;
//		
//		ThreePhaseCommitTransactionManager tpc = 
//			new ThreePhaseCommitTransactionManager(coordinator, addressBook);
//		
//		tpc.request(myRequest);
		
		
		ptp.start();
		ptp.stopParticipant(ptp.getParticipants().get(0).getUid());
		Thread.sleep(5000);
		Thread.sleep(5000);
		
		//ptp.startParticipant(ptp.getParticipants().get(0).getUid());
		//Thread.sleep(5000);
		ptp.stop();
		Thread.sleep(1000);
	}
}
