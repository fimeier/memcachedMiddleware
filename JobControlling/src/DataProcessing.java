
public class DataProcessing extends ASLJobControlling {

	public void runDataProcessing() {

		if (consolidateStatistics) {
			if(consolidateStatisticsBaseline21) {
				//consolidateStatisticsBaseline21();
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline21/ReadOnly plots **************************************\n");
				String configAggregation=experimentsBaseFolder+"configAggregationReadOnlyBaseline21Client";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline21/WriteOnly plots **************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationWriteOnlyBaseline21Client";
				createPlots(configAggregation);
			}
			if(consolidateStatisticsBaseline22) {
				//consolidateStatisticsBaseline22();
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline22/ReadOnly plots **************************************\n");
				String configAggregation=experimentsBaseFolder+"configAggregationReadOnlyBaseline22Client";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline22/WriteOnly plots **************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationWriteOnlyBaseline22Client";
				createPlots(configAggregation);
			}

			if(consolidateStatisticsBaseline31) {
				//consolidateStatisticsBaseline31();
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating baseline31/ReadOnly plots **************************************\n");
				String configAggregation=experimentsBaseFolder+"configAggregationReadOnlyBaseline31";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating baseline31/WriteOnly plots **************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationWriteOnlyBaseline31";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline31/ReadOnly plots **************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationReadOnlyBaseline31Client";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline31/WriteOnly plots **************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationWriteOnlyBaseline31Client";
				createPlots(configAggregation);


			}
			if(consolidateStatisticsBaseline32) {
				//consolidateStatisticsBaseline32();
				//consolidateStatisticsBaseline31();
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating baseline32/ReadOnly plots **************************************\n");
				String configAggregation=experimentsBaseFolder+"configAggregationReadOnlyBaseline32";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating baseline32/WriteOnly plots **************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationWriteOnlyBaseline32";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline32/ReadOnly plots **************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationReadOnlyBaseline32Client";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT baseline32/WriteOnly plots **************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationWriteOnlyBaseline32Client";
				createPlots(configAggregation);


			}
			if(consolidateStatisticsfullSystem41) {
				//consolidateStatisticsfullSystem41();
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating fullSystem41 plots *******************************************\n");
				String configAggregation=experimentsBaseFolder+"configAggregationfullSystem41";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT fullSystem41 plots *******************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationfullSystem41Client";
				createPlots(configAggregation);				
			}



			/**
			 * multi-gets vs gets
			 * 
			 * Middleware kann nicht unterscheiden ob get oder multiget
			 * 
			 */
			if(consolidateStatisticsshardedCase51) {
				//consolidateStatisticsshardedCase51();
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating shardedCase51/ReadOnly plots **************************************\n");
				String configAggregation=experimentsBaseFolder+"configAggregationshardedCase51";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT shardedCase51/ReadOnly plots **************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationshardedCase51Client";
				createPlots(configAggregation);
			}
			if(consolidateStatisticsnonshardedCase52) {
				//consolidateStatisticsnonshardedCase52();
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating nonshardedCase52/ReadOnly plots **************************************\n");
				String configAggregation=experimentsBaseFolder+"configAggregationnonshardedCase52";
				createPlots(configAggregation);

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating CLIENT nonshardedCase52/ReadOnly plots **************************************\n");
				configAggregation=experimentsBaseFolder+"configAggregationnonshardedCase52Client";
				createPlots(configAggregation);
			}
			if(consolidateStatisticsMultiGetsPercentiles) {
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating special plots Multi-Gets sharded and non-sharded **************************************\n");
				//configAggregationshardedCase51PercentilesClient
				String configAggregation=experimentsBaseFolder+"configAggregationshardedCase51Percentiles";
				createPlots(configAggregation);                        
				configAggregation=experimentsBaseFolder+"configAggregationnonshardedCase52Percentiles";
				createPlots(configAggregation);
			}
			if(createHistogramsMultiGets) {

				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** creating Histograms Multi-Gets sharded and non-sharded **************************************\n");
				String configAggregation=experimentsBaseFolder+"configAggregationshardedCase51Histogram";		
				createPlots(configAggregation); 


				configAggregation=experimentsBaseFolder+"configAggregationNonshardedCase52Histogram";		
				createPlots(configAggregation); 

				configAggregation=experimentsBaseFolder+"configAggregationshardedCase51HistogramClient";		
				createPlots(configAggregation); 

				configAggregation=experimentsBaseFolder+"configAggregationNonshardedCase52HistogramClient";		
				createPlots(configAggregation);
			}






			/**
			 * create special plots
			 */
			if (createConsolitatedPlots) {
				System.out.println("\n*****************************************************************************************************************");
				System.out.println("***************************************** consolidate PLOTS **************************************\n");
				String configAggregation=experimentsBaseFolder+"configAggregationAllLines";
				createSpecialPlots(configAggregation,"special Plot: allLines ");


			}


			return;
		}



	}

}
