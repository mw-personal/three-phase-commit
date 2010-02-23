package transactionProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class HeartMonitor {
	
	private String uid;
	private InetSocketAddress address;
	private Map<String, InetSocketAddress> addressBook;
	private Socket outbox;
	private ServerSocket inbox;
	private boolean started;
	
	// for sending heartbeats
	private Timer heartbeatTimer;
	private long heartbeatPeriod;
	private Thread heartbeatMonitor;
	
	// for keeping track of participants
	private Map<String, Long> lastPulse;
	
	public HeartMonitor(String uid, InetSocketAddress address,
			Map<String, InetSocketAddress> addressBook, long period) throws IOException {
		this.uid = uid;
		this.address = address;
		this.addressBook = addressBook;
		this.inbox = new ServerSocket(this.address.getPort());
		this.started = false;
		
		this.lastPulse = new HashMap<String, Long>();
		for (String s : this.addressBook.keySet()) {
			this.lastPulse.put(s, -1L);
		}
		
		this.heartbeatPeriod = period;
		this.heartbeatTimer = new Timer();
		this.heartbeatMonitor = new Thread(new Runnable() {
			public void run() {
				try {
					listen();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	public void start() {
		if (started || this.heartbeatTimer == null || this.heartbeatMonitor == null)
			throw new IllegalStateException();
		// start sending out pulses
		this.heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					sendSignal();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, this.heartbeatPeriod, this.heartbeatPeriod);
		
		// start listening for pulses
		this.heartbeatMonitor.start();
		this.started = true;
	}
	
	public void stop() throws IOException {
		if (!started || this.heartbeatTimer == null || this.heartbeatMonitor == null)
			throw new IllegalStateException();
		this.heartbeatTimer.cancel();
		this.heartbeatMonitor.interrupt();
		this.inbox.close();
		this.heartbeatMonitor = null;
		this.heartbeatTimer = null;
	}
	
	public List<String> getFailedParticipants(long cutoftime) {
		List<String> failedParticiapnts = new ArrayList<String>();
		for (Map.Entry<String, Long> entry : this.lastPulse.entrySet()) {
			if (entry.getValue() < cutoftime) {
				failedParticiapnts.add(entry.getKey());
			}
		}
		
		return failedParticiapnts;
	}
	
	private void sendSignal() throws IOException {
		for(InetSocketAddress isa : this.addressBook.values()) {
			System.out.println(uid + " notifed " + isa + " that he's alive!");
			this.outbox = new Socket(isa.getAddress(), isa.getPort());
			PrintWriter osw = new PrintWriter(this.outbox.getOutputStream());
			osw.write(this.uid);
			osw.close();
			this.outbox.close();
		}
	}
	
	private void listen() throws IOException {
		while(true) {
			Socket client = this.inbox.accept();
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String s = br.readLine();
			if (s != null && this.lastPulse.containsKey(s)) {
				System.out.println(uid + " received heartbeat from " + s);
				this.lastPulse.put(s, System.currentTimeMillis());
			}
			br.close();
			client.close();
		}
	}
		
	public static void main(String[] args) throws IOException {
		Map<String, InetSocketAddress> ab = new
			HashMap<String, InetSocketAddress>();
		ab.put("jose", new InetSocketAddress(InetAddress.getLocalHost(), 8081));
		ab.put("jose1", new InetSocketAddress(InetAddress.getLocalHost(), 8082));
		ab.put("jose2", new InetSocketAddress(InetAddress.getLocalHost(), 8083));
		ab.put("jose3", new InetSocketAddress(InetAddress.getLocalHost(), 8084));
		ab.put("jose4", new InetSocketAddress(InetAddress.getLocalHost(), 8085));

		long period = 1000;
		HeartMonitor hm = new HeartMonitor("jose", ab.get("jose"), ab, period);
		HeartMonitor hm1 = new HeartMonitor("jose1", ab.get("jose1"), ab, period);
		HeartMonitor hm2 = new HeartMonitor("jose2", ab.get("jose2"), ab, period);
		HeartMonitor hm3 = new HeartMonitor("jose3", ab.get("jose3"), ab, period);
		HeartMonitor hm4 = new HeartMonitor("jose4", ab.get("jose4"), ab, period);
		
		hm.start();
		hm1.start();
		hm2.start();
		hm3.start();
		hm4.start();
	}
	
//	private void receiveSignals() {
//		while(true) {
//			Socket client = this.inbox.accept();
//			
//		}
//	}
}
