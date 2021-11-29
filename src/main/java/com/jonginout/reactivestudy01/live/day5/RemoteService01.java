package com.jonginout.reactivestudy01.live.day5;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@SpringBootApplication
public class RemoteService01 {
    /**
     * 너무 많은 쓰레드 개수를 두는것 도 방법은 아니다 : 컨텍스트 스위칭에 대한 비용이 너무 크
     */
    @RestController
    public static class MyController {
        @Autowired
        MyService myService;

        static final String URL_1 = "http://localhost:8081/service?req={req}";
        static final String URL_2 = "http://localhost:8081/service2?req={req}";

        AsyncRestTemplate restTemplate = new AsyncRestTemplate();
        // 네티
//        AsyncRestTemplate restTemplate = new AsyncRestTemplate(
//                new Netty4ClientHttpRequestFactory(
//                        new NioEventLoopGroup(1)
//                )
//        );
//        RestTemplate restTemplate = new RestTemplate();

        @GetMapping("/rest")
        public ListenableFuture<ResponseEntity<String>> rest(int idx) {
            ListenableFuture<ResponseEntity<String>> res = restTemplate.getForEntity(URL_1, String.class, "hello" + idx);
//            String res = restTemplate.getForObject("http://localhost:8081/service?req={req}", String.class, "hello" + idx);

            // 스프링은 스레드를 즉시 반납하고 스프링 MVC가 알아서 콜백을 처리 해줌
            // 비동기적으로 호출을하고 응답이 오면 스프링 MVC가 알아서 응답 => 쓰레드가 1개 뿐이지만 응답성이 상당히 높아짐
            return res;
        }

        @GetMapping("/rest2")
        public DeferredResult<String> rest2(int idx) {
            DeferredResult<String> deferredResult = new DeferredResult<>();

            ListenableFuture<ResponseEntity<String>> lf = restTemplate.getForEntity(URL_2, String.class, "hello" + idx);
            lf.addCallback(s -> {
                ListenableFuture<ResponseEntity<String>> lf2 = restTemplate.getForEntity("http://localhost:8081/service2?req={req}", String.class, s.getBody());
                lf2.addCallback(s2 -> {
                    ListenableFuture<String> f3 = myService.work(s2.getBody());
                    f3.addCallback(deferredResult::setResult, e3 -> {
                        deferredResult.setErrorResult(e3.getMessage());
                    });
                }, e2 -> {
                    deferredResult.setErrorResult(e2.getMessage());
                });
            }, e -> {
                // 콜백 방식에서는 예외를 전파하는건 좀 위험
                deferredResult.setErrorResult(e.getMessage());
            });

            return deferredResult;
        }
    }

    @Service
    public static class MyService {
        public ListenableFuture<String> work(String req) {
            return new AsyncResult<>(req + "/myServiceWork");
        }
    }

    @Bean
    public ThreadPoolTaskExecutor myThreadPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(1);
        taskExecutor.setMaxPoolSize(1);
        taskExecutor.initialize();
        return taskExecutor;
    }

    public static void main(String[] args) {
        SpringApplication.run(RemoteService01.class, args);
    }
}
