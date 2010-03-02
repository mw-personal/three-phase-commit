package transactionProtocol;

public enum Vote {
	YES, NO;
		
	public Vote combine(Vote v) {
		if (this == NO) {
			return NO;
		} else {
			return v;
		}
	}
	
	public Vote combine(boolean b) {
		if (this == NO) {
			return NO;
		} else {
			return valueOf(b);
		}
	}
	
	public static Vote valueOf(boolean b) {
		return (b) ? YES : NO;
	}
}
