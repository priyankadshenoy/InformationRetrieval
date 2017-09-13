package com.assignment2;

/**
 * Created by ps on 26/05/17.
 */
public class JelinekMercer {

    static final long totalTerms = 21196640;
    static final double lambda = 0.5;

    public static double score(Document doc, Long docLength, long total_term_count) {

        if(docLength==0 || total_term_count==0){
            return 0;
        }
        long lengthOfRemainingDocs =  - docLength;
        double term1 = lambda * Double.valueOf(doc.getTermFrequency()) / docLength;
        double term2 = (1-lambda) * (total_term_count - doc.getTermFrequency())/
                Double.valueOf(lengthOfRemainingDocs);
        double sol = term1 + term2;

        if(sol == 0.0){
            System.exit(1);
        }

        return Math.log10(term1+term2);
    }

    public static double score(TokenMap token, Long docLength, Long total_freq_count) {
        if(docLength==0 || total_freq_count==0){
            return 0;
        }
        long lengthOfRemainingocs = totalTerms - docLength;


        double term1 = lambda * Double.valueOf(token.getCount()) / docLength;
        double term2 = (1-lambda) * (total_freq_count-token.getCount())/ Double.valueOf(lengthOfRemainingocs);
        double sol = term1 + term2;
        if(sol == 0.0){
            System.exit(1);
        }
        return Math.log10(term1+term2);
    }
}
