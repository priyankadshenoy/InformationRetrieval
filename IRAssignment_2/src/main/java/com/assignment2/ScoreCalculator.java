package com.assignment2;

import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by ps on 5/19/17.
 */
public class ScoreCalculator {

    ElasticSearchConnection elasticSearchConnection;
    private static final List<String> docIds = new ArrayList<String>();

    private static Properties docsLengthValue = new Properties();
    private static Properties intToStr = new Properties();
    private static Map<String, Long> termLookUpMap = new HashMap<String, Long>();
    private RandomAccessFile randomAccessFile = null;
    private List<OutputFormat> outputFileFormat = new ArrayList<OutputFormat>();

    ScoreCalculator(ElasticSearchConnection elasticSearchConnection) {
        this.elasticSearchConnection = elasticSearchConnection;
    }

    public static String getStemOfWord(String word) {
        PorterStemmer stemValue = new PorterStemmer();
        stemValue.setCurrent(word);
        stemValue.stem();
        return stemValue.getCurrent();
    }



   void generateOkapiScore(String[] queryTerms) {
        List<TokenMap> queryDocs = new ArrayList<TokenMap>();

        for(int i = 1 ; i < queryTerms.length;i++){
            String term = ScoreCalculator.getStemOfWord(queryTerms[i].trim().toLowerCase());

            if(!termLookUpMap.containsKey(term)){
                continue;
            }
            List<TokenMap> docs = getDocumentsForTerm(term);
            queryDocs.addAll(docs);
        }

        Map<String,Double> okapiScoreDocList= new HashMap<String, Double>();

        for (TokenMap doc : queryDocs) {
            double okapiScore = Okapi.okapiScore((long) doc.getCount(),
                    Long.valueOf(docsLengthValue.get(/*intToStr.get*/(doc.getDocId())).toString()));
            if (okapiScoreDocList.containsKey(doc.getDocId())) {
                double oScore = okapiScore + okapiScoreDocList.get(doc.getDocId());
                okapiScoreDocList.put(/*intToStr.get*/(doc.getDocId()).toString(), oScore);
            } else {
                okapiScoreDocList.put(/*intToStr.get*/(doc.getDocId()).toString(), okapiScore);
            }
        }
        Map<String,Double> sortedScore = SortValue.sortByComparator(okapiScoreDocList);
        try {
            SortValue.storeResultsToFile(sortedScore,queryTerms[0], "score_result/result_stemmed_okapi.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void generateBM25Score(String[] queryTerms) {
        List<TokenMap> queryDocs = new ArrayList<TokenMap>();
        for(int i = 1 ; i < queryTerms.length;i++){
            String term = ScoreCalculator.getStemOfWord(queryTerms[i].trim().toLowerCase());
            if(!termLookUpMap.containsKey(term)){
                continue;
            }
            List<TokenMap> docs = getDocumentsForTerm(term);
            queryDocs.addAll(docs);
        }
        Map<String,Double> bm25ScoreDocList= new HashMap<String, Double>();
        for (TokenMap token : queryDocs) {
            double bm25Score = BM25.okapiBM25Score(84678,
                    Long.valueOf(String.valueOf(token.getDocFreq())),
                    token.getCount(), Long.valueOf(docsLengthValue.get(token.getDocId()).toString()));
            if (bm25ScoreDocList.containsKey(token.getDocId())) {
                double bmScore = bm25Score + bm25ScoreDocList.get(token.getDocId());
                bm25ScoreDocList.put(token.getDocId(), bmScore);
            } else {
                bm25ScoreDocList.put(token.getDocId(), bm25Score);
            }
        }

        Map<String,Double> sortedScore = SortValue.sortByComparator(bm25ScoreDocList);
        try {
            SortValue.storeResultsToFile(sortedScore,queryTerms[0],"score_result/result_stemmed_bm25.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void generateJelinekScore(String[] queryTerms) {
        Map<String, Double> docScores = new HashMap<String, Double>();
        List<TokenMap> tempQueryTermList;
        for (int i = 1; i < queryTerms.length; i++) {
            String term = ScoreCalculator.getStemOfWord(queryTerms[i].trim().toLowerCase());
            if (termLookUpMap.containsKey(term)) {
                tempQueryTermList = new ArrayList<TokenMap>(getDocumentsForTerm(term));
                long termCount = 0;
                for (TokenMap aTempQueryTermList : tempQueryTermList) {
                    termCount = termCount + aTempQueryTermList.getCount();
                }
                if (termCount == 0) {
                    continue;
                }
                final List<String> docIdsOfExistingQueryTerms = new ArrayList<String>();
                for (TokenMap aTempQueryTermList : tempQueryTermList) {
                    docIdsOfExistingQueryTerms.add(aTempQueryTermList.getDocId());
                }
                for (String docId : docIds) {
                    if (!docIdsOfExistingQueryTerms.contains(docId)) {
                        if (Long.valueOf(docsLengthValue.get(docId).toString()) == 0) {
                            continue;
                        }
                        tempQueryTermList.add(new TokenMap(docId, 0, term));
                    }
                }
                for (TokenMap doc : tempQueryTermList) {
                    double jScore = JelinekMercer.score(doc, Long.valueOf
                            (docsLengthValue.get(doc.getDocId()).toString()), termCount);
                    if (docScores.containsKey(doc.getDocId())) {
                        double score = jScore + docScores.get(doc.getDocId());
                        docScores.put(doc.getDocId(), score);
                    } else {
                        docScores.put(doc.getDocId(), jScore);
                    }
                }
            }
        }
        Map<String, Double> sortedScore = SortValue.sortByComparator(docScores);
        try {
            SortValue.storeResultsToFile(sortedScore, queryTerms[0], "score_result/result_stemmed_jelinek.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loading the properties files into data structures
    void loadProperties() {
        try {
            docsLengthValue.load(ScoreCalculator.class.getClassLoader()
                    .getResourceAsStream("documentLength.properties"));

//            intToStr.load(ScoreCalculator.class.getClassLoader()
//            .getResourceAsStream("longToString.properties.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("result/FinalCatalogFile.txt")));
            String brStr = null;
            while ((brStr = br.readLine()) != null) {
                String[] temp = brStr.split(" ");
                termLookUpMap.put(temp[0], Long.valueOf(temp[1]));
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            randomAccessFile = new RandomAccessFile(new File("result/FinalInvertedIndex.txt"), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Properties docProperties = new Properties();
        try {
            docProperties.load(ScoreCalculator.class.getClassLoader()
                    .getResourceAsStream("docIds"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<Object, Object> objectObjectEntry : docProperties.entrySet()) {
            docIds.add(objectObjectEntry.getKey().toString());
        }
    }

    private  List<TokenMap> getDocumentsForTerm(String term) {
        List<TokenMap> tokens = new ArrayList<TokenMap>();
        Long termOffset = termLookUpMap.get(term);
        try {
            randomAccessFile.seek(termOffset);
            StringBuilder docsDescAsStr = new StringBuilder(randomAccessFile.readLine());
            String[] docsTokens = docsDescAsStr.toString().split("=")[1].split("~"); // doc freq
            int len=0;
            for(String doctk : docsTokens){
                String lenval [] = doctk.split("#")[1].split("-");
                len+= lenval.length;
            }
            int val = docsTokens.length;
            int t=0;
            for (String docToken : docsTokens) {
                String[] tempData = docToken.split("#");
                String[] tempData1 = tempData[1].split("-");
                tokens.add(new TokenMap(/*Long.parseLong*/(tempData[0]), tempData1.length, term, docToken.length()));
            }
            return tokens;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void generateOutputFile(String queryTerms) {
            String term = ScoreCalculator.getStemOfWord(queryTerms.trim().toLowerCase());
            if(termLookUpMap.containsKey(term)) {
                outputFileFormat.add(getOutputForTerm(term));
            }
    }

    private OutputFormat getOutputForTerm(String term) {
        List<TokenMap> tokens = new ArrayList<TokenMap>();
        Long termOffset = termLookUpMap.get(term);
        OutputFormat of = null;
        try {
            randomAccessFile.seek(termOffset);
            StringBuilder docsDescAsStr = new StringBuilder(randomAccessFile.readLine());
            of = new OutputFormat();
            String[] docsTokens = docsDescAsStr.toString().split("=")[1].split("~"); // doc freq
            of.setDocFreq(docsTokens.length);
            int len = 0;
            for (String doctk : docsTokens) {
                String lenval[] = doctk.split("#")[1].split("-");
                len += lenval.length;
            }
            of.setTermFreq(len);
            of.setTerm(term);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return of;
    }

    public void displayOutput() {
        List<String> outputFileString = new ArrayList<>();
        for(OutputFormat o : outputFileFormat){
            System.out.println(o.getTerm() + " " + o.getDocFreq() + " " + o.getTermFreq());
            String term = o.getTerm();
            String docFre = String.valueOf(o.getDocFreq());
            String termFre = String.valueOf(o.getTermFreq());
            outputFileString.add(term+" "+docFre+" "+ termFre);
        }
        Path file = Paths.get("actual_stemmed.txt");
        try {
            Files.write(file, outputFileString, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.out.println("Issue with output file generation");
            e.printStackTrace();
        }
    }
}