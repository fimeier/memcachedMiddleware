import java.util.ArrayList;
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

		/*
		 * deploy scripts
		 */
		if (deployClientScripts) {
			deployScripts(clientIPs, clientScriptSource);
			deployScripts(clientIPs, clientScriptSource2ndInstance);
		}

		if (deployMiddlewareScripts) {
			deployScripts(middlewareIPs, middlewareScriptSource);
		}
		if (deployMiddlewareJava) {
			deployScripts(middlewareIPs, middlewareJavaSource);
		}

		/*if (deployServerScripts) {
			deployScripts(serverIPs, serverScriptSoure);
		}*/
		
		//Test: inlcude this in each experiment with the needed values
		loadingMemcachedServer(4096);



		System.out.println("*****************************************************************************************************************\n");
		System.out.println("***************************************** starting ASLJobControlling().baseline21() *****************************\n");
		//baseline21();



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
					//new ASLJobControlling().doBenchmarks(experiment);
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
					//new ASLJobControlling().doBenchmarks(experiment);
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
						//new ASLJobControlling().doBenchmarks(experiment);

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
						new ASLJobControlling().doBenchmarks(experiment);

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
						new ASLJobControlling().doBenchmarks(experiment);

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
					new ASLJobControlling().doBenchmarks(experiment);

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
							new ASLJobControlling().doBenchmarks(experiment);
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
