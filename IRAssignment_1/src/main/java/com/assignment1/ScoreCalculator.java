package com.assignment1;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.util.*;

/**
 * Created by ps on 5/19/17.
 */
public class ScoreCalculator {

    ElasticSearchConnection elasticSearchConnection;

    private final Map<String, List<Document>> queryTermList = new HashMap();
    private static final List<String> docIds = new ArrayList<String>();
    private final String INDEX_NAME = "index_final_val";

    private static Properties docsLengthValue = new Properties();
    private static Properties docProperties = new Properties();

    ScoreCalculator(ElasticSearchConnection elasticSearchConnection) {
        this.elasticSearchConnection = elasticSearchConnection;
    }

    private String getStemOfWord(String word) {
        PorterStemmer stemValue = new PorterStemmer();
        stemValue.setCurrent(word);
        stemValue.stem();
        return stemValue.getCurrent();
    }


    void fetchDocuments(String[] queryTerms) {

        for (String queryTerm : queryTerms) {
            String word = getStemOfWord(queryTerm.toLowerCase());
            if (queryTermList.containsKey(word)) {
                continue;
            }
            List<Document> docList = new ArrayList<Document>();
            QueryBuilder qb = QueryBuilders.matchQuery("text", word);
            SearchResponse searchRes = elasticSearchConnection
                    .getClient()
                    .prepareSearch(INDEX_NAME)
                    .setScroll(new TimeValue(600000))
                    .setQuery(qb)
                    .setExplain(true)
                    .execute()
                    .actionGet();

            outerLoop:
            while (true) {
                for (SearchHit searchHit : searchRes.getHits().getHits()) {
                    if (searchRes.getHits().getTotalHits() > 10000) {

                        // do not consider terms occurring frequently
                        break outerLoop;
                    }

                    Document document = new Document();
                    document.setDocumentId(searchHit.getId());
                    document.setDocumentFrequency(searchRes.getHits().getTotalHits());
                    document.setTerm(word);

                    try {
                        long val = (long) Double.parseDouble((searchHit
                                .getExplanation()
                                .toString()
                                .split("termFreq=")[1])
                                .split("\n")[0]);
                        
                        document.setTermFrequency(val);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    docList.add(document);
                }

                searchRes = elasticSearchConnection.
                        getClient()
                        .prepareSearchScroll(searchRes.getScrollId())
                        .setScroll(new TimeValue(60000))
                        .execute()
                        .actionGet();
                // No hits are returned
                if (searchRes.getHits().getHits().length == 0) {
                    break;
                }
            }
            queryTermList.put(word, docList);
        }
    }


    void generateOkapiScore(String[] queryTerms) {

        List<Document> okapiDocument = new ArrayList<Document>();
        for(String query :  queryTerms){
            String term = getStemOfWord(query.toLowerCase());
            if(queryTermList.containsKey(term)){
                List<Document> okapiList = queryTermList.get(term);
                okapiDocument.addAll(okapiList);
            }
        }
        Map<String,Double> okapiScoreDocList= new HashMap<String, Double>();
        for (Document okapiVal : okapiDocument) {
            double okapiScore = Okapi.okapiScore
                    (okapiVal.getTermFrequency(),
                            Long.valueOf((String) docsLengthValue.get(okapiVal.getDocumentId())),
                            okapiVal.getDocumentId(),
                            okapiVal.toString());

            if (okapiScoreDocList.containsKey(okapiVal.getDocumentId())) {
                double okScore = okapiScore + okapiScoreDocList.get(okapiVal.getDocumentId());
                okapiScoreDocList.put(okapiVal.getDocumentId(), okScore);
            } else {
                okapiScoreDocList.put(okapiVal.getDocumentId(), okapiScore);
            }
        }
        Map<String,Double> sorted = SortValue.sortByComparator(okapiScoreDocList);

        try {
            String fileName = "results_okapi";
            SortValue.storeResultsToFile(sorted,queryTerms[0],fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
       // generateTFIDFScore(okapiDocument,okapiScoreDocList,queryTerms[0]);
    }

    private void generateTFIDFScore(List<Document> okapiDocument, Map<String, Double> okapiScoreDocList, String queryTerm) {
        Map<String,Double> tfIdfScoreDocumentList= new HashMap<String, Double>();
        for (Document doc : okapiDocument) {
            double tfidfScore = TFIDF.tfidfScore(okapiScoreDocList.get(doc.getDocumentId()),
                    Driver.TOTAL_DOCUMENTS, doc.getDocumentFrequency());
            if (tfIdfScoreDocumentList.containsKey(doc.getDocumentId())) {
                double tfScore = tfidfScore + tfIdfScoreDocumentList.get(doc.getDocumentId());
                tfIdfScoreDocumentList.put(doc.getDocumentId(), tfScore);
            } else {
                tfIdfScoreDocumentList.put(doc.getDocumentId(), tfidfScore);
            }
        }

        Map<String,Double> sortedScore = SortValue.sortByComparator(tfIdfScoreDocumentList);

        try {
            String fileName = "results_tfidf";
            SortValue.storeResultsToFile(sortedScore, queryTerm, fileName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void generateBM25Score(String[] queryTerms) {
        List<Document> bm25Document = new ArrayList<Document>();
        for(String query: queryTerms){
            String term = getStemOfWord(query.trim().toLowerCase());
            if(queryTermList.containsKey(term)){
                List<Document> bm25List = queryTermList.get(term);
                bm25Document.addAll(bm25List);
            }
        }

        Map<String,Double> bm25ScoreDocList= new HashMap<String, Double>();
        for (Document doc : bm25Document) {
            double bm25Score = BM25.okapiBM25Score(Driver.TOTAL_DOCUMENTS,
                    doc.getDocumentFrequency(), doc.getTermFrequency(),
                    Long.valueOf(docsLengthValue.get(doc.getDocumentId()).toString()));

            if (bm25ScoreDocList.containsKey(doc.getDocumentId())) {
                double bmScore = bm25Score + bm25ScoreDocList.get(doc.getDocumentId());
                bm25ScoreDocList.put(doc.getDocumentId(), bmScore);

            } else {
                bm25ScoreDocList.put(doc.getDocumentId(), bm25Score);
            }
        }
        Map<String,Double> sortedScore = SortValue.sortByComparator(bm25ScoreDocList);
        try {
            String fileName = "results_bm25";
            SortValue.storeResultsToFile(sortedScore,queryTerms[0], fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateLaplaceScore(String[] queryTerms) {
        List<Document> queryDocument = new ArrayList<Document>();
        for(int i = 1 ; i < queryTerms.length;i++){
            String term = getStemOfWord(queryTerms[i].trim().toLowerCase());
            if(queryTermList.containsKey(term)){
                List<Document> docsList = queryTermList.get(term);
                queryDocument.addAll(docsList);
            }
        }
        Map<String,Integer> penalizedDocs = new HashMap<String,Integer>();      // language models need different score calculation
        Iterator<Document> docIter = queryDocument.iterator();
        while(docIter.hasNext()){
            Document document = docIter.next();
            for (String term : queryTerms) {
                String qterm = getStemOfWord(term.trim().toLowerCase());
                if(document.getTerm().equals(qterm)){
                    if(penalizedDocs.containsKey(document.getDocumentId())){
                        penalizedDocs.put(document.getDocumentId(), penalizedDocs.get(document.getDocumentId())+1);
                    }
                    else{
                        penalizedDocs.put(document.getDocumentId(),1);
                    }
                }
            }
        }

        Map<String,Double> laplaceScoreDocList= new HashMap<String, Double>();
		docIter = queryDocument.iterator();
        while(docIter.hasNext()){
            Document doc = docIter.next();
            double uniLaplaceScore = Laplace.lapaceScore(doc.getTermFrequency(),
                    Long.valueOf(docsLengthValue.get(doc.getDocumentId()).toString()));

            if(laplaceScoreDocList.containsKey(doc.getDocumentId())){
                double uScore = uniLaplaceScore + laplaceScoreDocList.get(doc.getDocumentId());
                laplaceScoreDocList.put(doc.getDocumentId(), uScore);
            }
            else{
                laplaceScoreDocList.put(doc.getDocumentId(), uniLaplaceScore);
            }
        }

        // Add the penalized score
        docIter = queryDocument.iterator();
        while(docIter.hasNext()){
            Document doc = docIter.next();
            double penalizedScore = Laplace.penalizedScore(Long.valueOf(docsLengthValue.get(doc.getDocumentId()).toString()),
                    queryTerms.length - penalizedDocs.get(doc.getDocumentId()));
            double tScore = penalizedScore + laplaceScoreDocList.get(doc.getDocumentId());
            laplaceScoreDocList.put(doc.getDocumentId(), tScore);
        }

        for (Map.Entry<Object, Object> entry : docsLengthValue.entrySet()) {
            if (laplaceScoreDocList.containsKey(entry.getKey().toString().trim())) {
                continue;
            } else {
                double penalizedScore = Laplace.penalizedScore(Long.valueOf(entry.getValue().toString()), queryTerms.length);
                laplaceScoreDocList.put(entry.getKey().toString().trim(), penalizedScore);
            }
        }

        for (Map.Entry<String, Double> entry : laplaceScoreDocList.entrySet()) {
            entry.setValue(Math.log10(entry.getValue()));
        }


        Map<String,Double> sorted = SortValue.sortByComparator(laplaceScoreDocList);

        try {
            String fileName="results_laplace";
            SortValue.storeResultsToFile(sorted, queryTerms[0], fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void generateJelinekScore(String[] queryTerms) {
        Map<String,Double> docScores = new HashMap<String,Double>();
        List<Document> tempQueryTermList = null;
        for(int i = 1; i < queryTerms.length; i++){
            String term = getStemOfWord(queryTerms[i].trim().toLowerCase());
            if(queryTermList.containsKey(term)){
                tempQueryTermList = null;
                tempQueryTermList = new ArrayList<Document>(queryTermList.get(term));
                //compute total freq of all this term;
                long totalTermCount = 0;
                for (Document aTempQueryTermList : tempQueryTermList) {
                    totalTermCount = totalTermCount + aTempQueryTermList.getTermFrequency();
                }
                if(totalTermCount == 0){
                    continue;
                }
                final List<String> docIdsOfExistingQueryTerms = new ArrayList<String>();
                for (Document aTempQueryTermList : tempQueryTermList) {
                    docIdsOfExistingQueryTerms.add(aTempQueryTermList.getDocumentId());
                }
                for (String docId : docIds) {
                    if (docIdsOfExistingQueryTerms.contains(docId)) {
                        continue;
                    } else {

                        if (Long.valueOf(docsLengthValue.get(docId).toString()) == 0) {
                            continue;
                        }

                        Document document = new Document();
                        document.setDocumentId(docId);
                        document.setTerm(term);
                        tempQueryTermList.add(document);
                    }
                }

                for (Document doc : tempQueryTermList) {
                    double jScore = JelinekMercer.score(doc,
                            Long.valueOf(docsLengthValue.get(doc.getDocumentId()).toString()), totalTermCount);

                    if (docScores.containsKey(doc.getDocumentId())) {
                        double score = jScore + docScores.get(doc.getDocumentId());
                        docScores.put(doc.getDocumentId(), score);
                    } else {
                        docScores.put(doc.getDocumentId(), jScore);
                    }
                }
            }
        }

        Map<String,Double> sorted = SortValue.sortByComparator(docScores);


        try {
            String fileName ="results_Jelinek";
            SortValue.storeResultsToFile(sorted,queryTerms[0], fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loading the properties files into data structures
    void loadProperties() {
        try {
            docsLengthValue.load(ScoreCalculator.class.getClassLoader()
                    .getResourceAsStream("documentLength.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
