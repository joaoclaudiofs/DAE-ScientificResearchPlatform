package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class NotificationDTO {
    private long id;
    private String text;
    private String when;
    private boolean read;

    public NotificationDTO() {}

    public NotificationDTO(long id, String text, String when, boolean read) {
        this.id = id;
        this.text = text;
        this.when = when;
        this.read = read;
    }

    public long getId() { return id; }
    public String getText() { return text; }
    public String getWhen() { return when; }
    public boolean isRead() { return read; }
}
