package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.DocumentDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.ResponseDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.DocumentBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Document;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Stateless
@DeclareRoles({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
@Path("/documents")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class DocumentService {

    @EJB
    private DocumentBean documentBean;

    @EJB
    private UserBean userBean;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response uploadDocument(MultipartFormDataInput input) {
        try {
            var principal = securityContext.getUserPrincipal();
            if (principal == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ResponseDTO(false, "User not authenticated"))
                    .build();
            }

            String username = principal.getName();
            User user = userBean.find(username);
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ResponseDTO(false, "User not found"))
                    .build();
            }

            Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
            
            if (!formDataMap.containsKey("file")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, "No file provided"))
                    .build();
            }

            List<InputPart> fileParts = formDataMap.get("file");
            if (fileParts == null || fileParts.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, "No file provided"))
                    .build();
            }

            InputPart filePart = fileParts.get(0);
            
            String contentDisposition = filePart.getHeaders().getFirst("Content-Disposition");
            String filename = extractFilename(contentDisposition);
            
            if (!documentBean.isValidFileType(filename)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, "Invalid file type. Only PDF and ZIP files are allowed"))
                    .build();
            }
            
            InputStream fileInputStream = filePart.getBody(InputStream.class, null);
            long documentId = documentBean.create(filename, fileInputStream, user);

            Document document = documentBean.find(documentId);
            DocumentDTO documentDTO = new DocumentDTO(document.getId(), document.getFilename());

            return Response.status(Response.Status.CREATED)
                .entity(documentDTO)
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error uploading document: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getDocument(@PathParam("id") long id) {
        try {
            Document document = documentBean.find(id);
            if (document == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Document not found"))
                    .build();
            }

            DocumentDTO documentDTO = new DocumentDTO(document.getId(), document.getFilename());
            return Response.ok(documentDTO).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error retrieving document: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}/download")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, "application/pdf", "application/zip"})
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response downloadDocument(@PathParam("id") long id) {
        try {
            Document document = documentBean.find(id);
            if (document == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String filepath = document.getFilepath();
            if (filepath == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            java.nio.file.Path path = Paths.get(filepath);
            if (!Files.exists(path)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String filename = document.getFilename() != null ? document.getFilename() : path.getFileName().toString();

            String contentType = MediaType.APPLICATION_OCTET_STREAM;
            if (filename.toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (filename.toLowerCase().endsWith(".zip")) {
                contentType = "application/zip";
            }

            return Response.ok(path.toFile())
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", contentType)
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response deleteDocument(@PathParam("id") long id) {
        try {
            var principal = securityContext.getUserPrincipal();
            if (principal == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ResponseDTO(false, "User not authenticated"))
                    .build();
            }

            Document document = documentBean.find(id);
            if (document == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Document not found"))
                    .build();
            }

            String username = principal.getName();
            User currentUser = userBean.find(username);
            if (!document.getUser().getUsername().equals(username) && 
                !currentUser.getRole().getName().equals("ADMINISTRADOR")) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ResponseDTO(false, "You don't have permission to delete this document"))
                    .build();
            }

            documentBean.delete(id);

            return Response.ok()
                .entity(new ResponseDTO(true, "Document deleted successfully"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error deleting document: " + e.getMessage()))
                .build();
        }
    }

    private String extractFilename(String contentDisposition) {
        if (contentDisposition == null) {
            return "unknown";
        }
        String[] parts = contentDisposition.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("filename")) {
                return part.substring(part.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "unknown";
    }
}
