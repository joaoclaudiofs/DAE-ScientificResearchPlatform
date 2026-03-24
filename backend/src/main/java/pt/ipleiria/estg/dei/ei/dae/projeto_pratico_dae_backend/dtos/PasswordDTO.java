package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

public class PasswordDTO {
    private String password;

    public PasswordDTO() {
    }

    public PasswordDTO(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
