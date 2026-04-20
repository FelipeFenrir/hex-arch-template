package com.acme.application.personmdm.service;

//import com.acme.application.ports.out.PersonEventsOutPort;
//import com.acme.application.ports.out.PersonRepositoryOutPort;
//import com.acme.application.usecases.PersonManagementUseCase;
//import com.acme.domain.person.Person;
//import com.acme.domain.person.factory.PersonBuilderFactory;
//import com.acme.domain.vo.Id;

//public class PersonManagementService implements PersonManagementUseCase {
public class PersonManagementService {

//    private final PersonRepositoryOutPort repository;
//    private final PersonEventsOutPort events;
//
//    public PersonManagementService(PersonRepositoryOutPort repository, PersonEventsOutPort events) {
//        this.repository = repository;
//        this.events = events;
//    }
//
//    @Override
//    public Person create(String name, String personType, String tenant, String correlationId) {
//
//        var builder = PersonBuilderFactory.getBuilder(personType);
//        var person = builder
//                .withoutId()
//                .name(name)
//                .createdAt(Instant.now())
//                .build();
//
//
//        var saved = repository.save(person, tenant);
//        events.personRegisteredSqs(saved, tenant, correlationId);
//        events.personRegisteredSns(saved, tenant, correlationId);
//        return saved;
//    }
//
//    @Override
//    public Optional<Person> retrieve(Id id, String tenant) {
//        return repository.findById(id, tenant);
//    }
//
//    @Override
//    public Optional<Person> remove(Id id, String tenant) {
//        return repository.deleteById(id, tenant);
//    }
}
