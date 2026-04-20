package com.acme.security.oauth.out.password;

import com.acme.security.security.port.out.engine.PasswordEncoderPort;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@OutputAdapter
public class Argon2PasswordEncoderAdapter implements PasswordEncoderPort {

    // Configurações padrão recomendadas (ajustáveis conforme o hardware)
    private final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(
            16,     // salt length
            32,     // hash length
            1,      // parallelism
            65536,  // memory (64MB)
            3       // iterations
    );

    @Override
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
