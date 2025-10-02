package batch.batchapplication.auth.domain;

import java.io.Serializable;
import java.util.List;

public record UserSnapshot(
        Long id,
        String username,
        String email,
        String passwordHash,
        List<String> authorities
) implements Serializable {}