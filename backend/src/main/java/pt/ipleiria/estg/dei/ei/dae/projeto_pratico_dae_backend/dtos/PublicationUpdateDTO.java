package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class PublicationUpdateDTO {
    private String name;
    private String description;

    public PublicationUpdateDTO() {
    }

    public PublicationUpdateDTO(String name, String description, boolean isPublic) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
