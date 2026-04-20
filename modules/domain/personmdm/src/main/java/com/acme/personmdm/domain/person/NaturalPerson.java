package com.acme.personmdm.domain.person;

import com.acme.personmdm.domain.common.enumerator.BiologicalSex;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@SuperBuilder(builderMethodName = "NaturalPersonBuilderImpl", setterPrefix = "with")
public class NaturalPerson extends Person {
    private BiologicalSex biologicalSex;

    public static class NaturalPersonBuilderImpl extends NaturalPersonBuilder<NaturalPerson, NaturalPersonBuilderImpl> {
        public NaturalPersonBuilderImpl withBiologicalSex(String sexStr) {
            this.withBiologicalSex(BiologicalSex.fromCode(sexStr));
            return this;
        }
    }

    public static NaturalPersonBuilderImpl builder() {
        return new NaturalPersonBuilderImpl();
    }
}
