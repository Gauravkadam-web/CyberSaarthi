package org.cybersaarthi.report.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Year;

@Component
public class ReferenceCodeGenerator {

    // Crockford Base32 alphabet (excludes 0, 1, I, L, O to avoid reading ambiguity)
    private static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final int CODE_LENGTH = 5;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        int currentYear = Year.now().getValue();
        StringBuilder sb = new StringBuilder("CS-").append(currentYear).append("-");

        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
        }

        return sb.toString();
    }
}
