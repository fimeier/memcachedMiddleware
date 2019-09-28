package ch.ethz.gitlab.fimeier.asl19;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class deleteThis extends DataProcessing {


	//alte art statistiken zu generieren???
	//alte art statistiken zu generieren???
	//alte art statistiken zu generieren???
	/*
	 * 
	 * 
	 * alte art statistiken zu generieren???
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
	public void statisticsThroughputLatencyClientorMiddleware_DELETE(JsonThroughputLatencyConfig_DELETE conf) {
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
								HashMap<String,Double> extractedData = jsonMemtierBenchmarkAllStats_DELETE(absoluteFilename, String.valueOf(nWorkerThreads));
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
								HashMap<String,Double> extractedData = jsonMemtierBenchmarkAllStats_DELETE(absoluteFilename, workLoadJsonAllStatsKey[wl]);
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
				String plotConfig = createPlotConfig_DELETE(plotConf, conf, true);
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
				String plotConfig = createPlotConfig_DELETE(plotConf, conf, false);
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

		createGnuPlotScript_DELETE(absoluteScriptName,gnuplotConfigFiles);

		//create tex include
		createLatexInclude_DELETE(experimentsBaseFolder+experiment+"pictures.tex", pictureOutputName);

	}





	public static class PlotConfig_DELETE {
		//wenn fertig als info hinzufügen plot config is just for one picture to make it easier
		//-1 == default

		/**
		 * 
		 * @param _experiment source for data
		 * @param _mode default do memtier and middleware (needed of some data is not available (baseline21 => no middleware)
		 * 
		 * habe hier aufgehört
		 */
		public PlotConfig_DELETE(String _experiment, String _workLoad, String _mode) {

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



	public static class JsonThroughputLatencyConfig_DELETE{
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



	static void createLatexInclude_DELETE(String absoluteTexIncludeName, List<String> pictureOutputNameDropbox) {
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


	static void createGnuPlotScript_DELETE(String absoluteScriptName, List<String> gnuplotConfigFiles) {
		String script = "";
		script += "#!/bin/bash\n";
		for (String conf: gnuplotConfigFiles) {
			String cmd = "gnuplot -c "+conf;
			script += cmd +"\n";
		}
		writeFile(absoluteScriptName,script);
		System.out.println("writing file..."+absoluteScriptName);
	}


	static String createPlotConfig_DELETE(HashMap<String,String> plotConf, JsonThroughputLatencyConfig_DELETE conf, boolean isTp) {
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


	/**
	 * 
	 * @param file
	 * @param ob "Sets" "Gets" "Totals"
	 */
	public HashMap<String,Double> jsonMemtierBenchmarkAllStats_DELETE(String file, String ob) {
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

				double value = getPercentile_DELETE(object, "GET", percentile);

				jsonReader.close();
				fileReader.close();
				extractedData = new HashMap<>();
				extractedData.put("Latency", value);

				return extractedData;

			}

			extractedData = getmemtierAllStatsJson_DELETE(object, ob);



			jsonReader.close();
			fileReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return extractedData;
	}


	/**
	 * read out the CDF.... not finished
	 * @param object
	 * @param command SET or GET
	 */
	public double getPercentile_DELETE(JsonObject object, String command, int percentile){

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
	 * @param object
	 * @param command Sets, Gets or Totals
	 * @return Ops/sec, hits/sec, Misses/sec and KB/sec
	 */
	public static HashMap<String,Double> getmemtierAllStatsJson_DELETE(JsonObject object, String command){
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
	 */	//hier alle consolidateStatisticsBaseline21() etc methoden....


	/*
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


		//create client stuff

		statisticsThroughputLatencyClientorMiddleware(conf);

		//create midleware stuff


		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 1; //# files inner loop
		conf.titleLableThroughput = "Throughput vs. Number of clients (middleware data)";
		conf.titleLableLatency = "Latency vs. Number of clients (middleware data)";


		statisticsThroughputLatencyClientorMiddleware(conf);


		//queue stuff

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


		//service time memcached
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


		//create client stuff

		statisticsThroughputLatencyClientorMiddleware(conf);

		//create midleware stuff

		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 2; //# files inner loop
		conf.titleLableThroughput = "Throughput vs. Number of clients (middleware data)";
		conf.titleLableLatency = "Latency vs. Number of clients (middleware data)";


		statisticsThroughputLatencyClientorMiddleware(conf);


		//queue stuff

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


		//service time memcached

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


		//create client stuff

		statisticsThroughputLatencyClientorMiddleware(conf);

		//create midleware stuff


		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 2; //# files inner loop
		conf.titleLableThroughput = "Throughput vs. Number of clients (middleware data)";
		conf.titleLableLatency = "Latency vs. Number of clients (middleware data)";

		statisticsThroughputLatencyClientorMiddleware(conf);


		//queue stuff

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


		//service time memcached

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

			conf.nVCsamples = new int[] {1,3,6,9};
			conf.nWorkerThreadsTodoList = new int[] {0,25,50,75,90,99}; //steht für avg, 25-percentile, 50-percentile...
			conf.multiGets=true;
			conf.baseline3=true;
			conf.multiGetsWorkloads = new String[] {RATIO11, RATIO13, RATIO16, RATIO19};
			conf.defaultRep = new int[] {1,2,3,4,5,6};
			conf.maxThroughputThread = "_nWorkerThreads64_";

			//create client stuff

			statisticsThroughputLatencyClientorMiddleware(conf);
		}
		cons51Rest("shardedCase51/", "Sharded");

	}

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




		//create midleware stuff


		conf.middlewareMode = true;
		conf.expectedNumberOfFiles = 2; //# files inner loop
		//conf.titleLableThroughput = "Throughput vs. Number of clients (middleware data)";
		//conf.titleLableLatency = "Latency vs. Number of clients (middleware data)";

		conf.ySize = 5500;

		statisticsThroughputLatencyClientorMiddleware(conf);

		//if (true)
		//return;


		//queue stuff

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

		//service time memcached

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

		conf.nVCsamples = new int[] {1,3,6,9};
		conf.nWorkerThreadsTodoList = new int[] {0,25,50,75,90,99}; //steht für avg, 25-percentile, 50-percentile...
		conf.multiGets=true;
		conf.baseline3=true;
		conf.multiGetsWorkloads = new String[] {RATIO11, RATIO13, RATIO16, RATIO19};
		conf.defaultRep = new int[] {1,2,3,4,5,6};
		conf.maxThroughputThread = "_nWorkerThreads64_";

		//create client stuff

		statisticsThroughputLatencyClientorMiddleware(conf);

		cons51Rest("nonshardedCase52/", "Non-Sharded ");

	}
	 */


}
