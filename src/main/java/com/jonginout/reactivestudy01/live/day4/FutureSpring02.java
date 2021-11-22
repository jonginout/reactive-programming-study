//package com.jonginout.reactivestudy01.live.day4;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.AsyncResult;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.stereotype.Component;
//import org.springframework.util.concurrent.ListenableFuture;
//
//@Slf4j
//@SpringBootApplication
//@EnableAsync
//public class FutureSpring02 {
//
//    @Component
//    public static class MyService2 {
//
//        /**
//         * 콜백 리스너 방식으로 해보고 싶다면?
//         * ListenableFuture 요거는 Spring에 있는 것
//         */
//        @Async(value = "threadPool")
//        public ListenableFuture<String> hello() throws InterruptedException {
//            log.info("hello()");
//            Thread.sleep(2000);
//            return new AsyncResult<String>("Hello");
//        }
//    }
//
//    @Bean
//    ThreadPoolTaskExecutor threadPool() {
//        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
//        // 첫 요청이 들어올때 10개를 만든다
//        threadPoolTaskExecutor.setCorePoolSize(10);
//        // corePoolSize가 다 차면 Queue가 먼저 찬다
//        threadPoolTaskExecutor.setQueueCapacity(200);
//        // Queue가 꽉 차면 그 시점에 MaxPoolSize만큼 생성 : Queue 마져도 감당이 안되는 상황
//        threadPoolTaskExecutor.setMaxPoolSize(100);
//
//        threadPoolTaskExecutor.setThreadNamePrefix("jongin-");
//
//        threadPoolTaskExecutor.initialize();
//
//        return threadPoolTaskExecutor;
//    }
//
//    public static void main(String[] args) {
//        try (ConfigurableApplicationContext c = SpringApplication.run(FutureSpring02.class, args)) {
//
//        }
//    }
//
//    @Autowired
//    MyService2 myService2;
//
//    // controller 라고 생각
//    @Bean
//    ApplicationRunner run2() {
//        return args -> {
//            log.info("run()");
//            ListenableFuture<String> future = myService2.hello();
//            future.addCallback(
//                    s -> log.info("EXIT : " + s),
//                    e -> log.info("RESULT : " + e.getMessage())
//            );
//            /**
//             * 콜백방식이 더 장점이 많다
//             */
//        };
//    }
//}
