package tests;

import loader.ParticipantConfiguration;
import applications.banking.BankingParticipant;
import transactionProtocol.TransactionManagerTwo;
import transactionProtocol.TransactionManagerTwoFactory;


public class TestTransactionManagerTwoFactory {
	
	public static void main(String[] args) throws Exception{
	
		final String configFile = "testparticipantconfig.txt";
		
		ParticipantConfiguration.generateParticipantConfigurationFile(4, 8090, configFile);
		
		TransactionManagerTwoFactory factory = TransactionManagerTwoFactory.getTransactionManagerTwoFactory();
		TransactionManagerTwo<BankingParticipant> manager = factory.getTransactionManagerTwo(BankingParticipant.class, configFile, 8082);
	
		manager.initParticipants();
		
	}
	
}
