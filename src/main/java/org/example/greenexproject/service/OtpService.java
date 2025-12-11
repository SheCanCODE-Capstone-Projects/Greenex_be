package org.example.greenexproject.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class OtpService {


    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static class OtpEntry {
        String email;
        Instant expiresAt;

        OtpEntry(String email, Instant expiresAt) {
            this.email = email;
            this.expiresAt = expiresAt;
        }
    }


    public String generateOtp(String email) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        Instant expiresAt = Instant.now().plusSeconds(300);
        otpStore.put(String.valueOf(otp), new OtpEntry(email, expiresAt));
        return String.valueOf(otp);
    }


    public String validateOtp(String otp) {
        OtpEntry entry = otpStore.get(otp);
        if (entry == null) return null;

        if (Instant.now().isAfter(entry.expiresAt)) {
            otpStore.remove(otp);
            return null;
        }

        otpStore.remove(otp);
        return entry.email;
    }
}
