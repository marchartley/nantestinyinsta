package hartley.vente;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Entity;
import com.google.type.Date;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

@com.googlecode.objectify.annotation.Entity
public class Post {

    private Long id;
    private String image;
    private String author;
    private int likes;
    private String description;
    private Date date;

    public Post(long id, String image, String author, int likes, String description, Date date){
        this.image = image;
        this.author = author;
        this.likes = likes;
        this.description = description;
        this.date = date;
        
    }
}
