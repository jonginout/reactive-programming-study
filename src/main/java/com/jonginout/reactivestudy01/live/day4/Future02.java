package com.jonginout.reactivestudy01.live.day4;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@Slf4j
public class Future02 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        /**
         * FutureTask : Future 자체를 object로 받을 수 있게 하는
         * 약간 콜백 느낌
         */
        ExecutorService es = Executors.newCachedThreadPool();

        FutureTask<String> futureTask = new FutureTask<String>(() -> {
            Thread.sleep(2000);
            log.debug("Async");
            return "Hello";
        }) {
            @Override
            protected void done() {
                try {
                    log.debug("DONE!! ::::::::::: " + get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };

        es.execute(futureTask);

        log.debug("EXIT");

        es.shutdown();
    }
}
