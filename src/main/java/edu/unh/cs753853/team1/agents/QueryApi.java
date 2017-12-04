package edu.unh.cs753853.team1.agents;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import edu.unh.cs753853.team1.QueryManager;

@Path("/query")
public class QueryApi {
	private QueryManager qm = QueryManager.getInstance();

	@GET
	@Path("/{param}")
	public Response getResult(@PathParam("param") String param) {

		String query = param.toLowerCase();

		System.out.println("Receive query: " + query);
		// Call rank function to get result with query

		String result = qm.getResults(query);

		if (result.isEmpty()) {
			return Response.status(204).entity("No Results").build();
		}

		return Response.status(200).entity(result).build();

	}
}
