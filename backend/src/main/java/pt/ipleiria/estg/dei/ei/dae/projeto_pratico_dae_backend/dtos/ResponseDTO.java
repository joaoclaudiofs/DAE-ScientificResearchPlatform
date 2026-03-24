package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class ResponseDTO {
    private boolean success;
    private String message;

    public ResponseDTO() {

    }

    public ResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
