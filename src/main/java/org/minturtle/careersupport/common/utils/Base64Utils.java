package org.minturtle.careersupport.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Utils {


    public String encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    public String decode(String str) {
        byte[] decodedBytes = Base64.getDecoder().decode(str);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
