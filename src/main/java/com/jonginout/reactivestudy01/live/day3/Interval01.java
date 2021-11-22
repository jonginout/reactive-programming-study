package com.jonginout.reactivestudy01.live.day3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Interval01 {
    public static void main(String[] args) {
        Publisher<Integer> publisher = subscriber -> {
            subscriber.onSubscribe(new Subscription() {
                int no = 0;
                boolean canceled = false;

                @Override
                public void request(long n) {
                    ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
                    ses.scheduleAtFixedRate(() -> {
                        if (canceled) {
                            ses.shutdown();
                            return;
                        }
                        subscriber.onNext(no++);
                    }, 0, 300, TimeUnit.MILLISECONDS);
                }

                @Override
                public void cancel() {
                    canceled = true;
                }
            });
        };

        Publisher<Integer> take = subscriber -> {
            publisher.subscribe(new Subscriber<Integer>() {
                int count = 0;
                Subscription subscription;

                @Override
                public void onSubscribe(Subscription subscription) {
                    this.subscription = subscription;
                    subscriber.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer item) {
                    subscriber.onNext(item);
                    if (++count >= 5) {
                        subscription.cancel();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                }

                @Override
                public void onComplete() {
                    subscriber.onComplete();
                }
            });
        };

        take.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
                log.debug(":::::::: onSubscribe");
            }

            @Override
            public void onNext(Integer item) {
                log.debug(":::::::: onNext:{}", item);
            }

            @Override
            public void onError(Throwable throwable) {
                log.debug(":::::::: onError:{}", throwable);
            }

            @Override
            public void onComplete() {
                log.debug(":::::::: onComplete");
            }
        });

        log.debug("========> EXIT");
    }
}
