package org.example.greenexproject.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class OtpService {

    // otp -> email + expiry
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static class OtpEntry {
        String email;
        Instant expiresAt;

        OtpEntry(String email, Instant expiresAt) {
            this.email = email;
            this.expiresAt = expiresAt;
        }
    }

    // Generate 6-digit OTP
    public String generateOtp(String email) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        Instant expiresAt = Instant.now().plusSeconds(300); // 5 minutes
        otpStore.put(String.valueOf(otp), new OtpEntry(email, expiresAt));
        return String.valueOf(otp);
    }

    // Validate OTP without needing email
    public String validateOtp(String otp) {
        OtpEntry entry = otpStore.get(otp);
        if (entry == null) return null; // invalid OTP

        if (Instant.now().isAfter(entry.expiresAt)) {
            otpStore.remove(otp); // expired
            return null;
        }

        otpStore.remove(otp); // remove after success
        return entry.email; // return associated email
    }
}
