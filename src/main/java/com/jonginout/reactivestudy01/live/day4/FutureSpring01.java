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
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.Future;
//
///**
// * 별도의 비동기 쓰레드를 실행을 해서 작업을 돌리는걸
// * Spring에서는 어떻게 하나?
// */
//@Slf4j
//@SpringBootApplication
//@EnableAsync    // 비동기 작업을 위해 작성해야할 자바 코드가 @EnableAsync, @Async로 축약됨
//public class FutureSpring01 {
//
//    @Component
//    public static class MyService {
//
//        /**
//         * 비동기로 다른 쓰레드에서 작업해줘!
//         * 비동기 작업은 결과를 바로 가져올 수 없다
//         * Future or Callback을 사용해야 함
//         */
//        @Async
//        public Future<String> hello() throws InterruptedException {
//            log.info("hello()");
//            Thread.sleep(2000);
//            return new AsyncResult<String>("Hello");
//        }
//    }
//
//    public static void main(String[] args) {
//        try (ConfigurableApplicationContext c = SpringApplication.run(FutureSpring01.class, args)) {
//
//        }
//    }
//
//    @Autowired
//    MyService myService;
//
//    // controller 라고 생각
//    @Bean
//    ApplicationRunner run() {
//        return args -> {
//            log.info("run()");
//            Future<String> future = myService.hello();
//            log.info("EXIT : " + future.isDone());
//            log.info("RESULT : " + future.get());
//            /**
//             * 여기가 컨트롤라라고 생각하면
//             * future의 결과값은 -> 세션, 디비, 캐시 등..에 저장하고, 응답은 바로 해버림
//             * -> 이게 한 10년전 ... 방식?
//             */
//        };
//    }
//}
