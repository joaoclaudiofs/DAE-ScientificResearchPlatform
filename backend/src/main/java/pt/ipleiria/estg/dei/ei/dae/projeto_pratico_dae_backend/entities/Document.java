package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String filename;

    private String filepath;

    @ManyToOne
    private User user;

    public Document() {
    }

    public Document(String filename, String filepath, User user) {
        this.filename = filename;
        this.filepath = filepath;
        this.user = user;
    }


    public long getId() {
        return id;
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}