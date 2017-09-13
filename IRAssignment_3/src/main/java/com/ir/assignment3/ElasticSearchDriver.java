package com.ir.assignment3;

import java.io.File;
import java.io.IOException;

/**
 * Created by ps on 6/21/17.
 */
public class ElasticSearchDriver {
    private static File folder_path = new File("result_100");
    public static File[] listOfFiles = folder_path.listFiles();


    public static void main(String[] args) throws IOException {
    ElasticSearchConnect es = new ElasticSearchConnect();
    es.configure();
    es.indexDocument(listOfFiles);
    }
}
