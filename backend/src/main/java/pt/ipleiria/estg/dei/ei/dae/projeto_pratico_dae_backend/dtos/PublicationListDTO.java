package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PublicationListDTO {
    private long id;
    private String name;
    private String description;
    private String data;
    private String areaName;
    private int commentsCount;
    private List<String> tags;
    private double averageRating;
    private int ratingCount;
    private Boolean isPublic;
    private String userName;

    public PublicationListDTO(long id,
                              String name,
                              String description,
                              String data,
                              String areaName,
                              int commentsCount,
                              List<String> tags,
                              double averageRating,
                              int ratingCount,
                              Boolean isPublic,
                              String userName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.data = data;
        this.areaName = areaName;
        this.commentsCount = commentsCount;
        this.tags = tags;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
        this.isPublic = isPublic;
        this.userName = userName;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getData() {
        return data;
    }

    public String getAreaName() {
        return areaName;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    @JsonProperty("public")
    public Boolean getIsPublic() {
        return isPublic;
    }

    public String getUserName() {
        return userName;
    }

}
