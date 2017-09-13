package com.ir.assignment5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ps on 7/17/17.
 */
public class Score {

    public static double fMeasureScore(final List<Doc> listOfDoc, final int cutoff){
        if(listOfDoc.size()<cutoff){
            return 0.0;
        }
        Doc tempDoc = listOfDoc.get(cutoff-1);
        if(tempDoc.getPrecision()!=0.0 && tempDoc.getRecall()!=0.0)
            return 2.0 * tempDoc.getPrecision() * tempDoc.getRecall()/(tempDoc.getPrecision()+tempDoc.getRecall());
        return 0.0;

    }


    public static final double nDCGScore(final List<Doc> docs,  int cutoff, String queryNo){
        if(docs.size()<cutoff){
            return 0.0;
        }
        List<Doc> sortedDocs  = new ArrayList<Doc>();
        List<Doc> unsortedCutoffDocs = new ArrayList<Doc>();
        for(int i =0 ; i < cutoff; i++){
            unsortedCutoffDocs.add(docs.get(i));
        }

        for(int i = 0; i < docs.size();i++){
            sortedDocs.add(docs.get(i));
        }

        double unsorted_dcgScore =  dcgScore(unsortedCutoffDocs, queryNo);
        if(unsorted_dcgScore==0.0)
            return 0.0;
        Collections.sort(sortedDocs, new Comparator<Doc>() {


            public int compare(Doc o1, Doc o2) {
                return o2.getRelevance()-o1.getRelevance();
            }
        });

        double sorted_dcgScore = dcgScore(sortedDocs,queryNo);
        return unsorted_dcgScore/sorted_dcgScore;
    }


    private static final double dcgScore(final List<Doc> docs, String queryNo){
        double dcgScore = 0.0;

        if(docs.isEmpty()){
            return dcgScore;
        }
        dcgScore = docs.get(0).getRelevance();
        for(int i = 1; i < docs.size();i++){
            double temp = Math.log(i+1)/Math.log(2.0);
            dcgScore = dcgScore + (docs.get(i).getRelevance()/temp);
        }
        return dcgScore;
    }
}
