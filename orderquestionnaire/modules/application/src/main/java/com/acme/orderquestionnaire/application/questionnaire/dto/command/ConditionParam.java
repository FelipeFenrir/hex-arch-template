package com.acme.orderquestionnaire.application.questionnaire.dto.command;

import java.util.List;

public sealed interface ConditionParam permits
        ConditionParam.Equal,
        ConditionParam.Numeric,
        ConditionParam.Visibility,
        ConditionParam.Composite {

    record Equal(String questionRootCode, Object expectedValue) implements ConditionParam { }

    record Numeric(String questionRootCode, Double expectedValue, String operator) implements ConditionParam { }

    record Visibility(String questionRootCode, Object expectedValue) implements ConditionParam { }

    record Composite(boolean andOperator, List<ConditionParam> conditions) implements ConditionParam { }
}

