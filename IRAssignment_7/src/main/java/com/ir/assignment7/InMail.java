package com.ir.assignment7;

/**
 * Created by ps on 8/4/17.
 */
public class InMail {
    private transient String id;
    private String text;
    private String label;
    private String fileName;
    private String split;

    public InMail(String id, String text, String label, String fileName, String split) {
        super();
        this.id = id;
        this.text = text;
        this.label = label;
        this.fileName = fileName;
        this.split = split;
    }

    public String getId() {
        return id;
    }
    public String getText() {
        return text;
    }
    public String getSpamLabel() {
        return label;
    }
    public String getFileName() {
        return fileName;
    }
    public String getSplit() {
        return split;
    }
}
