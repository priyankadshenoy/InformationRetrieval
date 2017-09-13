package com.assignment2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.*;

/**
 * Created by ps on 5/17/17.
 */
public class Driver {

    public static File folder_path = new File("ap89_collection");
    static String query_path = "query_desc.51-100.short.txt"; /*"query_test.txt";*/

    public static File[] listOfFiles = folder_path.listFiles();
    //static ElasticSearchConnection elasticSearchConnection = new ElasticSearchConnection();
    private static ScoreCalculator scoreCalculator = new ScoreCalculator(null);

    //public static Map<String, Long> stringToLongMap = new HashMap<>();

    static Properties longToString = new Properties();


    static int TOTAL_DOCUMENTS = 84678;

    public static void main (String[] args) throws Exception {


//        ManualIndexing manualIndexing = new ManualIndexing();
//        manualIndexing.indexFile();
//        System.out.println("Manual Indexing Complete");
//
//        for(Map.Entry<String , Long>temp : stringToLongMap.entrySet()){
//            longToString.properties.setProperty(String.valueOf(temp.getValue()), temp.getKey());
//        }
//        longToString.properties.store(new FileOutputStream("src/main/resources/longToString.properties.properties"),
//                "long to string");

//        for (Map.Entry<String, Long> entry : stringToLongMap.entrySet()) {
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        }
//
        QueryProcessing qp = new QueryProcessing();
        List<String> queryList = qp.parseQueries();
        System.out.println("Query processing complete");

//        elasticSearchConnection.printIds();
//        System.out.println("Print ID's complete");

        scoreCalculator.loadProperties();
        System.out.println("Load properties complete");

//        for (String aQueryList : queryList) {
//            scoreCalculator.generateOutputFile(aQueryList);
//        }
//        scoreCalculator.displayOutput();

//        Okapi TF and IDF
//        for (String aQueryList : queryList) {
//            String[] queryTerms = qp.queryListWithoutStopwords(aQueryList);
//            String queryCopy[] = new String[queryTerms.length-1];
//            System.arraycopy(queryTerms, 1, queryCopy, 0, queryTerms.length-1);
//            scoreCalculator.generateOkapiScore(queryCopy);
//        }
//        System.out.println("Okapi TF");


        //BM25
//        for (String aQueryList : queryList) {
//            String[] queryTerms = qp.queryListWithoutStopwords(aQueryList);
//            String queryCopy[] = new String[queryTerms.length-1];
//            System.arraycopy(queryTerms, 1, queryCopy, 0, queryTerms.length-1);
//            scoreCalculator.generateBM25Score(queryCopy);
//            scoreCalculator.generateBM25Score(queryTerms);
//        }
//        System.out.println("BM25 complete");


//       Unigram Laplace
//        for (String aQueryList : queryList) {
//            String[] queryTerms = qp.queryListWithoutStopwords(aQueryList);
//            scoreCalculator.generateLaplaceScore(queryTerms);
//		}
//        System.out.println("Laplace complete");

        //Jelinek Mercer
        for (String aQueryList : queryList) {
            String[] queryTerms = qp.queryListWithoutStopwords(aQueryList);
//            String queryCopy[] = new String[queryTerms.length-1];
//            System.arraycopy(queryTerms, 1, queryCopy, 0, queryTerms.length-1);
            scoreCalculator.generateJelinekScore(queryTerms);
		}
        System.out.println("Jelinek complete");
//

        // Proximity Search
//        final String proximity_query_path = "query_proximity_search.txt";
//        File queryFile = new File(proximity_query_path);
//        BufferedReader reader = new BufferedReader(new FileReader(queryFile));
//        String readLine;
//        List<String> queries = new ArrayList<String>();
//        while((readLine = reader.readLine())!= null){
//            if(readLine.trim().length()>0)
//                queries.add(readLine.trim());
//        }
//        ListIterator<String> queryIter = queries.listIterator();
//        QueryProcessing qptest = new QueryProcessing();
//        while(queryIter.hasNext()){
//            StringBuilder query = new StringBuilder(queryIter.next());
//            String[] queryTerms = qptest.queryListWithoutStopwords(query.toString().toLowerCase());
//            ProximitySearchDriver pEngine = new ProximitySearchDriver();
//            pEngine.configure();
//            pEngine.generateProximityScores(queryTerms);
//            pEngine.close();
//        }
  }
}
