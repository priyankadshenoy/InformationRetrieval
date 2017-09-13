package com.assignment1;

/**
 * Created by ps on 19/05/17.
 */
public class Okapi {

    static double okapiScore(long termFrequency, Long docLen, String documentId, String s) {

        return Double.valueOf(termFrequency)/(termFrequency + 0.5 + Double.valueOf( 1.5 * (docLen / 250.0)));
    }
}
