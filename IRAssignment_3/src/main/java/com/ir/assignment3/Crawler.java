package com.ir.assignment3;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ps on 6/15/17.
 */
public class Crawler {
    static int TOTAL_DOCS_TO_BE_CRAWLED = 100;
    static  int TOTAL_DOCS_PER_FILE = 100;
    Set<String> visitedLinksSet = new HashSet<String>();
    int docCrawledYet = 0;
    int docCount =0;
    int fileCount;
    public static PriorityQue que = null;
    private Map<String, Set<String>> inlinkSet = new HashMap<>();
    BufferedWriter resultFile=null;
    BufferedWriter inlinkFile = null;
    BufferedWriter outlinkFile = null;
    private Map<String,Long> crawlTime = new HashMap<String, Long>();
    int skippedUrl =0;
    public  static  int max_queue_size =0;

    public Crawler(){
        try {
            resultFile = new BufferedWriter(new FileWriter("result_100/output_"+fileCount+".txt"));
            inlinkFile = new BufferedWriter(new FileWriter("result_100/inlink.txt"));
            outlinkFile = new BufferedWriter(new FileWriter("result_100/outlink.txt"));
            que = new PriorityQue(inlinkSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void crawlWeb() {;
        getSeedValues();

        while(que.hasOutlinks()){
            System.out.println("Going for dequeue");
            List<URLLink> linksSet = que.dequeue();
            for (URLLink link : linksSet) {
                if(!visitedLinksSet.contains(link.getCannonicalizedURL())){
                    long currentTime = System.currentTimeMillis();
                    if(crawlTime.containsKey(link.getAuthority())){
                        long sleepTime = currentTime - crawlTime.get(link.getAuthority());
                        if(sleepTime<1000){
                            try {
                                Thread.sleep(sleepTime);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    crawlTime.put(link.getAuthority(),currentTime);
                    crawlCleanedLink(link);
                }
            }
        }
    }

    public void getSeedValues() {
        String seed0 = "http://en.wikipedia.org/wiki/List_of_maritime_disasters";
        String seed1 = "http://en.wikipedia.org/wiki/2013_Lampedusa_migrant_shipwreck";
//        String seed4 = "http://www.maritimeherald.com/category/shipping-accidents";
//        String seed5 = "https://www.google.com/search?client=safari&rls=en&q=Lampedusa+migrant+shipwreck&ie=UTF-8&oe=UTF-8#q=Lampedusa+migrant+shipwreck&safe=off&rls=en&tbm=nws";
        try {
            URLLink link1 = new URLLink(seed0);
            URLLink link2 = new URLLink(seed1);
           // URLLink link3 = new URLLink(seed4);

//        URLLink link4 = new URLLink(seed5);
//        link4.setCannonicalizedURL("https://www.google.com/search?client=safari&rls=en&q=Lampedusa+migrant+shipwreck&ie=UTF-8&oe=UTF-8#q=Lampedusa+migrant+shipwreck&safe=off&rls=en&tbm=nws");

        crawlCleanedLink(link1);
        crawlCleanedLink(link2);
      //  crawlCleanedLink(link3);
//        crawlCleanedLink(link4);
        }
        catch (URISyntaxException e){
            e.printStackTrace();
        }
    }

    private void crawlCleanedLink(URLLink link) {
        cleanDOM(link.getSeed(), link.getCannonicalizedURL(), link.getFrontier());
        visitedLinksSet.add(link.getCannonicalizedURL());
    }

    private void cleanDOM(String seed, String cannonicalizedURL, int frontier) {
        try {

            Document docu = null;
            try {
                docu = Jsoup.connect(seed).get();
            } catch (HttpStatusException ht) {
                System.out.println("HTTP Exception");
                skippedUrl++;
                return;
            } catch (IOException io) {
                System.out.println(io + "Invalid URL");
                skippedUrl++;
                //io.printStackTrace();
                return;
            } catch (IllegalArgumentException ie) {
                System.out.print(ie + "Argument issue");
                skippedUrl++;
                return;
            } catch (Exception e) {
                System.out.println(e + "generic exception");
            }

            assert docu != null;
            String v = docu.select("html").first().attr("lang");
            if (!(v.equalsIgnoreCase("en"))) {
                skippedUrl++;
                return;
            }

            Document doc = cleanDocument(docu);
            String sbToString = null;

            try {
                sbToString = doc.body().text().toLowerCase();
            } catch (Exception e) {
                System.out.print(e + "issue with string");
                skippedUrl++;
                return;
            }
            int validDoc = 0;

            Pattern pattern1 = Pattern.compile("\\bsink|sinking|sank|sunk\\b", java.util.regex.Pattern.CASE_INSENSITIVE);
            Matcher matcher1 = pattern1.matcher(sbToString);
//            Pattern pattern2 = Pattern.compile("\\bship\\b");
//            Matcher matcher2 = pattern2.matcher(sbToString);
            Pattern pattern3 = Pattern.compile("\\bdeath|died|loss|injured\\b");
            Matcher matcher3 = pattern3.matcher(sbToString);


            if (sbToString.contains(" war"))
                validDoc++;
            if (matcher1.find())
                validDoc += 5;
            if (sbToString.contains("maritime"))
                validDoc += 10;
//            if(matcher2.find())
//                validDoc++;
            if (sbToString.contains("disaster"))
                validDoc += 10;
            if (matcher3.find())
                validDoc++;
            if (sbToString.contains(" ship") || sbToString.contains(" ships"))
                validDoc++;

            if (sbToString.contains("lampedusa"))
                validDoc += 10;

            if (validDoc <= 10)
            {
                skippedUrl++;
                return;
            }


            Elements ele = doc.getElementsByTag("a");
            Set<String> tempCanonURL = new HashSet<>();
            Set<String> tempOutLinkSet = new HashSet<>();
            for (Element e : ele) {
                URLLink link = null;
                try {
                    link = new URLLink(e.attr("abs:href"));
                } catch (URISyntaxException ex) {
                    System.out.println(ex + "\n");
                    continue;
                } catch (NullPointerException ne) {
                    System.out.println(ne + "\n");
                    continue;
                }
                if (tempCanonURL.contains(link.getCannonicalizedURL())) {
                    continue;
                } else {
                    if (!(link.getCannonicalizedURL().trim().length() == 0))
                        tempCanonURL.add(link.getCannonicalizedURL());
                }

                if (validateLink(link) && link.getCannonicalizedURL().trim().length() > 0) {
                    link.setFrontier(frontier + 1);
                    link.setValidity(validDoc);



                    //System.out.println(max_queue_size + "max");
                    if(PriorityQue.queueSize<= 1000000)
                    {
                        que.enqueue(link);
                    }



                    tempOutLinkSet.add(link.getCannonicalizedURL());

                    if (inlinkSet.containsKey(link.getCannonicalizedURL())) {
                        inlinkSet.get(link.getCannonicalizedURL()).add(cannonicalizedURL);
                    } else {
                        Set<String> tempSet = new HashSet<String>();
                        tempSet.add(cannonicalizedURL);
                        inlinkSet.put(link.getCannonicalizedURL(), tempSet);
                    }
                }
            }
            System.out.println("List of outlinks\t" + tempOutLinkSet.size());

            Doc document = new Doc();
            document.setDocno(cannonicalizedURL);
            document.setFrontier(frontier);
            document.setOutLinks(tempOutLinkSet);
            document.setText(doc.body().text());
            document.setUrl(seed);
            document.setTitle(doc.title());
            document.setRawData(docu.html());
            writeToFile(document);
            System.out.println("Documents crawled yet\t" + docCrawledYet +"\tskipped URl\t" + skippedUrl);
        }
        catch (Exception e){
            System.out.println(e+"God knows why");
        }
    }

    private Document cleanDocument(Document doc) {
        if(doc.select("div#mw-head").first() != null)
            doc.select("div#mw-head").first().remove();
        if(doc.select("div#toc").first() != null)
            doc.select("div#toc").first().remove();
        if(doc.select("div#mw-panel").first() != null)
            doc.select("div#mw-panel").first().remove();
        if(doc.select("div#catlinks").first() != null)
            doc.select("div#catlinks").first().remove();
        if(doc.select("div#footer").first() != null)
            doc.select("div#footer").first().remove();
        return doc;
    }

    private void writeToFile(Doc document) {
        try {
            if (docCount == TOTAL_DOCS_PER_FILE) {
                ++fileCount;
                resultFile.flush();
                resultFile.close();
                resultFile = new BufferedWriter(new FileWriter("result_100/output_" + fileCount + ".txt"));
                docCount = 0;
            }
            resultFile.write(Doc.setString(document));
            resultFile.write(System.lineSeparator());
            ++docCount;
            ++docCrawledYet;

            outlinkFile.write(Doc.setOutlink(document));

            if (docCrawledYet == TOTAL_DOCS_TO_BE_CRAWLED) {
//                inlinkFile.write(Doc.setInlink(document));
                writeInlinkFile();
                stop();
               // System.exit(1);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    private void writeInlinkFile() throws Exception {
        for (Map.Entry<String, Set<String>> entry : inlinkSet.entrySet()) {
            inlinkFile.write(Doc.setInlink(entry.getKey(), entry.getValue()));
//            inlinkFile.write(entry.getKey() + "=" +System.getProperty("line.separator")
//                    + URLLink.getLinkAsString(entry.getValue()) +
//            System.getProperty("line.separator"));
        }
    }

    private boolean validateLink(URLLink link) {
        String cannonicalizedURL = link.getCannonicalizedURL();
        return !visitedLinksSet.contains(cannonicalizedURL) &&
                  !CustomBot.unwantedURL(cannonicalizedURL);
        //&& CustomBot.canBeCrawled(cannonicalizedURL);
    }

    void stop() {
        try {
            resultFile.flush();
            resultFile.close();
            outlinkFile.flush();
            outlinkFile.close();
            inlinkFile.flush();
            inlinkFile.close();
            System.exit(1);
        }
    catch (Exception e){
            e.printStackTrace();
        }
    }
}
