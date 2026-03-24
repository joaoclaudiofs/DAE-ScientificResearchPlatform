package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.ResumoRequestDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.ResumoResponseDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.IABean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;

@Path("/ia")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class IAService {

    @EJB
    private IABean iaBean;

    @EJB
    private PublicationBean publicationBean;

    @POST
    @Path("/summary")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response summarize(ResumoRequestDTO dto) {
        String title;
        String text;

        if (dto.getPublicationId() != null) {
            Publication p = publicationBean.find(dto.getPublicationId());
            if (p == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            title = p.getName();
            text = p.getDescription();
        } else {
            title = dto.getTitle();
            text = dto.getText();
        }

        if (title == null) {
            title = "";
        }
        if (text == null || text.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Texto para resumir em falta").build();
        }

        String summary = iaBean.summarize(title, text);
        ResumoResponseDTO out = new ResumoResponseDTO(summary);
        return Response.ok(out).build();
    }
}
