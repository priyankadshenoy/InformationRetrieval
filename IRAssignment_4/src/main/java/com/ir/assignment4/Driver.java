package com.ir.assignment4;

/**
 * Created by ps on 6/30/17.
 */
public class Driver {
    public static void main(String[] args){
        // Load merged index for ES index
//        ElasticSearchConnect es = new ElasticSearchConnect();
//        es.configure();
//        es.loadInlinksFromIndex();

        // Page rank for w2g
//        PageRankCalculation pageRankW2g = new PageRankCalculation();
//        pageRankW2g.loadFile("wt2g_inlinks.txt");
//        pageRankW2g.createPageRankScore("result/pagerank_wt2g.txt");



        // Rank for hubs and authorities
        ElasticSearchConnect es = new ElasticSearchConnect();
        es.configure();
        es.rankAllPages();

        // Page rank for merged indexes
//        PageRankCalculation pageRankES = new PageRankCalculation();
//        pageRankES.loadFile("finalInlink.txt");
//        pageRankES.createPageRankScore("result/pagerank_es.txt");


    }
}
