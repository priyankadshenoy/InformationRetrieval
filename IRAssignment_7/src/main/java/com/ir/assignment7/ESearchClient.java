package com.ir.assignment7;


import com.google.gson.Gson;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by ps on 8/4/17.
 */
public class ESearchClient {
    public TransportClient transportClient = null;
    Map<String, String> mailSpamHamMap = new HashMap<>();
    Map<String, String> testingIdMap = new HashMap<String, String>();
    Map<String, String> trainingIdMap = new HashMap<String, String>();
    private static final List<InMail> mailList = new LinkedList<InMail>();
    private final Set<String> features = new HashSet<String>();
    private final Gson gson = new Gson();
    AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;

    ESearchClient() throws IOException {
    }

    public void configure() throws IOException {
        Settings settings = Settings.builder()
                .put("cluster.name", "my-application").build();
        transportClient = new PreBuiltTransportClient(settings);
        try {
            transportClient.addTransportAddress
                    (new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
            System.out.println("Transport Connected");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    void loadData() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("full/index"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] val = line.split(" ");
            String[] temp = val[1].split("/data/");
            mailSpamHamMap.put(temp[1].toLowerCase().trim(), val[0].toLowerCase().trim());
        }
        br.close();
    }

    void loadMapData() {
        File testingIdData = new File("full/testingData_new.txt");
        File trainingIdData = new File("full/trainingData_new.txt");
        loadTestTrainData(testingIdData, testingIdMap);
        loadTestTrainData(trainingIdData, trainingIdMap);
    }


    private void loadTestTrainData(File fileName, Map<String, String> mapData) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(" ");
                mapData.put(values[0].trim(), values[1].trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void createMatrix() throws IOException {
        Set<String> idVal = new HashSet<String>();
        idVal.addAll(trainingIdMap.keySet());
        idVal.addAll(testingIdMap.keySet());
        int totalcount = 0;
        List<String> tempList = new ArrayList<String>();
        for (String id : idVal) {
            tempList.add(id);
            if (tempList.size() > 1000) {
                totalcount = totalcount + tempList.size();
                System.out.println("File scanned" + totalcount);
                updateFeatureMatrix(tempList);
                tempList.clear();
            }

        }
        updateFeatureMatrix(tempList);
        BufferedWriter writer = new BufferedWriter(new FileWriter("features/featureMatrixSparce.txt"));
        System.out.print(features.size());
        int ct = 0;
        for (String feature : features) {
            if (!feature.isEmpty()) {
                writer.write(feature + System.lineSeparator());
            } else {
                System.out.println("left out");
            }
        }
        writer.flush();
        writer.close();
    }

    private void updateFeatureMatrix(List<String> tempList) {

        MultiGetResponse multiGetItemResponses = transportClient
                .prepareMultiGet()
                .add("ps_spam_new", "document", tempList)
                .get();

        for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
            GetResponse response = itemResponse.getResponse();
            if (response.isExists()) {
                String[] values = new JSONObject(response
                        .getSourceAsString())
                        .getString("text")
                        .split(" ");
                for (String value : values) {
                    if (!value.trim().isEmpty())
                        features.add(value.trim());
                }
            } else {
                System.out.println("Response doesnt exist");
            }
        }
        // System.out.println("feature1" + features.size());
    }

    void transformData() throws IOException {
        BufferedWriter trainingWriter = new BufferedWriter(new FileWriter("full/trainingData_new.txt"));
        BufferedWriter testingWriter = new BufferedWriter(new FileWriter("full/testingData_new.txt"));
        File folderName = new File("data");
        File[] inMails = folderName.listFiles();
        int hamTrainingCount = 0;
        int spamTrainingCount = 0;
        for (File mailFile : inMails) {
            if (mailFile.getName().contains("inmail")) {
                String text = parseFile(mailFile);
                int id = Integer.valueOf(mailFile.getName().split("\\.")[1]);
                String label = mailSpamHamMap.get(mailFile.getName().trim());

                int _80_SPAM_DOCS = 40160;
                int _80_HAM_DOCS = 20176;
                if (label.equals("spam") && spamTrainingCount <= _80_SPAM_DOCS) {
                    indexElasticSearch(id, text, mailSpamHamMap.get(mailFile.getName().trim()),
                            mailFile.getName(), "train", false);
                    ++spamTrainingCount;
                    trainingWriter.write(id + " " + mailFile.getName().trim() + System.lineSeparator());
                } else if (label.equals("spam") && spamTrainingCount > _80_SPAM_DOCS) {
                    indexElasticSearch(id, text, mailSpamHamMap.get(mailFile.getName().trim()),
                            mailFile.getName(), "test", false);
                    ++spamTrainingCount;
                    testingWriter.write(id + " " + mailFile.getName().trim() + System.lineSeparator());
                } else if (label.equals("ham") && hamTrainingCount <= _80_HAM_DOCS) {
                    indexElasticSearch(id, text, mailSpamHamMap.get(mailFile.getName().trim()),
                            mailFile.getName(), "train", false);
                    ++hamTrainingCount;
                    trainingWriter.write(id + " " + mailFile.getName().trim() + System.lineSeparator());
                } else if (label.equals("ham") && hamTrainingCount > _80_HAM_DOCS) {
                    indexElasticSearch(id, text, mailSpamHamMap.get(mailFile.getName().trim()),
                            mailFile.getName(), "test", false);
                    ++hamTrainingCount;
                    testingWriter.write(id + " " + mailFile.getName().trim() + System.lineSeparator());
                }
            }
        }
        indexElasticSearch(-1, null, null, null, null, true);
        trainingWriter.flush();
        trainingWriter.close();
        testingWriter.flush();
        testingWriter.close();
        System.out.println("ham training" + hamTrainingCount);
        System.out.println("spam training" + spamTrainingCount);
    }

    private void indexElasticSearch(int id, String text, String spamLabel, String fileName, String split, boolean boolVal) throws IOException {


        if (!boolVal) {
            mailList.add(new InMail(String.valueOf(id), text, spamLabel, fileName, split));
            if (mailList.size() < 1000) {
                return;
            }
        }
        BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
        for (InMail mail : mailList) {
            bulkRequest.add(transportClient.
                    prepareIndex("ps_spam_new", "document", mail.getId())
                    .setSource(gson.toJson(mail, InMail.class)));
        }

        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            System.out.println("Error in executing Bulk Request!");
        }
        mailList.clear();
    }

    private String parseFile(File mailFile) throws IOException {
        return cleanFile(Jsoup.parse(mailFile, "UTF-8").text());
    }

    private String cleanFile(String text) throws IOException {
//        return text.replaceAll("[-=/]", " ")
//                .replaceAll("\\(", "")
//                .replaceAll("\\)", "")
//                .replaceAll("[\":\"+()<>!,;\"]", "")
//                .replaceAll("\\.()", "");

        Scanner tVal = new Scanner(text);
        String textN = null;

        while (tVal.hasNextLine()) {
            String line = tVal.nextLine();
            StandardTokenizer tokenizer = new StandardTokenizer(factory);
            tokenizer.setReader(new StringReader(line));
            tokenizer.reset();
            CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
            while (tokenizer.incrementToken()) {
                textN = text + attr.toString() + " ";
            }
        }
        return textN;

    }

//    @Override
//    public String getTermVector(String id){
//        TermVectorsResponse resp = transportClient.prepareTermVectors()
//                .setIndex("ps_spam")
//                .setType("document")
//                .setId(id)
//                .setSelectedFields("text")
//                .execute().actionGet();
//        XContentBuilder builder;
//        try {
//            builder = XContentFactory.jsonBuilder().startObject();
//            resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
//            builder.endObject();
//            JSONObject obj = new JSONObject(builder.string());
//            JSONObject obj1 = obj.getJSONObject("term_vectors").getJSONObject("text").getJSONObject("terms");
//            return obj1.toString();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }


}