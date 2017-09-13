package com.ir.assignment4;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ps on 7/3/17.
 */
public class HubsAuthority {
    Set<String> crawledSet;
    Map<String, Double> hubVal;
    Map<String, Double> authVal;
    Map<String, Set<String>> inlinkMap;
    Map<String, Set<String>> outlinkMap;
    Map<String, Double> tempHubVal;
    Map<String, Double> tempAuthVal;

    public HubsAuthority(Set<String> crawledSet, Map<String, Double> hubVal,
                         Map<String, Double> authVal, Map<String, Set<String>> inlinkMap,
                         Map<String, Set<String>> outlinkMap) {
        this.crawledSet = crawledSet;
        this.hubVal = hubVal;
        this.authVal = authVal;
        this.inlinkMap = inlinkMap;
        this.outlinkMap = outlinkMap;
        this.tempHubVal = new HashMap<String, Double>(hubVal.size());
        this.tempAuthVal = new HashMap<String, Double>(authVal.size());
    }

    boolean algoComplete = false;

    public void rank() {
        while (!algoComplete) {
            tempAuthVal.clear();
            tempHubVal.clear();

            for (String s : crawledSet) {
                double authScore = getHubScore(inlinkMap.get(s));
                double temp = getAuthScore(outlinkMap.get(s));
                tempHubVal.put(s, temp);
                tempAuthVal.put(s, authScore);
            }

            normalizeScore(tempAuthVal);
            normalizeScore(tempHubVal);

            algoComplete= convergenceCheck(tempHubVal, tempAuthVal);

            hubVal.clear();
            hubVal.putAll(tempHubVal);

            authVal.clear();
            authVal.putAll(tempAuthVal);
        }

        Map<String, Double> sortedHubs = PageRankCalculation.sortScores(hubVal);
        Map<String, Double> sortedAuths = PageRankCalculation.sortScores(authVal);

        PageRankCalculation.storeScoresToFile(sortedHubs, "result/hubVal.txt");
        PageRankCalculation.storeScoresToFile(sortedAuths, "result/authVal.txt");

    }

    private boolean convergenceCheck(Map<String, Double> tempHubVal, Map<String, Double> tempAuthVal) {
        final double EPSILON = 0.00001;

        for (String link : authVal.keySet()) {
            if(Math.abs(hubVal.get(link)-tempHubVal.get(link))>EPSILON){
                return false;
            }
            if(Math.abs(authVal.get(link)-tempAuthVal.get(link))>EPSILON){
                return false;
            }
        }
        return true;
    }

    private void normalizeScore(Map<String, Double> tempAuthVal) {
        double temp = 0.0;
        for (Double score : tempAuthVal.values()) {
            temp += score;
        }
        for (Map.Entry<String, Double> entry : tempAuthVal.entrySet()) {
            tempAuthVal.put(entry.getKey(), entry.getValue() / temp);
        }
    }

    private double getAuthScore(Set<String> outlink) {
        if(outlink == null || outlink.isEmpty())
            return 0.0;
        double tempAuthScore = 0.0;
        for (String o : outlink) {
            if(authVal.containsKey(o)){
                tempAuthScore+= authVal.get(o);
            }
        }
        return tempAuthScore;
    }

    private double getHubScore(Set<String> inlink) {
        if (inlink == null || inlink.isEmpty())
            return 0.0;
        double temphubScore = 0.0;
        for (String i : inlink) {
            if (hubVal.containsKey(i)) {
                temphubScore += hubVal.get(i);
            }
        }
        return temphubScore;
    }
}
