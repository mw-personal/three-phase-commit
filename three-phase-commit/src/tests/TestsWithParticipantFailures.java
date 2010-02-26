package tests;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import loader.ParticipantConfiguration;
import loader.ParticipantThreadPool;
import threePhaseCommit.ThreePhaseCommitParticipant;
import transactionProtocol.ParticipantThread;
import transactionProtocol.TransactionManager;
import applications.banking.BankingParticipant;
import applications.banking.BankingRequest;
import applications.banking.BankingRequest.BankingRequestType;

public class TestsWithParticipantFailures {

	public static final String CONFIG_FILE = "TestsWithFailures.txt";
	public static final int NUM_PARTICIPANTS = 4;
	public static final int MANAGER_PORT = 8090;
	
	private ParticipantThread<BankingRequest, BankingParticipant> failingThread;
	private ParticipantThreadPool<BankingRequest, BankingParticipant> tpool;
	private TransactionManager<BankingRequest, BankingParticipant> transMan; // hehe

	@Before
	public void testSetup() throws IOException {
		ParticipantConfiguration.generateParticipantConfigurationFile(
				NUM_PARTICIPANTS, 9090, CONFIG_FILE);

		try {
			tpool = new ParticipantThreadPool<BankingRequest, BankingParticipant>
				(BankingParticipant.class, CONFIG_FILE);
			transMan = new TransactionManager<BankingRequest, BankingParticipant>(
					BankingParticipant.class, tpool,
					new InetSocketAddress(MANAGER_PORT));
			transMan.initParticipants();
			
			// find a thread that is not the coordinator
			for (ParticipantThread<BankingRequest, BankingParticipant> p : tpool.getParticipantThreads().values()) {
				if (!p.getParticipant().isCoordinator()) {
					failingThread = p;
					break;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@After
	public void testTearDown() {
		tpool.stop();
	}
	
	@Test
	public void testCreateAccountFailBeforeVoteReq() {
		failingThread.setPointToFail(ThreePhaseCommitParticipant.P_FAIL_BEFORE_VOTE_REQ);
		Assert.assertFalse(
					sendRequestToManager(BankingRequestType.CREATE, randomAccountName(), 200));
	}
	
	//@Test
	public void testCreateAccountFailAfterVoteBeforeSend() {
		failingThread.setPointToFail(ThreePhaseCommitParticipant.P_FAIL_AFTER_VOTE_BEFORE_SEND);
		Assert.assertFalse(
					sendRequestToManager(BankingRequestType.CREATE, randomAccountName(), 200));
	}
	
	//@Test
	public void testCreateAccountFailAfterVoteAfterSend() {
		failingThread.setPointToFail(ThreePhaseCommitParticipant.P_FAIL_AFTER_VOTE_AFTER_SEND);
		Assert.assertTrue(
					sendRequestToManager(BankingRequestType.CREATE, randomAccountName(), 200));
	}
	
	//@Test
	public void testCreateAccountFailAfterAck() {
		failingThread.setPointToFail(ThreePhaseCommitParticipant.P_FAIL_AFTER_ACK);
		Assert.assertTrue(
					sendRequestToManager(BankingRequestType.CREATE, randomAccountName(), 200));
	}
	
	//@Test
	public void testCreateAccountFailAfterCommit() {
		failingThread.setPointToFail(ThreePhaseCommitParticipant.P_FAIL_AFTER_COMMIT);
		Assert.assertTrue(
					sendRequestToManager(BankingRequestType.CREATE, randomAccountName(), 200));
	}	
	
	private boolean sendRequestToManager(BankingRequestType type, String accountName, double amount) {
		return transMan.sendRequest(new BankingRequest(0, type, accountName, amount));
	}

	private String randomAccountName() {
		return UUID.randomUUID().toString();
	}

}
