package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Role;

import java.util.List;
import java.util.stream.Collectors;

public class RoleDTO {
    private int id;
    private String name;

    public RoleDTO() {
    }

    public RoleDTO(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static RoleDTO from(Role role) {
        return new RoleDTO(role.getId(), role.getName());
    }

    public static List<RoleDTO> from(List<Role> roles) {
        return roles.stream().map(RoleDTO::from).collect(Collectors.toList());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
