package com.acme.adapters.in.rest.entrypoint;

//import com.acme.adapters.in.rest.request.CreatePersonRequest;
//import com.acme.application.usecases.PersonManagementUseCase;
//import com.acme.observability.Loggable;
//import com.acme.shared.TenantContextHolder;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/people")
//@Loggable
public class PersonController {
//    private final PersonManagementUseCase useCase;

//    public PersonController(PersonManagementUseCase useCase) {
//        this.useCase = useCase;
//    }

//    @PostMapping
//    public ResponseEntity<?> create(@RequestBody CreatePersonRequest req,
//                                    @RequestHeader("X-Correlation-Id") String correlationId) {
//        var tenant = TenantContextHolder.currentTenant();
//        var saved = useCase.create(req.name(), req.email(), tenant, correlationId);
//        return ResponseEntity.ok(Map.of("id", saved.id().toString()));
//    }
}
