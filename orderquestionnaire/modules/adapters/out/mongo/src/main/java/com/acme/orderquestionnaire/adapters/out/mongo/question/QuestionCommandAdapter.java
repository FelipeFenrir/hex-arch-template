package com.acme.orderquestionnaire.adapters.out.mongo.question;

import com.acme.orderquestionnaire.adapters.out.mongo.question.repository.QuestionCommandRepository;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@OutputAdapter
public class QuestionCommandAdapter implements QuestionCommandOutPort {

    private final QuestionCommandRepository questionCommandRepository;
    private final QuestionEntityMapper questionEntityMapper;

    public QuestionCommandAdapter(QuestionCommandRepository questionCommandRepository,
                                  QuestionEntityMapper questionEntityMapper) {
        this.questionCommandRepository = Objects.requireNonNull(questionCommandRepository,
                "questionCommandRepository must not be null");
        this.questionEntityMapper = Objects.requireNonNull(questionEntityMapper,
                "questionEntityMapper must not be null");
    }

    /**
     * Persiste uma nova Question no MongoDB e retorna o agregado reconstruído.
     * Falhas técnicas (ex.: MongoDB indisponível) propagam como RuntimeException,
     * pois são erros de infraestrutura, não de negócio.
     */
    @Override
    public Result<Question, List<DomainError>> create(Question entry) {
        var savedEntity = questionCommandRepository.save(questionEntityMapper.toEntity(entry));
        return Result.success(questionEntityMapper.toDomain(savedEntity));
    }

    /**
     * Atualiza uma Question existente no MongoDB e retorna o agregado reconstruído.
     * Usa save() com upsert semântico do Spring Data MongoDB.
     */
    @Override
    public Result<Question, List<DomainError>> update(Question question) {
        var savedEntity = questionCommandRepository.save(questionEntityMapper.toEntity(question));
        return Result.success(questionEntityMapper.toDomain(savedEntity));
    }

    @Override
    public boolean existsById(String id) {
        return questionCommandRepository.existsById(id);
    }

    /**
     * Busca uma Question pelo ID. Se o documento existir, mas não puder ser
     * rehydratado (dado corrompido), propaga {@link IllegalStateException}.
     */
    @Override
    public Optional<Question> findQuestionById(String id) {
        return questionCommandRepository.findById(id)
                .map(questionEntityMapper::toDomain);
    }

    /**
     * Remove uma Question pelo ID.
     * Erros de infraestrutura durante a deleção são capturados e retornados
     * como {@code Result.failure} usando {@link QuestionErrors#QUESTION_DELETE_FAILED}.
     */
    @Override
    public Result<Void, List<DomainError>> deleteById(String id) {
        try {
            questionCommandRepository.deleteById(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(List.of(
                    QuestionErrors.QUESTION_DELETE_FAILED.toDomainError(id, e.getMessage())));
        }
    }

    /**
     * Remove múltiplas Questions pelos IDs fornecidos.
     * Retorna o número de IDs submetidos à deleção em caso de sucesso.
     */
    @Override
    public Result<Integer, List<DomainError>> deleteAllByIds(List<String> ids) {
        try {
            questionCommandRepository.deleteAllById(ids);
            return Result.success(ids.size());
        } catch (Exception e) {
            return Result.failure(List.of(
                    QuestionErrors.QUESTION_DELETE_FAILED.toDomainError("bulk", e.getMessage())));
        }
    }
}

