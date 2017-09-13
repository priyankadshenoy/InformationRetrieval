package assignment6;

import java.io.IOException;

/**
 * Created by ps on 7/27/17.
 */
public class Driver {
    public static void main(String [] args){
        LRegression lRegression = new LRegression();
        try {
            lRegression.loadData();
            lRegression.createMatrix();
            lRegression.testingTest();
            lRegression.trainingTest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
