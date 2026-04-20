package com.acme.adapters.in.rest.entrypoint;

//import com.acme.observability.Loggable;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/animals")
//@Loggable
public class AnimalController {
    record CreateAnimalRequest(@NotBlank String name, @NotBlank String species, @NotBlank String ownerEmail) {}

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateAnimalRequest req,
                                    @RequestHeader("X-Correlation-Id") String correlationId,
                                    @RequestHeader("X-Journey-Id") String journeyId,
                                    @RequestHeader("X-Channel-Id") String channelId) {
        var id = UUID.randomUUID();
        return ResponseEntity.ok(Map.of("id", id.toString()));
    }
}

