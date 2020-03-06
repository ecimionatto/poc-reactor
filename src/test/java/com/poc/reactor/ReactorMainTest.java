package com.poc.reactor;

import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReactorMainTest {

    @Test
    public void map() {
        Flux.range(1, 10)
                .map(n -> n * 3)
                .filter(n -> n % 2 == 0)
                .flatMap(n -> {
                    if (n == 18) {
                        return Flux.empty();
                    } else {
                        return Flux.just(n, n + 1);
                    }
                })
                .subscribe(System.out::println);
    }

    @Test
    public void reduce() {
        Flux.range(1, 10)
                .map(n -> n * 3)
                .filter(n -> n % 2 == 0)
                .flatMap(n -> Flux.just(n, n + 1))
                .log()
                .reduce(0, (aggregator, n) -> n + aggregator)
                .log()
                .subscribe(System.out::println);
    }

    @Test
    public void flatMapZipDistinct() {

        Flux<String> source = Flux
                .just("para viajar basta existir")
                .flatMap(word -> Flux.fromArray(word.split("")));

        Flux<String> source2 = Flux.just("to travel one only needs to exist")
                .flatMap(word -> Flux.fromArray(word.split("")));

        Flux<String> letters = source
                .zipWith(source2,
                        (por, eng)
                        -> String.format("%s,%s", por, eng));

        letters.subscribe(System.out::println);
    }

    @Test
    public void defer() {

        Mono<Long> longMono = Mono.defer(() -> Mono.just(System.currentTimeMillis()));
        //Mono<Long> longMono =  Mono.just(System.currentTimeMillis());

        Flux<Long> longFlux = Flux.just(1L, 2L, 3L)
                .concatWith(longMono)
                .log();

        longFlux
                .subscribe();

        longFlux
                .subscribe();

    }

    @Test
    public void useScheduler() throws InterruptedException {
        Flux<Integer> flux1 = Flux.range(1, 100)
                .flatMap(n -> slowExecution(n, 100));

        Flux<Integer> flux2 = Flux.range(1, 100)
                .flatMap(n -> slowExecution(n, 200));

        Flux.merge(flux1, flux2).log()
                .subscribeOn(Schedulers.parallel())
                .subscribe(n -> System.out.println("subscribed results:" + n));

        TimeUnit.MINUTES.sleep(1);
    }

    private Publisher<? extends Integer> slowExecution(final Integer n, final Integer timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            return Flux.error(new IllegalStateException(e));
        }
        return Flux.just(n);
    }

    public <T> Flux<T> appendBoomError(Flux<T> source) {
        return source.concatWith(Mono.error(new IllegalArgumentException("boom")));
    }

    @Test
    public void appendBoomError() {
        Flux<String> source = Flux.just("thing1", "thing2");

        StepVerifier.create(
                appendBoomError(source))
                .expectNext("thing1")
                .expectNext("thing2")
                .verifyError(IllegalArgumentException.class);
//                .verifyComplete();
//                .expectErrorMessage("boom")
//                .verify();

    }

}