package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.HistoryDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.ResponseDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.HistoryBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.History;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;

import java.util.List;

@Path("history")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class HistoryService {
    @EJB
    private HistoryBean historyBean;

    @EJB
    private UserBean userBean;

    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/me")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getMyHistory() {
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

            List<History> history = historyBean.getHistoryByUser(user.getId());
            return Response.ok(HistoryDTO.from(history)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{userId}")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response getUserHistory(@PathParam("userId") long userId) {
        try {
            User user = userBean.find(userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "User not found"))
                        .build();
            }

            List<History> history = historyBean.getHistoryByUser(userId);
            return Response.ok(HistoryDTO.from(history)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        }
    }
}
