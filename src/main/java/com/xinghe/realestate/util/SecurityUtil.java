package com.xinghe.realestate.util;

import org.mindrot.jbcrypt.BCrypt;

public final class SecurityUtil {
    private static final int BCRYPT_COST = 12;

    private SecurityUtil() {
    }

    public static String hashPassword(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean verifyPassword(String raw, String hashed) {
        return raw != null && hashed != null && BCrypt.checkpw(raw, hashed);
    }
}
