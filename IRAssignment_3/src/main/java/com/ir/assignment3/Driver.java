package com.ir.assignment3;

/**
 * Created by ps on 6/15/17.
 */
public class Driver {
    public static void main(String [] args){
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Crawler crawler = new Crawler ();
        crawler.crawlWeb();
        crawler.stop();

    }
}
