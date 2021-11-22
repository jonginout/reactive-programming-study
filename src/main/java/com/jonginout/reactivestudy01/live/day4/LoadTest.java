package com.jonginout.reactivestudy01.live.day4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest {
    // 동시성 문제 해결
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8080/deferred";

        StopWatch main = new StopWatch();
        main.start();

        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                int idx = counter.addAndGet(1);
                log.info("::::::::::::::::: Thread {}", idx);

                StopWatch sw = new StopWatch();
                sw.start();
                restTemplate.getForObject(url, String.class);
                sw.stop();

                log.info("::::::::::::::::: Elapsed {} {}", idx, sw.getTotalTimeSeconds());
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(100, TimeUnit.SECONDS);

        main.stop();

        log.info("::::::::::::::::: Total {}", main.getTotalTimeSeconds());
    }
}
