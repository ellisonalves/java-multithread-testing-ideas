package com.ellisonalves.multithreadtesting;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class PersonRepositoryTest {

    static final LinkedHashSet<Person> PERSONS = new LinkedHashSet<>();

    PersonRepository personRepository;

    @BeforeEach
    void setUp() {
        PERSONS.clear();
        personRepository = new PersonRepository(PERSONS, new ReentrantLock(true));
    }

    @Nested
    class HandleCommonScenarios {

        @Test
        void shouldVerifyThatTheListSizeIsOne() {
            personRepository.persist(new Person("John"));

            assertEquals(1, PERSONS.size());
        }

        @Test
        void shouldVerifyThatTheFollowingNamesArePersisted() {
            personRepository.persist(new Person("Daniel"));
            personRepository.persist(new Person("Josep"));
            personRepository.persist(new Person("John"));

            assertArrayEquals(new String[]{"Daniel", "Josep", "John"}, PERSONS.stream().map(Person::name).toArray());
        }

        @Test
        void shouldRestrictTheNumberOfPeopleInRepository() {
            for (int i = 0; i < 10; i++) {
                personRepository.persist(new Person("Person n: " + i));
            }

            assertThrowsExactly(IllegalStateException.class, () -> personRepository.persist(new Person("Ellison")));
        }

    }

    @Nested
    class HandlingMultiThreadingScenarios {

        ExecutorService executorService;

        @BeforeEach
        void setUp() {
            executorService = Executors.newFixedThreadPool(3);
        }

        @AfterEach
        void tearDown() throws InterruptedException {
            executorService.shutdown();
            awaitTerminationAndShutdownNow();
        }

        @RepeatedTest(15)
        void shouldPersistPersonCorrectlyWithMultipleThreads() throws InterruptedException {
            persistManyPersonsConcurrently(15);

            awaitTerminationAndShutdownNow();

            assertEquals(10, PERSONS.size());
        }

        @Test
        void shouldPerformThePersistenceOfPersonsWithinTheGivenTimeout() {
            assertTimeout(Duration.ofSeconds(1), () -> persistManyPersonsConcurrently(20));
        }

        private List<Person> persistManyPersonsConcurrently(int numberOfPersons) {
            List<Person> personList = new ArrayList<>();
            for (int i = 0; i < numberOfPersons; i++) {
                final int indexCopy = i;
                executorService.submit(() -> personRepository.persist(new Person("Person n: " + indexCopy)));
            }
            return personList;
        }

        private void awaitTerminationAndShutdownNow() throws InterruptedException {
            boolean timeout = executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
            if (timeout) {
                executorService.shutdownNow();
            }
        }

    }

}