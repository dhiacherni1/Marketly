package Services;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Envoi d'emails (facture en pièce jointe).
 * Configurez src/main/resources/mail.properties ou les propriétés système :
 * mail.smtp.host, mail.smtp.port, mail.smtp.auth, mail.user, mail.password
 */
public class EmailService {
    private static final String CONFIG = "mail.properties";

    /**
     * Envoie un e-mail avec pièce jointe.
     * @return null si succès, sinon message d'erreur à afficher
     */
    public static String sendWithAttachment(String toEmail, String subject, String bodyText, File attachment) {
        if (toEmail == null || toEmail.trim().isEmpty()) return "Adresse e-mail manquante.";
        Properties props = loadMailProperties();
        String user = trim(props.getProperty("mail.user"));
        String password = trim(props.getProperty("mail.password"));
        if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
            return "E-mail non configuré. Éditez src/main/resources/mail.properties et renseignez mail.user et mail.password (Gmail : mot de passe d'application).";
        }
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", props.getProperty("mail.smtp.host", "smtp.gmail.com"));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(user));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail.trim()));
            msg.setSubject(subject != null ? subject : "Facture");

            MimeMultipart multipart = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();
            String body = bodyText != null ? bodyText : "Veuillez trouver votre facture en pièce jointe.";
            textPart.setText(body, "UTF-8");
            multipart.addBodyPart(textPart);

            if (attachment != null && attachment.exists()) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.setDataHandler(new DataHandler(new ByteArrayDataSource(Files.readAllBytes(attachment.toPath()), "application/pdf")));
                attachPart.setFileName(attachment.getName());
                multipart.addBodyPart(attachPart);
            }
            msg.setContent(multipart);
            Transport.send(msg);
            return null;
        } catch (MessagingException e) {
            String errMsg = getMessageFromException(e);
            if (errMsg.contains("Authentication") || errMsg.contains("535") || errMsg.contains("534") || errMsg.contains("Username and Password not accepted")) {
                return "Authentification refusée. Pour Gmail, utilisez un mot de passe d'application : https://myaccount.google.com/apppasswords";
            }
            if (errMsg.contains("Could not connect") || errMsg.contains("Connection refused") || errMsg.contains("timed out")) {
                return "Impossible de se connecter au serveur mail. Vérifiez votre connexion internet et le pare-feu (port 587).";
            }
            return "Échec d'envoi : " + errMsg;
        } catch (IOException e) {
            return "Erreur : " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    private static String getMessageFromException(Throwable t) {
        String m = t.getMessage();
        if (t.getCause() != null) {
            String c = getMessageFromException(t.getCause());
            if (c != null && !c.isEmpty()) return c;
        }
        return m != null ? m : t.getClass().getSimpleName();
    }

    private static String trim(String s) {
        return s != null ? s.trim() : null;
    }

    private static Properties loadMailProperties() {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.port", "587");
        try {
            java.io.InputStream is = EmailService.class.getResourceAsStream("/" + CONFIG);
            if (is != null) {
                props.load(is);
                is.close();
            }
        } catch (IOException ignored) {}
        // Fallback : charger depuis le répertoire du projet (fichier édité dans src/main/resources)
        String user = trim(props.getProperty("mail.user"));
        String pwd = props.getProperty("mail.password");
        if ((user == null || user.isEmpty() || pwd == null || pwd.isEmpty())) {
            java.io.File projectFile = new java.io.File(System.getProperty("user.dir"), "src/main/resources/" + CONFIG);
            if (!projectFile.exists()) projectFile = new java.io.File(System.getProperty("user.dir"), CONFIG);
            if (projectFile.canRead()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(projectFile)) {
                    props.load(fis);
                } catch (IOException ignored) {}
            }
        }
        String host = System.getProperty("mail.smtp.host");
        if (host != null) props.setProperty("mail.smtp.host", host);
        String port = System.getProperty("mail.smtp.port");
        if (port != null) props.setProperty("mail.smtp.port", port);
        String sysUser = System.getProperty("mail.user");
        if (sysUser != null) props.setProperty("mail.user", sysUser);
        String sysPwd = System.getProperty("mail.password");
        if (sysPwd != null) props.setProperty("mail.password", sysPwd);
        return props;
    }
}
