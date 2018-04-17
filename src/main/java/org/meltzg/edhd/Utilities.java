package org.meltzg.edhd;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Utilities {
    public static String decodeBase64(String encoded) {
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }
}
