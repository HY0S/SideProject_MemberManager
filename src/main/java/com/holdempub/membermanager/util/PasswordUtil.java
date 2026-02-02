package com.holdempub.membermanager.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 관리자 비밀번호 SHA-256 해시 유틸.
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";

    private PasswordUtil() {}

    public static String hash(String plain) {
        if (plain == null) plain = "";
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(ALGORITHM + " not available", e);
        }
    }

    public static boolean verify(String plain, String hashed) {
        if (hashed == null || hashed.isEmpty()) return false;
        return hash(plain).equals(hashed);
    }
}
