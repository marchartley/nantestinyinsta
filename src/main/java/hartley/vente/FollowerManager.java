package hartley.vente;

import java.io.IOException;
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

@WebServlet(name="Follower", urlPatterns= {"/follower"})
public class FollowerManager extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3536529189806207306L;
/*
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.addHeader("Access-Control-Allow-Origin", "*");
		
		String post = req.getParameter("user");
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query("Comment").setFilter(new FilterPredicate("post", FilterOperator.EQUAL, post));
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> comments = pq.asList(FetchOptions.Builder.withDefaults());

		HashMap<String, Entity> users = new HashMap<String, Entity>();
		for(Entity e : comments) {
			String authorEmail = (String) e.getProperty("author");

			q = new Query("User");
			q.setFilter(new FilterPredicate("email", FilterOperator.EQUAL, authorEmail));
			Entity author = datastore.prepare(q).asSingleEntity();
			users.put(authorEmail, author);
		}
		
		resp.getWriter().print(new Gson().toJson(new Object[]{comments, users}));
	}
*/

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.addHeader("Access-Control-Allow-Origin", "*");

		String follower = req.getParameter("follower");
		String following = req.getParameter("following");
		int followAdd = 1;
		
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity user = datastore.prepare(new Query("User").setFilter(new FilterPredicate("email", FilterOperator.EQUAL, follower))).asSingleEntity();
        String followChain = (String) user.getProperty("followings");
        int index = followChain.indexOf(following);
        if(index != -1) {
        	followAdd = -1;
        	followChain = String.join("", followChain.substring(0, index), followChain.substring(index + following.length() + 1));
        } else {
        	followChain += following + ";";
        }
        user.setProperty("followings", followChain);
        
        int numberCpt = UserManager.numberOfCounts;
        int rand = new Random().nextInt(Math.toIntExact(numberCpt));
        Query q = new Query("FollowersCount").setFilter(new FilterPredicate("user", FilterOperator.EQUAL, following));
        PreparedQuery pq = datastore.prepare(q);
        Entity followCount = pq.asList(FetchOptions.Builder.withDefaults()).get(rand);
        long cpt = (long) followCount.getProperty("value");
        followCount.setProperty("value", (long) cpt + followAdd);
        datastore.put(followCount);
        datastore.put(user);
        
        resp.getWriter().print("Before : " + cpt + ", after : " + (long) followCount.getProperty("value"));

	}
	

	public static void createFollowersCount(String idUser, int numberOfCounts) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		for(int i = 0; i < numberOfCounts; i++) {
			Entity c = new Entity("FollowersCount", idUser + "-" + i);
			c.setProperty("user", idUser);
			c.setProperty("value", 0);
			datastore.put(c);
		}
	}
}
