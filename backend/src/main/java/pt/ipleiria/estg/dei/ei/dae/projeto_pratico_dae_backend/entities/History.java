package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "history")
@NamedQueries({
        @NamedQuery(
                name = "getHistoryByUser",
                query = "SELECT h FROM History h WHERE h.user.id = :userId ORDER BY h.date DESC"
        ),
        @NamedQuery(
                name = "getAllHistory",
                query = "SELECT h FROM History h ORDER BY h.date DESC"
        )
})
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String endpoint;

    @Column(columnDefinition = "TEXT")
    private String body;

    public History() {
    }

    public History(User user, String action, String endpoint, String body) {
        this.user = user;
        this.date = LocalDateTime.now();
        this.action = action;
        this.endpoint = endpoint;
        this.body = body;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
