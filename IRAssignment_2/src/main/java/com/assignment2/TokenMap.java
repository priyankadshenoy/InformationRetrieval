package com.assignment2;

/**
 * Created by ps on 6/7/17.
 */
public class TokenMap implements Comparable<TokenMap> {
    private String docId;
    private int count;
    private String term;
    private String positions;
    private int docFreq;


//    public TokenMap(String docId, int count) {
//        super();
//        this.docId = docId;
//        this.count = count;
//    }

    TokenMap(String docId, int count, String term) {
        super();
        this.docId = docId;
        this.count = count;
        this.term = term;
    }

    TokenMap(String docId, int count, String term, int docFreq) {
        super();
        this.docId = docId;
        this.count = count;
        this.term = term;
        this.docFreq = docFreq;

    }

    TokenMap(String docId, int count, String term, String position) {
        super();
        this.docId = docId;
        this.count = count;
        this.term = term;
        this.positions = position;
    }

    String getDocId() {
        return docId;
    }
    public void setDocId(String docId) {
        this.docId = docId;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

    public String getPositions() {
        return positions;
    }

    public void setPositions(String positions) {
        this.positions = positions;
    }

    public int compareTo(TokenMap o) {
        return o.count-this.count;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }


    int getDocFreq() {
        return docFreq;
    }
}
