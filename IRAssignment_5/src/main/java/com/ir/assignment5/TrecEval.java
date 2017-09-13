package com.ir.assignment5;

import java.io.*;
import java.util.*;

/**
 * Created by ps on 7/16/17.
 */
public class TrecEval {

    private final Map<String,Map<String,Integer>> qrelMap = new HashMap<String, Map<String,Integer>>();
    private final Map<String,List<Doc>> resultMap = new HashMap<String, List<Doc>>();
    private List<Double> avgPrecisionList = new ArrayList<Double>();
    private List<Double> rPrecisionList = new ArrayList<Double>();
    private final Map<String, Map<String, List<Double>>> mergedQrelMap = new HashMap<String, Map<String, List<Double>>>();

    public void loadData(String qrel, String result)  {
        try {
            loadQrelMap(qrel);
            loadResultMap(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadResultMap(String result) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(result));
        String line;
        while((line=br.readLine())!=null){
            String[] resultArr = line.split(" ");
            Doc doc = new Doc();
            doc.setDocId(resultArr[1].trim());
           // doc.setScore(Double.parseDouble(resultArr[4].trim()));
            doc.setRank(Integer.parseInt(resultArr[2].trim()));
            if(qrelMap.containsKey(resultArr[0].trim()) &&
                    qrelMap.get(resultArr[0].trim()).containsKey(resultArr[1].trim())){
                doc.setRelevance(qrelMap.get(resultArr[0].trim()).get(resultArr[1].trim()));
            }else{
                doc.setRelevance(0);
            }
            if(resultMap.containsKey(resultArr[0].trim())){
                resultMap.get(resultArr[0].trim()).add(doc);
            }else{
                List<Doc> tempList = new ArrayList<Doc>();
                tempList.add(doc);
                resultMap.put(resultArr[0].trim(), tempList);
            }
        }

        br.close();
//        for(String query: resultMap.keySet()){
//            List<Doc> tempList = resultMap.get(query);
//            Collections.sort(tempList);
//            List<Doc> temp = new ArrayList<Doc>();
//            for(int i = 0;i< tempList.size(); i++){
//                temp.add(tempList.get(i));
//            }
//            resultMap.get(query).clear();
//            resultMap.get(query).addAll(temp);
//        }
    }

    private void loadQrelMap(String qrel) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(qrel));
        String line;
        while((line = br.readLine())!= null){
            String [] qrelArr = line.split(" ");
            if(qrelMap.containsKey(qrelArr[0].trim())){
                qrelMap.get(qrelArr[0]).put(qrelArr[1].trim(), Integer.parseInt(qrelArr[2].trim()));
            } else{
                Map<String,Integer> tempMap = new HashMap<String, Integer>();
                tempMap.put(qrelArr[1], Integer.parseInt(qrelArr[2]));
                qrelMap.put(qrelArr[0], tempMap);
            }
        }
        br.close();
    }

    public void generatePrecRecall() {
        for(String t : resultMap.keySet()){
            int relevalntDoc = getRelevantDocs(qrelMap.get(t));
            System.out.println("tot"+ relevalntDoc);
            calculatePrecRecall(resultMap.get(t), relevalntDoc);
        }
    }

    private void calculatePrecRecall(List<Doc> list, int totalRelevalntDoc) {
        int relCount = 0;
        for (int i = 0; i < list.size(); i++) {
            Doc tempDoc = list.get(i);
            if(tempDoc.getRelevance() == 1){
                ++relCount;
            }
            tempDoc.setRecall((double)relCount/totalRelevalntDoc);
            tempDoc.setPrecision((double)relCount/(i+1));

            System.out.println(tempDoc.getDocId()+tempDoc.getPrecision()+" precis " + tempDoc.getRecall()+ " recall");
        }
    }

    private int getRelevantDocs(Map<String, Integer> strMap) {
        int relCount = 0;
        for (int rVal : strMap.values()) {
            relCount = relCount + rVal;
        }
        return relCount;
    }

    public void generateMeasureScores() {
        for (String query : resultMap.keySet()) {
            List<Doc> docs = resultMap.get(query);
            int total_number_of_relevant = getRelevantDocs(qrelMap.get(query));
            Measure fMeasure = Measure.calculateFMeasure(query,docs);
            Measure ndcgMeasure = Measure.calculateNDCGMeasure(query, docs, resultMap);
            double avgPrecision = Measure.calculateAvgPrecision(query,docs,total_number_of_relevant, qrelMap);
            double rPrecision = Measure.calculateRPrecision(query,docs,total_number_of_relevant, qrelMap);

            avgPrecisionList.add(avgPrecision);
            rPrecisionList.add(rPrecision);

        }
    }

    private int getRelDoc(List<Doc> docs) {
        int count = 0;
        for (Doc doc : docs) {
            if(doc.getRelevance()==1){
                ++count;
            }
        }
        return count;
    }

//    void createMergeAvgFile(String s) {
//        try{
//            BufferedReader br = new BufferedReader(new FileReader(s));
//            String line;
//            Map<String, Double> temp = new HashMap<String, Double>();
//            Map<String, String> temp1 = new HashMap<String, String>();
//            while((line = br.readLine())!=null){
//                String [] val = line.split(" ");
//                if(temp.containsKey(val[2]))
//                {
//                    double tempVal= temp.get(val[2]);
//                    tempVal+= Double.parseDouble(val[3]);
//                    temp.put(val[2], tempVal);
//                }
//                else
//                {
//                    temp.put(val[2], Double.parseDouble(val[3]));
//                    temp1.put(val[2], val[0]);
//                }
//            }
//            BufferedWriter bfw1 = new BufferedWriter(new FileWriter("tempMer.txt"));
//
//            for(Map.Entry<String, Double> k : temp.entrySet()){
//                k.setValue(k.getValue()/3);
//                String str =(temp1.get(k.getKey()) + " " + k.getKey() + " " + k.getValue());
//                bfw1.write(str + System.getProperty("line.separator"));
//            }
//            bfw1.close();
//        }
//
//
//        catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    public void createBooleanFile(String originalQrelFile) {
try {
    BufferedReader reader = new BufferedReader(new FileReader(originalQrelFile));
    String line = new String();
    while ((line = reader.readLine()) != null) {
        String[] values = line.split(" ");
        if (mergedQrelMap.containsKey(values[0].trim())) {
            if (mergedQrelMap.get(values[0].trim()).containsKey(values[2].trim())) {
                mergedQrelMap.get(values[0].trim()).get(values[2].trim()).add(Double.parseDouble(values[3].trim()));
            } else {
                List<Double> tempList = new ArrayList<Double>();
                tempList.add(Double.parseDouble(values[3].trim()));
                mergedQrelMap.get(values[0].trim()).put(values[2].trim(), tempList);
            }
        } else {
            Map<String, List<Double>> tempMap = new HashMap<String, List<Double>>();
            List<Double> tempList = new ArrayList<Double>();
            tempList.add(Double.parseDouble(values[3].trim()));
            tempMap.put(values[2].trim(), tempList);
            mergedQrelMap.put(values[0].trim(), tempMap);
        }
    }

    reader.close();

    BufferedWriter bfw = new BufferedWriter(new FileWriter("qrelFileBoolean.txt"));
    BufferedWriter bfwNonBoolean = new BufferedWriter(new FileWriter("qrelFileNonBoolean.txt"));
    for (Map.Entry<String, Map<String, List<Double>>> entry : mergedQrelMap.entrySet()) {
        String queryNo = entry.getKey();
        Map<String, List<Double>> values = entry.getValue();
        for (Map.Entry<String, List<Double>> queryEntry : values.entrySet()) {
            double value1 = queryEntry.getValue().get(0);
            double value2 = queryEntry.getValue().get(1);
            double value3 = queryEntry.getValue().get(2);
            double avg = (value1 + value2 + value3) / 3.0;
            if (avg < 0.5) {
                bfw.write(queryNo + " " + queryEntry.getKey() + " " + 0 + System.getProperty("line.separator"));
            } else {
                bfw.write(queryNo + " " + queryEntry.getKey() + " " + 1 + System.getProperty("line.separator"));
            }

            bfwNonBoolean.write(queryNo + " " + queryEntry.getKey() + " " + (value1 + value2 + value3) / 3.0 +
                    System.getProperty("line.separator"));
            }
    }
    bfw.flush();
    bfw.close();
    bfwNonBoolean.flush();
    bfwNonBoolean.close();
}
catch (Exception e){
    e.printStackTrace();}
    }
}
