package dk.itu.sass.teame.boundary;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.itu.sass.teame.controller.CommentController;

@Path("comment")
@Produces(MediaType.APPLICATION_JSON)
public class CommentResource {
	
	@Inject
	private CommentController commentController;
	
	@POST
	public Response addComment(
			@FormParam("comment") String comment, 
			@FormParam("userId")  long userId,
			@FormParam("imageId") long imageId){
			long c = commentController.addComment(comment, userId, imageId);
			return Response.status(Response.Status.ACCEPTED).entity(c).build();
	}

	@GET
	public Response getImageComments(
			@QueryParam("imageId") String imageId) {
		
		String json = commentController.getComments(imageId);
		
		return Response.status(Response.Status.ACCEPTED).entity(json).build();
	}

}
