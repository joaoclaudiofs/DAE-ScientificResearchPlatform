package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class ResumoResponseDTO {
    private String summary;

    public ResumoResponseDTO() {
    }

    public ResumoResponseDTO(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
