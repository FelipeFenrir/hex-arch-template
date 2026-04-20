package com.acme.shared.engine.state;

import com.acme.shared.engine.rule.GenericRule;
import com.acme.shared.engine.rule.RuleValidator;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StateMachineConfig<S, Ctx> {

    private final Map<S, List<Transition<S, Ctx>>> transitions = new HashMap<>();

    public StateMachineConfig<S, Ctx> addTransition(S from, S to) {
        return addTransition(from, to, List.of(), List.of());
    }

    public StateMachineConfig<S, Ctx> addTransition(S from,
                                                     S to,
                                                     List<GenericRule<Ctx>> guards,
                                                     List<TransitionAction<Ctx>> actions) {
        transitions.computeIfAbsent(from, ignored -> new ArrayList<>())
                .add(new Transition<>(from, to, List.copyOf(guards), List.copyOf(actions)));
        return this;
    }

    public Result<TransitionResult<S, Ctx>, List<DomainError>> transition(S current,
                                                                           S desired,
                                                                           Ctx context) {
        if (current.equals(desired)) {
            return Result.success(new TransitionResult<>(desired, context));
        }

        Optional<Transition<S, Ctx>> transition = transitions
                .getOrDefault(current, List.of())
                .stream()
                .filter(item -> item.to().equals(desired))
                .findFirst();

        if (transition.isEmpty()) {
            return Result.failure(List.of(new DomainError(
                    "INVALID_STATUS_TRANSITION",
                    "Cannot transition from " + current + " to " + desired
            )));
        }

        RuleValidator<Ctx> validator = new RuleValidator<>();
        validator.addSpecs(transition.get().guards());
        var notification = validator.validate(context);
        if (notification.hasErrors()) {
            List<DomainError> errors = notification.getErrors().stream()
                    .map(message -> new DomainError("TRANSITION_GUARD_VIOLATION", message))
                    .toList();
            return Result.failure(errors);
        }

        Result<Ctx, List<DomainError>> contextResult = Result.success(context);
        for (TransitionAction<Ctx> action : transition.get().actions()) {
            contextResult = contextResult.flatMap(action::apply);
        }

        return contextResult.map(updatedContext -> new TransitionResult<>(desired, updatedContext));
    }
}

