package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Area;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Document;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Role;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;

import java.time.LocalDateTime;

@Startup
@Singleton
public class ConfigBean {
    @EJB
    private RoleBean roleBean;

    @EJB
    private UserBean userBean;

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    public void populateDB() {
        createRoleIfMissing("ADMINISTRADOR");
        createRoleIfMissing("RESPONSAVEL");
        createRoleIfMissing("COLABORADOR");

        try {
            Role adminRole = roleBean.find("ADMINISTRADOR");
            Role responsavelRole = roleBean.find("RESPONSAVEL");
            Role colaboradorRole = roleBean.find("COLABORADOR");

            var admin = userBean.find("admin@mail.pt");
            if (admin == null) {
                userBean.create(
                    "Admin Demo",
                    "admin@mail.pt",
                    "123",
                    adminRole
                );
            } else {
                userBean.update(
                    admin.getId(),
                    admin.getName(),
                    admin.getEmail(),
                    "123",
                    adminRole
                );
            }

            var responsavel = userBean.find("r@mail.pt");
            if (responsavel == null) {
                userBean.create(
                    "Responsável Demo",
                    "r@mail.pt",
                    "123",
                    responsavelRole
                );
            } else {
                userBean.update(
                    responsavel.getId(),
                    responsavel.getName(),
                    responsavel.getEmail(),
                    "123",
                    responsavelRole
                );
            }

            var colaborador = userBean.find("colab@mail.pt");
            if (colaborador == null) {
                colaborador = userBean.create(
                    "Colaborador Demo",
                    "colab@mail.pt",
                    "123",
                    colaboradorRole
                );
            } else {
                userBean.update(
                    colaborador.getId(),
                    colaborador.getName(),
                    colaborador.getEmail(),
                    "123",
                    colaboradorRole
                );
            }

            Area areaIA = new Area( "Inteligência Artificial");
            Area areaEngSoft = new Area("Engenharia de Software");
            Area areaWeb = new Area("Desenvolvimento Web");

            entityManager.persist(areaIA);
            entityManager.persist(areaEngSoft);
            entityManager.persist(areaWeb);

            if (colaborador != null) {
                Document doc1 = new Document("relatorio-demo.pdf", "/uploads/relatorio-demo.pdf", colaborador);
                Document doc2 = new Document("artigo-exemplo.pdf", "/uploads/artigo-exemplo.pdf", colaborador);

                entityManager.persist(doc1);
                entityManager.persist(doc2);

                Publication pub1 = new Publication(
                    "Aplicação de IA na classificação de documentos",
                    java.time.LocalDateTime.of(2025, 12, 1, 10, 30),
                    "Estudo sobre o uso de modelos de linguagem para auxiliar na classificação automática de documentos académicos.",
                    true,
                    doc1,
                    areaIA,
                    colaborador
                );
                pub1.setVisible(true);

                Publication pub2 = new Publication(
                    "Arquiteturas RESTful em aplicações empresariais",
                    java.time.LocalDateTime.of(2025, 11, 20, 14, 15),
                    "Discussão de boas práticas na construção de APIs RESTful com foco em segurança, escalabilidade e manutenção.",
                    true,
                    doc2,
                    areaEngSoft,
                    colaborador
                );
                pub2.setVisible(true);

                entityManager.persist(pub1);
                entityManager.persist(pub2);

                Tag tagIA = new Tag("Inteligência Artificial");
                Tag tagNLP = new Tag("NLP");
                Tag tagArquiteturas = new Tag("Arquiteturas");
                Tag tagREST = new Tag("REST");

                Tag tagOculta = new Tag("Tag Oculta");
                tagOculta.setVisible(false);

                entityManager.persist(tagIA);
                entityManager.persist(tagNLP);
                entityManager.persist(tagArquiteturas);
                entityManager.persist(tagREST);
                entityManager.persist(tagOculta);

                pub1.getTags().add(tagIA);
                pub1.getTags().add(tagNLP);

                pub2.getTags().add(tagArquiteturas);
                pub2.getTags().add(tagREST);

                colaborador.getTags().add(tagIA);
                colaborador.getTags().add(tagNLP);
                
                admin.getTags().add(tagArquiteturas);
                admin.getTags().add(tagREST);
                
                responsavel.getTags().add(tagIA);
                responsavel.getTags().add(tagArquiteturas);

                Comment c1 = new Comment(
                    pub1,
                    colaborador,
                    "Comentário de exemplo visível sobre a publicação de IA.",
                    LocalDateTime.now().minusDays(1),
                    true
                );

                Comment c2 = new Comment(
                    pub1,
                    colaborador,
                    "Comentário que começa oculto para moderação.",
                    LocalDateTime.now().minusHours(2),
                    false
                );

                entityManager.persist(c1);
                entityManager.persist(c2);

                Rating r1 = new Rating(
                    pub1,
                    colaborador,
                    4,
                    LocalDateTime.now().minusDays(2)
                );

                Rating r2 = new Rating(
                    pub1,
                    colaborador,
                    5,
                    LocalDateTime.now().minusDays(1)
                );

                Rating r3 = new Rating(
                    pub2,
                    colaborador,
                    3,
                    LocalDateTime.now().minusDays(3)
                );

                entityManager.persist(r1);
                entityManager.persist(r2);
                entityManager.persist(r3);

                Publication pubOculta = new Publication(
                    "Protótipo interno - API privada",
                    java.time.LocalDateTime.of(2025, 10, 5, 9, 45),
                    "Publicação criada apenas para testar o fluxo de publicações ocultas.",
                    true,
                    doc2,
                    areaWeb,
                    colaborador
                );
                pubOculta.setVisible(false);

                entityManager.persist(pubOculta);
            }
        } catch (Exception e) {
        }
    }

    private void createRoleIfMissing(String name) {
        Role role = roleBean.find(name);
        if (role == null) {
            roleBean.create(name);
        }
    }
}
