package com.joaogil.money_tracker.application.service.fakes;

import com.joaogil.money_tracker.application.port.out.PasswordHasher;

public class FakePasswordHasher implements PasswordHasher {
    @Override public String hash(String raw) { return "hashed:" + raw; }
}
