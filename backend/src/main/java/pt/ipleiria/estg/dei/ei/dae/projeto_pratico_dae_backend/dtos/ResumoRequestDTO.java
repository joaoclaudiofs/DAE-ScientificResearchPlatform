package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class ResumoRequestDTO {
    private Long publicationId;
    private String title;
    private String text;

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
