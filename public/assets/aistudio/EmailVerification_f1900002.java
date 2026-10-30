/*
 * EmailVerification.java
 * Sends a 6-digit verification code to an email address and checks it.
 *
 * Requires Java 17+ and the Jakarta Mail library. Maven dependencies:
 *
 *   <dependency>
 *     <groupId>jakarta.mail</groupId>
 *     <artifactId>jakarta.mail-api</artifactId>
 *     <version>2.1.3</version>
 *   </dependency>
 *   <dependency>
 *     <groupId>org.eclipse.angus</groupId>
 *     <artifactId>angus-mail</artifactId>
 *     <version>2.0.3</version>
 *   </dependency>
 *
 * Set these environment variables before running (never hard-code them):
 *   SMTP_HOST  e.g. smtp.gmail.com  (or smtp.sendgrid.net, smtp.mailgun.org ...)
 *   SMTP_PORT  e.g. 587
 *   SMTP_USER  your SMTP username (for Gmail: your address)
 *   SMTP_PASS  your SMTP password (for Gmail: an App Password, not your login password)
 *   MAIL_FROM  the sender address, e.g. no-reply@yourdomain.com
 */

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

public class EmailVerification {

    // ---- Settings -------------------------------------------------------
    private static final int CODE_LENGTH = 6;
    private static final Duration CODE_LIFETIME = Duration.ofMinutes(10);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // ---- State (in memory; use a database or Redis in a real app) --------
    private static class PendingCode {
        final String salt;
        final String hash;
        final Instant expiresAt;
        final Instant sentAt;
        int attempts = 0;

        PendingCode(String salt, String hash, Instant expiresAt, Instant sentAt) {
            this.salt = salt;
            this.hash = hash;
            this.expiresAt = expiresAt;
            this.sentAt = sentAt;
        }
    }

    private final Map<String, PendingCode> pending = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    // ---- Public API -----------------------------------------------------

    /** Generates a code, stores a hash of it, and emails it. Returns a message for the user. */
    public String sendCode(String email) {
        email = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "That doesn't look like a valid email address.";
        }

        PendingCode existing = pending.get(email);
        if (existing != null) {
            Duration sinceLast = Duration.between(existing.sentAt, Instant.now());
            if (sinceLast.compareTo(RESEND_COOLDOWN) < 0) {
                long wait = RESEND_COOLDOWN.minus(sinceLast).toSeconds() + 1;
                return "Please wait " + wait + " seconds before requesting another code.";
            }
        }

        String code = generateCode();
        String salt = generateSalt();
        Instant now = Instant.now();
        pending.put(email, new PendingCode(salt, hash(code, salt), now.plus(CODE_LIFETIME), now));

        try {
            sendEmail(email, code);
            return "Verification code sent to " + email + ". It expires in "
                    + CODE_LIFETIME.toMinutes() + " minutes.";
        } catch (MessagingException e) {
            pending.remove(email); // don't keep a code the user never received
            return "Could not send the email: " + e.getMessage();
        }
    }

    /** Checks the code the user typed in. */
    public boolean verify(String email, String input) {
        email = email.trim().toLowerCase();
        PendingCode entry = pending.get(email);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isAfter(entry.expiresAt)) {
            pending.remove(email);
            return false;
        }
        entry.attempts++;
        if (entry.attempts > MAX_ATTEMPTS) {
            pending.remove(email); // too many guesses: force a new code
            return false;
        }

        byte[] expected = entry.hash.getBytes(StandardCharsets.UTF_8);
        byte[] actual = hash(input.trim(), entry.salt).getBytes(StandardCharsets.UTF_8);
        boolean ok = MessageDigest.isEqual(expected, actual); // constant-time compare

        if (ok) {
            pending.remove(email); // single use
        }
        return ok;
    }

    // ---- Helpers --------------------------------------------------------

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        return String.format("%0" + CODE_LENGTH + "d", random.nextInt(bound));
    }

    private String generateSalt() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String hash(String code, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((salt + code).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // SHA-256 is always available
        }
    }

    private static String env(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing environment variable: " + name);
        }
        return value;
    }

    private void sendEmail(String to, String code) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", env("SMTP_HOST"));
        props.put("mail.smtp.port", env("SMTP_PORT"));

        final String user = env("SMTP_USER");
        final String pass = env("SMTP_PASS");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(env("MAIL_FROM")));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Your verification code");
        message.setText("Your verification code is: " + code + "\n\n"
                + "It expires in " + CODE_LIFETIME.toMinutes() + " minutes.\n"
                + "If you didn't request this, you can ignore this email.");

        Transport.send(message);
    }

    // ---- Demo: console flow --------------------------------------------

    public static void main(String[] args) {
        EmailVerification service = new EmailVerification();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        System.out.println(service.sendCode(email));

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            System.out.print("Enter the code from your email (or 'resend'): ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("resend")) {
                System.out.println(service.sendCode(email));
                continue;
            }
            if (service.verify(email, input)) {
                System.out.println("Email verified!");
                return;
            }
            System.out.println("Wrong or expired code.");
        }
        System.out.println("Too many attempts. Please start again.");
    }
}
