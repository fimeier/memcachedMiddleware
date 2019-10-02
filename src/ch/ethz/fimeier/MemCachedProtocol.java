package ch.ethz.asltest;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemCachedProtocol {

	private int BYTEStoStore = 0;

	static final private Pattern patternSet = Pattern.compile("^set .*");
	static final private Pattern patternGet = Pattern.compile("^get .*");
	static final private Pattern patternEnd = Pattern.compile("^END$");
	static final private Pattern patternValue = Pattern.compile("^VALUE .*");
	static final private Pattern patternStored = Pattern.compile("^STORED$");




	static final private Pattern patternQuit = Pattern.compile("^quit$");




	List<String> mcAddresses;
	int nMemcachedServers = 0;

	private QueueMetaData memCachedServers[];


	StringBuilder inString = new StringBuilder(MyMiddleware.CAPACITYBUFFER);//); //
	//StringBuilder outString = new StringBuilder();

	//Buffer for Output in Memcached direction
	StringBuilder outMemcached = new StringBuilder(MyMiddleware.CAPACITYBUFFER);//);
	//Buffer for Inputs from Memcached 
	StringBuilder inMemcached = new StringBuilder(MyMiddleware.CAPACITYBUFFER);//);

	QueueMetaData activeClient;
	public boolean quitConnection = false;


	/**
	 * Instrumentation per Thread
	 */
	public MyMiddlewareStatistics[] threadStatisticWindows;
	public MyMiddlewareStatistics threadStatistic;
	public boolean getStatistics = false;

	public int nActiveWindow = 0;
	public int nWindows = 0;
	public long windowSize = 0;

	private void changeThreadStatisticWindows(int windowNumberClient) {
		nActiveWindow = windowNumberClient;

		/*
		 * 
		 * 
		 * 
		 * uncomment this to actually change the windows!!!!!!
		 * 
		 * 
		 * 
		 * 
		 */
		MyMiddleware.logger.info(Thread.currentThread().getName() +" changeing threadStatistic to threadStatisticWindows["+nActiveWindow+"]");

		threadStatistic = threadStatisticWindows[windowNumberClient];

	}
	private void setWindow(int windowNumberClient) {
		if (nActiveWindow<windowNumberClient) {
			if (nActiveWindow+1!=windowNumberClient) {
				MyMiddleware.logger.severe("Thread with no data in last window... nActiveWindow+1!=windowNumberClient (nActiveWindow="+nActiveWindow+ " windowNumberClient="+windowNumberClient);
			}

			changeThreadStatisticWindows(windowNumberClient);
		}

	}


	public void resetProtocol(QueueMetaData _activeCient) {
		quitConnection = false;
		inString.setLength(0); // = new StringBuilder();//MyMiddleware.CAPACITYBUFFER);
		//outString.setLength(0); // = new StringBuilder();//MyMiddleware.CAPACITYBUFFER*10);

		outMemcached.setLength(0); // = new StringBuilder();//MyMiddleware.CAPACITYBUFFER);
		inMemcached.setLength(0); // = new StringBuilder();//MyMiddleware.CAPACITYBUFFER*10);


		/*
		 * prepare protocol for client, window, ...
		 */
		this.activeClient = _activeCient;

		//change window here !!!!!!!!!!!!!!!!!!!!!!!
		setWindow(activeClient.windowNumber);
	}

	/*
	MemCachedProtocol() {
		//1. parse input
		//2. store parameters
		//3. check parameters
		//4. decide what to do...
	}*/

	public MemCachedProtocol(List<String> _mcAddresses, String _threadName, int _nWindows, long _windowSize) {
		nWindows = _nWindows;
		windowSize = _windowSize;
		mcAddresses = _mcAddresses;
		nMemcachedServers = mcAddresses.size();
		memCachedServers = new QueueMetaData[nMemcachedServers];
		threadStatisticWindows = new MyMiddlewareStatistics[nWindows];
		MyMiddleware.logger.info(Thread.currentThread().getName()+": MyMiddlewareStatistics["+nWindows+"]" );

		/*
		 * Instrumentation: Create data structures for all windows for this thread
		 */
		for (int i = 0; i<nWindows; i++) {
			threadStatisticWindows[i] = new MyMiddlewareStatistics(i);
			threadStatisticWindows[i].threadName = _threadName;
		}
		//set the first window active
		this.threadStatistic = threadStatisticWindows[0];
		this.nActiveWindow = 0;
		/*
		 * open connections to all memcached Servers
		 * 
		 * TODO
		 * verify that connections stay open (for longer than 1sec inactivity)
		 */

		int i = 0;
		for (String mcAddress: mcAddresses) {
			String ip = mcAddress.split(":")[0];
			int port = Integer.parseInt(mcAddress.split(":")[1]);
			MyMiddleware.logger.info("Opening Connection with "+ mcAddress +" on port "+port);

			try {
				Socket clientSocket = new Socket(ip, port);
				memCachedServers[i]= new QueueMetaData(clientSocket);
				i++;					
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
	}

	/**
	 * processes the client request and sends the data back before returning
	 * @returns true means close connection
	 * @returns false re-add client to memtierConnectionsQueue
	 */
	public boolean processCommand() {//QueueMetaData activeClient) {

		parseInput();


		if (!quitConnection) {
			return false;
		} else {
			return true;
		}

	}

	private void parseInput() {

		/*
		 * get the command and parse it
		 */
		activeClient.readLineLikeFunction(inString);

		/*
		 * some regex to parse command-type
		 */
		Matcher isSetMatcher = patternSet.matcher(inString);
		boolean isSetCommand = isSetMatcher.matches();
		Matcher isGetMatcher = patternGet.matcher(inString);
		boolean isGetCommand = isGetMatcher.matches();

		Matcher isQuitMatcher = patternQuit.matcher(inString);
		boolean isQuitCommand = isQuitMatcher.matches();


		/*
		 * Splits up the command, used to parse arguments/or separate key's
		 */
		String[] split = inString.toString().split(" ");


		/*
		 * 
		 * SET !!!!!!!!!!!!
		 */
		if (isSetCommand) {
			/*
			 * Parse the Command (get #-bytes to be stored
			 * 1. parse set command: set <key> <flags> <exptime> <bytes> [noreply]\r\n
			 * assuming correct commands (no sanity checks)... seems to be ok according to project
			 */
			BYTEStoStore = Integer.parseInt(split[4]); //split[4];
			//NOREPLY = 0; //split[5];

			/*
			 * Instrumentation startAvgMemServiceTimeSet
			 * Parse the Data and send it together with the command to all memcached Server/s
			 * inString.append("\r\n") contains the command
			 */
			activeClient.sendSetToAllMemchachedServers(inString.append("\r\n"), this.memCachedServers, BYTEStoStore, true, threadStatistic);

			//adds the response from the memcached server to the output in client direction
			boolean stored = true;
			for (QueueMetaData memcachedServer: memCachedServers) {
				inMemcached.setLength(0);
				memcachedServer.readLineLikeFunction(inMemcached);

				if (!patternStored.matcher(inMemcached).matches()) {
					//TODO outputstring, protokoll anpassen??? welche errornachricht??
					System.out.println("ERROR memcached response in set was: "+inMemcached);
					stored = false;
				} else {
					//System.out.println("received STORED from memcachedServer "+memcachedServer.clientSocket.getPort());
				}
			}
			/*
			 * Instrumentation stopAvgMemServiceTimeSet
			 */
			if(getStatistics) {
				threadStatistic.stopAvgMemServiceTimeSet();
			}

			if (stored) {
				activeClient.printLikeFunction(new StringBuilder("STORED\r\n"), true);
			} else {
				activeClient.printLikeFunction(new StringBuilder("ERROR\r\n"), true);
			}

			/*
			 * Instrumentation: Stop avgWorkingThreadTime => stopAvgWTServiceTimeSet
			 */
			if(getStatistics) {
				threadStatistic.stopAvgWTServiceTimeSet();
			}

		}

		/*
		 * 
		 * 
		 * GET !!!!!!!!!
		 * 
		 */
		else if (isGetCommand){
			/* TODO Checks?? errorhandling....
			 * 1. at most 10 key's in a request
			 * assuming correct commands (project description)
			 */
			if (split.length>11) {
				/*
				 * TODO global: error handling
				 */
				//ERROR=1;
				System.out.println("Too many key's..... #key's: "+split.length);

			} else {

				List<QueueMetaData> responseMemcachedServers = new ArrayList<>();
				int nKeys = split.length-1;

				/*
				 * split keys for sharded-reading && more than 1 key
				 * MULTI-gets!!!!!
				 * distributes the keys evenly over all servers
				 */
				if (MyMiddleware.readSharded && split.length >2) {
					/*
					 * split="get key1 key2......key10"
					 * (split.length-1) = #-keys => key1=split[1]
					 */
					String[] memCachedServerKeys = new String[MyMiddleware.nMemcachedServers];
					int nMCServers = MyMiddleware.nMemcachedServers;

					/*
					 * max 1 key for each memcached Server
					 */
					if (nKeys <= nMCServers) {

						/*
						 * MULTI-get!!!
						 * Instrumentation startAvgMemServiceTimeMultiGet(); (1of3Places)
						 */
						if(getStatistics) {
							threadStatistic.startAvgMemServiceTimeMultiGet(nKeys);
						}

						for (int i=0; i<nKeys; i++) {
							memCachedServerKeys[i]="get " + split[i+1];

							//directly send the command to the servers
							QueueMetaData memcached = memCachedServers[i];
							memcached.printLikeFunction(new StringBuilder(memCachedServerKeys[i]).append("\r\n"), true);
							responseMemcachedServers.add(memcached);
						}
					}
					/*
					 * at least 1 key for each memcached server
					 */
					else {
						int i;
						for (i=0; i<nMCServers; i++) {
							memCachedServerKeys[i]="get " + split[i+1];
						}
						for (int k=i; k<nKeys; k++) {
							memCachedServerKeys[k%nMCServers]+= (" ") + split[k+1];
						}

						/*
						 * MULTI-get!!!
						 * Instrumentation startAvgMemServiceTimeMultiGet(); (2of3Places)
						 */
						if(getStatistics) {
							threadStatistic.startAvgMemServiceTimeMultiGet(nKeys);
						}
						//send the commands to the servers
						for (i=0; i<nMCServers; i++) {
							QueueMetaData memcached = memCachedServers[i];
							memcached.printLikeFunction(new StringBuilder(memCachedServerKeys[i]).append("\r\n"), true);
							responseMemcachedServers.add(memcached);
						}
					}
				}
				/*
				 * non-sharded reading or just 1 key
				 * REMARK: Instrumentation introduces another condition...
				 */
				else {
					//single key case or non-sharded-reading
					/*
					 * SINGLE-get!!!
					 * Instrumentation startAvgMemServiceTimeGet(); (1of1Places)
					 */
					if(getStatistics&&nKeys==1) {
						threadStatistic.startAvgMemServiceTimeGet();
					}
					/*
					 * MULTI-get!!!
					 * Instrumentation startAvgMemServiceTimeMultiGet(); (3of3Places)
					 * nKeys used for cache miss ratio
					 */
					else if(getStatistics&&nKeys>1) {
						threadStatistic.startAvgMemServiceTimeMultiGet(nKeys);
					}
					QueueMetaData memcached = memCachedServers[activeClient.prefferedMemcachedServer];
					memcached.printLikeFunction(inString.append("\r\n"), true);
					responseMemcachedServers.add(memcached);
				}



				/*
				 * read the returned "command"
				 */
				int nKeysReturned = 0;
				for (QueueMetaData memcachedServer: responseMemcachedServers) {
					boolean endReceived = false;
					while(!endReceived) {
						memcachedServer.readLineLikeFunction(inMemcached);
						//memResponseCommand = memcached.in.readLine();

						if (patternValue.matcher(inMemcached).matches()) {
							/*
							 * read the returned data
							 * and return it to the client
							 */

							//Instrumentation temp value
							nKeysReturned++;

							BYTEStoStore = Integer.parseInt(inMemcached.toString().split(" ")[3]);


							activeClient.printLikeFunction(inMemcached.append("\r\n"), false);

							//activeClient.printReadLineFromStreamFunction(memcached, true);

							activeClient.printReadNBytesFromStreamFunction(memcachedServer, BYTEStoStore, false);

							//reset buffer
							inMemcached.setLength(0);
						} else if (patternEnd.matcher(inMemcached).matches()) {

							//activeClient.printLikeFunction(inMemcached.append("\r\n"), true);

							endReceived = true;
							inMemcached.setLength(0);


						} else {
							//TODO Error behavior
							System.out.println("Error!!!!!!!! Server response was: "+inMemcached);
							inMemcached.setLength(0);
						}

					}

				}
				/*
				 * Instrumentation stopAvgMemServiceTimeGet();
				 * nKeysReturned used to calculate cache miss ratio
				 */
				//single-get
				if(getStatistics&&nKeys==1) {
					threadStatistic.stopAvgMemServiceTimeGet(nKeysReturned);
				}
				// MULTI-get!!!
				else if(getStatistics&&nKeys>1) {
					threadStatistic.stopAvgMemServiceTimeMultiGet(nKeysReturned);
				}

				//add END end send data back to client
				activeClient.printLikeFunction(new StringBuilder("END\r\n"), true);

				/*
				 * Instrumentation: Stop avgWorkingThreadTime => get or multiget
				 */
				// SINGLE-get!!!
				if(getStatistics&&nKeys==1) {
					threadStatistic.stopAvgWTServiceTimeGet();
				}
				// MULTI-get!!!
				else if(getStatistics&&nKeys>1) {
					threadStatistic.stopAvgWTServiceTimeMultiGet();
				}

			}
		}

		/*
		 * Quit !!!!!!!!!
		 */
		else if(isQuitCommand) {
			System.out.println("MemCachedProtocol.parseInput(): received quit from client. Closing connection...");
			//response = "quit";
			quitConnection = true;
			return;
		}

		else {
			System.out.println("######################### MemCachedProtocol.parseInput() found a malformed command '"+inString+"'");
			quitConnection = true;
			return;
		}

	}


}

