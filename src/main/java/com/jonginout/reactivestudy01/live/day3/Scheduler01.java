package com.jonginout.reactivestudy01.live.day3;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class Scheduler01 {
    /**
     * Observer 패턴에서 제일 중요한건
     * => 블록킹이 안되도록 짜야함
     * => 실제 웹서비스에서는 응답 자체는 빨리 내려오도록 짜야
     * <p>
     * Publisher와 Subscriber가 같은 스레드에서 동기적으로 짜는건 좋지 않음
     * 그 문제를 해결하기 위해선 Scheduler를 사용해야함
     */
    public static void main(String[] args) {

        Publisher<Integer> publisher = subscriber -> {
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    System.out.println(Thread.currentThread().getName() + ": request ");
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onNext(3);
                    subscriber.onNext(4);
                    subscriber.onNext(5);
                    subscriber.onComplete();
                }

                @Override
                public void cancel() {

                }
            });
        };

        // 연결

        /**
         *  main 메소드는 subscribe 호출하고 끝나고
         *  퍼블리싱이 느리고 컨슈밍이 빠른 환경에서
         *  ex) 디비나 네트워크에서 데이터를 가져올때
         *
         *  subscribe를 다른 쓰레드로 돌린다
         */
        Publisher<Integer> subscribeOn = subscriber -> {
            ExecutorService es = Executors.newSingleThreadExecutor(
                    new CustomizableThreadFactory() {
                        @Override
                        public String getThreadNamePrefix() {
                            return "subscribeOn-";
                        }
                    }
            );
            es.execute(() -> {
                publisher.subscribe(subscriber);
            });
        };

        /**
         * PublishOn을 이용해서
         * 퍼블리싱이 빠르고 컨슈밍이 느린 환경에서
         *
         * Publisher는 빠르게 퍼블리쉬하고
         * Subscriber는 컨슘을 별개의 쓰레드에서 하도록 해보자
         *
         * 콜러 쓰레드 (메인 쓰레드는 구독하고 바로끝) -> 응답 속도 UP
         * 다른 쓰레드가 (컨슘을 함)
         */
        Publisher<Integer> publishOn = subscriber -> {
            subscribeOn.subscribe(new Subscriber<Integer>() {
                ExecutorService es = Executors.newSingleThreadExecutor(
                        new CustomizableThreadFactory() {
                            @Override
                            public String getThreadNamePrefix() {
                                return "publishOn-";
                            }
                        }
                );

                @Override
                public void onSubscribe(Subscription subscription) {
                    subscriber.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer item) {
                    es.execute(() -> {
                        subscriber.onNext(item);
                    });
                }

                @Override
                public void onError(Throwable throwable) {
                    es.execute(() -> {
                        subscriber.onError(throwable);
                    });
                    es.shutdown();
                }

                @Override
                public void onComplete() {
                    es.execute(() -> {
                        subscriber.onComplete();
                    });
                    es.shutdown();
                }
            });
        };

        // 연결

        publishOn.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
                System.out.println(Thread.currentThread().getName() + ": onSubscribe ::::::: ");
            }

            @Override
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread().getName() + ": onNext ::::::: " + item);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(Thread.currentThread().getName() + ": onError ::::::: " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + ": onComplete ::::::: ");
            }
        });

        System.out.println(Thread.currentThread().getName() + ": Exit");
    }
}
