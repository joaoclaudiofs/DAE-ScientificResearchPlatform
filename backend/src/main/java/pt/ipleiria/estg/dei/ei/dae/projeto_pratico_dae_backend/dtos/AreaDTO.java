package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class AreaDTO {

    private int id;
    private String name;

    public AreaDTO() {
    }

    public AreaDTO(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
