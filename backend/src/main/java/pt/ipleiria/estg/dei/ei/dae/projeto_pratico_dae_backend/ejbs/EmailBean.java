package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.Asynchronous;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.EmailDTO;

@Stateless
public class EmailBean {
    @Resource(name = "java:/jboss/mail/fakeSMTP")
    private Session mailSession;
    public void send(String to, String subject, String text) throws
            MessagingException {
            Message message = new MimeMessage(getSession());
        try {
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to, false));
            message.setSubject(subject);
            message.setText(text);
            Date timeStamp = new Date();
            message.setSentDate(timeStamp);
            Transport.send(message);
        } catch (MessagingException e) {
            throw e;
        }
    }

    @Asynchronous
    public void send(EmailDTO dto) {
        Logger logger = Logger.getLogger(EmailBean.class.getName());
        if (dto == null) return;
        List<String> recipients = dto.getRecipients();
        try {
            Session session = getSession();
            String sessionHost = session.getProperty("mail.smtp.host");
            String sessionPort = session.getProperty("mail.smtp.port");
            logger.info("Preparing to send email. mailSession present=" + (mailSession != null) + ", smtpHost=" + sessionHost + ", smtpPort=" + sessionPort + ", recipients=" + (recipients == null ? "[]" : recipients.toString()) + ", subject=" + dto.getSubject());
            Message message = new MimeMessage(session);
        try {
            if (recipients != null && !recipients.isEmpty()) {
                InternetAddress[] addresses = recipients.stream()
                        .map(r -> {
                            try {
                                return new InternetAddress(r);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(a -> a != null)
                        .toArray(InternetAddress[]::new);
                message.setRecipients(Message.RecipientType.TO, addresses);
            }

            message.setSubject(dto.getSubject() != null ? dto.getSubject() : "");

            if (Boolean.TRUE.equals(dto.getHtml())) {
                message.setContent(dto.getBody() != null ? dto.getBody() : "", "text/html; charset=UTF-8");
            } else {
                message.setText(dto.getBody() != null ? dto.getBody() : "");
            }

            message.setSentDate(new Date());
            Transport.send(message);
            logger.info("Email send completed (Transport.send) to " + (recipients == null ? "[]" : recipients.toString()));
        } catch (MessagingException e) {
            logger.log(Level.WARNING, "Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected error sending email: " + e.getMessage(), e);
        }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed preparing email send: " + e.getMessage(), e);
        }
    }

        private Session getSession() {
            String envHost = System.getenv("SMTP_HOST");
            String envPort = System.getenv("SMTP_PORT");
            if (envHost != null && !envHost.isBlank()) {
                java.util.Properties props = new java.util.Properties();
                props.put("mail.smtp.host", envHost);
                props.put("mail.smtp.port", (envPort != null && !envPort.isBlank()) ? envPort : "25");
                props.put("mail.smtp.auth", "false");
                return Session.getInstance(props);
            }
            if (mailSession != null) return mailSession;
            java.util.Properties props = new java.util.Properties();
            props.put("mail.smtp.host", System.getenv().getOrDefault("SMTP_HOST", "smtp"));
            props.put("mail.smtp.port", System.getenv().getOrDefault("SMTP_PORT", "25"));
            props.put("mail.smtp.auth", "false");
            Session s = Session.getInstance(props);
            return s;
        }
}
