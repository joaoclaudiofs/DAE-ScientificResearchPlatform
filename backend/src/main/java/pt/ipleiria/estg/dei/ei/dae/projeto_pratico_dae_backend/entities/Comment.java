package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(optional = false)
    private Publication publication;

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private boolean visible;

    public Comment() {
    }

    public Comment(Publication publication,
                   User user,
                   String text,
                   LocalDateTime date,
                   boolean visible) {
        this.publication = publication;
        this.user = user;
        this.text = text;
        this.date = date;
        this.visible = visible;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}