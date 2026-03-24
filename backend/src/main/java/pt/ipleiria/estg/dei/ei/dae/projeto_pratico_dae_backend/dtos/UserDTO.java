package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Role;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private Integer role;
    private List<Long> tags;
    private boolean active;

    public UserDTO() {}

    public UserDTO(String name, String email, String password) {
        this.name = name;
        this.email = email;
    }

    public UserDTO(Long id, String name, String email, Integer role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public UserDTO(Long id, String name, String email, String password, Integer role, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    public UserDTO(Long id, String name, String email, Integer role, List<Long> tags, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.tags = tags;
        this.active = active;
    }

    public UserDTO(Long id, String name, String email, String password, Integer role, List<Long> tags, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.tags = tags;
        this.active = active;
    }

    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getId(),
                user.getTags().stream().map(Tag::getId).collect(Collectors.toList()),
                user.isActive()
        );
    }

    public static List<UserDTO> from(List<User> users) {
        return users.stream().map(UserDTO::from).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Long> getTags() {
        return tags;
    }

    public void setTags(List<Long> tags) {
        this.tags = tags;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}