package hartley.vente;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Entity;
import com.googlecode.objectify.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;


@com.googlecode.objectify.annotation.Entity
public class User implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -493520835275415776L;
	@PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
    @Id private String email;
    @Persistent
    private String password;
    @Persistent
    private String pseudo;
    @Persistent
    private String firstname;
    @Persistent
    private String lastname;
    @Persistent
    private ArrayList<User> followers;
    @Persistent
    private int followingsCount;

    public User(String email, String password, String firstname, String lastname, 
    		String pseudo, ArrayList<User> followers, int followingsCount){
        this.email = email;
        this.password = password;
        this.pseudo = pseudo;
        this.lastname = lastname;
        this.firstname = firstname;
        this.followers = followers;
        this.followingsCount = followingsCount;
    }


}
