package ch.ethz.fimeier;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger; 


public class MyMiddleware{
	/*
	 * Logger stuff
	 */
	//static final Logger logger = Logger.getLogger("fimeierasl");
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"[%1$tF %1$tT] [%4$-7s] %5$s %n");
	}
	/*
	static final Level LOGLEVEL = Level.ALL;//Level.ALL;
	static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	static final Handler handler = new ConsoleHandler();*/
	static public Level LOGLEVEL = Level.WARNING; //Level.WARNING; Level.ALL;
	static public Logger logger;// = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	static public Handler handler;// = new ConsoleHandler();

	static String myIp = null;
	static int myPort = 0;
	static List<String> mcAddresses = null;
	static int nMemcachedServers = 0;
	static int numThreadsPTP = -1;
	static boolean readSharded = false;

	static private ServerSocket serverSocket;
	static final private int backLogServerSocket = 1024;
	//static final private LinkedBlockingQueue<QueueMetaData> memtierConnectionsQueue = new LinkedBlockingQueue<QueueMetaData>();

	static final private ConcurrentLinkedQueue<QueueMetaData> memtierConnectionsQueue = new ConcurrentLinkedQueue<QueueMetaData>();
	
	static final private BlockingQueue<QueueMetaData> memtierRequestsQueue = new LinkedBlockingQueue<QueueMetaData>();
	//static private ThreadPoolExecutor executor;

	private volatile boolean acceptConnections = true;
	static public int nTotalOpenedClientSockets = 0;

	static volatile boolean keepRunning = true;

	static volatile boolean closeEverything = false;


	/*
	 * Statistics
	 */
	/**
	 * BUCKETS = 500 * 10
	 */
	//    !!!!!!!!!!!!!!!!!!!!!!!! Achtung ant clean!!! nötig wenn dieser Wert geändert wird!!!
	static final int BUCKETS = 1000 * 10; //assuming longest command takes 1000 ms.. per millisecond 10 buckets (100us steps)
	static long tWaitBeforeMeasurements = 0; //start values set through constructor 1'500ms
	static long tTimeForMeasurements = 50000; //start values set through constructor ....
	public static final long windowSizeInMilliSeconds = 1000; //should be in the same size unit as the time measurements //TODO change this... problem provided parameters are in millisecond
	public static final long windowSizeInNanoSeconds = windowSizeInMilliSeconds*1000000;
	public static int nWindows;// = (int) Math.ceil((double) tTimeForMeasurements / (double) windowSize);
	public static int firstWinToUse = 15;
	public static int lastWinToUse = 74;

	static boolean startMeasuring = false;
	static public int nActiveWindow = 0; //created windows (depends on clients sending data)

	static MyMiddlewareStatisticsPerWindow[] overallStatisticWindows;
	static MyMiddlewareStatisticsPerWindow overallStatistic;
	
	
	//Queuing Theory: Inter Arrival Time
	static public long tClientArrivalCum = 0;
	static public long nClientArrival = 0;
	static private long lastClientArrivalTime = 0;
	static private void clientInterArrivalTime(long arrivalTime) {
		//implies 1 request
		if (lastClientArrivalTime==0) {
			lastClientArrivalTime = arrivalTime;
			return;
		}
		//nClientArrival  tClientArrivalCum
		tClientArrivalCum += (arrivalTime - lastClientArrivalTime);
		nClientArrival++;
		lastClientArrivalTime = arrivalTime;
	}; 
	

	//todo set this while starting measureing to system time
	static private long oldWindow = 0;
	static private boolean checkAndSetWindowActiveWindow() {
		//for the first measurement )1st client && startMeasuring==true
		if (oldWindow == 0 ) {
			//set the starttime for the first window
			oldWindow = System.nanoTime();
			return true;
		}
		long newWindow = System.nanoTime();
		long tWindowDurration = newWindow - oldWindow;
		//  	time in ns	 > time in ms * 10^6
		if (tWindowDurration > windowSizeInNanoSeconds) {
			/*
			 * fixing window length to be exact
			 */
			oldWindow += 1000000000; //oldWindow + 1 sec
			overallStatistic.settstopMeasuringCompare(oldWindow);
			//overallStatistic.settstopMeasuringCompare(newWindow);
			nActiveWindow++;

			//mefi84
			//oldWindow = newWindow;
			//oldWindow++; //add 1 ns


			//window change
			overallStatistic  = overallStatisticWindows[nActiveWindow];
			MyMiddleware.logger.info("Changeing nActiveWindow to "+ nActiveWindow);
			return true;
			//return false; //change this to true!!!!!!
		}


		return false;
	}

	static final private BlockingQueue<MemCachedProtocol> memCachedProtocolPerThread = new LinkedBlockingQueue<MemCachedProtocol>();

	//	static volatile AtomicInteger needWork = new AtomicInteger(0);
	//	static final boolean useThreadPool = false;
	private static Thread threads[] = null;
	private static Thread networkHelperThread = null;

	//static final int operationMode = 0; //0==BufferedReader, PrintWriter
	static final int CAPACITYBUFFER = 5000;
	//static final boolean blockingConnectionsQueue = false;


	/*
	 * needed for auto-testing
	 */
	public MyMiddleware(String _myIp, int _myPort, List<String> _mcAddresses, int _numThreadsPTP, boolean _readSharded, long _tWaitBeforeMeasurements, long _tTimeForMeasurements) {
		//this(_myIp, _myPort, _mcAddresses,_numThreadsPTP, _readSharded);
		/*
		 * logger
		 */

		MyMiddleware.logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		MyMiddleware.handler = new ConsoleHandler();
		MyMiddleware.handler.setLevel(MyMiddleware.LOGLEVEL);
		MyMiddleware.logger.addHandler(handler);
		MyMiddleware.logger.setLevel(MyMiddleware.LOGLEVEL);
		MyMiddleware.logger.setUseParentHandlers(false);

		MyMiddleware.tWaitBeforeMeasurements = _tWaitBeforeMeasurements;
		MyMiddleware.tTimeForMeasurements = _tTimeForMeasurements;
		MyMiddleware.nWindows = (int) Math.ceil((double) tTimeForMeasurements / (double) windowSizeInMilliSeconds);
		MyMiddleware.overallStatisticWindows = new MyMiddlewareStatisticsPerWindow[nWindows];
		for (int i = 0; i<nWindows; i++) {
			MyMiddleware.overallStatisticWindows[i] = new MyMiddlewareStatisticsPerWindow(i);
		}
		MyMiddleware.overallStatistic = overallStatisticWindows[0];
		MyMiddleware.logger.info("init MyMiddleware.... setting tWaitBeforeMeasurements="+tWaitBeforeMeasurements +" and tTimeForMeasurements="+tTimeForMeasurements);
		MyMiddleware.logger.warning("Using "+nWindows+" windows in this run... not really... just MEMTIERTESTTIME many... as data collecting starts with the first request!!");

		//	}

		//	public MyMiddleware(String _myIp, int _myPort, List<String> _mcAddresses, int _numThreadsPTP, boolean _readSharded) {





		MyMiddleware.logger.info("init MyMiddleware....");
		MyMiddleware.myIp = _myIp;
		MyMiddleware.myPort = _myPort;
		MyMiddleware.mcAddresses = _mcAddresses;
		MyMiddleware.nMemcachedServers = _mcAddresses.size();
		MyMiddleware.numThreadsPTP = _numThreadsPTP;
		MyMiddleware.readSharded = _readSharded;

		MyMiddleware.logger.setUseParentHandlers(false);
		MyMiddleware.logger.info("init MyMiddleware.... myIp="+myIp+ " myPort="+myPort +" mcAddresses="+mcAddresses +" numThreadsPTP="+numThreadsPTP+ " readSharded=" +readSharded);

		//needWork.set(0);
		/*
		 * adding ShutdownHook to terminate Middleware
		 * TODO wir das sauber beendet?????
		 * was wird eigentlich alles blockiert.... wenn hauptthread dann ja wahrscheinlich die ganze logik...
		 * vermute ich muss alles andere in einem neuen thread laufen lassen
		 * 
		 * was ist mit run()...... network queue???
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				/*
				 * logger
				 */
				MyMiddleware.handler.setLevel(MyMiddleware.LOGLEVEL);
				//MyMiddleware.logger.addHandler(handler);
				MyMiddleware.logger.setLevel(MyMiddleware.LOGLEVEL);

				//MyMiddleware.logger.setUseParentHandlers(false);

				MyMiddleware.logger.info("ShutdownHook activated.... setting flags!!!");
				keepRunning = false;
				acceptConnections = false;

				//now should run() terminate big while..

				try {
					while(!closeEverything) {
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MyMiddleware.logger.info("End of ShutdownHook... last message. ByeBye!!!");
				MyMiddleware.handler.flush();
				MyMiddleware.handler.close();

			}
		});


		/*
		 * create "normal" threads and use blocking queue
		 */
		MyMiddleware.threads = new Thread[_numThreadsPTP];
		for (int i=0; i<_numThreadsPTP; i++) {
			String threadName = "WorkerThread-"+i;
			MyMiddleware.threads[i]= new Thread(new ProcessMemtierRequestsRunnable(nWindows, windowSizeInMilliSeconds), threadName);
			MyMiddleware.threads[i].start();
			MyMiddleware.logger.info("Thread-"+i+" started... tid=" +MyMiddleware.threads[i].getId());
		}


	}

	private void shutDown() {
		/*
		 * logger
		 */
		MyMiddleware.handler.setLevel(MyMiddleware.LOGLEVEL);
		MyMiddleware.logger.addHandler(handler);
		MyMiddleware.logger.setLevel(MyMiddleware.LOGLEVEL);
		MyMiddleware.logger.setUseParentHandlers(false);


		MyMiddleware.logger.info("in shutdown...");


		/*
		 * stopping WorkerThreads
		 */
		MyMiddleware.logger.info("shutDown(): stopping WorkerThreads...");
		for (Thread thread: MyMiddleware.threads) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		MyMiddleware.logger.info("shutDown(): WorkerThreads stopped!");

		//... :-)
		//for (int i=0; i<nWindows; i++) {
		/*
		 * TODO implement checks... probably the last window doesn't contain all threads..
		 * compare for each main.run() and thread its own nActiveWindow field
		 */

		MyMiddlewareStatisticsPerWindow.collectMemcachedProtocolsPerThread(memCachedProtocolPerThread);

		long startOutputStatistic = System.currentTimeMillis();
		//wieso nicht <??....  nActiveWindow is index of active window (not #activeWindows)
		for (int i=0; i<=nActiveWindow; i++) {
			//warm up cool down... do not use first 5 windows... win0, win1,win2,win3,win4... start with win5-win64=60wins total
			if (i<firstWinToUse || lastWinToUse<i) {
				continue;
			}
			System.out.println("*******************************BEGIN: Output statistic for window "+i+"*******************************");



			MyMiddleware.logger.info("**************************************************Generating statistics.......");

			MyMiddleware.logger.info("**********MyMiddleware.overallStatistic.getStatisticsPerWindowStaticPart()**********");
			System.out.println(MyMiddleware.overallStatisticWindows[i].getStatisticsPerWindowStaticPart());

			if (MyMiddleware.overallStatisticWindows[i].iAmAGoodWindow) {
				MyMiddleware.logger.info("**********MyMiddleware.overallStatistic.getStatisticsPerWindow()**********");
				System.out.println(MyMiddleware.overallStatisticWindows[i].getStatisticsPerWindow());
			} else {
				System.out.println("I do not use the window "+i+", because it is bad...");

			}


		}

		MyMiddleware.logger.info("**********MyMiddlewareStatisticsPerWindow.finalStats()**********");

		System.out.println("**********MyMiddlewareStatisticsPerWindow.finalStats()**********");
		System.out.println(MyMiddlewareStatisticsPerWindow.finalStats(memtierConnectionsQueue));
		System.out.println("ACHTUNG sind in file finalStats");
		System.out.println("****************************************************************");


		MyMiddleware.logger.info("all statistics created!!!");

		System.out.println("***************************************************************************************************");
		System.out.println("Statistic creation for "+(nActiveWindow+1)+" windows took "+(System.currentTimeMillis()-startOutputStatistic)+"ms");
		System.out.flush();
		//no logging in this method??????

	}


	/*
	 * TODO ACHTUNG, da serverSocket.accept(); blockiert, kann dieser Thread nicht sauber beendet werden
	 * UEBERLEGE warum der join überhaupt funktioniert / Auswirkungen?
	 */
	private class NetworkHelperThread implements Runnable {

		@Override
		public void run() {

			MyMiddleware.logger.info("NetworkHelperThread: Accepting new connections...");

			/*
			 * accept new client connections and add them to the memtierConnectionsQueue
			 */
			try {
				while(acceptConnections) {

					Socket clientSocket = serverSocket.accept();
					QueueMetaData newClient = new QueueMetaData(clientSocket);
					MyMiddleware.logger.info("NetworkHelperThread: Accepted new client connection. "+newClient.printNetworkInfos());
					nTotalOpenedClientSockets++;
					memtierConnectionsQueue.add(newClient);
				}
				MyMiddleware.logger.info("NetworkHelperThread stopping because of acceptConnections=false...");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				MyMiddleware.logger.info("NetworkHelperThread interrupted...");
				System.out.flush();
			}


			/*
			 * TODO close all connections... or not...
			 * code will never reach this point!!!!!
			 */
			MyMiddleware.logger.info("NetworkHelperThread: Shutting down....");
		}


	}

	public void run() {
		/*MyMiddleware.handler.setLevel(MyMiddleware.LOGLEVEL);
		MyMiddleware.logger.addHandler(handler);
		MyMiddleware.logger.setLevel(MyMiddleware.LOGLEVEL);
		MyMiddleware.logger.setUseParentHandlers(false);

		MyMiddleware.logger.info("running MyMiddleware....");*/

		/*
		 * Start ServerSocket and create Queues
		 */
		try {

			serverSocket = new ServerSocket(MyMiddleware.myPort, MyMiddleware.backLogServerSocket);
		} catch (IOException e) {
			MyMiddleware.logger.info("ERROR: Exception caught when trying to listen on port "
					+ myPort + " or listening for a connection");
			MyMiddleware.logger.info(e.getMessage());
		}

		/*
		 * start NetworkHelperThread()
		 */
		MyMiddleware.logger.info("starting NetworkHelperThread()...");

		//NetworkHelperThread ht = new NetworkHelperThread(serverSocket, memtierConnectionsQueue);
		NetworkHelperThread ht = new NetworkHelperThread();
		MyMiddleware.networkHelperThread = (new Thread(ht));
		MyMiddleware.networkHelperThread.start();
		MyMiddleware.logger.info("NetworkHelperThread() started...");



		Thread threadStartMeasuring = new Thread() {
			public void run() {
				try {
					Thread.sleep(tWaitBeforeMeasurements);
					MyMiddleware.logger.info("Start collecting mesurements...");
					MyMiddlewareStatisticsPerWindow.setTstartMeasuring();// = System.currentTimeMillis();
					MyMiddleware.startMeasuring = true;
					Thread.sleep(tTimeForMeasurements);
					MyMiddlewareStatisticsPerWindow.setTstopMeasuring();// = System.currentTimeMillis();
					MyMiddleware.startMeasuring = false;
					MyMiddleware.logger.info("Stopped collecting mesurements!");


					//Debuggin output
					//Thread.sleep(1000);
					//stats = MyMiddleware.overallStatistic.getMiddlewareStatistics();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		MyMiddleware.logger.info("Starting Measuring-Start-Thread countdown....");
		threadStartMeasuring.start();
		MyMiddleware.logger.info("Measuring-Start-Thread started....");


		MyMiddleware.logger.info("***********************Start big while**************************");
		boolean firstClientWithData = true;
		int loadBalancerActiveMCServer = 0;
		/*
		 * ShutdownHook will set keepRunning to false
		 */
		while (keepRunning) {

			QueueMetaData activeClient = null;
			activeClient = memtierConnectionsQueue.poll();

			if (activeClient == null) {
				//MyMiddleware.logger.info("activeClient was null....");
				//MyMiddleware.logger.info("memtierConnectionsQueue.size()="+memtierConnectionsQueue.size());
				continue;
			}

			if (activeClient.readyLikeFunction()) {
				//round-robin load balancer
				activeClient.prefferedMemcachedServer = loadBalancerActiveMCServer;
				loadBalancerActiveMCServer++;
				if (loadBalancerActiveMCServer>=nMemcachedServers) {
					loadBalancerActiveMCServer = 0;
				}


				if(startMeasuring) { 

					/*
					 * Window-switching!!!!!!!!!!!!
					 */

					/*
					 * Instrumentation 
					 * 
					 * nCommandsTotal++; used for the total number of commands received by the middleware (not correctly counting multigets, not used for cache miss ratio or throughput)
					 * getStatistics = true; forces the WorkerThread to measure
					 * tClientAddedToQueue; used to calculate avgQueueWaitingTime, service time, ...
					 *
					 * the call to checkAndSetWindowActiveWindow probably changes the window
					 */
					boolean startingNewWindow = checkAndSetWindowActiveWindow();
					if (startingNewWindow) {
						firstClientWithData = true;
					}
					if (firstClientWithData) {
						overallStatistic.settstartMeasuringCompare(oldWindow);
						firstClientWithData = false;
					}

					overallStatistic.nCommandsAddedToQueue++;
					activeClient.getStatistics = true;
					activeClient.windowNumber = nActiveWindow;


					long temp = System.nanoTime(); 
					activeClient.tClientAddedToQueue = temp;// / 1000000;
					
					//Queuing Theory: inter arrival rate
					clientInterArrivalTime(temp);	
					

					/*
					 * also set during in window change
					 */
					overallStatistic.settstopMeasuringCompare(temp);

				} else {
					activeClient.getStatistics = false;
				}

				/*
				 * ADD client to QUEUE
				 */
				memtierRequestsQueue.add(activeClient);
			} else {
				//if the client is inactive (store it back in the queue)
				memtierConnectionsQueue.add(activeClient);
			}

		}
		/*
		 * ShutdownHook will set keepRunning to false and by that escaping the above loop
		 * At this point of the code... cleaning up etc.....
		 */

		MyMiddleware.logger.info("***********************!!!End of big while... calling shutDown()-Method**************************");

		shutDown();

		MyMiddleware.logger.info("shutDown method executed.... signaling hook to close everything....");
		MyMiddleware.closeEverything = true;

		MyMiddleware.logger.info("###################################### Middleware closing....!!!!!!!!!!!!!!!!!!!!!!!");
	}


	/**
	 * WorkerThreads
	 */
	private class ProcessMemtierRequestsRunnable implements Runnable {
		int nWindows = 0;
		long windowSize = 0;

		ProcessMemtierRequestsRunnable( final int _nWindows, final long _windowSize){
			nWindows = _nWindows;
			windowSize = _windowSize;			
		}

		@Override
		public void run() {


			String threadName = Thread.currentThread().getName();

			if (nWindows==0 || windowSize==0) {
				MyMiddleware.logger.severe(threadName+": nWindows="+nWindows+" windowSize="+windowSize);
			}
			MemCachedProtocol protocolPerThread = new MemCachedProtocol(mcAddresses, threadName, nWindows, windowSize);

			//used to collect all MemCachedProtocols for the final statistics
			memCachedProtocolPerThread.add(protocolPerThread);

			try {
				while(keepRunning) {
					//wait for the next client request (blocking)
					QueueMetaData activeClient = memtierRequestsQueue.take();
					//needWork.decrementAndGet();
					long currentTimeInNansoseconds = System.nanoTime();

					/*
					 * reset the protocol for each new command, probably changes the window
					 */
					protocolPerThread.resetProtocol(activeClient);

					/*
					 * Instrumentation: Start instrumentation
					 */
					if (activeClient.getStatistics) {
						protocolPerThread.getStatistics = true;
						protocolPerThread.threadStatistic.startPerThreadStatistics(activeClient, currentTimeInNansoseconds);
						protocolPerThread.threadStatistic.addQueueLength(memtierRequestsQueue.size());
						activeClient.clientThinkTime();
						//work balancing experimental result
						protocolPerThread.requestsSentToServer[activeClient.prefferedMemcachedServer]++;

					} else {
						protocolPerThread.getStatistics = false;
					}

					/**
					 * processes the client request and sends the data back before returning
					 * @returns true means close connection
					 * @returns false re-add client to memtierConnectionsQueue
					 */
					boolean closeConnection = protocolPerThread.processCommand();
					
					/* 
					 * clientThinkTime: set send back time
					 */
					activeClient.tClientResponseSent = System.nanoTime();


					if (!closeConnection) {
						//store client back to connections queue
						memtierConnectionsQueue.add(activeClient);
					} else {
						activeClient.closeClientSocket();
					}

				}
				/* leaving loop because of keepRunning=false */
				System.out.println("Working-Thread pid="+Thread.currentThread().getId()+" leaved loop through keepRunning=false");
			} catch (InterruptedException e) {
				/* leaving loop because of an interrupt... thread should be blocked on the queue */
				//System.out.println("Working-Thread pid="+Thread.currentThread().getId()+" leaved loop through interrupt");
				//e.printStackTrace();
			}

		}
	}


}
