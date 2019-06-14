package com.melardev.spring.controllers;


import com.melardev.spring.dtos.responses.ErrorResponse;
import com.melardev.spring.entities.Todo;
import com.melardev.spring.services.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Optional;
import java.util.function.Function;

@CrossOrigin
@RestController
@RequestMapping("todos")
public class TodosController {

    @Autowired
    TodoService todoService;

    @GetMapping
    public Flux<Todo> getAll() {
        return todoService.findAllHqlSummary();
    }


    @GetMapping("/pending")
    public Flux<Todo> getPending() {
        return todoService.findAllPending();
    }


    @GetMapping("/completed")
    public Flux<Todo> getCompleted() {
        return todoService.findAllCompleted();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity> getById(@PathVariable("id") Long id) {
        return this.todoService.findById(id)
                .map(todo -> {
                    if (todo.isPresent()) {
                        return ResponseEntity.ok(todo.get());
                    } else
                        return new ResponseEntity(new ErrorResponse("Todo not found"), HttpStatus.NOT_FOUND);
                });
    }


    @PostMapping
    public Mono<Todo> create(@Valid @RequestBody Todo todo) {
        return todoService.save(todo);
    }


    @PutMapping("/{id}")
    public Mono<ResponseEntity> update(@PathVariable("id") Long id, @RequestBody Todo todoInput) {
        // Do you know how to make it better? Let me know on Twitter or Pull request please.
        return todoService.findById(id)
                .map(new Function<Optional<Todo>, Mono<ResponseEntity>>() {
                    @Override
                    public Mono<ResponseEntity> apply(Optional<Todo> t) {
                        if (!t.isPresent())
                            return Mono.just(new ResponseEntity(new ErrorResponse("Not found"), HttpStatus.NOT_FOUND));

                        Todo todo = t.get();
                        String title = todoInput.getTitle();
                        if (title != null)
                            todo.setTitle(title);

                        String description = todoInput.getDescription();
                        if (description != null)
                            todo.setDescription(description);

                        todo.setCompleted(todoInput.isCompleted());
                        Mono<ResponseEntity> response = todoService.save(todo)
                                .flatMap(new Function<Todo, Mono<ResponseEntity>>() {
                                    @Override
                                    public Mono<ResponseEntity> apply(Todo todo) {
                                        return Mono.just(ResponseEntity.ok(todo));
                                    }
                                });

                        return response;
                    }
                }).flatMap(new Function<Mono<ResponseEntity>, Mono<? extends ResponseEntity>>() {
                    @Override
                    public Mono<? extends ResponseEntity> apply(Mono<ResponseEntity> responseEntityMono) {
                        return responseEntityMono.map(new Function<ResponseEntity, ResponseEntity>() {
                            @Override
                            public ResponseEntity apply(ResponseEntity responseEntity) {
                                return responseEntity;
                            }
                        });
                    }
                });
    }


    @DeleteMapping("/{id}")
    public Mono<ResponseEntity> delete(@PathVariable("id") Long id) {
        return todoService.findById(id)
                .flatMap(ot -> todoService.delete(ot))
                .map(new Function<Boolean, ResponseEntity>() {
                    @Override
                    public ResponseEntity apply(Boolean bool) {
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    }
                }).defaultIfEmpty(new ResponseEntity<>(new ErrorResponse("Todo not found"), HttpStatus.NOT_FOUND));
    }


    @DeleteMapping
    public Mono<ResponseEntity<Void>> deleteAll() {
        return todoService.deleteAll().then(Mono.just(new ResponseEntity<>(HttpStatus.NO_CONTENT)));
    }

}