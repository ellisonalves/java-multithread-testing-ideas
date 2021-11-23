package com.ellisonalves.multithreadtesting;

import java.util.Set;
import java.util.concurrent.locks.Lock;

public class PersonRepository {

    private final Set<Person> persons;

    private final Lock lock;

    public PersonRepository(Set<Person> persons, Lock lock) {
        this.persons = persons;
        this.lock = lock;
    }

    void persist(Person person) {
        lock.lock();
        try {
            if (persons.size() >= 10) {
                throw new IllegalStateException("The repository can not handle more than 10 persons");
            }
            persons.add(person);
        } finally {
            lock.unlock();
        }
    }

}
