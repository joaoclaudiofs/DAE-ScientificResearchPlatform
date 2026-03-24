package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

import java.util.List;

public class PublicationDetailDTO {
    private long id;
    private long user;
    private String name;
    private String date_created;
    private List<String> tags;
    private boolean isPublic;
    private String description;
    private List<CommentSummaryDTO> comments;
    private List<RatingSummaryDTO> ratings;
    private Long area;
    private List<String> history;
    private String userName;
    private boolean visible;

    public PublicationDetailDTO() {
    }

    public PublicationDetailDTO(long id, long user, String name, String date_created, 
                                List<String> tags, boolean isPublic, String description,
                                List<CommentSummaryDTO> comments, List<RatingSummaryDTO> ratings,
                                Long area, List<String> history, String userName, boolean visible) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.date_created = date_created;
        this.tags = tags;
        this.isPublic = isPublic;
        this.description = description;
        this.comments = comments;
        this.ratings = ratings;
        this.area = area;
        this.history = history;
        this.userName = userName;
        this.visible = visible;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CommentSummaryDTO> getComments() {
        return comments;
    }

    public void setComments(List<CommentSummaryDTO> comments) {
        this.comments = comments;
    }

    public List<RatingSummaryDTO> getRatings() {
        return ratings;
    }

    public void setRatings(List<RatingSummaryDTO> ratings) {
        this.ratings = ratings;
    }

    public Long getArea() {
        return area;
    }

    public void setArea(Long area) {
        this.area = area;
    }

    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isVisible() {
        return visible;
    }

    public static class CommentSummaryDTO {
        private long id;
        private String text;
        private String author;
        private String date;

        public CommentSummaryDTO(long id, String text, String author, String date) {
            this.id = id;
            this.text = text;
            this.author = author;
            this.date = date;
        }

        public long getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public String getAuthor() {
            return author;
        }

        public String getDate() {
            return date;
        }
    }

    public static class RatingSummaryDTO {
        private long id;
        private int value;
        private String author;
        private String date;

        public RatingSummaryDTO(long id, int value, String author, String date) {
            this.id = id;
            this.value = value;
            this.author = author;
            this.date = date;
        }

        public long getId() {
            return id;
        }

        public int getValue() {
            return value;
        }

        public String getAuthor() {
            return author;
        }

        public String getDate() {
            return date;
        }
    }
}
