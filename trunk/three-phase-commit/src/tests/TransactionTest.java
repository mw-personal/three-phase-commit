package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import atomicCommit.Message;
import atomicCommit.MessageTimeoutException;
import atomicCommit.Participant;

public class TransactionTest {
	
	protected static String PARTICIPANT_CONFIG = "participantconfig.txt";
	
	protected static void generateParticipantConfig(int num, int portStart) throws UnknownHostException {
		StringBuilder sb = new StringBuilder();
		InetAddress host = InetAddress.getLocalHost();
		for (int i = 0; i < num; i++) {
			sb.append(UUID.randomUUID().toString());
			sb.append("::");
			sb.append(host.getHostAddress());
			sb.append("::");
			sb.append(portStart+i);
			sb.append("\n");
		}
		
		try {
			File f = new File(PARTICIPANT_CONFIG);
			FileWriter fw = new FileWriter(f);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static List<Participant> createParticipantsFromConfigFile(String configFile) throws NumberFormatException, UnknownHostException, IOException {
		ArrayList<Participant> list = new ArrayList<Participant>();
		
		BufferedReader f = new BufferedReader(new FileReader(configFile));

		String s = f.readLine();
		int i = 0;
		while (s != null) {
			String[] is = s.split("::");
			Participant p = new Participant(is[0], InetAddress.getByName(is[1]),
					Integer.parseInt(is[2]), "mylogfile"+(i++)+".txt");
			list.add(p);
			s = f.readLine();
		}

		f.close();
		
		
		return list;
	}
	
	protected static Map<String, InetSocketAddress> createAddressBookFromParticipants(List<Participant> peeps) {
		HashMap<String, InetSocketAddress> addressBook = new HashMap<String, InetSocketAddress>();
		
		for(Participant p : peeps) {
			addressBook.put(p.getUid(), p.getAddress());
		}
		
		return addressBook;
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		generateParticipantConfig(100, 1987);
		List<Participant> peeps = createParticipantsFromConfigFile(PARTICIPANT_CONFIG);
		Map<String, InetSocketAddress> addressBook = createAddressBookFromParticipants(peeps);
		for(Participant p : peeps) {
			p.setAddressBook(addressBook);
		}
		
		System.out.println();
		
//		try {
//			Participant p = new Participant("jfalcon", InetAddress
//					.getLocalHost(), 8082, "mylogfile.txt", "participantconfig.txt");
//			
//			Participant p2 = new Participant("chris", InetAddress
//					.getLocalHost(), 8083, "mylogfile2.txt", "participantconfig.txt");
//			
//			
//			p.sendMessage(p2.getAddress(), Message.ACK);
//			p.sendMessage(p2.getAddress(), Message.ABORT);
//			p.sendMessage(p2.getAddress(), Message.COMMIT);
//			
//			try {
//				System.out.println(p2.receiveMessage(0));
//				System.out.println(p2.receiveMessage(0));
//				System.out.println(p2.receiveMessage(0));
//			//	System.out.println(p2.receiveMessage(0));
//			} catch (MessageTimeoutException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			System.out.println("finished");
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
