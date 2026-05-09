package com.acme.orderquestionnaire.domain.questionnaire.vo;

import java.util.Objects;

/**
 * Value Object representing the numeric constraints for NUMBER type answer configurations.
 * Encapsulates min, max, and step boundaries; validates logical consistency.
 * All values are optional (nullable), allowing flexible constraint definitions.
 * When step is defined, it should divide evenly into the range for valid answers.
 */
public record NumericRange(Double min, Double max, Double step) {
    public NumericRange {
        // Validate logical consistency only if both bounds are specified
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException(
                "min (" + min + ") must not be greater than max (" + max + ")"
            );
        }
        // Step should be positive if specified
        if (step != null && step <= 0) {
            throw new IllegalArgumentException(
                "step must be positive, got: " + step
            );
        }
    }

    public static NumericRange of(Double min, Double max, Double step) {
        return new NumericRange(min, max, step);
    }

    /**
     * Returns a new NumericRange with the given minimum value.
     */
    public NumericRange withMin(Double newMin) {
        return new NumericRange(newMin, max, step);
    }

    /**
     * Returns a new NumericRange with the given maximum value.
     */
    public NumericRange withMax(Double newMax) {
        return new NumericRange(min, newMax, step);
    }

    /**
     * Returns a new NumericRange with the given step value.
     */
    public NumericRange withStep(Double newStep) {
        return new NumericRange(min, max, newStep);
    }

    /**
     * Checks if the given value is within the defined constraints.
     */
    public boolean isWithinRange(Double value) {
        if (value == null) return false;
        if (min != null && value < min) return false;
        if (max != null && value > max) return false;
        if (step != null && value % step != 0) return false;
        return true;
    }
}

