package com.irassignment8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static com.irassignment8.TopicModel.cleanText;
import static com.irassignment8.TopicModel.sortByComparator;


public class ClusteringModel {
    private final Set<String> stopWords = new HashSet<String>();
    private final Set<String> tokenSet = new HashSet<String>();
    private final Map<String,Doc> docMap = new LinkedHashMap<String,Doc>();
    private final Map<String,String> tokenMap = new HashMap<String, String>();
    private final Map<String,String> tokenReverseMap = new HashMap<String, String>();
    private final Map<String,Map<String,Double>> topicToTokenMapping = new HashMap<String, Map<String,Double>>();
    private final Map<String,Map<String,Double>> docToTopicMapping = new HashMap<String, Map<String,Double>>();

    public void loadFiles() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("stoplist.txt"));
        String line;
        while((line=reader.readLine())!=null){
            stopWords.add(line.trim());
        }
        reader.close();
        File folder = new File("ap89_collection");
        File[] docFiles = folder.listFiles();
        for (File docFile : docFiles) {
            if (validateFile(docFile)) {
                List<Doc> listOfDocs = parseFile(docFile);
                for (Doc doc : listOfDocs) {
                    docMap.put(doc.getDOCNO(), doc);
                }
            }
        }
    }

    public void generateWordsMapping() throws Exception{
        for (Doc doc : docMap.values()) {
            String[] tokens = getDocText(doc).split(" ");
            for (String token : tokens) {
                tokenSet.add(token.toLowerCase().trim());
            }
        }
        generateTokenMappingFile();
        generateMatrixFile();
    }

    public void generateTokenMappingFile() throws IOException{
        final BufferedWriter writer = new BufferedWriter(new FileWriter("Clustering/TokensMapping.txt"));
        long count = 0;
        for (String token : tokenSet) {
            if(validToken(token) && !token.equals("null")){
                writer.write(token+" "+count+System.lineSeparator());
                tokenMap.put(token, String.valueOf(count));
                tokenReverseMap.put(String.valueOf(count),token);
                ++count;
            }
        }
        writer.flush();
        writer.close();
    }

    public boolean validToken(String token){
        return !stopWords.contains(token.toLowerCase().trim()) && token.length()>3;
    }

    private void generateMatrixFile() throws IOException{
        final BufferedWriter writer = new BufferedWriter(new FileWriter("Clustering/MatrixFile.txt"));
        final BufferedWriter docIdWriter = new BufferedWriter(new FileWriter("Clustering/DocIdWriter.txt"));
        for (String docId : docMap.keySet()) {
            docIdWriter.write(docId+System.lineSeparator());
            writer.write("|");
            Doc doc  = docMap.get(docId);
            String[] words = getDocText(doc).split(" ");
            for (String word : words) {
                String token = word.toLowerCase().trim();
                if(tokenMap.containsKey(token)){
                    writer.write(" "+ tokenMap.get(token));
                }
            }
            writer.write(System.lineSeparator());
        }
        writer.flush();docIdWriter.flush();
        writer.close();docIdWriter.close();
    }


    private String getDocText(Doc doc){
        return cleanText(doc.getHEAD() + " " + doc.getTEXT());
    }


    private boolean validateFile(File docFile) {
        return Pattern.compile("^ap").matcher(docFile.getName()).find();
    }


    private List<Doc> parseFile(File docFile) throws IOException {
        List<Doc> listOfDocs = new ArrayList<Doc>();

        Document fileAsDocList = Jsoup.parse(docFile, "UTF-8");
        Elements docs = fileAsDocList.getElementsByTag("Doc");

        Iterator<Element> docElemIter = docs.iterator();
        while(docElemIter.hasNext()){
            Element doc = docElemIter.next();
            Doc tempDoc = unmarshellDOC(doc);
            if(tempDoc!=null){
                listOfDocs.add(tempDoc);
            }
            if(tempDoc.getTEXT()==null){
                System.out.println("Text null foung for " + doc.html());
            }
        }
        return listOfDocs;
    }

    private Doc unmarshellDOC(Element doc) {
        Doc tempDOC = new Doc();
        String[] tags = {"DOCNO","HEAD","TEXT"};

        for (String tag : tags) {
            updateDOC(tag,tempDOC,doc);
        }
        return tempDOC;
    }

    private void updateDOC(String tag, Doc tempDOC, Element doc) {
        Elements docTextElements = doc.getElementsByTag(tag);
        Iterator<Element> textIter = docTextElements.iterator();
        while(textIter.hasNext()){
            String textValue = textIter.next().text();
            switch(tag){
                case "DOCNO":tempDOC.setDOCNO(textValue);break;
                case "HEAD":tempDOC.setHEAD(textValue);break;
                case "TEXT":tempDOC.setTEXT(textValue);break;
            }
        }
    }

    public void generateTopics() throws IOException {
        final int NO_OF_TOPICS = 50;
        BufferedReader reader = new BufferedReader(new FileReader("Clustering/shortModel.txt"));
        for(int i=0;i<10;i++){
            reader.readLine();
        }

        List<Map<String,Double>> topics = new ArrayList<Map<String,Double>>();
        for(int i = 0;i < NO_OF_TOPICS;i++){
            topics.add(new HashMap<String, Double>());
        }

        for(int word = 0; word <= tokenMap.size(); word++){
            String[] values = reader.readLine().trim().split(" ");
            String token = tokenReverseMap.get(values[0].trim());

            for(int topicIndex = 0; topicIndex< NO_OF_TOPICS;topicIndex++){
                double value = 0.1;
                try{
                    value = Double.valueOf(values[topicIndex+1]);
                }catch(Exception e){
                }
                topics.get(topicIndex).put(token, value);
            }
        }

        printTopics(topics);
        reader.close();
    }

    private void printTopics(List<Map<String, Double>> topics) throws IOException {
        final int NO_OF_WORDS_PER_TOPIC = 20;
        BufferedWriter writer = new BufferedWriter(new FileWriter("Clustering/Topics.txt"));
        for (int i = 0 ; i < topics.size() ;i++) {
            writer.write("TOPIC-"+i+System.lineSeparator());
            //write the topic
            Map<String,Double> tempMap = new HashMap<String, Double>(NO_OF_WORDS_PER_TOPIC);
            Map<String,Double> sortedMap = sortByComparator(topics.get(i));
            Iterator<Map.Entry<String, Double>> iter = sortedMap.entrySet().iterator();
            for(int j = 0 ; j < NO_OF_WORDS_PER_TOPIC;j++){
                Entry<String,Double> entry = iter.next();
                writer.write(entry.getKey()+" "+ entry.getValue()+System.lineSeparator());
                tempMap.put(entry.getKey(), entry.getValue());
            }
            topicToTokenMapping.put("TOPIC-"+i, tempMap);
        }
        writer.flush();
        writer.close();
    }

    public void generateTopicDocDistribution() {
        int count = 0;
        for (String docId : docMap.keySet()) {
            Map<String,Integer> docTf = getDocTf(docMap.get(docId));
            Map<String,Double> tempMap = new HashMap<String,Double>();
            for(int i = 0; i < 50; i++){
                Map<String,Double> listOfWordsForTopic = topicToTokenMapping.get("TOPIC-"+i);
                double value = 0.0;
                for (String word : listOfWordsForTopic.keySet()) {
                    if(docTf.containsKey(word)){
                        value = value+ docTf.get(word) * listOfWordsForTopic.get(word);
                    }
                }
                tempMap.put("TOPIC-"+i, value);
            }
            docToTopicMapping.put(docId, tempMap);
        }
    }

    private Map<String, Integer> getDocTf(Doc doc) {
        Map<String,Integer> map = new HashMap<String, Integer>();
        String[] words = getDocText(doc).toLowerCase().split(" ");
        for (String word : words) {
            word = word.toLowerCase().trim();
            if(map.containsKey(word)){
                map.put(word, map.get(word)+1);
            }else{
                map.put(word,1);
            }
        }
        return map;
    }

    public void generateMatrix() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("test.arff"));
        for (String docId : docToTopicMapping.keySet()) {
            writer.write(docId+",");
            Map<String,Double> topicsMap = docToTopicMapping.get(docId);
            for(int i=0;i<50;i++){
                if(i==49){
                    writer.write(topicsMap.get("TOPIC-"+i)+System.lineSeparator());
                }else{
                    writer.write(topicsMap.get("TOPIC-"+i)+",");
                }
            }
        }
        writer.flush();
        writer.close();
    }


}
