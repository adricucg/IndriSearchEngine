import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Created by cloudera on 3/18/16.
 */
@XmlRootElement(name = "Post")
@XmlType(propOrder={"initposttext" , "initcomment"})
@XmlAccessorType(XmlAccessType.FIELD)
public class InitPost {

    private String initposttext;
    private List<String> initcomment;

    public String getPostText() {
        return this.initposttext;
    }

    public void setPostText(String posttext){ this.initposttext = posttext; }

    public List<String> getComments(){
        return this.initcomment;
    }

    public void setComments(List<String> comments) {
        this.initcomment = comments;
    }
}
