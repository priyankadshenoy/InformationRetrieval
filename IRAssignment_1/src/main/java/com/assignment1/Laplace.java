package com.assignment1;

/**
 * Created by ps on 21/05/17.
 */
public class Laplace {

    static final long vocabulary = 160000;
//    static final long vocabulary = 178081;

    static double lapaceScore(long termFrequency, Long docLength) {

        return Double.valueOf(termFrequency+1.0)/(docLength + vocabulary);
    }

    static double penalizedScore(Long doclength, int frequency) {

        return Double.valueOf(1.0/(doclength + vocabulary)) * frequency;
    }

}
