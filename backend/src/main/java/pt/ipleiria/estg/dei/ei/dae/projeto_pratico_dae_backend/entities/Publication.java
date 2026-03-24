package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    private boolean visible;

    private LocalDateTime data;

    @ManyToMany
    @JoinTable(
        name = "publication_tags",
        joinColumns = @JoinColumn(name = "publication_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(length = 4000)
    private String description;

    private boolean isPublic;

    @OneToMany(mappedBy = "publication")
    private List<Comment> comments;

    @OneToMany(mappedBy = "publication")
    private List<Rating> ratings;

    @ManyToOne
    private Document file;

    @ManyToOne
    private Area area;

    @ManyToOne
    private User user;

    public Publication() {
    }

    public Publication(String name,
                       LocalDateTime data,
                       String description,
                       boolean isPublic,
                       Document file,
                       Area area,
                       User user) {
        this.name = name;
        this.data = data;
        this.description = description;
        this.isPublic = isPublic;
        this.file = file;
        this.area = area;
        this.user = user;
        this.visible = true;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    public Document getFile() {
        return file;
    }

    public void setFile(Document file) {
        this.file = file;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}