package com.example.solution.securityUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HmacUtil {

    /**
     * Handles the signature verification for incoming webhook requests.
     */

    private static final String ALGORITHM = "HmacSHA256";

    public static String calculateHmac(String data, String secret) throws Exception{
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);

    }

}
