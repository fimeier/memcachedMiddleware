package ch.ethz.gitlab.fimeier.asl19.jobcontrolling.evaluation;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import ch.ethz.gitlab.fimeier.asl19.jobcontrolling.ASLJobControlling;

public class DataProcessing extends ASLJobControlling {

	public ArrayList<KeyProperties> middlewareDataKeys = null;

	public void runDataProcessing() {

		if (consolidateStatistics) {
			if(consolidateStatisticsBaseline21) {
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline21/numClients plots **************************************\n");
				String configAggregation=plotConfigBaseFolder+"Baseline21numClients_MemtierClient";
				createPlots(configAggregation);
				
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline21/valueSize plots **************************************\n");
				configAggregation=plotConfigBaseFolder+"Baseline21valueSize_MemtierClient";
				createPlots(configAggregation);
			}
			if(consolidateStatisticsBaseline22) {
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline22/numClients plots **************************************\n");
				String configAggregation=plotConfigBaseFolder+"Baseline22numClients_MemtierClient";
				createPlots(configAggregation);
				
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline22/valueSize plots **************************************\n");
				configAggregation=plotConfigBaseFolder+"Baseline22valueSize_MemtierClient";
				createPlots(configAggregation);

			
			}

			if(consolidateStatisticsBaseline31) {
				//consolidateStatisticsBaseline31();
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating baseline31/ReadOnly plots **************************************\n");
				String configAggregation=plotConfigBaseFolder+"configAggregationReadOnlyBaseline31";
				createPlots(configAggregation);

			
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline31/ReadOnly plots **************************************\n");
				configAggregation=plotConfigBaseFolder+"configAggregationReadOnlyBaseline31Client";
				createPlots(configAggregation);


			}
			if(consolidateStatisticsBaseline32) {
				//consolidateStatisticsBaseline32();
				//consolidateStatisticsBaseline31();
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating baseline32/ReadOnly plots **************************************\n");
				String configAggregation=plotConfigBaseFolder+"configAggregationReadOnlyBaseline32";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline32/ReadOnly plots **************************************\n");
				configAggregation=plotConfigBaseFolder+"configAggregationReadOnlyBaseline32Client";
				createPlots(configAggregation);



			}
			if(consolidateStatisticsfullSystem41) {
				//consolidateStatisticsfullSystem41();
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating fullSystem41 plots *******************************************\n");
				String configAggregation=plotConfigBaseFolder+"configAggregationfullSystem41";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT fullSystem41 plots *******************************************\n");
				configAggregation=plotConfigBaseFolder+"configAggregationfullSystem41Client";
				createPlots(configAggregation);				
			}

			
			
			
			

			/**
			 * create special plots
			 */
			if (createConsolitatedPlots) {
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** consolidate PLOTS **************************************\n");
				String configAggregation=plotConfigBaseFolder+"configAggregationAllLines";
				createSpecialPlots(configAggregation,"special Plot: allLines ");


			}


			return;
		}



	}

	public void createPlots(String configAggregation) {

		/*
		 * selected keywords from finalstats: --alter Kommentar?--
		 * throughputSet","throughputGet","throughputMultiGet","throughputAllCommands",
		 * "throughputGetAllKeys","avgQueueWaitingTime","avgQueueLength","avgMemcachedResponseTimeSet",
		 * "avgMemcachedResponseTimeGet","avgMemcachedResponseTimeMultiGet","avgWorkerThreadResponseTimeSet",
		 * "avgWorkerThreadResponseTimeGet","avgWorkerThreadResponseTimeMultiGet","avgMiddlewareResponseTimeSet","
		 * avgMiddlewareResponseTimeGet","avgMiddlewareResponseTimeMultiGet"
		 */
		//
		middlewareDataKeys = new ArrayList<>();
		String jobResult = createAllStatistics(configAggregation);
		String plotConfig = jobResult.split(" ")[0];
		String texFile = jobResult.split(" ")[1];
		callGnuPlotAndPDFLatex(plotConfig,texFile);
	}


	public void callGnuPlotAndPDFLatex(String plotConfigFile, String texFile) {
		//call gnuplot
		String[] cmd = new String[]{"/usr/bin/gnuplot","-c", plotConfigFile};
		executeSimpleLocalCmd(cmd);

		//call latex
		String c ="/usr/bin/pdflatex";
		String arg1 = "-synctex=1";
		String arg2 ="-interaction=nonstopmode";
		//String oF ="-output-directory=/home/fimeier/Dropbox/00ETH/HS18/05_Advanced_Systems_Lab/git/asl-fall18-project/JobControlling/experiments/generatedPlots";
		String oF ="-output-directory="+experimentsBaseFolder+"generatedPlots";
		cmd = new String[] {c,arg1,arg2,oF,texFile};
		executeSimpleLocalCmd(cmd);
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
			aggregationFolder = experimentsBaseFolder+experimentFolder+"aggregationMemtierClient/";
			aggregatedDataOutputFile = experimentsBaseFolder+experimentFolder+experimentSubfolder+"MemtierClient";
			latexOutputFile = experimentsBaseFolder+experimentFolder+experimentName+"_MemtierClient.tex";
			gnuPlotConfigOutputFile = experimentsBaseFolder+experimentFolder+"aggregationMemtierClient/PlotConfigMemtierClient"+experimentSubfolder;
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
		 * create folders:
		 * aggregationFolder
		 */
		String[] foldersToCreate = {aggregationFolder, experimentsBaseFolder+"generatedPlots", experimentsBaseFolder+"generatedPlots/allLines/source"};
		for (String folder: foldersToCreate) {
			new File(folder).mkdirs();
		}



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
		 * Filter to select the files
		 * Used in the loop.. compare the containsTemp String
		 */
		List<String[]> filterInnerLoopContains = new ArrayList<>();
		for (String x: xAxisFilter) {
			for (String line: linesFilter) {
				for (String rep: repetitionFilter) {
					String[] temp = {x+"_",line+"_",rep+"_"};
					filterInnerLoopContains.add(temp);
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


		
		/*
		 * 
		 * 
		 * 
		 * 
		 * 
		 * START aggregation aggregation aggregation aggregation aggregation aggregation aggregation aggregation aggregation
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

					//String containsTemp = filterInnerLoopContains.get(i);
					//add here other strings that should be in the filename....
					//String[] contains = {containsTemp};
					String[] contains = filterInnerLoopContains.get(i);
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

					//add the runs if needed for plotting => meint dass Werte der Runs hinzugefügt werden,
					//auch wenn eigentlich nur avg über alle runs geplottet wird
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
		 * 
		 * 
		 * 
		 * 
		 * 
		 * ENDE aggregation aggregation aggregation aggregation aggregation aggregation aggregation aggregation aggregation
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
			String pdfFileName = aggregationFolder+experimentSubfolder+"_"+middlewareDataKeys.get(k).key+".pdf";

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



	public void createSpecialPlots(String configAggregation,String texHeader) {
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




	public void createLatexInclude(ArrayList<String> pdfOutputFiles, String latexOutputFile, ArrayList<String> keys, String texHeader) {

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
	 * read out the CDF.... not finished (alter Kommentar?)
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




	private HashMap<String, String> parseConfigAggregation(String absoluteFilename) {
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
					key = prefixKeyProperties + key; //???
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






	public HashMap<String, Double> middlewareGetFinalStats(String absoluteFilename) {
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

	public ArrayList<String> getFilesByFilter(final File folder, String startsWith, String[] contains, int expectedNumberOfFiles) {

		ArrayList<String> filesFound = new ArrayList<>();

		//Nur debugging
//		String contAsString = "";
//		if (contains != null) {
//			for (int i = 0; i < contains.length; i++) {
//				contAsString += contains[i] +", ";
//			}
//		}

		File[] filesForAggregation = folder.listFiles(getFilter(startsWith, contains));
		//debugg
		String fname = folder.getAbsolutePath();


		//System.out.println("Found "+filesForAggregation.length +" many files with this filter startsWith="+startsWith +" contains="+contAsString);
		if (filesForAggregation.length !=expectedNumberOfFiles) {
			System.out.println("ERRRRRRRRRRRRRRRRRRRRRRROR expectedNumberOfFiles....="+expectedNumberOfFiles+" vs "+filesForAggregation.length );
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

	public ArrayList<Double> calculateStats(ArrayList<Double> values){
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






}
