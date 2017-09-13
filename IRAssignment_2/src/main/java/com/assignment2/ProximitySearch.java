package com.assignment2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ps on 6/9/17.
 */
public class ProximitySearch {
    public static int scoreForDocument(List<List<Integer>> lists) throws Exception{
        long maxList = 0;
        for (List<Integer> list : lists) {
            if(maxList<list.size()){
                maxList = list.size();
            }
        }
        int docScore = Integer.MAX_VALUE;
        int[] scores = new int[lists.size()];
        Map<Integer,Integer> map = new HashMap<Integer,Integer>();

        for (int i = 0; i < lists.size()*maxList; i++){

            map.clear();
            for(int j = 0 ; j < lists.size(); j++){
                scores[j] = lists.get(j).get(0);
                map.put(scores[j], j);
            }

            bubbleSort(scores);
            int tempScore = scores[scores.length-1]-scores[0];
            if(tempScore < docScore){
                docScore = tempScore+1;
            }

            boolean isDone = true;
            for (List<Integer> list : lists) {
                isDone = isDone && (list.size() == 1);
            }
            if(isDone){
                return docScore;
            }

            for(int y = 0 ; y< lists.size();y++){

                int arrPos = map.get(scores[y]);
                if(lists.get(arrPos).size()==1){
                    continue;
                }else{
                    lists.get(arrPos).remove(0);
                    break;
                }
            }

        }

        throw new Exception("Malfunctioning");
    }

    public static void bubbleSort(int[] a){
        for(int i = 0 ; i < a.length; i++){
            boolean swap = false;
            for(int j=0; j < (a.length-i-1); j++ ){
                if(a[j] > a[j+1]){
                    int temp = a[j];
                    a[j] = a[j+1];
                    a[j+1] = temp;
                    swap =true;
                }
            }
            if(!swap){
                break;
            }
        }
    }
}
