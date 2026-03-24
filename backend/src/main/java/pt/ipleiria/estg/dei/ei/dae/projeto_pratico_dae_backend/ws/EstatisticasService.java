package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Area;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Stateless
@PermitAll
@Path("/statistics")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class EstatisticasService {

	@PersistenceContext
	private EntityManager entityManager;

	@GET
	@Path("/publications-by-area")
	public Map<String, Integer> getPublicacoesPorArea() {
		List<Area> areas = entityManager.createQuery("SELECT a FROM Area a", Area.class).getResultList();
		Map<String, Integer> result = new HashMap<>();
		for (Area area : areas) {
			int count = area.getPublications() != null ? area.getPublications().size() : 0;
			result.put(area.getName(), count);
		}
		return result;
	}

	@GET
	@Path("/average-rating-by-area")
	public Map<String, Double> getMediaRatingPorArea() {
		List<Area> areas = entityManager.createQuery("SELECT a FROM Area a", Area.class).getResultList();
		Map<String, Double> result = new HashMap<>();
		for (Area area : areas) {
			List<Publication> pubs = area.getPublications();
			List<Rating> ratings = pubs.stream().flatMap(p -> p.getRatings() != null ? p.getRatings().stream() : Stream.empty()).collect(Collectors.toList());
			double avg = ratings.isEmpty() ? 0.0 : ratings.stream().mapToInt(Rating::getValue).average().orElse(0.0);
			result.put(area.getName(), avg);
		}
		return result;
	}

	@GET
	@Path("/average-rating-by-tag")
	public Map<String, Double> getMediaRatingPorTag() {
		List<Tag> tags = entityManager.createQuery("SELECT t FROM Tag t", Tag.class).getResultList();
		Map<String, Double> result = new HashMap<>();
		for (Tag tag : tags) {
			List<Publication> pubs = new ArrayList<>();
			try {
				//se existir método getPublications() na Tag
				java.lang.reflect.Method m = tag.getClass().getMethod("getPublications");
				Object pubsObj = m.invoke(tag);
				if (pubsObj instanceof Collection<?>) {
					for (Object o : (Collection<?>)pubsObj) {
						if (o instanceof Publication) {
							pubs.add((Publication)o);
						}
					}
				}
			} catch (Exception e) {
			}
			List<Rating> ratings = pubs.stream().flatMap(p -> p.getRatings() != null ? p.getRatings().stream() : Stream.empty()).collect(Collectors.toList());
			double avg = ratings.isEmpty() ? 0.0 : ratings.stream().mapToInt(Rating::getValue).average().orElse(0.0);
			result.put(tag.getName(), avg);
		}
		return result;
	}

	//por mes 
	@GET
	@Path("/evolution-submissions")
	public Map<String, Integer> getEvolucaoSubmissoes() {
		List<Publication> pubs = entityManager.createQuery("SELECT p FROM Publication p", Publication.class).getResultList();
		Map<String, Integer> result = new TreeMap<>();
		for (Publication pub : pubs) {
			if (pub.getData() != null) {
				String key = pub.getData().getYear() + "-" + String.format("%02d", pub.getData().getMonthValue());
				result.put(key, result.getOrDefault(key, 0) + 1);
			}
		}
		return result;
	}
}