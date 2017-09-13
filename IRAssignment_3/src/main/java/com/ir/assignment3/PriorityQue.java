package com.ir.assignment3;

import java.util.*;

/**
 * Created by ps on 6/16/17.
 */
class PriorityQue{
    private Map<String,Set<String>> inlinkSet;
    public List<URLLink> que = new ArrayList<URLLink>();
    static  int queueSize;


    PriorityQue(Map<String, Set<String>> inlinkSet) {
        this.inlinkSet = inlinkSet;
    }

    public PriorityQue() {

    }

    void enqueue(URLLink link) {
        link.setWaitingTime(System.currentTimeMillis());
        que.add(link);
//        System.out.println("ques "+ que.size());
    }

    boolean hasOutlinks() {
        return que.size() > 0;
    }

    List<URLLink> dequeue() {
//        if(que.size()<=2500100)
//        {
            proioritizeQueue();
        //}
        List<URLLink> set = new ArrayList<URLLink>();
        System.out.println("Queue size\t" + que.size());
        if(!que.isEmpty()){
            while(set.size()!= 200 && que.size()!= 0){      // top 500 links
                set.add(que.remove(0));
            }
            queueSize=que.size();
            return set;
        }
        queueSize = que.size();
        return null;
    }

    private void proioritizeQueue() {
//        try {

            Collections.sort(que, new Comparator<URLLink>() {
                public int compare(URLLink link1, URLLink link2) {
                    if (link1.getValidity() == link2.getValidity()) {
                        //Set t = inlinkSet.get(link1.getCannonicalizedURL());
                        if (inlinkSet.get(link1.getCannonicalizedURL()).size()
                                == inlinkSet.get(link2.getCannonicalizedURL()).size()) {
                            return (link1.getTimeOut() - link2.getTimeOut() > 0) ? 1 : -1;
                        }
                        return inlinkSet.get(link2.getCannonicalizedURL()).size()
                                - inlinkSet.get(link1.getCannonicalizedURL()).size();
                    }
                    return (link2.getValidity() - link1.getValidity());
                }
            });
        }
//        catch (OutOfMemoryError e){
//            System.out.println("Out of memory Exception");
//        }
    }


