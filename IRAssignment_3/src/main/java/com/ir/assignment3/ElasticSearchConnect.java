package com.ir.assignment3;

import com.google.gson.Gson;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by ps on 6/21/17.
 */
public class ElasticSearchConnect {
    Settings settings= null;
    TransportClient transportClient = null;
    static String indexName = "bpp";
    static final String indexType = "document";
    static int docsIndexed = 0;
    final HttpClient httpClient = HttpClientBuilder.create().build();
    private final Gson gson = new Gson();
    private final Map<String,Set<String>> inlinksMap = new HashMap<String,Set<String>>();

    public void configure() {
        // TODO : change cluster URL and Port
        settings = Settings.builder()
                .put("cluster.name", "paulbiypri").build();
        transportClient = new PreBuiltTransportClient(settings);

        try {
            // todo : change ip
            transportClient.addTransportAddress(new InetSocketTransportAddress
                    (InetAddress.getByName("127.0.0.1"), 9300));

             System.out.println("Transport Connected");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        loadInlinksFile();
    }

    private void loadInlinksFile() {
        try {
            File f = new File("result_100/inlink.txt");
            Document document = Jsoup.parse(f, "UTF-8");
            Elements docs = document.getElementsByTag("DOC");
            for (Element element : docs) {
                String key = element.getElementsByTag("DOCNO").text();
                String val = element.getElementsByTag("INLINKS").text();
                inlinksMap.put(key, getOutlinks(val));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void indexDocument(File[] listOfFiles) throws IOException {
        System.out.println("Hello"+listOfFiles.length);
        for(File f: listOfFiles){
            if(f.getName().contains("output_")) {
                List<Doc> listOfDoc = parseFile(f);
                if(listOfDoc!=null)
                {
                    createESIndex(listOfDoc);
                }
            }
        }

    }

    private void createESIndex(List<Doc> listOfDoc) {
            for (Doc doc : listOfDoc) {

                // TODO : else ?? index whole document
                if(doc!=null){
                    boolean isSourceHtmlPresent = false;
                    //obtain the handler to elasticsearch
                    QueryBuilder qb = QueryBuilders.matchQuery("_id",doc.getDocno());

                    SearchResponse scrollResp = transportClient.prepareSearch(indexName)
                            .setQuery(qb).execute().actionGet();

                    if(scrollResp.getHits().getHits().length > 1){
                        System.out.println(doc.getDocno() +"Already present");
                        continue;
                    }
                    else if(scrollResp.getHits().getHits().length == 1){
                        isSourceHtmlPresent = true;
                        SearchHit hit = scrollResp.getHits().getHits()[0];

                        Map t=hit.getSource();

                        List tempList = (List) hit.getSource().get("in_links");
                        if(tempList!=null){
                            doc.getInlinks().addAll(tempList);
                        }
                        tempList = (List) hit.getSource().get("out_links");
                        if(tempList!=null){
                            doc.getOutLinks().addAll(tempList);
                        }
                        tempList = (List) hit.getSource().get("author");
                        if(tempList!=null){
                            doc.getAuthors().addAll(tempList);
                        }
                        doc.setRawData(hit.getSource().get("html_Source").toString());
//                        doc.setHttpHeader(hit.getSource().get("HTTPheader").toString());
                    }
                    // NOW Index the updated document!
                    try{
                        if(!isSourceHtmlPresent){
                            updateHtmlForDoc(doc);
                        }
                        IndexResponse indexResponse = transportClient.prepareIndex(indexName, indexType)
                                .setId(doc.getDocno())
                                .setSource(gson.toJson(doc)).get();

                    }catch(Exception e){
                        e.printStackTrace();
                        System.out.println("No indexing"+doc.getDocno());
                    }

                }
            }
            docsIndexed+= listOfDoc.size();
            System.out.println("Files index" + docsIndexed);
        }

    private void updateHtmlForDoc(Doc doc) {
//        HttpGet request = new HttpGet(doc.getUrl());
//        HttpResponse response;
//        try {
//            response = httpClient.execute(request);
//            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
//                StringBuilder headerStringBuilder = new StringBuilder();
//                Header[] headers = response.getAllHeaders();
//                for (Header header : headers) {
//                    headerStringBuilder.append(header.toString());
//                }
//                doc.setHttpHeader(headerStringBuilder.toString());
//                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//                StringBuilder htmlSourceCode = new StringBuilder();
//                String line = "";
//                while ((line = rd.readLine()) != null) {
//                    htmlSourceCode.append(line);
//                }
//                doc.setRawData(htmlSourceCode.toString());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    private List<Doc> parseFile(File f) throws IOException {
        Document document = Jsoup.parse(f, "UTF-8");
        Elements docs = document.getElementsByTag("DOC");
        List<Doc> listOfDocs = new ArrayList<Doc>();

        for (Element element : docs) {
            Doc createdDoc = createDocument(element);
            if (createdDoc != null) {
                listOfDocs.add(createdDoc);
            }
        }

        return listOfDocs;
    }

    private Doc createDocument(Element element) {
        Doc doc = new Doc();
        String[] tags = {"DOCNO","TEXT","FRONTIER","URL","OUTLINKS","HEAD", "RAWDATA"};
        for(String tag : tags){
            Elements eleTag = element.getElementsByTag(tag);

            for (Element anEleTag : eleTag) {
                String textValue = anEleTag.text();
                if (tag.equals("DOCNO")) {
                    doc.setDocno(textValue);
                } else if (tag.equals("FRONTIER")) {
                    doc.setFrontier(Integer.valueOf(textValue));
                } else if (tag.equals("URL")) {
                    doc.setUrl(textValue);
                } else if (tag.equals("OUTLINKS")) {
                    doc.setOutLinks(getOutlinks(textValue));
                } else if (tag.equals("HEAD")) {
                    doc.setTitle(textValue);
                } else if (tag.equals("TEXT")) {
                    doc.setText(textValue);
                }
                else if (tag.equals("RAWDATA")){
                    doc.setRawData(textValue);
                }
                Set<String> tempInlinkSet = getInlinksForDocument(doc.getDocno());
                if(null!=tempInlinkSet)
                    doc.getInlinks().addAll(tempInlinkSet);

                doc.getAuthors().add("Shenoy");
//                return doc;

            }
        }

        return doc;
    }

    private Set<String> getInlinksForDocument(String docno) {
        return inlinksMap.get(docno);
    }

    private Set<String> getOutlinks(String textValue) {
        if(textValue == null || textValue.isEmpty()){
            return null;
        }
        String[] outlinkSet = textValue.split(" ");
        Set<String> tempOutlinkSet = new LinkedHashSet<String>();
        for (String outlink : outlinkSet) {
            tempOutlinkSet.add(outlink.trim());
        }
        return tempOutlinkSet;
    }
}
