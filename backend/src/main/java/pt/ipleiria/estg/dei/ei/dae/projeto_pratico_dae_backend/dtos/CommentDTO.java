package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class CommentDTO {
    private long id;
    private long publicationId;
    private String publicationName;
    private String author;
    private String text;
    private String date;
    private boolean visible;

    public CommentDTO(long id,
                      long publicationId,
                      String publicationName,
                      String author,
                      String text,
                      String date,
                      boolean visible) {
        this.id = id;
        this.publicationId = publicationId;
        this.publicationName = publicationName;
        this.author = author;
        this.text = text;
        this.date = date;
        this.visible = visible;
    }

    public long getId() {
        return id;
    }

    public long getPublicationId() {
        return publicationId;
    }

    public String getPublicationName() {
        return publicationName;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public boolean isVisible() {
        return visible;
    }
}
