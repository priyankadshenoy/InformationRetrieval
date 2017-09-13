package com.ir.assignment4;

import java.io.*;
import java.util.*;

/**
 * Created by ps on 7/3/17.
 */
public class PageRankCalculation {
    private Map<String, Set<String>> inlinkMap = new HashMap<>();
    private Map<String, Set<String>> outlinkMapTemp = new HashMap<>();
    private Map<String, Double> pageRankScoreMap = new HashMap<>();
    private Map<String, Double> outlinkMap = new HashMap<>();
    private Set<String> danglingNodesSet = new HashSet<>();


    public void loadFile(String file) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));

            String str;
            while ((str = br.readLine()) != null) {
                String[] arr = str.split(" ");
                if(arr.length > 1){
                   for(int i=1; i<arr.length; i++){
                       //  update inlinks
                       if(inlinkMap.containsKey(arr[0])) {
                           inlinkMap.get(arr[0]).add(arr[i]);
                       }
                       else {
                           Set<String> set = new HashSet<String>();
                           set.add(arr[i]);
                           inlinkMap.put(arr[0], set);
                       }

                       // update outlinks
                       if(outlinkMapTemp.containsKey(arr[i])){
                           outlinkMapTemp.get(arr[i]).add(arr[0]);
                       }else{
                           Set<String> set = new HashSet<String>();
                           set.add(arr[0]);
                           outlinkMapTemp.put(arr[i], set);
                       }
                   }
                }
                else {
                    // create new inlink entry
                    if(!(inlinkMap.containsKey(arr[0]))){
                        inlinkMap.put(arr[0], new HashSet<String>());
                    }
                }
                pageRankScoreMap.put(arr[0], null);

            }
            System.out.print("Inlink map\t" + inlinkMap.size() +"\n"+
            "Outlink map\t" + outlinkMapTemp.size() + "\n"+
            "PageRank score map\t" + pageRankScoreMap.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void createPageRankScore(String fName) {
        long sizeOfInput = pageRankScoreMap.size();

        // initial page rank value 1/N
        for(Map.Entry<String, Double> t: pageRankScoreMap.entrySet()){
            t.setValue(1.0/sizeOfInput);
        }

        for(Map.Entry<String, Set<String>> o : outlinkMapTemp.entrySet()){
            outlinkMap.put(o.getKey(), (double) o.getValue().size());
        }

        // inlink - outlink = dangling
        danglingNodesSet.addAll(inlinkMap.keySet());
        danglingNodesSet.removeAll(outlinkMap.keySet());
        System.out.print("Dangling" + danglingNodesSet.size());

        computePageRank(pageRankScoreMap, inlinkMap, outlinkMap, danglingNodesSet, fName);
    }

    private void computePageRank(Map<String, Double> pageRankScoreMap,
                                 Map<String, Set<String>> inlinkMap,
                                 Map<String, Double> outlinkMap,
                                 Set<String> danglingNodesSet,
                                 String fileName) {

        int total = pageRankScoreMap.size();
        double d = 0.85;
        int maxIteration = 5;
        double oldConvergenceVal = computeConvergence(pageRankScoreMap);
        int count = 0;


        while(count <= maxIteration){
            double dangling = 0.0;
            for (String dang : danglingNodesSet) {
                dangling += pageRankScoreMap.get(dang);
            }

            Map<String,Double> tempScore = new HashMap<String, Double>();
            Set<String> keys = pageRankScoreMap.keySet();
            for (String docId : keys) {
                double newScore = (1.0 - d)/(double)total;
                newScore += d * dangling / (double)total;

                Set<String> inlinks  = inlinkMap.get(docId);

                if(inlinks!=null && !inlinks.isEmpty()){
                    for(String inlinkId : inlinks){
                        if(outlinkMap.containsKey(inlinkId) && pageRankScoreMap.containsKey(inlinkId))
                            newScore += d * pageRankScoreMap.get(inlinkId) / (double) outlinkMap.get(inlinkId);
                    }
                }
                tempScore.put(docId, newScore);
            }

            for (Map.Entry<String, Double> entry : pageRankScoreMap.entrySet()) {
                entry.setValue(tempScore.get(entry.getKey()));
            }

            double newConvergenceScore = computeConvergence(pageRankScoreMap);
            if(scoreConverge(oldConvergenceVal,newConvergenceScore)){
                ++count;
            }
            oldConvergenceVal = newConvergenceScore;
        }

        Map<String, Double> sortedDoc =  sortScores(pageRankScoreMap);
        storeScoresToFile(sortedDoc,fileName);

    }

    static Map<String,Double> sortScores(Map<String, Double> pageRankScoreMap) {
        List<Map.Entry<String, Double>> list =
                new LinkedList<>(pageRankScoreMap.entrySet());
        list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    static void storeScoresToFile(Map<String, Double> sortedDocs, String file) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            Iterator<Map.Entry<String, Double>> iter = sortedDocs.entrySet().iterator();
            int count = 0;
            while (iter.hasNext()) {
                if (count++ == 500) break;
                Map.Entry<String, Double> entry = iter.next();
                bw.write(entry.getKey().trim()+ "\t" + entry.getValue() + System.lineSeparator());
            }
            bw.flush();
            bw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean scoreConverge(double oldConvergenceVal, double newConvergenceScore) {
        return ((int)Math.floor(oldConvergenceVal) % 10 ==
                (int)Math.floor(newConvergenceScore) % 10) &&
                ((int)oldConvergenceVal == (int)newConvergenceScore) ;
    }

    private double computeConvergence(Map<String, Double> pageRankScoreMap) {
        double perplexityScore = 0.0;
        for (Double value : pageRankScoreMap.values()) {
            perplexityScore += value * Math.log(value)/Math.log(2);
        }
        return Math.pow(2, -1 * perplexityScore);
    }

}
