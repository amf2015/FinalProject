package edu.unh.cs753853.team1.agents;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import edu.unh.cs753853.team1.QueryParagraphs;

@Path("/query")
public class QueryApi {

	@GET
	@Path("/{param}")
	public Response getResult(@PathParam("param") String param) {

		String query = param;

		// Call rank function to get result with query

		String result = QueryParagraphs.getResults(query);

		return Response.status(200).entity(result).build();

	}
}
