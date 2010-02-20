package loader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;

import transactionProtocol.Participant;
import transactionProtocol.Request;

public class ParticipantLauncher<P extends Participant<? extends Request>> {
	
	private List<P> peeps;
	private ExecutorService executorService;
	
	public ParticipantLauncher(Class<P> type, String configFile) {
		this(type, new File(configFile));
	}
	
	public ParticipantLauncher(Class<P> type, File configFile) {
		try {
			this.peeps = ParticipantConfiguration.<P>
				readParticipantConfiguration(type, configFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.executorService = Executors.newCachedThreadPool();
	}
	
	public void start() {
		for (final P p : this.peeps) {
			this.executorService.submit(new Runnable() {
				public void run() {
					System.out.println("Thread " + Thread.currentThread().getId() + 
							":\n" + p + "\n====================");
				}
			});
		}
	}
	
	public void stop() {
		this.executorService.shutdownNow();
	}

}
