package com.poc.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReactorMain {

    public static final int COUNT = 1000;

    public static void main(String args[]) throws InterruptedException {
        int timeout = 1000;

        Flux.range(1, COUNT)
                .log()
                .map(n -> n * 2)
                .onBackpressureBuffer(5)
//                .flatMap(n -> {
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(timeout);
//                    } catch (InterruptedException e) {
//                        return Flux.error(new IllegalStateException(e));
//                    }
//                    return Flux.just(n);
//                })
                .log()
                //.publishOn(Schedulers.fromExecutor(Executors.newFixedThreadPool(5)))
                .subscribe(n -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("subscribed results:" + n);
                });

        TimeUnit.MILLISECONDS.sleep(timeout * COUNT + 10);

    }


}
