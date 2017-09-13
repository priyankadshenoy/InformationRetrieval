package com.ir.assignment5;

import com.sun.xml.internal.ws.api.model.MEP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ps on 7/17/17.
 */
public class Measure {

    private double cutOffFive;
    private double cutOffTen;
    private double cutOffTwenty;
    private double cutOffFifty;
    private double cutOffHundred;
    private double cutOffTwoHundred;
    private double cutOffFiveHundred;
    private double cutOffThousand;

    public static final Map<String, Measure> fMeasureMap = new HashMap<String, Measure>();
    public static final Map<String, Measure> ndcgMeasureMap = new HashMap<String, Measure>();

    public double getCutOffFive() {
        return cutOffFive;
    }

    public void setCutOffFive(double cutOffFive) {
        this.cutOffFive = cutOffFive;
    }

    public double getCutOffTen() {
        return cutOffTen;
    }

    public void setCutOffTen(double cutOffTen) {
        this.cutOffTen = cutOffTen;
    }

    public double getCutOffTwenty() {
        return cutOffTwenty;
    }

    public void setCutOffTwenty(double cutOffTwenty) {
        this.cutOffTwenty = cutOffTwenty;
    }

    public double getCutOffFifty() {
        return cutOffFifty;
    }

    public void setCutOffFifty(double cutOffFifty) {
        this.cutOffFifty = cutOffFifty;
    }

    public double getCutOffHundred() {
        return cutOffHundred;
    }

    public void setCutOffHundred(double cutOffHundred) {
        this.cutOffHundred = cutOffHundred;
    }

    public double getCutOffTwoHundred() {
        return cutOffTwoHundred;
    }

    public void setCutOffTwoHundred(double cutOffTwoHundred) {
        this.cutOffTwoHundred = cutOffTwoHundred;
    }

    public double getCutOffFiveHundred() {
        return cutOffFiveHundred;
    }

    public void setCutOffFiveHundred(double cutOffFiveHundred) {
        this.cutOffFiveHundred = cutOffFiveHundred;
    }

    public double getCutOffThousand() {
        return cutOffThousand;
    }

    public void setCutOffThousand(double cutOffThousand) {
        this.cutOffThousand = cutOffThousand;
    }


    public static Measure calculateFMeasure(String query, List<Doc> docs) {
        Measure fmeasure = new Measure();
        fmeasure.setCutOffFive(Score.fMeasureScore(docs, 5));
        fmeasure.setCutOffTen(Score.fMeasureScore(docs, 10));
        fmeasure.setCutOffTwenty(Score.fMeasureScore(docs, 20));
        fmeasure.setCutOffFifty(Score.fMeasureScore(docs, 50));
        fmeasure.setCutOffHundred(Score.fMeasureScore(docs, 100));
        fmeasure.setCutOffTwoHundred(Score.fMeasureScore(docs, 200));
        fmeasure.setCutOffFiveHundred(Score.fMeasureScore(docs, 500));
        fmeasure.setCutOffThousand(Score.fMeasureScore(docs, 1000));
        fMeasureMap.put(query, fmeasure);
        return fmeasure;
    }

    public static Measure calculateNDCGMeasure(String query, List<Doc> docs, Map<String, List<Doc>> resultMap) {
        Measure ndcgMeasure = new Measure();
        ndcgMeasure.setCutOffFive(Score.nDCGScore(resultMap.get(query), 5,query));
        ndcgMeasure.setCutOffTen(Score.nDCGScore(resultMap.get(query), 10,query));
        ndcgMeasure.setCutOffTwenty(Score.nDCGScore(resultMap.get(query), 20,query));
        ndcgMeasure.setCutOffFifty(Score.nDCGScore(resultMap.get(query), 50,query));
        ndcgMeasure.setCutOffHundred(Score.nDCGScore(resultMap.get(query), 100,query));
        ndcgMeasure.setCutOffTwoHundred(Score.nDCGScore(docs, 200,query));
        ndcgMeasure.setCutOffFiveHundred(Score.nDCGScore(docs, 500,query));
        ndcgMeasure.setCutOffThousand(Score.nDCGScore(docs, 1000,query));
        ndcgMeasureMap.put(query, ndcgMeasure);
        return ndcgMeasure;
    }

    public static double calculateAvgPrecision(final String query, final List<Doc> docs, int total_number_of_relevant, Map<String, Map<String, Integer>> qrelMap){
        if(qrelMap.containsKey(query)){
            double precisionSum = 0.0;
            for(int i = 0 ; i < docs.size(); i++){
                if(docs.get(i).getRelevance() == 1)
                    precisionSum = precisionSum + docs.get(i).getPrecision();
            }
            return (precisionSum/total_number_of_relevant);
        }
        return 0.0;
    }

    public static double calculateRPrecision(final String query, final List<Doc> docs, int total_number_of_relevant, Map<String, Map<String, Integer>> qrelMap){
        if(qrelMap.containsKey(query)){
            return docs.get(total_number_of_relevant-1).getPrecision();
        }
        return 0.0;

    }
}
