package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(optional = false)
    private Publication publication;

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private int value;

    @Column(nullable = false)
    private LocalDateTime date;

    public Rating() {
    }

    public Rating(Publication publication,
                  User user,
                  int value,
                  LocalDateTime date) {
        this.publication = publication;
        this.user = user;
        this.value = value;
        this.date = date;
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}