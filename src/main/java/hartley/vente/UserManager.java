package hartley.vente;

import java.io.IOException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;

import com.google.gson.Gson;

@WebServlet(name="User", urlPatterns= {"/user"})
public class UserManager extends HttpServlet {
	public static int numberOfCounts = 5;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3536529189806207306L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		resp.addHeader("Access-Control-Allow-Origin", "*");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String email = req.getParameter("mail");
		String pass = req.getParameter("pass");

		Entity result = UserManager.getUser(email, datastore);
		if(pass != null && !pass.equals((String) result.getProperty("password"))) {
			result = null;
		}
		resp.getWriter().print(new Gson().toJson(result));
	}
	
	public static Entity getUser(String email, DatastoreService ds) {
		Query q = new Query("User").setFilter(new FilterPredicate("email", FilterOperator.EQUAL, email));
		PreparedQuery pq = ds.prepare(q);
		Entity user= pq.asSingleEntity();
		user.setProperty("followersCount", getFollowersCount(email));
		return user;
	}
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.addHeader("Access-Control-Allow-Origin", "*");
		String email = req.getParameter("mail");
		String firstname = req.getParameter("firstname");
		String lastname = req.getParameter("lastname");
		String password = req.getParameter("password");
		String pseudo = req.getParameter("pseudo");
		
		Entity e = new Entity("User", email);
		e.setProperty("email", email);
		e.setProperty("firstname", firstname);
		e.setProperty("lastname", lastname);
		e.setProperty("password", password);
		e.setProperty("pseudo", pseudo);
		e.setProperty("postsCount", 0);
		e.setProperty("followings", "");
		e.setProperty("likes", "");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);
		FollowerManager.createFollowersCount(email, UserManager.numberOfCounts);
	}
	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse resp) {
		resp.addHeader("Access-Control-Allow-Origin", "*");
		String email = req.getParameter("mail");
		String firstname = req.getParameter("firstname");
		String lastname = req.getParameter("lastname");
		String password = req.getParameter("password");
		String pseudo = req.getParameter("pseudo");

		Entity e = new Entity("User", email);
		e.setProperty("firstname", firstname);
		e.setProperty("lastname", lastname);
		e.setProperty("password", password);
		e.setProperty("pseudo", pseudo);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);
	}

	public static int getFollowersCount(String idUser) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("FollowersCount").setFilter(new FilterPredicate("user", FilterOperator.EQUAL, idUser));
		List<Entity> counts = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		int count = 0;
		for(Entity e : counts)
			count += (long) e.getProperty("value");
		return count;
	}
	
	public static List<Entity> search(String keyword, DatastoreService ds) {
		Filter f = CompositeFilterOperator.or(
				UserManager.createStartWithFilter("pseudo", keyword),
				UserManager.createStartWithFilter("firstname", keyword),
				UserManager.createStartWithFilter("lastname", keyword),
				UserManager.createStartWithFilter("email", keyword)
				);

		Query q = new Query("User").setFilter(f);
		return ds.prepare(q).asList(FetchOptions.Builder.withDefaults());
	}
	
	public static Filter createStartWithFilter(String property, String search) {
		return CompositeFilterOperator.and(
				FilterOperator.GREATER_THAN_OR_EQUAL.of(property, search),
				FilterOperator.LESS_THAN.of(property, search + "\uFFFD")
				);
	}
}
