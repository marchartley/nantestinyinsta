package hartley.vente;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.*;
import com.google.gson.Gson;

@WebServlet(name="Search", urlPatterns= {"/search"})
public class SearchManager extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3816295909685797853L;

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		resp.addHeader("Access-Control-Allow-Origin", "*");

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String search = req.getParameter("search");
		
		if(search != null && search != "") {
			List<Entity> posts = new ArrayList<Entity>();
			List<Entity> users = new ArrayList<Entity>();
			
			Object[] results = PostManager.doFullSearch(search, datastore);
			users = (List<Entity>) results[0];
			posts = (List<Entity>) results[1];

			resp.getWriter().print(new Gson().toJson(
					new Object[]
							{users, new Object[] {
									posts, PostManager.getPostUsers(posts, datastore)} }));
		}
	}
}