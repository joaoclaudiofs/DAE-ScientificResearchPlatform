package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.ResponseDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.PasswordDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.TagSubscriptionDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.UpdateEmailDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.HistoryBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.RoleBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Role;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;

import java.util.List;

@Path("users")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class UserService {
    @EJB
    private UserBean userBean;
    
    @EJB
    private RoleBean roleBean;

    @EJB
    private HistoryBean historyBean;

    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/")
    @PermitAll
    public List<UserDTO> getAllUsers() {
        return UserDTO.from(userBean.findAll());
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public Response getUser(@PathParam("id") long id) {
        User user = userBean.find(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "User not found"))
                    .build();
        }

        return Response.ok(UserDTO.from(user)).build();
    }

    @POST
    @Path("/")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response createNewUser (UserDTO userDTO) {
        try {
            var principal = securityContext.getUserPrincipal();
            User currentUser = principal != null ? userBean.find(principal.getName()) : null;

            Role colabRole = roleBean.find("COLABORADOR");
            
            userBean.create(
                    userDTO.getName(),
                    userDTO.getEmail(),
                    userDTO.getPassword(),
                    colabRole
            );

            if (currentUser != null) {
                historyBean.create(currentUser, "POST", "/users", 
                    String.format("{\"name\":\"%s\",\"email\":\"%s\"}", userDTO.getName(), userDTO.getEmail()));
            }

            return Response.status(Response.Status.CREATED)
                    .entity(new ResponseDTO(
                            true,
                            "The user " + userDTO.getName() + " has been created"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(
                            false,
                            e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response deleteUser(@PathParam("id") long id) {
        User user = userBean.find(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(
                            false,
                            "User not found"))
                    .build();
        }

        var principal = securityContext.getUserPrincipal();
        User currentUser = principal != null ? userBean.find(principal.getName()) : null;
        String userName = user.getName();

        userBean.delete(id);

        if (currentUser != null) {
            historyBean.create(currentUser, "DELETE", "/users/" + id, null);
        }

        return Response.ok(
                new ResponseDTO(
                    true,
                    "The user " + userName + " has been deleted"))
                .build();
    }

    @PUT
    @Path("/{id}/active")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response setUserActiveStatus(@PathParam("id") long id, UserDTO userDTO) {
        User user = userBean.find(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseDTO(false, "User not found"))
                    .build();
        }

        var principal = securityContext.getUserPrincipal();
        User currentUser = principal != null ? userBean.find(principal.getName()) : null;

        userBean.setActive(id, userDTO.isActive());

        if (currentUser != null) {
            historyBean.create(currentUser, "PUT", "/users/" + id + "/active", 
                String.format("{\"active\":%b}", userDTO.isActive()));
        }

        String action = userDTO.isActive() ? "activated" : "deactivated";
        return Response.ok(new ResponseDTO(true, "The user " + user.getName() + " has been " + action)).build();
    }

    @PUT
    @Path("/{id}/role")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response setUserRole(@PathParam("id") long id, UserDTO userDTO) {
        try {
            User user = userBean.find(id);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "User not found"))
                        .build();
            }

            var principal = securityContext.getUserPrincipal();
            User currentUser = principal != null ? userBean.find(principal.getName()) : null;

            Role role = roleBean.find(userDTO.getRole());
            userBean.setRole(id, role);

            if (currentUser != null) {
                historyBean.create(currentUser, "PUT", "/users/" + id + "/role", 
                    String.format("{\"role\":%d}", userDTO.getRole()));
            }

            return Response.ok(new ResponseDTO(true, "The user " + user.getName() + "'s role has been changed")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        }
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response updateUser(@PathParam("id") long id, UserDTO userDTO) {
        try {
            User user = userBean.find(id);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "User not found"))
                        .build();
            }

            var principal = securityContext.getUserPrincipal();
            User currentUser = principal != null ? userBean.find(principal.getName()) : null;

            Role role = roleBean.find(userDTO.getRole());
            if (role == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseDTO(false, "Role not found"))
                        .build();
            }

            userBean.updateUser(
                    id,
                    userDTO.getName(),
                    userDTO.getEmail(),
                    role,
                    userDTO.isActive()
            );

            if (currentUser != null) {
                historyBean.create(currentUser, "PATCH", "/users/" + id, 
                    String.format("{\"name\":\"%s\",\"email\":\"%s\",\"role\":%d,\"active\":%b}", 
                        userDTO.getName(), userDTO.getEmail(), userDTO.getRole(), userDTO.isActive()));
            }

            return Response.ok(new ResponseDTO(true, "The user " + user.getName() + " has been updated")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("me/tags")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response subscribeToTag(TagSubscriptionDTO tagSubscriptionDTO) {
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
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "User not found"))
                        .build();
            }

            userBean.subscribeTag(username, tagSubscriptionDTO.getTag());

            historyBean.create(user, "POST", "/users/me/tags", 
                String.format("{\"tag\":%d}", tagSubscriptionDTO.getTag()));

            return Response.status(Response.Status.CREATED)
                    .entity(new ResponseDTO(true, "Subscribed to the tag"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("me/tags/{tagId}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response unsubscribeFromTag(@PathParam("tagId") long tagId) {
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
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "User not found"))
                        .build();
            }

            userBean.unsubscribeTag(username, tagId);

            historyBean.create(user, "DELETE", "/users/me/tags/" + tagId, null);

            return Response.ok(new ResponseDTO(true, "Unsubscribed from the tag"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        }
    }

    @PATCH
    @Path("/me/password")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response updateMyPassword(PasswordDTO passwordDTO) {
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
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "User not found"))
                        .build();
            }

            userBean.updatePassword(user.getId(), passwordDTO.getPassword());

            historyBean.create(user, "PATCH", "/users/me/password", null);

            return Response.ok(new ResponseDTO(true, "Password updated successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        }
    }

    @PATCH
    @Path("/me/email")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response updateMyEmail(UpdateEmailDTO emailDTO) {
        try {
            String email = securityContext.getUserPrincipal().getName();
            User user = userBean.find(email);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "User not found"))
                        .build();
            }

            userBean.updateEmail(user.getId(), emailDTO.getEmail());

            historyBean.create(user, "PATCH", "/users/me/email", 
                String.format("{\"email\":\"%s\"}", emailDTO.getEmail()));

            return Response.ok(new ResponseDTO(true, "Email updated successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        }
    }
}

