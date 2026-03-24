package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

import java.util.Objects;

public class DocumentDTO {
    
    private long id;
    private String filename;

    public DocumentDTO() {
    }

    public DocumentDTO(long id, String filename) {
        this.id = id;
        this.filename = filename;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "DocumentDTO{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentDTO)) return false;
        DocumentDTO that = (DocumentDTO) o;
        return id == that.id && Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filename);
    }

}
