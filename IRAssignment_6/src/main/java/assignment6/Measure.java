package assignment6;

import org.elasticsearch.search.suggest.term.TermSuggestion;

/**
 * Created by ps on 7/31/17.
 */
public class Measure implements Comparable<Measure> {
    private String queryId;
    private String docId;
    private double score;

    public Measure(String docId, double score, String queryId){
        this.docId = docId;
        this.score = score;
        this.queryId = queryId;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getDocId() {
        return docId;
    }

    public double getScore() {
        return score;
    }


    public int compareTo(Measure o) {
        return Double.compare(o.getScore(), this.getScore());
    }


}
