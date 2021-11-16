package com.jonginout.reactivestudy01.live.day2;

import reactor.core.publisher.Flux;

public class Reactor01 {
    public static void main(String[] args) {
        /**
         * Op01에서 오퍼레이터를 만들었던걸
         * Reactor를 쓰면 간단해진다
         *
         * Flux : 일종의 Publisher
         * - subscribe의 첫번째 메소드에 consumer를 작성하게끔 하는데 이게 : onNext 정도로 이해하면 된다.
         */

        Flux.<Integer>create(event -> {
                    event.next(1);
                    event.next(2);
                    event.next(3);
                    event.next(4);
                })
                .log()
                .map(s -> s * 10)
                .reduce(0, (a, b) -> a + b)
                .log()
                .subscribe(s -> System.out.println("::::::::::::::::::: " + s));
    }
}
