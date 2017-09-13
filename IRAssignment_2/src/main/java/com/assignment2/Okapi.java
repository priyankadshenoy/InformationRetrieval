package com.assignment2;

/**
 * Created by ps on 19/05/17.
 */
public class Okapi {

    public static double okapiScore(long temFreq, long docLen){
        return Double.valueOf(temFreq)/(temFreq + 0.5 + Double.valueOf( 1.5 * (docLen / 250.0)));
    }
}
