import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
//import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;


/**
 * @author fimeier
 *
 */
public class ASLJobControlling {
	/*
	 * HERE IS THE WHOLE CONFIGURATION
	 * TODO: Parse parameters from configfile 1:1
	 */
	
	/*
	 * 
	 * 
	 * 
	 * Parameters used for Benchmarks-Configuration
	 * 
	 * 
	 */
	
	/*
	 * loading of the memcached server's
	 * TODO: find necessary and needed limit's (do not waste time here)
	 * jedes Experiment macht ein restart des memcached servers und läd ihn....
	 * jeweils immer alle clients server mit der benötigten value-size
	 */
	//public static boolean loadIt = false;
	public static int MEMTIERLOADIterations = 1;
	public static int MEMTIERLOADTIMEWriteOnly = 3;
	public static int MEMTIERLOADTIMEReadOnly = 6;

	/*
	 * deployment of needed "software" for individual task's
	 */
	public static boolean createFolders = true;
	
	public static boolean deployClientScripts = true;
	
	public static boolean deployMiddlewareScripts = true;
	public static boolean deployMiddlewareJava = true;
	
	//public static boolean deployServerScripts = false; //überlege ob das nötig ist
	

	/*
	 * 
	 * Timing-Parameters for Benchmarks
	 * 
	 * 
	 */
	//delay before the memtierclient get started
	public long STARTUPTIMEMIDDLEWARE = 2000;
	
	//time in seconds for the memtier_benchmark to be running
	public int MEMTIERTESTTIME = 64; //Set this to 63 for final experiments
	/*
	 * MyMiddleware parameters: start, measure, kill
	 */
	long tWAITBEFOREMEASUREMENTS = STARTUPTIMEMIDDLEWARE - 500; //effective start is with first client sending data
	long tTIMEFORMEASUREMENTS = MEMTIERTESTTIME*1000 + 1500;
	long JAVAMIDDLEWAREKILLDELAY = STARTUPTIMEMIDDLEWARE + tWAITBEFOREMEASUREMENTS +tTIMEFORMEASUREMENTS +1000; //~2+1.5+65.5+1=68


	/*
	 * 
	 * ASL2018 not needed anymore
	 * 
	 * 
	 * 
	 */
	/*
	 * ASL2018 not needed anymore: workload (read/write and ratio parameters)
	 * 
	 * wird bei loadIt benutzt
	 */
	static String WRITEONLY = " --ratio=1:0";
	static String READONLY = " --ratio=0:1";
	static String RATIO11 = " --ratio=1:1";
	static String RATIO13 = " --ratio=1:3";
	static String RATIO16 = " --ratio=1:6";
	static String RATIO19 = " --ratio=1:9";
	static String getSimpleWorkloadName(String wl) { 
		if (wl.equals(WRITEONLY))
			return "WriteOnly";
		if (wl.equals(READONLY))
			return "ReadOnly";

		if (wl.equals(RATIO11))
			return "ratio=1t1";

		if (wl.equals(RATIO13))
			return "ratio=1t3";

		if (wl.equals(RATIO16))
			return "ratio=1t6";

		if (wl.equals(RATIO19))
			return "ratio=1t9";

		return "UnknownWorkload";
	}

	/**
	 * scheint auch im ASL18 nicht gebraucht worden zu sein
	 * 
	 * those keys are not available as real data
	 * they can be used to aggreagate aggregated data
	 * HINT: Maybe better do define a separate function
	 */
	/*public static Set<String> virtualKeys = new HashSet<>(); 
	static{
		virtualKeys.add("combinedResponseTimeGetMultiGet");
	}*/

	
	
	
	
	
	/*
	 * 
	 * 
	 * Parameters used for DataProcessing-Configuration
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	/*
	 * Colors for plots
	 * TODO: reuse Write Colors... or find better ones
	 */
	static String[] defaultColorsThroughputRead = {"#33ccff", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"}; 
	static String[] defaultColorsThroughputWrite = {"#33ccff", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"};
	static String[] defaultColorsLatencyRead = {"#ffcc99", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"};
	static String[] defaultColorsLatencyWrite = {"#661400", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"};
	static String[] unknownTypeColors = {"#D30FAF", "#D30FAF","#D30FAF", "#D30FAF", "#D30FAF", "#D30FAF"};

	
	/*
	 * what pictures should be generate (from ASL2018)
	 */
	public static boolean consolidateStatistics = false;
	public static boolean consolidateStatisticsBaseline21 = false;
	public static boolean consolidateStatisticsBaseline22 = false;
	public static boolean consolidateStatisticsBaseline31 = false;
	public static boolean consolidateStatisticsBaseline32 = false;
	public static boolean consolidateStatisticsfullSystem41 = false;
	public static boolean consolidateStatisticsshardedCase51 = false;
	public static boolean consolidateStatisticsnonshardedCase52 = false;
	public static boolean consolidateStatisticsMultiGetsPercentiles = false;
	public static boolean createHistogramsMultiGets = false;
	public static boolean createConsolitatedPlots = false;

	
	/*
	 * Helper for Statistics
	 */
	//used in createAllStatistics&createSpecialPlots
	public static boolean showIdInLineTitle = true;

	
	/*
	 * TODO: move inti DataProcessing (everything is local!!!)
	 * used in createAllStatistics()
	 */
	static private int globalUniqueLineIdentifier = 0;
	static public String getUniqueLineIdentifier() {
		globalUniqueLineIdentifier++;
		return globalUniqueLineIdentifier+"";
	}
	static private int allLinesFileID = 0;
	static public String getallLinesFileID() {
		allLinesFileID++;
		return allLinesFileID+"";
	}


	
	
	/*
	 * 
	 * FILES
	 * FOLDERS
	 * IP
	 * PORTS
	 * 
	 * 
	 */


	//used in statisticsThroughputLatencyClientorMiddleware
	public static String experimentsFolderForLatex = "ANPASSEN";// ../git/asl-fall18-project/JobControlling/experiments/";



	/*
	 * working folder for clients, servers and middlewares
	 * 
	 * REMARK:
	 * 		-everything gets startet in this folder from this class (ASLJobControlling)
	 * 		-this class itself is in the gitrepo/dist as runnable-jar file stored
	 */
	public static String workingFolder = "~/automato/";

	public static String gitBaseFolder = "/home/fimeier/asl-fall19-project/";

	public static String experimentsBaseFolder = gitBaseFolder+"JobControlling/experiments/";
	public static String allLinesBaseFolder = experimentsBaseFolder+"generatedPlots/allLines/";
	public static String allLinesOutputFolderSource = allLinesBaseFolder+"source/";

	public static String scriptsFolder = gitBaseFolder+"JobControlling/bashScripts/";
	public static String javaSourceFolder = gitBaseFolder+"dist/";

	public static String[] middlewareIPs = new String[] {"10.0.0.21","10.0.0.22"};
	public static String[] middlewarePorts = new String[] {"11212","12212"};
	public static String middlewareScriptSource = scriptsFolder + "runMiddleware.bash";
	public static String middlewareScriptTarget = workingFolder + "runMiddleware.bash";
	public static String jarFile = "middleware-fimeier.jar";
	public static String middlewareJavaSource = javaSourceFolder + jarFile;
	public static String middlewareOutputFile = workingFolder+"outputMiddleware";
	public static String middlewareErrorFile = workingFolder+"errorMiddleware";
	public static String middlewarefinalStats = workingFolder+"finalStats";
	public static String middlewarehistogram = workingFolder+"histogram";

	public static String[] middlewareFiles = new String[] {middlewareOutputFile,middlewareErrorFile,middlewarefinalStats,middlewarehistogram};

	public static String[] clientIPs = new String[] {"10.0.0.11","10.0.0.12","10.0.0.13"};
	public static String clientScriptTarget = workingFolder+"runMemtierBenchmark.bash";
	public static String clientScriptTarget2ndInstance = workingFolder+"runMemtierBenchmark2ndInstance.bash";
	public static String clientScriptSource = scriptsFolder + "runMemtierBenchmark.bash";
	public static String clientScriptSource2ndInstance = scriptsFolder + "runMemtierBenchmark2ndInstance.bash";

	public static String clientOutputFile = workingFolder+"outputClient";
	public static String clientOutputJSONFile = workingFolder+"json.txt";
	public static String clientErrorFile = workingFolder+"errorClient";
	public static String[] clientFiles = new String[] {clientOutputFile,clientErrorFile,clientOutputJSONFile};
	public static String[] clientFilesIncluding2ndInstance = new String[] {clientOutputFile,clientErrorFile,clientOutputJSONFile,clientOutputFile+"2ndInst",clientErrorFile+"2ndInst",clientOutputJSONFile+"2ndInst"};

	public static String[] serverIPs = new String[] {"10.0.0.31","10.0.0.32","10.0.0.33"};
	public static String[] serverPorts = new String[] {"12333","12444","12555"};
	public static String[] serverIpandPorts = new String[] {serverIPs[0]+":"+serverPorts[0],serverIPs[1]+":"+serverPorts[1],serverIPs[2]+":"+serverPorts[2]};

	/*
	public static String serverScriptSoure = scriptsFolder + "start_memcached.bash";
	public static String serverOutputFile = workingFolder+"outputMemcached";
	public static String serverErrorFile = workingFolder+"errorMemcached";
	public static String[] serverFiles = new String[] {serverOutputFile,serverErrorFile};
	*/
	

	/**
	 * inner loop:
	 * x-axis#run1#run2#run3#avg#stddev always the same
	 *		BUT run1 for example:
	 *			ops... sum all values 
	 *			latency... avg all values
	 *
	 *
	 *	ACHTUNG: throughputGet vs throughputGetAllKeys (<= der richtige Wert)
	 *
	 */
	public ArrayList<KeyProperties> middlewareDataKeys = null;
	ASLJobControlling(){
		/*
		 * todo... erstelle hier liste gemäss middleware key=value... alle möglichne Werte
		 * so kann dieses Ding auch in Zukunft für andere Statistiken verwendet werden
		 */
		/*
		 * selected keywords from finalstats...
	throughputSet","throughputGet","throughputMultiGet","throughputAllCommands","throughputGetAllKeys","avgQueueWaitingTime","avgQueueLength","avgMemcachedResponseTimeSet","avgMemcachedResponseTimeGet","avgMemcachedResponseTimeMultiGet","avgWorkerThreadResponseTimeSet","avgWorkerThreadResponseTimeGet","avgWorkerThreadResponseTimeMultiGet","avgMiddlewareResponseTimeSet","avgMiddlewareResponseTimeGet","avgMiddlewareResponseTimeMultiGet"
		 */
		middlewareDataKeys = new ArrayList<>();

	}

	public static abstract class Commands{
		String commandType = "";
		String[] cmd;

	}

	public static class MemtierCommand extends Commands{

		public MemtierCommand() {
			super.commandType = "MemtierCommand";

		}

		HashMap<String,String> defaultArguments;

		public void setDefault() {
			/*
			 * create hash map with default options
			 */
			defaultArguments = new HashMap<>();
			defaultArguments.put("--server","--server="+middlewareIPs[0]);
			defaultArguments.put("--port", "--port="+middlewarePorts[0]);
			defaultArguments.put("--protocol", "--protocol=memcache_text");
			defaultArguments.put("--json-out-file", "--json-out-file=json.txt");
			defaultArguments.put("--expiry-range", "--expiry-range=2000000-2000001"); //>2.5weeks
			defaultArguments.put("--key-maximum", "--key-maximum=10000");
			defaultArguments.put("--run-count", "--run-count=1");
			//defaultArguments.put("--requests", "--requests=n100");
			defaultArguments.put("--data-size", "--data-size=4096");
			//defaultArguments.put("--clients", "--clients=1");
			//defaultArguments.put("--threads", "--threads=1");
			defaultArguments.put("--test-time", "--test-time=60");

			//defaultArguments.put("--distinct-client-seed", "--distinct-client-seed"); // Use a different random seed for each client
			//defaultArguments.put("--randomize", "--randomize"); //random seed based on timestamp (default is constant value)

		}

		public void setMemtierCommand(String memtierIP, String argsDifferentFromDefault, boolean start2ndInstance) {
			setDefault();

			String memtierCommand = clientScriptTarget;
			if (start2ndInstance) {
				memtierCommand = clientScriptTarget2ndInstance;
				defaultArguments.put("--json-out-file", "--json-out-file=json.txt2ndInst");
			}
			if (argsDifferentFromDefault!=null) {
				for (String s: argsDifferentFromDefault.split(" ")) {
					String key = s.split("=")[0];
					String value = s.split("=")[1];
					/*
					 * add (keyargument, keyargument=value) => s!!
					 */
					defaultArguments.put(key, s);
				}
			}

			for (String argumentKey: defaultArguments.keySet()) {
				/*
				 *cmdduntilehere+=" "--server=12.0.0.66
				 */
				memtierCommand += " " + defaultArguments.get(argumentKey);

			}


			super.cmd = new  String[]  {"ssh", memtierIP, memtierCommand};
		}

	}


	/**
	 * 
	 * Usage: -l <MyIP> -p <MyListenPort> -t <NumberOfThreadsInPool> -s <readSharded> -m <MemcachedIP:Port> <MemcachedIP2:Port2> ...

	 * @author fimeier
	 *
	 */
	public static class MiddlewareCommand extends Commands{
		public MiddlewareCommand() {//, String[] _nodesForThisRun) {
			super.commandType = "MiddlewareCommand";

		}
		HashMap<String,String> defaultArguments;

		long javaJobKillDelay = 15000;
		String middlewareIP = "";

		public void setDefault() {
			/*
			 * create hash map with default options
			 */
			defaultArguments = new HashMap<>();
			//defaultArguments.put("-l","-l "+middlewareIPs[0]);
			defaultArguments.put("-p","-p "+middlewarePorts[0]);
			defaultArguments.put("-t","-t 8");
			defaultArguments.put("-s","-s false");
			defaultArguments.put("-m","-m "+serverIpandPorts[0]+ " "+serverIpandPorts[1]);
			defaultArguments.put("-tWaitBeforeMeasurements","-tWaitBeforeMeasurements 0");
			defaultArguments.put("-tTimeForMeasurements","-tTimeForMeasurements 6000");

			javaJobKillDelay = 15000;


			//starttime + startInstrumentationDelay == effektiver start messung
			//starttime + startInstrumentationDelay + InstrumentationInterval == effektives Ende der messung
		}

		public void setMiddlewareCommand(String _middlewareIP, String argsDifferentFromDefault, long _javaJobKillDelay) {
			setDefault();
			javaJobKillDelay = _javaJobKillDelay;
			middlewareIP = _middlewareIP;

			String getmiddlewareCommand = middlewareScriptTarget + " " + jarFile;


			if (argsDifferentFromDefault!=null) {
				String[] split = argsDifferentFromDefault.split(" ");
				int splitLength = split.length;
				for (int i=0; i<splitLength; i+=2) {
					String key = split[i];
					String value;
					if (key.equals("-m")) {
						value = split[i];
						for (String memcachedServer: split[i+1].split("#")) {
							value += " "+memcachedServer;
						}
					} else {
						value = split[i] + " " + split[i+1];
					}

					defaultArguments.put(key, value);
				}
			}

			for (String argumentKey: defaultArguments.keySet()) {
				/*
				 *cmdduntilehere+=" "--server=12.0.0.66
				 */
				getmiddlewareCommand += " " + defaultArguments.get(argumentKey);

			}

			super.cmd = new  String[] {"ssh", middlewareIP, getmiddlewareCommand};
		}

	}


	/*
	 * 
	 * workflow
	 * 0. set start/kill/mesure/delaytime....
	 * 0.0. add suffix=runX and clients in test to middleware object... used to collect/name files
	 * 1. start middleware
	 * 2. start memtierclients
	 * ....
	 * RunCommandInThread with middleware will be killed through shutdownhook.... collect all files
	 * 
	 */
	public class TestSetting{
		/*
		 * memtier client arguments config
		 */
		String argsDifferentFromDefault1stInstance = "";
		String argsDifferentFromDefault2ndInstance = "";

		/*
		 * middleware arguments config
		 */
		String argsMiddleware1 = "";
		String argsMiddleware2 = "";

		/**
		 * #-memcached servers
		 */
		int nServer = 1;
		int nVirtualMachineClients = 1;
		String[] clientInTestIPs = null;
		int nInstancesOfMemtierPerMachine = 1; //sollte immer gleich sein wie 2 / nCT

		/**
		 * nCT == 1 implies start 2x memtier_benchmark per VM-client
		 */
		int nCT = 2;
		boolean start2ndMemtierInstance = false;
		int nVC = 1;
		String workload = "";
		String suffixForThisRun = "Run666";
		/**
		 * #-middleware servers
		 * 1 Implies middleware1
		 * 0 implies test without middleware
		 * 2 implies middleware1 and middleware2
		 */
		int nMW = 1;


		/**
		 * number of threads in the middleware
		 */
		int nWorkerThreads = 8;

		int multiGetSize = 0;

		String shardedReading = "";

		String experimentFolder = "";

		TestSetting(int _nServer, int _nVirtualMachineClients, int _nVC, int _nCT, String _workload, String _suffixForThisRun, int _nMW, int _nWorkerThreads, int _multiGetSize, String _shardedReading, String _experimentFolder){
			nServer = _nServer;

			nVirtualMachineClients = _nVirtualMachineClients;
			clientInTestIPs = new String[nVirtualMachineClients];
			for(int i = 0; i<nVirtualMachineClients;i++) {
				clientInTestIPs[i]=ASLJobControlling.clientIPs[i];
			}

			//connect
			nInstancesOfMemtierPerMachine = 2 / _nCT;

			nCT = _nCT;
			if (nCT == 1) {
				start2ndMemtierInstance = true;
			}

			nVC = _nVC;

			workload = _workload;

			suffixForThisRun = _suffixForThisRun;

			nMW = _nMW;

			nWorkerThreads = _nWorkerThreads;

			multiGetSize = _multiGetSize;

			shardedReading = _shardedReading;

			experimentFolder = _experimentFolder;


			/*
			 * middleware arguments config
			 */

			//-l 127.0.0.1 -p 11212 -t 2 -s true -m 127.0.0.1:12333
			argsMiddleware1 = "-tWaitBeforeMeasurements "+ tWAITBEFOREMEASUREMENTS;
			argsMiddleware1 += " -tTimeForMeasurements "+ tTIMEFORMEASUREMENTS;
			argsMiddleware1 += " -t "+nWorkerThreads;
			argsMiddleware1 += " -s "+shardedReading;

			String middlewareServerConnectString = " -m ";
			for (int s = 0; s<nServer; s++) {
				//ugly hack needed
				//middlewareargsDifferentFromDefault += "-m "+serverIpandPorts[0]+ "#"+serverIpandPorts[1] +"#"+serverIpandPorts[2] +" ";
				if (s==0) {
					middlewareServerConnectString += serverIpandPorts[s];
				} else {
					middlewareServerConnectString += "#"+serverIpandPorts[s];
				}
			}
			argsMiddleware1 += middlewareServerConnectString;

			if (nMW==2) {
				argsMiddleware2 = argsMiddleware1;

				argsMiddleware2 += " -l "+middlewareIPs[1];
				argsMiddleware2 += " -p "+middlewarePorts[1];

			}

			argsMiddleware1 += " -l "+middlewareIPs[0];
			argsMiddleware1 += " -p "+middlewarePorts[0];




			/*
			 * memtier client arguments config
			 */
			String memtierTestTimeArgument = "--test-time="+MEMTIERTESTTIME; //if you change it, middleware delay maybe wrong

			//1. always set time to test
			argsDifferentFromDefault1stInstance = memtierTestTimeArgument;

			//set virtual clients
			argsDifferentFromDefault1stInstance += " --clients="+nVC;

			//set threads per vm
			argsDifferentFromDefault1stInstance += " --threads="+nCT;

			//set workload
			argsDifferentFromDefault1stInstance += workload;

			//set multiGet if needed
			if (multiGetSize>0) {
				argsDifferentFromDefault1stInstance += " --multi-key-get="+multiGetSize;
			}


			//change the server if needed (default middleware1
			if (nMW==1 && nCT==2){
				//default use middleware1 and connect both "Threads" to it
				argsDifferentFromDefault1stInstance += " --server="+middlewareIPs[0] + " --port="+middlewarePorts[0];
			}

			if (nMW==0 && nServer==1 && nCT==2) {
				//Tests without middleware with server1 and two client threads (1x memtier_benchmark)
				argsDifferentFromDefault1stInstance += " --server="+serverIPs[0] + " --port="+serverPorts[0];

			}
			//do not change the memtierarguments after this point
			if (nMW==2 && nCT==1 && start2ndMemtierInstance==true) {
				//connect memtier with 2 middlewares => start 2 instances per client
				argsDifferentFromDefault2ndInstance = argsDifferentFromDefault1stInstance;
				argsDifferentFromDefault1stInstance += " --server="+middlewareIPs[0] + " --port="+middlewarePorts[0];
				argsDifferentFromDefault2ndInstance += " --server="+middlewareIPs[1] + " --port="+middlewarePorts[1];
			}
			if (nMW==0 && nServer==2 && start2ndMemtierInstance==true) {
				//Tests without middleware with server 2 && testSetting.nCT==1 (2x memtier_benchmark)
				argsDifferentFromDefault2ndInstance = argsDifferentFromDefault1stInstance;
				argsDifferentFromDefault1stInstance += " --server="+serverIPs[0] + " --port="+serverPorts[0];
				argsDifferentFromDefault2ndInstance += " --server="+serverIPs[1] + " --port="+serverPorts[1];
			}

			/*
			defaultArguments = new HashMap<>();
			defaultArguments.put("--server","--server="+middlewareIPs[0]);
			defaultArguments.put("--port", "--port="+middlewarePorts[0]);
			defaultArguments.put("--protocol", "--protocol=memcache_text");
			defaultArguments.put("--json-out-file", "--json-out-file=json.txt");
			defaultArguments.put("--expiry-range", "--expiry-range=9999-10000");
			defaultArguments.put("--key-maximum", "--key-maximum=100");
			defaultArguments.put("--run-count", "--run-count=1");
			//defaultArguments.put("--requests", "--requests=n100");
			defaultArguments.put("--data-size", "--data-size=4096");
			//defaultArguments.put("--clients", "--clients=1");
			//defaultArguments.put("--threads", "--threads=1");
			defaultArguments.put("--test-time", "--test-time=60");
			 */

		}

	}



	/**
	 * 
	 * @param file
	 * @param ob "Sets" "Gets" "Totals"
	 */
	public static HashMap<String,Double> jsonMemtierBenchmarkAllStats(String file, String ob) {
		HashMap<String,Double> extractedData = null;
		try {
			Reader fileReader = new BufferedReader(new FileReader(file));
			JsonReader jsonReader = Json.createReader(fileReader);
			JsonObject object = jsonReader.readObject();

			//default get avg
			if (ob.equals("0"))
				ob = "Gets";

			if (ob.equals("25")||ob.equals("50")||ob.equals("75")||ob.equals("90")||ob.equals("99")) {
				int percentile = Integer.parseInt(ob);

				double value = getPercentile(object, "GET", percentile);

				jsonReader.close();
				fileReader.close();
				extractedData = new HashMap<>();
				extractedData.put("Latency", value);

				return extractedData;

			}

			extractedData = getmemtierAllStatsJson(object, ob);



			jsonReader.close();
			fileReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return extractedData;
	}


	public static void parseJsonMemtierBenchmark(String file) {
		/*
		 * process generated data: Example get throughput infos out of json file
		 */

		try {
			//String file="/home/fimeier/asl-fall18-project/JobControlling/experiments/baseline21/json.txtBaseline21_ReadOnly_nVC=2_Rep=1_Client3";
			Reader fileReader = new BufferedReader(new FileReader(file));
			JsonReader jsonReader = Json.createReader(fileReader);
			JsonObject object = jsonReader.readObject();

			//call this get all stats details
			HashMap<String,Double> extractedDataSet = getmemtierAllStatsJson(object, "Sets");
			HashMap<String,Double> extractedDataGet = getmemtierAllStatsJson(object, "Gets");
			HashMap<String,Double> extractedDataTotals = getmemtierAllStatsJson(object, "Totals");

			jsonReader.close();
			fileReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}





		/*
		 * process generated data: Example get LATENCY infos out of json file
		 */

		/*
		try {
			//String file="/home/fimeier/asl-fall18-project/JobControlling/experiments/baseline21/json.txtBaseline21_ReadOnly_nVC=2_Rep=1_Client3";
			Reader fileReader = new BufferedReader(new FileReader(file));
			JsonReader jsonReader = Json.createReader(fileReader);
			JsonObject object = jsonReader.readObject();

			getmemtierCDF(object, "SET");
			getmemtierCDF(object, "GET");


			jsonReader.close();
			fileReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */

	}

	/**
	 * read out the CDF.... not finished
	 * @param object
	 * @param command SET or GET
	 */
	public static double getPercentile(JsonObject object, String command, int percentile){

		JsonArray jsonArrayToExtractfrom = object.getJsonObject("ALL STATS").getJsonArray(command);


		double value = 0.0;

		for(int i = 0; i<jsonArrayToExtractfrom.size(); i++) {
			double msec = Double.valueOf(jsonArrayToExtractfrom.get(i).asJsonObject().get("<=msec").toString());
			double perc =Double.valueOf(jsonArrayToExtractfrom.get(i).asJsonObject().get("percent").toString());

			if (perc>=percentile) {
				//System.out.println(percentile+"th Found: "+msec+ " and "+ perc);
				return msec;
			}
			//System.out.println(percentile+"th NOOO SMALL: "+msec+ " and "+ perc);

			value = msec;

		}

		return value;


	}

	/**
	 * 
	 * @param absoluteFilename
	 * @param command
	 * @param lowIndexInMs (bucketLowIndex,bucketHighIndex] in ms!!!!
	 * @param highIndexInMs (bucketLowIndex,bucketHighIndex] in ms!!!!
	 * @return
	 */
	public static ArrayList<Double> getClientHistogramData(String absoluteFilename, String command, double bucketLowIndex, double bucketHighIndex){

		/*
		 * "GET":[
				{
					"<=msec": 0.880
					,"percent": 0.02
					}
				,{
					"<=msec": 0.900
					,"percent": 0.03
					}
		 */
		ArrayList<Double> values = new ArrayList<>();
		try {
			Reader fileReader = new BufferedReader(new FileReader(absoluteFilename));
			JsonReader jsonReader = Json.createReader(fileReader);
			JsonObject object = jsonReader.readObject();
			JsonArray jsonArrayToExtractfrom = object.getJsonObject("ALL STATS").getJsonArray(command);


			double cumPerc = 0.0;
			double percOld = 0.0;

			for(int i = 0; i<jsonArrayToExtractfrom.size(); i++) {
				double msec = Double.valueOf(jsonArrayToExtractfrom.get(i).asJsonObject().get("<=msec").toString());
				double perc =Double.valueOf(jsonArrayToExtractfrom.get(i).asJsonObject().get("percent").toString());

				double percentInThisDataPoint = perc - percOld;
				percOld = perc;
				cumPerc+=percentInThisDataPoint;

				boolean correctBucket = false;
				if (bucketLowIndex==bucketHighIndex && msec<=bucketHighIndex)
					correctBucket=true;
				if (bucketLowIndex<msec && msec<=bucketHighIndex)
					correctBucket=true;
				if (!correctBucket)
					continue;


				if (percentInThisDataPoint<0)
					System.out.println("Error....");
				//System.out.println("msec="+msec + " perc="+perc + " percentInThisDataPoint="+percentInThisDataPoint+ " cumPerc="+cumPerc+ " file="+absoluteFilename);

				values.add(percentInThisDataPoint);

			}



		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return values;



	}





	/**
	 * 
	 * @param object
	 * @param command Sets, Gets or Totals
	 * @return Ops/sec, hits/sec, Misses/sec and KB/sec
	 */
	public static HashMap<String,Double> getmemtierAllStatsJson(JsonObject object, String command){
		//call this get all stats details
		HashMap<String,Double> extractedData = new HashMap<>();

		JsonObject jsonObjectToExtractfrom = object.getJsonObject("ALL STATS").getJsonObject(command);

		extractedData.put("OPs", Double.valueOf(jsonObjectToExtractfrom.get("Ops/sec").toString()));
		extractedData.put("Hits", Double.valueOf(jsonObjectToExtractfrom.get("Hits/sec").toString()));
		extractedData.put("Misses", Double.valueOf(jsonObjectToExtractfrom.get("Misses/sec").toString()));
		extractedData.put("Latency", Double.valueOf(jsonObjectToExtractfrom.get("Latency").toString()));
		extractedData.put("KBs", Double.valueOf(jsonObjectToExtractfrom.get("KB/sec").toString()));

		/*
		for (String key: extractedData.keySet()) {
			System.out.println(key+"="+extractedData.get(key));
		}*/

		return extractedData;
	}




	public void doBenchmarks(TestSetting testSetting) {



		/*
		 * Start Middleware
		 */
		List<Thread> middlewareThreads = new ArrayList<>();
		if (testSetting.nMW > 0){

			//always start one middleware
			MiddlewareCommand mwRun = new MiddlewareCommand();
			mwRun.setMiddlewareCommand(middlewareIPs[0], testSetting.argsMiddleware1, JAVAMIDDLEWAREKILLDELAY);

			Thread threadMiddleware = new Thread(new RunCommandInThread(mwRun));
			middlewareThreads.add(threadMiddleware);
			threadMiddleware.start();

			//start a second middleware if needed
			if (testSetting.nMW==2) {
				MiddlewareCommand mwRun2 = new MiddlewareCommand();
				mwRun2.setMiddlewareCommand(middlewareIPs[1], testSetting.argsMiddleware2, JAVAMIDDLEWAREKILLDELAY);

				Thread threadMiddleware2 = new Thread(new RunCommandInThread(mwRun2));
				middlewareThreads.add(threadMiddleware2);
				threadMiddleware2.start();
			}

			//give the middleware(s) some time to boot before launching the clients
			try {
				Thread.sleep(STARTUPTIMEMIDDLEWARE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*
		 * start memtier_benchmark
		 */
		List<Thread> clientThreads = new ArrayList<>();
		/*
		 * start all clients
		 */
		for (int i = 0; i < testSetting.nVirtualMachineClients; i++) {

			//always start one instance
			MemtierCommand memtierRun = new MemtierCommand();
			boolean start2ndInstance = false;
			memtierRun.setMemtierCommand(testSetting.clientInTestIPs[i], testSetting.argsDifferentFromDefault1stInstance, start2ndInstance);

			Thread threadMemtier = new Thread(new RunCommandInThread(memtierRun));
			clientThreads.add(threadMemtier);
			threadMemtier.start();

			//start a second instance if needed
			if(testSetting.start2ndMemtierInstance==true) {
				MemtierCommand memtierRun2 = new MemtierCommand();
				start2ndInstance = true;
				memtierRun2.setMemtierCommand(testSetting.clientInTestIPs[i], testSetting.argsDifferentFromDefault2ndInstance, start2ndInstance);

				Thread threadMemtier2 = new Thread(new RunCommandInThread(memtierRun2));
				clientThreads.add(threadMemtier2);
				threadMemtier2.start();
			}
		}

		/*
		 * join all clients
		 */
		int i = 0;
		for (Thread threadMemtier: clientThreads) {
			try {
				System.out.println("Trying clientThreads["+i+"].join()... "+(i+1)+"-out-of-"+clientThreads.size());
				threadMemtier.join();
				System.out.println("clientThreads["+i+"].join() succeded!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}

		/*
		 * copy all client files back
		 */
		int c = 1;
		for (String clientIP: testSetting.clientInTestIPs) {
			String completeFileSuffix = testSetting.suffixForThisRun+"_Client"+c;
			copyFilesback(new String[] {clientIP},completeFileSuffix, testSetting.start2ndMemtierInstance, testSetting.experimentFolder);
			c++;

		}

		if (testSetting.nMW > 0){
			/*
			 * join middleware	
			 */
			try {
				int m = 1;
				for (Thread middlewareThread: middlewareThreads) {
					System.out.println("Trying middlewareThread["+m+"].join()... "+(m+1)+"-out-of-"+middlewareThreads.size());
					middlewareThread.join();
					System.out.println("middlewareThread["+m+"].join() succeded!");
					m++;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
			 * copy middleware files back
			 */
			for (int m = 0; m < testSetting.nMW; m++) {
				String middlewareIP = ASLJobControlling.middlewareIPs[m];
				String completeFileSuffix = testSetting.suffixForThisRun+"_Middleware"+(m+1);
				copyFilesback(new String[] {middlewareIP},completeFileSuffix, false, testSetting.experimentFolder);
			}
		}




		System.out.println("after do benchmarks....");

	}

	public static void printCommand(String[] cmd) {
		for (String s: cmd) {
			System.out.print(s+" ");
		}
		System.out.print("\n");
	}


	public class RunCommandInThread implements Runnable {
		String[] cmd;
		boolean isMiddlewareObject = false;
		boolean isMemtierObject = false;

		Commands commandObject;

		MiddlewareCommand middlewareObject = null;

		MemtierCommand memtierObject = null;


		public RunCommandInThread(Commands _commandObject) {
			this.commandObject = _commandObject;
			this.cmd = commandObject.cmd;	
			if (commandObject.commandType.equals("MiddlewareCommand")) {
				isMiddlewareObject = true;
				middlewareObject =(MiddlewareCommand) commandObject;
			}
			if (commandObject.commandType.equals("MemtierCommand")) {
				isMemtierObject = true;
				memtierObject =(MemtierCommand) commandObject;
			}
		}

		@Override
		public void run() {


			try {
				Process p = Runtime.getRuntime().exec(cmd); 

				System.out.print("Thread, starting: " ); printCommand(cmd);





				//BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

				//BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				if (isMiddlewareObject) {
					try {
						Thread.sleep(middlewareObject.javaJobKillDelay); //Einlfuss??? :-)
						/*
						 * get pid.. returned from bash script
						 */
						BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String pidJavaProcess = stdInput.readLine();
						System.out.println("Trying to kill the MW with pid="+pidJavaProcess);

						String killCommand = "kill -15 "+pidJavaProcess;
						String[] cmdkillMW = new String[]{"ssh", middlewareObject.middlewareIP, killCommand};

						Process p2 = Runtime.getRuntime().exec(cmdkillMW);

						/*
						 * give the middleware time to create the statistics						
						 */
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}



					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//assuming it is a client
				else {
					try {
						System.out.println("Memtier_benchmark Exitcode was :"+p.waitFor());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}



			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}




	public void copyFilesback(String[] nodesToCollectFrom, String _suffix, boolean copy2ndInstance, String experimentFolder) {
		String suffix = (_suffix!=null)? _suffix : "";
		List<String[]> scpJobs = new ArrayList<String[]>();

		for (String hostIP: nodesToCollectFrom) {

			String[] absolutePaths = null;

			//clients
			if (hostIP.startsWith("10.0.0.1")) {
				absolutePaths=ASLJobControlling.clientFiles;
				if(copy2ndInstance) {
					absolutePaths=ASLJobControlling.clientFilesIncluding2ndInstance;
				}
			}

			//Middlewares
			if (hostIP.startsWith("10.0.0.2"))
				absolutePaths=ASLJobControlling.middlewareFiles;

			//Server
			/*
			if (hostIP.startsWith("10.0.0.3"))
				absolutePaths=ASLJobControlling.serverFiles;
			*/

			for(String absoluteFileName: absolutePaths) {
				String hostFileSource = hostIP + ":" + absoluteFileName;
				/*
				 * hack for true filename: needed because absoluteFileName contains the path
				 */
				String fileName = absoluteFileName.split("/")[absoluteFileName.split("/").length -1];
				String destinationFile = ASLJobControlling.experimentsBaseFolder + experimentFolder +"/" + fileName + suffix;


				String[] clientGetFile = {"scp", hostFileSource, destinationFile};
				scpJobs.add(clientGetFile);
			}
		}

		//copy the schnitzle
		scpJopsExecutor(scpJobs);

	}

	private static void scpJopsExecutor(List<String[]> scpJobs) {
		//copy the schnitzle
		for (String[] scpJob: scpJobs) {
			try {
				System.out.println("Copying files: "+ scpJob[0] + " "+ scpJob[1] + " "+ scpJob[2]);
				Process p = Runtime.getRuntime().exec(scpJob);
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String result = stdInput.readLine();
				if (result != null)
					System.out.println("***Error scpJopsExecutor: "+result);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	//sshJobs target command
	public static void sshJopsExecutor(List<String[]> sshJobs) {
		for (String[] sshJob: sshJobs) {
			try {
				System.out.println("Running Job: "+ sshJob[0] + " "+ sshJob[1]+ " "+ sshJob[2]);
				Process p = Runtime.getRuntime().exec(sshJob);
				BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String error = stdError.readLine();
				
				BufferedReader stdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String output = stdOutput.readLine();
				
				//BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				
				if (error != null)
					System.out.println("***Error sshJopsExecutor: "+error);
				else
					if (output != null)
						System.out.println("sshJopsExecutor-Output: "+output);
					else
						System.out.println("sshJopsExecutor-Executed: "+sshJob[2]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void deployScripts(String[] ips, String scriptToCopy) {
		List<String[]> scpJobs = new ArrayList<String[]>();
		for (String hostIP: ips) {

			String fileSource = scriptToCopy;
			String fileDestination =hostIP + ":" + ASLJobControlling.workingFolder + ".";

			String[] copyFile = {"scp", fileSource, fileDestination};
			scpJobs.add(copyFile);
		}
		scpJopsExecutor(scpJobs);
	}
	


	static FilenameFilter getFilter(String startsWith, String[] contains) {
		return new FilenameFilter() {

			public boolean accept(File dir, String fileName) {

				boolean result = false;

				if (startsWith != null){
					if (fileName.startsWith(startsWith)) {
						result = true;
					} else {
						return false; //ends here
					}
				}

				if (contains != null){
					for (String cont: contains) {

						if (fileName.contains(cont)) {
							result = true;
						} else {
							return false; //ends here
						}
					}
				}
				return result;
			}
		};
	}

	public static ArrayList<String> getFilesByFilter(final File folder, String startsWith, String[] contains, int expectedNumberOfFiles) {

		ArrayList<String> filesFound = new ArrayList<>();

		String contAsString = "";
		if (contains != null) {
			for (int i = 0; i < contains.length; i++) {
				contAsString += contains[i] +", ";
			}
		}

		File[] filesForAggregation = folder.listFiles(getFilter(startsWith, contains));
		//debugg
		String fname = folder.getAbsolutePath();


		//System.out.println("Found "+filesForAggregation.length +" many files with this filter startsWith="+startsWith +" contains="+contAsString);
		if (filesForAggregation.length !=expectedNumberOfFiles) {
			System.out.println("ERROR expectedNumberOfFiles....="+expectedNumberOfFiles+" vs "+filesForAggregation.length );
		}

		for (final File fileEntry: filesForAggregation) {
			if (fileEntry.isDirectory()) {
				//listFilesForFolder(fileEntry);
				System.out.println("ERROR: I am a directory: "+fileEntry.getAbsoluteFile().toString());
			} else {
				//System.out.println(fileEntry.getName() + " " +fileEntry.getAbsolutePath());

				filesFound.add(fileEntry.getAbsolutePath());

			}
		}

		return filesFound;
	}

	public static ArrayList<Double> calculateStats(ArrayList<Double> values){
		ArrayList<Double> result = new ArrayList<>();

		int nValues = values.size();

		double sum = 0.0;

		// avg
		double avg = 0.0;
		for (double val: values) {
			sum += val;
		}
		avg = sum / nValues;

		// stddev
		double stddev = 0.0;
		for (double val: values) {
			stddev += Math.pow(val-avg, 2);
		}
		stddev = Math.sqrt(stddev / (nValues-1));



		result.add(sum);
		result.add(avg);
		result.add(stddev);


		return result;

	}



	/**
	 * "LOOPs"
	 * 1. create Read-Only stats (then the same for WriteOnly)
	 * 2. _nVC=1_ ... _nVC=32
	 * 		3. Rep=1 .. Rep=3
	 * 			a) 4. get data for all 3 clients and consolidate
	 * 					a) OPs => throughput
	 * 					b) Latency => responsetime
	 * 			b) calculate avg and stddev
	 * 			c) append line in file
	 * 				1) throughput_Baseline21_ReadOnly
	 *  			2) latency_Baseline21_ReadOnly
	 */
	public static void consolidateStatisticsBaseline21() {
		JsonThroughputLatencyConfig conf = new JsonThroughputLatencyConfig();

		conf.experiment = "baseline21/";
		conf.workLoad = new String[] {"ReadOnly", "WriteOnly"};
		conf.workLoadJsonAllStatsKey = new String[] {"Gets", "Sets"};
		conf.workLoadOutputFile = new String[] {experimentsBaseFolder+conf.experiment+"ReadOnly", experimentsBaseFolder+conf.experiment+"WriteOnly"};

		conf.expectedNumberOfFiles = 3; //# files inner loop

		//Labels throughput
		conf.titleLableThroughput = "Throughput vs. Number of clients";
		conf.xlabelLableThroughput = "Number of Clients";
		conf.ylabelLableThroughput = "Throughput (Ops/sec)";
		conf.linetitleLableThroughput = "One memcached server"; // ("workLoad") wird automatisch hizugefügt

		//Labels Latency
		conf.titleLableLatency = "Latency vs. Number of clients";
		conf.xlabelLableLatency = "Number of Clients";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.linetitleLableLatency = "One memcached server"; // ("workLoad") wird automatisch hizugefügt


		//create the stuff
		statisticsThroughputLatencyClientorMiddleware(conf);

	}

	public static void consolidateStatisticsBaseline22() {
		JsonThroughputLatencyConfig conf = new JsonThroughputLatencyConfig();

		conf.experiment = "baseline22/";
		conf.workLoad = new String[] {"ReadOnly", "WriteOnly"};
		conf.workLoadJsonAllStatsKey = new String[] {"Gets", "Sets"};
		conf.workLoadOutputFile = new String[] {experimentsBaseFolder+conf.experiment+"ReadOnly", experimentsBaseFolder+conf.experiment+"WriteOnly"};

		conf.expectedNumberOfFiles = 2; //# files inner loop

		//Labels throughput
		conf.titleLableThroughput = "Throughput vs. Number of clients";
		conf.xlabelLableThroughput = "Number of Clients";
		conf.ylabelLableThroughput = "Throughput (Ops/sec)";
		conf.linetitleLableThroughput = "Two memcached server"; // ("workLoad") wird automatisch hizugefügt

		//Labels Latency
		conf.titleLableLatency = "Latency vs. Number of clients";
		conf.xlabelLableLatency = "Number of Clients";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.linetitleLableLatency = "Two memcached server"; // ("workLoad") wird automatisch hizugefügt


		//create the stuff
		statisticsThroughputLatencyClientorMiddleware(conf);

	}

	/**
	 * one middleware
	 */
	public static void consolidateStatisticsBaseline31() {
		JsonThroughputLatencyConfig conf = new JsonThroughputLatencyConfig();

		conf.experiment = "baseline31/";
		conf.workLoad = new String[] {"ReadOnly", "WriteOnly"};
		conf.workLoadJsonAllStatsKey = new String[] {"Gets", "Sets"};
		conf.workLoadOutputFile = new String[] {experimentsBaseFolder+conf.experiment+"ReadOnly", experimentsBaseFolder+conf.experiment+"WriteOnly"};

		conf.expectedNumberOfFiles = 3; //# files inner loop

		//Labels throughput
		conf.titleLableThroughput = "Throughput vs. Number of clients (memtier data)";
		conf.xlabelLableThroughput = "Number of Clients";
		conf.ylabelLableThroughput = "Throughput (Ops/sec)";
		conf.linetitleLableThroughput = "One Middleware "; // ("workLoad") wird automatisch hizugefügt

		//Labels Latency
		conf.titleLableLatency = "Latency vs. Number of clients (memtier data)";
		conf.xlabelLableLatency = "Number of Clients";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.linetitleLableLatency = "One Middleware "; // ("workLoad") wird automatisch hizugefügt


		//baseline31/32
		conf.nVCsamples = new int[] {1,4,8,12,16,20,24,28,32};
		conf.nWorkerThreadsTodoList = new int[] {8, 16, 32, 64};
		conf.baseline3=true;


		/*
		 * create client stuff
		 */
		statisticsThroughputLatencyClientorMiddleware(conf);

		/*
		 * create midleware stuff
		 */

		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 1; //# files inner loop
		conf.titleLableThroughput = "Throughput vs. Number of clients (middleware data)";
		conf.titleLableLatency = "Latency vs. Number of clients (middleware data)";


		statisticsThroughputLatencyClientorMiddleware(conf);


		/*
		 * queue stuff
		 */
		//more pictures
		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 1; //# files inner loop
		//		("avgQueueLength"); //==throughput
		conf.titleLableThroughput = "avg Queue-Length vs. Number of clients";
		conf.ylabelLableThroughput = "#-clients waiting";

		//		("avgQueueWaitingTime"); //latency
		conf.titleLableLatency = "avg Queue-Waiting-Time vs. Number of clients";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.moreStats = true;
		conf.tp = "avgQueueLength";
		conf.lt = "avgQueueWaitingTime";

		statisticsThroughputLatencyClientorMiddleware(conf);


		/*
		 * service time memcached
		 */
		//more pictures
		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 1; //# files inner loop

		//		("avgMemcachedResponseTimeSet"); //==throughput
		conf.titleLableThroughput = "Average time waiting for memcached vs. Number of clients (set)";
		conf.ylabelLableThroughput = "Latency (ms)";

		//		("avgMemcachedResponseTimeMultiGet"); //latency
		conf.titleLableLatency = "Average time waiting for memcached vs. Number of clients (get)";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.moreStats = true;

		conf.tp = "avgMemcachedResponseTimeSet";
		conf.lt = "avgMemcachedResponseTimeGet";


		statisticsThroughputLatencyClientorMiddleware(conf);

	}

	/**
	 * two middleware
	 */
	public static void consolidateStatisticsBaseline32() {
		JsonThroughputLatencyConfig conf = new JsonThroughputLatencyConfig();

		conf.experiment = "baseline32/";
		conf.workLoad = new String[] {"ReadOnly", "WriteOnly"};
		conf.workLoadJsonAllStatsKey = new String[] {"Gets", "Sets"};
		conf.workLoadOutputFile = new String[] {experimentsBaseFolder+conf.experiment+"ReadOnly", experimentsBaseFolder+conf.experiment+"WriteOnly"};

		conf.expectedNumberOfFiles = 6; //# files inner loop

		//Labels throughput
		conf.titleLableThroughput = "Throughput vs. Number of clients (memtier data)";
		conf.xlabelLableThroughput = "Number of Clients";
		conf.ylabelLableThroughput = "Throughput (Ops/sec)";
		conf.linetitleLableThroughput = "Two Middleware "; // ("workLoad") wird automatisch hizugefügt

		//Labels Latency
		conf.titleLableLatency = "Latency vs. Number of clients (memtier data)";
		conf.xlabelLableLatency = "Number of Clients";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.linetitleLableLatency = "Two Middleware "; // ("workLoad") wird automatisch hizugefügt


		//baseline31/32
		conf.nVCsamples = new int[] {1,4,8,12,16,20,24,28,32};
		conf.nWorkerThreadsTodoList = new int[] {8, 16, 32, 64};
		conf.baseline3=true;


		/*
		 * create client stuff
		 */
		statisticsThroughputLatencyClientorMiddleware(conf);

		/*
		 * create midleware stuff
		 */

		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 2; //# files inner loop
		conf.titleLableThroughput = "Throughput vs. Number of clients (middleware data)";
		conf.titleLableLatency = "Latency vs. Number of clients (middleware data)";


		statisticsThroughputLatencyClientorMiddleware(conf);


		/*
		 * queue stuff
		 */
		//more pictures
		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 2; //# files inner loop

		//		("avgQueueLength"); //==throughput
		conf.titleLableThroughput = "avg Queue-Length vs. Number of clients";
		conf.ylabelLableThroughput = "#-clients waiting";

		//		("avgQueueWaitingTime"); //latency
		conf.titleLableLatency = "avg Queue-Waiting-Time vs. Number of clients";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.moreStats = true;
		conf.tp = "avgQueueLength";
		conf.lt = "avgQueueWaitingTime";

		statisticsThroughputLatencyClientorMiddleware(conf);


		/*
		 * service time memcached
		 */
		//more pictures
		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 2; //# files inner loop

		//		("avgMemcachedResponseTimeSet"); //==throughput
		conf.titleLableThroughput = "Average time waiting for memcached vs. Number of clients (set)";
		conf.ylabelLableThroughput = "Latency (ms)";

		//		("avgMemcachedResponseTimeMultiGet"); //latency
		conf.titleLableLatency = "Average time waiting for memcached vs. Number of clients (get)";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.moreStats = true;

		conf.tp = "avgMemcachedResponseTimeSet";
		conf.lt = "avgMemcachedResponseTimeGet";


		statisticsThroughputLatencyClientorMiddleware(conf);

	}


	public static void consolidateStatisticsfullSystem41() {
		JsonThroughputLatencyConfig conf = new JsonThroughputLatencyConfig();

		conf.experiment = "fullSystem41/";
		conf.workLoad = new String[] {"WriteOnly"};
		conf.workLoadJsonAllStatsKey = new String[] {"Sets"};
		conf.workLoadOutputFile = new String[] {experimentsBaseFolder+conf.experiment+"WriteOnly"};

		conf.expectedNumberOfFiles = 6; //# files inner loop

		//Labels throughput
		conf.titleLableThroughput = "Throughput vs. Number of clients (memtier data)";
		conf.xlabelLableThroughput = "Number of Clients";
		conf.ylabelLableThroughput = "Throughput (Ops/sec)";
		conf.linetitleLableThroughput = "Two Middlewares "; // ("workLoad") wird automatisch hizugefügt

		//Labels Latency
		conf.titleLableLatency = "Latency vs. Number of clients (memtier data)";
		conf.xlabelLableLatency = "Number of Clients";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.linetitleLableLatency = "Two Middlewares "; // ("workLoad") wird automatisch hizugefügt


		//fullsystem41
		conf.nVCsamples = new int[] {1,4,8,12,16,20,24,28,32};
		conf.nWorkerThreadsTodoList = new int[] {8, 16, 32, 64};
		conf.baseline3=true;
		conf.defaultRep = new int[] {1,2,3,4,5,6};


		/*
		 * create client stuff
		 */
		statisticsThroughputLatencyClientorMiddleware(conf);

		/*
		 * create midleware stuff
		 */

		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 2; //# files inner loop
		conf.titleLableThroughput = "Throughput vs. Number of clients (middleware data)";
		conf.titleLableLatency = "Latency vs. Number of clients (middleware data)";

		statisticsThroughputLatencyClientorMiddleware(conf);


		/*
		 * queue stuff
		 */
		//more pictures
		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 2; //# files inner loop

		//		("avgQueueLength"); //==throughput
		conf.titleLableThroughput = "avg Queue-Length vs. Number of clients";
		conf.ylabelLableThroughput = "#-clients waiting";

		//		("avgQueueWaitingTime"); //latency
		conf.titleLableLatency = "avg Queue-Waiting-Time vs. Number of clients";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.moreStats = true;
		conf.tp = "avgQueueLength";
		conf.lt = "avgQueueWaitingTime";

		statisticsThroughputLatencyClientorMiddleware(conf);


		/*
		 * service time memcached
		 */
		//more pictures
		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 2; //# files inner loop

		//		("avgMemcachedResponseTimeSet"); //==throughput
		conf.titleLableThroughput = "Average time waiting for memcached vs. Number of clients (set)";
		conf.ylabelLableThroughput = "Latency (ms)";

		//		("avgMemcachedResponseTimeMultiGet"); //latency
		conf.titleLableLatency = "Average time waiting for memcached vs. Number of clients (multi-Get)";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.moreStats = true;

		conf.tp = "avgMemcachedResponseTimeSet";
		conf.lt = "avgMemcachedResponseTimeMultiGet";


		statisticsThroughputLatencyClientorMiddleware(conf);



	}


	public static void consolidateStatisticsshardedCase51() {
		{
			JsonThroughputLatencyConfig conf = new JsonThroughputLatencyConfig();

			conf.experiment = "shardedCase51/";
			conf.workLoad = new String[] {"ReadOnly"}; //files in diesen Ordner verschieben
			conf.workLoadJsonAllStatsKey = new String[] {"Gets"}; //, "GET"}; //GET is percent //just create one pic.. special case
			conf.workLoadOutputFile = new String[] {experimentsBaseFolder+conf.experiment+"ReadOnly"};

			conf.expectedNumberOfFiles = 6; //# files inner loop


			//Labels Latency
			conf.titleLableLatency = "Latency vs. Multi-get size (Sharded)";
			conf.xlabelLableLatency = "Multi-get size (1:x)";
			conf.ylabelLableLatency = "Latency (ms)";
			conf.linetitleLableLatency = "sharded "; // ("workLoad") wird automatisch hizugefügt


			//special
			//total fucked up
			/*
		conf.nVCsamples = new int[] {2};
		conf.nWorkerThreadsTodoList = new int[] {64}; //prüfe ob korrekt ok gemäss fullsystem4
		conf.multiGets=true;
		conf.baseline3=true;
		conf.multiGetsWorkloads = new String[] {RATIO11, RATIO13, RATIO16, RATIO19};
		conf.defaultRep = new int[] {1,2,3,4,5,6};
			 */
			conf.nVCsamples = new int[] {1,3,6,9};
			conf.nWorkerThreadsTodoList = new int[] {0,25,50,75,90,99}; //steht für avg, 25-percentile, 50-percentile...
			conf.multiGets=true;
			conf.baseline3=true;
			conf.multiGetsWorkloads = new String[] {RATIO11, RATIO13, RATIO16, RATIO19};
			conf.defaultRep = new int[] {1,2,3,4,5,6};
			conf.maxThroughputThread = "_nWorkerThreads64_";

			/*
			 * create client stuff
			 */
			statisticsThroughputLatencyClientorMiddleware(conf);
		}
		cons51Rest("shardedCase51/", "Sharded");

	}

	// ACHTUNG: Mode sollte ich umbenennen da es nicht um middleware etc geht....
	public static void cons51Rest(String caseGet, String mode) {
		JsonThroughputLatencyConfig conf = new JsonThroughputLatencyConfig();

		conf.experiment = caseGet;
		conf.workLoad = new String[] {"ReadOnly"};
		conf.workLoadJsonAllStatsKey = new String[] {"Gets"};
		conf.workLoadOutputFile = new String[] {experimentsBaseFolder+conf.experiment+"ReadOnly"};

		conf.expectedNumberOfFiles = 2; //# files inner loop 2x middleware

		//Labels throughput
		conf.titleLableThroughput = "Throughput vs. Multi-Gets key size";
		conf.xlabelLableThroughput = "key size";
		conf.ylabelLableThroughput = "Throughput (Ops/sec)";
		conf.linetitleLableThroughput = mode+" "; // ("workLoad") wird automatisch hizugefügt

		//Labels Latency
		conf.titleLableLatency = "Latency vs. Multi-Gets key size ";
		conf.xlabelLableLatency = "key size";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.linetitleLableLatency =  mode+" "; // ("workLoad") wird automatisch hizugefügt


		conf.nVCsamples = new int[] {1,3,6,9};
		conf.nWorkerThreadsTodoList = new int[] {64};//{8, 16, 32, 64, 128}; //{64};// 
		conf.baseline3=true;
		conf.defaultRep = new int[] {1,2,3,4,5,6};

		conf.base5Rest = true;
		conf.maxThroughputThread = "64";


		/*
		 * create client stuff
		 */
		//	statisticsThroughputLatencyClientorMiddleware(conf);

		/*
		 * create midleware stuff
		 */

		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 2; //# files inner loop
		//conf.titleLableThroughput = "Throughput vs. Number of clients (middleware data)";
		//conf.titleLableLatency = "Latency vs. Number of clients (middleware data)";

		conf.ySize = 5500;

		statisticsThroughputLatencyClientorMiddleware(conf);

		//if (true)
		//return;


		/*
		 * queue stuff
		 */
		//more pictures
		//.middlewareMode = true;
		//conf.expectedNumberOfFiles = 12; //# files inner loop

		//		("avgQueueLength"); //==throughput
		conf.titleLableThroughput = "avg Queue-Length vs. Multi-Gets key size";
		conf.ylabelLableThroughput = "#-clients waiting";

		//		("avgQueueWaitingTime"); //latency
		conf.titleLableLatency = "avg Queue-Waiting-Time vs. Multi-Gets key size";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.moreStats = true;
		conf.tp = "avgQueueLength";
		conf.lt = "avgQueueWaitingTime";

		conf.ySize = 3;


		statisticsThroughputLatencyClientorMiddleware(conf);


		//anpassen

		/*
		 * service time memcached
		 */
		//more pictures
		conf.middlewareMode = true;
		//conf.expectedNumberOfFiles = 12; //# files inner loop

		//		("avgMemcachedResponseTimeGet"); //==throughput
		conf.titleLableThroughput = "Average time waiting for memcached vs. Multi-Gets key size (SET)";//"Average time waiting for memcached vs. Multi-Gets key size";
		conf.ylabelLableThroughput = "Latency (ms)";

		//		("avgMemcachedResponseTimeMultiGet"); //latency
		conf.titleLableLatency = "Average time waiting for memcached vs. Multi-Gets key size";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.moreStats = true;

		conf.tp = "avgMemcachedResponseTimeSet";
		conf.lt = "avgMemcachedResponseTimeMultiGet";

		conf.ySize = 10;

		conf.ySizeTP = 30;
		conf.useySizeTP = true;


		statisticsThroughputLatencyClientorMiddleware(conf);

		conf.useySizeTP = false;

		//set stuff

		conf.middlewareMode = true;
		//conf.expectedNumberOfFiles = 12; //# files inner loop

		//		("avgMemcachedResponseTimeGet"); //==throughput
		conf.titleLableThroughput = "Throughput vs. Multi-Gets key size (SET)";//"Average time waiting for memcached vs. Multi-Gets key size";
		conf.ylabelLableThroughput = "Throughput (Ops/sec)";

		//		("avgMemcachedResponseTimeMultiGet"); //latency
		conf.titleLableLatency = "Latency vs. Multi-Gets key size (SET)";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.moreStats = true;

		conf.tp = "throughputSet";
		conf.lt = "avgMiddlewareResponseTimeSet";

		conf.ySizeTP = 1000;
		conf.useySizeTP = true;	
		conf.ySize = 20;


		statisticsThroughputLatencyClientorMiddleware(conf);

	}

	public static void consolidateStatisticsnonshardedCase52() {
		JsonThroughputLatencyConfig conf = new JsonThroughputLatencyConfig();

		conf.experiment = "nonshardedCase52/";
		conf.workLoad = new String[] {"ReadOnly"}; //files in diesen Ordner verschieben
		conf.workLoadJsonAllStatsKey = new String[] {"Gets"}; //, "GET"}; //GET is percent //just create one pic.. special case
		conf.workLoadOutputFile = new String[] {experimentsBaseFolder+conf.experiment+"ReadOnly"};

		conf.expectedNumberOfFiles = 6; //# files inner loop


		//Labels Latency
		conf.titleLableLatency = "Latency vs. Multi-get size (Non-sharded)";
		conf.xlabelLableLatency = "Multi-get size (1:x)";
		conf.ylabelLableLatency = "Latency (ms)";
		conf.linetitleLableLatency = "Non-Sharded "; // ("workLoad") wird automatisch hizugefügt


		//special
		//total fucked up
		/*
		conf.nVCsamples = new int[] {2};
		conf.nWorkerThreadsTodoList = new int[] {64}; //prüfe ob korrekt ok gemäss fullsystem4
		conf.multiGets=true;
		conf.baseline3=true;
		conf.multiGetsWorkloads = new String[] {RATIO11, RATIO13, RATIO16, RATIO19};
		conf.defaultRep = new int[] {1,2,3,4,5,6};
		 */
		conf.nVCsamples = new int[] {1,3,6,9};
		conf.nWorkerThreadsTodoList = new int[] {0,25,50,75,90,99}; //steht für avg, 25-percentile, 50-percentile...
		conf.multiGets=true;
		conf.baseline3=true;
		conf.multiGetsWorkloads = new String[] {RATIO11, RATIO13, RATIO16, RATIO19};
		conf.defaultRep = new int[] {1,2,3,4,5,6};
		conf.maxThroughputThread = "_nWorkerThreads64_";

		/*
		 * create client stuff
		 */
		statisticsThroughputLatencyClientorMiddleware(conf);

		cons51Rest("nonshardedCase52/", "Non-Sharded ");

	}






	/**
	 * idee:
	 * subfolders....
	 * 		ReadOnly z.b. klar dass keine gets vorhanden
	 * 		multigets z.B. klar, dass etwas spezielles gemacht werden muss....
	 * 	1. erstelle ein datenfile pro subfolder in den experimenten: z.B. ReadOnly => throughput, latency, queuewaitingtime, etc.... alle möglichen
	 * 	2. falls ReadOnly/Write only etc grafiken nötig sind, dann erstelle die in einem zweiten anlauf indem datenfiles aggregiert werden
	 * 	 * @author fimeier
	 *
	 */
	public static class PlotConfig {
		//wenn fertig als info hinzufügen plot config is just for one picture to make it easier
		//-1 == default

		/**
		 * 
		 * @param _experiment source for data
		 * @param _mode default do memtier and middleware (needed of some data is not available (baseline21 => no middleware)
		 * 
		 * habe hier aufgehört
		 */
		public PlotConfig(String _experiment, String _workLoad, String _mode) {

			experiment = _experiment;

			String workLoad = _workLoad; //"ReadOnly", "WriteOnly"

			workLoadOutputFile = experimentsBaseFolder+experiment+workLoad;

			//set mode
			if (_mode.equals("default")) {
				middlewareMode = true;
				jsonMode = true;
			}
			if (_mode.equals("middleware")) {
				middlewareMode = true;
			}
			if (_mode.equals("json")) {
				jsonMode = true;
			}
		}

		/**
		 * sollte allenfalls nur ein Workload sein
		 */
		String experiment = null;
		String workLoad = null; //"ReadOnly", "WriteOnly"
		String[] workLoadStats = null; //new String[] {"get", "multiget", "set", "queueWaitingTime", ..... nehme Keywords von middleware finalstats

		String workLoadOutputFile = null;// new String[] {experimentsFolder+conf.experiment+"ReadOnly", experimentsFolder+conf.experiment+"WriteOnly"};

		//sollte automatisch klar sein....
		String[] workLoadJsonAllStatsKey = new String[] {"Gets", "Sets"};


		boolean middlewareMode = false;
		boolean jsonMode = false;

		//plot x-y range
		public boolean ySpecialRange = false;
		public int yRangeMin = 0;
		public int yRangeMax = -1;
		public boolean xSpecialRange = false;
		public int xRangeMin = -1;
		public int xRangeMax = -1;

		//Labels
		/**
		 * the title for this plot
		 */
		String titleLable = "";
		String xLabelLable = "";
		String yLabelLable = "";
		/**
		 * the label for this line in the legend
		 */
		String lineTitleLablePrefix = "";







		//Stuff for loops
		int[] nVCsamples = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};
		int[] defaultRep = {1,2,3};

		int expectedNumberOfFiles = -1; //# files inner loop

	}


	public class KeyProperties {
		public String key = "";
		/**
		 * sum or avg... defines how values of 1(!) run are getting aggregated
		 */
		public String aggregationType = "";
		public String yRange = "[0:*]";
		public String xRange = "[0:*]";
		public String keyPosition = "right top";
		public String colorsToUse = "defaultColorsThroughputRead";
		public String xLabel = "default label";
		public String yLabel = "default label";
		public String title = "default title";
		public String[] lineTitles = new String[]{"Line1","Line2","Line3","Line4","Line5", "Line6", "Line7", "Line8"};
		public String linePrefix = "";

		public boolean useScaleYValues = false;
		public ArrayList<Integer> scaleYValues = new ArrayList<>();

		public boolean useFixedScaleYValue = false;
		public double fixedScaleYValue = 0.0;


		//KeyProperties(String _key, String _aggregationType, String yRange, String xRange){
		//key=_key;
		//aggregationType=_aggregationType;
		//}
		/**
		 * optional stuff that can be overridden after creation: yRange, xRange, keyPosition, xLabel, yLabel, title, lineTitles
		 * @param _key
		 * @param _aggregationType avg, sum
		 * @param mainType tp ,latency, length (avgQueuelength)
		 * @param operation get, set, multiget
		 * @param colorsToUse defaultColorsThroughputRead, defaultColorsThroughputWrite, defaultColorsLatencyRead, defaultColorsLatencyWrite
		 * @param _linePrefix Used to create lineTitle: linePrefix + linesToPlotInput(<- loop #wt or similary) (only used if lineTitles still in default state==lineTitle[0].equals("Line1")
		 * 
		 */
		KeyProperties(String _key, String _aggregationType, String _mainType, String _operation, String _colorsToUse, String _linePrefix){
			key=_key;
			aggregationType=_aggregationType;
			linePrefix=_linePrefix;
			//this(_key, _aggregationType, "default", "default");
			//System.out.println("_mainType="+_mainType);
			if (_mainType.equals("tp")) {
				yRange = "[0:*]";
				xRange = "[0:*]";
				keyPosition = "right bottom";
				colorsToUse = _colorsToUse;
				xLabel = "Number of Clients";
				yLabel = "Throughput "+"["+_operation+"/sec]";
				title = "default title";
				lineTitles = new String[]{"Line1","Line2","Line3","Line4","Line5", "Line6", "Line7", "Line8"};
			}
			else if (_mainType.equals("latency")) {
				yRange = "[0:*]";
				xRange = "[0:*]";
				keyPosition = "left top";
				colorsToUse = _colorsToUse;
				xLabel = "Number of Clients";
				yLabel = "Latency "+_operation+" [ms]";
				title = "default title";
				lineTitles = new String[]{"Line1","Line2","Line3","Line4","Line5", "Line6", "Line7", "Line8"};
			}
			else if (_mainType.equals("QueueLength")) {
				yRange = "[0:*]";
				xRange = "[0:*]";
				keyPosition = "left top";
				colorsToUse = _colorsToUse;
				xLabel = "Number of Clients";
				yLabel = "#-clients waiting";
				title = "default title";
				lineTitles = new String[]{"Line1","Line2","Line3","Line4","Line5", "Line6", "Line7", "Line8"};
			}
			else if (_mainType.equals("kb")) {
				yRange = "[0:*]";
				xRange = "[0:*]";
				keyPosition = "right bottom";
				colorsToUse = _colorsToUse;
				xLabel = "Number of Clients";
				yLabel = "Throughput "+_operation+" [KB/sec]";
				title = "default title";
				lineTitles = new String[]{"Line1","Line2","Line3","Line4","Line5", "Line6", "Line7", "Line8"};
			}
			else if (_mainType.equals("histogram")) {
				yRange = "[0:*]";
				xRange = "[0:*]";
				keyPosition = "default";
				colorsToUse = _colorsToUse;
				xLabel = "Buckets in us";
				yLabel = "Number of Clients ("+_operation+") served in this time";
				title = "default title";
				lineTitles = new String[]{"Line1","Line2","Line3","Line4","Line5", "Line6", "Line7", "Line8"};
			}
			else {
				System.out.println("ERROR unknown mainType: "+_mainType);
				colorsToUse = "unknownType";
			}

		}
	}



	public String createAllStatistics(String configAggregation) {//PlotConfig plotconfig) {

		HashMap<String, String> confAggregation = parseConfigAggregation(configAggregation);

		/*
		 * mandatory config
		 */
		String experimentFolder = confAggregation.get("experimentFolder");// "baseline31/";
		String experimentSubfolder = confAggregation.get("experimentSubfolder");//"ReadOnly";//, "WriteOnly"

		String experimentName = confAggregation.get("experimentName"); //baseline31ReadOnly

		String mode = confAggregation.get("mode");
		boolean middlewareMode = (mode.equals("middleware"))? true: false;
		boolean clientMode = (mode.equals("client"))? true: false;
		boolean percentilesMode = (mode.equals("percentiles"))? true: false;
		boolean histogramMode = (mode.equals("histogram"))? true: false;


		//String filterInnerLoopPrefix = 
		String filterInnerLoopPrefix = confAggregation.get("filterInnerLoopPrefix");//"finalStatsBaseline31_ReadOnly";
		int expectedNumberOfFiles = Integer.parseInt(confAggregation.get("expectedNumberOfFiles")); //1-middleware => 1 file


		/*
		 * auto default values
		 */
		//used if no other value has been provided
		String defaultTitle = "Datasource: ../"+experimentFolder+experimentSubfolder;
		if (confAggregation.containsKey("defaultTitle"))
			defaultTitle = confAggregation.get("defaultTitle");

		String aggregationFolder = experimentsBaseFolder+experimentFolder+"aggregation/";
		String aggregatedDataOutputFile = experimentsBaseFolder+experimentFolder+experimentSubfolder+"Aggregated";
		String latexOutputFile = experimentsBaseFolder+experimentFolder+experimentName+"Middleware.tex";
		String gnuPlotConfigOutputFile = experimentsBaseFolder+experimentFolder+"aggregation/PlotConfigMiddleware"+experimentSubfolder;

		if (clientMode) {
			aggregationFolder = experimentsBaseFolder+experimentFolder+"aggregationClient/";
			aggregatedDataOutputFile = experimentsBaseFolder+experimentFolder+experimentSubfolder+"AggregatedClient";
			latexOutputFile = experimentsBaseFolder+experimentFolder+experimentName+"Client.tex";
			gnuPlotConfigOutputFile = experimentsBaseFolder+experimentFolder+"aggregationClient/PlotConfigClient"+experimentSubfolder;
		}
		if (percentilesMode) {
			aggregationFolder = experimentsBaseFolder+experimentFolder+"aggregationPercentiles/";
			aggregatedDataOutputFile = experimentsBaseFolder+experimentFolder+experimentSubfolder+"AggregatedPerentiles";
			latexOutputFile = experimentsBaseFolder+experimentFolder+experimentName+"Percentiles.tex";
			gnuPlotConfigOutputFile = experimentsBaseFolder+experimentFolder+"aggregationPercentiles/PlotConfigPercentiles"+experimentSubfolder;
		}

		int nBuckets = 0;
		int bucketSizeUs = 0;
		//int[] buckets = null;
		boolean histogramMiddleware = false;
		boolean histogramClient = false;
		ArrayList<Double> sumValuesAllBuckets = new ArrayList<>();

		if (histogramMode) {
			histogramMiddleware = confAggregation.get("submode").equals("histogramMiddleware")?true:false;
			histogramClient = confAggregation.get("submode").equals("histogramClient")?true:false;
			String clientSuffix = histogramClient?"Client":"";
			aggregationFolder = experimentsBaseFolder+experimentFolder+"aggregationHistogram/";
			aggregatedDataOutputFile = experimentsBaseFolder+experimentFolder+experimentSubfolder+"AggregatedHistogram"+clientSuffix;
			latexOutputFile = experimentsBaseFolder+experimentFolder+experimentName+"Histogram"+clientSuffix+".tex";
			gnuPlotConfigOutputFile = experimentsBaseFolder+experimentFolder+"aggregationHistogram/PlotConfigHistogram"+experimentSubfolder+clientSuffix;
			nBuckets = Integer.parseInt(confAggregation.get("nBucketsSource"));
			//buckets = new int[nBuckets];
			bucketSizeUs = Integer.parseInt(confAggregation.get("bucketSizeUsSource"));
			histogramMiddleware = confAggregation.get("submode").equals("histogramMiddleware")?true:false;
			histogramClient = confAggregation.get("submode").equals("histogramClient")?true:false;
		}

		//where are the data files... should be okay without changes
		String collectFolderInputString = experimentsBaseFolder+experimentFolder+experimentSubfolder;
		File folderContainingInputData = new File(collectFolderInputString);


		/*
		 * Stuff for loops
		 * 1. x-axis.... main-LOOP: ex #clients, multigetkeysize
		 * 		=> int[] xAxisDataPointsInput
		 * 		=> String[] xAxisFilter (use this as input to filter out the correct files
		 * 2. loop for "line in graph": e.x. #WT, x-percentile
		 * 		=> int[] linesToPlotInput
		 * 		=> String[] linesFilter (use this as input to filter out the correct files
		 * 3. #runs/reps.... 
		 * 		=> int[] numberOfRepetitions
		 * 		=> String[] repetitionFilter (use this as input to filter out the correct files)
		 * 
		 */


		int[] xAxisDataPointsInput = parseIntArray(confAggregation.get("xAxisDataPointsInput"));//{1,4,8,12,16,20,24,28,32}; //change this
		int ratioXaxis = Integer.parseInt(confAggregation.get("ratioXaxis"));//6; //"6*xAxisDataPointsInput"=xvalue change this ex 3 clients with 2 CT = 6
		String xFilterPrefix = confAggregation.get("xFilterPrefix");//"_nVC="; //change this

		/**
		 * used to find correct files for "one" xValue
		 */
		String[] xAxisFilter = new String[xAxisDataPointsInput.length];
		/**
		 * used to calculate x-value
		 * example:
		 */
		int[] xAxis = new int[xAxisDataPointsInput.length];
		for (int i=0; i<xAxisDataPointsInput.length; i++) {
			int x = xAxisDataPointsInput[i];
			xAxisFilter[i] = xFilterPrefix + x;
			xAxis[i] = ratioXaxis*x;

			if(xFilterPrefix.equals("UsefixedXFilterPrefix")) {
				xAxisFilter[i] = confAggregation.get("fixedXFilterPrefix");
			}
		}

		/**
		 * used to find correct files for a specific line
		 */
		String[] linesToPlotInput = parseStringArray(confAggregation.get("linesToPlotInput"));//{8, 16, 32, 64};
		String linesFilterPrefix = confAggregation.get("linesFilterPrefix");//"nWorkerThreads"; //change this

		String[] linesFilter = new String[linesToPlotInput.length];
		for (int i=0; i<linesToPlotInput.length; i++) {
			String line = linesToPlotInput[i];
			linesFilter[i] = linesFilterPrefix + line;
			if (linesFilterPrefix.equals("DoNotUseAnyFilter"))
				linesFilter[i] = "";
			//example for multigets.. if the data for multiple lines is in the same file use a fixed filter
			if (linesFilterPrefix.equals("UseFixedLinesFilterPrefix"))
				linesFilter[i] = confAggregation.get("fixedLinesFilterPrefix");
		}


		int[] numberOfRepetitions = parseIntArray(confAggregation.get("numberOfRepetitions"));//{1,2,3};
		String repetitionFilterPrefix = confAggregation.get("repetitionFilterPrefix");//"_Rep=";

		String[] repetitionFilter = new String[numberOfRepetitions.length];
		for (int i=0; i<numberOfRepetitions.length; i++) {
			int rep = numberOfRepetitions[i];
			repetitionFilter[i] = repetitionFilterPrefix + rep;
		}



		/*
		 * finalStatsBaseline31_ReadOnly _nVC=1nWorkerThreads8_Rep=3_Middleware1
		 * json.txtBaseline31_ReadOnly   _nVC=28nWorkerThreads8_Rep=1_Client1
		 * 
		 * Used in the loop.. compare the containsTemp String
		 * String containsTemp = filterInnerLoopContains.get(i);
		 * String[] contains = {containsTemp}; //add here other strings that should be in the filename....
		 */
		//use this as default and change it if needed (complete for for...
		ArrayList<String> filterInnerLoopContains = new ArrayList<>();
		for (String x: xAxisFilter) {
			for (String line: linesFilter) {
				for (String rep: repetitionFilter) {
					String temp = x + line + rep; //change this if needed
					filterInnerLoopContains.add(temp);
					//System.out.println(temp);
				}
			}
		}



		/*
		 * 	middlewareDataKeys.add(new KeyProperties("throughputGet","sum","tp","get","defaultColorsThroughputRead","#WT="));
		 */
		//KeyProperties(String _key, String _aggregationType, String _mainType, String _operation, String _colorsToUse, String _linePrefix){

		for (String key: confAggregation.get("middlewareDataKeys").split(",")) {
			//System.out.println("Adding middlewareDataKey:"+key+":");
			String _key =key;
			String _aggregationType = confAggregation.get(key+"_aggregationType");
			String _mainType = confAggregation.get(key+"_mainType");
			String _operation = confAggregation.get(key+"_operation");
			String _colorsToUse = confAggregation.get(key+"_colorsToUse");
			String _linePrefix = confAggregation.get(key+"_linePrefix");

			KeyProperties kp = new KeyProperties(_key,_aggregationType,_mainType,_operation,_colorsToUse,_linePrefix);

			//optional stuff: check degfault stuff before other stuff
			if (confAggregation.containsKey("defaultxLabel"))
				kp.xLabel = confAggregation.get("defaultxLabel");

			String[] optionalParameters = {"fixedScaleYValue","scaleYValues","yRange", "xRange", "keyPosition", "xLabel", "yLabel", "title", "lineTitles"};
			for (String o: optionalParameters) {
				String optK = key+"_"+o;

				if (confAggregation.containsKey(optK)) {
					System.out.println("...."+key + " has optK "+optK);
					String value = confAggregation.get(optK);

					switch(o) {
					case "fixedScaleYValue":{
						kp.useFixedScaleYValue = true;
						kp.fixedScaleYValue = Double.parseDouble(confAggregation.get(optK));
						break;
					}
					case "scaleYValues":{
						kp.useScaleYValues = true;
						for (String scaleFactor: confAggregation.get(optK).split(",")) {
							//System.out.println("scaleFactor="+scaleFactor);
							kp.scaleYValues.add(Integer.parseInt(scaleFactor));
						}
						break;
					}
					case "yRange":{
						kp.yRange = value;
						break;
					}
					case "xRange":{
						kp.xRange = value;
						break;
					}
					case "keyPosition":{
						kp.keyPosition = value;
						break;
					}
					case ("xLabel"):{
						kp.xLabel = value;
						break;
					}
					case "yLabel":{
						kp.yLabel = value;
						break;
					}
					case "title":{
						kp.title = value;
						break;
					}
					case "lineTitles":{
						//Lineli 1 #wt8;Lineli 2 #wt28;Lineli 3 #wt38;Lineli 4 #wt48
						int nLines = value.split(";").length;
						String[] lines = new String[nLines];
						for (int i=0; i<nLines; i++) {
							lines[i] = value.split(";")[i];
						}

						kp.lineTitles = lines;
						System.out.println("kp.lineTitles:" +value);
						//kp.lineTitles = null;//value;
						break;
					}

					}
				}
			}
			middlewareDataKeys.add(kp);

		}
		//middlewareDataKeys.add(new KeyProperties("throughputGet","sum","tp","get","defaultColorsThroughputRead","#WTT="));
		//middlewareDataKeys.add(new KeyProperties("avgMiddlewareResponseTimeGet","avg","latency","get","defaultColorsLatencyRead","#WTT="));



		//////////////////////////////////////////////////////////////////////////////////////
		/*
		 * Stuff for loops
		 * 1. x-axis.... main-LOOP: ex #clients, multigetkeysize
		 * 		=> int[] xAxisDataPointsInput
		 * 		=> String[] xAxisFilter (use this as input to filter out the correct files
		 * 2. loop for "line in graph": e.x. #WT, x-percentile
		 * 		=> int[] linesToPlotInput
		 * 		=> String[] linesFilter (use this as input to filter out the correct files
		 * 3. #runs/reps.... 
		 * 		=> int[] numberOfRepetitions
		 * 		=> String[] repetitionFilter (use this as input to filter out the correct files)
		 * 
		 */


		StringBuilder output = new StringBuilder();

		/**
		 * ganze logik ist oben in filterInnerLoopContains drin, weshalb hier viele Werte irrelevant sind		
		 */
		int i = 0;
		int rawXIndex = 0;
		for (int rawX: xAxisDataPointsInput) {

			String outputOneLineInDataFile = xAxis[rawXIndex] +"";

			int linesToPlotRawIndex = 0;
			for (String linesToPlotRaw: linesToPlotInput) {

				ArrayList<ArrayList<Double>> aggregatedRunValuesAllKeys = new ArrayList<>();
				for (int k=0; k<middlewareDataKeys.size(); k++)
					aggregatedRunValuesAllKeys.add(new ArrayList<Double>());

				int repIndex = 0;
				for (int rep: numberOfRepetitions) {

					String containsTemp = filterInnerLoopContains.get(i);
					//add here other strings that should be in the filename....
					String[] contains = {containsTemp};
					ArrayList<String> filesFoundMiddleware = getFilesByFilter(folderContainingInputData, filterInnerLoopPrefix, contains, expectedNumberOfFiles);

					/*
					 * get all values for a run for all keys
					 * a run consists of probably multiple values, because there can be for example multiple clients
					 */
					//List for all Keys that contains per Key a List with all values from all runs
					ArrayList<ArrayList<Double>> runValuesAllKeys = new ArrayList<>();
					for (int k=0; k<middlewareDataKeys.size(); k++)
						runValuesAllKeys.add(new ArrayList<Double>());

					for (String absoluteFilenameMiddleware: filesFoundMiddleware) {

						HashMap<String,Double> extractedDataMiddleware = null;
						if (middlewareMode)
							extractedDataMiddleware = middlewareGetFinalStats(absoluteFilenameMiddleware);
						else if (clientMode)
							extractedDataMiddleware = jsonAllStats(absoluteFilenameMiddleware);
						else if (percentilesMode) {
							//normally just one key
							for (int k=0; k<middlewareDataKeys.size(); k++) {
								String key = middlewareDataKeys.get(k).key;
								//for example  getPercentiles(latencyGet,0.75) to get the 75th percentile for latencyGet
								double value = getPercentiles(absoluteFilenameMiddleware, key, linesToPlotRaw);
								runValuesAllKeys.get(k).add(value);
							}
							continue;
						}
						else if (histogramMode) {
							//normally just one key
							for (int k=0; k<middlewareDataKeys.size(); k++) {
								if (sumValuesAllBuckets.size()<k+1) {
									sumValuesAllBuckets.add(0.0);
								}
								/**
								 * todo......
								 * parse the file and add each column the array
								 * sollte dann automatisch ausgerechnet werden...
								 * werte auf xAxis geben an, welche Buckets existieren...
								 * mit der #buckets und buucketsize kann ausgerechnet werden, wieviele Werte bereits vor dem speichern in die Liste aggregiert werden müssen
								 * die gespeicherten werte in der liste 
								 * 
								 */
								String key = middlewareDataKeys.get(k).key;
								int mw = (absoluteFilenameMiddleware.contains("Middleware1")?1:2);
								int rawXIndexOld = (rawXIndex!=0)?rawXIndex-1:0;
								int rawXold = xAxisDataPointsInput[rawXIndexOld];
								//System.out.println("buckets for ("+rawXold+","+rawX + "] filterInnerLoopPrefix="+ filterInnerLoopPrefix+" containsTemp="+ containsTemp + " MW="+mw);

								//contains the stats for one file (aggregated buckets)
								ArrayList<Double> statsBucket = null;
								if(histogramMiddleware) {
									statsBucket = parseBucketsMiddleware(absoluteFilenameMiddleware, key, rawXold, rawX);

								}
								if(histogramClient) {
									statsBucket = parseBucketsClient(absoluteFilenameMiddleware, key, rawXold, rawX);
								}

								double sumValues = statsBucket.get(0);
								double avgValues = statsBucket.get(1);
								//double stdevValues = statsBucket.get(2);
								//for debuggin and to figure out how many ops are in this histogram to manually scale client data

								sumValuesAllBuckets.set(k, sumValuesAllBuckets.get(k)+sumValues);
								//System.out.println(sumValues);

								String aggregationType = middlewareDataKeys.get(k).aggregationType;
								double valueBucketAggregated = 0.0;

								if (aggregationType.equals("sum")) {
									valueBucketAggregated = sumValues;
								}
								else if (aggregationType.equals("avg")) {
									valueBucketAggregated = avgValues;
								}
								else {
									System.out.println("ERROR valueRunAggregated.....");
								}


								if (middlewareDataKeys.get(k).useFixedScaleYValue) {
									valueBucketAggregated *= middlewareDataKeys.get(k).fixedScaleYValue;
								}

								runValuesAllKeys.get(k).add(valueBucketAggregated);
							}
							continue;
						}


						for (int k=0; k<middlewareDataKeys.size(); k++) {
							String key = middlewareDataKeys.get(k).key;

							/**special case to aggregate multi-gets
							 * Needed because the middleware output counts
							 * 		#gets (==single key) [ops/sec]
							 * 		#multigets in [ops/sec]
							 * 		#getkeys == effective throughput in [keys/sec]
							 * 
							 * 		=> PROBLEM: "1:1 multigets" gives a datapoint for get
							 * 					1:3 etc gives a datapoint for mutligets
							 * 		=>>>>>> gets has just one datapoint for "x=1"
							 * 				multigets has no datapoint for x=1
							 * 
							 */
							if (key.equals("avgMiddlewareResponseTimeAllKeys")) {
								if (rawX==1)
									key = "avgMiddlewareResponseTimeGet";
								else
									key = "avgMiddlewareResponseTimeMultiGet";
							}
							if (key.equals("avgMemcachedResponseTimeAllKeys")) {
								if (rawX==1)
									key = "avgMemcachedResponseTimeGet";
								else
									key = "avgMemcachedResponseTimeMultiGet";
							}
							if (key.equals("avgWorkerThreadResponseTimeAllKeys")) {
								if (rawX==1)
									key = "avgWorkerThreadResponseTimeGet";
								else
									key = "avgWorkerThreadResponseTimeMultiGet";
							}

							//scaleYValues = new ArrayList<>();
							double scaleY = 1;
							if (middlewareDataKeys.get(k).useScaleYValues) {
								scaleY = middlewareDataKeys.get(k).scaleYValues.get(rawXIndex);
								//System.out.println("scaleY="+scaleY+" for key="+middlewareDataKeys.get(k).key+" for x="+rawXIndex);
							}

							double value  = extractedDataMiddleware.get(key)*scaleY;
							runValuesAllKeys.get(k).add(value);
						}
					}

					/*
					 * aggregate a run according to its rules for all keys
					 */
					for (int k=0; k<middlewareDataKeys.size(); k++) {
						ArrayList<Double> allStats = calculateStats(runValuesAllKeys.get(k));
						double sumValues = allStats.get(0);
						double avgValues = allStats.get(1);
						//double stdevValues = allStats.get(2);

						String aggregationType = middlewareDataKeys.get(k).aggregationType;
						double valueRunAggregated = 0.0;

						if (aggregationType.equals("sum")) {
							valueRunAggregated = sumValues;
						}
						else if (aggregationType.equals("avg")) {
							valueRunAggregated = avgValues;
						}
						else {
							System.out.println("ERROR valueRunAggregated.....");
						}

						aggregatedRunValuesAllKeys.get(k).add(valueRunAggregated);
					}


					i++;
					repIndex++;
				}//end of rep-loop

				//aggregate all runs and calculate avg and stddev per key
				for (int k=0; k<middlewareDataKeys.size(); k++) {
					ArrayList<Double> allStats = calculateStats(aggregatedRunValuesAllKeys.get(k));
					//double sumValues = allStats.get(0);
					double avgValues = allStats.get(1);
					double stdevValues = allStats.get(2);

					//add the runs if needed for plotting
					for (double valueRun: aggregatedRunValuesAllKeys.get(k))
						outputOneLineInDataFile += " " + String.format("%.2f", valueRun);

					outputOneLineInDataFile += " " + String.format("%.2f", avgValues) + " " + String.format("%.2f", stdevValues);
				}			
				linesToPlotRawIndex++;
			}//end of linesToPlotRaw-loop

			output.append(outputOneLineInDataFile);
			output.append("\n");

			rawXIndex++;
		}//end of rawX-loop

		//System.out.println(output.toString());

		//write the data
		writeFile(aggregatedDataOutputFile,output.toString());


		/*
		 * BEGIN gnuPlotConfig
		 * allenfalls auslagern bzw anpassen wenn variablen oben ausgelagert werden
		 * 
		 */
		//create the plot configs
		String[] allgnuPlotConfigs = new String[middlewareDataKeys.size()];
		String allgnuPlotConfigsAsOneFile = "";


		ArrayList<String> pdfOutputFiles = new ArrayList<>();

		String allLines = "";

		//do this per key
		for (int k=0; k<middlewareDataKeys.size(); k++) {

			String gnuPlotConfig = "";

			//outputfile
			gnuPlotConfig += "set terminal pdf \n";
			String pdfFileName = aggregationFolder+middlewareDataKeys.get(k).key+".pdf";

			gnuPlotConfig += "set output \""+pdfFileName+"\" \n";
			pdfOutputFiles.add(pdfFileName);


			/* BEGIN ranges
			 * search for special ranges for this key
			 */
			String yRange = middlewareDataKeys.get(k).yRange;
			String xRange = middlewareDataKeys.get(k).xRange;

			gnuPlotConfig += "set xrange "+xRange+" \n";
			gnuPlotConfig += "set yrange "+yRange+" \n";
			//gnuPlotConfig += "set xtics 1 \n";
			//END RANGES


			String keyPosition = middlewareDataKeys.get(k).keyPosition;
			gnuPlotConfig += "set key "+keyPosition+" \n";


			//Colors
			String colorsToUse = middlewareDataKeys.get(k).colorsToUse;
			String ps = "0.5";


			String[] defaultColors = null;

			if (colorsToUse.equals("defaultColorsThroughputRead"))
				defaultColors = defaultColorsThroughputRead;
			else if (colorsToUse.equals("defaultColorsThroughputWrite"))
				defaultColors = defaultColorsThroughputWrite;
			else if (colorsToUse.equals("defaultColorsLatencyRead"))
				defaultColors = defaultColorsLatencyRead;
			else if (colorsToUse.equals("defaultColorsLatencyWrite"))
				defaultColors = defaultColorsLatencyWrite;
			else {
				System.out.println("ATTENTION: Using defaultColors for "+middlewareDataKeys.get(k).key+"....");
				defaultColors = unknownTypeColors;
			}



			//multiline case or single line
			for(int lineN=1; lineN<= linesToPlotInput.length; lineN++) {
				String color = defaultColors[lineN-1];
				gnuPlotConfig += "set style line "+lineN+" lc rgb '"+color+"' lt 1 lw 2 pt 7 ps "+ps+" \n"; 
			}


			//Labels
			String xLabel = middlewareDataKeys.get(k).xLabel;
			String yLabel = middlewareDataKeys.get(k).yLabel;
			String title = middlewareDataKeys.get(k).title;
			if (title.equals("default title")) {
				title=defaultTitle;
			}

			gnuPlotConfig += "set xlabel \""+xLabel+"\" \n";
			gnuPlotConfig += "set ylabel \""+yLabel+"\" \n";
			gnuPlotConfig += "set title \""+title+"\" \n";


			//multiple lines //offset hinzu für reps
			//	int firstDataPointInRow = numberOfRepetitions.length + 1;
			int datapointOffset = numberOfRepetitions.length + 2; //+2 for avg stddev: rep1 rep2 rep3 avg stdev rep1 rep2 rep3 avg stdev
			int lineOffset = datapointOffset*middlewareDataKeys.size();

			//specific for per key
			int keyOffset = datapointOffset*k;


			String[] lineTitles = middlewareDataKeys.get(k).lineTitles;
			if (lineTitles[0].equals("Line1")) {
				for (int x=0; x<linesToPlotInput.length;x++) {
					lineTitles[x]=middlewareDataKeys.get(k).linePrefix+linesToPlotInput[x];
				}
			}


			for(int lineN=1; lineN<= linesToPlotInput.length; lineN++) {
				int avgValuePosition = lineOffset*(lineN-1) + keyOffset;
				//if (lineN==1) {
				avgValuePosition += datapointOffset; //should point to first avg in row
				//}

				int stddevValuePosition = avgValuePosition + 1;

				String dataPosition= "1:"+avgValuePosition+":"+stddevValuePosition;

				String id = getUniqueLineIdentifier();
				String idString = "";
				if(showIdInLineTitle)
					idString = "(ID="+id+") ";

				if (lineN==1) {
					//String defaultDataPos = "1:"+(datapointOffset+keyOffset)+":"+(datapointOffset+keyOffset+1);
					gnuPlotConfig += "plot \""+aggregatedDataOutputFile+"\" using "+dataPosition+"  with errorlines title \""+idString+lineTitles[lineN-1]+"\" ls "+lineN+" ";
				} else {
					//int avgCol = 5+datapointOffset + (5+datapointOffset)*(lineN-1);
					//avgCol = lineOffset+keyOffset;
					//int stdCol = avgCol + 1;
					//String dataPosition= "1:"+avgCol+":"+stdCol;
					gnuPlotConfig += ", \""+aggregatedDataOutputFile+"\" using "+dataPosition+"  with errorlines title \""+idString+lineTitles[lineN-1]+"\" ls "+lineN+" ";
				}

				//used to consolidate multiple plots
				//allLines  += "id:="+ id+" experimentName:="+experimentName + " datafile:="+aggregatedDataOutputFile +" dataPosition:=" +dataPosition + " color:="+defaultColors[lineN-1]+ " lineTitles:="+lineTitles[lineN-1] +"\n";

				String key= middlewareDataKeys.get(k).key;
				allLines  += id+":="+experimentName 
						+ ":="+aggregatedDataOutputFile 
						+":=" +dataPosition
						+ ":="+defaultColors[lineN-1]
								+ ":="+lineTitles[lineN-1]
										+":="+key
										+":="+mode+"\n";

			}


			//System.out.println(gnuPlotConfig);

			allgnuPlotConfigs[k] = gnuPlotConfig;
			allgnuPlotConfigsAsOneFile += gnuPlotConfig + "\n";

		}//END gnuPlotConfig

		/*System.out.println("allLines****************************************************");
		System.out.println(allLines);
		System.out.println("allLines****************************************************");*/

		writeFile(allLinesOutputFolderSource+getallLinesFileID()+experimentName,allLines);


		//write gnuPlotConfig
		writeFile(gnuPlotConfigOutputFile,allgnuPlotConfigsAsOneFile);

		//System.out.println("gnuplot -c "+gnuPlotConfigOutputFile);
		ArrayList<String> keys = new ArrayList<>();
		for (int k=0; k<middlewareDataKeys.size(); k++) {
			keys.add(middlewareDataKeys.get(k).key);
		}
		createLatexInclude(pdfOutputFiles,latexOutputFile, keys, mode+": "+experimentName);

		if (histogramMode) {
			for (int k=0; k<middlewareDataKeys.size(); k++)
				//Werte machen bei avg keinen Sinn
				System.out.println("Histogram for "+mode+":"+experimentName+":"+middlewareDataKeys.get(k).key+": sumValuesAllBuckets="+sumValuesAllBuckets.get(k));
		}
		return gnuPlotConfigOutputFile+" "+latexOutputFile;
	}

	private ArrayList<Double> parseBucketsClient(String absoluteFilename, String bucketOpToExctract, int bucketLowIndex, int bucketHighIndex) {


		String command = "";
		if (bucketOpToExctract.equals("histogramMultiGetLatencyClient"))
			command = "GET";
		else if (bucketOpToExctract.equals("histogramSetLatencyClient"))
			command = "SET";
		else
			System.out.println("ERROR unknown bucketOpToExctract="+bucketOpToExctract);

		double lowIndexInMs = (double) bucketLowIndex / 1000.;
		double highIndexInMs = (double) bucketHighIndex / 1000.;


		ArrayList<Double> values = getClientHistogramData(absoluteFilename, command, lowIndexInMs, highIndexInMs);




		return calculateStats(values);
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @param absoluteFilename
	 * @param key
	 * @param bucketLowIndex (bucketLowIndex, bucketHighIndex]
	 * @param bucketHighIndex (bucketLowIndex, bucketHighIndex]
	 * @return 
	 */
	private ArrayList<Double> parseBucketsMiddleware(String absoluteFilename, String bucketOpToExctract, int bucketLowIndex, int bucketHighIndex) {
		/*
		 * 	BEGIN: Histogram (response time <;#-Get;#-MultiGet;#-Set)
			histogramOutOfScopeNumber=56
			histogramOutOfScopeRatio=0.0018247580566326696
			END: Histogram (#-out of scoope requests=56, 0.0018247580566326696%, buckets for [0ms, 1000ms])


			(];"+nGet+";"+nMultiGet+";"+nSet
			100;0;0;0 
			200;0;0;0 
			300;0;0;2 
			400;0;0;106 
			500;0;37;669 
			600;0;237;1154 
			700;0;749;1548 
			800;0;1123;1684 
			900;0;1501;1495 
			1000;0;1455;1138 
			1100;0;1287;787 
			1200;0;937;628 
		 */
		//
		ArrayList<Double> values = new ArrayList<>();
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(absoluteFilename));
			HashMap<String,String> notKey = new HashMap<>();
			notKey.put("BEGIN:",""); notKey.put("END:",""); notKey.put("histogramOutOfScopeNumber",""); notKey.put("histogramOutOfScopeRatio","");
			while(fileReader.ready()) {
				String line = fileReader.readLine().replace("=", " ");
				String key = line.split(" ")[0];
				if(notKey.containsKey(key))
					continue;

				//line==(700];0;749;1548
				//(];"+nGet+";"+nMultiGet+";"+nSet

				String[] valueString = line.replace(" ", "").split(";");
				double bucket = Double.parseDouble(valueString[0]);
				double nGet = Double.parseDouble(valueString[1]);
				double nMultiGet = Double.parseDouble(valueString[2]);
				double nSet = Double.parseDouble(valueString[3]);

				double val = 0.0;

				//decide if bucket should be used
				//corner case: first bucket
				boolean correctBucket = false;
				if (bucketLowIndex==bucketHighIndex && bucket<=bucketHighIndex)
					correctBucket=true;
				if (bucketLowIndex<bucket && bucket<=bucketHighIndex)
					correctBucket=true;
				if (!correctBucket)
					continue;

				if (bucketOpToExctract.equals("histogramMultiGetLatency"))
					val = nMultiGet;
				else if (bucketOpToExctract.equals("histogramSetLatency"))
					val = nSet;
				else
					System.out.println("ERROR unknown bucketOpToExctract="+bucketOpToExctract);

				values.add(val);				
				//System.out.println("using for "+line +" value="+val);
				//parsedData.put(key, value);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return calculateStats(values);
	}

	public static void createLatexInclude(ArrayList<String> pdfOutputFiles, String latexOutputFile, ArrayList<String> keys, String texHeader) {

		/*
		 * create latexInclude
		 */
		String header =  "\\fancyhead[C]{"+texHeader+"}";

		String latex = "\\documentclass[11pt,a4paper]{article}\n" + 
				"\n" + 
				"\\usepackage{fullpage}\n" + 
				"\\usepackage{hyperref}\n" + 
				"\\usepackage{graphicx}\n" + 
				"\\usepackage{caption}\n" + 
				"\\usepackage{subcaption}\n" + 
				"\\usepackage{graphics} \n" + 
				"\n" + 
				"\n" + 
				"\\usepackage{fancyhdr}\n" + 
				"\\pagestyle{fancy}\n" + 
				"\\fancyhf{}\n" + 
				"\\usepackage{todonotes}\n" + 
				"\n" + 
				"\\renewcommand{\\headrulewidth}{0pt}\n" + 
				"\\renewcommand{\\footrulewidth}{0pt}\n" + 
				"\n" + 
				"\n" + 
				"\\begin{document}\n";

		int picsPerPage = 0;


		int pageN = 0;
		int k = 0;
		for (String pdf: pdfOutputFiles) {
			picsPerPage++;
			String key = keys.get(k);
			switch(picsPerPage) {
			case 1:{
				latex += header +
						"\\begin{figure}\n" + 
						"	\\begin{subfigure}[t]{.5\\textwidth}\n" + 
						"		\\centering\n" + 
						"		\\includegraphics[width=\\linewidth]{"+pdf+"}\n" + 
						"		\\caption{"+key+"}\n" + 
						"	\\end{subfigure}\n" + 
						"	\\hfill\n";
				break;
			}
			case 2:{
				latex += "\\begin{subfigure}[t]{0.5\\textwidth}\n" + 
						"		\\centering\n" + 
						"		\\includegraphics[width=\\linewidth]{"+pdf+"}\n" + 
						"		\\caption{"+key+"}\n" + 
						"	\\end{subfigure}\n" + 
						"	\n" + 
						"	\\medskip\n";
				break;
			}
			case 3:{
				latex += "	\\begin{subfigure}[t]{.5\\textwidth}\n" + 
						"		\\centering\n" + 
						"		\\includegraphics[width=\\linewidth]{"+pdf+"}\n" + 
						"		\\caption{"+key+"}\n" + 
						"	\\end{subfigure}\n" + 
						"	\\hfill\n";
				break;
			}
			case 4:{
				latex += "\\begin{subfigure}[t]{.5\\textwidth}\n" + 
						"		\\centering\n" + 
						"		\\includegraphics[width=\\linewidth]{"+pdf+"}\n" + 
						"		\\caption{"+key+"}\n" + 
						"	\\end{subfigure}\n" +
						"	\n" + 
						"	\\medskip\n";
				;
				break;
			}
			case 5:{
				latex += "	\\begin{subfigure}[t]{.5\\textwidth}\n" + 
						"	\\centering\n" + 
						"		\\includegraphics[width=\\linewidth]{"+pdf+"}\n" + 
						"		\\caption{"+key+"}\n" + 
						"\\end{subfigure}\n" + 
						"\\hfill\n" + 
						"";
				break;
			}
			case 6:{
				latex += "\\begin{subfigure}[t]{0.5\\textwidth}\n" + 
						"	\\centering\n" + 
						"		\\includegraphics[width=\\linewidth]{"+pdf+"}\n" + 
						"		\\caption{"+key+"}\n" + 
						"\\end{subfigure}\n" + 
						"\n" + 
						"\\medskip\n" + 
						"\n" + 
						"";
				break;
			}
			case 7:{
				latex += "\\begin{subfigure}[t]{.5\\textwidth}\n" + 
						"	\\centering\n" + 
						"		\\includegraphics[width=\\linewidth]{"+pdf+"}\n" + 
						"		\\caption{"+key+"}\n" + 
						"\\end{subfigure}\n" + 
						"\\hfill\n";
				break;
			}
			case 8:{
				latex += "\\begin{subfigure}[t]{.5\\textwidth}\n" + 
						"	\\centering\n" + 
						"		\\includegraphics[width=\\linewidth]{"+pdf+"}\n" + 
						"		\\caption{"+key+"}\n" + 
						"\\end{subfigure}\n" + 
						"\n" + 
						"\\end{figure}\n" + 
						"\n\\newpage\n";
				picsPerPage = 0;
				pageN++;
				break;
			}
			}
			k++;
		}

		if (pdfOutputFiles.size()%8!=0)
			latex += "\\end{figure}\n";
		latex += "\\end{document}\n";

		//System.out.println("latex.......................");
		//System.out.println(latex);

		writeFile(latexOutputFile,latex);

		//return gnuPlotConfigOutputFile+" "+latexOutputFile;
	}



	/**
	 * read out the CDF.... not finished
	 * @param object
	 * @param command SET or GET
	 */
	public static double getPercentiles(String dataFile, String _command, String linesToPlotRaw) {
		Reader fileReader = null;
		try {
			fileReader = new BufferedReader(new FileReader(dataFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonReader jsonReader = Json.createReader(fileReader);
		JsonObject object = jsonReader.readObject();

		String command = "";
		if (_command.startsWith("GET")) { //GETpercentilesLatency
			command = "GET";
			if (linesToPlotRaw.equals("avg")&&_command.equals("GETpercentilesLatency")) {
				double msec = Double.valueOf(object.getJsonObject("ALL STATS").getJsonObject("Gets").get("Latency").toString());
				return msec;
			}
		}



		JsonArray jsonArrayToExtractfrom = object.getJsonObject("ALL STATS").getJsonArray(command);

		double percentile =Double.valueOf(linesToPlotRaw);
		double value = 0.0;

		for(int i = 0; i<jsonArrayToExtractfrom.size(); i++) {
			double msec = Double.valueOf(jsonArrayToExtractfrom.get(i).asJsonObject().get("<=msec").toString());
			double perc =Double.valueOf(jsonArrayToExtractfrom.get(i).asJsonObject().get("percent").toString());
			if (perc>=percentile) {
				//System.out.println(percentile+"th Found: "+msec+ " and "+ perc);
				return msec;
			}
			//System.out.println(percentile+"th NOOO SMALL: "+msec+ " and "+ perc);
			value = msec;
		}
		return value;
	}


	/**
	 * keys
	 * throughputGet,latencyGet
	 * throughputSet,latencySet
	 * throughputTotal,latencyTotal
	 * KBget, KBset, KBtotal
	 * @param absoluteFilenameMiddleware
	 * @return
	 */
	private HashMap<String, Double> jsonAllStats(String dataFile) {
		HashMap<String,Double> extractedData = new HashMap<>();
		try {
			Reader fileReader = new BufferedReader(new FileReader(dataFile));
			JsonReader jsonReader = Json.createReader(fileReader);
			JsonObject object = jsonReader.readObject();


			//Sets, Gets or Totals

			/*
			 * Gets
			 * throughputGet,latencyGet, KBget
			 */
			{
				String cmd = "Gets";
				String ops = "throughputGet";
				String latency = "latencyGet";
				String kb = "KBget";
				JsonObject jsonObjectToExtractfrom = object.getJsonObject("ALL STATS").getJsonObject(cmd);

				extractedData.put(ops, Double.valueOf(jsonObjectToExtractfrom.get("Ops/sec").toString()));
				//extractedData.put("Hits", Double.valueOf(jsonObjectToExtractfrom.get("Hits/sec").toString()));
				//extractedData.put("Misses", Double.valueOf(jsonObjectToExtractfrom.get("Misses/sec").toString()));
				extractedData.put(latency, Double.valueOf(jsonObjectToExtractfrom.get("Latency").toString()));
				extractedData.put(kb, Double.valueOf(jsonObjectToExtractfrom.get("KB/sec").toString()));
			}
			/*
			 * Sets
			 * throughputSet,latencySet, KBset
			 */
			{
				String cmd = "Totals";
				String ops = "throughputTotal";
				String latency = "latencyTotal";
				String kb = "KBtotal";
				JsonObject jsonObjectToExtractfrom = object.getJsonObject("ALL STATS").getJsonObject(cmd);

				extractedData.put(ops, Double.valueOf(jsonObjectToExtractfrom.get("Ops/sec").toString()));
				//extractedData.put("Hits", Double.valueOf(jsonObjectToExtractfrom.get("Hits/sec").toString()));
				//extractedData.put("Misses", Double.valueOf(jsonObjectToExtractfrom.get("Misses/sec").toString()));
				extractedData.put(latency, Double.valueOf(jsonObjectToExtractfrom.get("Latency").toString()));
				extractedData.put(kb, Double.valueOf(jsonObjectToExtractfrom.get("KB/sec").toString()));
			}
			/*
			 * Sets
			 * throughputSet,latencySet, KBset
			 */
			{
				String cmd = "Sets";
				String ops = "throughputSet";
				String latency = "latencySet";
				String kb = "KBset";
				JsonObject jsonObjectToExtractfrom = object.getJsonObject("ALL STATS").getJsonObject(cmd);

				extractedData.put(ops, Double.valueOf(jsonObjectToExtractfrom.get("Ops/sec").toString()));
				//extractedData.put("Hits", Double.valueOf(jsonObjectToExtractfrom.get("Hits/sec").toString()));
				//extractedData.put("Misses", Double.valueOf(jsonObjectToExtractfrom.get("Misses/sec").toString()));
				extractedData.put(latency, Double.valueOf(jsonObjectToExtractfrom.get("Latency").toString()));
				extractedData.put(kb, Double.valueOf(jsonObjectToExtractfrom.get("KB/sec").toString()));
			}



			jsonReader.close();
			fileReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return extractedData;
	}

	public static class JsonThroughputLatencyConfig{
		public int ySizeTP = 0;
		public int ySize;
		public boolean base5Rest;
		public String lt;
		public String tp;
		public boolean moreStats;
		public String maxThroughputThread = "";
		boolean multiGets = false;
		String[] multiGetsWorkloads = null; 

		boolean middlewareMode = false;

		String experiment = "";
		String[] workLoad = null;
		String[] workLoadJsonAllStatsKey = null;
		String[] workLoadOutputFile = null;


		//Labels throughput
		String titleLableThroughput = "";
		String xlabelLableThroughput = "";
		String ylabelLableThroughput = "";
		String linetitleLableThroughput = "";

		//Labels Latency
		String titleLableLatency = "";
		String xlabelLableLatency = "";
		String ylabelLableLatency = "";
		String linetitleLableLatency = "";


		//baseline31/32 adaptions
		//defaultvalue
		int[] nVCsamples = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};
		int[] nWorkerThreadsTodoList = null;
		boolean baseline3 = false;

		int[] defaultRep = {1,2,3};



		int expectedNumberOfFiles = -1; //# files inner loop		
		public boolean useySizeTP = false;
	}



	public static void statisticsThroughputLatencyClientorMiddleware(JsonThroughputLatencyConfig conf) {
		String experiment = conf.experiment;
		String[] workLoad = conf.workLoad;
		String[] workLoadJsonAllStatsKey = conf.workLoadJsonAllStatsKey;
		String[] workLoadOutputFile = conf.workLoadOutputFile;


		int expectedNumberOfFiles = conf.expectedNumberOfFiles; //# files inner loop

		List<String> gnuplotConfigFiles = new LinkedList<>();
		List<String> pictureOutputName = new LinkedList<>();




		for (int wl=0; wl<workLoad.length; wl++) {

			String collectFolder = experimentsBaseFolder+experiment+workLoad[wl];
			File folder = new File(collectFolder); 


			String startsWith = "json.txt";
			if (conf.middlewareMode)
				startsWith = "finalStats";


			String outputThroughput = "";
			String outputResponseTime = "";

			//default 1-32
			for (int nVC: conf.nVCsamples) {


				// 3 clients with 2 CT
				int NumClients = 3*2*nVC;

				String outputThroughputLine = NumClients +"";
				String outputResponseTimeLine = NumClients + "";

				if (conf.multiGets || conf.base5Rest) {
					outputThroughputLine = nVC+"";
					outputResponseTimeLine = nVC+"";
				}




				//zusätzlicher nWorkerThreads loop....
				//no effect if list empty
				int [] nWorkThreadsList = (conf.nWorkerThreadsTodoList!=null) ? conf.nWorkerThreadsTodoList: new int[] {1};
				for (int nWorkerThreads: nWorkThreadsList) {

					ArrayList<Double> valuesOpsAllRunsAggregated = new ArrayList<>();
					ArrayList<Double> valuesLatencyAllRunsAggregated = new ArrayList<>();




					//default 1,2,3
					for (int rep: conf.defaultRep) {

						String[] contains = new String[] {"Rep="+rep,"_nVC="+nVC+"_"}; 

						if (conf.baseline3) {
							//conf.nVCsamples
							//conf.nWorkerThreadsTodoList
							//wrong file names.... forgott _ _nVC=2nWorkerThreads8 is 1! filter
							//int nWorkerThreads = 8;
							contains = new String[] {"Rep="+rep,"_nVC="+nVC+"nWorkerThreads"+nWorkerThreads}; 
						}

						if (conf.multiGets || conf.base5Rest) {
							/*neu das multiGets
							 * json.txtshardedCase51ratio=1:3_nWorkerThreads8_Rep=5_Client2
							 * _nWorkerThreads8_ korrekt bzw sowieso fix kann ignoriert werden...
							 * aber ratio=1:3_ asl aggeg
							 * 
							 */
							contains = new String[] {"Rep="+rep,"ratio=1:"+nVC, conf.maxThroughputThread}; 
						}
						if (conf.base5Rest) {
							/*neu das multiGets
							 * json.txtshardedCase51ratio=1:3_nWorkerThreads8_Rep=5_Client2
							 * _nWorkerThreads8_ korrekt bzw sowieso fix kann ignoriert werden...
							 * aber ratio=1:3_ asl aggeg
							 * 	finalStatsshardedCase51ratio=1:3_nWorkerThreads64_Rep=2_Middleware1

							 */
							contains = new String[] {"Rep="+rep,"ratio=1:"+nVC, "_nWorkerThreads"+nWorkerThreads+"_"}; 
						}


						ArrayList<String> filesFound = getFilesByFilter(folder, startsWith, contains, expectedNumberOfFiles);

						ArrayList<Double> valuesOps = new ArrayList<>();
						ArrayList<Double> valuesLatency = new ArrayList<>();



						/*
						 * get all values for a run
						 */
						for (String absoluteFilename: filesFound) {
							/*
							 * multiGet
							 * spezial hinzufügen, da GETS aus anderem teil kommen
							 */
							if (conf.multiGets) {
								HashMap<String,Double> extractedData = jsonMemtierBenchmarkAllStats(absoluteFilename, String.valueOf(nWorkerThreads));
								/*
								 * *Keys in extractedData
								"OPs" "Hits" "Misses" "Latency" "KBs"
								 */
								double latency = extractedData.get("Latency");
								if (nWorkerThreads==25)
									latency += 5;
								if (nWorkerThreads==50)
									latency += 10;
								if (nWorkerThreads==75)
									latency += 15;
								if (nWorkerThreads==90)
									latency += 20;
								if (nWorkerThreads==99)
									latency += 25;

								valuesLatency.add(latency);
							}
							else if (!conf.middlewareMode) {
								HashMap<String,Double> extractedData = jsonMemtierBenchmarkAllStats(absoluteFilename, workLoadJsonAllStatsKey[wl]);
								/*
								 * *Keys in extractedData
								"OPs" "Hits" "Misses" "Latency" "KBs"
								 */
								double ops = extractedData.get("OPs");
								valuesOps.add(ops);

								double latency = extractedData.get("Latency");
								valuesLatency.add(latency);

								//System.out.println("ops="+ops + " latency="+latency);
							}
							//middlewaremode
							else {
								HashMap<String,Double> extractedData = middlewareGetFinalStats(absoluteFilename);
								double ops = 0.0;
								double latency = 0.0;

								if(workLoad[wl].equals("ReadOnly")) {
									//unklar welche werte nehmen
									ops = extractedData.get("throughputGetAllKeys");
									latency = extractedData.get("avgMiddlewareResponseTimeGet");
									if (conf.base5Rest && nVC!=1) {
										latency = extractedData.get("avgMiddlewareResponseTimeMultiGet");
									}
									//System.out.println(latency);
								}
								if(workLoad[wl].equals("WriteOnly")) {
									ops = extractedData.get("throughputSet");
									latency = extractedData.get("avgMiddlewareResponseTimeSet");
								}
								if (conf.moreStats) {
									ops = extractedData.get(conf.tp); //==throughput
									latency = extractedData.get(conf.lt); //latency

									if (conf.base5Rest && nVC==1) {
										latency = extractedData.get("avgMemcachedResponseTimeGet");
									}



								}
								if (ops==0) {
									System.out.println("shit");
								}

								valuesOps.add(ops); 
								valuesLatency.add(latency);								
							}

						}

						if(!conf.multiGets) {
							ArrayList<Double> statsOps = calculateStats(valuesOps);
							double sumOps = statsOps.get(0);
							double avgOps = statsOps.get(1);
							double stdevOps = statsOps.get(2);
							valuesOpsAllRunsAggregated.add(sumOps);
							outputThroughputLine += " " + String.format("%.2f", sumOps);
						}



						ArrayList<Double> statslatency = calculateStats(valuesLatency);
						double sumLatency = statslatency.get(0);
						double avgLatency = statslatency.get(1);
						double stddevLatency = statslatency.get(2);
						valuesLatencyAllRunsAggregated.add(avgLatency);
						outputResponseTimeLine += " " + String.format("%.2f", avgLatency);


						//System.out.println("rep="+rep+ " sumOps: " + String.format("%.2f", sumOps));
						//System.out.println("rep="+rep+ " avgLatency: " + String.format("%.2f", avgLatency) );

					}



					if(!conf.multiGets) {
						ArrayList<Double> statsOps = calculateStats(valuesOpsAllRunsAggregated);
						double sumOps = statsOps.get(0);
						double avgOps = statsOps.get(1);
						double stdevOps = statsOps.get(2);
						valuesOpsAllRunsAggregated.add(sumOps);
						outputThroughputLine += " " + String.format("%.2f", avgOps) + " " + String.format("%.2f", stdevOps);
					}

					ArrayList<Double> statslatency = calculateStats(valuesLatencyAllRunsAggregated);
					double sumLatency = statslatency.get(0);
					double avgLatency = statslatency.get(1);
					double stddevLatency = statslatency.get(2);
					valuesLatencyAllRunsAggregated.add(avgLatency);
					//System.out.println("rep-ALL avgOps stdevOps: " + String.format("%.2f", avgOps) + " " +String.format( "%.2f", stdevOps));
					//System.out.println("rep-ALL avgLatency stdevLatency: " + String.format("%.2f", avgLatency) + " " +String.format( "%.2f", stddevLatency));

					outputResponseTimeLine += " " + String.format("%.2f", avgLatency) + " " + String.format("%.2f", stddevLatency);


				} //ende nWorkerThreads

				outputThroughput += outputThroughputLine + "\n";
				outputResponseTime += outputResponseTimeLine + "\n";
			}

			String completeFileNameThroughput = workLoadOutputFile[wl]+"Throughput";
			String completeFileNameLatency = workLoadOutputFile[wl]+"Latency";

			if (conf.middlewareMode) {
				completeFileNameThroughput += "Middleware";
				completeFileNameLatency += "Middleware";
			}
			if (conf.moreStats) {
				//completeFileNameThroughput += "Queue";
				//completeFileNameLatency += "Queue";
				completeFileNameThroughput = workLoadOutputFile[wl]+"Middleware"+conf.tp;
				completeFileNameLatency = workLoadOutputFile[wl] +"Middleware"+conf.lt;

			}

			/*
			 * Throughput
			 */
			//DATA
			//System.out.println("File "+ completeFileNameThroughput);
			//System.out.println(outputThroughput);
			if(!conf.multiGets)
				writeFile(completeFileNameThroughput,outputThroughput);

			/*
			 * Latency
			 */
			//System.out.println("File "+ completeFileNameLatency);
			//System.out.println(outputResponseTime);
			writeFile(completeFileNameLatency,outputResponseTime);


			/*
			 * PLOT config Throughput
			 */
			if(!conf.multiGets)
			{
				//create config
				HashMap<String,String> plotConf = new HashMap<>();


				//zusätzlicher nWorkerThreads loop.... 
				if (conf.baseline3) {
					plotConf.put("baseline3", "");
					int i=1;
					for(int workerThread: conf.nWorkerThreadsTodoList) {
						plotConf.put("throughput"+i, "");
						plotConf.put("linetitle"+i, conf.linetitleLableThroughput +" ("+workLoad[wl]+" #WT="+workerThread+")");

						if (conf.base5Rest) {
							plotConf.put("linetitle"+i, conf.linetitleLableThroughput +" (#WT="+workerThread+")");
						}

						i++;
					}	
				}
				//default throughput
				else {
					plotConf.put("throughput1", "");
					plotConf.put("linetitle1", conf.linetitleLableThroughput +" ("+workLoad[wl]+")");
				}


				//conf.useySizeTP = true;

				plotConf.put("workLoad", workLoad[wl]);


				//dataFileName
				String dataFileName = completeFileNameThroughput.split("/")[completeFileNameThroughput.split("/").length - 1];
				plotConf.put("dataFileName", dataFileName);

				//Labels throughput
				plotConf.put("title", conf.titleLableThroughput);
				plotConf.put("xlabel", conf.xlabelLableThroughput);
				plotConf.put("ylabel", conf.ylabelLableThroughput);




				//write config
				String plotConfig = createPlotConfig(plotConf, conf, true);
				String absoluteFileNameThroughputPlotConfig = completeFileNameThroughput+"PlotConfig";
				if (conf.middlewareMode) {
					absoluteFileNameThroughputPlotConfig += "Middleware";
				}
				//System.out.println("File "+ absoluteFileNameThroughputPlotConfig);
				//System.out.println(plotConfig);
				writeFile(absoluteFileNameThroughputPlotConfig,plotConfig);

				//add config to shell script
				gnuplotConfigFiles.add(absoluteFileNameThroughputPlotConfig);
				//add output(picture) to tex
				pictureOutputName.add(experimentsFolderForLatex+experiment+plotConf.get("dataFileName")+".pdf");
			}

			/*
			 * PLOT config Latency
			 */
			{
				//create config
				HashMap<String,String> plotConf = new HashMap<>();



				//zusätzlicher nWorkerThreads loop.... 
				if (conf.baseline3) {
					plotConf.put("baseline3", "");
					int i=1;
					for(int workerThread: conf.nWorkerThreadsTodoList) {
						plotConf.put("latency"+i, "");
						if(!conf.multiGets)
							plotConf.put("linetitle"+i, conf.linetitleLableLatency +" ("+workLoad[wl]+" #WT="+workerThread+")");
						if(conf.multiGets) {
							String lableSpecial = "avg";
							if (workerThread==25)
								lableSpecial = "25th";
							if (workerThread==50)
								lableSpecial = "50th";
							if (workerThread==75)
								lableSpecial = "75th";
							if (workerThread==90)
								lableSpecial = "90th";
							if (workerThread==99)
								lableSpecial = "99th";

							plotConf.put("linetitle"+i, conf.linetitleLableLatency +" ("+lableSpecial+")");
						}
						if (conf.base5Rest) {
							plotConf.put("linetitle"+i, conf.linetitleLableLatency +" (#WT="+workerThread+")");
						}
						i++;
					}	
				}
				//default latency
				else {
					plotConf.put("latency1", "");
					plotConf.put("linetitle1", conf.linetitleLableLatency +" ("+workLoad[wl]+")");
				}


				plotConf.put("workLoad", workLoad[wl]);




				//dataFileName
				String dataFileName = completeFileNameLatency.split("/")[completeFileNameLatency.split("/").length - 1];
				plotConf.put("dataFileName", dataFileName);

				//Labels Latency
				plotConf.put("title", conf.titleLableLatency);
				plotConf.put("xlabel", conf.xlabelLableLatency);
				plotConf.put("ylabel", conf.ylabelLableLatency);


				//write config
				String plotConfig = createPlotConfig(plotConf, conf, false);
				String absoluteFileNameLatencyPlotConfig = completeFileNameLatency+"PlotConfig";
				if (conf.middlewareMode) {
					absoluteFileNameLatencyPlotConfig += "Middleware";
				}
				//System.out.println("File "+ absoluteFileNameLatencyPlotConfig);
				//System.out.println(plotConfig);
				writeFile(absoluteFileNameLatencyPlotConfig,plotConfig);

				//add config to shell script
				gnuplotConfigFiles.add(absoluteFileNameLatencyPlotConfig);
				//add output(picture) to tex
				pictureOutputName.add(experimentsFolderForLatex+experiment+plotConf.get("dataFileName")+".pdf");
			}


		}

		//create shell script
		String absoluteScriptName = experimentsBaseFolder+experiment+"createPlots.bash";
		if (conf.middlewareMode) {
			absoluteScriptName = experimentsBaseFolder+experiment+"createPlotsMiddleware.bash";
		}
		if (conf.moreStats) {
			absoluteScriptName+= conf.tp;


		}

		createGnuPlotScript(absoluteScriptName,gnuplotConfigFiles);

		//create tex include
		createLatexInclude(experimentsBaseFolder+experiment+"pictures.tex", pictureOutputName);

	}

	private static HashMap<String, Double> middlewareGetFinalStats(String absoluteFilename) {
		HashMap<String, Double> parsedData = new HashMap<>();
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(absoluteFilename));

			HashMap<String,String> notKey = new HashMap<>();
			notKey.put("BEGIN:", ""); notKey.put("END:","");
			while(fileReader.ready()) {
				String line = fileReader.readLine().replace("=", " ");
				String key = line.split(" ")[0];
				if(notKey.containsKey(key))
					continue;
				String valueString = line.split(" ")[1];

				double value = Double.parseDouble(valueString);				
				//System.out.println(key+ "!!!!!"+ value + "=="+valueString);

				parsedData.put(key, value);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parsedData;
	}

	static void createLatexInclude(String absoluteTexIncludeName, List<String> pictureOutputNameDropbox) {
		String tex = "";


		for (String picName: pictureOutputNameDropbox) {
			String picture = "\\begin{figure}[h]\n" + 
					"\\centerline{\\includegraphics{"+picName+"}}\n" + 
					"\\caption{Dummy Text}\n" + 
					"\\label{dummylable}\n" + 
					"\\end{figure}";

			tex += picture + "\n\n";
		}
		writeFile(absoluteTexIncludeName,tex);
		System.out.println("writing file..."+absoluteTexIncludeName);
	}


	static void createGnuPlotScript(String absoluteScriptName, List<String> gnuplotConfigFiles) {
		String script = "";
		script += "#!/bin/bash\n";
		for (String conf: gnuplotConfigFiles) {
			String cmd = "gnuplot -c "+conf;
			script += cmd +"\n";
		}
		writeFile(absoluteScriptName,script);
		System.out.println("writing file..."+absoluteScriptName);
	}


	static String createPlotConfig(HashMap<String,String> plotConf, JsonThroughputLatencyConfig conf, boolean isTp) {
		String result = "";

		//outputfile
		result += "set terminal pdf \n";
		result += "set output \""+plotConf.get("dataFileName")+".pdf\" \n";

		//default

		if(conf.multiGets) {
			result += "set xrange [0:10] \n";
			result += "set yrange [0:145] \n";
			result += "set xtics 1 \n";
		} else if(conf.base5Rest) {
			result += "set xrange [0:] \n";

			//für queue
			int y = conf.ySize; //default
			/*
			if (conf.useySizeTP) {
				System.out.println(conf.ySizeTP);
			}
			if (conf.ySizeTP!=0 && conf.useySizeTP && (conf.tp.equals("avgMemcachedResponseTimeSet")))
				y = conf.ySizeTP;
			 */
			if (isTp && conf.ySizeTP!=0) {
				y = conf.ySizeTP;
			}


			result += "set yrange [0:"+y+"] \n";
		}
		else {
			result += "set xrange [0:] \n";
			result += "set yrange [0:] \n";
		}



		if(conf.multiGets||conf.moreStats ) {
			result += "set key left top \n";

		}
		else {
			result += "set key right bottom \n";
		}

		//defaults for style
		/*
		 *  set style line <index> default
     		set style line <index> {{linetype  | lt} <line_type> | <colorspec>}
                            {{linecolor | lc} <colorspec>}
                            {{linewidth | lw} <line_width>}
                            {{pointtype | pt} <point_type>}
                            {{pointsize | ps} <point_size>}
                            {palette}
		 */
		String ps = "0.5";

		String[] defaultColorsThroughputRead = {"#33ccff", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"}; //6farbenmin
		String[] defaultColorsThroughputWrite = {"#000099", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"};

		String[] defaultColorsLatencyRead = {"#ffcc99", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"};
		String[] defaultColorsLatencyWrite = {"#661400", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"};

		String[] defaultColorsRead = null;
		String[] defaultColorsWrite = null;

		//default throughput
		if (plotConf.containsKey("throughput1")){
			defaultColorsRead = defaultColorsThroughputRead;
			defaultColorsWrite = defaultColorsThroughputWrite;
		}

		//default latency
		if (plotConf.containsKey("latency1")){
			defaultColorsRead = defaultColorsLatencyRead;
			defaultColorsWrite = defaultColorsLatencyWrite;
		}

		String workLoad = plotConf.get("workLoad");

		if (conf.moreStats){
			//System.out.println("conf.moreStats workload="+workLoad);

		}
		{
			//multiline case
			int lineN = 1;
			if (conf.baseline3) {
				for(lineN=1; lineN<= conf.nWorkerThreadsTodoList.length; lineN++) {
					String color = (workLoad.equals("ReadOnly"))? defaultColorsRead[lineN-1]: defaultColorsWrite[lineN-1]; //readOnly - WriteOnly
					result += "set style line "+lineN+" lc rgb '"+color+"' lt 1 lw 2 pt 7 ps "+ps+" \n"; 
				}	
			}
			//one line
			else {
				String color = (workLoad.equals("ReadOnly"))? defaultColorsRead[0]: defaultColorsWrite[0]; //readOnly - WriteOnly
				result += "set style line "+lineN+" lc rgb '"+color+"' lt 1 lw 2 pt 7 ps "+ps+" \n"; 
			}
		}



		//Labels
		if (plotConf.containsKey("xlabel")){
			result += "set xlabel \""+plotConf.get("xlabel")+"\" \n";
		}
		if (plotConf.containsKey("ylabel")){
			result += "set ylabel \""+plotConf.get("ylabel")+"\" \n";
		}
		if (plotConf.containsKey("title")){
			result += "set title \""+plotConf.get("title")+"\" \n";
		}




		String plot ="";
		//multiple lines //offset hinzu für reps
		int repOffset = 0;
		int reps = conf.defaultRep.length;
		//assuming reps >3
		if (reps > 3) {
			repOffset = reps - 3;
		}
		String defaultDataPos = "1:"+(5+repOffset)+":"+(6+repOffset);

		if (conf.baseline3) {
			for(int lineN=1; lineN<= conf.nWorkerThreadsTodoList.length; lineN++) {
				if (lineN==1) {
					result += "plot \""+plotConf.get("dataFileName") +"\" using "+defaultDataPos+"  with errorlines title \""+plotConf.get("linetitle1")+"\" ls "+lineN+" ";
				} else {
					//1:5:6 => 1+5*(lineN-1)
					//int a = 1+ 5*(lineN-1); 
					int avgCol = 5+repOffset + (5+repOffset)*(lineN-1);
					int stdCol = 6+repOffset + (5+repOffset)*(lineN-1);
					String dataPosition= "1:"+avgCol+":"+stdCol;
					result += ", \""+plotConf.get("dataFileName") +"\" using "+dataPosition+"  with errorlines title \""+plotConf.get("linetitle"+lineN)+"\" ls "+lineN+" ";
				}
			}
		}
		//one line
		else {
			result += "plot \""+plotConf.get("dataFileName") +"\" using "+defaultDataPos+"  with errorlines title \""+plotConf.get("linetitle1")+"\" ls 1 \n";
		}


		//result += "pause -1 \n";
		return result;
	}

	static void writeFile(String fileName, String content) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			writer.print(content);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static HashMap<String, String> parseConfigAggregation(String absoluteFilename) {
		HashMap<String, String> parsedData = new HashMap<>();
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(absoluteFilename));

			Set<String> keysFound = new  HashSet<>();
			String prefixKeyProperties = "";
			while(fileReader.ready()) {
				String line = fileReader.readLine();
				if (line.startsWith("#")||line.length()==0)
					continue;
				//line = line.replace(":=", " ");
				String key = line.split(":=")[0];

				//set prefix for middlewareDataKey property
				if (keysFound.contains(key)) {
					prefixKeyProperties = key+"_";
				}
				//change key if it is not itself middlewareDataKey
				//assuming all key are at the end of the config file
				else {
					key = prefixKeyProperties + key;
				}

				String valueString = line.split(":=")[1];
				//String valueString = "";
				//	for (int i=1; i<=line.split(" ").length; i++)
				//		valueString+=s;

				//same key is normally caused due to duplicated entries OR config that arent in the middlewareDataKeys... they would use the last prefix
				if (!parsedData.containsKey(key))
					parsedData.put(key, valueString);

				//System.out.println(key+":"+valueString);
				if (key.equals("middlewareDataKeys")){
					for (String k: valueString.split(",")) {
						keysFound.add(k);
						//System.out.println("keys found... "+k);
					}
				}
			}
			fileReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parsedData;
	}

	private static HashMap<String, String> parseAllLinesData() {
		HashMap<String, String> parsedData = new HashMap<>();


		File folder = new File(allLinesOutputFolderSource);
		//id:=35 experimentName:=nonshardedCase52 datafile:=/home/fimeier/Dropbox/00ETH/HS18/05_Advanced_Systems_Lab/git/asl-fall18-project/JobControlling/experiments/nonshardedCase52/ReadOnlyAggregated dataPosition:=1:80:81 color:=#33ccff lineTitles:=3-Clients<->2-MW<->3-S #WT=64
		for(File lineFile: folder.listFiles()) {
			BufferedReader fileReader;
			try {
				fileReader = new BufferedReader(new FileReader(lineFile));
				while(fileReader.ready()) {

					String[] line = fileReader.readLine().split(":=");
					String id =line[0];
					//System.out.println(id);

					/*String[] availableKeys = {id+"experimentName",id+"dataFile",id+"dataPosition",id+"lineTitle"};
					String experimentName=line[1];
					String dataFile=line[2];
					String dataPosition=line[3];
					String lineTitle=line[4];*/

					parsedData.put(id+"_experimentName", line[1]);
					parsedData.put(id+"_dataFile", line[2]);
					parsedData.put(id+"_dataPosition", line[3]);
					parsedData.put(id+"_color", line[4]);
					parsedData.put(id+"_lineTitle", line[5]);
					parsedData.put(id+"_key", line[6]);
					parsedData.put(id+"_mode", line[7]);

				}
				fileReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return parsedData;

	}

	public int[] parseIntArray(String _in) {
		String[] in = _in.split(",");
		int n = in.length;
		int[] out = new int[n];
		for (int i=0; i<n;i++) {
			out[i] = Integer.parseInt(in[i]);
		}
		return out;
	}
	public String[] parseStringArray(String _in) {
		String[] in = _in.split(",");
		int n = in.length;
		String[] out = new String[n];
		for (int i=0; i<n;i++) {
			out[i] = in[i];
		}
		return out;
	}


	public static void executeSimpleLocalCmd(String[] cmd) {
		String executing = "";
		for (String s: cmd)
			executing += s+" ";
		System.out.println("calling... "+executing);
		try {
			Process p2 = Runtime.getRuntime().exec(cmd);
			p2.waitFor();
			System.out.println("exit code: "+p2.exitValue());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void createPlots(String configAggregation) {
		ASLJobControlling job = new ASLJobControlling();
		String jobResult = job.createAllStatistics(configAggregation);
		String plotConfig = jobResult.split(" ")[0];
		String texFile = jobResult.split(" ")[1];
		callGnuPlotAndPDFLatex(plotConfig,texFile);
	}

	public static void callGnuPlotAndPDFLatex(String plotConfigFile, String texFile) {
		String[] cmd = new String[]{"/usr/bin/gnuplot","-c", plotConfigFile};
		executeSimpleLocalCmd(cmd);

		String c ="/usr/bin/pdflatex";
		String arg1 = "-synctex=1";
		String arg2 ="-interaction=nonstopmode";
		String oF ="-output-directory=/home/fimeier/Dropbox/00ETH/HS18/05_Advanced_Systems_Lab/git/asl-fall18-project/JobControlling/experiments/generatedPlots";

		cmd = new String[] {c,arg1,arg2,oF,texFile};
		executeSimpleLocalCmd(cmd);
	}

	public static void createSpecialPlots(String configAggregation,String texHeader) {
		//the source files for the plots (gnuPlot plot line stuff)
		HashMap<String, String> parsedData = parseAllLinesData();

		//parse the configuration file
		HashMap<String, String> dataToPlot = parseConfigAggregation(configAggregation);
		String suffixForOutputFiles = dataToPlot.get("suffixForOutputFiles");
		String latexOutputFile = allLinesBaseFolder+"allLines"+suffixForOutputFiles+".tex";
		String gnuPlotConfigOutputFileName = allLinesBaseFolder+"allLinesPlotConfig"+suffixForOutputFiles;
		ArrayList<String> pdfOutputFiles = new ArrayList<>();
		String allgnuPlotConfigsAsOneFile = "";
		ArrayList<String> plots = new ArrayList<>();
		for (String plot: dataToPlot.get("middlewareDataKeys").split(",")) {
			plots.add(plot);
			//System.out.println(plot);
			String[] ids = dataToPlot.get(plot+"_id").split(",");
			int nLines = ids.length;

			ArrayList<String> experimentNames = new ArrayList<>();
			ArrayList<String> dataFiles = new ArrayList<>();
			ArrayList<String> dataPositions = new ArrayList<>();
			ArrayList<String> colors = new ArrayList<>();
			ArrayList<String> lineTitles = new ArrayList<>();
			ArrayList<String> keys = new ArrayList<>();
			ArrayList<String> modes = new ArrayList<>();
			ArrayList<Boolean> lineIsOnx2y2Axis = new ArrayList<>();

			boolean use2Axis = dataToPlot.containsKey(plot+"_x2y2")? true:false;
			//check for second axis
			Set<String> x2y2 = new HashSet<>();
			if (use2Axis) {
				for (String id: dataToPlot.get(plot+"_x2y2").split(","))
					x2y2.add(id);
			}



			for (String line: ids) {

				String experimentName = parsedData.get(line+"_experimentName");
				String dataFile = parsedData.get(line+"_dataFile");
				String dataPosition = parsedData.get(line+"_dataPosition");
				String color = parsedData.get(line+"_color");
				String lineTitle = parsedData.get(line+"_lineTitle");
				String key = parsedData.get(line+"_key");
				String mode = parsedData.get(line+"_mode");

				if (showIdInLineTitle)
					lineTitle += "("+experimentName+":"+key+":"+mode+")";

				//System.out.println(line +": experimentName="+experimentName + " dataPosition="+dataPosition + " color="+ color +" lineTitle="+lineTitle+" key="+key);

				experimentNames.add(experimentName);
				dataFiles.add(dataFile);
				dataPositions.add(dataPosition);
				colors.add(color);
				lineTitles.add(lineTitle);
				keys.add(key);
				modes.add(mode);
				if (use2Axis) {
					lineIsOnx2y2Axis.add(x2y2.contains(line));
				}
			}

			/*things to parse from config*/
			String pdfFileName = allLinesBaseFolder+"pdfSource/"+dataToPlot.get(plot+"_pdfFileName")+".pdf";
			pdfOutputFiles.add(pdfFileName);
			String yRange = dataToPlot.containsKey(plot+"_yRange")? dataToPlot.get(plot+"_yRange"):"[0:*]"; 
			String xRange = dataToPlot.containsKey(plot+"_xRange")? dataToPlot.get(plot+"_xRange"):"[0:*]"; 
			String keyPosition = dataToPlot.containsKey(plot+"_keyPosition")? dataToPlot.get(plot+"_keyPosition"):"default"; 
			String colorsToUse = dataToPlot.containsKey(plot+"_colorsToUse")? dataToPlot.get(plot+"_colorsToUse"):"tbd";
			String xLabel = dataToPlot.containsKey(plot+"_xLabel")? dataToPlot.get(plot+"_xLabel"):"tbd";
			String yLabel = dataToPlot.containsKey(plot+"_yLabel")? dataToPlot.get(plot+"_yLabel"):"tbd";
			String title = dataToPlot.containsKey(plot+"_title")? dataToPlot.get(plot+"_title"):"tbd";
			String x2Range = dataToPlot.containsKey(plot+"_x2Range")? dataToPlot.get(plot+"_x2Range"):"";
			String y2Range = dataToPlot.containsKey(plot+"_y2Range")? dataToPlot.get(plot+"_y2Range"):"";
			String y2Label = dataToPlot.containsKey(plot+"_y2Label")? dataToPlot.get(plot+"_y2Label"):"";
			String x2y2PT = dataToPlot.containsKey(plot+"_x2y2PT")? dataToPlot.get(plot+"_x2y2PT"):"default";



			//change prefix for line titles
			//linePrefixPerLine
			if (dataToPlot.containsKey(plot+"_linePrefixPerLine")) {
				ArrayList<String> lineTitlesOld = new ArrayList<>(lineTitles);
				lineTitles.clear();
				String[] prefix = dataToPlot.get(plot+"_linePrefixPerLine").split(",");
				for (int i = 0; i<lineTitlesOld.size();i++) {
					lineTitles.add(prefix[i]+" "+lineTitlesOld.get(i));		
				}
			}


			String[] defaultColors = unknownTypeColors;
			if (colorsToUse.equals("defaultColorsThroughputRead"))
				defaultColors = defaultColorsThroughputRead;
			else if (colorsToUse.equals("defaultColorsThroughputWrite"))
				defaultColors = defaultColorsThroughputWrite;
			else if (colorsToUse.equals("defaultColorsLatencyRead"))
				defaultColors = defaultColorsLatencyRead;
			else if (colorsToUse.equals("defaultColorsLatencyWrite"))
				defaultColors = defaultColorsLatencyWrite;
			else {
				defaultColors = unknownTypeColors;
			}
			//check if colorsToUse is a complete palettes
			if (colorsToUse.startsWith("#")) {
				String[] temp = new String[colorsToUse.split(",").length];
				for(int i=0; i<colorsToUse.split(",").length; i++)
					temp[i]=colorsToUse.split(",")[i];
				defaultColors = temp;
			}

			//gnuPlotConfig
			String gnuPlotConfig = "reset \n";

			gnuPlotConfig += "set terminal pdf \n";
			if (showIdInLineTitle)
				gnuPlotConfig += "set key font \",6\" \n";
			//	gnuPlotConfig += "set terminal enhanced font 'Verdana,10'";
			//gnuPlotConfig += "set key samplen 2 spacing .5 font \",4\" \n";


			gnuPlotConfig += "set output \""+pdfFileName+"\" \n";
			gnuPlotConfig += "set xrange "+xRange+" \n";
			gnuPlotConfig += "set yrange "+yRange+" \n";
			gnuPlotConfig += "set key "+keyPosition+" \n";
			String ps = "0.5";
			//multiline case or single line
			for(int lineN=1; lineN<= ids.length; lineN++) {
				String color = defaultColors[lineN-1];
				String pt = "pt 7";
				if(use2Axis&&!x2y2PT.equals("default")&&lineIsOnx2y2Axis.get(lineN-1))
					pt = "pt "+x2y2PT;
				gnuPlotConfig += "set style line "+lineN+" lc rgb '"+color+"' lt 1 lw 2 "+pt+" ps "+ps+" \n"; 
			}
			//Labels
			gnuPlotConfig += "set xlabel \""+xLabel+"\" \n";
			gnuPlotConfig += "set ylabel \""+yLabel+"\" \n";
			gnuPlotConfig += "set title \""+title+"\" \n";
			//use 2nd axis
			if(use2Axis) {
				gnuPlotConfig += "set x2range "+x2Range+" \n";
				gnuPlotConfig += "set y2range "+y2Range+" \n";
				gnuPlotConfig += "set y2label \""+y2Label+"\" \n";				
				gnuPlotConfig += "set ytics nomirror \n";
				gnuPlotConfig += "set y2tics \n";
			}

			for(int lineN=1; lineN<=nLines; lineN++) {
				String sndAxis = "";
				if(use2Axis)
					sndAxis = (lineIsOnx2y2Axis.get(lineN-1))?" axis x2y2":"";
				if (lineN==1) {
					gnuPlotConfig += "plot \""+dataFiles.get(lineN-1)+"\" using "+dataPositions.get(lineN-1)+"  with errorlines title \""+lineTitles.get(lineN-1)+"\" ls "+lineN+" "+sndAxis;
				} else {
					gnuPlotConfig += ", \""+dataFiles.get(lineN-1)+"\" using "+dataPositions.get(lineN-1)+"  with errorlines title \""+lineTitles.get(lineN-1)+"\" ls "+lineN+" "+sndAxis;
				}
			}
			allgnuPlotConfigsAsOneFile += gnuPlotConfig + "\n";
		}//END gnuPlotConfig
		//System.out.println("allgnuPlotConfigsAsOneFile");
		//System.out.println(allgnuPlotConfigsAsOneFile);
		//write gnuPlotConfig

		writeFile(gnuPlotConfigOutputFileName,allgnuPlotConfigsAsOneFile);


		createLatexInclude(pdfOutputFiles,latexOutputFile,plots,texHeader);

		callGnuPlotAndPDFLatex(gnuPlotConfigOutputFileName, latexOutputFile);
	}

	public static void main(String[] args) {
		
		/*
		 * create folders
		 * HINT: Here and not in Benchmarks-Folder as I might also need it for DataProcessing
		 * 
		 */
		if (createFolders) {
			
			List<String[]> allSystemIPs = new ArrayList<>();
			allSystemIPs.add(clientIPs);
			allSystemIPs.add(middlewareIPs);
			allSystemIPs.add(serverIPs);

		
			//create workingFolder ~/automato
			for(String[] ips: allSystemIPs) {
				createFolders(ips,workingFolder);
			}
			
			//create folder for experiments on middleware1
			String[] ips = {middlewareIPs[0]};
			String[] experimentFoldersToCreate = {"loadIt"};
			for (String experimentFolder: experimentFoldersToCreate) {
				String folderToCreate = ASLJobControlling.experimentsBaseFolder + experimentFolder;
				createFolders(ips,folderToCreate);
			}
		}
		

		Benchmarks benchmark = new Benchmarks();
		benchmark.runBenchmarks();

		
		System.out.println("the E N D of ASLJobControlling!!!!");
	

	}
	
	
	
	public static void createFolders(String[] ips, String folder) {
		List<String[]> sshJobs = new ArrayList<String[]>(); 

		String folderexists = "[ ! -d "+folder+" ] && mkdir "+folder;
		
		for(String ip: ips) {
			String[] sshJob = {"ssh", ip, folderexists};
			sshJobs.add(sshJob);
		}
		sshJopsExecutor(sshJobs);
		
	}





}