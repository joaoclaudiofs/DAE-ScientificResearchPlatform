package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.AuthDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.RecoverPasswordDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.ResponseDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.TokenIssuer;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class AuthService {

    @EJB
    private UserBean userBean;

    @EJB
    private pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.HistoryBean historyBean;

    @EJB
    private pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.EmailBean emailBean;

    @Context
    private SecurityContext securityContext;

    public static class LoginResponse {
        public String token;
        public Long id;
        public String username;
        public String email;
        public String name;
        public String role;

        public LoginResponse(String token, Long id, String username, String email, String name, String role) {
            this.token = token;
            this.id = id;
            this.username = username;
            this.email = email;
            this.name = name;
            this.role = role;
        }
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(AuthDTO authDTO) {
        if (authDTO == null || authDTO.getUsername() == null || authDTO.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Username and password are required").build();
        }

        boolean canLogin = userBean.canLogin(authDTO.getUsername(), authDTO.getPassword());
        if (!canLogin) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }

        User user = userBean.find(authDTO.getUsername());
        String token = TokenIssuer.issue(authDTO.getUsername());

        historyBean.create(user, "POST", "/auth/login", null);

        LoginResponse response = new LoginResponse(
            token,
            user.getId(),
            user.getEmail(),
            user.getEmail(),
            user.getName(),
            user.getRole() != null ? user.getRole().getName() : null
        );

        return Response.ok(response).build();
    }

    @POST
    @Path("/logout")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response logout() {
        try {
            var principal = securityContext.getUserPrincipal();
            if (principal == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ResponseDTO(false, "User not authenticated"))
                        .build();
            }

            User user = userBean.find(principal.getName());
            if (user != null) {
                historyBean.create(user, "POST", "/auth/logout", null);
            }

            return Response.ok()
                    .entity(new ResponseDTO(true, "You have been logged out"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseDTO(false, "Error during logout: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/recover")
    @PermitAll
    public Response recoverPassword(RecoverPasswordDTO dto) {
        try {
            if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseDTO(false, "Email is required"))
                        .build();
            }

            String email = dto.getEmail();
            User user = userBean.find(email);
            if (user == null) {
                historyBean.create(null, "POST", "/auth/recover", email);
                return Response.ok()
                        .entity(new ResponseDTO(true, "A password recovery link has been sent"))
                        .build();
            }

            String temporaryPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
            userBean.updatePassword(user.getId(), temporaryPassword);

            String subject = "Recuperação de palavra-passe";
            String body = "A nova palavra-passe é: " + temporaryPassword;
            emailBean.send(email, subject, body);

            historyBean.create(user, "POST", "/auth/recover", null);

            return Response.ok()
                    .entity(new ResponseDTO(true, "If the email exists, a password recovery link has been sent"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseDTO(false, "Error during password recovery: " + e.getMessage()))
                    .build();
        }
    }
}
