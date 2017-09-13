package com.ir.assignment4;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by ps on 6/30/17.
 */
public class ElasticSearchConnect {
    private Settings settings = null;
    TransportClient transportClient = null;
    Set<String> crawledSet= null;
    Map<String, Set<String>> inlinkMap = new HashMap<String, Set<String>>();
    Map<String, Set<String>> outlinkMap = new HashMap<String, Set<String>>();
    Map<String, Double> hubVal = new HashMap<>();
    Map<String, Double> authVal = new HashMap<>();

    void configure() {
        settings = Settings.builder()
                .put("cluster.name", "paulbiypri").build();
        transportClient = new PreBuiltTransportClient(settings);
        try {
            transportClient.addTransportAddress(new InetSocketTransportAddress
                    (InetAddress.getByName("127.0.0.1"), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    void loadInlinksFromIndex() {
        BufferedWriter inlink  = null;
        try {
            inlink = new BufferedWriter(new FileWriter("finalInlink.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        QueryBuilder qb = QueryBuilders.matchAllQuery();
        SearchResponse scrollResp = transportClient.prepareSearch("bpp")
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100)
                .execute()
                .actionGet();
        while (true) {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                List inlinksList = (List) hit.getSource().get("in_links");
                StringBuilder sb = new StringBuilder();
                for (Object object : inlinksList) {
                    sb.append(String.valueOf(object)).append(" ");
                }
                try {
                    inlink.write(hit.getId()
                            +" "
                            +sb.toString().trim()
                            +System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            scrollResp = transportClient.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(60000))
                    .execute()
                    .actionGet();

            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
        try {
            inlink.flush();
            inlink.close();
            System.out.print("Merged inlink created\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rankAllPages() {
        QueryBuilder qb = QueryBuilders.queryStringQuery("maritime disaster");
        SearchResponse sr = transportClient.prepareSearch("bpp")
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(1000)
                .execute()
                .actionGet();

        for (SearchHit hit : sr.getHits().getHits()) {
         crawledSet.add(hit.getId());
         List inlinkList = (List) hit.getSource().get("in_links");
         List outlinkList = (List) hit.getSource().get("out_links");
         crawledSet.addAll(outlinkList);
         int maxL = inlinkList.size()> 100 ? 100 : inlinkList.size();
         crawledSet.addAll(inlinkList.subList(0, maxL));
         inlinkMap.put(hit.getId(), new HashSet<>(inlinkList));
         outlinkMap.put(hit.getId(), new HashSet<>(outlinkList));
        }

        for(String s: crawledSet){
            hubVal.put(s, 1.0);
            authVal.put(s, 1.0);
        }

        HubsAuthority hubsAuthourity = new HubsAuthority(crawledSet, hubVal, authVal, inlinkMap, outlinkMap);
        hubsAuthourity.rank();
    }
}
