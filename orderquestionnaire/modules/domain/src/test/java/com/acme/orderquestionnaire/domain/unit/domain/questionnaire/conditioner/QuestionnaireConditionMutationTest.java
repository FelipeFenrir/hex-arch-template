package com.acme.orderquestionnaire.domain.unit.domain.questionnaire.conditioner;

import com.acme.orderquestionnaire.domain.questionnaire.conditioner.CompositeCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Condition mutation tests")
class QuestionnaireConditionMutationTest {

    @Test
    @DisplayName("numeric condition keeps boundary semantics for >= and <=")
    void shouldRespectBoundaryForInclusiveOperators() {
        var greaterOrEqual = new NumericCondition("score", 7, ">=");
        var lessOrEqual = new NumericCondition("score", 7, "<=");

        assertTrue(greaterOrEqual.isSatisfy(Map.of("score", 7)));
        assertTrue(lessOrEqual.isSatisfy(Map.of("score", 7)));
        assertFalse(greaterOrEqual.isSatisfy(Map.of("score", 6)));
        assertFalse(lessOrEqual.isSatisfy(Map.of("score", 8)));
    }

    @Test
    @DisplayName("numeric condition rejects non numeric answers")
    void shouldRejectNonNumericAnswer() {
        var condition = new NumericCondition("score", 7, ">");

        assertFalse(condition.isSatisfy(Map.of("score", "8")));
        assertFalse(condition.isSatisfy(Map.of("score", true)));
    }

    @Test
    @DisplayName("composite and and or operators keep expected truth table")
    void shouldRespectCompositeTruthTable() {
        var gtFive = new NumericCondition("q_one", 5, ">");
        var ltTen = new NumericCondition("q_one", 10, "<");

        var andComposite = new CompositeCondition(true);
        andComposite.addCondition(gtFive);
        andComposite.addCondition(ltTen);

        var orComposite = new CompositeCondition(false);
        orComposite.addCondition(gtFive);
        orComposite.addCondition(ltTen);

        assertTrue(andComposite.isSatisfy(Map.of("q_one", 8)));
        assertFalse(andComposite.isSatisfy(Map.of("q_one", 4)));

        assertTrue(orComposite.isSatisfy(Map.of("q_one", 4)));
        assertTrue(orComposite.isSatisfy(Map.of("q_one", 12)));
        assertFalse(orComposite.isSatisfy(Map.of("q_one", "not-a-number")));
    }
}




