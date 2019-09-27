import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class Benchmarks extends ASLJobControlling {


	public static int totalNumberOfexperiments = 0;



	/*
	 * Konfiguration is in ASLJobControlling
	 * deploy needed files for benchmarks
	 * load memcached memory with appropriate values
	 * do the benchmarks
	 */
	public void runBenchmarks() {

		long startExperiment = System.currentTimeMillis();
		totalNumberOfexperiments = 0;




		//Test: inlcude this in each experiment with the needed values
		loadingMemcachedServer(4096);


		System.out.println("*****************************************************************************************************************\n");
		System.out.println("***************************************** starting fullSystem41() *****************************\n");
		fullSystem41();

		System.out.println("*****************************************************************************************************************\n");
		System.out.println("***************************************** starting baseline21() *****************************\n");
		baseline21();

		


		long experimentDurationinSeconds = (System.currentTimeMillis() - startExperiment) / 1000;
		long expectedRuntimeInSeconds = totalNumberOfexperiments * 77;
		System.out.println("Total Number of Experiments: "+totalNumberOfexperiments);
		System.out.println("Total Runtime: "+experimentDurationinSeconds);
		System.out.println("Expected Runtime for 60sec runs: "+expectedRuntimeInSeconds +"sec, "+expectedRuntimeInSeconds/3600+"h");


		System.out.println("leaving programm....");

	}

	/*
	 * 
	 * functions for initial population of memcached memory
	 * 
	 * TODO change load time parameters and check 0% misses after loading...
	 * 			=> probably more time needed on azure!!!
	 */
	public void loadingMemcachedServer(int dataSize) {


		//HERE Restart all Servers!!!
		List<String[]> sshJobs = new ArrayList<String[]>(); 
		String cmd = "sudo service memcached restart";
		for(String ip: serverIPs) {
			String[] sshJob = {"ssh", ip, cmd};
			sshJobs.add(sshJob);
		}
		sshJopsExecutor(sshJobs);


		for(int i = 0; i<MEMTIERLOADIterations; i++) {
			System.out.println("Loading the system.....");

			int loadTime = MEMTIERLOADTIMEWriteOnly;
			loadIt(WRITEONLY, loadTime, i, dataSize);

			loadTime = MEMTIERLOADTIMEReadOnly;
			loadIt(READONLY, loadTime, i, dataSize);
		}

	}

	public void loadIt(String workload, int loadDuration, int loadItRun, int dataSize) {

		String experimentFolder = "loadIt";

		//--data-size", "--data-size=4096")

		String argsMemtier = "--test-time="+loadDuration;
		argsMemtier += " --clients="+50;
		argsMemtier += " --threads="+4;
		argsMemtier += workload;
		argsMemtier += " --data-size="+dataSize;

		if (workload.equals(WRITEONLY)) {
			argsMemtier += " --key-pattern=P:P";
		}
		/*
		 * start memtier_benchmark
		 */
		List<Thread> clientThreads = new ArrayList<>();
		/*
		 * start all clients
		 */

		for (int i = 0; i<3; i++) {
			MemtierCommand memtierRun = new MemtierCommand();
			boolean start2ndInstance = false;
			memtierRun.setMemtierCommand(clientIPs[i], argsMemtier + " --server="+serverIPs[i] + " --port="+serverPorts[i], start2ndInstance);

			Thread threadMemtier = new Thread(new RunCommandInThread(memtierRun));
			clientThreads.add(threadMemtier);
			threadMemtier.start();
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
		for (String clientIP: ASLJobControlling.clientIPs) {
			String completeFileSuffix = "_"+getSimpleWorkloadName(workload)+"_loadItRun="+loadItRun+"_Client="+c;
			copyFilesback(new String[] {clientIP},completeFileSuffix, false, experimentFolder);
			c++;

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
					//String value = s.split("=")[1];
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

						//Process p2 = Runtime.getRuntime().exec(cmdkillMW);
						Runtime.getRuntime().exec(cmdkillMW);

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



	public static void printCommand(String[] cmd) {
		for (String s: cmd) {
			System.out.print(s+" ");
		}
		System.out.print("\n");
	}






	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * Benchmarks (the real tests)
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	//allready defined experiments ASL2018
	/**
	 * Experiment according to section 2.1 of the report outline
	 */
	public void baseline21(){
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;
		// paramters to change for real experiment


		int repetitions = 3;
		int nVCmax = 32;

		String experimentFolder = "baseline21";
		String[] workloads = {WRITEONLY, READONLY};

		for (String workload: workloads) {
			for (int rep = 1; rep <= repetitions; rep++) {
				String suffixForThisRunPrefix = "Baseline21_"+getSimpleWorkloadName(workload);
				for (int nVC = 1; nVC <=nVCmax; nVC++) {
					String suffixForThisRun = suffixForThisRunPrefix + "_nVC="+nVC+"_Rep="+rep;
					int nServer=1;
					int nVirtualMachineClients=3;
					//int nVC
					int nCT = 2;
					//String workload
					//String suffixForThisRun
					int nMW = 0;
					int nWorkerThreads = 0;
					int  multiGetSize = 0;
					String shardedReading = "false";
					TestSetting experiment = new TestSetting(nServer, nVirtualMachineClients, nVC, nCT, workload, suffixForThisRun, nMW, nWorkerThreads, multiGetSize, shardedReading, experimentFolder);

					numExperiments++;
					doBenchmarks(experiment);
				}
			}
		}


		totalNumberOfexperiments += numExperiments;
		long experimentDuration = System.currentTimeMillis() - startExperiment;
		System.out.println("Experiment "+experimentFolder+" did "+numExperiments+" many experiments and it took "+ experimentDuration /1000 +"sec with a memtiertestime of "+MEMTIERTESTTIME );

	}

	/**
	 * Experiment according to section 2.2 of the report outline
	 */
	public void baseline22() {
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;

		// paramters to change for real experiment
		int repetitions = 3; //3
		int nVCmax = 32; //32

		String experimentFolder = "baseline22";
		String[] workloads = {WRITEONLY, READONLY};

		for (String workload: workloads) {
			for (int rep = 1; rep <= repetitions; rep++) {
				String suffixForThisRunPrefix = "Baseline22_"+getSimpleWorkloadName(workload);
				for (int nVC = 1; nVC <=nVCmax; nVC++) {
					String suffixForThisRun = suffixForThisRunPrefix + "_nVC="+nVC+"_Rep="+rep;
					int nServer=2;
					int nVirtualMachineClients=1;
					//int nVC
					int nCT = 1;
					//String workload
					//String suffixForThisRun
					int nMW = 0;
					int nWorkerThreads = 0;
					int  multiGetSize = 0;
					String shardedReading = "false";

					TestSetting experiment = new TestSetting(nServer, nVirtualMachineClients, nVC, nCT,workload, suffixForThisRun, nMW, nWorkerThreads, multiGetSize, shardedReading, experimentFolder);

					numExperiments++;
					doBenchmarks(experiment);
				}
			}
		}


		totalNumberOfexperiments += numExperiments;

		long experimentDuration = System.currentTimeMillis() - startExperiment;
		System.out.println("Experiment "+experimentFolder+" did "+numExperiments+" many experiments and it took "+ experimentDuration /1000 +"sec with a memtiertestime of "+MEMTIERTESTTIME );


	}

	/**
	 * Experiment according to section 3.1 of the report outline
	 */
	public void baseline31() {
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;


		// paramters to change for real experiment
		int repetitions = 3;
		//int nVCmax = 32; //
		int[] nVCsamples = {1,4};//{1,4,8,12,16,20,24,28,32};
		int[] nWorkerThreadsTodoList = {8,16};//{8, 16, 32, 64};




		String experimentFolder = "baseline31";
		String[] workloads = {WRITEONLY, READONLY};

		/*
		 * test duration
		 * 							|workload|	* rep * |nVC| 	* |nWorkerThreads| 	* JAVAMIDDLEWAREKILLDELAY
		 * 	50'000sec=13.76h			2		*	3 * 32		*	4				* 68	
		 */
		for (String workload: workloads) {
			for (int rep = 1; rep <= repetitions; rep++) {
				String suffixForThisRunPrefix = "Baseline31_"+getSimpleWorkloadName(workload);
				//for (int nVC = 1; nVC <=nVCmax; nVC++) {
				for (int nVC: nVCsamples) {
					for (int nWorkerThreads: nWorkerThreadsTodoList) {
						String suffixForThisRun = suffixForThisRunPrefix + "_nVC="+nVC+"nWorkerThreads"+nWorkerThreads+"_Rep="+rep;
						int nServer=1;
						int nVirtualMachineClients=3;
						//int nVC
						int nCT = 2;
						//String workload
						//String suffixForThisRun
						int nMW = 1;
						//int nWorkerThreads = 8;
						int  multiGetSize = 0;
						String shardedReading = "false";
						TestSetting experiment = new TestSetting(nServer, nVirtualMachineClients, nVC, nCT,workload, suffixForThisRun, nMW, nWorkerThreads, multiGetSize, shardedReading, experimentFolder);

						numExperiments++;
						doBenchmarks(experiment);

					}
				}
			}
		}

		totalNumberOfexperiments += numExperiments;

		long experimentDuration = System.currentTimeMillis() - startExperiment;
		System.out.println("Experiment "+experimentFolder+" did "+numExperiments+" many experiments and it took "+ experimentDuration /1000 +"sec with a memtiertestime of "+MEMTIERTESTTIME );


	}

	/**
	 * Experiment according to section 3.2 of the report outline
	 */
	public void baseline32() {
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;

		// paramters to change for real experiment
		int repetitions = 3;
		//int nVCmax = 32;
		int[] nVCsamples = {1,4,8,12,16,20,24,28,32};

		int[] nWorkerThreadsTodoList = {8, 16, 32, 64};


		String experimentFolder = "baseline32";
		String[] workloads = {WRITEONLY, READONLY};


		for (String workload: workloads) {
			for (int rep = 1; rep <= repetitions; rep++) {
				String suffixForThisRunPrefix = "Baseline32_"+getSimpleWorkloadName(workload);
				//for (int nVC = 1; nVC <=nVCmax; nVC++) {
				for (int nVC: nVCsamples) {

					for (int nWorkerThreads: nWorkerThreadsTodoList) {
						String suffixForThisRun = suffixForThisRunPrefix + "_nVC="+nVC+"nWorkerThreads"+nWorkerThreads+"_Rep="+rep;
						int nServer=1;
						int nVirtualMachineClients=3;
						//int nVC
						int nCT = 1;
						//String workload
						//String suffixForThisRun
						int nMW = 2;
						//int nWorkerThreads = 8;
						int  multiGetSize = 0;
						String shardedReading = "false";
						TestSetting experiment = new TestSetting(nServer, nVirtualMachineClients, nVC, nCT,workload, suffixForThisRun, nMW, nWorkerThreads, multiGetSize, shardedReading, experimentFolder);

						numExperiments++;
						doBenchmarks(experiment);

					}
				}
			}
		}

		totalNumberOfexperiments += numExperiments;

		long experimentDuration = System.currentTimeMillis() - startExperiment;
		System.out.println("Experiment "+experimentFolder+" did "+numExperiments+" many experiments and it took "+ experimentDuration /1000 +"sec with a memtiertestime of "+MEMTIERTESTTIME );

	}

	/**
	 * Experiment according to section 4.1 of the report outline
	 */
	public void fullSystem41() {
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;
		// paramters to change for real experiment
		int repetitions = 6;
		//int nVCmax = 32;
		int[] nVCsamples = {1,4,8,12,16,20,24,28,32};

		int[] nWorkerThreadsTodoList = {8, 16, 32, 64};

		String experimentFolder = "fullSystem41";
		String[] workloads = {WRITEONLY};

		for (String workload: workloads) {
			for (int rep = 1; rep <= repetitions; rep++) {
				String suffixForThisRunPrefix = "fullSystem41"+getSimpleWorkloadName(workload);
				//for (int nVC = 1; nVC <=nVCmax; nVC++) {
				for (int nVC: nVCsamples) {
					for (int nWorkerThreads: nWorkerThreadsTodoList) {
						String suffixForThisRun = suffixForThisRunPrefix + "_nVC="+nVC+"nWorkerThreads"+nWorkerThreads+"_Rep="+rep;
						int nServer=3;
						int nVirtualMachineClients=3;
						//int nVC
						int nCT = 1;
						//String workload
						//String suffixForThisRun
						int nMW = 2;
						//int nWorkerThreads = 8;
						int  multiGetSize = 0;
						String shardedReading = "false";
						TestSetting experiment = new TestSetting(nServer, nVirtualMachineClients, nVC, nCT,workload, suffixForThisRun, nMW, nWorkerThreads,  multiGetSize, shardedReading, experimentFolder);

						numExperiments++;
						doBenchmarks(experiment);

						System.out.println(experimentFolder+": did "+numExperiments+ " of 216 experiments");


					}
				}
			}
		}

		totalNumberOfexperiments += numExperiments;

		long experimentDuration = System.currentTimeMillis() - startExperiment;
		System.out.println("Experiment "+experimentFolder+" did "+numExperiments+" many experiments and it took "+ experimentDuration /1000 +"sec with a memtiertestime of "+MEMTIERTESTTIME );

	}

	/**
	 * Experiment according to section 5.1 (called with true) or 5.2 (called with false)  of the report outline
	 */
	public void multiGets(boolean _shardedReading) {
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;

		// paramters to change for real experiment
		int repetitions = 6; //CHANGE!!!!

		int[] nWorkerThreadsTodoList = {8, 16, 32, 64, 128}; //TODO max throughput wird hier gesucht

		String experimentFolder = (_shardedReading) ? "shardedCase51": "nonshardedCase52";
		String[] workloads = {RATIO11, RATIO13, RATIO16, RATIO19};
		int[] multiGetSizes = {1,3,6,9};

		for (int i = 0; i < multiGetSizes.length; i++) {
			int multiGetSize = multiGetSizes[i];
			String workload = workloads[i];
			for (int rep = 1; rep <= repetitions; rep++) {
				String suffixForThisRunPrefix = experimentFolder+getSimpleWorkloadName(workload);
				for (int nWorkerThreads: nWorkerThreadsTodoList) {
					int nServer=3;
					int nVirtualMachineClients=3;
					int nVC=2;
					int nCT = 1;
					//String workload
					//String suffixForThisRun
					int nMW = 2;
					//int nWorkerThreads = 8;
					String shardedReading = String.valueOf(_shardedReading);

					String suffixForThisRun = suffixForThisRunPrefix + "_nWorkerThreads"+nWorkerThreads+"_Rep="+rep;

					TestSetting experiment = new TestSetting(nServer, nVirtualMachineClients, nVC, nCT,workload, suffixForThisRun, nMW, nWorkerThreads, multiGetSize, shardedReading, experimentFolder);

					/*
					System.out.println("experiment.argsMiddleware1: " +experiment.argsMiddleware1);
					System.out.println("experiment.argsMiddleware2: "+experiment.argsMiddleware2);
					System.out.println("experiment.argsDifferentFromDefault1stInstance: "+experiment.argsDifferentFromDefault1stInstance);
					System.out.println("experiment.argsDifferentFromDefault2ndInstance: "+experiment.argsDifferentFromDefault2ndInstance);
					 */

					numExperiments++;
					doBenchmarks(experiment);

					System.out.println(experimentFolder+": did "+numExperiments+ " of 120 experiments");
				}
			}
		}

		totalNumberOfexperiments += numExperiments;

		long experimentDuration = System.currentTimeMillis() - startExperiment;
		System.out.println("Experiment "+experimentFolder+" did "+numExperiments+" many experiments and it took "+ experimentDuration /1000 +"sec with a memtiertestime of "+MEMTIERTESTTIME );

	}

	/**
	 * Experiment according to section 6 of the report outline
	 */
	public void twoKAnalysis6() {
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;

		// paramters to change for real experiment
		int repetitions = 6;
		String experimentFolder = "twoKAnalysis6";
		String[] workloads = {WRITEONLY, READONLY};
		int[] memcachedServers = {1, 3};
		int[] middlewares = {1, 2};
		int[] nWorkerThreadsTodoList = {8, 32};



		for (String workload: workloads) {

			for (int rep = 1; rep <= repetitions; rep++) {
				//suffixForThisRun += "_rep="+rep;

				for (int nMemcached: memcachedServers) {
					//suffixForThisRun += "_nServer="+nMemcached;

					for (int nMiddlewares: middlewares) {
						//suffixForThisRun += "_nMW="+nMiddlewares;

						for (int nWorkerThreads: nWorkerThreadsTodoList) {
							//suffixForThisRun += "_nWorkerThreads="+nWorkerThreads;

							String suffixForThisRun = experimentFolder+getSimpleWorkloadName(workload)+ "_rep="+rep + "_nServer="+nMemcached+"_nMW="+nMiddlewares+"_nWorkerThreads="+nWorkerThreads;
							//System.out.println(suffixForThisRun);

							int nServer=nMemcached;
							int nVirtualMachineClients=3;
							int nVC = 32;
							int nCT = 2 / nMiddlewares;
							//String workload
							//String suffixForThisRun
							int nMW = nMiddlewares;
							//int nWorkerThreads = 8;
							int  multiGetSize = 0;
							String shardedReading = "false";
							TestSetting experiment = new TestSetting(nServer, nVirtualMachineClients, nVC, nCT,workload, suffixForThisRun, nMW, nWorkerThreads,  multiGetSize, shardedReading, experimentFolder);

							/*
							System.out.println("experiment.argsMiddleware1: " +experiment.argsMiddleware1);
							System.out.println("experiment.argsMiddleware2: "+experiment.argsMiddleware2);
							System.out.println("experiment.argsDifferentFromDefault1stInstance: "+experiment.argsDifferentFromDefault1stInstance);
							System.out.println("experiment.argsDifferentFromDefault2ndInstance: "+experiment.argsDifferentFromDefault2ndInstance);
							System.out.println("*****************************************************************************************************************\n");
							 */

							numExperiments++;
							doBenchmarks(experiment);
							System.out.println(experimentFolder+": did "+numExperiments+ " of 96 experiments");

						}
					}
				}
			}
		}

		totalNumberOfexperiments += numExperiments;

		long experimentDuration = System.currentTimeMillis() - startExperiment;
		System.out.println("Experiment "+experimentFolder+" did "+numExperiments+" many experiments and it took "+ experimentDuration /1000 +"sec with a memtiertestime of "+MEMTIERTESTTIME );

	}







}
