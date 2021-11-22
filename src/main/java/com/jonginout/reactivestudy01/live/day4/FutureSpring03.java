package com.jonginout.reactivestudy01.live.day4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
@EnableAsync
public class FutureSpring03 {

    /**
     * spring은 서블릿 위에 만든 프레임 워크
     * <p>
     * 서블릿 3.0 부터 비동기적으로 서블릿 요청을 처리는 것들이 시작됨..
     * 서블릿은 모든게 다 블록킹 구조였다
     * IO라는건 http 요청을 받아서 결과를 응답을 하는것
     * 그 IO작업이 일어나는것에 따라서 IO마다 쓰레드를 하나씩 할당해줬었다
     * 100개의 커넥션이면 100개의 쓰레드가 할당이 되어있었다
     * => 블록킹 방식에서는 어쩔 수 없는 구조
     * => 서블릿에서는 Http 커넥션과 연결되어 http servelt request, response를 읽고 쓸때 내부적으로 IO작업에 inputstream, outputstream을 사용 -> inputstream, outputstream 이건 블록킹 방식
     * => 쓰레드가 블록킹 되는 상황은 컨텍스트 스위칭이 일어난다는 이야기고 -> 이는 CPU나 메모리 사용량이 올라간다..
     * ====> 블록킹이 되면 이 쓰레드가 사용되고 있지 않다는걸 CPU가 알아채는 순간 그 쓰레드를 대기상태로 전환하고 -> 다른 쓰레드를 끌어오고 그 쓰레드를 사용 : 컨텍스트 스위칭 -> 이때 CPU자원을 많이 먹음
     * ====> 기본적으로 블록킹되면 대기상태, 대기상태 해제,, 등 작업이 일어나니깐 -> 컨텍스트가 많이 스위칭 됨 -> CPU자원 많이 먹음
     * <p>
     * 해결
     * Servelt Thread1 : request -> ... 뭔가 작업 동안 블락킹 (워커쓰레드) -> response
     * 이렇게 톰캣 쓰레드가 꽉차고 큐에 까지 꽉 차면 문제..
     * 그렇다고 마냥 톰캣 쓰레드를 많이 만드는게 좋은건 아님 -> OOM가능성 / 컨텍스트 스위칭이 많아져 CPU가 뻗음
     * 그래서 빨리 Servelt Thread들을 응답시켜서 해소시켜주는게 중요 (빨리 스레드 풀에 반환)
     * => 비동기 서블릿으로 해결 -> 밑에 예제 @GetMapping("/async")
     * ===> 결국 워커 스레드 자체는 많이 할당되는거 아니냐??? => DeferredResult 사용
     * <p>
     * <p>
     * 서블릿 3.0 : 비동기 서블릿 3.0
     * - http connection은 이미 논블럭킹 IO
     * - 서블릿 요청 읽기, 응답 쓰기는 블록킹 => IO작업 자체는 블록킹인게 여전히 문제
     * - 비동기 작업 시작 즉시 서블릿 쓰레드 반납
     * - 비동기 작업이 완료되면 서블릿 쓰레드 재할당
     * - 비동기 서블릿 컨텍스트 이용 (AsyncContext)
     * 서블릿 3.1 : 논블럭킹 IO
     * - 논블록킹 서블릿 요청, 응답 처리 => Callback 방식으로 해결
     * - Callback
     */

    @RestController
    public static class MyController {
        Queue<DeferredResult<String>> results = new ConcurrentLinkedDeque<>();

        // 비동기 서블릿
        @GetMapping("/async")
        public Callable<String> async() {
            log.info("::::::: Callable");
            return () -> {
                log.info("::::::: Async");
                Thread.sleep(2000);
                return "hello" + LocalDateTime.now();
            };
        }

        @GetMapping("/callable")
        public String callable() throws InterruptedException {
            log.info("::::::: Async");
            Thread.sleep(2000);
            return "hello" + LocalDateTime.now();
        }

        /**
         * DeferredResult 장점 -> 워커 스레드를 사용하지 않는다.
         */
        @GetMapping("/deferred")
        public DeferredResult deferredResult() {
            log.info("::::::: deferredResult");
            DeferredResult<String> deferredResult = new DeferredResult<>();
            results.add(deferredResult);
            return deferredResult;
        }

        @GetMapping("/deferred/count")
        public String deferredCounter() {
            return String.valueOf(results.size());
        }

        @GetMapping("/deferred/event")
        public String deferredEvent(String msg) {
            for (DeferredResult<String> deferredResult : results) {
                deferredResult.setResult("Hello : " + msg);
                results.remove(deferredResult);
            }
            return "OK";
        }

        /**
         * 여러번에 나눠서 데이터를 보내는
         * 즉 한번 요청에 여러번 응답을 보내는
         * sse
         */
        @GetMapping("/emitter")
        public ResponseBodyEmitter emitter(String msg) {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    for (int i = 1; i <= 50; i++) {
                        emitter.send("<p>Stream " + i + "</p>");
                        Thread.sleep(100);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });

            return emitter;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(FutureSpring03.class, args);
    }
}
