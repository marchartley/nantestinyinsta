package hartley.vente;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@WebServlet(name="Likes", urlPatterns= {"/post_like"})
public class LikesManager extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3536529189806207306L;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.addHeader("Access-Control-Allow-Origin", "*");

		String user = req.getParameter("user");
		String post = req.getParameter("post");
		int likeAdd = 1;
		
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query("User").setFilter(new FilterPredicate("email", FilterOperator.EQUAL, user));
        Entity u = datastore.prepare(q).asSingleEntity();
        String likesChain = (String) u.getProperty("likes");
        int index = likesChain.indexOf(post);
        if(index != -1) {
        	likeAdd = -1;
        	likesChain = String.join("", likesChain.substring(0, index), likesChain.substring(index + post.length() + 1));
        } else {
        	likesChain += post + ";";
        }
        u.setProperty("likes", likesChain);
        
        int numberCpt = PostManager.numberOfCounts;
        int rand = new Random().nextInt(Math.toIntExact(numberCpt));
        q = new Query("LikesCount").setFilter(new FilterPredicate("post", FilterOperator.EQUAL, post));
        PreparedQuery pq = datastore.prepare(q);
        Entity likeCount = pq.asList(FetchOptions.Builder.withDefaults()).get(rand);
        long cpt = (long) likeCount.getProperty("value");
        likeCount.setProperty("value", (long) cpt + likeAdd);
        datastore.put(likeCount);
        datastore.put(u);
	}
	

	public static void createLikesCount(String idPost, int numberOfCounts) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		for(int i = 0; i < numberOfCounts; i++) {
			Entity c = new Entity("LikesCount", idPost + "-" + i);
			c.setProperty("post", idPost);
			c.setProperty("value", 0);
			datastore.put(c);
		}
	}
	public static int getLikes(String idPost) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("LikesCount").setFilter(new FilterPredicate("post", FilterOperator.EQUAL, idPost));
		List<Entity> counts = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		int count = 0;
		for(Entity e : counts)
			count += (long) e.getProperty("value");
		return count;
	}
}
