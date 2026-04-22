package com.acme.orderquestionnaire.domain.questionnaire.answer;

import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestionFactory;

import java.util.ArrayList;
import java.util.List;

public final class AnswerOptionComposer {

    private AnswerOptionComposer() {
        throw new IllegalStateException("Utility class");
    }

    public static AnswerOptionItem option(String value, String label) {
        return AnswerOptionItem.createNew(value, label);
    }

    public static ConfiguredQuestionFactory.ListConfig options(AnswerOptionItem... options) {
        return new ConfiguredQuestionFactory.ListConfig(List.of(options), null);
    }

    public static ConfiguredQuestionFactory.ListConfig options(String customErrorMessage, AnswerOptionItem... options) {
        return new ConfiguredQuestionFactory.ListConfig(List.of(options), customErrorMessage);
    }

    public static OptionsComposer compose() {
        return new OptionsComposer();
    }

    public static final class OptionsComposer {
        private final List<AnswerOptionItem> options = new ArrayList<>();
        private String customErrorMessage;

        private OptionsComposer() {
        }

        public OptionsComposer add(String value, String label) {
            this.options.add(AnswerOptionComposer.option(value, label));
            return this;
        }

        public OptionsComposer add(AnswerOptionItem option) {
            this.options.add(option);
            return this;
        }

        public OptionsComposer withCustomErrorMessage(String message) {
            this.customErrorMessage = message;
            return this;
        }

        public ConfiguredQuestionFactory.ListConfig build() {
            return new ConfiguredQuestionFactory.ListConfig(List.copyOf(options), customErrorMessage);
        }
    }
}

