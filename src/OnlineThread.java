import javax.xml.bind.annotation.*;
import java.util.*;

/**
 * Created by cloudera on 3/4/16.
 */
@XmlRootElement (name = "OnlineThread")
@XmlType(propOrder={"title" , "initpost", "replypost", "htmlpage", "length"})
@XmlAccessorType(XmlAccessType.FIELD)
public class OnlineThread {

    private String title;
    private InitPost initpost;
    private List<ReplyPost> replypost;
    private List<String> htmlpage;
    private int length;

    @XmlTransient
    private String path;

    public OnlineThread()
    {
        this.replypost = new ArrayList<ReplyPost>();
        this.htmlpage = new ArrayList<String>();
    }

    public OnlineThread(String title, InitPost initpost, List<ReplyPost> replyPosts)
    {
        this();

        this.title = title;
        this.initpost = initpost;
        this.replypost = replyPosts;
        this.length = replypost == null? 0 : replypost.size();
    }

    public String getTitle()
    {
        return title;
    }

    public String getPath() {return path; }

    public List<ReplyPost> getReplyPosts()
    {
        return replypost;
    }

    public List<String> getCollectionHtmlPages()
    {
        return htmlpage;
    }

    public int getLength()
    {
        length = replypost == null? 0 : replypost.size();

        return length;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
