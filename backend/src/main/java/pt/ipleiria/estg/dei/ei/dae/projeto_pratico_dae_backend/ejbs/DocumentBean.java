package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Document;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Stateless
public class DocumentBean {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String UPLOAD_DIR = "uploads";
    private static final String[] ALLOWED_EXTENSIONS = {".pdf", ".zip"};

    public boolean isValidFileType(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }
        String lowerFilename = filename.toLowerCase();
        for (String extension : ALLOWED_EXTENSIONS) {
            if (lowerFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public long create(String filename, InputStream fileInputStream, User user) throws IOException {
        if (!isValidFileType(filename)) {
            throw new IllegalArgumentException("Invalid file type. Only PDF and ZIP files are allowed");
        }

        java.nio.file.Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String uniqueFilename = System.currentTimeMillis() + "_" + filename;
        java.nio.file.Path filePath = uploadPath.resolve(uniqueFilename);

        Files.copy(fileInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

        Document document = new Document(filename, filePath.toString(), user);
        entityManager.persist(document);
        entityManager.flush();

        return document.getId();
    }

    public Document find(long id) {
        return entityManager.find(Document.class, id);
    }

    public void delete(long id) throws IOException {
        Document document = find(id);
        if (document != null) {
            if (document.getFilepath() != null) {
                java.nio.file.Path filePath = Paths.get(document.getFilepath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            }
            entityManager.remove(document);
        }
    }

    public String getFilepath(long id) {
        Document document = find(id);
        return document != null ? document.getFilepath() : null;
    }

    public String getFilename(long id) {
        Document document = find(id);
        return document != null ? document.getFilename() : null;
    }
}
