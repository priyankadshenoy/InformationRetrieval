package com.irassignment8;

import com.irassignment8.Doc;
import com.irassignment8.TopicModel;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by ps on 8/16/17.
 */
public class GenerateMatrixRep {
    public static void main(String[] args) throws Exception {
        final String FOLDER_PATH = "ap89_collection";
        GenerateMatrix matrix = new GenerateMatrix();
//        matrix.loadFiles(FOLDER_PATH);
//        matrix.generateRepresentation();
        matrix.generateTFMapping();
    }

    private static class GenerateMatrix {
        private final Pattern validFilePattern = Pattern.compile("^ap");
        private final Set<String> stopWords = new HashSet<String>();
        private final Map<String,Doc> docMap = new HashMap<String,Doc>();
        private final Map<String,Set<String>> queryDocumentMapping = new HashMap<String, Set<String>>();


        public void loadFiles(String folderName) throws Exception {
            BufferedReader reader = new BufferedReader(new FileReader( "stoplist.txt"));
            String line;
            while((line=reader.readLine())!=null){
                stopWords.add(line.trim());
            }
            reader.close();
            File folder = new File(folderName);
            validateDataFolder(folder);
            File[] docFiles = folder.listFiles();
            assert docFiles != null;
            for (File docFile : docFiles) {
                if(validateFile(docFile)){
                    List<Doc> listOfDocs = parseFile(docFile);
                    for (Doc doc : listOfDocs) {
                        docMap.put(doc.getDOCNO(), doc);
                    }
                    System.out.println("ParsedFile: " + docFile.getName() + " No of <Doc>'s : " + listOfDocs.size());
                }
            }


            BufferedReader bm25Rreader  = new BufferedReader(new FileReader("results_bm25"));
            while((line=bm25Rreader.readLine())!=null){
                String[] data = line.split(" ");
                if(queryDocumentMapping.containsKey(data[0].trim())){
                    queryDocumentMapping.get(data[0].trim()).add(data[2].trim());
                }else{
                    Set<String> docIds = new HashSet<String>();
                    docIds.add(data[2].trim());
                    queryDocumentMapping.put(data[0].trim(),docIds);
                }
            }
            bm25Rreader.close();

            BufferedReader qrelRreader  = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));
            while((line=qrelRreader.readLine())!=null){
                String[] data = line.split(" ");
                if(queryDocumentMapping.containsKey(data[0].trim())){
                    queryDocumentMapping.get(data[0].trim()).add(data[2].trim());
                }
            }
            qrelRreader.close();
        }

        public void generateRepresentation() throws IOException {
            File folder = new File("Topics");
            File[] docFiles = folder.listFiles();
            for (File docFile : docFiles) {
                genereateTopicDistMatrix(docFile.getName()
                        .split("_")[1]
                        .replaceAll(".txt", ""), docFile);
            }
        }


        private void genereateTopicDistMatrix(String queryId,File file) throws IOException {
            BufferedWriter writer = new BufferedWriter(new FileWriter("TopicsDist/topicDist_"+queryId+".txt"));
            Map<String,Double> topicsValues = new HashMap<String, Double>();
            List<Doc> docs  = getListOfDocs(queryId);
            List<Map<String,Double>> topicsList = getTopicsList(queryId);
            for (Doc doc : docs) {
                writer.write(doc.getDOCNO()+ getRepData(doc,topicsList,topicsValues)+System.lineSeparator());
            }
            writer.flush();
            writer.close();
            BufferedWriter writer1 = new BufferedWriter(new FileWriter("TopicsDist1/topicDist_"+queryId+".txt"));
            Map<String,Double> topicsValues1 = TopicModel.sortByComparator(topicsValues);
            for (Map.Entry<String, Double> map : topicsValues1.entrySet()) {
                writer1.write(map.getKey()+":"+map.getValue()+System.lineSeparator());
            }
            writer1.flush();
            writer1.close();
        }


        private String getRepData(Doc doc, List<Map<String, Double>> topicsList,Map<String,Double> topicsValues) {
            StringBuilder builder = new StringBuilder();
            Map<String, Integer> tfCount = getTfForDoc(doc);
            for (int i = 0;i < 20;i++) {
                Map<String,Double> map = topicsList.get(i);
                Set<String> keys = map.keySet();
                double value = 0.0;
                for (String key : keys) {
                    if(tfCount.containsKey(key)){
                        value = value + tfCount.get(key) * map.get(key);
                    }
                }
                if(value!=0.0){
                    builder.append(" TOPIC-"+i+" "+value);
                    if(topicsValues.containsKey("TOPIC-"+i)){
                        topicsValues.put("TOPIC-"+i, topicsValues.get("TOPIC-"+i)+value);
                    }else{
                        topicsValues.put("TOPIC-"+i,value);
                    }
                }
            }
            return builder.toString();
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

        private String getDocText(Doc doc){
            return TopicModel.cleanText(new StringBuilder().append(doc.getHEAD()).append(" ").append(doc.getTEXT()).toString());
        }

        private List<Map<String, Double>> getTopicsList(String queryId) throws IOException {
            List<Map<String,Double>> list = new ArrayList<Map<String,Double>>();
            BufferedReader reader = new BufferedReader(new FileReader("Topics/Topics_"+queryId+".txt"));
            String line = new String();
            while((line=reader.readLine())!= null){
                if(line.contains("TOPIC")){
                    Map<String,Double> map = new HashMap<String, Double>();
                    String topicName = line;
                    for(int i = 0 ; i < 30; i++){
                        String[] value = reader.readLine().split(":");
                        map.put(value[0], Double.valueOf(value[1]));
                    }
                    list.add(map);
                }
            }
            return list;
        }

        private List<Doc> getListOfDocs(String queryId) {
            List<Doc> list = new ArrayList<Doc>();
            Set<String> set = queryDocumentMapping.get(queryId);
            for (String docId : set) {
                list.add(docMap.get(docId));
            }
            return list;
        }

        private boolean validateFile(File docFile) {
            return validFilePattern.matcher(docFile.getName()).find();
        }

        private void validateDataFolder(File folder) throws Exception {
            if(folder.exists() && folder.isDirectory()){
                return;
            }
            throw new Exception("Datafolder path is incorrect!");
        }

        private List<Doc> parseFile(File docFile) throws IOException {
            List<Doc> listOfDocs = new ArrayList<Doc>();
            Document fileAsDocList = Jsoup.parse(docFile, "UTF-8");
            Elements docs = fileAsDocList.getElementsByTag("Doc");
            String content = FileUtils.readFileToString(docFile);
            String headData = content.split("</HEAD>")[0].split("<HEAD>")[1];
            Iterator<Element> docElemIter = docs.iterator();
            while(docElemIter.hasNext()){
                Element doc = docElemIter.next();
                Doc tempDoc = unmarshellDOC(doc, headData);
                if(tempDoc!=null){
                    listOfDocs.add(tempDoc);
                }
                if(tempDoc.getTEXT()==null){
                    System.out.println("Text null foung for " + doc.html());
                }
            }
            return listOfDocs;
        }

        private Doc unmarshellDOC(Element doc, String headData) {
            Doc tempDOC = new Doc();
            String[] tags = {"DOCNO","HEAD","TEXT"};

            for (String tag : tags) {
                updateDOC(tag,tempDOC,doc, headData);
            }
            return tempDOC;
        }

        private void updateDOC(String tag, Doc tempDOC, Element doc, String headData) {
            Elements docTextElements = doc.getElementsByTag(tag);
            Iterator<Element> textIter = docTextElements.iterator();
            tempDOC.setHEAD(headData);
            while(textIter.hasNext()){
                String textValue = textIter.next().text();
                switch(tag){
                    case "DOCNO":tempDOC.setDOCNO(textValue);break;
                    //case "FILEID":tempDOC.setFILEID(textValue);break;
                    //case "FIRST":tempDOC.setFIRST(textValue);break;
                    //case "SECOND":tempDOC.setSECOND(textValue);break;
                    case "HEAD":tempDOC.setHEAD(textValue);break;
                    //case "BYLINE":tempDOC.setBYLINE(textValue);break;
                    //case "DATELINE":tempDOC.setDATELINE(textValue);break;
                    case "TEXT":tempDOC.setTEXT(textValue);break;
                }
            }
        }


        public void generateTFMapping() throws IOException {
            File folder = new File("Mapping");
            Map<String,Integer> wordCount = new HashMap<String, Integer>();
            File[] docFiles = folder.listFiles();
            BufferedWriter writer = new BufferedWriter(new FileWriter("termFreqMapping.txt"));
            for (File docFile : docFiles) {
                BufferedReader reader = new BufferedReader(new FileReader(docFile));
                String line;
                while((line=reader.readLine())!=null){
                    String[] values = line.split(" ");
                    if(wordCount.containsKey(values[0].trim())){
                        wordCount.put(values[0].trim(), wordCount.get(values[0].trim())+1);
                    }else{
                        wordCount.put(values[0].trim(), 1);
                    }
                }
                reader.close();
            }


            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                writer.write(entry.getKey()+" "+entry.getValue()+System.lineSeparator());
            }

            writer.flush();
            writer.close();
        }
    }
}
