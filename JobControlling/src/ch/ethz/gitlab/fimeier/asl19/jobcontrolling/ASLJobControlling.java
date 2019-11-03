package ch.ethz.gitlab.fimeier.asl19.jobcontrolling;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.gitlab.fimeier.asl19.jobcontrolling.evaluation.DataProcessing;
import ch.ethz.gitlab.fimeier.asl19.jobcontrolling.measuring.Benchmarks;


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
	 * jedes Experiment macht ein restart des memcached servers und lädt ihn....
	 * jeweils immer alle clients server mit der benötigten value-size
	 */
	//public static boolean loadIt = false;
	public static int MEMTIERLOADIterations = 1;
	public static int MEMTIERLOADTIMEWriteOnly = 6; //Ziel: 10'000 Schlüssel schreiben, bei 5000writes/sec
	public static int MEMTIERLOADTIMEReadOnly = 3;

	/*
	 * deployment of needed "software" for individual task's
	 */
	public static boolean createFolders = true;
	public static boolean deployNMONScript = true;

	public static boolean deployClientScripts = true;

	public static boolean deployMiddlewareScripts = true;
	public static boolean deployMiddlewareJava = true;

	//public static boolean deployServerScripts = false; //!!!??????überlege ob das nötig ist


	/*
	 * 
	 * Timing-Parameters for Benchmarks
	 * 
	 * 
	 */
	//delay before the memtierclient get started
	public long STARTUPTIMEMIDDLEWARE = 2000;

	//time in seconds for the memtier_benchmark to be running
	public int MEMTIERTESTTIME = 80;//80 for final experiments
	/*
	 * MyMiddleware parameters: start, measure, kill
	 */
	public long tWAITBEFOREMEASUREMENTS = STARTUPTIMEMIDDLEWARE - 500; //effective start is with first client sending data
	public long tTIMEFORMEASUREMENTS = MEMTIERTESTTIME*1000 + 1500;
	public long JAVAMIDDLEWAREKILLDELAY = STARTUPTIMEMIDDLEWARE + tWAITBEFOREMEASUREMENTS +tTIMEFORMEASUREMENTS +1000; //~2+1.5+65.5+1=68


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
	public static String WRITEONLY = " --ratio=1:0";
	public static String READONLY = " --ratio=0:1";
//	public static String RATIO11 = " --ratio=1:1";
//	public static String RATIO13 = " --ratio=1:3";
//	public static String RATIO16 = " --ratio=1:6";
//	public static String RATIO19 = " --ratio=1:9";
	public static String getSimpleWorkloadName(String wl) { 
		if (wl.equals(WRITEONLY))
			return "WriteOnly";
		if (wl.equals(READONLY))
			return "ReadOnly";

//		if (wl.equals(RATIO11))
//			return "ratio=1t1";
//
//		if (wl.equals(RATIO13))
//			return "ratio=1t3";
//
//		if (wl.equals(RATIO16))
//			return "ratio=1t6";
//
//		if (wl.equals(RATIO19))
//			return "ratio=1t9";

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
	static public String[] defaultColorsThroughputRead = {"#33ccff", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"}; 
	static public String[] defaultColorsThroughputWrite = {"#33ccff", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"};
	static public String[] defaultColorsLatencyRead = {"#ffcc99", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"};
	static public String[] defaultColorsLatencyWrite = {"#661400", "#33cc33","#cc3300", "#000066", "#5370AF", "#8BA068"};
	static public String[] unknownTypeColors = {"#D30FAF", "#D30FAF","#D30FAF", "#D30FAF", "#D30FAF", "#D30FAF"};


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
	public static String nmonScriptSource = scriptsFolder + "nmonHelper.bash";
	public static String nmonScriptTarget = workingFolder + "nmonHelper.bash";
	public static String nmonCaptureDir = experimentsBaseFolder + "NMON/";



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
	public static String clientScriptTarget3thInstance = workingFolder+"runMemtierBenchmark3thInstance.bash";

	
	public static String clientScriptSource = scriptsFolder + "runMemtierBenchmark.bash";
	
	public static String clientScriptSource2ndInstance = scriptsFolder + "runMemtierBenchmark2ndInstance.bash";
	public static String clientScriptSource3thInstance = scriptsFolder + "runMemtierBenchmark3thInstance.bash";


	public static String clientOutputFile = workingFolder+"outputClient";
	public static String clientOutputJSONFile = workingFolder+"json.txt";
	public static String clientErrorFile = workingFolder+"errorClient";
	public static String[] clientFiles = new String[] {clientOutputFile,clientErrorFile,clientOutputJSONFile};
	public static String[] clientFilesIncluding2ndInstance = new String[] {clientOutputFile,clientErrorFile,clientOutputJSONFile,clientOutputFile+"2ndInst",clientErrorFile+"2ndInst",clientOutputJSONFile+"2ndInst"};
	public static String[] clientFilesIncluding3thInstance = new String[] {clientOutputFile,clientErrorFile,clientOutputJSONFile,
																			clientOutputFile+"2ndInst",clientErrorFile+"2ndInst",clientOutputJSONFile+"2ndInst",
																			clientOutputFile+"3thInst",clientErrorFile+"3thInst",clientOutputJSONFile+"3thInst"
																			};

	
	public static String[] serverIPs = new String[] {"10.0.0.31","10.0.0.32","10.0.0.33"};
	public static String[] serverPorts = new String[] {"12333","12444","12555"};
	public static String[] serverIpandPorts = new String[] {serverIPs[0]+":"+serverPorts[0],serverIPs[1]+":"+serverPorts[1],serverIPs[2]+":"+serverPorts[2]};


	public static void main(String[] args) {

		/*
		 * create folders
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
			String[] experimentFoldersToCreate = {
					"loadIt",
					"baseline21",
					"baseline22",
					"baseline31",
					"baseline32",
					"baseline33",
					"baseline34",
					"twoKAnalyse",
					"testCompleteSystem",
					"NMON"
					};
			//create the experiment Base folder 
			createFolders(ips,ASLJobControlling.experimentsBaseFolder);
			for (String experimentFolder: experimentFoldersToCreate) {
				String folderToCreate = ASLJobControlling.experimentsBaseFolder + experimentFolder;
				createFolders(ips,folderToCreate);
			}
		}

		/*
		 * deploy scripts/software
		 */
		
		if (deployNMONScript) {
			deployScripts(clientIPs, nmonScriptSource);
			deployScripts(middlewareIPs, nmonScriptSource);
			deployScripts(serverIPs, nmonScriptSource);
		}
		if (deployClientScripts) {
			deployScripts(clientIPs, clientScriptSource);
			deployScripts(clientIPs, clientScriptSource2ndInstance);
			deployScripts(clientIPs, clientScriptSource3thInstance);
		}
		if (deployMiddlewareScripts) {
			deployScripts(middlewareIPs, middlewareScriptSource);
		}
		if (deployMiddlewareJava) {
			deployScripts(middlewareIPs, middlewareJavaSource);
		}


		/*
		 * run Benchmarks
		 */
		Benchmarks benchmark = new Benchmarks();
		benchmark.runBenchmarks();

		/*
		 * do DataProcessing (plots,...)
		 */
		//DataProcessing dataprocessing = new DataProcessing();
		//dataprocessing.runDataProcessing();

		System.out.println("the E N D of ASLJobControlling!!!!");


	}


	/*
	 * 
	 * Helpers
	 * 
	 * createFolders()
	 * copyFilesback()
	 * scpJopsExecutor()
	 * sshJopsExecutor()
	 */

	public static void createFolders(String[] ips, String folder) {
		List<String[]> sshJobs = new ArrayList<String[]>(); 

		String folderexists = "[ ! -d "+folder+" ] && mkdir "+folder;

		for(String ip: ips) {
			String[] sshJob = {"ssh", ip, folderexists};
			sshJobs.add(sshJob);
		}
		sshJopsExecutor(sshJobs);

	}

	public static void copyFilesback(String[] nodesToCollectFrom, String _suffix, boolean copy2ndInstance, boolean copy3thInstance, String experimentFolder) {
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
				if(copy3thInstance) {
					absolutePaths=ASLJobControlling.clientFilesIncluding3thInstance;
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

	public static void scpJopsExecutor(List<String[]> scpJobs) {
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

			String[] copyFile = {"scp","-p",fileSource, fileDestination};
			scpJobs.add(copyFile);
		}
		scpJopsExecutor(scpJobs);
	}



	public static void writeFile(String fileName, String content) {
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
	
	public static void appendFile(String fileName, String content) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileOutputStream(new File(fileName),true));
			writer.write(content);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int[] parseIntArray(String _in) {
		String[] in = _in.split(",");
		int n = in.length;
		int[] out = new int[n];
		for (int i=0; i<n;i++) {
			out[i] = Integer.parseInt(in[i]);
		}
		return out;
	}

	public static String[] parseStringArray(String _in) {
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






}