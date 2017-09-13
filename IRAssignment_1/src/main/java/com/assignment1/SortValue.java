package com.assignment1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by ps on 5/19/17.
 */
public class SortValue {

    public static Map<String,Double> sortByComparator(Map<String, Double> scoreDocList) {
        // Convert Map to List
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(scoreDocList.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // Convert sorted map back to a Map
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, Double> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public static void storeResultsToFile(Map<String, Double> sortedScore, String queryTerm, String fileName) throws IOException {

        File okapiFile = new File(fileName);
        FileWriter fw = new FileWriter(okapiFile,true);
        Iterator<Map.Entry<String, Double>> qsIter = sortedScore.entrySet().iterator();
        int count = 1;
        while(qsIter.hasNext()){
            if(count == 1001){
                break;
            }
            Map.Entry<String, Double> tempEntry = qsIter.next();
            fw.write(queryTerm+" "+ "Q0" + " " + tempEntry.getKey()+" "+ count + " " +
                    tempEntry.getValue()+" " +"ResultValue");
            ++count;
            fw.append(System.getProperty("line.separator"));
        }

        fw.flush();
        fw.close();
    }
}
