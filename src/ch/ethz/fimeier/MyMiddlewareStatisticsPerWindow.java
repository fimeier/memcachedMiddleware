package ch.ethz.asltest;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class MyMiddlewareStatisticsPerWindow extends MyMiddlewareStatistics {

	public MyMiddlewareStatisticsPerWindow(int _nWindowNumber) {
		super(_nWindowNumber);
	}



	/*
	 * global static stuff
	 */
	static int nGoodWindows = 0;
		
	static void addArrayAtoB(int[] arrayA, int[] arrayB) {
		if (arrayA.length!=arrayB.length) {
			MyMiddleware.logger.severe("ERROR: arrayA.length!=arrayB.length");
		}
		for (int i = 0; i<arrayA.length; i++) {
			arrayB[i] += arrayA[i];
		}
	}


	static private ArrayList<MemCachedProtocol> workingThreadMemcachedProtocols = new ArrayList<>();
	
	/**
	 * call this once
	 * This method collects all the MemCachedProtocols for the WorkerThreads
	 * and therefore all MyMidlewareStatistics
	 * It decides wheter a window contains "enough" data , by calculating its runtime (should be 99% of the definded value)
	 * => normally this is 100%, because the windows have a fixed size
	 * 
	 * @param memCachedProtocolPerThread
	 */
	static void collectMemcachedProtocolsPerThread(BlockingQueue<MemCachedProtocol> memCachedProtocolPerThread) {
		while (!memCachedProtocolPerThread.isEmpty()) {
			try {
				workingThreadMemcachedProtocols.add(memCachedProtocolPerThread.take());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		runTimeMeasuringInNanoSeconds = tstopMeasuring - tstartMeasuring;


		/*
		 * too small window < 1sec*.99
		 * should not happen while measuring => indicates a problem
		 */
		for (MyMiddlewareStatisticsPerWindow windowStats: MyMiddleware.overallStatisticWindows) {
			//do not use a window if it is too small
			windowStats.calculateRunTimeWindow();
			MyMiddleware.logger.info("Window Nr-"+windowStats.nWindowNumber+": windowStats.runTimeWindow+="+windowStats.runTimeWindow);
			//mefi84 test.... change value back to .99...
			if (windowStats.runTimeWindow>(MyMiddleware.windowSizeInNanoSeconds*.99)) {
				nGoodWindows++;
				windowStats.iAmAGoodWindow = true;
				continue;
			} else {
				MyMiddleware.logger.severe("BAD WINDOW: Window Nr-"+windowStats.nWindowNumber+": windowStats.runTimeWindow+="+windowStats.runTimeWindow);
			}

		}

	}


	//used to calculate overall runtime memtier_benchamrk
	static private long tstartMeasuring = 0; // setze diese Zeit "durch Start-Statistics-Thread" --gibt es nur 1 Mal
	static void setTstartMeasuring() {
		tstartMeasuring = System.nanoTime();
	}
	static private long tstopMeasuring = 0; // setze diese Zeit "durch Start-Statistics-Thread" stop... --gibt es nur 1 Mal
	static void setTstopMeasuring() {
		tstopMeasuring = System.nanoTime();
	}

	static private long runTimeMeasuringInNanoSeconds = 0;


	/*
	 * MyMiddleware statistics (aggregated statistics per window)
	 * this is the MyMiddlewareStatistic from main.run()
	 * 
	 */
	public boolean iAmAGoodWindow = false;
	
	private int[] histogramForCompleteWindowGet = new int[MyMiddleware.BUCKETS];
	private int[] histogramForCompleteWindowMultiGet = new int[MyMiddleware.BUCKETS];
	private int[] histogramForCompleteWindowSet = new int[MyMiddleware.BUCKETS];



	private long tStartTimeWindow = 0;// setze diese Zeit durch run pick queuer ;-debugging 
	/**
	 * call this for the first client... used to calculate the overall runtime of the window
	 */
	public void settstartMeasuringCompare(long oldWindow) {
		/*juju
		tStartTimeWindow = System.currentTimeMillis();
		 */
		tStartTimeWindow = oldWindow;
	}

	private long tEndTimeWindow = 0; // setze diese Zeit durch run pick queuer ;-debugging 
	
	/**
	 * call this for the last client in the window (or for all)
	 * Used to calculate the overall runtime of the client
	 * TODO.... change this call... at the moment each client added to queueu will also set this time
	 */
	public void settstopMeasuringCompare(long clientAddedToQueue) {
		//tstopMeasuringCompare = System.currentTimeMillis();
		tEndTimeWindow = clientAddedToQueue;
	}

	/**
	 * nano seconds to seconds
	 */
	static private double timeUnitToSec = Math.pow(10, 9); //wrong... but do not change it should be -9
	static private double timeUnitToMilSec = Math.pow(10, -6);
	private long runTimeWindow = 0;
	private void calculateRunTimeWindow() {
		MyMiddleware.logger.info("window :"+nWindowNumber + "\ttStartTimeWindow="+tStartTimeWindow+"\ttEndTimeWindow="+tEndTimeWindow);
		runTimeWindow = tEndTimeWindow - tStartTimeWindow;
		MyMiddleware.logger.info("runTimeWindow="+runTimeWindow);
	}

	/**
	 * used in main.run() to count cmd's added to queue (during a window)
	 */
	public long nCommandsAddedToQueue = 0;

	/**
	 * checkDiffCmdInQueueInWorkerThreads = nCommandsAddedToQueue - nSetCmdProcessedByWorkers - nGetProcessedByWorkers - nMultiGetProcessedByWorkers;
	 */
	long checkDiffCmdInQueueInWorkerThreads = 0;


	// cache miss ratio
	/**
	 * cumulated over all threads in this window
	 */
	protected long nMWGetKeysSent = 0;
	/**
	 * cumulated over all threads in this window
	 */
	protected long nMWGetKeysReturned = 0;

	// Throughput
	private double setOPperSecWindow = 0;
	private double getOPperSecWindow = 0;
	private double multigetOPperSecWindow = 0;
	private double totalOPperSecWindow = 0;
	private double keyGetOPperSecWindow = 0;



	// Queue length
	private double avgQueueLengthWindow = 0;
	/**
	 * cumulated over all threads in this window
	 */
	protected double tcumQueueLengthWindow = 0;
	/**
	 * cumulated over all threads in this window
	 */
	protected double tcumQueueLengthChecksWindow = 0;

	private void calculateAvgQueueLengthMiddleware() {
		avgQueueLengthWindow = tcumQueueLengthWindow / tcumQueueLengthChecksWindow;
	}

	/*
	 * aggregated data for this window
	 */
	// Middleware service-time (not used for final stats)
	double tAvgServiceTimeMiddlewareSetInNanoSeconds = 0;
	double tAvgServiceTimeMiddlewareGetInNanoSeconds = 0;
	double tAvgServiceTimeMiddlewareMultiGetInNanoSeconds = 0;
	double tAvgServiceTimeMiddlewareForAllCommandsInNanoSeconds = 0;

	// Memcached cumulated service time
	double tCumulatedTimeMemcachedServedSetInNanoSeconds = 0;
	double tCumulatedTimeMemcachedServedGetInNanoSeconds = 0;
	double tCumulatedTimeMemcachedServedMultiGetInNanoSeconds = 0;

	// Memcached service-time
	double tAvgServiceTimeMemcachedSetInNanoSeconds = 0;
	double tAvgServiceTimeMemcachedGetInNanoSeconds = 0;
	double tAvgServiceTimeMemcachedMultiGetInNanoSeconds = 0;
	double tAvgServiceTimeMemcachedForAllCommandsInNanoSeconds = 0;
	
	// WorkerThread cumulated service time
	double tCumulatedTimeWorkerThreadServedSetInNanoSeconds = 0;
	double tCumulatedTimeWorkerThreadServedGetInNanoSeconds = 0;
	double tCumulatedTimeWorkerThreadServedMultiGetInNanoSeconds = 0;

	// #-commands processed by workers
	long nSetCmdProcessedByWorkers = 0;
	long nGetProcessedByWorkers = 0;
	long nMultiGetProcessedByWorkers = 0;
	long nCommandsProcessedByWorkers = 0; //<= total processed commands

	// queue waiting times
	double tCumulatedTimeCmdsWaitedInQueueInNanoSeconds = 0;
	double tAvgWaitingTimeInQueueInNanoSeconds = 0;

	//cache miss ratio
	long KeysNotReturned = 0;
	double cacheMissRatio = 0;


	/*
	 * 
	 * 
	 * TODO... "alle" Werte oberhalb auslagern.... so dass Felder nur noch in main.run() verfügbar
	 */


	public String getStatisticsPerWindowStaticPart() {
		//calculateRunTimeWindow(); allready called in collectMemcachedProtocolsPerThread()
		StringBuilder stats = new StringBuilder();
		/*
		 * BEGIN: Middleware runtimes (Remark: If the Middleware gets interrupted, the
		 * finished time will be zero)\n");
		 */
		stats.append("\nBEGIN: Middleware runtimes (for window="+nWindowNumber+")\n");
	
		stats.append("startTimeFirstMeasurementInWindow=" + tStartTimeWindow + "\n");
		stats.append("stopTimeLastMeasurementInWindow=" + tEndTimeWindow + "\n");
		stats.append("runTimeWindow=" + runTimeWindow + " ("+runTimeWindow/1000000+"ms)\n");
		stats.append("nTotalOpenedClientSockets="+MyMiddleware.nTotalOpenedClientSockets+ "\n");
		
		stats.append("MyMiddleware.nActiveWindow="+MyMiddleware.nActiveWindow+ "\n");
		stats.append("nWindowNumber="+nWindowNumber+"\n");
		stats.append("nGoodWindows="+nGoodWindows+"\n");
		stats.append("iAmAGoodWindow="+iAmAGoodWindow+ "\n");
		stats.append("END: Middleware runtimes\n\n");

		/*
		 * END: Middleware runtimes
		 */

		return stats.toString();
	}

	/**
	 * aggregates for nWindowNumber all stats
	 * @return
	 */
	public String getStatisticsPerWindow() {
		StringBuilder output = new StringBuilder();
		MyMiddleware.logger.info("getStatisticsPerThread()......\n");

		/*
		 * BEGIN: header for per thread statistic
		 */
		output.append("BEGIN: per thread statistic (for window="+nWindowNumber+") (");
		// Queue
		output.append("threadName#nCommands#tcumulatedInQueue#tavgInQueue");

		// Total ServiceTime... add queue=>WorkingThread=>SendDataback
		output.append("#avgSetMiddleW#avgGetMiddleW#avgMultiGettMiddleW");

		// Set-memcached-service-time
		output.append("#avgMCServiceTimeSet#cumTimeSet#nSet:");
		// Get-memcached-service-time
		output.append("#avgMCServiceTimeGet#tcumTimeGet#nGet");
		// MultiGet-memcached-service-time
		output.append("#avgMCServiceTimeMultiGet#cumTimeMultiGet#nMultiGet");
		output.append(")\n");
		/*
		 * END: header for per thread statistic
		 */




		/*
		 * workerThreadsLoop
		 */
		for (MemCachedProtocol workingThread : workingThreadMemcachedProtocols) {
			/*
			 * here I choose the window
			 */
			MyMiddlewareStatistics wTStats = workingThread.threadStatisticWindows[nWindowNumber];
			
			wTStats.calculateFinalValues();

			/*
			 * BEGIN: DATA for per thread statistic
			 */
			// Queue
			output.append(wTStats.threadName + "#" + wTStats.nCommands + "#" + wTStats.tcumulatedInQueueInNanoSeconds + "#"
					+ wTStats.avgQueueWaitingTimePerThreadInNanoseconds);

			// Total Service Times
			output.append("#" + wTStats.totalAvgSetInNanoSeconds + "#" + wTStats.totalAvgGetInNanoSeconds + "#" + wTStats.totalAvgMultiGetInNanoSeconds);

			// Set-memcached-service-time
			output.append(
					"#" + wTStats.avgMCServiceTimeSetInNanoSeconds + "#" + wTStats.tcumulatedMCServiceTimeSetInNanoSeconds + "#" + wTStats.nSet);

			// Get-memcached-service-time
			output.append(
					"#" + wTStats.avgMCServiceTimeGetInNanoSeconds + "#" + wTStats.tcumulatedMCServiceTimeGetInNanoSeconds + "#" + wTStats.nGet);

			// MultiGet-memcached-service-time
			output.append("#" + wTStats.avgMCServiceTimeMultiGetInNanoSeconds + "#" + wTStats.tcumulatedMCServiceTimeMultiGetInNanoSeconds + "#"
					+ wTStats.nMultiGet);

			output.append("\n");
			/*
			 * END: DATA for per thread statistic
			 */

			// Middleware service-time (not used for final stats)
			tAvgServiceTimeMiddlewareSetInNanoSeconds += wTStats.totalAvgSetInNanoSeconds;
			tAvgServiceTimeMiddlewareGetInNanoSeconds += wTStats.totalAvgGetInNanoSeconds;
			tAvgServiceTimeMiddlewareMultiGetInNanoSeconds += wTStats.totalAvgMultiGetInNanoSeconds;

			// Queue Time in Queue
			nCommandsProcessedByWorkers += wTStats.nCommands; //used
			tCumulatedTimeCmdsWaitedInQueueInNanoSeconds += wTStats.tcumulatedInQueueInNanoSeconds; //used
			tAvgWaitingTimeInQueueInNanoSeconds += wTStats.avgQueueWaitingTimePerThreadInNanoseconds;

			// memcached time Set
			tAvgServiceTimeMemcachedSetInNanoSeconds += wTStats.avgMCServiceTimeSetInNanoSeconds;
			tCumulatedTimeMemcachedServedSetInNanoSeconds += wTStats.tcumulatedMCServiceTimeSetInNanoSeconds;
			nSetCmdProcessedByWorkers += wTStats.nSet;

			// memcached time Get
			tAvgServiceTimeMemcachedGetInNanoSeconds += wTStats.avgMCServiceTimeGetInNanoSeconds;
			tCumulatedTimeMemcachedServedGetInNanoSeconds += wTStats.tcumulatedMCServiceTimeGetInNanoSeconds;
			nGetProcessedByWorkers += wTStats.nGet;

			// memcached time Multi-Get
			tAvgServiceTimeMemcachedMultiGetInNanoSeconds += wTStats.avgMCServiceTimeMultiGetInNanoSeconds;
			tCumulatedTimeMemcachedServedMultiGetInNanoSeconds += wTStats.tcumulatedMCServiceTimeMultiGetInNanoSeconds;
			nMultiGetProcessedByWorkers += wTStats.nMultiGet;
			
			//cumulated time workerthreads served get, set or multiget
			tCumulatedTimeWorkerThreadServedSetInNanoSeconds += wTStats.tcumulatedWTServiceTimeSetInNanoSeconds;
			tCumulatedTimeWorkerThreadServedGetInNanoSeconds += wTStats.tcumulatedWTServiceTimeGetInNanoSeconds;
			tCumulatedTimeWorkerThreadServedMultiGetInNanoSeconds += wTStats.tcumulatedWTServiceTimeMultiGetInNanoSeconds;

			//collect histogram
			addArrayAtoB(wTStats.threadHistogramWindowGet, histogramForCompleteWindowGet);
			addArrayAtoB(wTStats.threadHistogramWindowMultiGet, histogramForCompleteWindowMultiGet);
			addArrayAtoB(wTStats.threadHistogramWindowSet, histogramForCompleteWindowSet);
		}
		output.append("END: per thread statistic\n\n");
		/*
		 * workerThreadsLoop ENDE
		 */
		
		tAvgWaitingTimeInQueueInNanoSeconds /= MyMiddleware.numThreadsPTP;


		tAvgServiceTimeMemcachedSetInNanoSeconds /= MyMiddleware.numThreadsPTP;
		tAvgServiceTimeMemcachedGetInNanoSeconds /= MyMiddleware.numThreadsPTP;
		tAvgServiceTimeMemcachedMultiGetInNanoSeconds /= MyMiddleware.numThreadsPTP;
		tAvgServiceTimeMemcachedForAllCommandsInNanoSeconds = (tAvgServiceTimeMemcachedSetInNanoSeconds * nSetCmdProcessedByWorkers + tAvgServiceTimeMemcachedGetInNanoSeconds * nGetProcessedByWorkers
				+ tAvgServiceTimeMemcachedMultiGetInNanoSeconds * nMultiGetProcessedByWorkers) / nCommandsProcessedByWorkers;

		tAvgServiceTimeMiddlewareSetInNanoSeconds /= MyMiddleware.numThreadsPTP;
		tAvgServiceTimeMiddlewareGetInNanoSeconds /= MyMiddleware.numThreadsPTP;
		tAvgServiceTimeMiddlewareMultiGetInNanoSeconds /= MyMiddleware.numThreadsPTP;
		tAvgServiceTimeMiddlewareForAllCommandsInNanoSeconds = (tAvgServiceTimeMiddlewareSetInNanoSeconds * nSetCmdProcessedByWorkers + tAvgServiceTimeMiddlewareGetInNanoSeconds * nGetProcessedByWorkers
				+ tAvgServiceTimeMiddlewareMultiGetInNanoSeconds * nMultiGetProcessedByWorkers) / nCommandsProcessedByWorkers;

		// cache miss ratio
		KeysNotReturned = nMWGetKeysSent - nMWGetKeysReturned;
		cacheMissRatio = (double) KeysNotReturned / (double) nMWGetKeysSent;

		/*
		 * Throughput
		 */
		/*juju*/
		setOPperSecWindow = (double) nSetCmdProcessedByWorkers / (double) runTimeWindow * timeUnitToSec;
		getOPperSecWindow = (double) nGetProcessedByWorkers / (double) runTimeWindow * timeUnitToSec;
		multigetOPperSecWindow = (double) nMultiGetProcessedByWorkers / (double) runTimeWindow * timeUnitToSec;
		totalOPperSecWindow = setOPperSecWindow + getOPperSecWindow + multigetOPperSecWindow;
		keyGetOPperSecWindow = (double) nMWGetKeysSent / (double) runTimeWindow * timeUnitToSec;

		// avg queue length
		calculateAvgQueueLengthMiddleware();


		/*
		 * BEGIN: All-Threads avg (
		 */
		output.append("BEGIN: All-Threads avg\n");
		//#-commands
		output.append("nCommandsProcessedByWorkers="+nCommandsProcessedByWorkers+"\n");
		output.append("nSetCmdProcessedByWorkers="+nSetCmdProcessedByWorkers+"\n");
		output.append("nGetProcessedByWorkers="+nGetProcessedByWorkers+"\n");
		output.append("nMultiGetProcessedByWorkers="+nMultiGetProcessedByWorkers+"\n");
		//Queue length and time
		output.append("tCumulatedTimeCmdsWaitedInQueueInNanoSeconds="+tCumulatedTimeCmdsWaitedInQueueInNanoSeconds+"\n"); //total milliseconds all commands waited in queue. Attention with interpretation!
		output.append("tAvgWaitingTimeInQueueInNanoSeconds="+tAvgWaitingTimeInQueueInNanoSeconds+"\n");
		//Middleware service-time for the commands
		output.append("tAvgServiceTimeMiddlewareSetInNanoSeconds="+tAvgServiceTimeMiddlewareSetInNanoSeconds+" ("+tAvgServiceTimeMiddlewareSetInNanoSeconds/1000000+"ms)\n");
		output.append("tAvgServiceTimeMiddlewareGetInNanoSeconds="+tAvgServiceTimeMiddlewareGetInNanoSeconds+" ("+tAvgServiceTimeMiddlewareGetInNanoSeconds/1000000+"ms)\n");
		output.append("tAvgServiceTimeMiddlewareMultiGetInNanoSeconds="+tAvgServiceTimeMiddlewareMultiGetInNanoSeconds+" ("+tAvgServiceTimeMiddlewareMultiGetInNanoSeconds/1000000+"ms)\n");
		output.append("tAvgServiceTimeMiddlewareForAllCommandsInNanoSeconds="+ tAvgServiceTimeMiddlewareForAllCommandsInNanoSeconds +" ("+tAvgServiceTimeMiddlewareForAllCommandsInNanoSeconds/1000000+"ms)\n");
		//Memcached service-time overallAvgMCST...
		output.append("tAvgServiceTimeMemcachedSetInNanoSeconds="+tAvgServiceTimeMemcachedSetInNanoSeconds+"\n");
		output.append("tAvgServiceTimeMemcachedGetInNanoSeconds="+tAvgServiceTimeMemcachedGetInNanoSeconds+"\n");
		output.append("tAvgServiceTimeMemcachedMultiGetInNanoSeconds="+tAvgServiceTimeMemcachedMultiGetInNanoSeconds+"\n");
		output.append("tAvgServiceTimeMemcachedForAllCommandsInNanoSeconds="+ tAvgServiceTimeMemcachedForAllCommandsInNanoSeconds + "\n");


		//Memcached nTotalcumTime... cumulated time the memcached spent to answere queries
		output.append("tCumulatedTimeMemcachedServedSetInNanoSeconds="+tCumulatedTimeMemcachedServedSetInNanoSeconds+"\n");
		output.append("tCumulatedTimeMemcachedServedGetInNanoSeconds="+tCumulatedTimeMemcachedServedGetInNanoSeconds+"\n");
		output.append("tCumulatedTimeMemcachedServedMultiGetInNanoSeconds="+tCumulatedTimeMemcachedServedMultiGetInNanoSeconds+"\n");




		output.append("END: All-Threads avg\n\n");
		/*
		 * END: All-Threads avg
		 */

		/*
		 * BEGIN: Check the numbers
		 */
		checkDiffCmdInQueueInWorkerThreads = nCommandsAddedToQueue - nSetCmdProcessedByWorkers - nGetProcessedByWorkers - nMultiGetProcessedByWorkers;
		output.append("BEGIN: Check the numbers\n");
		output.append("nCommandsAddedToQueue=" + nCommandsAddedToQueue + "\n");
		output.append("nCommandsProcessedByWorkers=" + nCommandsProcessedByWorkers + "\n");
		output.append("Total-set-get-multiget(eq0?)=" + checkDiffCmdInQueueInWorkerThreads + "\n");
		output.append("nSetCmdProcessedByWorkers=" + nSetCmdProcessedByWorkers + "\n");
		output.append("nGetProcessedByWorkers=" + nGetProcessedByWorkers + "\n");
		output.append("nMultiGetProcessedByWorkers=" + nMultiGetProcessedByWorkers + "\n");
		output.append("END: Check the numbers\n\n");
		/*
		 * END: Check the numbers
		 */

		/*
		 * BEGIN: cacheMissRatio
		 */
		output.append("BEGIN: cacheMissRatio\n");
		output.append("cacheMissRatio=" + cacheMissRatio + "\n");
		output.append("KeysNotReturned=" + KeysNotReturned + "\n");
		output.append("nMWGetKeysSent=" + nMWGetKeysSent + "\n");
		output.append("END: cacheMissRatio\n\n");
		/*
		 * END: cacheMissRatio
		 */

		/*
		 * BEGIN: Throughput and (again) avgTOALMiddleware latency
		 */
		output.append("BEGIN: Throughput (totalOPperSec is just the addition of the individual cmd-types)\n");
		output.append("setOPperSec=" + setOPperSecWindow+"\n");
		output.append("getOPperSec=" + getOPperSecWindow+"\n");
		output.append("multigetOPperSec=" + multigetOPperSecWindow+"\n");
		output.append("totalOPperSec=" + totalOPperSecWindow+"\n");
		output.append("keyGetOPperSec=" + keyGetOPperSecWindow+"\n");
		output.append("END: Throughput\n\n");
		/*
		 * END: Throughput and (again) avgTOALMiddleware latency
		 */

		/*
		 * BEGIN: queue length
		 */
		output.append("BEGIN: queue length (tCumulatedTimeCmdsWaitedInQueue: total milliseconds all commands waited in queue. Attention with interpretation!)\n");
		output.append("avgQueueLengthWindow=" + avgQueueLengthWindow + "\n");
		output.append("tCumulatedTimeCmdsWaitedInQueueInNanoSeconds="+tCumulatedTimeCmdsWaitedInQueueInNanoSeconds + "\n");
		output.append("tAvgWaitingTimeInQueueInNanoSeconds="+tAvgWaitingTimeInQueueInNanoSeconds + "\n");
		output.append("END: queue length\n");
		/*
		 * END: queue length
		 */

		MyMiddleware.logger.info("REMARK: avgXXXMiddleW is the combined time of avgInQueue + Time in WorkerThread...\ntherefore if an OP-type (ex multiGet) is not executed, there is still a value, the avgInQueueu value");

		return output.toString();
	}


	/*
	 * FINAL AGGREGATED OUTPUT OVER ALL WINDOWS
	 */
	static public String finalStats() {
		StringBuilder output = new StringBuilder();
		
		StringBuilder finalStatsFile = new StringBuilder();
		StringBuilder histogramFile = new StringBuilder();
		
		
		long runTimeAllWindows = 0; //ok
		
		//cache miss ratio
		double cacheMissRatioFinal = 0.0;
		double keysSentToServerFinal = 0.0;
		double keysReturnedFromServerFinal = 0.0;
		double keysNotReturnedFinal = 0.0;
		
		
		double throughputSet = 0; //ok
		double throughputGet = 0; //ok
		double throughputMultiGet = 0; //ok
		double throughputAllCommands = 0; //ok
		double throughputGetAllKeys = 0; //ok
		
		double avgQueueLength = 0; //ok
		double avgQueueLengthNumberOfChecks = 0; //ok
		
		//avg time in queue fehlt noch in statistik
		
		// Queue Time in Queue
		double avgQueueWaitingTime = 0.0;
		
		//memcached ResponseTime
		double avgMemcachedResponseTimeGet = 0.0;
		double avgMemcachedResponseTimeMultiGet = 0.0;
		double avgMemcachedResponseTimeSet = 0.0;
		
		double numberSetCommands = 0.0;
		double numberGetCommands = 0.0;
		double numberMultiGetCommands = 0.0;
		//compare nCommandsProcessedByWorkersOverRun (the sum


		//workerthread ResponseTime
		double avgWorkerThreadResponseTimeGet = 0.0;
		double avgWorkerThreadResponseTimeMultiGet = 0.0;
		double avgWorkerThreadResponseTimeSet = 0.0;
		
		double avgMiddlewareResponseTimeGet = 0.0;
		double avgMiddlewareResponseTimeMultiGet = 0.0;
		double avgMiddlewareResponseTimeSet = 0.0;

		
		long nCommandsAddedToQueueOverRun = 0;
		long nCommandsProcessedByWorkersOverRun = 0;
		long checkDiffCmdInQueueInWorkerThreadsOverRun = 0;
		
		int[] threadHistogramOverRunGet = new int[MyMiddleware.BUCKETS];
		int[] threadHistogramOverRunMultiGet = new int[MyMiddleware.BUCKETS];
		int[] threadHistogramOverRunSet = new int[MyMiddleware.BUCKETS];


		int nWindowsUsedForAggregation = 0;
		
		for (int i = 0; i<nGoodWindows; i++) {
			//warm up cool down... do not use first 2 windows... win0, win1... start with win2-win61=60wins total
			if (i<2 || 61<i) {
				continue;
			}
			nWindowsUsedForAggregation++;
			
			MyMiddlewareStatisticsPerWindow windowStats = MyMiddleware.overallStatisticWindows[i];		
			runTimeAllWindows += windowStats.runTimeWindow; //ok
			
			//cache miss ratio
			keysSentToServerFinal += windowStats.nMWGetKeysSent;
			keysReturnedFromServerFinal += windowStats.nMWGetKeysReturned;
						
			throughputSet += windowStats.setOPperSecWindow; 
			//throughputSet += (double) windowStats.nSetCmdProcessedByWorkers;
			
			throughputGet += windowStats.getOPperSecWindow; 
			throughputMultiGet += windowStats.multigetOPperSecWindow; 
			throughputAllCommands += windowStats.totalOPperSecWindow; 
			throughputGetAllKeys += windowStats.keyGetOPperSecWindow; //neu und hinweis dass die primär throughput sein soll
			
			avgQueueWaitingTime +=	windowStats.tCumulatedTimeCmdsWaitedInQueueInNanoSeconds; 
			
			//memcachedResponseTime
			avgMemcachedResponseTimeGet += windowStats.tCumulatedTimeMemcachedServedGetInNanoSeconds;
			avgMemcachedResponseTimeMultiGet += windowStats.tCumulatedTimeMemcachedServedMultiGetInNanoSeconds;
			avgMemcachedResponseTimeSet += windowStats.tCumulatedTimeMemcachedServedSetInNanoSeconds;
			
			//workerThreadResponseTime
			avgWorkerThreadResponseTimeGet += windowStats.tCumulatedTimeWorkerThreadServedGetInNanoSeconds;
			avgWorkerThreadResponseTimeMultiGet += windowStats.tCumulatedTimeWorkerThreadServedMultiGetInNanoSeconds;
			avgWorkerThreadResponseTimeSet += windowStats.tCumulatedTimeWorkerThreadServedSetInNanoSeconds;
			
			numberSetCommands += windowStats.nSetCmdProcessedByWorkers;
			numberGetCommands += windowStats.nGetProcessedByWorkers;
			numberMultiGetCommands += windowStats.nMultiGetProcessedByWorkers;

			
			avgQueueLengthNumberOfChecks += windowStats.tcumQueueLengthChecksWindow;
			avgQueueLength += windowStats.tcumQueueLengthWindow;

				

			nCommandsAddedToQueueOverRun += windowStats.nCommandsAddedToQueue;
			nCommandsProcessedByWorkersOverRun += windowStats.nCommandsProcessedByWorkers;
			checkDiffCmdInQueueInWorkerThreadsOverRun +=windowStats.checkDiffCmdInQueueInWorkerThreads;
			
			//threadHistogramOverRun
			//collect histogram
			addArrayAtoB(windowStats.histogramForCompleteWindowGet, threadHistogramOverRunGet);
			addArrayAtoB(windowStats.histogramForCompleteWindowMultiGet, threadHistogramOverRunMultiGet);
			addArrayAtoB(windowStats.histogramForCompleteWindowSet, threadHistogramOverRunSet);
			
		}
		
		//cache miss ratio
		keysNotReturnedFinal = keysSentToServerFinal - keysReturnedFromServerFinal;
		cacheMissRatioFinal = (double) keysNotReturnedFinal / (double) keysSentToServerFinal;

		throughputSet /= nWindowsUsedForAggregation;	
		throughputGet /= nWindowsUsedForAggregation;
		throughputMultiGet /= nWindowsUsedForAggregation;
		throughputAllCommands /= nWindowsUsedForAggregation;
		throughputGetAllKeys /= nWindowsUsedForAggregation;
		
		avgQueueWaitingTime = avgQueueWaitingTime / nCommandsProcessedByWorkersOverRun*timeUnitToMilSec;
		
		//memcached ResponseTime
		if(numberGetCommands!=0)
			avgMemcachedResponseTimeGet = avgMemcachedResponseTimeGet / numberGetCommands *timeUnitToMilSec;
		if(numberMultiGetCommands !=0)
			avgMemcachedResponseTimeMultiGet = avgMemcachedResponseTimeMultiGet / numberMultiGetCommands *timeUnitToMilSec;
		if(numberSetCommands !=0)
			avgMemcachedResponseTimeSet = avgMemcachedResponseTimeSet / numberSetCommands *timeUnitToMilSec;
		
		//workerthreads ResponseTime
		if(numberGetCommands!=0)
			avgWorkerThreadResponseTimeGet = avgWorkerThreadResponseTimeGet / numberGetCommands *timeUnitToMilSec;
		if(numberMultiGetCommands !=0)
			avgWorkerThreadResponseTimeMultiGet = avgWorkerThreadResponseTimeMultiGet / numberMultiGetCommands *timeUnitToMilSec;
		if(numberSetCommands !=0)
			avgWorkerThreadResponseTimeSet = avgWorkerThreadResponseTimeSet / numberSetCommands *timeUnitToMilSec;
		
		if(avgWorkerThreadResponseTimeGet!=0)
			avgMiddlewareResponseTimeGet = avgQueueWaitingTime + avgWorkerThreadResponseTimeGet;
		if(avgWorkerThreadResponseTimeMultiGet!=0)
			avgMiddlewareResponseTimeMultiGet = avgQueueWaitingTime + avgWorkerThreadResponseTimeMultiGet;
		if(avgWorkerThreadResponseTimeSet!=0)
			avgMiddlewareResponseTimeSet = avgQueueWaitingTime + avgWorkerThreadResponseTimeSet;
		
		avgQueueLength /= avgQueueLengthNumberOfChecks;

		
			
		
		finalStatsFile.append("BEGIN: FINAL STATS\n");
		finalStatsFile.append("nWindows="+MyMiddleware.nWindows+"\n");
		finalStatsFile.append("nGoodWindows="+nGoodWindows+"\n");
		finalStatsFile.append("nWindowsUsedForAggregation="+nWindowsUsedForAggregation+"\n");

		finalStatsFile.append("runTimeOverall="+runTimeMeasuringInNanoSeconds+" (wrong value if threadstopMeasuring didn't had the chance to stop everything... interrupt?)\n");
		finalStatsFile.append("runTimeWindowCumulatedOverRun="+runTimeAllWindows+" ("+((double) runTimeAllWindows)/1000000+"ms)\n");
		
		finalStatsFile.append("cacheMissRatioFinal="+cacheMissRatioFinal+"\n");
		finalStatsFile.append("keysSentToServerFinal="+keysSentToServerFinal+"\n");
		finalStatsFile.append("keysReturnedFromServerFinal="+keysReturnedFromServerFinal+"\n");
		finalStatsFile.append("keysNotReturnedFinal="+keysNotReturnedFinal+"\n");
		
		finalStatsFile.append("numberSetCommands="+numberSetCommands+"\n");
		finalStatsFile.append("numberGetCommands="+numberGetCommands+"\n");
		finalStatsFile.append("numberMultiGetCommands="+numberMultiGetCommands+"\n");

		
		finalStatsFile.append("throughputSet=" + throughputSet+"\n");

		
		finalStatsFile.append("throughputGet=" + throughputGet+"\n");
		finalStatsFile.append("throughputMultiGet=" + throughputMultiGet+"\n");
		finalStatsFile.append("throughputAllCommands=" + throughputAllCommands+"\n");
		finalStatsFile.append("throughputGetAllKeys=" + throughputGetAllKeys + "\n");
		
		finalStatsFile.append("avgQueueWaitingTime="+avgQueueWaitingTime+"\n");
		finalStatsFile.append("avgQueueLength=" + avgQueueLength+"\n");
		
		finalStatsFile.append("avgMemcachedResponseTimeSet="+avgMemcachedResponseTimeSet+"\n");
		finalStatsFile.append("avgMemcachedResponseTimeGet="+avgMemcachedResponseTimeGet+"\n");
		finalStatsFile.append("avgMemcachedResponseTimeMultiGet="+avgMemcachedResponseTimeMultiGet+"\n");
		
		finalStatsFile.append("avgWorkerThreadResponseTimeSet="+avgWorkerThreadResponseTimeSet+"\n");
		finalStatsFile.append("avgWorkerThreadResponseTimeGet="+avgWorkerThreadResponseTimeGet+"\n");
		finalStatsFile.append("avgWorkerThreadResponseTimeMultiGet="+avgWorkerThreadResponseTimeMultiGet+"\n");

		finalStatsFile.append("avgMiddlewareResponseTimeSet="+ avgMiddlewareResponseTimeSet+"\n");
		finalStatsFile.append("avgMiddlewareResponseTimeGet="+ avgMiddlewareResponseTimeGet+"\n");
		finalStatsFile.append("avgMiddlewareResponseTimeMultiGet="+ avgMiddlewareResponseTimeMultiGet+"\n");

	

		finalStatsFile.append("nCommandsAddedToQueueOverRun=" + nCommandsAddedToQueueOverRun + "\n");
		finalStatsFile.append("nCommandsProcessedByWorkersOverRun=" + nCommandsProcessedByWorkersOverRun + "\n");
		finalStatsFile.append("Total-set-get-multiget=" + checkDiffCmdInQueueInWorkerThreadsOverRun + "\n");
		
		finalStatsFile.append("END: FINAL STATS");
				
		/*
		 * TODO: save histogram directly into a file
		 */
		
		
		histogramFile.append("BEGIN: ACHUNG HIER WERDEN ALLENFALLS ALLE WINDOWS ausgewertet... ein Fehler!!! Histogram (response time <;#-Get;#-MultiGet;#-Set)\n");

		
		long check = nCommandsProcessedByWorkersOverRun;
		for(int i = 0; i< MyMiddleware.BUCKETS; i++) {
			//habe das wie oben hinzugefügt... sollte jetzt stimmen!!
			//warm up cool down... do not use first 2 windows... win0, win1... start with win2-win61=60wins total
			if (i<2 || 61<i) {
				continue;
			}
			int nGet = threadHistogramOverRunGet[i];
			int nMultiGet = threadHistogramOverRunMultiGet[i];
			int nSet = threadHistogramOverRunSet[i];
			check -= (nGet+nMultiGet+nSet);
			histogramFile.append((i+1)*100+";"+nGet+";"+nMultiGet+";"+nSet+" \n");
		}
		double checkInPro = (double) check / (double) nCommandsProcessedByWorkersOverRun;
		histogramFile.append("histogramOutOfScopeNumber="+check+"\n");
		histogramFile.append("histogramOutOfScopeRatio="+checkInPro+"\n");
		histogramFile.append("END: Histogram (#-out of scoope requests="+check+", "+checkInPro+"%, buckets for [0ms, "+MyMiddleware.BUCKETS/10+"ms])\n");

		//write stats and histogram
		String fileNameFinalStats = "/home/fimeier/automato/finalStats";
		String fileNameHistogram = "/home/fimeier/automato/histogram";
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileNameFinalStats, "UTF-8");
			writer.print(finalStatsFile);
			writer.flush();
			writer.close();
			
			writer = new PrintWriter(fileNameHistogram, "UTF-8");
			writer.print(histogramFile);
			writer.flush();
			writer.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return output.toString();

	}



}
