//package com.assignment2;
//
//import java.io.*;
//import java.util.*;
//
///**
// * Created by ps on 6/9/17.
// */
//public class ProximitySearchDriver {
//
//    private RandomAccessFile randomAccessFile = null;
//    private static Properties docsLengthMap = new Properties();
//    private static Map<String, Long> termLookUpMap = new HashMap<String, Long>();
//
//    static{
//
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(new File("result/FinalCatalogFile.txt")));
//            String brStr;
//            while ((brStr = br.readLine()) != null) {
//                String[] temp = brStr.split(" ");
//                termLookUpMap.put(temp[0], Long.valueOf(temp[1]));
//            }
//            br.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            docsLengthMap.load(ProximitySearchDriver.class.getClassLoader().getResourceAsStream("docLength.properties"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    void configure(){
//        try {
//            randomAccessFile = new RandomAccessFile(new File("result/FinalInvertedIndex.txt"), "r");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    void generateProximityScores(String[] queryTerms) throws Exception {
//        List<TokenMap> queryDocs = new ArrayList<TokenMap>();
//
//        for(int i = 1 ; i < queryTerms.length;i++){
//            String term = /*ScoreCalculator.getStemOfWord*/(queryTerms[i].trim().toLowerCase());
//            if(termLookUpMap.containsKey(term)) {
//                List<TokenMap> docs = getDocumentsForTerm(term);
//                queryDocs.addAll(docs);
//            }
//        }
//        Map<String,List<String>> temp = new HashMap<String, List<String>>();
//        for (TokenMap TokenMap : queryDocs) {
//            if(temp.containsKey(TokenMap.getDocId())){
//                temp.get(TokenMap.getDocId()).add(TokenMap.getPositions());
//            }else{
//                List<String> tList = new ArrayList<String>();
//                tList.add(TokenMap.getPositions());
//                temp.put(TokenMap.getDocId(), tList);
//            }
//        }
//        Map<String,Double> proximityScoreList= new HashMap<String, Double>();
//
//        for (Map.Entry<String, List<String>> entry : temp.entrySet()) {
//            List<String> termsInDocumentList = entry.getValue();
//            List<List<Integer>> positionList = new ArrayList<List<Integer>>();
//            for (String matchingString : termsInDocumentList) {
//                List<Integer> posList = new ArrayList<Integer>();
//                String[] pos = matchingString.split("-");
//                posList.add(Integer.valueOf(pos[0]));
//                int position = Integer.valueOf(pos[0]);
//                for (int i = 1; i < pos.length; i++) {
//                    position = position + Integer.valueOf(pos[i]);
//                    posList.add(position);
//                }
//                positionList.add(posList);
//            }
//            int minSpan;
//            if (positionList.size() == 1) {
//                minSpan = 1;
//            } else {
//                try {
//                    minSpan = ProximitySearch.scoreForDocument(positionList);
//                } catch (Exception e) {
//                    minSpan = 1;
//                }
//            }
//
//            double score = proximityScore(minSpan, positionList.size(), entry.getKey());
//            proximityScoreList.put(entry.getKey(), score);
//        }
//
//        Map<String,Double> sortedScore = SortValue.sortByComparator(proximityScoreList);
//        try {
//            SortValue.storeResultsToFile(sortedScore,queryTerms[0], "score_result/result_proximity.txt");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static double proximityScore(int minRange, int noOfContainingTerms, String docId){
//        return (1500.0 - minRange) * noOfContainingTerms/(Long.valueOf(String.valueOf(docsLengthMap.get(docId))) + 174584);
//    }
//
//    private List<TokenMap> getDocumentsForTerm(String term) {
//        List<TokenMap> tokens = new ArrayList<TokenMap>();
//        Long termOffset = termLookUpMap.get(term);
//        try {
//            randomAccessFile.seek(termOffset);
//            StringBuilder docsDescAsStr = new StringBuilder(randomAccessFile.readLine());
//            String[] docsTokens = docsDescAsStr.toString().split("=")[1].split("~");
//            for (String docToken : docsTokens) {
//                String[] tempData = docToken.split("#");
//                String[] tempData1 = tempData[1].split("-");
//                tokens.add(new TokenMap(tempData[0], tempData1.length,term,tempData[1]));
//            }
//            return tokens;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public void close(){
//        try {
//            randomAccessFile.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
