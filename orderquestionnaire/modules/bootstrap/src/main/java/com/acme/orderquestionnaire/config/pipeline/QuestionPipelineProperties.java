package com.acme.orderquestionnaire.config.pipeline;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;

@ConfigurationProperties(prefix = "question.pipeline")
public class QuestionPipelineProperties {

    private final LinkedHashMap<String, StepProperties> createquestion = new LinkedHashMap<>();
    private final LinkedHashMap<String, StepProperties> updatequestion = new LinkedHashMap<>();
    private final LinkedHashMap<String, StepProperties> deletequestion = new LinkedHashMap<>();

    public LinkedHashMap<String, StepProperties> getCreatequestion() {
        return createquestion;
    }

    public LinkedHashMap<String, StepProperties> getUpdatequestion() {
        return updatequestion;
    }

    public LinkedHashMap<String, StepProperties> getDeletequestion() {
        return deletequestion;
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

