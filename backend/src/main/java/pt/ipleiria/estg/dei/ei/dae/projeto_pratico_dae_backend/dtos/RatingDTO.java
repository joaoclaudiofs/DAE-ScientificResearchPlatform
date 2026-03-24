package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class RatingDTO {
    private int value;

    public RatingDTO() {
    }

    public RatingDTO(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
