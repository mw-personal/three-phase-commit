package applications.calendar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import transactionProtocol.Participant;
import transactionProtocol.Vote;


public class CalendarParticipant extends Participant<CalendarRequest> {

	public CalendarParticipant(String uid, int ranking, String defaultVote,
			InetSocketAddress address, InetSocketAddress heartAddress,
			Map<String, InetSocketAddress> addressBook,
			Map<String, InetSocketAddress> heartBook, String logFile)
			throws IOException {
		super(uid, ranking, defaultVote, address, heartAddress, addressBook, heartBook,
				logFile);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		String s = super.toString();
		return s + "CALENDAR!";
	}

	@Override
	public void abort() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vote castVote(CalendarRequest r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub
		
	}
	
	

}
