import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Created by cloudera on 3/21/16.
 */
@XmlRootElement(name = "ReplyPost")
@XmlType(propOrder={"replyposttext" , "replycomment"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ReplyPost {

    private String replyposttext;
    private List<String> replycomment;

    public String getPostText() {
        return this.replyposttext;
    }

    public void setPostText(String posttext){
        this.replyposttext = posttext;
    }

    public List<String> getComments(){
        return this.replycomment;
    }

    public void setComments(List<String> comments) {
        this.replycomment = comments;
    }
}
