import lemurproject.indri.*;

import java.util.*;

/**
 * Created by cloudera on 3/13/16.
 */
public class SearchThread {

    public List<QueryResult> searchIndex(String indexPath, String query) {

        try {

            List<QueryResult> queryResults = new ArrayList<QueryResult>();
            QueryEnvironment queryEnvironment = new QueryEnvironment();

            queryEnvironment.setMemory(1024000000);
            queryEnvironment.setStopwords(StopWords.Words);
            queryEnvironment.setScoringRules(new String[] {"method:dirichlet,mu:2000"});

            queryEnvironment.addIndex(indexPath);

            String expandedQuery = buildIndriQuery(query);
            ScoredExtentResult[] hits = queryEnvironment.runQuery(expandedQuery, 10);

            for(int i = 0; i < hits.length; i++) {

                String[] htmlPages = queryEnvironment.documentMetadata(new int[] { hits[i].document }, "htmlpage");
                String[] titles = queryEnvironment.documentMetadata(new int[] { hits[i].document }, "title");
                String[] paths = queryEnvironment.documentMetadata(new int[] { hits[i].document }, "docno");

                queryResults.add(new QueryResult(hits[i].score, titles[0], paths[0], Arrays.asList(htmlPages)));
            }

            return queryResults;
        }
        catch (Exception ex) {

            System.err.println(ex.getMessage());
            return null;
        }
    }

    public String buildIndriQuery(String searchTerm){

        StringTokenizer tokenizer = new StringTokenizer(searchTerm);
        String internalQuery = "";

        while(tokenizer.hasMoreElements()) {
            String queryTerm = tokenizer.nextToken();
            internalQuery = internalQuery +
                    "6.0 " + queryTerm + ".title" +
                    " 1.5 " + queryTerm + ".initposttext" +
                    " 0.5 " + queryTerm + ".initcomment" +
                    " 0.5 " + queryTerm + ".replycomment" +
                    " 1.5 " + queryTerm +".replyposttext ";
        }

        String indriQuery =  "#weight( 1.0 #prior(LENGTH) 1.0 #weight( " + internalQuery + ") )";

        return indriQuery;
    }
}
