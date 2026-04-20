package com.acme.application.orderquestionnaire;

//import com.acme.application.ports.out.PersonEventsOutPort;
//import com.acme.application.ports.out.PersonRepositoryOutPort;
//import com.acme.domain.exception.DomainValidationException;
//import com.acme.domain.person.Person;
//import com.acme.domain.person.factory.PersonBuilderFactory;
//import com.acme.domain.vo.Id;
//import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.time.Instant;
//import java.util.UUID;

//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;

class PersonManagementServiceTest {

//    PersonManagementService serviceInPort;
//    PersonRepositoryOutPort repositoryOutPort;
//    PersonEventsOutPort eventsOutPort;
//
//    private final String tenant = "public";
//    private final String correlationId = UUID.randomUUID().toString();
//    private final String commonTestId = "12345678-1234-1234-1234-1234567890ab";
//
//    @BeforeEach
//    void setUp() {
//        repositoryOutPort = mock(PersonRepositoryOutPort.class);
//        eventsOutPort = mock(PersonEventsOutPort.class);
//        serviceInPort = new PersonManagementService(repositoryOutPort, eventsOutPort);
//    }
//
//    @Test
//    void create() {
//
//        // Arrange
//        var personId = Id.withId(commonTestId);
//        var personName = "Fulano";
//        var personType = "NP";
//        var naturalPersonBuilder = PersonBuilderFactory.naturalPersonBuilder();
//        var personMock = naturalPersonBuilder
//                .withId(personId.uuid().toString())
//                .withName(personName)
//                .withCreatedAt(Instant.now())
//                .build();
//
////        when(repositoryOutPort.save(Mockito.any(), Mockito.any())).thenAnswer(input -> {
////            return input.getArgument(0);
////        });
//        when(repositoryOutPort.save(Mockito.any(), Mockito.any())).thenReturn(personMock);
//
//        // Act
//        var personCreated = serviceInPort.create(personName, personType, tenant, correlationId);
//
//        // Assert
//        assertNotNull(personCreated);
//        assertEquals(personId,personCreated.getId());
//        assertEquals(personName,personCreated.getName());
//
//        verify(repositoryOutPort, times(1)).save(any(Person.class), any(String.class));
//    }

//    @Test
//    void create_with_validation_error() {
//
//        // Arrange
//        var personName = " ";
//        var personType = "";
//
//        // Act
//        DomainValidationException exception = assertThrows(DomainValidationException.class,
//                () -> serviceInPort.create(personName, personType, tenant, correlationId));
//
//        // Assert
//        assertFalse(exception.getMessage().isBlank());
//        assertEquals("Domain validation fail:\nName is blank\nEmail is blank", exception.getMessage());
//        verify(repositoryOutPort, times(0)).save(any(Person.class), any(String.class));
//    }

    @Test
    void retrieve() {
    }

    @Test
    void remove() {
    }
}