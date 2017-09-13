package com.irassignment8;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by ps on 8/17/17.
 */
public class ClusteringDriver {
    public static void main(String [] args) throws Exception {
        ClusteringModel client = new ClusteringModel();
        client.loadFiles();
        client.generateWordsMapping();
        client.generateTopics();
        client.generateTopicDocDistribution();
        client.generateMatrix();


    }
}
