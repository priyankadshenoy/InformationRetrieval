package com.assignment2;

/**
 * Created by ps on 20/05/17.
 */
public class TFIDF {

    public static double tfidfScore(Double okapiScore, int totalDocuments, long documentFrequency) {

        if(0 == documentFrequency){
            throw new IllegalStateException();
        }
        return okapiScore * Math.log10(totalDocuments/documentFrequency);
    }
}
