package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.AreaCreateDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.AreaDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.ResponseDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.AreaBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Area;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;

import java.util.List;
import java.util.stream.Collectors;

@Stateless
@Path("/areas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class AreasService {

    @EJB
    private AreaBean areaBean;

    @EJB
    private pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.HistoryBean historyBean;

    @EJB
    private pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.UserBean userBean;

    @Context
    private SecurityContext securityContext;


    @GET
    public List<AreaDTO> getAllAreas() {
        return areaBean.findAll()
                .stream()
                .map(a -> new AreaDTO(a.getId(), a.getName()))
                .collect(Collectors.toList());
    }

    @POST
    public Response createArea(AreaCreateDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, "Area name is required"))
                    .build();
        }

        Area area = areaBean.create(dto.getName());

        var principal = securityContext.getUserPrincipal();
        if (principal != null) {
            pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User currentUser = userBean.find(principal.getName());
            if (currentUser != null) {
                historyBean.create(currentUser, "POST", "/areas",
                    String.format("{\"name\":\"%s\"}", dto.getName()));
            }
        }

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseDTO(true, "The area " + area.getName() + " has been created successfully"))
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteArea(@PathParam("id") int id) {
        try {
            Area area = areaBean.find(id);
            if (area == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "Area not found"))
                        .build();
            }

            String areaName = area.getName();
            areaBean.delete(id);

            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User currentUser = userBean.find(principal.getName());
                if (currentUser != null) {
                    historyBean.create(currentUser, "DELETE", "/areas/" + id, null);
                }
            }

            return Response.ok()
                    .entity(new ResponseDTO(true, "The area " + areaName + " has been deleted successfully"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("constraint")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseDTO(false, "Cannot delete area because it is being used by publications"))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseDTO(false, "Error deleting area: " + e.getMessage()))
                    .build();
        }
    }
}
