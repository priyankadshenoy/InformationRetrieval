package com.ir.assignment7;

import com.sun.xml.internal.bind.v2.runtime.output.SAXOutput;

import java.io.IOException;

/**
 * Created by ps on 8/4/17.
 */
public class Driver {
    public static void main(String []args) throws IOException {
        ESearchClient eSearchClient = new ESearchClient();
        eSearchClient.configure();
//        System.out.println("ES Configure complete");
//        eSearchClient.loadData();
//        System.out.println("Load data complete");
//        eSearchClient.transformData();
//        System.out.println("Transform and indexing complete");
//        eSearchClient.loadMapData();
//        System.out.println("Load map data");
//        eSearchClient.createMatrix();
//        System.out.println("Creating sparse matrix complete");

        SpamMatrix spamMatrix = new SpamMatrix(eSearchClient);
        spamMatrix.loadData();
        spamMatrix.createMatrix();
        //spamMatrix.changeFeaureMatrix();


    }
}
