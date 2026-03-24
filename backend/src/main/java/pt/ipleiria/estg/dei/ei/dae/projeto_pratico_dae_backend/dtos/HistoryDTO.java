package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.History;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryDTO {
    private String date;
    private String action;
    private String endpoint;
    private String body;

    public HistoryDTO() {
    }

    public HistoryDTO(String date, String action, String endpoint, String body) {
        this.date = date;
        this.action = action;
        this.endpoint = endpoint;
        this.body = body;
    }

    public static HistoryDTO from(History history) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return new HistoryDTO(
                history.getDate().format(formatter),
                history.getAction(),
                history.getEndpoint(),
                history.getBody()
        );
    }

    public static List<HistoryDTO> from(List<History> historyList) {
        return historyList.stream().map(HistoryDTO::from).collect(Collectors.toList());
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
