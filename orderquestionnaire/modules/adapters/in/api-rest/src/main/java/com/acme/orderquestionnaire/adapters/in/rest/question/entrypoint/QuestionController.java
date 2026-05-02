package com.acme.orderquestionnaire.adapters.in.rest.question.entrypoint;

import com.acme.orderquestionnaire.adapters.in.rest.question.request.CreateQuestionRequest;
import com.acme.orderquestionnaire.application.question.port.in.usecase.CreateQuestionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.acme.observability.Loggable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/question")
@Loggable
public class QuestionController {
    private final CreateQuestionUseCase createQuestionUseCase;

    public QuestionController(CreateQuestionUseCase createQuestionUseCase) {
        this.createQuestionUseCase = Objects.requireNonNull(createQuestionUseCase,
                "createQuestionUseCase must not be null");
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateQuestionRequest request) {
        var saved = createQuestionUseCase.execute(request.toCommand());
        return ResponseEntity.ok(saved);
    }
}
