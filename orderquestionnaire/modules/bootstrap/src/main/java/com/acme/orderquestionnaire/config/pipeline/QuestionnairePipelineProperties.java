package com.acme.orderquestionnaire.config.pipeline;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;

/**
 * Binds the {@code questionnaire.pipeline} section from {@code application.yml}.
 *
 * <p>Uses {@link LinkedHashMap} deliberately to preserve the YAML declaration order,
 * which drives the step execution sequence in each pipeline.
 *
 * <p>Example configuration:
 * <pre>
 * questionnaire:
 *   pipeline:
 *     createquestionnaire:
 *       VALIDATE_COMMAND:
 *         enabled: true
 *       BUILD_AUDIT:
 *         enabled: true
 *       CHECK_CHANNEL_DISTRIBUTION:
 *         enabled: true
 *       CHECK_JOURNEY_DISTRIBUTION:
 *         enabled: true
 *       CHECK_NO_DUPLICATE:
 *         enabled: true
 *       BUILD_AND_PERSIST_QUESTIONNAIRE:
 *         enabled: true
 * </pre>
 */
@ConfigurationProperties(prefix = "questionnaire.pipeline")
public class QuestionnairePipelineProperties {

    private final LinkedHashMap<String, StepProperties> createquestionnaire = new LinkedHashMap<>();
    private final LinkedHashMap<String, StepProperties> updatequestionnaire = new LinkedHashMap<>();
    private final LinkedHashMap<String, StepProperties> deletequestionnaire = new LinkedHashMap<>();
    private final LinkedHashMap<String, StepProperties> validatequestionnaireanswers = new LinkedHashMap<>();

    public LinkedHashMap<String, StepProperties> getCreatequestionnaire() {
        return createquestionnaire;
    }

    public LinkedHashMap<String, StepProperties> getUpdatequestionnaire() {
        return updatequestionnaire;
    }

    public LinkedHashMap<String, StepProperties> getDeletequestionnaire() {
        return deletequestionnaire;
    }

    public LinkedHashMap<String, StepProperties> getValidatequestionnaireanswers() {
        return validatequestionnaireanswers;
    }

    public static class StepProperties {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
