package com.jonginout.reactivestudy01.live.day3;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class FluxScheduler01 {
    /**
     * reactor에서는 어떻게 하는지?
     */
    public static void main(String[] args) {

        /**
         * Flux는 일종의 Publisher
         */
        Flux.range(1, 10)
                .publishOn(Schedulers.newSingle("publishOn-"))
                .log()
                .subscribeOn(Schedulers.newSingle("subscribeOn-"))
                .subscribe(s -> {
//                    System.out.println(Thread.currentThread().getName() + ": subscribe ::::::: " + s);
                });

        System.out.println("======EXIT=======");
    }
}
