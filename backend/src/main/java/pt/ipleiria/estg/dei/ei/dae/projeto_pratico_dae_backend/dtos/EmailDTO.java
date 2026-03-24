package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos;

import java.util.ArrayList;
import java.util.List;

public class EmailDTO {
	private List<String> recipients = new ArrayList<>();
	private String subject;
	private String body;
	private Boolean html = false;
	private Long publicationId;
	private String tagName;
	private String action;

	public EmailDTO() {
	}

	public EmailDTO(List<String> recipients, String subject, String body) {
		this.recipients = recipients;
		this.subject = subject;
		this.body = body;
	}

	public List<String> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<String> recipients) {
		this.recipients = recipients;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Boolean getHtml() {
		return html;
	}

	public void setHtml(Boolean html) {
		this.html = html;
	}

	public Long getPublicationId() {
		return publicationId;
	}

	public void setPublicationId(Long publicationId) {
		this.publicationId = publicationId;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
