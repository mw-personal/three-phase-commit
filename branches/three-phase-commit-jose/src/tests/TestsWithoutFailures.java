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
import org.junit.BeforeClass;
import org.junit.Test;

import loader.ParticipantConfiguration;
import transactionProtocol.TransactionManager;
import applications.banking.BankingParticipant;
import applications.banking.BankingRequest;
import applications.banking.BankingRequest.BankingRequestType;

public class TestsWithoutFailures {

	public static final String CONFIG_FILE = "TestsWithoutFailures.txt";
	public static final int NUM_PARTICIPANTS = 4;
	public static final int MANAGER_PORT = 8082;
	
	private static TransactionManager<BankingRequest, BankingParticipant> transMan; // hehe
	private static int testCount;
	private static Set<String> usedAccountNames;
	private static final Random GEN = new Random();

	@BeforeClass
	public static void classSetup() throws IOException {
		ParticipantConfiguration.generateParticipantConfigurationFile(
				NUM_PARTICIPANTS, 9000, CONFIG_FILE);

		try {
			testCount = 0;
			usedAccountNames = new HashSet<String>();
			transMan = new TransactionManager<BankingRequest, BankingParticipant>(
					BankingParticipant.class, CONFIG_FILE,
					new InetSocketAddress(MANAGER_PORT));
			transMan.initParticipants();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void classTearDown() {
		// tm.close()?
	}
	
	@After
	public void testTearDown() {
		Assert.assertTrue(transMan.assertEqualState());
		System.out.println("Completed test #" + testCount++);
	}

	@Test
	public void testCreateAccountValidAmount() {
		// run this a few times to populate the database
		for (int i = 0; i < 5; i++) {
			Assert.assertTrue(
					sendRequestToManager(BankingRequestType.CREATE, freshAccountName(), 200));
		}
	}
	
	@Test
	public void testCreateAccountInvalidAmount() {
		Assert.assertFalse(
				sendRequestToManager(BankingRequestType.CREATE, randomAccountName(), -200));
	}
	
	@Test
	public void testCreateAccountAlreadyExists() {
		Assert.assertFalse(
				sendRequestToManager(BankingRequestType.CREATE, usedAccountName(), 1000));		
	}
	
	@Test
	public void testDeleteAccount() {
		String toDelete = usedAccountName();
		Assert.assertTrue(
				sendRequestToManager(BankingRequestType.DELETE, toDelete, 0));
		usedAccountNames.remove(toDelete);
	}
	
	@Test
	public void testDeleteNonexistentAccount() {
		Assert.assertFalse(
				sendRequestToManager(BankingRequestType.DELETE, randomAccountName(), 0));
	}
	
	@Test
	public void testDepositIntoAccountValidAmount() {
		Assert.assertTrue(
				sendRequestToManager(BankingRequestType.DEPOSIT, usedAccountName(), 1000));
	}
	
	@Test
	public void testDepositIntoAccountInvalidAmount() {
		Assert.assertFalse(
				sendRequestToManager(BankingRequestType.DEPOSIT, usedAccountName(), -100));
	}
	
	@Test
	public void testDepositIntoNonexistantAccount() {
		Assert.assertFalse(
				sendRequestToManager(BankingRequestType.DEPOSIT, randomAccountName(), 1000));		
	}
	
	@Test
	public void testWithdrawFromAccountValidAmount() {
		Assert.assertTrue(
				sendRequestToManager(BankingRequestType.WITHDRAW, usedAccountName(), 10));
	}
	
	@Test
	public void testWithdrawFromAccountInvalidAmount() {
		Assert.assertFalse(
				sendRequestToManager(BankingRequestType.WITHDRAW, usedAccountName(), -100));
	}
	
	@Test
	public void testWithdrawFromAccountWithInsufficientFunds() {
		Assert.assertFalse(
				sendRequestToManager(BankingRequestType.WITHDRAW, usedAccountName(), 100000000));
	}
	
	@Test
	public void testWithdrawFromNonexistantAccount() {
		Assert.assertFalse(
				sendRequestToManager(BankingRequestType.WITHDRAW, randomAccountName(), 1000));		
	}
	
	@Test
	public void complexBankingTransactionTest() {
		String account = randomAccountName();
		Assert.assertTrue(
				sendRequestToManager(BankingRequestType.CREATE, account, 0));
		Assert.assertFalse(
				sendRequestToManager(BankingRequestType.CREATE, account, 0));
		Assert.assertTrue(
				sendRequestToManager(BankingRequestType.DEPOSIT, account, 500));
		Assert.assertTrue(
				sendRequestToManager(BankingRequestType.DEPOSIT, account, 500));
		// at this point we have 1000 dollars in the bank!
		Assert.assertTrue(
				sendRequestToManager(BankingRequestType.WITHDRAW, account, 500));
		// at this point we have 500
		Assert.assertTrue(
				sendRequestToManager(BankingRequestType.DELETE, account, 1000));
		// delete account
		// attempt to remove the final 500
		Assert.assertFalse(
				sendRequestToManager(BankingRequestType.WITHDRAW, account, 500));		
	}
	
	
	private boolean sendRequestToManager(BankingRequestType type, String accountName, double amount) {
		return transMan.sendRequest(new BankingRequest(testCount, type, accountName, amount));
	}
	
	private String usedAccountName() {
		if (usedAccountNames.isEmpty()) {
			return null;
		}
		
		return (String) usedAccountNames.toArray()[GEN.nextInt(usedAccountNames.size())];
	}
	
	private String freshAccountName() {
		final String name = UUID.randomUUID().toString();
		usedAccountNames.add(name);
		return name;
	}
	
	private String randomAccountName() {
		return UUID.randomUUID().toString();
	}

}
