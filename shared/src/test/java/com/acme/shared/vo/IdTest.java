package com.acme.shared.vo;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTest
@DisplayName("Id")
class IdTest {

    public static final String UUID = "23773b26-3297-4fd9-ac1b-f5b287005f1b";

    @Test
    @DisplayName("Then I create ID Object using the factory method with Id")
    void withId() {
        var uuid = java.util.UUID.fromString(UUID);
        var id = Id.withId(UUID);

        assertAll(
                () -> {
                    assertNotNull(id);
                    assertInstanceOf(Id.class, id);
                    assertNotNull(id.stringfyId());
                    assertEquals(UUID, id.stringfyId());
                    assertNotNull(id.uuid());
                    assertEquals(uuid, id.uuid());
                }
        );
    }

    @Test
    @DisplayName("Then I create ID Object using the factory method without Id")
    void withoutId() {
        var id = Id.withoutId();

        assertAll(
                () -> {
                    assertNotNull(id);
                    assertInstanceOf(Id.class, id);
                    assertNotNull(id.stringfyId());
                    assertNotNull(id.uuid());
                    assertInstanceOf(java.util.UUID.class, id.uuid());
                }
        );
    }

    @Test
    @DisplayName("Then I create ID Object and get ID in string format")
    void stringifyId() {
        var id = Id.withId(UUID);

        assertAll(
                () -> {
                    assertNotNull(id);
                    assertInstanceOf(Id.class, id);
                    assertNotNull(id.stringfyId());
                    assertEquals(UUID, id.stringfyId());
                }
        );
    }

    @Test
    @DisplayName("Then I create ID Object and get uuid")
    void uuid() {
        var uuid = java.util.UUID.fromString(UUID);
        var id = Id.withId(UUID);

        assertAll(
                () -> {
                    assertNotNull(id);
                    assertInstanceOf(Id.class, id);
                    assertNotNull(id.uuid());
                    assertEquals(uuid, id.uuid());
                }
        );
    }
}