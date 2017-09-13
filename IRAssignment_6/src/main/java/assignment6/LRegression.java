package assignment6;

import java.io.*;
import java.util.*;

/**
 * Created by ps on 7/27/17.
 */
public class LRegression {

    private static double bm25coEfficient, jelinekCoefficient, laplaceCoefficient, okapiCoefficient, tfidfCoefficient;

    Map<String, String> test1 = new HashMap<String, String>();
    Map<String, String> test2 = new HashMap<String, String>();
    Map<String, Integer> countQuery = new HashMap<>();
    static {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("output/results_matrix.model"));
            int count = 1;
            while (count <= 6) {
                ++count;
                reader.readLine();
            }
            bm25coEfficient = Double.valueOf(reader.readLine().trim());
            okapiCoefficient = Double.valueOf(reader.readLine().trim());
            tfidfCoefficient= Double.valueOf(reader.readLine().trim());
            laplaceCoefficient = Double.valueOf(reader.readLine().trim());
            jelinekCoefficient = Double.valueOf(reader.readLine().trim());

            System.out.println("BM25-CO-EFFICIENT: " + bm25coEfficient);
            System.out.println("JELINEK-CO-EFFICIENT: " + jelinekCoefficient);
            System.out.println("LAPLACE-CO-EFFICIENT: " + laplaceCoefficient);
            System.out.println("OKAPI-CO-EFFICIENT: " + okapiCoefficient);
            System.out.println("TFIDF-CO-EFFICIENT: " + tfidfCoefficient);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private final Set<String> trainingQuerySet = new HashSet<String>();
    private final Set<String> testingQuerySet = new HashSet<String>();

    // Map score for models
    private final Map<String, Map<String, Double>> bm25ScoreMap = new LinkedHashMap<String, Map<String, Double>>();
    private final Map<String, Map<String, Double>> tfidfScoreMap = new LinkedHashMap<String, Map<String, Double>>();
    private final Map<String, Map<String, Double>> okapiScoreMap = new LinkedHashMap<String, Map<String, Double>>();
    private final Map<String, Map<String, Double>> jelinekScoreMap = new LinkedHashMap<String, Map<String, Double>>();
    private final Map<String, Map<String, Double>> laplaceScoreMap = new LinkedHashMap<String, Map<String, Double>>();

    // Map min score for models
    private final Map<String, Double> bm25MinScoreMap = new HashMap<String, Double>();
    private final Map<String, Double> tfidfMinScoreMap = new HashMap<String, Double>();
    private final Map<String, Double> okapiMinScoreMap = new HashMap<String, Double>();
    private final Map<String, Double> jelinekMinScoreMap = new HashMap<String, Double>();
    private final Map<String, Double> laplaceMinScoreMap = new HashMap<String, Double>();

    public void loadData() throws IOException {
       loadTrainingAndTestingQueries();
       loadScoreData(bm25ScoreMap, "scores_1/results_bm25", bm25MinScoreMap);
       loadScoreData(okapiScoreMap, "scores_1/results_okapi", okapiMinScoreMap);
       loadScoreData(tfidfScoreMap, "scores_1/results_tfidf", tfidfMinScoreMap);
       loadScoreData(jelinekScoreMap, "scores_1/results_jelinek", jelinekMinScoreMap);
       loadScoreData(laplaceScoreMap, "scores_1/results_laplace", laplaceMinScoreMap);
    }

    private void loadScoreData(Map<String, Map<String, Double>> scoreData,
                               String fileName, Map<String, Double> minScoreData) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = br.readLine()) != null) {
            String[] stringData = line.split(" ");
            if (line.trim().length() == 0) continue;
            if (scoreData.containsKey(stringData[0].trim())) {
                scoreData.get(stringData[0].trim()).put(stringData[2].trim(), Double.valueOf(stringData[4].trim()));
            } else {
                Map<String, Double> tempMap = new LinkedHashMap<String, Double>();
                tempMap.put(stringData[2].trim(), Double.valueOf(stringData[4].trim()));
                scoreData.put(stringData[0].trim(), tempMap);
            }
            if (minScoreData.containsKey(stringData[0].trim())) {
                double value = minScoreData.get(stringData[0].trim());
                if (Double.valueOf(stringData[4].trim()) < value) {
                    minScoreData.put(stringData[0].trim(), Double.valueOf(stringData[4].trim()));
                }
            } else {
                minScoreData.put(stringData[0].trim(), Double.valueOf(stringData[4].trim()));
            }


            test2.computeIfAbsent(stringData[0], k -> stringData[2]);
            {
                String temp = test2.get(stringData[0]);
                if(!(temp.contains(stringData[2])))
                    test2.put(stringData[0], temp.concat("*" + stringData[2].trim()));
            }


        }
        br.close();


        //System.out.println("score" + scoreData.size()+"\t"+ minScoreData.size());
    }

    private void loadTrainingAndTestingQueries() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader("testingTrainingQueries"));
        for (String query : properties.get("testingQueries").toString().split(",")) {
            testingQuerySet.add(query.trim());
        }
        for (String query : properties.get("trainingQueries").toString().split(",")) {
            trainingQuerySet.add(query.trim());
        }
        //System.out.println(trainingQuerySet.size() +"\t"+ testingQuerySet.size());
    }

    public void createTest1() throws IOException {
        //BufferedWriter bw = new BufferedWriter(new FileWriter("output/results_matrix_new_3"));
        BufferedReader br = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));


        String line;
        while ((line = br.readLine()) != null) {
            String[] qrelDetails = line.split(" ");
            if (trainingQuerySet.contains(qrelDetails[0].trim())) {
                test1.computeIfAbsent(qrelDetails[0], k -> qrelDetails[2]);
                {
                    String temp = test1.get(qrelDetails[0]);
                    if(!(temp.contains(qrelDetails[2])))
                        test1.put(qrelDetails[0], temp.concat("*" + qrelDetails[2].trim()));
                }
            }
        }

        System.out.println(test1.size());
        br.close();
//        bw.flush();
//        bw.close();
    }

    public void createMatrix() throws IOException {
        createTest1();
        BufferedWriter bw = new BufferedWriter(new FileWriter("output/results_matrix_new_5"));
        BufferedReader br = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));

        Map<String, String> tempBool = new HashMap();


        String line;
        while ((line = br.readLine()) != null) {
            String[] qrelDetails = line.split(" ");
            if (trainingQuerySet.contains(qrelDetails[0].trim())) {
                dumpResults(qrelDetails[0], qrelDetails[2], qrelDetails[3], bw);
            }
        }
        extraData("0",bw);

        for(Map.Entry<String, Integer> k: countQuery.entrySet()){
            System.out.println(k.getKey()+"\t"+k.getValue());
        }

        br.close();
        bw.flush();
        bw.close();
    }

    private void dumpResults(String qrelDetail, String docId, String rel, BufferedWriter bufferedWriter) throws IOException {
        //Double score = calculateScore(bm25ScoreMap, qrelDetail, docId, bm25MinScoreMap);
        Double score = calculateScore(jelinekScoreMap, qrelDetail, docId, jelinekMinScoreMap);
       // Double score = calculateScore(laplaceScoreMap, qrelDetail, docId, laplaceMinScoreMap);
       // Double score = calculateScore(okapiScoreMap, qrelDetail, docId, okapiMinScoreMap);
     //    Double score = calculateScore(tfidfScoreMap, qrelDetail, docId, tfidfMinScoreMap);

//        for(String t : test1.keySet()){
//            List<String> val1= Arrays.asList(test1.get(t).split(" "));
//            List<String> val2=Arrays.asList(test2.get(t).split(" "));
//            List<String> finalList = new ArrayList<>();
//            for(String s: val2){
//                if(!(val1.contains(s)))
//                    //finalList.add(s);
//                    calculateScore(bm25ScoreMap, t, s, bm25MinScoreMap);
//            }



        buffWriterWrite(rel, score, bufferedWriter, qrelDetail);

    }

    private void extraData(String rel, BufferedWriter bufferedWriter) throws IOException {

        for (String z : test1.keySet()) {
            List<String> temp1 = Arrays.asList(test1.get(z).split("\\*"));
            List<String> temp2 = Arrays.asList(test2.get(z).split("\\*"));
            Set<String> t = new HashSet<>();

            for (String s : temp2) {
                   if(!(temp1.contains(s))){
                        double bm25Score = calculateScore(jelinekScoreMap, z, s, jelinekMinScoreMap);
                        buffWriterWrite(rel, bm25Score, bufferedWriter, z);
                    }
                }
            }
    }


    private void buffWriterWrite(String rel, Double score, BufferedWriter bufferedWriter, String qrelDetail) throws IOException {
        if(countQuery.get(qrelDetail)== null)
        countQuery.put(qrelDetail, 1);
        else if(countQuery.get(qrelDetail)<=1000)
        {

            bufferedWriter.write(rel+ /* " 1:" + score +*/
   //                 " 2:" + score +
    //           " 3:" + score +
     //         " 4:" + score +
                " 5:" + score +
                            System.lineSeparator());

            int ct = countQuery.get(qrelDetail);
            countQuery.put(qrelDetail, ++ct);
        }
    }

    private Double calculateScore(Map<String, Map<String, Double>> scoreMap, String qrelDetail, String docId, Map<String, Double> minScoreMap) {
        double score = 0.0;
        try {
            score = scoreMap.get(qrelDetail).get(docId);
        } catch (Exception e) {
            score = minScoreMap.get(qrelDetail);
        }
        return score;
    }

    public void trainingTest() throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("output/trainingResult.txt"));
        BufferedReader bufferedReader = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));
        int c=0;
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] qrelDetails = line.split(" ");
            if (trainingQuerySet.contains(qrelDetails[0].trim())) {
                dumpResultsTestData(qrelDetails[0], qrelDetails[2], bufferedWriter);
                ++c;
            }
        }

        System.out.println(c);
        bufferedReader.close();
        bufferedWriter.flush();
        bufferedWriter.close();

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("output/trainingResult.txt"));

        for (String queryId : trainingQuerySet) {
            Set<String> docIds = new HashSet<String>();
            docIds.addAll(bm25ScoreMap.get(queryId).keySet());
            docIds.addAll(jelinekScoreMap.get(queryId).keySet());
            docIds.addAll(laplaceScoreMap.get(queryId).keySet());
            docIds.addAll(okapiScoreMap.get(queryId).keySet());
            docIds.addAll(tfidfScoreMap.get(queryId).keySet());

            List<Measure> list = new ArrayList<Measure>();
            for (String docId : docIds) {
                double scoreValue = writeResultForTestingQuery(queryId, docId);
                Measure score = new Measure(docId, scoreValue, queryId);
                list.add(score);
            }
            Collections.sort(list);
            int count = 1;
            for (Measure score : list) {
                bufferedWriter.write(score.getQueryId() + " " + "Q0" + " " + score.getDocId() + " " + count
                        + " " + score.getScore() + " " + "Sample" + System.lineSeparator());
                ++count;
            }

        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    private void dumpResultsTestData(String qrelDetail, String docId, BufferedWriter bw) throws IOException {
        double s = getResultsForTrainingData(qrelDetail, docId);
        bw.write(qrelDetail + " " + "Q0" + " " + docId + " " + 1 + " " +
                s + " " + "Sample" + System.lineSeparator());

    }

    private double getResultsForTrainingData(String qrelDetail, String docId) {
        Double bm25Score = calculateScore(bm25ScoreMap, qrelDetail, docId, bm25MinScoreMap);
        Double jelinekScore = calculateScore(jelinekScoreMap, qrelDetail, docId, jelinekMinScoreMap);
        Double laplaceScore = calculateScore(laplaceScoreMap, qrelDetail, docId, laplaceMinScoreMap);
        Double okapiScore = calculateScore(okapiScoreMap, qrelDetail, docId, okapiMinScoreMap);
        Double tfidfScore = calculateScore(tfidfScoreMap, qrelDetail, docId, tfidfMinScoreMap);
        return getRegrssionScore(bm25Score,jelinekScore,laplaceScore,okapiScore,null,tfidfScore);
    }

    public void testingTest() throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("output/testingResult.txt"));

        for (String queryId : testingQuerySet) {
            Set<String> docIds = new HashSet<String>();
            docIds.addAll(bm25ScoreMap.get(queryId).keySet());
            docIds.addAll(jelinekScoreMap.get(queryId).keySet());
            docIds.addAll(laplaceScoreMap.get(queryId).keySet());
            docIds.addAll(okapiScoreMap.get(queryId).keySet());
            docIds.addAll(tfidfScoreMap.get(queryId).keySet());

            List<Measure> list = new ArrayList<Measure>();
            for (String docId : docIds) {
                double scoreValue = writeResultForTestingQuery(queryId, docId);
                Measure score = new Measure(docId, scoreValue, queryId);
                list.add(score);
            }
            Collections.sort(list);
            int count = 1;
            for (Measure score : list) {
                bufferedWriter.write(score.getQueryId() + " " + "Q0" + " " + score.getDocId() + " " + count
                        + " " + score.getScore() + " " + "Sample" + System.lineSeparator());
                ++count;
            }

        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    private double writeResultForTestingQuery(String queryId, String docId) throws IOException {
        Double bm25Score = calculateScore(bm25ScoreMap, queryId, docId, bm25MinScoreMap);
        Double jelinekScore = calculateScore(jelinekScoreMap, queryId, docId, jelinekMinScoreMap);
        Double laplaceScore = calculateScore(laplaceScoreMap, queryId, docId, laplaceMinScoreMap);
        Double okapiScore = calculateScore(okapiScoreMap, queryId, docId, okapiMinScoreMap);
        Double tfidfScore = calculateScore(tfidfScoreMap, queryId, docId, tfidfMinScoreMap);
        return getRegrssionScore(bm25Score,jelinekScore, laplaceScore, okapiScore, null, tfidfScore);
    }


    private double getRegrssionScore(Double bm25Score, Double jelinekScore, Double laplaceScore,
                                     Double okapiScore, Double proxScore, Double tfidfScore) {
        return bm25coEfficient * bm25Score
                + jelinekCoefficient * jelinekScore
                + laplaceCoefficient * laplaceScore
                + okapiCoefficient * okapiScore
                + tfidfCoefficient * tfidfScore;
    }
}

