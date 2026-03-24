package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.RoleDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.RoleBean;

import java.util.List;

@Path("roles")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class RoleService {
    @EJB
    private RoleBean roleBean;

    @GET
    @Path("/")
    @RolesAllowed({"ADMINISTRADOR", "RESPONSAVEL", "COLABORADOR"})
    public List<RoleDTO> getAllRoles() {
        return RoleDTO.from(roleBean.findAll());
    }
}
