package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class Funcoes {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static String gerarHashSHA256(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(senha.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Converte "dd/MM/aaaa" em LocalDate. Devolve null se o texto for inválido. */
    public static LocalDate lerData(String texto) {
        try {
            return LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    public static String formatarData(LocalDate data) {
        return data == null ? "-" : data.format(FORMATO_DATA);
    }
}
