package ma.fstt.ecommerce.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** Hachage salé des mots de passe (format stocké : sel$hash, tous deux en Base64). */
public final class PasswordUtil {

    private static final SecureRandom RNG = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hasher(String motDePasse) {
        byte[] sel = new byte[16];
        RNG.nextBytes(sel);
        return Base64.getEncoder().encodeToString(sel) + "$" + calculer(sel, motDePasse);
    }

    public static boolean verifier(String motDePasse, String valeurStockee) {
        if (motDePasse == null || valeurStockee == null) {
            return false;
        }
        String[] parties = valeurStockee.split("\\$");
        if (parties.length != 2) {
            return false;
        }
        byte[] sel = Base64.getDecoder().decode(parties[0]);
        byte[] attendu = parties[1].getBytes(StandardCharsets.UTF_8);
        byte[] obtenu = calculer(sel, motDePasse).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(attendu, obtenu);
    }

    private static String calculer(byte[] sel, String motDePasse) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(sel);
            return Base64.getEncoder().encodeToString(md.digest(motDePasse.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
