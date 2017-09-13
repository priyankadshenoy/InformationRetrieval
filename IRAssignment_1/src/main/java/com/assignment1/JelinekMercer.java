package com.assignment1;

/**
 * Created by ps on 26/05/17.
 */
public class JelinekMercer {

    static final long TOTAL_NUMBER_OF_TERMS = 21196640;
    static final double LAMBDA = 0.5;

    public static double score(Document doc, Long docLength, long total_term_count) {

        if(docLength==0 || total_term_count==0){
            return 0;
        }
        long lengthOfRemainingDocs = TOTAL_NUMBER_OF_TERMS - docLength;


        double term1 = LAMBDA * Double.valueOf(doc.getTermFrequency()) / docLength;
        double term2 = (1-LAMBDA) * (total_term_count - doc.getTermFrequency())/
                Double.valueOf(lengthOfRemainingDocs);
        double sol = term1 + term2;

        if(sol == 0.0){
            System.out.println("test");
            System.exit(1);
        }

        return Math.log10(term1+term2);
    }
}
