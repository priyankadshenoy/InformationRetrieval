package com.assignment1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by ps on 5/17/17.
 */
public class Indexing {

    private Pattern compilePattern = Pattern.compile("^ap");
    private Properties properties = new Properties();

    void indexFile(File[] listOfFiles) throws IOException {
        for(File file : listOfFiles) {
            if (validateFile(file)) {
                List<Doc> listOfDocs =  parseFile(file);
                Driver.elasticSearchConnection.createIndex(listOfDocs);
            }
        }
        try {
            properties.store(new FileOutputStream("src/main/resources/documentLength.properties"),
                    "DocLength");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Doc> parseFile(File file) throws IOException {

        Document document = Jsoup.parse(file, "UTF-8");
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
        String[] tags = {"DOCNO","TEXT"};
        for(String tag : tags){
            Elements eleTag = element.getElementsByTag(tag);

            for (Element anEleTag : eleTag) {
                String textValue = anEleTag.toString();
                if (tag.equals("DOCNO"))
                    doc.setDOCNO(textValue);
                else if (tag.equals("TEXT"))
                    doc.setTEXT(textValue);
            }
        }
        int docLength = doc.getTEXT().split(" ").length;
        properties.setProperty(doc.getDOCNO(), String.valueOf(docLength));
        return doc;
    }

    private boolean validateFile(File file) {
        return compilePattern.matcher(file.getName()).find();
    }
}
