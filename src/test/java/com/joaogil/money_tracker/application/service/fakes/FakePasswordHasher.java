package com.joaogil.money_tracker.application.service.fakes;

import com.joaogil.money_tracker.application.port.out.auth.PasswordHasher;

public class FakePasswordHasher implements PasswordHasher {
    public int verifications = 0;

    @Override public String hash(String raw) { return "hashed:" + raw; }
    @Override public boolean matches(String raw, String storedHash) {
        verifications++;
        return storedHash != null && storedHash.equals("hashed:" + raw);
    }
}
