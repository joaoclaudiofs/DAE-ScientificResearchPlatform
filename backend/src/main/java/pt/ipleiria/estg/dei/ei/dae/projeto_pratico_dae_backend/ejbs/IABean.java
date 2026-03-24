package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.io.StringReader;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Stateless
public class IABean {
    private String aiBaseUrl; 
    private String aiModel; 

    @PostConstruct
    private void init() {
        String envBaseUrl = System.getenv("AI_BASE_URL");
        String envModel = System.getenv("AI_MODEL");

        this.aiBaseUrl = (envBaseUrl != null && !envBaseUrl.isBlank())
            ? envBaseUrl
            : "http://ollama:11434";

        this.aiModel = (envModel != null && !envModel.isBlank())
            ? envModel
            : "llama3";
    }

    public String summarize(String title, String text) {
        Client client = ClientBuilder.newClient();
        try {
            JsonObject request = Json.createObjectBuilder()
                .add("model", aiModel)
                .add("stream", false)
                .add(
                        "prompt",
                        "Regras de Output: Não incluas saudações nem frases introdutórias (como 'Aqui está o resumo').\n"
                                + "Não incluas conclusões.\n"
                                + "Mantém o tom original do texto.\n"
                                + "O resumo deve ser conciso e direto ao ponto.\n\n"
                                + "Faz um resumo curto e claro em português desta publicação.\n"
                                + "Não adiciones \"Resumo:\"\n"
                                + "Título: " + title + "\n"
                                + "Texto: " + text
                )
                .build();


            Response resp = client
                .target(aiBaseUrl)
                .path("/api/generate")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(request));

            String body = resp.readEntity(String.class);

            if (resp.getStatus() != 200) {
                throw new IllegalStateException("Erro na IA: HTTP " + resp.getStatus() + " - " + body);
            }

            try {
                JsonObject json = Json.createReader(new StringReader(body)).readObject();
                if (json.containsKey("response")) return json.getString("response");
                if (json.containsKey("text")) return json.getString("text");
                return json.toString();
            } catch (Exception ex) {
                return body;
            }
        } finally {
            client.close();
        }
    }
}
