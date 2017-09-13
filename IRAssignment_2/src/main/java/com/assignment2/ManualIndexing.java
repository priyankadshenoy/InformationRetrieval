package com.assignment2;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by ps on 6/5/17.
 */
public class ManualIndexing {
    private Pattern compilePattern = Pattern.compile("^ap");
    private Properties properties = new Properties();

    Set<String> stopWordsSet = new HashSet<String>();
    Map<String, List<Integer>> termPositionForDocumentMap = new HashMap<String, List<Integer>>();
    private List<Map<String, List<TokenMap>>> listOfIntermediateInvtdIndexes = new LinkedList<Map<String,List<TokenMap>>>();
//    Map<Integer, Str>
    int fileNo = 1;
    File docLen;

    long createDocNo = 0L;

    ManualIndexing(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("stoplist.txt"));
            String stopWord;
            while((stopWord=br.readLine())!=null){
                stopWordsSet.add(stopWord);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void indexFile() throws IOException {
        File[] listOfFile = Driver.folder_path.listFiles();
        ArrayList<Doc> docsForIndexing = new ArrayList<Doc>();
        for(File eachFile : listOfFile){
            if(validateFile(eachFile)){
                List<Doc> totalDocsEachFile = parseFile(eachFile);
                // not loading entire data into memory
                if(docsForIndexing.size() + totalDocsEachFile.size() > 5000){
                    tokenizationOfDocuments(docsForIndexing);
                    docsForIndexing.clear();
                    docsForIndexing.addAll(totalDocsEachFile);
                }
                else{
                    docsForIndexing.addAll(totalDocsEachFile);
                }
            }
        }
        properties.store(new FileOutputStream("src/main/resources/documentLength.properties"),
                "doc len");
        tokenizationOfDocuments(docsForIndexing);
    }

    private void tokenizationOfDocuments(ArrayList<Doc> docsForIndexing) {
        //System.out.print("Tokenization");
        Map<String,List<TokenMap>> tempInvertedTokens = new HashMap<String,List<TokenMap>>();
        String tokenValues []=null;
        for(Doc docForIndexing : docsForIndexing){
            //cleaning documents
            tokenValues = filterDocument(docForIndexing.getTEXT().trim().toLowerCase());
            //get token and its frequency
            Map<String, Integer> tokenAndFrequency = processTokenFrequency(tokenValues);

            // Creating inverted token list with term and freq
            for (Map.Entry<String, Integer> tf : tokenAndFrequency.entrySet()) {
                TokenMap tokenMap = new TokenMap(/*Driver.stringToLongMap.get*/(docForIndexing.getDOCNO()),
                        tf.getValue(), null, getPositionOfTerm(tf.getKey()));
                if (tempInvertedTokens.containsKey(tf.getKey())) {
                    tempInvertedTokens.get(tf.getKey()).add(tokenMap);
                } else {
                    List<TokenMap> temp = new ArrayList<TokenMap>();
                    temp.add(tokenMap);
                    tempInvertedTokens.put(tf.getKey(), temp);
                }
            }

//            for (Map.Entry<String, List<TokenMap>> entry : tempInvertedTokens.entrySet())
//            {
//                System.out.println(entry.getKey() + " key");
//                for(TokenMap t: entry.getValue()){
//                    System.out.println(t.getDocId()+" Doc id \n"+
//                            t.getPositions()+ " positions \n" +
//                            t.getTerm() +" term \n" +
//                            t.getCount() + " count \n");
//                }
//            }
        }

        storeTokens(tempInvertedTokens,fileNo);
        generateOffsetFile(fileNo);
        try {
            mergeIndexes(fileNo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ++fileNo;
    }

    private void mergeIndexes(int fileNo) throws  Exception{
        // interim files
        final File intermediateInvertedIndexFile = new File("result/intermediate_index_"+ fileNo +".txt");
        final File intermediateCatalogFile = new File("result/intermediate_catalog_"+ fileNo +".txt");
        // temp files
        final File tempFinalIndexFile = new File("result/tempFinalIndexFile.txt");
        final File tempCatalogFile  = new File("result/tempCatalogFile.txt");
        // final files
        final File finalInvertedIndexFile = new File("result/FinalInvertedIndex.txt");
        final File finalCatalogFile = new File("result/FinalCatalogFile.txt");


        if(fileNo == 1){
            intermediateInvertedIndexFile.renameTo(finalInvertedIndexFile);
            intermediateCatalogFile.renameTo(finalCatalogFile);
            return;
        }

        // using random access file directly moves pointer to specified position
        RandomAccessFile raIntermediateIndexFile = new RandomAccessFile(intermediateInvertedIndexFile, "r");
        RandomAccessFile raFinalIndexFile = new RandomAccessFile(finalInvertedIndexFile, "r");
        BufferedWriter bfw = new BufferedWriter(new FileWriter(tempFinalIndexFile));

        // Load the catalog files into memory
        Map<String,Long> intermediateCatalogMap = loadInterimCatalogFile(intermediateCatalogFile);
        Map<String,Long> finalCatalogMap = 	loadInterimCatalogFile(finalCatalogFile);

        for (Map.Entry<String, Long> tempEntry : intermediateCatalogMap.entrySet()) {
            raIntermediateIndexFile.seek(tempEntry.getValue());
            List<TokenMap> tokenMapList = getTokenMapList(raIntermediateIndexFile.readLine());

            if (finalCatalogMap.containsKey(tempEntry.getKey())) {
                long value = finalCatalogMap.get(tempEntry.getKey());
                raFinalIndexFile.seek(value);
                tokenMapList.addAll(getTokenMapList(raFinalIndexFile.readLine()));
                finalCatalogMap.remove(tempEntry.getKey());
            }
            Sort.mergeSort(tokenMapList, 0, tokenMapList.size() - 1);
            bfw.write(tempEntry.getKey().trim() +
                    "=" +
                    getStringFromTokenMapList(tokenMapList) +
                    System.getProperty("line.separator"));
        }

        for (Map.Entry<String, Long> tempEntry : finalCatalogMap.entrySet()) {
            long value = finalCatalogMap.get(tempEntry.getKey());
            raFinalIndexFile.seek(value);
            List<TokenMap> tokenDescList = new ArrayList<TokenMap>();
            tokenDescList.addAll(getTokenMapList(raFinalIndexFile.readLine()));
            Sort.mergeSort(tokenDescList, 0, tokenDescList.size() - 1);
            bfw.write(tempEntry.getKey().trim() +
                    "=" +
                    getStringFromTokenMapList(tokenDescList) +
                    System.getProperty("line.separator"));
        }

        bfw.flush();
        bfw.close();

        raFinalIndexFile.close();
        raIntermediateIndexFile.close();
        generateOffsetFileFinal(tempFinalIndexFile,tempCatalogFile);
        tempFinalIndexFile.renameTo(finalInvertedIndexFile);
        tempCatalogFile.renameTo(finalCatalogFile);
    }

    private void generateOffsetFileFinal(File tempFinalIndexFile, File tempCatalogFile) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(tempCatalogFile));
            BufferedReader br = new BufferedReader(new FileReader(tempFinalIndexFile));
            String term;
            long offSet = 0;
            while((term =  br.readLine())!=null){
                String[] termStringTokens = term.split("=");
                bw.write(termStringTokens[0]+
                        " "+
                        offSet+
                        " "+
                        term.length() +
                        System.getProperty("line.separator"));
                offSet+=term.length()+1;
            }
            bw.flush();
            bw.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStringFromTokenMapList(List<TokenMap> tokenMapList) {
        StringBuilder str = new StringBuilder();
        for (TokenMap token : tokenMapList) {
            str.append(token.getDocId())
                    .append("#")
                    .append(token.getPositions())
                    .append("~");
        }
        return str.toString();
    }


    private List<TokenMap> getTokenMapList(String s) {
        List<TokenMap> temp = new ArrayList<TokenMap>();
        String[] tokenMapList = s.split("=")[1].split("~");
        for (String TokenMap : tokenMapList) {
            String[] tokenData = TokenMap.split("#");
            try{
                temp.add(new TokenMap(/*Long.parseLong*/(tokenData[0]),
                        tokenData[1].split("-").length,
                        null,
                        tokenData[1]));
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return temp;
    }

    private Map<String,Long> loadInterimCatalogFile(File interimCatalogFile) {
        Map<String,Long> tempCatalogMap = new HashMap<String,Long>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(interimCatalogFile));
            String line;
            while((line  = br.readLine())!=null ){
                String[] data = line.split(" ");
                tempCatalogMap.put(data[0], Long.valueOf(data[1]));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
         return tempCatalogMap;
    }

    private void generateOffsetFile(int fileNo) {
        File tempOffset = new File("result/intermediate_catalog_"+ fileNo +".txt");
        File tempIndex = new File("result/intermediate_index_"+ fileNo +".txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(tempIndex));
            BufferedWriter bw = new BufferedWriter(new FileWriter(tempOffset));
            String term;
            long offset = 0;
            while((term =  br.readLine())!=null){
                String[] termStringTokens = term.split("=");
                bw.write(termStringTokens[0]+
                        " "+
                        offset+
                        " "+
                        term.length()+
                        System.getProperty("line.separator"));
                offset+=term.length()+1;
            }
            bw.flush();
            bw.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeTokens(Map<String, List<TokenMap>> tempInvertedTokens, int fileNo) {
        File file = new File("result/intermediate_index_"+ fileNo +".txt");
        FileWriter fw;
        try {
            fw = new FileWriter(file);
            for (Map.Entry<String, List<TokenMap>> tempToken : tempInvertedTokens.entrySet()) {
                fw.write(tempToken.getKey() + "=");
                for (TokenMap TokenMap : tempToken.getValue()) {
                    fw.write(TokenMap.getDocId() +
                            "#" +
                            TokenMap.getPositions() +
                            "~");
                }
                fw.write(System.getProperty("line.separator"));
            }
            fw.flush();
            fw.close();
            System.out.println("Inv index" + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // gets position of term in document along with - separating each location where the term is found
    private String getPositionOfTerm(String key) {
        if(!termPositionForDocumentMap.containsKey(key)){
            throw new IllegalStateException("Function malfunctioning");
        }
        List<Integer> termPos = termPositionForDocumentMap.get(key);
        StringBuilder termPosBuilder = new StringBuilder();
        for (Integer pos : termPos) {
            termPosBuilder.append(pos);
            termPosBuilder.append("-");
        }

        return termPosBuilder.toString().substring(0, termPosBuilder.toString().length()-1);
    }

    // gets stem of word and frequency
    private Map<String,Integer> processTokenFrequency(String[] tokenValues) {
        Map<String,Integer> tokenList = new HashMap<String,Integer>();
        for (String token : tokenValues) {
            token = ScoreCalculator.getStemOfWord(token);
            if(tokenList.containsKey(token)){
                tokenList.put(token, tokenList.get(token)+1);
            }else{
                if(token.length()>0)
                tokenList.put(token,1);
            }
        }
        return tokenList;
    }

    // removes unwanted characters and values
    private String[] filterDocument(String s) {
        StringBuilder temp = new StringBuilder();

        temp.append(s.replaceAll("'s", " is")
                .replaceAll("-", " ")
                //.replaceAll("<text>", "")
                //.replaceAll("</text>", "")
                //.replaceAll("(a-z)*(\\.)+", " ")//added new
                //.replaceAll("[a-z](\\.)[a-z]", " ") // added new
                .replaceAll("[^A-Za-z0-9. ]", ""));



        String[] tokenArray = temp.toString().split(" ");
        for(int i = 0; i < tokenArray.length; i++){
            tokenArray[i] = tokenArray[i].replaceAll("(\\.$|^\\.)", "");
        }

        List<String> tempTokenList = new ArrayList<String>();
        for(String tA: tokenArray){
            if(tA.length() >0){
                tempTokenList.add(tA);
            }
        }

        String [] tokens = new String[tempTokenList.size()];
        tokens=tempTokenList.toArray(tokens);

//        for(String tk : tokens){
//            if(tk.equals("text"))
//                System.out.print("text here");
//        }

        termPositionForDocumentMap.clear();
        int position = 0;

        List<String> cleanTokens = new ArrayList<String>();
        for (String token : tokens) {
            String stemmedToken = null;
            if(stopWordsSet.contains(token.trim())){
                position++;
                continue;
            }else if(token.length() == 1){
                position++;
                continue;
            }else if(token.equals(".")){
                position++;
                continue;
            }else if(token.matches("[\\d]+(\\.)[a-z]+")){// 1.Name, 2.Name
                stemmedToken = ScoreCalculator.getStemOfWord(token.split("\\.")[1].trim());
                try{
                    cleanTokens.add(token.split("\\.")[1].trim());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            else{
                stemmedToken = ScoreCalculator.getStemOfWord(token);
                cleanTokens.add(token);
            }

            if(termPositionForDocumentMap.containsKey(stemmedToken)){
                termPositionForDocumentMap.get(stemmedToken)
                        .add(position - termPositionForDocumentMap
                                .get(stemmedToken)
                                .get(termPositionForDocumentMap.get(stemmedToken).size()-1));
            }else{
                List<Integer> tempList = new ArrayList<Integer>();
                tempList.add(position);
                termPositionForDocumentMap.put(stemmedToken, tempList);
            }
            position++;
        }

        String[] test = new String[cleanTokens.size()];

        return cleanTokens.toArray(test);
    }

    private boolean validateFile(File file) {
        return compilePattern.matcher(file.getName()).find();
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
                {
                    doc.setDOCNO(textValue);
//                    String txt = textValue.substring(10,23);
//                    if(Driver.stringToLongMap.get(txt)==null)
//                    {
//                        Driver.stringToLongMap.put(txt, ++createDocNo);
//                       // Driver.longToStringMap.put(createDocNo, txt);
//                    }
//                    else
//                        System.out.println("blah");
                }
                else if (tag.equals("TEXT"))
                {
                    StringBuilder sb= new StringBuilder();
                    sb.append(textValue.replaceAll("<text>","")
                            .replaceAll("</text>", ""));
                    doc.setTEXT(sb.toString());
                }
            }
        }
        int docLength = doc.getTEXT().split(" ").length;
        properties.setProperty(doc.getDOCNO(), String.valueOf(docLength));
        return doc;
    }

}
