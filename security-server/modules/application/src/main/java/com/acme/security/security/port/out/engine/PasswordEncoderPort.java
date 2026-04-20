package com.acme.security.security.port.out.engine;

import com.acme.shared.stereotypes.core.OutputPort;

@OutputPort
public interface PasswordEncoderPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
