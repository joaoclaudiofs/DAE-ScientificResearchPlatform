package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class AreaCreateDTO {

    private String name;

    public AreaCreateDTO() {
    }

    public AreaCreateDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
