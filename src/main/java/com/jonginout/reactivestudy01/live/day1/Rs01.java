package com.jonginout.reactivestudy01.live.day1;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;

public class Rs01 {
    /*
    - Reactive Streams (리액티브 스트림스)
    - Observer 패턴의 2가지 문제를 해결함
     */
    public static void main(String[] args) throws InterruptedException {

        /**
         Publisher (Observer 패턴에서는 : Observable)
         Subscriber (Observer 패턴에서는 : Observer)
         */

        Iterable<Integer> itr = Arrays.asList(1, 2, 3, 4, 5);

        ExecutorService es = Executors.newSingleThreadExecutor();

        Publisher publisher = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> it = itr.iterator();

                Subscription subscription = new Subscription() {
                    @Override
                    // Back pressure (배압)을 위해서 가져올 개수를 정할 수 있음
                    // 무조건 떙겨오거나 푸시하는건 결국 성능에 문제를 줄 수 있을테니깐?
                    public void request(long n) {
                        // 요청
                        es.execute(() -> {
                            int i = 0;
                            try {
                                while (i++ < n) {
                                    if (it.hasNext()) {
                                        subscriber.onNext(it.next());
                                    } else {
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                subscriber.onError(e);
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                        // 요청
                    }
                };
                subscriber.onSubscribe(subscription);
            }
        };

        /**
         subscriber는 퍼블리셔가 시퀀셜하게 보내준다고 가정하고 받는다
         즉, publisher는 멀티 쓰레드에서 동시에 여러개를 보내는건 못한다.
         (즉 한 subscription에 대해서는 onNext는 한 쓰레드에서)
         - 어느 한 순간에는 한쓰레드에서 오는걸 보장 : 동시성 문제 어느정도 해결
         */
        Subscriber subscriber = new Subscriber() {
            int bufferSize = 2;
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                // 구독시
                System.out.println(Thread.currentThread().getName() + ":::::::: onSubscribe");
                this.subscription = subscription;
                this.subscription.request(2);
            }

            @Override
            public void onNext(Object item) {
                // 다음거
                System.out.println(Thread.currentThread().getName() + ":::::::: onNext " + item);
                if (--bufferSize <= 0) {
                    bufferSize = 2;
                    this.subscription.request(2);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                // 에러시
                System.out.println(":::::::: onError " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                // 완료시
                System.out.println(Thread.currentThread().getName() + ":::::::: onComplete");
            }
        };

        // 발행자가 구독을 함
        publisher.subscribe(subscriber);

        es.awaitTermination(10, TimeUnit.HOURS);
        es.shutdown();
    }
}
