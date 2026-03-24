package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.*;
import java.time.LocalDateTime;

public class PublicationDTO {
    private String name;
    private LocalDateTime uploadDate; 
    private Tag tags;
    private String description;
    private boolean isPublic;
    private boolean isVisible;
    private Comment comments;
    private Rating ratings;
    private Document file;
    private Area area;
    private Publication publicationHistoric;

    public PublicationDTO(String name, LocalDateTime uploadDate, Tag tags, String description, boolean isPublic, Comment comments, Rating ratings, Document file, Area area, Publication publicationHistoric, boolean isVisible) {
        this.name = name;
        this.uploadDate = uploadDate;
        this.tags = tags;
        this.description = description;
        this.isPublic = isPublic;
        this.comments = comments;
        this.ratings = ratings;
        this.file = file;
        this.area = area;
        this.publicationHistoric = publicationHistoric;
        this.isVisible = isVisible;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public Tag getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public Comment getComments() {
        return comments;
    }

    public Rating getRatings() {
        return ratings;
    }

    public Document getFile() {
        return file;
    }

    public Area getArea() {
        return area;
    }

    public Publication getPublicationHistoric() {
        return publicationHistoric;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setTags(Tag tags) {
        this.tags = tags;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public void setComments(Comment comments) {
        this.comments = comments;
    }

    public void setRatings(Rating ratings) {
        this.ratings = ratings;
    }

    public void setFile(Document file) {
        this.file = file;
    }

    public void setArea(Area area) {
        this.area = area;
    }
    
    public void setPublicationHistoric(Publication publicationHistoric) {
        this.publicationHistoric = publicationHistoric;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }
}
