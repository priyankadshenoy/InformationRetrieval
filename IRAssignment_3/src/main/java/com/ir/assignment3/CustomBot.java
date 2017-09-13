package com.ir.assignment3;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ps on 6/18/17.
 */
public class CustomBot {
    static Map<String,Boolean> crawledPage = new HashMap<String,Boolean>();
    static String[] unwantedData = {
            ".jpg",
            ".jpeg",
            ".png",
            "facebook",
            "instagram",
            "twitter",
            "shop",
            "wikimedia",
            "ads",
            "foursquare",
            "mediawiki",
            "aenetworks",
            "contact_us",
            "license",
            "plus.google.com",
            "fyi.tv",
            "email",
            "support",
            "emails",
            "wiki/special:",
            "portal:featured_content",
            "portal:current_events",
            "special:random",
            "help:contents",
            "wikipedia:about",
            "wikipedia:community_portal",
            "special:recentchanges",
            "wikipedia:file_upload_wizard",
            "special",
            "wikipedia:general_disclaimer",
            "en.m.",
            "action=edit",
            "Help:Category",
            "international_standard_book_number",
            ".pdf",
            "file:",
            "youtube",
            "\\.tv",
            "mylifetime",
            "intellectualproperty",
            "integrated_authority",
            "citation",
            ".php",
            ".asp",
            ".aspx"};



//    static boolean canBeCrawled(String url){
//        URL tempUrl = null;
//        try{
//            tempUrl = new URL(url.trim());
//            if(crawledPage.containsKey(tempUrl.getAuthority())){
//                return crawledPage.get(tempUrl.getAuthority());
//            }
//
//            StringBuilder sb = new StringBuilder();
//            sb.append(tempUrl.getProtocol())
//                    .append("://")
//                    .append(tempUrl.getAuthority())
//                    .append("/")
//                    .append("robots.txt");
//
//
//            if(Jsoup.connect(sb.toString()).get().body().text().contains("User-agent: * Disallow: /")){
//                crawledPage.put(tempUrl.getAuthority(), false);
//                return false;
//            }
//
//        }catch(Exception e){
//            if(tempUrl!= null){
//                crawledPage.put(tempUrl.getAuthority(), true);
//            }
//            return true;
//        }
//        crawledPage.put(tempUrl.getAuthority(), true);
//        return true;
//    }

    static boolean unwantedURL(String url){
        for(String a: unwantedData) {
            if(url.toLowerCase().contains(a))
            {
                return true;
            }
        }
        return false;
    }
}
