package transactionProtocol;

import java.util.Comparator;

public class ParticipantComparator<R extends Request, P extends Participant<R>> implements Comparator<P> {

	public int compare(P o1, P o2) {
		
		if(o1 == null && o2 == null){
			return 0;
		} else if(o1 == null && o2 != null){
			return -1;
		} else if(o1 != null && o2 == null){
			return 1;
		}
				
		if(o1.getRanking() == o2.getRanking()){
			return 0;
		} else if(o1.getRanking() > o2.getRanking()){
			return 1;
		} else if(o1.getRanking() < o2.getRanking()){
			return -1;
		}
		
		return 0;
	}

}
