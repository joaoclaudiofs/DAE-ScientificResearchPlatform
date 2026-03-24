package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class CommentCreateDTO {
    private String text;

    public CommentCreateDTO() {
    }

    public CommentCreateDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
