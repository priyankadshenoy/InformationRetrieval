package com.assignment1;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by ps on 5/17/17.
 */
public class Driver {

    private static File folder_path = new File("ap89_collection");
    static String query_path = "query_desc.51-100.short.txt";

    public static File[] listOfFiles = folder_path.listFiles();
    static ElasticSearchConnection elasticSearchConnection = new ElasticSearchConnection();
    private static ScoreCalculator scoreCalculator = new ScoreCalculator(elasticSearchConnection);

    static int TOTAL_DOCUMENTS = 84678;

    public static void main (String[] args) throws IOException {

        elasticSearchConnection.connect();

//        Indexing index = new Indexing();
//        index.indexFile(listOfFiles);
//        System.out.println("Index Complete");

        QueryProcessing qp = new QueryProcessing();
        List<String> queryList = qp.parseQueries();
        System.out.println("Query processing complete");

//        elasticSearchConnection.printIds();
//        System.out.println("Print ID's complete");
//
        scoreCalculator.loadProperties();

        for (String aQueryList : queryList) {
            String[] queryTerms = qp.queryListWithoutStopwords(aQueryList);
            scoreCalculator.fetchDocuments(queryTerms);
        }
        System.out.println("Document fetch complete");

        //Okapi TF and IDF
//        for (String aQueryList : queryList) {
//            String[] queryTerms = qp.queryListWithoutStopwords(aQueryList);
//            scoreCalculator.generateOkapiScore(queryTerms);
//        }
//        System.out.println("Okapi TF & TFIDF complete");


        //BM25
//        for (String aQueryList : queryList) {
//            String[] queryTerms = qp.queryListWithoutStopwords(aQueryList);
//            scoreCalculator.generateBM25Score(queryTerms);
//		}
//        System.out.println("BM25 complete");


//       Unigram Laplace
        for (String aQueryList : queryList) {
            String[] queryTerms = qp.queryListWithoutStopwords(aQueryList);
            scoreCalculator.generateLaplaceScore(queryTerms);
		}
        System.out.println("Laplace complete");

        // Jelinek Mercer
//        for (String aQueryList : queryList) {
//            String[] queryTerms = qp.queryListWithoutStopwords(aQueryList);
//            scoreCalculator.generateJelinekScore(queryTerms);
//		}
//        System.out.println("Jelinek complete");


    }
}
