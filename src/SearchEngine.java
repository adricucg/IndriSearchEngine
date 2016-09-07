import java.util.Arrays;
import java.util.List;

public class SearchEngine {

    public static void main(String[] args) {

        String usage = "java -jar SearchEngine \n\n"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update] \n"
                + " [-index INDEX_PATH] [-search QUERY] \n"
                + " [-preprocess DOCS_PATH] [-forum FORUM_TYPE] [-output OUTPUT_PATH] \n"
                + " [-prior] [-index INDEX_PATH] [-output OUTPUT_PATH] \n\n"
                + " -index indexes the threads in DOCS_PATH, creating or updating an Indri index \n"
                + " -search performs a search of the QUERY terms in an Indri index found in the INDEX_PATH \n"
                + " -preprocess processes the html pages in DOCS_PATH with -forum specifying the forum type: tripadvisor or stackoverflow \n"
                + " -prior generates an saves priors to the OUTPUT_PATH based on information stored in the INDEX_PATH \n";

        boolean invalid = true;
        String indexPath = null;
        String docsPath = null;
        boolean create = true;
        String query = null;
        String forumType = null;
        String preProcessorPath = null;
        String outputPath = null;
        boolean prior = false;

        for(int i=0;i<args.length;i++) {
            if ("-index".equals(args[i])) {
                indexPath = args[i+1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i+1];
                i++;
            } else if ("-update".equals(args[i])) {
                create = false;
            } else if ("-search".equals(args[i])) {
                query = Arrays.toString(args);
                StringBuilder builder = new StringBuilder();
                for(int j = i+1; j < args.length; j++) {
                    builder.append(args[j]);

                    if(j != args.length - 1) {
                        builder.append(" ");
                    }
                }

                query = builder.toString();
                i++;
            } else if ("-forum".equals(args[i])) {
                forumType = args[i+1];
                i++;
            } else if ("-preprocess".equals(args[i])) {
                preProcessorPath = args[i+1];
                i++;
            } else if ("-output".equals(args[i])) {
                outputPath = args[i+1];
                i++;
            } else if ("-prior".equals(args[i])) {
                prior = true;
            }
        }

        // indexing
        if(indexPath!= null && docsPath != null){
            IndexThread index = new IndexThread();
            index.indexDirectory(indexPath, docsPath, forumType, create);

            invalid = false;
        }

        // querying
        if(indexPath != null && query != null){

            System.out.println("Searching relevant threads for query: " + query);

            SearchThread searchThread = new SearchThread();
            List<QueryResult> results = searchThread.searchIndex(indexPath, query);
            if(results != null && results.size() > 0) {
                for (QueryResult queryResult : results) {
                    System.out.println(queryResult.toString());
                }
            } else {
                System.out.println("No results found");
            }

            invalid = false;
        }

        // html pages pre-processing
        if(preProcessorPath != null && forumType != null && outputPath != null) {

            System.out.println("Pre-processing the following html pages found at: " + preProcessorPath);

            OnlineThreadPreProcessor processor = new OnlineThreadPreProcessor(forumType);
            List<OnlineThread> threads = processor.processDocumentCollection(preProcessorPath);

            for(OnlineThread thread : threads){
                System.out.println(thread.getTitle());
            }

            processor.saveThreadsAsXml(threads, outputPath);

            System.out.println("Finished");

            invalid = false;
        }

        // generating priors
        if(prior && indexPath != null && outputPath != null) {

            System.out.println("Generating priors for index: " + indexPath);

            OnlineThreadPreProcessor processor = new OnlineThreadPreProcessor("");
            processor.extractPriors(indexPath, outputPath);

            System.out.println("Saved priors to priors.txt to: " + outputPath);

            invalid = false;
        }

        if(invalid) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }
    }
}

