import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.ParsedDocument;
import lemurproject.indri.Specification;
import lemurproject.indri.TagExtent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by cloudera on 2/7/16.
 */
public class IndexThread {

    public void indexDirectory(String indexPath, String docsPath, String forumType, boolean create) {

        final File docDir = new File(docsPath);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            IndexEnvironment indexEnvironment = new IndexEnvironment();
            indexEnvironment.setStoreDocs(true);

            // setup the parameters for the Indri repository
            indexEnvironment.setMemory(1024000000);
            indexEnvironment.setStemmer("porter");
            indexEnvironment.setStopwords(StopWords.Words);
            indexEnvironment.setNormalization(true);
            indexEnvironment.setIndexedFields(new String[]
                    {
                            "title", "initposttext", "initcomment", "replyposttext", "replycomment", "document"
                    });
            indexEnvironment.setMetadataIndexedFields(
                    new String[] {"docno", "htmlpage", "length", "title"},
                    new String[] {"docno", "htmlpage", "length", "title"});

            indexEnvironment.setOrdinalField("document", true);
            indexEnvironment.setParentalField("document", true);

            Specification specification = indexEnvironment.getFileClassSpec("xml");
            specification.index = new String[]
                    {
                            "title", "initposttext", "initcomment", "replyposttext", "replycomment", "document"
                    };
            specification.metadata = new String[] {"htmlpage", "length", "title"};

            indexEnvironment.addFileClass(specification);

            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                indexEnvironment.create(indexPath);
            } else {
                // Add new documents to an existing index:
                indexEnvironment.open(indexPath);
            }

            indexDocs(indexEnvironment, docDir);
            
            indexEnvironment.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    private void indexDocs(IndexEnvironment indexEnvironment, File file) throws IOException {

        // do not try to index files that cannot be read
        if (file.canRead()) {
            if(file.isDirectory()) {
                String[] files = file.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(indexEnvironment, new File(file, files[i]));
                    }
                }
            } else {

                try {

                    String path = file.getPath();
                    if(path.endsWith("xml")) {

                        indexEnvironment.addFile(path);

                        System.out.println("adding " + file);
                    }
                }

                catch(Exception ex) {

                    System.err.println(ex.getStackTrace());
                }
            }
        }
    }
}
