package ch.ethz.gitlab.fimeier.asl19;

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
		
		System.out.println("******************New Version*********************");

		long startExperiment = System.currentTimeMillis();
		totalNumberOfexperiments = 0;



		System.out.println("*****************************************************************************************************************\n");
		System.out.println("***************************************** starting baseline21() *****************************\n");
		//baseline21();

		System.out.println("*****************************************************************************************************************\n");
		System.out.println("***************************************** starting baseline22() *****************************\n");
		//baseline22();

		System.out.println("*****************************************************************************************************************\n");
		System.out.println("***************************************** starting baseline31() *****************************\n");
		//baseline31();

		System.out.println("*****************************************************************************************************************\n");
		System.out.println("***************************************** starting baseline32() *****************************\n");
		//baseline32();

		System.out.println("*****************************************************************************************************************\n");
		System.out.println("***************************************** starting baseline33() *****************************\n");
		//baseline33();

		System.out.println("*****************************************************************************************************************\n");
		System.out.println("***************************************** starting baseline34() *****************************\n");
		//baseline34();
		
		System.out.println("*****************************************************************************************************************\n");
		System.out.println("***************************************** starting twoKAnalyse() ****************************\n");
		//twoKAnalyse();



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

		System.out.println("*****************************************************************************************************************");
		System.out.println("******************************loading --data-size="+dataSize+" into memcached server's*************************************");
		System.out.println("*****************************************************************************************************************");


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

	//load it is eigentlich ein "normaler" benchmark wie alle anderen auch
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
			int memtierInstance = 1;
			memtierRun.setMemtierCommand(clientIPs[i], argsMemtier + " --server="+serverIPs[i] + " --port="+serverPorts[i], memtierInstance);

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
			String completeFileSuffix = "_"+getSimpleWorkloadName(workload)+"_loadItRun="+loadItRun+"_dataSize="+dataSize+"_Client="+c;
			copyFilesback(new String[] {clientIP},completeFileSuffix, false, false, experimentFolder);
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
		//Testsettings
		String experimentFolder = "";
		int nServer = 0;
		int nVirtualMachineClients = 0;
		int nInstancesOfMemtierPerMachine = 0;
		int nCT = 0; //nCT_ThreadsPerMemtierInstance
		int nVC = 0; //nVC_VirtualClients
		String workload = READONLY;
		int dataSize = 0;
		/**
		 * #-middleware servers
		 * 1 Implies middleware1
		 * 0 implies test without middleware
		 * 2 implies middleware1 and middleware2
		 */
		int nMW = 1;
		int nWorkerThreads = 8;
		String suffixForThisRun = "replaceThis";


		/*
		 * memtier client arguments config
		 */
		String[] clientInTestIPs = null;
		String argsDifferentFromDefault1stInstance = "";
		String argsDifferentFromDefault2ndInstance = "";
		String argsDifferentFromDefault3thInstance = "";
		boolean start2ndMemtierInstance = false;
		boolean start3thMemtierInstance = false;


		/*
		 * middleware arguments config
		 */
		String argsMiddleware1 = "";
		String argsMiddleware2 = "";

		TestSetting(
				String _experimentFolder,
				int _nServer,
				int _nVirtualMachineClients,
				int memtierInstancesPerClient,  //neu
				int nCT_ThreadsPerMemtierInstance, //früher _nCT
				int nVC_VirtualClients, //früher _nVC
				String _workload,
				int _dataSize, //neu
				int _nMW,
				int _nWorkerThreads,
				String _suffixForThisRun
				){
			experimentFolder = _experimentFolder;
			nServer = _nServer;
			nVirtualMachineClients = _nVirtualMachineClients;
			nInstancesOfMemtierPerMachine = memtierInstancesPerClient;
			nCT = nCT_ThreadsPerMemtierInstance;
			nVC = nVC_VirtualClients;
			workload = _workload;
			dataSize = _dataSize;
			nMW = _nMW;
			nWorkerThreads = _nWorkerThreads;
			suffixForThisRun = _suffixForThisRun;

			clientInTestIPs = new String[nVirtualMachineClients];
			for(int i = 0; i<nVirtualMachineClients;i++) {
				clientInTestIPs[i]=ASLJobControlling.clientIPs[i];
			}

			if (nInstancesOfMemtierPerMachine > 1) {
				start2ndMemtierInstance = true;
			}
			if (nInstancesOfMemtierPerMachine == 3) {
				start3thMemtierInstance = true;
			}




			/*
			 * middleware arguments config
			 */

			//-l 127.0.0.1 -p 11212 -t 2 -s true -m 127.0.0.1:12333
			argsMiddleware1 = "-tWaitBeforeMeasurements "+ tWAITBEFOREMEASUREMENTS;
			argsMiddleware1 += " -tTimeForMeasurements "+ tTIMEFORMEASUREMENTS;
			argsMiddleware1 += " -t "+nWorkerThreads;

			String middlewareServerConnectString = " -m ";
			for (int s = 0; s<nServer; s++) {
				//ugly hack: -m ip1:12333#ip2:port2... not really needed
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

			//set data size
			argsDifferentFromDefault1stInstance += " --data-size="+dataSize;


			//set workload
			argsDifferentFromDefault1stInstance += workload;





			//example case: baseline21
			//connect 1-instance to one memcached-server
			if (nMW==0 && nServer==1 && nInstancesOfMemtierPerMachine==1) {				
				argsDifferentFromDefault1stInstance += " --server="+serverIPs[0] + " --port="+serverPorts[0];			
			}

			//example case: baseline22
			//connect all-3 instances to a different memcached-server
			else if (nMW==0 && nServer==3 && nInstancesOfMemtierPerMachine==3) {

				argsDifferentFromDefault2ndInstance = argsDifferentFromDefault1stInstance;
				argsDifferentFromDefault3thInstance = argsDifferentFromDefault1stInstance;

				argsDifferentFromDefault1stInstance += " --server="+serverIPs[0] + " --port="+serverPorts[0];
				argsDifferentFromDefault2ndInstance += " --server="+serverIPs[1] + " --port="+serverPorts[1];
				argsDifferentFromDefault3thInstance += " --server="+serverIPs[2] + " --port="+serverPorts[2];				
			}


			//example case: baseline31, baseline32
			//connect 1-instance to 1 MW
			else if (nMW==1 && nInstancesOfMemtierPerMachine==1) {				
				argsDifferentFromDefault1stInstance += " --server="+middlewareIPs[0] + " --port="+middlewarePorts[0];			
			}

			//example case: baseline33, baseline34
			//connect 2-instance to 2 MW
			else if (nMW==2 && nInstancesOfMemtierPerMachine==2) {
				argsDifferentFromDefault2ndInstance = argsDifferentFromDefault1stInstance;

				argsDifferentFromDefault1stInstance += " --server="+middlewareIPs[0] + " --port="+middlewarePorts[0];
				argsDifferentFromDefault2ndInstance += " --server="+middlewareIPs[1] + " --port="+middlewarePorts[1];
			}

			else {
				System.out.println("Error: memtier client arguments config (no case defined)");
				throw new RuntimeException("This is thrown intentionally");

			}

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

			//always start 1st instance
			MemtierCommand memtierRun = new MemtierCommand();
			int memtierInstance = 1;
			memtierRun.setMemtierCommand(testSetting.clientInTestIPs[i], testSetting.argsDifferentFromDefault1stInstance, memtierInstance);

			Thread threadMemtier = new Thread(new RunCommandInThread(memtierRun));
			clientThreads.add(threadMemtier);
			threadMemtier.start();

			//start a second instance if needed
			if(testSetting.start2ndMemtierInstance==true) {
				MemtierCommand memtierRun2 = new MemtierCommand();
				memtierInstance = 2;
				memtierRun2.setMemtierCommand(testSetting.clientInTestIPs[i], testSetting.argsDifferentFromDefault2ndInstance, memtierInstance);

				Thread threadMemtier2 = new Thread(new RunCommandInThread(memtierRun2));
				clientThreads.add(threadMemtier2);
				threadMemtier2.start();
			}

			//start a third instance if needed
			if(testSetting.start3thMemtierInstance==true) {
				MemtierCommand memtierRun3 = new MemtierCommand();
				memtierInstance = 3;
				memtierRun3.setMemtierCommand(testSetting.clientInTestIPs[i], testSetting.argsDifferentFromDefault2ndInstance, memtierInstance);

				Thread threadMemtier3 = new Thread(new RunCommandInThread(memtierRun3));
				clientThreads.add(threadMemtier3);
				threadMemtier3.start();
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
			String completeFileSuffix = testSetting.suffixForThisRun+"_Client="+c;
			copyFilesback(new String[] {clientIP},completeFileSuffix, testSetting.start2ndMemtierInstance, testSetting.start3thMemtierInstance, testSetting.experimentFolder);
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
				String completeFileSuffix = testSetting.suffixForThisRun+"_Middleware="+(m+1);
				copyFilesback(new String[] {middlewareIP},completeFileSuffix, false, false, testSetting.experimentFolder);
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

		public void setMemtierCommand(String memtierIP, String argsDifferentFromDefault, int memtierInstance) {
			setDefault();

			String memtierCommand = clientScriptTarget;
			if (memtierInstance==2) {
				memtierCommand = clientScriptTarget2ndInstance;
				defaultArguments.put("--json-out-file", "--json-out-file=json.txt2ndInst");
			}
			else if (memtierInstance==3) {
				memtierCommand = clientScriptTarget3thInstance;
				defaultArguments.put("--json-out-file", "--json-out-file=json.txt3thInst");
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



	//                 EXPERIMENTS
	//                 EXPERIMENTS
	//                 EXPERIMENTS
	//                 EXPERIMENTS
	//                 EXPERIMENTS
	//                 EXPERIMENTS
	//                 EXPERIMENTS

	//Experiment filenames
	//	outputClient_e=Baseline21_wl=ReadOnly_nVC=8_Rep=2_Client=3
	/*
	 * 
	 * 
	 * 
	 */

	// Experiment PARAMETERS

	//  String experimentFolder = "baseline21";
	//	int nServer=1;
	//	int nVirtualMachineClients=3;
	//	int memtierInstancesPerClient=1;
	//	int nCT_ThreadsPerMemtierInstance = 3;

	//	int nVC_VirtualClients = 4;
	//  int[] nVCSamples = {4,8,16,32};

	//	String workload = READONLY;

	//	int dataSize=64;
	//  int[] dataSizeSamples = {64, 256, 512, 1024};

	//	int nMW = 0;

	//	int nWorkerThreads = 0;
	//  int[] nWorkerThreadsSamples = {8, 32, 64};
	//	int repetitions = 3;


	/**
	 * Experiment according to section 2.1 of the report outline
	 */
	public void baseline21(){
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;


		String experimentFolder = "baseline21";
		int nServer=1;
		int nVirtualMachineClients=3;
		int memtierInstancesPerClient=1;
		int nCT_ThreadsPerMemtierInstance = 3;

		//int nVC_VirtualClients = 4;
		int[] nVCSamples = {4,8,16,32};

		String workload = READONLY;

		//int dataSize=64; 
		int[] dataSizeSamples = {64, 256, 512, 1024};

		int nMW = 0;
		int nWorkerThreads = 0;
		int repetitions = 3;


		for (int dataSize: dataSizeSamples) {
			//populate the needed values
			loadingMemcachedServer(dataSize);

			for (int rep = 1; rep <= repetitions; rep++) {

				for (int nVC_VirtualClients: nVCSamples) {

					String suffixForThisRun =
							"_e="+experimentFolder
							+"_nS="+nServer
							+"_nC="+nVirtualMachineClients
							+"_nInst="+memtierInstancesPerClient
							+"_nCT="+nCT_ThreadsPerMemtierInstance
							+"_nVC="+nVC_VirtualClients
							+"_wl="+getSimpleWorkloadName(workload)
							+"_dataSize="+dataSize
							+"_nMW="+nMW
							+"_nWT="+nWorkerThreads
							+"_rep="+rep;

					TestSetting experiment = new TestSetting(
							experimentFolder,
							nServer,
							nVirtualMachineClients,
							memtierInstancesPerClient,
							nCT_ThreadsPerMemtierInstance,
							nVC_VirtualClients,
							workload,
							dataSize,
							nMW,
							nWorkerThreads,
							suffixForThisRun
							);

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
	public void baseline22(){
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;


		String experimentFolder = "baseline22";
		int nServer=3;
		int nVirtualMachineClients=3;
		int memtierInstancesPerClient=3;
		int nCT_ThreadsPerMemtierInstance = 1;

		//int nVC_VirtualClients = 4;
		int[] nVCSamples = {4,8,16,32};

		String workload = READONLY;

		//int dataSize=64; 
		int[] dataSizeSamples = {64, 256, 512, 1024};

		int nMW = 0;
		int nWorkerThreads = 0;
		int repetitions = 3;


		for (int dataSize: dataSizeSamples) {
			//populate the needed values
			loadingMemcachedServer(dataSize);

			for (int rep = 1; rep <= repetitions; rep++) {

				for (int nVC_VirtualClients: nVCSamples) {

					String suffixForThisRun =
							"_e="+experimentFolder
							+"_nS="+nServer
							+"_nC="+nVirtualMachineClients
							+"_nInst="+memtierInstancesPerClient
							+"_nCT="+nCT_ThreadsPerMemtierInstance
							+"_nVC="+nVC_VirtualClients
							+"_wl="+getSimpleWorkloadName(workload)
							+"_dataSize="+dataSize
							+"_nMW="+nMW
							+"_nWT="+nWorkerThreads
							+"_rep="+rep;

					TestSetting experiment = new TestSetting(
							experimentFolder,
							nServer,
							nVirtualMachineClients,
							memtierInstancesPerClient,
							nCT_ThreadsPerMemtierInstance,
							nVC_VirtualClients,
							workload,
							dataSize,
							nMW,
							nWorkerThreads,
							suffixForThisRun
							);

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
	public void baseline31(){
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;


		String experimentFolder = "baseline31";
		int nServer=1;
		int nVirtualMachineClients=3;
		int memtierInstancesPerClient=1;
		int nCT_ThreadsPerMemtierInstance = 2;

		//int nVC_VirtualClients = 4;
		int[] nVCSamples = {4,8,16,32};

		String workload = READONLY;

		//int dataSize=64; 
		int[] dataSizeSamples = {64, 256, 512, 1024};

		int nMW = 1;
		//int nWorkerThreads = 0;
		int[] nWorkerThreadsSamples = {8, 32, 64};
		int repetitions = 3;


		for (int dataSize: dataSizeSamples) {
			//populate the needed values
			loadingMemcachedServer(dataSize);

			for (int rep = 1; rep <= repetitions; rep++) {

				for (int nVC_VirtualClients: nVCSamples) {

					for (int nWorkerThreads: nWorkerThreadsSamples) {

						String suffixForThisRun =
								"_e="+experimentFolder
								+"_nS="+nServer
								+"_nC="+nVirtualMachineClients
								+"_nInst="+memtierInstancesPerClient
								+"_nCT="+nCT_ThreadsPerMemtierInstance
								+"_nVC="+nVC_VirtualClients
								+"_wl="+getSimpleWorkloadName(workload)
								+"_dataSize="+dataSize
								+"_nMW="+nMW
								+"_nWT="+nWorkerThreads
								+"_rep="+rep;

						TestSetting experiment = new TestSetting(
								experimentFolder,
								nServer,
								nVirtualMachineClients,
								memtierInstancesPerClient,
								nCT_ThreadsPerMemtierInstance,
								nVC_VirtualClients,
								workload,
								dataSize,
								nMW,
								nWorkerThreads,
								suffixForThisRun
								);

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
	public void baseline32(){
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;


		String experimentFolder = "baseline32";
		int nServer=3;
		int nVirtualMachineClients=3;
		int memtierInstancesPerClient=1;
		int nCT_ThreadsPerMemtierInstance = 2;

		//int nVC_VirtualClients = 4;
		int[] nVCSamples = {4,8,16,32};

		String workload = READONLY;

		//int dataSize=64; 
		int[] dataSizeSamples = {64, 256, 512, 1024};

		int nMW = 1;
		//int nWorkerThreads = 0;
		int[] nWorkerThreadsSamples = {8, 32, 64};
		int repetitions = 3;


		for (int dataSize: dataSizeSamples) {
			//populate the needed values
			loadingMemcachedServer(dataSize);

			for (int rep = 1; rep <= repetitions; rep++) {

				for (int nVC_VirtualClients: nVCSamples) {

					for (int nWorkerThreads: nWorkerThreadsSamples) {

						String suffixForThisRun =
								"_e="+experimentFolder
								+"_nS="+nServer
								+"_nC="+nVirtualMachineClients
								+"_nInst="+memtierInstancesPerClient
								+"_nCT="+nCT_ThreadsPerMemtierInstance
								+"_nVC="+nVC_VirtualClients
								+"_wl="+getSimpleWorkloadName(workload)
								+"_dataSize="+dataSize
								+"_nMW="+nMW
								+"_nWT="+nWorkerThreads
								+"_rep="+rep;

						TestSetting experiment = new TestSetting(
								experimentFolder,
								nServer,
								nVirtualMachineClients,
								memtierInstancesPerClient,
								nCT_ThreadsPerMemtierInstance,
								nVC_VirtualClients,
								workload,
								dataSize,
								nMW,
								nWorkerThreads,
								suffixForThisRun
								);

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
	 * Experiment according to section 3.3 of the report outline
	 */
	public void baseline33(){
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;


		String experimentFolder = "baseline33";
		int nServer=1;
		int nVirtualMachineClients=3;
		int memtierInstancesPerClient=2;
		int nCT_ThreadsPerMemtierInstance = 1;

		//int nVC_VirtualClients = 4;
		int[] nVCSamples = {4,8,16,32};

		String workload = READONLY;

		//int dataSize=64; 
		int[] dataSizeSamples = {64, 256, 512, 1024};

		int nMW = 2;
		//int nWorkerThreads = 0;
		int[] nWorkerThreadsSamples = {8, 32, 64};
		int repetitions = 3;


		for (int dataSize: dataSizeSamples) {
			//populate the needed values
			loadingMemcachedServer(dataSize);

			for (int rep = 1; rep <= repetitions; rep++) {

				for (int nVC_VirtualClients: nVCSamples) {

					for (int nWorkerThreads: nWorkerThreadsSamples) {

						String suffixForThisRun =
								"_e="+experimentFolder
								+"_nS="+nServer
								+"_nC="+nVirtualMachineClients
								+"_nInst="+memtierInstancesPerClient
								+"_nCT="+nCT_ThreadsPerMemtierInstance
								+"_nVC="+nVC_VirtualClients
								+"_wl="+getSimpleWorkloadName(workload)
								+"_dataSize="+dataSize
								+"_nMW="+nMW
								+"_nWT="+nWorkerThreads
								+"_rep="+rep;

						TestSetting experiment = new TestSetting(
								experimentFolder,
								nServer,
								nVirtualMachineClients,
								memtierInstancesPerClient,
								nCT_ThreadsPerMemtierInstance,
								nVC_VirtualClients,
								workload,
								dataSize,
								nMW,
								nWorkerThreads,
								suffixForThisRun
								);

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
	 * Experiment according to section 3.4 of the report outline
	 */
	public void baseline34(){
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;


		String experimentFolder = "baseline34";
		int nServer=3;
		int nVirtualMachineClients=3;
		int memtierInstancesPerClient=2;
		int nCT_ThreadsPerMemtierInstance = 1;

		//int nVC_VirtualClients = 4;
		int[] nVCSamples = {4,8,16,32};

		String workload = READONLY;

		//int dataSize=64; 
		int[] dataSizeSamples = {64, 256, 512, 1024};

		int nMW = 2;
		//int nWorkerThreads = 0;
		int[] nWorkerThreadsSamples = {8, 32, 64};
		int repetitions = 3;


		for (int dataSize: dataSizeSamples) {
			//populate the needed values
			loadingMemcachedServer(dataSize);

			for (int rep = 1; rep <= repetitions; rep++) {

				for (int nVC_VirtualClients: nVCSamples) {

					for (int nWorkerThreads: nWorkerThreadsSamples) {

						String suffixForThisRun =
								"_e="+experimentFolder
								+"_nS="+nServer
								+"_nC="+nVirtualMachineClients
								+"_nInst="+memtierInstancesPerClient
								+"_nCT="+nCT_ThreadsPerMemtierInstance
								+"_nVC="+nVC_VirtualClients
								+"_wl="+getSimpleWorkloadName(workload)
								+"_dataSize="+dataSize
								+"_nMW="+nMW
								+"_nWT="+nWorkerThreads
								+"_rep="+rep;

						TestSetting experiment = new TestSetting(
								experimentFolder,
								nServer,
								nVirtualMachineClients,
								memtierInstancesPerClient,
								nCT_ThreadsPerMemtierInstance,
								nVC_VirtualClients,
								workload,
								dataSize,
								nMW,
								nWorkerThreads,
								suffixForThisRun
								);

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
	 * Experiment according to section 4 of the report outline
	 */
	public void twoKAnalyse(){
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;


		String experimentFolder = "twoKAnalyse";

		//int nServer=3;
		int[] nServers = {1, 3};

		int nVirtualMachineClients=3;

		//int memtierInstancesPerClient=2; //special
		//int nCT_ThreadsPerMemtierInstance = 1; //special

		//int nVC_VirtualClients = 4;
		int[] nVCSamples = {4,8,16,32};

		String workload = READONLY;

		//int dataSize=64; 
		int[] dataSizeSamples = {256};

		//int nMW = 2;
		int[] nMWs = {1, 2};

		//int nWorkerThreads = 0;
		int[] nWorkerThreadsSamples = {8, 32, 64};

		int repetitions = 3;


		for (int dataSize: dataSizeSamples) {
			//populate the needed values
			loadingMemcachedServer(dataSize);

			for (int rep = 1; rep <= repetitions; rep++) {

				for (int nVC_VirtualClients: nVCSamples) {

					for (int nWorkerThreads: nWorkerThreadsSamples) {

						for (int nServer: nServers) {

							for (int nMW: nMWs) {

								int memtierInstancesPerClient = nMW;
								int nCT_ThreadsPerMemtierInstance = (nMW==1 ? 2:1);
								assert(memtierInstancesPerClient*nCT_ThreadsPerMemtierInstance==2);

								String suffixForThisRun =
										"_e="+experimentFolder
										+"_nS="+nServer
										+"_nC="+nVirtualMachineClients
										+"_nInst="+memtierInstancesPerClient
										+"_nCT="+nCT_ThreadsPerMemtierInstance
										+"_nVC="+nVC_VirtualClients
										+"_wl="+getSimpleWorkloadName(workload)
										+"_dataSize="+dataSize
										+"_nMW="+nMW
										+"_nWT="+nWorkerThreads
										+"_rep="+rep;

								TestSetting experiment = new TestSetting(
										experimentFolder,
										nServer,
										nVirtualMachineClients,
										memtierInstancesPerClient,
										nCT_ThreadsPerMemtierInstance,
										nVC_VirtualClients,
										workload,
										dataSize,
										nMW,
										nWorkerThreads,
										suffixForThisRun
										);

								numExperiments++;
								doBenchmarks(experiment);
							}
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
