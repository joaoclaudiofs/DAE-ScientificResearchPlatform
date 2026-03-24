package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

import java.util.List;

public class PublicationCreateDTO {
    private String name;
    private List<Long> tags;
    private boolean isPublic;
    private String description;
    private Long area;

    public PublicationCreateDTO() {
    }

    public PublicationCreateDTO(String name, List<Long> tags, boolean isPublic, String description, Long area) {
        this.name = name;
        this.tags = tags;
        this.isPublic = isPublic;
        this.description = description;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getTags() {
        return tags;
    }

    public void setTags(List<Long> tags) {
        this.tags = tags;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getArea() {
        return area;
    }

    public void setArea(Long area) {
        this.area = area;
    }
}
