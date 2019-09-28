package ch.ethz.gitlab.fimeier.asl19;

public class deleteThisBenchmarks extends Benchmarks{
	
	



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
	public void baseline21ASL18(){
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
					TestSetting experiment = new TestSetting(nServer, nVirtualMachineClients, nVC, nCT, workload, suffixForThisRun, nMW, 
							nWorkerThreads, multiGetSize, shardedReading, experimentFolder);

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
	public void baseline22ASL18() {
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
	public void baseline31ASL18() {
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
	public void baseline32ASL18() {
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
	public void fullSystem41ASL18() {
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
	public void multiGetsASL18(boolean _shardedReading) {
		long startExperiment = System.currentTimeMillis();
		int numExperiments = 0;

		// paramters to change for real experiment
		int repetitions = 6; //CHANGE!!!!

		int[] nWorkerThreadsTodoList = {8, 16, 32, 64, 128}; //TODO max throughput wird hier gesucht

		String experimentFolder = (_shardedReading) ? "shardedCase51": "nonshardedCase52";
		String[] workloads = {"dummy removed"};//{RATIO11, RATIO13, RATIO16, RATIO19};
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
	public void twoKAnalysis6ASL18() {
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
