package com.irassignment8;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by ps on 8/15/17.
 */
public class TopicModel {
    private Set<String> stopWords = new HashSet<String>();
    private final Map<String,Doc> docMap = new HashMap<String,Doc>();
    private final Map<String,Set<String>> queryDocMap = new HashMap<String, Set<String>>();
    private final Map<String,Set<String>> queryTokensMapping = new HashMap<String, Set<String>>();
    final Map<String,Map<String,Double>> queryScoreMapping = new HashMap<String, Map<String,Double>>();
    private final Map<String,Integer> tfCount  = new HashMap<String, Integer>();

    public void loadFiles() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("stoplist.txt"));
        String line;
        while((line=reader.readLine())!=null){
            stopWords.add(line.trim());
        }
        reader.close();

        File folder = new File("ap89_collection");
        File[] docFiles = folder.listFiles();
        assert docFiles != null;
        for (File docFile : docFiles) {
            if(validateFile(docFile)){
                List<Doc> listOfDocs = parseFile(docFile);
                for (Doc doc : listOfDocs) {
                    docMap.put(doc.getDOCNO(), doc);
                }
              //  System.out.println("ParsedFile: " + docFile.getName() + " No of <DOC>'s : " + listOfDocs.size());
            }
        }

        BufferedReader bm25Rreader  = new BufferedReader(new FileReader("results_bm25"));
        while((line=bm25Rreader.readLine())!=null){
            String[] data = line.split(" ");
            if(queryDocMap.containsKey(data[0].trim())){
                queryDocMap.get(data[0].trim()).add(data[2].trim());
            }else{
                Set<String> docIds = new HashSet<String>();
                docIds.add(data[2].trim());
                queryDocMap.put(data[0].trim(),docIds);
            }
        }
        bm25Rreader.close();

//        BufferedReader qrelRreader  = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));
//        while((line=qrelRreader.readLine())!=null){
//            String[] data = line.split(" ");
//            if(queryDocMap.containsKey(data[0].trim())){
//                queryDocMap.get(data[0].trim()).add(data[2].trim());
//            }
//            else
//            {
//                System.out.println("We missed something");
//            }
//        }
//        qrelRreader.close();


        //REad tf file
        BufferedReader tfReader  = new BufferedReader(new FileReader("termFreqMapping.txt"));
        while((line=tfReader.readLine())!=null){
            String[] data = line.split(" ");
            tfCount.put(data[0].trim(), Integer.valueOf(data[1].trim()));
        }
        tfReader.close();
    }

    private List<Doc> parseFile(File docFile) throws IOException {
        List<Doc> listOfDocs = new ArrayList<Doc>();
        String content = FileUtils.readFileToString(docFile);
        String headData = content.split("</HEAD>")[0].split("<HEAD>")[1];
        Document fileAsDocList = Jsoup.parse(docFile, "UTF-8");
        Elements docs = fileAsDocList.getElementsByTag("DOC");
        for (Element doc : docs) {
            Doc tempDoc = unwrapDoc(doc, headData);
            if (tempDoc != null) {
                listOfDocs.add(tempDoc);
            }
        }
        return listOfDocs;
    }

    private Doc unwrapDoc(Element doc, String headData) {
        Doc tempDOC = new Doc();
        String[] tags = {"DOCNO", "TEXT"};
        for (String tag : tags) {
            updateDoc(tag,tempDOC,doc, headData);
        }
        return tempDOC;
    }

    private void updateDoc(String tag, Doc tempDOC, Element doc, String headData) {
        tempDOC.setHEAD(headData);
        Elements docTextElements = doc.getElementsByTag(tag);
        for (Element docTextElement : docTextElements) {
            String textValue = docTextElement.text();
            switch (tag) {
                case "DOCNO":
                    tempDOC.setDOCNO(textValue);
                    break;
                case "TEXT":
                    tempDOC.setTEXT(textValue);
                    break;
            }
        }
    }


    private boolean validateFile(File docFile) {
        return Pattern.compile("^ap").matcher(docFile.getName()).find();
    }

    public void createWordMapping() throws IOException {
        for (Map.Entry<String, Set<String>> entry : queryDocMap.entrySet()) {
            createTokensMapping(entry.getKey(),entry.getValue());
            System.out.println("Mapping created "+ entry.getKey());
        }
        for (String queryId : queryDocMap.keySet()) {
            generateMatrixFile(queryId);
            System.out.println("Matrix created"+ queryId);
        }

    }

    private void generateMatrixFile(String queryId) throws IOException {
        BufferedWriter writer = new BufferedWriter
                (new FileWriter
                        ("Matrix/MatrixFile_"+queryId+".txt"));
        BufferedWriter docMappingwriter = new BufferedWriter
                (new FileWriter
                        ("MatrixDocIdMapping/MatrixFileDocMapping_"+queryId+".txt"));

        Map<String,String> tokenMapping = getTokenMapping(queryId);
        Set<String> docIds = queryDocMap.get(queryId);
        int count = 1;
        for (String docId : docIds) {
            writer.write("|"+" ");
            Doc doc = docMap.get(docId);
            String[] cleanTextTokens = getDocText(doc).split(" ");
            for (String token : cleanTextTokens) {
                if(tokenMapping.containsKey(token)){
                    if(token.equals("null"))continue;
                        writer.write(tokenMapping.get(token) + " ");
                }
            }
            writer.write(System.lineSeparator());
            docMappingwriter.write(count+" "+docId+System.lineSeparator());
            ++count;
        }
        writer.flush();
        docMappingwriter.flush();
        writer.close();
        docMappingwriter.close();
    }

    private void createTokensMapping(String queryId, Set<String> docIds) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter
                ("Mapping/TokenMapping_"+queryId+".txt"));
        final Set<String> tokens = new HashSet<String>();
        for (String docId : docIds) {
            Doc tempDoc = docMap.get(docId);
            String[] cleanTextTokens = getDocText(tempDoc).split(" ");
            for (String token : cleanTextTokens) {
                if(!stopWords.contains(token.toLowerCase().trim()) && (token.length()>3)){
                    tokens.add(token.toLowerCase().trim());
                }
            }
        }
        long count = 1;
        for (String token : tokens) {
            writer.write(token+" "+ count+ System.lineSeparator());
            ++count;
        }
        writer.flush();
        writer.close();
        queryTokensMapping.put(queryId, tokens);
    }

    private String getDocText(Doc doc){
        return cleanText(doc.getHEAD() + " " + doc.getTEXT());
    }

    public static String cleanText(String s) {
        return s.replaceAll("[^a-zA-Z]+", " ");
    }

    private Map<String, String> getTokenMapping(String queryId) throws IOException {
        Map<String,String> tempMapping = new HashMap<String, String>();
        BufferedReader reader  = new BufferedReader(new FileReader("Mapping/TokenMapping_"+queryId+".txt"));
        String line;
        while((line=reader.readLine())!=null){
            String[] values = line.split(" ");
            tempMapping.put(values[0].trim(), values[1].trim());
        }
        reader.close();
        return tempMapping;
    }

    public void createTopics() throws IOException {;
        System.out.println("into create topics");
        for (Map.Entry<String, Set<String>> entry : queryDocMap.entrySet()) {
            try{
                dumpToTopicFile(entry.getKey());
            }catch(FileNotFoundException fe){

            }
            System.out.println("TOPICS FILE CREATED FOR QUERY: "+ entry.getKey());
        }
        printWeightedScores();
    }

    private void printWeightedScores() {
        for (String key : queryScoreMapping.keySet()) {
            Map<String,Double> map = queryScoreMapping.get(key);
            Map<String,Double> sortedMap = sortByComparator(map);
            System.out.println("------------"+key+"--------------------");
            for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
                System.out.println(entry.getKey()+":"+entry.getValue());
            }
        }
    }

    private void dumpToTopicFile(String queryId) throws IOException {
        Map<String,Double> map = new HashMap<String, Double>();
        final BufferedWriter writer = new BufferedWriter(new FileWriter("Topics/Topics_"+queryId+".txt"));
        final BufferedReader reader  = new BufferedReader(new FileReader("Model/ModelFile_"+queryId+".txt"));
        for(int i=0;i<10;i++){
            reader.readLine();
        }
        List<Map<String,Double>> topicsList = new ArrayList<Map<String,Double>>();
        for(int i = 0 ; i < 20;i++){
            topicsList.add(new HashMap<String, Double>());
        }
        double[] weightedScore = new double[20];
        Map<String,String> tokenMapping = new HashMap<String, String>();
        Map<String,String> reverseTokenMapping = new HashMap<String, String>();
        uploadTokenMappings(tokenMapping,reverseTokenMapping,queryId);
        for(int i = 0; i < queryTokensMapping.get(queryId).size();i++){
            String[] values = reader.readLine().split(" ");
            for(int topicIndex = 1;topicIndex <=20; topicIndex++){
                topicsList.get(topicIndex-1)
                        .put(reverseTokenMapping.get(values[0].trim()),
                                Double.valueOf(values[topicIndex]));
            }
        }
        int count = 1;
        for (Map<String, Double> topicMap : topicsList) {
            Map<String,Double> sortedMap = sortByComparator(topicMap);
            writer.write("TOPIC-"+count+System.lineSeparator());
            Iterator<Map.Entry<String, Double>> iter = sortedMap.entrySet().iterator();
            for(int k = 0 ; k < 30; k++){
                Map.Entry<String,Double> entry = iter.next();
                writer.write(entry.getKey()+":"+iter.next().getValue()+System.lineSeparator());
                weightedScore[count-1] = weightedScore[count-1]+ iter.next().getValue()
                        * tfCount.get(iter.next().getKey());
            }

            map.put("TOPIC-"+count, weightedScore[count-1]);
            ++count;
        }
        writer.flush();
        writer.close();
        queryScoreMapping.put(queryId, map);
        reader.close();
		generateTopicDistForQuery(queryId,topicsList);
    }

    private void uploadTokenMappings(Map<String, String> tokenMapping2,
                                     Map<String, String> reverseTokenMapping, String queryId) throws IOException  {
        BufferedReader reader  = new BufferedReader(new FileReader("Mapping/TokenMapping_"+queryId+".txt"));
        String line;
        while((line=reader.readLine())!=null){
            String[] values = line.split(" ");
            tokenMapping2.put(values[0].trim(), values[1].trim());
            reverseTokenMapping.put(values[1].trim(), values[0].trim());
        }
        reader.close();
    }


    private void generateTopicDistForQuery(final String queryId, List<Map<String,Double>> topicList) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter("TopicsDist/topicDist"+queryId+".txt"));
        for (String docId : queryDocMap.get(queryId)) {
            Doc doc = docMap.get(docId);
            Map<String,Integer> docTermCount = getTfForDoc(doc);
            writer.write(doc.getDOCNO());
            for(int i=0;i<20;i++){
                Map<String,Double> sortedMap = sortByComparator(topicList.get(i));
                double value = 0.0;
                Iterator<Map.Entry<String, Double>> iter = sortedMap.entrySet().iterator();
                for(int k = 0 ; k < 30; k++){
                    Map.Entry<String,Double> entry = iter.next();
                    if(docTermCount.containsKey(entry.getKey())){
                        value = value + docTermCount.get(entry.getKey()) * entry.getValue();
                    }
                }
                if(value!= 0.0)
                    writer.write(" TOPIC-"+i+":"+value);
            }
            writer.write(System.lineSeparator());
        }
        writer.flush();
        writer.close();
    }

    private Map<String, Integer> getTfForDoc(Doc doc) {
        Map<String,Integer> map = new HashMap<String, Integer>();
        String[] words = getDocText(doc).split(" ");
        for (String word : words) {
            word = word.toLowerCase().trim();
            if(map.containsKey(word)){
                map.put(word, map.get(word));
            }else{
                map.put(word, 1);
            }
        }
        return map;
    }


    public static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {

        // Convert Map to List
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        list.sort(new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // Convert sorted map back to a Map
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

}
