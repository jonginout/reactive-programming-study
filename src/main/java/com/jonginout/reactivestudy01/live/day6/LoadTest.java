package com.jonginout.reactivestudy01.live.day6;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest {
    // 동시성 문제 해결
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8080/rest?idx={idx}";

        // 경계?
        CyclicBarrier barrier = new CyclicBarrier(101);

        StopWatch main = new StopWatch();
        main.start();

        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                int idx = counter.addAndGet(1);

                // 101번째 쓰레드에 블록킹
                barrier.await();

                log.info("::::::::::::::::: Thread {}", idx);

                StopWatch sw = new StopWatch();
                sw.start();
                String res = restTemplate.getForObject(url, String.class, idx);
                sw.stop();

                log.info("::::::::::::::::: Elapsed {} {} / {}", idx, sw.getTotalTimeSeconds(), res);

                return null;
            });
        }

        barrier.await();

        executorService.shutdown();
        executorService.awaitTermination(100, TimeUnit.SECONDS);

        main.stop();

        log.info("::::::::::::::::: Total {}", main.getTotalTimeSeconds());
    }
}
