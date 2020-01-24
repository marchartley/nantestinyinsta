package hartley.vente;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
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

import com.google.gson.Gson;

@WebServlet(name="Comment", urlPatterns= {"/post_comment"})
public class CommentManager extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3536529189806207306L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.addHeader("Access-Control-Allow-Origin", "*");
		
		String post = req.getParameter("post");
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
			author.setProperty("followersCount", UserManager.getFollowersCount((String) author.getProperty("email")));
			users.put(authorEmail, author);
		}
		
		resp.getWriter().print(new Gson().toJson(new Object[]{comments, users}));
	}


	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.addHeader("Access-Control-Allow-Origin", "*");

		String user = req.getParameter("user");
		String post = req.getParameter("post");
		String commentContent = req.getParameter("comment");
		
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        int numberCpt = PostManager.numberOfCounts;
        int rand = new Random().nextInt(Math.toIntExact(numberCpt));
        Query q = new Query("CommentCount").setFilter(new FilterPredicate("post", FilterOperator.EQUAL, post));
        PreparedQuery pq = datastore.prepare(q);
        Entity commentCount = pq.asList(FetchOptions.Builder.withDefaults()).get(rand);
        long cpt = (long) commentCount.getProperty("value");
        commentCount.setProperty("value", (long) cpt + 1);
        datastore.put(commentCount);
        
        Date date = new Date();
        Entity comment = new Entity("Comment");
        comment.setIndexedProperty("post", post);
        comment.setProperty("author", user);
        comment.setProperty("date", date.getTime());
        comment.setProperty("message", commentContent);
        datastore.put(comment);
		resp.getWriter().print(new Gson().toJson(comment));
	}

	public static void createCommentsCount(String idPost, int numberOfCounts) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		for(int i = 0; i < numberOfCounts; i++) {
			Entity c = new Entity("CommentCount", idPost + "-" + i);
			c.setProperty("post", idPost);
			c.setProperty("value", 0);
			datastore.put(c);
		}
	}
	public static int getCommentsCount(String idPost) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("CommentCount").setFilter(new FilterPredicate("post", FilterOperator.EQUAL, idPost));
		List<Entity> counts = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		int count = 0;
		for(Entity e : counts)
			count += (long) e.getProperty("value");
		return count;
	}
}
