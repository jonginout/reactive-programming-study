package com.jonginout.reactivestudy01.live.day3;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FluxScheduler02 {
    public static void main(String[] args) throws InterruptedException {

        /**
         * Thread는
         * - User 쓰레드
         * - Daemon 쓰레드
         * 두개가 있음
         * JVM은 User쓰레드가 하나라도 있으면 죽이지 않는다
         * 다만 Daemon는 그냥 죽임
         *
         * Flux가 만드는 쓰레드는 -> Daemon 쓰레드
         */
        Flux.interval(Duration.ofMillis(200))
                .take(10)   // 10개만 받고 끝냄
                .subscribe(s -> log.debug("onNext:{}", s));

        log.debug("EXIT");
        TimeUnit.SECONDS.sleep(5);
    }
}
