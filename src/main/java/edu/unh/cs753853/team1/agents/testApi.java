package edu.unh.cs753853.team1.agents;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/test")
public class testApi {

	@GET
	@Path("/{param}")
	public Response getResult(@PathParam("param") String param) {

		String query = param;

		System.out.println("Receive query: " + query);
		// Call rank function to get result with query

		// String result = QueryParagraphs.getResults(query);

		String result = "Fake Result.";
		return Response.status(200).entity(result).build();

	}
}
