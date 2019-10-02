package ch.ethz.asltest;

public class MyMiddlewareStatistics {
	
	public MyMiddlewareStatistics(int _nWindowNumber) {
		this.nWindowNumber = _nWindowNumber;
		//this.windowDataStore = new ThreadStatisticWindowData[nWindows];
	}
	/**
	 * this stuff here is for each window in main.run().... it should contain the aggregated data from the windows...
	 * basically the stuff I callculate in my two output functions
	 */
	
	/**
	 * the window number the data was collected in
	 */
	public int nWindowNumber = 0;
	
	
	
	
	
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * per thread statistics
	 */
	String threadName = "";
	QueueMetaData activeClient = null;
	
	/*
	 * Histogram stuff
	 */
	protected int[] threadHistogramWindowSet = new int[MyMiddleware.BUCKETS];
	private void addToThreadHistogramBucketSet(long responseTimeInNanoSeconds) {
		long responseTimeInMuSeconds = responseTimeInNanoSeconds / 1000;
		int bucketID = (int) (responseTimeInMuSeconds / 100); //assuming round down integer style
		//MyMiddleware.logger.info("responseTimeInMuSeconds="+responseTimeInMuSeconds + " using threadHistogramWindow["+bucketID+"]");
		if (bucketID<MyMiddleware.BUCKETS) {
			threadHistogramWindowSet[bucketID]++;
		}
	}
	protected int[] threadHistogramWindowGet = new int[MyMiddleware.BUCKETS];
	private void addToThreadHistogramBucketGet(long responseTimeInNanoSeconds) {
		long responseTimeInMuSeconds = responseTimeInNanoSeconds / 1000;
		int bucketID = (int) (responseTimeInMuSeconds / 100); //assuming round down integer style
		//MyMiddleware.logger.info("responseTimeInMuSeconds="+responseTimeInMuSeconds + " using threadHistogramWindow["+bucketID+"]");
		if (bucketID<MyMiddleware.BUCKETS) {
			threadHistogramWindowGet[bucketID]++;	
		}
	}
	protected int[] threadHistogramWindowMultiGet = new int[MyMiddleware.BUCKETS];
	private void addToThreadHistogramBucketMultiGet(long responseTimeInNanoSeconds) {
		long responseTimeInMuSeconds = responseTimeInNanoSeconds / 1000;
		int bucketID = (int) (responseTimeInMuSeconds / 100); //assuming round down integer style
		//MyMiddleware.logger.info("responseTimeInMuSeconds="+responseTimeInMuSeconds + " using threadHistogramWindow["+bucketID+"]");
		if (bucketID<MyMiddleware.BUCKETS) {
			threadHistogramWindowMultiGet[bucketID]++;
		}
	}
	
	
	/*
	 * cache miss ratio
	 */
	private long nGetKeys = 0;
	private long nGetKeysReturned = 0;

	public void getKeys(int nKeys) {
		nGetKeys += nKeys;
	}

	public void nGetKeysReturned(int nKeys) {
		nGetKeysReturned += nKeys;
	}

	/*
	 * used to calculate average queue length per thread
	 */
	private double avgQueueLengthPerThread = 0;
	private double tcumQueueLength = 0;
	private long tcumQueueLengthChecks = 0;

	/**
	 * call this function per thread in ProcessMemtierRequestsRunnable.run()
	 * @param length
	 */
	public void addQueueLength(int length) {
		tcumQueueLength += length;
		tcumQueueLengthChecks++;
	}

	private void calculateAvgQueueLengthPerThread() {
		avgQueueLengthPerThread = (double) tcumQueueLength / (double) tcumQueueLengthChecks;

		/*
		 * totally ugly.. needed because i introduced the windows as the last feature
		 */
		MyMiddleware.overallStatisticWindows[nWindowNumber].tcumQueueLengthWindow += tcumQueueLength;
		MyMiddleware.overallStatisticWindows[nWindowNumber].tcumQueueLengthChecksWindow += tcumQueueLengthChecks;
	}

	/**
	 * used to calculate average waiting time in the queue
	 * aggregiert pro thread in window
	 */
	protected double avgQueueWaitingTimePerThreadInNanoseconds = 0;
	/**
	 * same as #requests for this thread
	 */
	protected long nCommands = 0;
	protected long tClientAddedToQueueInNanoSeconds = 0;
	protected long tClientPickedFromQueueInNanoSeconds = 0;
	protected double tcumulatedInQueueInNanoSeconds = 0;

	/**
	 * call this method in ProcessMemtierRequestsRunnable.run() when start processing a client
	 */
	public void startPerThreadStatistics(QueueMetaData _activeClient, long currentTimeInNansoseconds) {
		tClientAddedToQueueInNanoSeconds = _activeClient.tClientAddedToQueue;
		stopAvgQueueWaitingTimeInNanoSeconds(currentTimeInNansoseconds);
		nCommands++;
		this.activeClient = _activeClient;
	}

	public void stopAvgQueueWaitingTimeInNanoSeconds(long currentTimeInNansoseconds) {
		tClientPickedFromQueueInNanoSeconds = currentTimeInNansoseconds;//System.currentTimeMillis();
		tcumulatedInQueueInNanoSeconds += tClientPickedFromQueueInNanoSeconds - tClientAddedToQueueInNanoSeconds;
	}

	private void calculateAvgQueueWaitingTime() {
		avgQueueWaitingTimePerThreadInNanoseconds = (double) tcumulatedInQueueInNanoSeconds / (double) nCommands;
	}

	/*
	 * used to calculate average memcached service time for get's
	 */
	protected double avgMCServiceTimeGetInNanoSeconds = 0;
	protected long nGet = 0;
	protected long tMCServiceTimeGetStartInNanoSeconds = 0;
	protected long tMCServiceTimeGetStopInNanoSeconds = 0;
	protected double tcumulatedMCServiceTimeGetInNanoSeconds = 0;

	public void startAvgMemServiceTimeGet() {
		nGet++;
		tMCServiceTimeGetStartInNanoSeconds = System.nanoTime();//System.currentTimeMillis();
		getKeys(1);
	}

	public void stopAvgMemServiceTimeGet(int nKeysReturned) {
		tMCServiceTimeGetStopInNanoSeconds = System.nanoTime();//System.currentTimeMillis();
		tcumulatedMCServiceTimeGetInNanoSeconds += tMCServiceTimeGetStopInNanoSeconds - tMCServiceTimeGetStartInNanoSeconds;
		// cache miss ratio
		nGetKeysReturned(nKeysReturned);
	}

	private void calculateAvgMemServiceTimeGet() {
		if (nGet != 0)
			avgMCServiceTimeGetInNanoSeconds = (double) tcumulatedMCServiceTimeGetInNanoSeconds / (double) nGet;
	}

	// Working Thread Service Time Get
	private double avgWTServiceTimeGetInNanoSeconds = 0;
	public double tcumulatedWTServiceTimeGetInNanoSeconds = 0;

	public void stopAvgWTServiceTimeGet() {
		long temp = System.nanoTime() - tClientPickedFromQueueInNanoSeconds;
		addToThreadHistogramBucketGet(temp);
		
		tcumulatedWTServiceTimeGetInNanoSeconds += temp;
	}

	private void calculateavgWTServiceTimeGet() {
		if (nGet != 0)
			avgWTServiceTimeGetInNanoSeconds = (double) tcumulatedWTServiceTimeGetInNanoSeconds / (double) nGet;
	}

	/*
	 * used to calculate average memcached service time for multiget's
	 */
	protected double avgMCServiceTimeMultiGetInNanoSeconds = 0;
	protected long nMultiGet = 0;
	protected long tMCServiceTimeMultiGetStartInNanoSeconds = 0;
	protected long tMCServiceTimeMultiGetStopInNanoSeconds = 0;
	protected double tcumulatedMCServiceTimeMultiGetInNanoSeconds = 0;

	public void startAvgMemServiceTimeMultiGet(int nKeys) {
		nMultiGet++;
		tMCServiceTimeMultiGetStartInNanoSeconds = System.nanoTime();//currentTimeMillis();
		getKeys(nKeys);
	}

	public void stopAvgMemServiceTimeMultiGet(int nKeysReturned) {
		tMCServiceTimeMultiGetStopInNanoSeconds = System.nanoTime();//currentTimeMillis();
		tcumulatedMCServiceTimeMultiGetInNanoSeconds += tMCServiceTimeMultiGetStopInNanoSeconds - tMCServiceTimeMultiGetStartInNanoSeconds;
		
		// cache miss ratio
		nGetKeysReturned(nKeysReturned);
	}

	private void calculateAvgMemServiceTimeMultiGet() {
		if (nMultiGet != 0)
			avgMCServiceTimeMultiGetInNanoSeconds = (double) tcumulatedMCServiceTimeMultiGetInNanoSeconds / (double) nMultiGet;
	}

	// WorkingThreadSTMultiGet
	private double avgWTServiceTimeMultiGetInNanoSeconds = 0;
	public double tcumulatedWTServiceTimeMultiGetInNanoSeconds = 0;

	public void stopAvgWTServiceTimeMultiGet() {
		long temp = System.nanoTime() - tClientPickedFromQueueInNanoSeconds;
		addToThreadHistogramBucketMultiGet(temp);
		tcumulatedWTServiceTimeMultiGetInNanoSeconds += temp;
	}

	private void calculateavgWTServiceTimeMultiGet() {
		if (nMultiGet != 0)
			avgWTServiceTimeMultiGetInNanoSeconds = (double) tcumulatedWTServiceTimeMultiGetInNanoSeconds / (double) nMultiGet;
	}

	/*
	 * used to calculate average memcached service time for set's
	 */
	protected double avgMCServiceTimeSetInNanoSeconds = 0;
	protected long nSet = 0;
	protected long tMCServiceTimeSetStartInNanoSeconds = 0;
	protected long tMCServiceTimeSetStopInNanoSeconds = 0;
	protected double tcumulatedMCServiceTimeSetInNanoSeconds = 0;
	

	public void startAvgMemServiceTimeSet() {
		nSet++;
		tMCServiceTimeSetStartInNanoSeconds = System.nanoTime();//currentTimeMillis();
	}

	public void stopAvgMemServiceTimeSet() {
		tMCServiceTimeSetStopInNanoSeconds = System.nanoTime();//currentTimeMillis();
		tcumulatedMCServiceTimeSetInNanoSeconds += tMCServiceTimeSetStopInNanoSeconds - tMCServiceTimeSetStartInNanoSeconds;
	}

	private void calculateAvgMemServiceTimeSet() {
		if (nSet != 0)
			avgMCServiceTimeSetInNanoSeconds = (double) tcumulatedMCServiceTimeSetInNanoSeconds / (double) nSet;
	}

	// WorkingThreadSTSet
	private double avgWTServiceTimeSetInNanoSeconds = 0;
	public double tcumulatedWTServiceTimeSetInNanoSeconds = 0;

	public void stopAvgWTServiceTimeSet() {
		long temp = System.nanoTime() - tClientPickedFromQueueInNanoSeconds;
		addToThreadHistogramBucketSet(temp);
		tcumulatedWTServiceTimeSetInNanoSeconds += temp;
	}

	private void calculateavgWTServiceTimeSet() {
		if (nSet != 0)
			avgWTServiceTimeSetInNanoSeconds = (double) tcumulatedWTServiceTimeSetInNanoSeconds / (double) nSet;
	}

	public void calculateMiddlewareFinalValues() {

	}

	/*
	 * NOT THREADSAFE.... call this method per thread to read out the "final" values
	 * also calculate here the other values
	 */
	protected void calculateFinalValues() {
		// Queue
		calculateAvgQueueWaitingTime();
		// memcached
		calculateAvgMemServiceTimeSet();
		calculateAvgMemServiceTimeGet();
		calculateAvgMemServiceTimeMultiGet();
		// WorkingThread
		calculateavgWTServiceTimeSet();
		calculateavgWTServiceTimeGet();
		calculateavgWTServiceTimeMultiGet();

		// Total Time= Queue+WorkinThread
		totalAvgSetInNanoSeconds = avgQueueWaitingTimePerThreadInNanoseconds + avgWTServiceTimeSetInNanoSeconds;
		
		totalAvgGetInNanoSeconds = avgQueueWaitingTimePerThreadInNanoseconds + avgWTServiceTimeGetInNanoSeconds;
		totalAvgMultiGetInNanoSeconds = avgQueueWaitingTimePerThreadInNanoseconds + avgWTServiceTimeMultiGetInNanoSeconds;

		// cache miss ratio
		MyMiddleware.overallStatisticWindows[nWindowNumber].nMWGetKeysSent += nGetKeys;
		MyMiddleware.overallStatisticWindows[nWindowNumber].nMWGetKeysReturned += nGetKeysReturned;

		// Queue length per Thread
		calculateAvgQueueLengthPerThread();
	}

	public double totalAvgSetInNanoSeconds = 0;
	public double totalAvgGetInNanoSeconds = 0;
	public double totalAvgMultiGetInNanoSeconds = 0;


}
