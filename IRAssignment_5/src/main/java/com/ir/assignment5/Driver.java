package com.ir.assignment5;

/**
 * Created by ps on 7/16/17.
 */
public class Driver {
    public static void main(String args[]){
        TrecEvalDiff trec = new TrecEvalDiff();
        trec.loadData("qrels.adhoc.51-100.AP89_test.txt", "results_bm25.txt");
        trec.generatePrecRecall();
        trec.generateMeasureScores();

//        TrecEval trecBool = new TrecEval();
//        trecBool.createBooleanFile("mergedQrel.txt");
        //trecBool.createMergeAvgFile("mergedQrel.txt");

//        TrecEval trec1 = new TrecEval();
//        trec1.loadData("qrelFileBoolean.txt", "temp_1.txt");
//        trec1.generatePrecRecall();
//        trec1.generateMeasureScores();
    }
}
