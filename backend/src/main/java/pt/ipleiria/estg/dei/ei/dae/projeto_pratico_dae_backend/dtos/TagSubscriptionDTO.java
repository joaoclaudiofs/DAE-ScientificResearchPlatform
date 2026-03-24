package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class TagSubscriptionDTO {
    private Long tag;

    public TagSubscriptionDTO() {}

    public TagSubscriptionDTO(Long tag) {
        this.tag = tag;
    }

    public Long getTag() {
        return tag;
    }

    public void setTag(Long tag) {
        this.tag = tag;
    }
}
