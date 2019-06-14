package com.melardev.spring.seeds;


import com.github.javafaker.Faker;
import com.melardev.spring.entities.Todo;
import com.melardev.spring.services.TodoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Locale;
import java.util.Set;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toSet;

@Component
public class DbSeeder implements CommandLineRunner {


    private final TodoService todoService;
    private final Faker faker;

    public DbSeeder(TodoService todoService) {
        this.todoService = todoService;
        faker = new Faker(Locale.getDefault());
    }

    @Override
    public void run(String... args) throws Exception {
/*
        for (String collectionName : mongoTemplate.getCollectionNames()) {
            if (collectionName.startsWith("todo")) {
                mongoTemplate.getCollection(collectionName).deleteMany((new BasicDBObject()));
            }
        }
*/
        int maxItemsToSeed = 32;
        Long currentTodosInDb = this.todoService.count().block();
        //long currentTodosInDb = 10;
        Set<Todo> todos = LongStream.range(currentTodosInDb, maxItemsToSeed)
                .mapToObj(i -> {
                    Todo todo = new Todo();
                    todo.setTitle(faker.lorem().sentence());
                    todo.setDescription(faker.lorem().paragraph());
                    todo.setCompleted(faker.random().nextBoolean());
                    return todo;
                })
                .collect(toSet());

        Flux<Todo> a = this.todoService.saveAll(todos);
        a.subscribe();
        System.out.println("[+] " + (maxItemsToSeed - currentTodosInDb) + " todos seeded");
    }

}
