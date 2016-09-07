import lemurproject.indri.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.io.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Created by ac318 on 3/4/16.
 */
public class OnlineThreadPreProcessor {

    private String initPostClass;
    private String replyPostClass;
    private String initPostAttributeType;
    private String replyPostAttributeType;
    private int index;

    public OnlineThreadPreProcessor(String dataSetType) {

        if(dataSetType.equalsIgnoreCase("tripadvisor")) {
            initPostClass ="postBody";
            replyPostClass="postBody";
            initPostAttributeType = "class";
            replyPostAttributeType = "class";
            index = 1;
        }

        if(dataSetType.equalsIgnoreCase("stackoverflow")) {
            initPostClass ="question";
            replyPostClass="answer-";
            initPostAttributeType = "id";
            replyPostAttributeType = "id";
            index = 0;
        }
    }

    public List<OnlineThread> processDocumentCollection(String folderPath) {
        HashMap<String, OnlineThread> threadsMap = new HashMap<String, OnlineThread>();

        final File folder = new File(folderPath);

        if(folder.exists() && folder.isDirectory() && folder.canRead()) {
            processFilesForFolder(folder, threadsMap);
        } else {
            System.out.println("Document directory '" + folder.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        return new ArrayList<OnlineThread>(threadsMap.values());
    }

    public void processFilesForFolder(File folder, HashMap<String, OnlineThread> threadsMap) {

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                processFilesForFolder(fileEntry, threadsMap);
            } else {
                try {
                    Document document = Jsoup.parse(fileEntry, "UTF-8");

                    String title = document.title();

                    System.out.println("Processing file: " + title);

                    List<ReplyPost> replyPosts = extractReplyPosts(document);

                    if(threadsMap.containsKey(title)) {

                        OnlineThread existingThread = threadsMap.get(title);

                        //add all the replies and this new html page reference to the existing thread
                        if(index == 0) {
                            // for stackoverflow pages add the title extension
                            existingThread.getCollectionHtmlPages().add(fileEntry.getAbsolutePath() + "/" + title);
                        } else {
                            existingThread.getCollectionHtmlPages().add(fileEntry.getAbsolutePath());
                        }

                        existingThread.getReplyPosts().addAll(replyPosts);

                    } else {
                        //thread does not exist so we create a new one
                        InitPost initPost = extractInitPost(document);

                        OnlineThread newThread = new OnlineThread(title, initPost, replyPosts);

                        if(index == 0) {
                            newThread.getCollectionHtmlPages().add(fileEntry.getAbsolutePath() + "/" + title);
                        } else {
                            newThread.getCollectionHtmlPages().add(fileEntry.getAbsolutePath());
                        }


                        threadsMap.put(title, newThread);
                    }

                } catch (FileNotFoundException ex) {
                    // checking if the file is there
                    return;
                } catch (IOException ex) {
                    // checking if the file content can be read
                    return;
                }
            }
        }
    }

    public void extractPriors(String indexPath, String outputPath) {

        try {

            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath + "/priors.txt"), "utf-8"));

            ArrayList<String> threadTermsCount = new ArrayList<String>();
            Set<String> threadDistinctTermsCount = new HashSet<String>();

            QueryEnvironment queryEnvironment = new QueryEnvironment();
            queryEnvironment.addIndex(indexPath);

            long termsCount = queryEnvironment.termCount();
            long uniqueTermsCount = queryEnvironment.termCountUnique();

            int docCount = (int)queryEnvironment.documentCount();
            int[] docIds = new int[docCount];
            for(int i=0; i < docCount; i++) {
                docIds[i] = i + 1;
            }

            String[] threadsLengths = queryEnvironment.documentMetadata(docIds, "length");
            int maxLength = getMaxLength(threadsLengths);

            DocumentVector[] docVectors = queryEnvironment.documentVectors(docIds);
            for(int j=0; j < docVectors.length; j++) {

                threadTermsCount.clear();
                threadDistinctTermsCount.clear();

                DocumentVector docVector = docVectors[j];
                int threadLength = Integer.parseInt(threadsLengths[j]);

                for(int i=0; i< docVector.positions.length; i++) {
                    int position = docVector.positions[i];
                    threadTermsCount.add(docVector.stems[position]);
                    threadDistinctTermsCount.add(docVector.stems[position]);
                }

                double prior = calculatePrior(
                        maxLength, (int)termsCount, (int)uniqueTermsCount, threadLength,
                        threadTermsCount.size(), threadDistinctTermsCount.size());

                writer.write(outputPath + "/thread" + (j+1) + ".xml" + " " + Math.log(prior) + "\r\n");
            }

            writer.close();

        }
        catch (Exception ex) {

            System.err.println(ex.getMessage());
        }
    }

    public void saveThreadsAsXml(List<OnlineThread> threads, String path){

        if(threads == null || threads.size() == 0)
        {
            return;
        }

        Marshaller xmlMarshaller = null;
        try {
            JAXBContext jContext = JAXBContext.newInstance(OnlineThread.class);
            xmlMarshaller = jContext.createMarshaller();
            xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            xmlMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);

        }catch (JAXBException ex) {
            System.err.println("Unable to create the xml serialization context '" + ex.getMessage());
        }

        int i = 1;
        for(OnlineThread thread : threads){

            try{
                File threadOutputFile = new File(path + "/thread" + i + ".xml");
                if(threadOutputFile.createNewFile() && xmlMarshaller != null) {
                    xmlMarshaller.marshal(thread, threadOutputFile);
                }

                i++;

            } catch (IOException ex) {
                System.err.println("File can't be created in the designated path '" + path);
                return;

            } catch (JAXBException ex) {
                System.err.println("Unable to serialize the thread content '" + thread.getTitle());
                return;
            }
        }
    }

    private InitPost extractInitPost(Document document) {

        Elements allElements = document.getElementsByAttributeValue(initPostAttributeType, initPostClass);
        if(allElements != null && allElements.size() > 0) {

            InitPost initPost = new InitPost();

            Element initPostElement = allElements.first();
            Elements commentsElements = initPostElement.getElementsByAttributeValue("class", "comment ");

            List<String> comments = getComments(commentsElements);
            String postText = initPostElement.getElementsByTag("p").text();

            initPost.setPostText(postText);
            initPost.setComments(comments);

            return initPost;
        }

        return null;
    }

    private List<ReplyPost> extractReplyPosts(Document document) {

        Elements allElements = document.getElementsByAttributeValueStarting(replyPostAttributeType, replyPostClass);
        List<ReplyPost> replyPosts = new ArrayList<ReplyPost>();

        if(allElements != null && allElements.size() > 0) {
            for(int i=index; i < allElements.size(); i++) {

                ReplyPost replyPost = new ReplyPost();

                Element replyElement = allElements.get(i);
                Elements commentsElements = replyElement.getElementsByAttributeValue("class", "comment ");

                List<String> comments = getComments(commentsElements);
                String postText = replyElement.getElementsByTag("p").text();
                //String reputationScore = initPostElement.getElementsByAttributeValue("class", "reputation-score").first().text();

                replyPost.setComments(comments);
                replyPost.setPostText(postText);

                replyPosts.add(replyPost);
            }
        }

        return replyPosts;
    }

    private List<String> getComments(Elements commentsElements) {

        List<String> comments = new ArrayList<String>();
        for(int j = 0; j < commentsElements.size(); j++) {

            String vote = commentsElements.get(j)
                    .getElementsByAttributeValue("class", " comment-score")
                    .first().getElementsByTag("span")
                    .text();

            if (vote != null && !vote.isEmpty()) {
                int noOfVotes = Integer.parseInt(vote);

                // ignore comments with 0 votes
                if (noOfVotes > 0) {

                    String commentText = commentsElements.get(j)
                            .getElementsByAttributeValue("class", "comment-copy")
                            .first()
                            .text();

                    comments.add(commentText);
                }
            }
        }

        return comments;
    }

    private double calculatePrior(
            int maxLength, int totalTermsCount, int totalDistinctTermsCount,
            int threadLength, int threadTermsCount, int threadDistTermsCount)
    {

        double lambda1 = 0.50;
        double lambda2 = 0.25;
        double lambda3 = 0.25;

        double prior = lambda1 * ((double)threadLength/(double)maxLength)
                + lambda2 * ((double)threadTermsCount/(double)totalTermsCount)
                + lambda3 * ((double)threadDistTermsCount/(double)totalDistinctTermsCount);

        return prior;
    }

    private int getMaxLength(String[] threadsLengths) {
        int maxLength = 0;
        for(int k = 0; k < threadsLengths.length; k++) {
            int length = Integer.parseInt(threadsLengths[k]);
            if(length > maxLength) {
                maxLength = length;
            }
        }
        return maxLength;
    }
}
