package com.ir.assignment7;

import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ps on 8/5/17.
 */
public class SpamMatrix {
    private final Map<String,String> mailsSpamMapping = new HashMap<String, String>();
    private final Map<String,String> testingIdsMapping = new LinkedHashMap<String, String>();
    private final Map<String,String> trainingIdsMapping = new LinkedHashMap<String, String>();
    private final Map<String,String> featureMapping = new LinkedHashMap<String, String>();
    private ESearchClient elSearchClient = null;


    int count=1;
    SpamMatrix(ESearchClient eSearchClient) {
        elSearchClient = eSearchClient;
    }

    void loadData() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("full/index"));
            String line;
            while((line=reader.readLine())!=null){
                String[] values = line.split(" ");
                String temp[] = values[1].split("/data/");
                mailsSpamMapping.put(temp[1].trim(), values[0].trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File testingIdsMapper = new File("full/testingData_new.txt");
        File trainingIdsMapper = new File("full/testingData_new.txt");
        if(testingIdsMapper.exists() && trainingIdsMapper.exists()){
            loadTestTrainData(testingIdsMapper,testingIdsMapping);
            loadTestTrainData(trainingIdsMapper,trainingIdsMapping);
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader("features/featuressmall.txt"));
            String line;
            while((line=reader.readLine())!=null){
                String[] values = line.split("-");
                try{
                    featureMapping.put(values[0].trim(), values[1].trim());
                }catch(Exception e){
                    System.out.println(line);
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void loadTestTrainData(File fileName, Map<String, String> map){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while((line=reader.readLine())!=null){
                String[] values = line.split(" ");
                map.put(values[0].trim(), values[1].trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createMatrix() {
        try {
            generateTrainingFeatureMatrix();
            generateTestingFeatureMatrix();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateTestingFeatureMatrix() throws IOException {
        BufferedWriter featureMatrixWriter = new BufferedWriter
                (new FileWriter("matrixdata/testingFeatureMatrixData_long.txt"));
        TermVectorsResponse resp;
        for (String id : testingIdsMapping.keySet()) {

            resp = elSearchClient
                    .transportClient
                    .prepareTermVectors()
                    .setIndex("ps_spam_new")
                    .setType("document")
                    .setId(id)
                    .setSelectedFields("text")
                    .execute()
                    .actionGet();
            XContentBuilder build = XContentFactory.jsonBuilder().startObject();
            resp.toXContent(build.field("text"), ToXContent.EMPTY_PARAMS);
            build.endObject();
            Map<String, Object> map = XContentHelper.convertToMap(build.bytes(), false).v2();
            Map<String, Object> txt= (Map) map.get("text");
            Map<String, Object> termVec= (Map) txt.get("term_vectors");
            Map<String, Object> text= ((Map) termVec.get("text"));
            Map<String, Object> terms=((Map) text.get("terms"));
            //String termVectorString = transportClient.getTermVector(id);

            writeToFile(mailsSpamMapping.get(testingIdsMapping.get(id)),terms,featureMatrixWriter);
//            ++count;
//            System.out.println(count);
        }
        featureMatrixWriter.flush();
        featureMatrixWriter.close();
    }

    private void generateTrainingFeatureMatrix() throws IOException {
        BufferedWriter featureMatrixWriter = new BufferedWriter
                (new FileWriter("matrixdata/trainingFeatureMatrixData_long.txt"));
        TermVectorsResponse resp;
            for (String id : trainingIdsMapping.keySet()) {

                 resp = elSearchClient
                        .transportClient
                        .prepareTermVectors()
                        .setIndex("ps_spam_new")
                        .setType("document")
                        .setId(id)
                        .setSelectedFields("text")
                        .execute()
                        .actionGet();
                XContentBuilder build = XContentFactory.jsonBuilder().startObject();
                resp.toXContent(build.field("text"), ToXContent.EMPTY_PARAMS);
                build.endObject();
                Map<String, Object> map = XContentHelper.convertToMap(build.bytes(), false).v2();
                Map<String, Object> txt= (Map) map.get("text");
                Map<String, Object> termVec= (Map) txt.get("term_vectors");
                Map<String, Object> text= ((Map) termVec.get("text"));
                Map<String, Object> terms=((Map) text.get("terms"));
                //String termVectorString = transportClient.getTermVector(id);

            writeToFile(mailsSpamMapping.get(trainingIdsMapping.get(id)),terms,featureMatrixWriter);
//            ++count;
//            System.out.println(count);
        }
        featureMatrixWriter.flush();
        featureMatrixWriter.close();
    }

    private void writeToFile(String isSpam, Map<String, Object> termVectorString, BufferedWriter featureMatrixWriter) throws IOException {
        String isSpamValue = isSpam.equals("spam")?"0":"1";
        Map<Integer,Integer> featureMap = new TreeMap<Integer, Integer>();
        JSONObject jsonObject = new JSONObject(termVectorString);
        for (String featureName : featureMapping.keySet()) {
            if(jsonObject.has(featureName)){
                featureMap.put(Integer.valueOf(featureMapping.get(featureName)),
                        jsonObject.getJSONObject(featureName).getInt("term_freq"));
                //featureMatrixWriter.write(" "+featureMapping.get(featureName)+":"+jsonObject.getJSONObject(featureName).getInt("term_freq"));
            }
        }
        featureMatrixWriter.write(isSpamValue+" ");
        for (int feature : featureMap.keySet()) {
            featureMatrixWriter.write(" "+feature+":"+featureMap.get(feature));
        }
        featureMatrixWriter.write(System.lineSeparator());
    }

    public void changeFeaureMatrix() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("features/featureMatrixSparce.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("features/featureMatrixSparceMap.txt"));
        String line;
        long ct=1;
        while((line=br.readLine())!=null){
            bw.write(line.trim()+"-"+ct);
            bw.write(System.lineSeparator());
            ct++;
        }
        bw.flush();
        bw.close();
    }
}
