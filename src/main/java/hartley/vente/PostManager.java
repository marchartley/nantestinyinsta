package hartley.vente;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;

import com.google.gson.Gson;

@WebServlet(name="Post", urlPatterns= {"/post"})
public class PostManager extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5576456063149794302L;
	public static int numberOfCounts = 10;
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		resp.addHeader("Access-Control-Allow-Origin", "*");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String offset = req.getParameter("offset");
		String limit = req.getParameter("limit");
		String email = req.getParameter("lookingUser");
		String user = req.getParameter("user");
		String id = req.getParameter("id");

		List<Entity> posts = new ArrayList<Entity>();
		
		Entity me = datastore.prepare(new Query("User").setFilter(new FilterPredicate("email", FilterOperator.EQUAL, user))).asSingleEntity();
		if(id != null) {
			posts = getPost(id, datastore);
		} else if(email != null) {
			posts = PostManager.getUsersPosts(email, datastore, Integer.parseInt(limit), Integer.parseInt(offset), "date");
		} else {
			posts = PostManager.getPostsWithFollowers(me, datastore, Integer.parseInt(limit), Integer.parseInt(offset), "date");
		}
		HashMap<String, Entity> usersMapping = PostManager.getPostUsers(posts, datastore);
		
		resp.getWriter().print(new Gson().toJson(new Object[]{posts, usersMapping}));
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.addHeader("Access-Control-Allow-Origin", "*");
		
		
		String image = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		String email = req.getParameter("author");
		String description = req.getParameter("description");
		String title = req.getParameter("title");
		Date date = new Date();
//		String image = req.getParameter("image");
		String id = email + date.getTime();
		
		Entity e = new Entity("Post", id);
		e.setProperty("id", id);
		e.setProperty("author", email);
		e.setProperty("description", description);
		e.setProperty("title", title);
		e.setProperty("date", date.getTime());
		e.setProperty("image", new Text(image));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);
		
		Entity user = datastore.prepare(new Query("User").setFilter(new FilterPredicate("email", FilterOperator.EQUAL, email))).asSingleEntity();
		long nbPosts = (long) user.getProperty("postsCount");
		user.setProperty("postsCount", nbPosts+1);
		datastore.put(user);
		
		LikesManager.createLikesCount(id, PostManager.numberOfCounts);
		CommentManager.createCommentsCount(id, PostManager.numberOfCounts);
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

	public static List<Entity> search(String keyword, DatastoreService ds) {
		Filter f = CompositeFilterOperator.or(
				UserManager.createStartWithFilter("title", keyword),
				UserManager.createStartWithFilter("author", keyword)
				);

		Query q = new Query("Post").setFilter(f);
//		return q.toString();
		List<Entity> posts = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());

		for(Entity e : posts) {
			e.setProperty("likes", LikesManager.getLikes((String) e.getProperty("id")));
			e.setProperty("commentCount", CommentManager.getCommentsCount((String) e.getProperty("id")));
		}
		return posts;
	}
	
	public static List<Entity> getPost(String idPost, DatastoreService ds) {
		Query q = new Query("Post");
		q.setFilter(new FilterPredicate("id", FilterOperator.EQUAL, idPost));
		List<Entity> posts = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());

		for(Entity e : posts) {
			e.setProperty("likes", LikesManager.getLikes((String) e.getProperty("id")));
			e.setProperty("commentCount", CommentManager.getCommentsCount((String) e.getProperty("id")));
		}
		return posts;
	}
	public static List<Entity> getPostsWithFollowers(Entity user, DatastoreService ds, int limit, int offset, String sort) {
		Query q = new Query("Post");
		String followingsChain = "";
		String[] followings = new String[] {};
		if(user != null) {
			followingsChain = (String) user.getProperty("followings");
			if(followingsChain != null && followingsChain != "") {
				followingsChain += user;
				followings = followingsChain.split(";");
			}
		}
		if(followings.length > 0) {
			q.setFilter(new FilterPredicate("author", FilterOperator.IN, Arrays.asList(followings))).addSort(sort);
		}
		List<Entity> posts = ds.prepare(q).asList(FetchOptions.Builder.withLimit(limit).offset(offset));

		for(Entity e : posts) {
			e.setProperty("likes", LikesManager.getLikes((String) e.getProperty("id")));
			e.setProperty("commentCount", CommentManager.getCommentsCount((String) e.getProperty("id")));
		}
		return posts;
	}
	public static List<Entity> getUsersPosts(String user, DatastoreService ds, int limit, int offset, String sort) {
		Query q = new Query("Post");
		q.setFilter(new FilterPredicate("author", FilterOperator.EQUAL, user)).addSort(sort);
		List<Entity> posts = ds.prepare(q).asList(FetchOptions.Builder.withLimit(limit).offset(offset));

		for(Entity e : posts) {
			e.setProperty("likes", LikesManager.getLikes((String) e.getProperty("id")));
			e.setProperty("commentCount", CommentManager.getCommentsCount((String) e.getProperty("id")));
		}
		return posts;
	}
	public static HashMap<String, Entity> getPostUsers(List<Entity> posts, DatastoreService ds) {
		HashMap<String, Entity> usersMapping = new HashMap<String, Entity>();
		for(Entity e : posts) {
			String authorEmail = (String) e.getProperty("author");
			usersMapping.put(authorEmail, UserManager.getUser(authorEmail, ds));
		}
		return usersMapping;
	}
	public static Object[] doFullSearch(String keyword, DatastoreService ds) {
		Object[] results = new Object[2];
		results[0] = UserManager.search(keyword, ds);
		results[1] = PostManager.search(keyword, ds);
		return results;
	}
}
