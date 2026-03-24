package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.*;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.DocumentBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.*;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
@DeclareRoles({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
@PermitAll
@Path("/publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class PublicationService {

    @PersistenceContext
    private EntityManager entityManager;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private UserBean userBean;

    @EJB
    private DocumentBean documentBean;

    @EJB
    private pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.HistoryBean historyBean;

    @Context
    private SecurityContext securityContext;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createPublication(MultipartFormDataInput input) {
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
            
            String name = getFormFieldValue(formDataMap, "name");
            String description = getFormFieldValue(formDataMap, "description");
            String isPublicStr = getFormFieldValue(formDataMap, "public");
            boolean isPublic = Boolean.parseBoolean(isPublicStr);
            Integer areaId = getFormFieldIntegerValue(formDataMap, "area");
            List<Long> tagIds = getFormFieldLongList(formDataMap, "tags");

            if (name == null || name.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, "Publication name is required"))
                    .build();
            }

            Long documentId = null;
            if (formDataMap.containsKey("file")) {
                List<InputPart> fileParts = formDataMap.get("file");
                if (fileParts != null && !fileParts.isEmpty()) {
                    InputPart filePart = fileParts.get(0);
                    
                    String contentDisposition = filePart.getHeaders().getFirst("Content-Disposition");
                    String filename = extractFilename(contentDisposition);
                    
                    if (!documentBean.isValidFileType(filename)) {
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ResponseDTO(false, "Invalid file type. Only PDF and ZIP files are allowed"))
                            .build();
                    }
                    
                    InputStream fileInputStream = filePart.getBody(InputStream.class, null);
                    documentId = documentBean.create(filename, fileInputStream, user);
                }
            }

            long publicationId = publicationBean.create(
                name,
                LocalDateTime.now(),
                description,
                isPublic,
                documentId,
                areaId,
                tagIds,
                user
            );

            historyBean.create(user, "POST", "/publications",
                String.format("{\"name\":\"%s\",\"description\":\"%s\",\"public\":%b}", name, description != null ? description : "", isPublic));

            return Response.status(Response.Status.CREATED)
                .entity(new ResponseDTO(true, "The publication has been created"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error creating publication: " + e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deletePublication(@PathParam("id") long id) {
        try {
            Publication publication = publicationBean.find(id);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Publication not found"))
                    .build();
            }

            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                User currentUser = userBean.find(principal.getName());
                if (currentUser != null) {
                    historyBean.create(currentUser, "DELETE", "/publications/" + id, null);
                }
            }

            publicationBean.delete(id);

            return Response.ok()
                .entity(new ResponseDTO(true, "The publication has been deleted"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error deleting publication: " + e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updatePublication(@PathParam("id") long id, PublicationUpdateDTO publicationDTO) {
        try {
            Publication publication = publicationBean.find(id);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Publication not found"))
                    .build();
            }

            publicationBean.update(
                id,
                publicationDTO.getName(),
                publicationDTO.getDescription()
            );

            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                User currentUser = userBean.find(principal.getName());
                if (currentUser != null) {
                    historyBean.create(currentUser, "PUT", "/publications/" + id,
                        String.format("{\"name\":\"%s\",\"description\":\"%s\"}",
                            publicationDTO.getName(), publicationDTO.getDescription() != null ? publicationDTO.getDescription() : ""));
                }
            }

            return Response.ok()
                .entity(new ResponseDTO(true, "The publication has been changed"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error updating publication: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/{id}/ratings")
    public Response addRating(@PathParam("id") long id, RatingDTO ratingDTO) {
        try {
            Publication publication = publicationBean.find(id);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Publication not found"))
                    .build();
            }

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

            publicationBean.addRating(id, user, ratingDTO.getValue());

            historyBean.create(user, "POST", "/publications/" + id + "/ratings",
                String.format("{\"value\":%d}", ratingDTO.getValue()));

            return Response.status(Response.Status.CREATED)
                .entity(new ResponseDTO(true, "The rating " + ratingDTO.getValue() + " has been added to publication"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error adding rating: " + e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}/ratings/{ratingId}")
    public Response removeRating(@PathParam("id") long id, @PathParam("ratingId") long ratingId) {
        try {
            Publication publication = publicationBean.find(id);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Publication not found"))
                    .build();
            }

            pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Rating rating = publicationBean.findRating(ratingId);
            if (rating == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Rating not found"))
                    .build();
            }

            int ratingValue = rating.getValue();
            publicationBean.removeRating(id, ratingId);

            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                User currentUser = userBean.find(principal.getName());
                if (currentUser != null) {
                    historyBean.create(currentUser, "DELETE", "/publications/" + id + "/ratings/" + ratingId, null);
                }
            }

            return Response.ok()
                .entity(new ResponseDTO(true, "The rating " + ratingValue + " has been removed from publication"))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error removing rating: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/{id}/tag")
    public Response addTag(@PathParam("id") long id, TagIdDTO tagIdDTO) {
        try {
            if (tagIdDTO == null || tagIdDTO.getId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, "Tag id is required"))
                    .build();
            }

            Publication publication = publicationBean.find(id);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Publication not found"))
                    .build();
            }

            publicationBean.addTag(id, tagIdDTO.getId());

            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                User currentUser = userBean.find(principal.getName());
                if (currentUser != null) {
                    historyBean.create(currentUser, "POST", "/publications/" + id + "/tag",
                        String.format("{\"id\":%d}", tagIdDTO.getId()));
                }
            }

            return Response.status(Response.Status.CREATED)
                .entity(new ResponseDTO(true, "The tag " + tagIdDTO.getId() + " has been added to publication"))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error adding tag: " + e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setPublicationVisibility(
            @PathParam("id") long id,
            PublicationVisibilityDTO dto
    ) {
        if (securityContext == null ||
                !(securityContext.isUserInRole("RESPONSAVEL") || securityContext.isUserInRole("ADMINISTRADOR"))) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access forbidden for this resource").build();
        }
        try {
            publicationBean.setPublicationVisibility(id, dto.isPublic());

            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                User currentUser = userBean.find(principal.getName());
                if (currentUser != null) {
                    historyBean.create(currentUser, "PUT", "/publications/" + id + "/visibility",
                        String.format("{\"public\":%b}", dto.isPublic()));
                }
            }

            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{pubId}/comments/{commentId}/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setCommentVisibility(
            @PathParam("pubId") long pubId,
            @PathParam("commentId") long commentId,
            VisibilityDTO dto
    ) {
        if (securityContext == null ||
                !(securityContext.isUserInRole("RESPONSAVEL") ||
                        securityContext.isUserInRole("ADMINISTRADOR"))) {

            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ResponseDTO(false, "Access forbidden for this resource"))
                    .build();
        }

        try {
            publicationBean.setCommentVisibility(
                    pubId,
                    commentId,
                    dto.isVisible()
            );

            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                User currentUser = userBean.find(principal.getName());
                if (currentUser != null) {
                    historyBean.create(currentUser, "PUT", "/publications/" + pubId + "/comments/" + commentId + "/visibility",
                        String.format("{\"visible\":%b}", dto.isVisible()));
                }
            }

            String message = dto.isVisible() ? "Comment visibility set to visible" : "Comment visibility set to hidden";
            return Response.ok()
                    .entity(new ResponseDTO(true, message))
                    .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        }
    }


    @GET
    @Path("/hidden")
    public List<PublicationListDTO> getHiddenPublications() {
    if (securityContext == null ||
        !(securityContext.isUserInRole("RESPONSAVEL") || securityContext.isUserInRole("ADMINISTRADOR"))) {
        throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
            .entity("Access forbidden for this resource").build());
    }

    return publicationBean.getHiddenPublications()
        .stream()
        .map(p -> {

            int commentsCount = 0;
            if (p.getComments() != null) {
            commentsCount = (int) p.getComments().stream()
                .filter(Comment::isVisible)
                .count();
            }

            List<String> tagNames = p.getTags() == null
                ? List.of()
                : p.getTags().stream()
                .filter(Tag::isVisible)
                .map(Tag::getName)
                .collect(Collectors.toList());


            int ratingCount = 0;
            double averageRating = 0.0;
            if (p.getRatings() != null && !p.getRatings().isEmpty()) {
            ratingCount = p.getRatings().size();
            double sum = p.getRatings().stream()
                .mapToInt(Rating::getValue)
                .sum();
            averageRating = sum / ratingCount;
            }

            String userName = p.getUser() != null ? p.getUser().getUsername() : null;

            return new PublicationListDTO(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getData() != null ? p.getData().toString() : null,
                p.getArea() != null ? p.getArea().getName() : null,
                commentsCount,
                tagNames,
                averageRating,
                ratingCount,
                p.isPublic(),
                userName
            );
        })
        .collect(Collectors.toList());
    }

    @DELETE
    @Path("/{id}/tag/{tagId}")
    public Response removeTag(@PathParam("id") long id, @PathParam("tagId") long tagId) {
        try {
            Publication publication = publicationBean.find(id);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Publication not found"))
                    .build();
            }

            publicationBean.removeTag(id, tagId);

            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                User currentUser = userBean.find(principal.getName());
                if (currentUser != null) {
                    historyBean.create(currentUser, "DELETE", "/publications/" + id + "/tag/" + tagId, null);
                }
            }

            return Response.ok()
                .entity(new ResponseDTO(true, "The tag " + tagId + " has been removed from publication"))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error removing tag: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/{id}/comments")
    public Response addComment(@PathParam("id") long id, CommentCreateDTO commentDTO) {
        try {
            if (commentDTO == null || commentDTO.getText() == null || commentDTO.getText().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, "Text is required"))
                    .build();
            }

            Publication publication = publicationBean.find(id);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Publication not found"))
                    .build();
            }

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

            publicationBean.addComment(id, user, commentDTO.getText());

            historyBean.create(user, "POST", "/publications/" + id + "/comments",
                String.format("{\"text\":\"%s\"}", commentDTO.getText()));

            return Response.status(Response.Status.CREATED)
                .entity(new ResponseDTO(true, "The comment has been added to publication"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error adding comment: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}/comments")
    public Response getPublicationComments(@PathParam("id") long id) {
        try {
            Publication publication = publicationBean.find(id);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Publication not found"))
                    .build();
            }

            final boolean isResponsavel;
            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                String username = principal.getName();
                User user = userBean.find(username);
                isResponsavel = user != null && user.getRole() != null && "RESPONSAVEL".equals(user.getRole().getName());
            } else {
                isResponsavel = false;
            }

            //filter comments based on user role
            List<CommentResponseDTO> commentDTOs = publication.getComments() == null
                ? List.of()
                : publication.getComments().stream()
                    .filter(c -> isResponsavel || c.isVisible())
                    .map(c -> new CommentResponseDTO(
                        c.getId(),
                        c.getText(),
                        c.getDate().toString(),
                        c.getUser().getId(),
                        c.getUser().getName(), c.isVisible()
                    ))
                    .collect(Collectors.toList());

            return Response.ok(commentDTOs).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ResponseDTO(false, "Error fetching comments: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}/comments/hidden")
    public Response getPublicationHiddenComments(@PathParam("id") long id) {
        try {
            Publication publication = publicationBean.find(id);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "Publication not found"))
                        .build();
            }

            List<CommentResponseDTO> commentDTOs = publication.getComments() == null
                    ? List.of()
                    : publication.getComments().stream()
                    .filter(c -> !c.isVisible())
                    .map(c -> new CommentResponseDTO(
                            c.getId(),
                            c.getText(),
                            c.getDate().toString(),
                            c.getUser().getId(),
                            c.getUser().getName(), c.isVisible()
                    ))
                    .collect(Collectors.toList());

            return Response.ok(commentDTOs).build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, "Error fetching comments: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/me")
    public Response getMyPublications() {
        try {
            var principal = securityContext.getUserPrincipal();
            if (principal == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            String username = principal.getName();
            User user = userBean.find(username);
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            List<Publication> publications = publicationBean.findByUser(user);

            List<PublicationDetailDTO> result = publications.stream()
                .map(p -> {
                    List<String> tagNames = p.getTags() == null
                        ? List.of()
                        : p.getTags().stream()
                            .map(Tag::getName)
                            .collect(Collectors.toList());

                    List<PublicationDetailDTO.CommentSummaryDTO> commentDTOs = p.getComments() == null
                        ? List.of()
                        : p.getComments().stream()
                            .map(c -> new PublicationDetailDTO.CommentSummaryDTO(
                                c.getId(),
                                c.getText(),
                                c.getUser().getName(),
                                c.getDate().toString()
                            ))
                            .collect(Collectors.toList());

                    List<PublicationDetailDTO.RatingSummaryDTO> ratingDTOs = p.getRatings() == null
                        ? List.of()
                        : p.getRatings().stream()
                            .map(r -> new PublicationDetailDTO.RatingSummaryDTO(
                                r.getId(),
                                r.getValue(),
                                r.getUser().getName(),
                                r.getDate().toString()
                            ))
                            .collect(Collectors.toList());

                    return new PublicationDetailDTO(
                        p.getId(),
                        p.getUser() != null ? p.getUser().getId() : 0,
                        p.getName(),
                        p.getData() != null ? p.getData().toString() : null,
                        tagNames,
                        p.isPublic(),
                        p.getDescription(),
                        commentDTOs,
                        ratingDTOs,
                        p.getArea() != null ? (long) p.getArea().getId() : null,
                        List.of(), 
                            p.getUser().getName(),
                            p.isVisible()
                    );
                })
                .collect(Collectors.toList());

            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ResponseDTO(false, "Error fetching publications: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getPublication(@PathParam("id") long id) {
        try {
            Publication p = publicationBean.find(id);
            if (p == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "Publication not found"))
                    .build();
            }

            List<String> tagNames = p.getTags() == null
                ? List.of()
                : p.getTags().stream()
                    .filter(Tag::isVisible)
                    .map(Tag::getName)
                    .collect(Collectors.toList());

            List<PublicationDetailDTO.CommentSummaryDTO> commentDTOs = p.getComments() == null
                ? List.of()
                : p.getComments().stream()
                    .filter(Comment::isVisible)
                    .map(c -> new PublicationDetailDTO.CommentSummaryDTO(
                        c.getId(),
                        c.getText(),
                        c.getUser().getName(),
                        c.getDate().toString()
                    ))
                    .collect(Collectors.toList());

            List<PublicationDetailDTO.RatingSummaryDTO> ratingDTOs = p.getRatings() == null
                ? List.of()
                : p.getRatings().stream()
                    .map(r -> new PublicationDetailDTO.RatingSummaryDTO(
                        r.getId(),
                        r.getValue(),
                        r.getUser().getName(),
                        r.getDate().toString()
                    ))
                    .collect(Collectors.toList());

            PublicationDetailDTO result = new PublicationDetailDTO(
                p.getId(),
                p.getUser() != null ? p.getUser().getId() : 0,
                p.getName(),
                p.getData() != null ? p.getData().toString() : null,
                tagNames,
                p.isPublic(),
                p.getDescription(),
                commentDTOs,
                ratingDTOs,
                p.getArea() != null ? (long) p.getArea().getId() : null,
                List.of(), 
                p.getUser() != null ? p.getUser().getName() : null,
                p.isVisible()
            );

            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ResponseDTO(false, "Error fetching publication: " + e.getMessage()))
                .build();
        }
    }

    @GET
    public List<PublicationListDTO> getAllPublications() {

        TypedQuery<Publication> query = entityManager.createQuery(
                "SELECT p FROM Publication p",
                Publication.class
        );

        return query.getResultList()
                .stream()
                .map(p -> {

                    int commentsCount = 0;
                    if (p.getComments() != null) {
                        commentsCount = (int) p.getComments().stream()
                                .filter(Comment::isVisible)
                                .count();
                    }

                    List<String> tagNames = p.getTags() == null
                            ? List.of()
                            : p.getTags().stream()
                            .filter(Tag::isVisible)
                            .map(Tag::getName)
                            .collect(Collectors.toList());

                    int ratingCount = 0;
                    double averageRating = 0.0;
                    if (p.getRatings() != null && !p.getRatings().isEmpty()) {
                        ratingCount = p.getRatings().size();
                        double sum = p.getRatings().stream()
                                .mapToInt(Rating::getValue)
                                .sum();
                        averageRating = sum / ratingCount;
                    }

                    return new PublicationListDTO(
                            p.getId(),
                            p.getName(),
                            p.getDescription(),
                            p.getData() != null ? p.getData().toString() : null,
                            p.getArea() != null ? p.getArea().getName() : null,
                            commentsCount,
                            tagNames,
                            averageRating,
                            ratingCount,
                            p.isPublic(),
                            p.getUser() != null ? p.getUser().getUsername() : null
                    );
                })
                .collect(Collectors.toList());
    }



    @GET
    @Path("/visible")
    public List<PublicationListDTO> getVisiblePublications() {

        TypedQuery<Publication> query = entityManager.createQuery(
                "SELECT p FROM Publication p WHERE p.isPublic = true",
                Publication.class
        );

        return query.getResultList()
                .stream()
                .map(p -> {

                    int commentsCount = 0;
                    if (p.getComments() != null) {
                        commentsCount = (int) p.getComments().stream()
                                .filter(Comment::isVisible)
                                .count();
                    }

                    List<String> tagNames = p.getTags() == null
                            ? List.of()
                            : p.getTags().stream()
                            .filter(Tag::isVisible)
                            .map(Tag::getName)
                            .collect(Collectors.toList());

                    int ratingCount = 0;
                    double averageRating = 0.0;
                    if (p.getRatings() != null && !p.getRatings().isEmpty()) {
                        ratingCount = p.getRatings().size();
                        double sum = p.getRatings().stream()
                                .mapToInt(Rating::getValue)
                                .sum();
                        averageRating = sum / ratingCount;
                    }

                    return new PublicationListDTO(
                            p.getId(),
                            p.getName(),
                            p.getDescription(),
                            p.getData() != null ? p.getData().toString() : null,
                            p.getArea() != null ? p.getArea().getName() : null,
                            commentsCount,
                            tagNames,
                            averageRating,
                            ratingCount,
                            p.isPublic(),
                            p.getUser() != null ? p.getUser().getUsername() : null
                    );
                })
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}/file")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, "application/pdf", "application/zip"})
    @PermitAll
    public Response getPublicationFile(@PathParam("id") long id) {
        Publication publication = entityManager.find(Publication.class, id);
        if (publication == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Document file = publication.getFile();
        if (file == null || file.getFilepath() == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        java.nio.file.Path path = java.nio.file.Paths.get(file.getFilepath());
        if (!java.nio.file.Files.exists(path)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String filename = file.getFilename() != null ? file.getFilename() : path.getFileName().toString();
        
        String contentType = MediaType.APPLICATION_OCTET_STREAM;
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (lowerFilename.endsWith(".zip")) {
            contentType = "application/zip";
        }

        return Response.ok(path.toFile())
            .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
            .header("Content-Type", contentType)
            .build();
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

    private String getFormFieldValue(Map<String, List<InputPart>> formDataMap, String fieldName) throws IOException {
        if (!formDataMap.containsKey(fieldName)) {
            return null;
        }
        List<InputPart> parts = formDataMap.get(fieldName);
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        return parts.get(0).getBodyAsString();
    }

    private Long getFormFieldLongValue(Map<String, List<InputPart>> formDataMap, String fieldName) throws IOException {
        String value = getFormFieldValue(formDataMap, fieldName);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getFormFieldIntegerValue(Map<String, List<InputPart>> formDataMap, String fieldName) throws IOException {
        String value = getFormFieldValue(formDataMap, fieldName);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<Long> getFormFieldLongList(Map<String, List<InputPart>> formDataMap, String fieldName) throws IOException {
        if (!formDataMap.containsKey(fieldName)) {
            return List.of();
        }
        List<InputPart> parts = formDataMap.get(fieldName);
        if (parts == null || parts.isEmpty()) {
            return List.of();
        }
        
        List<Long> result = new java.util.ArrayList<>();
        for (InputPart part : parts) {
            String value = part.getBodyAsString();
            if (value != null && !value.isBlank()) {
                try {
                    result.add(Long.parseLong(value));
                } catch (NumberFormatException e) {
                    //skip
                }
            }
        }
        return result;
    }

    private static class CommentResponseDTO {
        private long id;
        private String text;
        private String date;
        private long user;
        private String userName;
        private boolean visible;

        public CommentResponseDTO(long id, String text, String date, long user, String userName, boolean visible) {
            this.id = id;
            this.text = text;
            this.date = date;
            this.user = user;
            this.userName = userName;
            this.visible = visible;
        }

        public long getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public String getDate() {
            return date;
        }

        public long getUser() {
            return user;
        }

        public String getUserName() {
            return userName;
        }

        public boolean isVisible() {
            return visible;
        }
    }
}


