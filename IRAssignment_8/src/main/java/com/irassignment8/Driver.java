package com.irassignment8;

/**
 * Created by ps on 8/13/17.
 */
public class Driver {
    public static void main(String[] args) throws Exception {
        TopicModel topicModel = new TopicModel();
        topicModel.loadFiles();
        topicModel.createWordMapping();
        topicModel.createTopics();

    }
}
