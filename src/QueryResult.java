import java.util.List;

/**
 * Created by cloudera on 3/23/16.
 */
public class QueryResult {

    private double score;
    private String title;
    private String path;
    private List<String> pages;

    public QueryResult(double score, String title, String path, List<String> pages) {
        this.score = score;
        this.title = title;
        this.path = path;
        this.pages = pages;
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        for(String page : pages) {
            builder.append(page);
        }
        return "Thread: " + title + "\n"
                + "path: " + path + "\n"
                + "score: " + score + "\n"
                + "html pages: " + "\n"
                + builder.toString() + "\n"
                + "...................................................." + "\n";
    }
}
