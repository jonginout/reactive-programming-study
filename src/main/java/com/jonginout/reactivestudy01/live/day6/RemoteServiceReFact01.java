package com.jonginout.reactivestudy01.live.day6;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@SpringBootApplication
public class RemoteServiceReFact01 {
    /**
     * 너무 많은 쓰레드 개수를 두는것 도 방법은 아니다 : 컨텍스트 스위칭에 대한 비용이 너무 크
     */
    @RestController
    public static class MyController {

        static final String URL_1 = "http://localhost:8081/service?req={req}";
        static final String URL_2 = "http://localhost:8081/service2?req={req}";

        AsyncRestTemplate restTemplate = new AsyncRestTemplate();

        @GetMapping("/rest")
        public DeferredResult<String> rest(int idx) {
            DeferredResult<String> deferredResult = new DeferredResult<>();

            Completion
                    .from(restTemplate.getForEntity(URL_1, String.class, "hello" + idx))
                    .andApply(s -> restTemplate.getForEntity(URL_2, String.class, s.getBody()))
                    .andError(deferredResult::setErrorResult)
                    .andAccept(s -> deferredResult.setResult(s.getBody()));

            return deferredResult;
        }
    }

    public static class AcceptCompletion<S> extends Completion<S, Void> {
        /**
         * 컨슈머를 받아서 저장해두고 그걸 가지고 생성
         */
        Consumer<S> consumer;

        public AcceptCompletion(Consumer<S> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run(S s) {
            consumer.accept(s);
        }
    }

    public static class ApplyCompletion<S, T> extends Completion<S, T> {
        /**
         * Function을 받아서 처리
         */
        Function<S, ListenableFuture<T>> fn;

        public ApplyCompletion(Function<S, ListenableFuture<T>> fn) {
            this.fn = fn;
        }

        @Override
        public void run(S s) {
            ListenableFuture<T> listenableFuture = fn.apply(s);
            listenableFuture.addCallback(this::complete, this::error);
        }
    }

    public static class ErrorCompletion<T> extends Completion<T, T> {
        Consumer<Throwable> econsumer;

        public ErrorCompletion(Consumer<Throwable> econsumer) {
            this.econsumer = econsumer;
        }

        @Override
        public void run(T s) {
            if (this.next != null) {
                next.run(s);
            }
        }

        @Override
        public void error(Throwable e) {
            econsumer.accept(e);
        }
    }

    public static class Completion<S, T> {
        Completion next;

        public void andAccept(Consumer<T> consumer) {
            this.next = new AcceptCompletion<T>(consumer);
        }

        public Completion<T, T> andError(Consumer<Throwable> econsumer) {
            Completion<T, T> completion = new ErrorCompletion<T>(econsumer);
            this.next = completion;
            return completion;
        }

        public <V> Completion<T, V> andApply(Function<T, ListenableFuture<V>> fn) {
            Completion<T, V> completion = new ApplyCompletion(fn);
            this.next = completion;
            return completion;
        }

        public static <S, T> Completion<S, T> from(ListenableFuture<T> listenableFuture) {
            Completion completion = new Completion();
            listenableFuture.addCallback(completion::complete, completion::error);
            return completion;
        }

        public void error(Throwable e) {
            if (this.next != null) {
                this.next.error(e);
            }
        }

        public void complete(T s) {
            if (this.next != null) {
                this.next.run(s);
            }
        }

        public void run(S s) {
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
        SpringApplication.run(RemoteServiceReFact01.class, args);
    }
}
