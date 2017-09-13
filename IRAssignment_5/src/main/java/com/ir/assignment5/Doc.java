package com.ir.assignment5;

/**
 * Created by ps on 7/17/17.
 */
public class Doc implements Comparable<Doc>{

    private String docId;
    private double precision;
    private double recall;
    private double score;
    private int relevance;
    private int rank;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getRelevance() {
        return relevance;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int compareTo(Doc o) {
        return Double.compare(o.getScore(), this.getScore());
    }

    @Override
    public String toString() {
        return "Doc [docId=" + docId + ", precision=" + precision + ", recall="
                + recall + ", relevance=" + relevance + ", score=" + score
                + "]";
    }
}
