package applications;

public interface Bank {
	
	public boolean createAccount(String name, double intialAmount);

	public boolean destroyAccount(String name);
	
	public boolean requestWithdraw(String name, double amount);
	
	public boolean requestDeposit(String name, double amount);
	
}
