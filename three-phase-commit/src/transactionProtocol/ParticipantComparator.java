package transactionProtocol;

import java.util.Comparator;

public class ParticipantComparator<R extends Request, P extends Participant<R>> implements Comparator {

	@SuppressWarnings("unchecked")
	public int compare(Object o1, Object o2) {
		
		if(o1 == null && o2 == null){
			return 0;
		} else if(o1 == null && o2 != null){
			return -1;
		} else if(o1 != null && o2 == null){
			return 1;
		}
		
		Participant<R> p1 = null;
		Participant<R> p2 = null;
		
		try{
			p1 = (Participant<R>)o1;
		} catch(ClassCastException e){
			return -1;
		}
		
		try{
			p2 = (Participant<R>)o2;
		} catch(ClassCastException e){
			return 1;
		}
		
		if(p1.getRanking() == p2.getRanking()){
			return 0;
		} else if(p1.getRanking() > p2.getRanking()){
			return 1;
		} else if(p1.getRanking() < p2.getRanking()){
			return -1;
		}
		
		return 0;
	}

}
