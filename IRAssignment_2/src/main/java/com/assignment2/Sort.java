package com.assignment2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ps on 6/8/17.
 */
public class Sort {
    public static void Merge(List<TokenMap> a, int low, int mid, int high){
        List<TokenMap> temp = new ArrayList<TokenMap>();
        temp.addAll(a);
        int i = low; int j = mid+1; int count = low;

        while(i<=mid && j<=high){
            if(temp.get(j).getCount() < temp.get(i).getCount()){
                a.set(count, temp.get(i));
                i++;
            }else{
                a.set(count, temp.get(j));
                j++;
            }
            count++;
        }
        while(i<=mid){
            a.set(count, temp.get(i));
            i++;
            count++;
        }
    }
    
    static void mergeSort(List<TokenMap> a, int low, int high){
        if(low<high){
            int mid = (low+high)/2;
            mergeSort(a,low,mid);
            mergeSort(a,mid+1,high);
            Merge(a,low,mid,high);
        }
    }
}
