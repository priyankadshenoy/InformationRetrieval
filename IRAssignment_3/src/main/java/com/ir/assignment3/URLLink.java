package com.ir.assignment3;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

/**
 * Created by ps on 6/15/17.
 */

// TODO : 1. use time and in link for prioritize ; cannonicalization for http: https : adding raw data URL
public class URLLink {
    private String seed;
    private String cannonicalizedURL;
    private String authority;
    private long timeOut;
    private int frontier;
    private int validity;
    private long waitingTime;
    private long inlinkCount;

    public URLLink(String seed) throws URISyntaxException {
        this.seed = seed;
        cannonicalizedURL = canonicalizeURL(seed);
        frontier = 0;
        authority = (new URI(seed)).getAuthority();
    }

    private String canonicalizeURL(String seed) {
        StringBuilder sb = new StringBuilder();
        if (!(seed == null || seed.trim().length() == 0)) {
            try {
                URL tempURL = new URL(seed.split("#")[0]);

                if(tempURL.getProtocol().equalsIgnoreCase("http")
                        || tempURL.getProtocol().equalsIgnoreCase("https"))
                    sb=sb.append("http://");
                else
                    sb = sb.append(tempURL.getProtocol().toLowerCase()).append("://");

                sb = sb.append(tempURL.getAuthority().toLowerCase());

                sb.append(tempURL.getPath().replaceAll("//", "/"));

                if(tempURL.getPort() != -1 && tempURL.getProtocol()
                        .equalsIgnoreCase("http") && tempURL.getPort()!=80){
                    sb.append(":").append(tempURL.getPort());
                }else if(tempURL.getPort() != -1 && tempURL.getProtocol()
                        .equalsIgnoreCase("https") && tempURL.getPort()!=443){
                    sb.append(":").append(tempURL.getPort());
                }

            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } else{
            //TODO
        }
        return sb.toString();
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getCannonicalizedURL() {
        return cannonicalizedURL;
    }

    public void setCannonicalizedURL(String cannonicalizedURL) {
        this.cannonicalizedURL = cannonicalizedURL;
    }

    public int getFrontier() {
        return frontier;
    }

    public void setFrontier(int frontier) {
        this.frontier = frontier;
    }

    public String getAuthority() {
        return authority;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public int getValidity() {
        return validity;
    }


    public void setValidity(int validity) {
        this.validity = validity;
    }

    public void setWaitingTime(long waitingTime) {
        this.waitingTime = waitingTime;
    }

    public static String getLinkAsString(Set<String> inlink) {
        StringBuilder sb = new StringBuilder();
        for (String link : inlink) {
            sb.append(link).append(" ");
        }
        return sb.toString().trim();
    }
}
