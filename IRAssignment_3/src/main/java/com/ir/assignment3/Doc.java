package com.ir.assignment3;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ps on 6/16/17.
 */
public class Doc {
    @SerializedName("docno")        //
    private String docno;

    @SerializedName("text")         //
    private String text;

    @SerializedName("url")          //
    private String url;

    @SerializedName("out_links")    //
    private Set<String> outLinks = new HashSet<String>();

    @SerializedName("title")        //
    private String title;

    @SerializedName("depth")       //
    private Integer frontier;

    @SerializedName("html_Source")  //
    private String rawData;

    @SerializedName("in_links")     //
    private Set<String> inlinks = new HashSet<String>();

    public void setInlinks(Set<String> inlinks) {
        this.inlinks = inlinks;
    }

    public void setAuthors(Set<String> authors) {
        this.authors = authors;
    }

    public String getHttpHeader() {
        return httpHeader;
    }

    @SerializedName("author")       //
    private Set<String> authors = new HashSet<String>();

//    @SerializedName()      //
//    private String htmlSource;

    @SerializedName("HTTPheader")       //
    private String httpHeader;

    Doc() {
    }

    public String getDocno() {
        return docno;
    }

    public void setDocno(String docno) {
        this.docno = docno;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public Set<String> getOutLinks() {
        return outLinks;
    }

    public void setOutLinks(Set<String> outLinks) {
        this.outLinks = outLinks;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getFrontier() {
        return frontier;
    }

    public void setFrontier(Integer frontier) {
        this.frontier = frontier;
    }


    public static String setString(Doc document) {
        StringBuilder sb = new StringBuilder();
        sb.append("<DOC>")
                .append(System.getProperty("line.separator"));
        sb.append("<DOCNO>")
                .append(document.getDocno())
                .append("</DOCNO>")
                .append(System.getProperty("line.separator"));
        sb.append("<URL>")
                .append(document.getUrl())
                .append("</URL>")
                .append(System.getProperty("line.separator"));
        sb.append("<TEXT>")
                .append(document.getText())
                .append("</TEXT>")
                .append(System.getProperty("line.separator"));
        sb.append("<HEAD>")
                .append(document.getTitle())
                .append("</HEAD>")
                .append(System.getProperty("line.separator"));
        sb.append("<FRONTIER>")
                .append(document.getFrontier())
                .append("</FRONTIER>")
                .append(System.getProperty("line.separator"));
        sb.append("<OUTLINKS>")
                .append(getLinksAsString(document.getOutLinks()))
                .append("</OUTLINKS>")
                .append(System.getProperty("line.separator"));
        sb.append("<RAWDATA>")
                .append(System.getProperty("line.separator"))
                .append(document.getRawData())
                .append("</RAWDATA>")
                .append(System.getProperty("line.separator"));
        sb.append("</DOC>");


        return sb.toString();
    }

    public static String getLinksAsString(Set<String> outLink) {
        StringBuilder builder = new StringBuilder();
        for (String link : outLink) {
            builder.append(link).append("\n");
        }
        return builder.toString().trim();
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public String getRawData() {
        return rawData;
    }

    public static String setOutlink(Doc document) {
        StringBuilder sb = new StringBuilder();
        sb.append("<DOC>")
                .append(System.getProperty("line.separator"));
        sb.append("<DOCNO>")
                .append(document.getDocno())
                .append("</DOCNO>")
                .append(System.getProperty("line.separator"));
        sb.append("<OUTLINKS>")
                .append(getLinksAsString(document.getOutLinks()))
                .append("</OUTLINKS>")
                .append(System.getProperty("line.separator"));
        sb.append("</DOC>");

        return sb.toString();
    }


    public static String setInlink(String key, Set<String> value) {
        StringBuilder sb = new StringBuilder();
        sb.append("<DOC>")
                .append(System.getProperty("line.separator"));
        sb.append("<DOCNO>")
                .append(key)
                .append("</DOCNO>")
                .append(System.getProperty("line.separator"));
        sb.append("<INLINKS>")
                .append(getLinksAsString(value))
                .append("</INLINKS>")
                .append(System.getProperty("line.separator"));
        sb.append("</DOC>");
        return sb.toString();

    }

    public Set<String> getInlinks() {
        return inlinks;
    }

    public Set<String> getAuthors() {
        return authors;
    }

    public void setHttpHeader(String httpHeader) {
        this.httpHeader = httpHeader;
    }
}
