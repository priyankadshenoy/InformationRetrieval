package com.assignment1;

/**
 * Created by ps on 20/05/17.
 */
public class BM25 {

    static double okapiBM25Score(int totalDocuments, long documentFrequency, long termFrequency, Long docLength) {
        double k1 = 1.2, b = 0.75;
        double avg_len_doc = 250;
        double term1 = Math.log10(Double.valueOf((totalDocuments+0.5))/(documentFrequency+0.5));
        double term2 = Double.valueOf(termFrequency + k1 * termFrequency)
                /(termFrequency+k1*((1-b)+b*Double.valueOf(docLength)/avg_len_doc));
        return term1*term2;

    }

}
