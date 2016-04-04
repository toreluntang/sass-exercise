package dk.itu.sass.teame.boundary;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.google.gson.JsonObject;

import dk.itu.sass.teame.controller.FileController;
import dk.itu.sass.teame.entity.File;

@Path("file")
public class FileResource {
	
	private String fileLocation = "/Users/Alexander/Code/Servers/wildfly-10-sass/fakestagram/images";
	
	@Inject
	FileController fc;
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response recieveFile(MultipartFormDataInput input, @QueryParam("userid") String userId) {

		JsonObject json = new JsonObject();
		
		Long uid = null;
		try {
			uid = Long.parseLong(userId);
		} catch (Exception e){
			json.addProperty("Error", "Wrong user id: " + userId);
			return Response.status(Status.BAD_REQUEST).entity(json.toString()).build();
		}
		
		Map<String, List<InputPart>> maps = input.getFormDataMap();
		List<InputPart> f = maps.get("file");

		MultivaluedMap<String, String> mv = f.get(0).getHeaders();
		
		//Hijacking filenames xD Timestamp/uuid to fix - but it's a cool feature.
		String filename = getFileName(mv);
		
		java.nio.file.Path sti = null;
		
		try {
			InputStream is = f.get(0).getBody(InputStream.class, null);
			byte[] barr = IOUtils.toByteArray(is);
			java.nio.file.Path p = Paths.get(fileLocation,filename);//#fail
			Files.deleteIfExists(p); //#fail
			Files.createDirectories(p.getParent());//#fail
			sti = Files.write(p, barr, StandardOpenOption.CREATE_NEW);//#fail
		} catch(Exception e){
			json.addProperty("Error", "Couldn't write file");
			json.addProperty("Trace", e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
		}
		
		File file = fc.uploadFile(uid,sti);
		
		json.addProperty("id", file.getId());
		json.addProperty("userId", file.getUserId());
		json.addProperty("path", file.getPath().toString());
		json.addProperty("timestamp", file.getTimestamp().toString());
		
		return Response.ok().entity(json.toString()).build();
	}
	
	private String getFileName(MultivaluedMap<String, String> header) {

		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
		
		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");
				
				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

}
