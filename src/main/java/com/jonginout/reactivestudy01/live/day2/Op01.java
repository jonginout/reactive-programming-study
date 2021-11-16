package com.jonginout.reactivestudy01.live.day2;

import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Op01 {
    /**
     * Operator란? (연산자)
     * 데이터를 가공해주는 역할
     * Java에서 Stream을 정의해놓고 map filter 하는 이런거랑 비슷
     * <p>
     * 결국 아래처럼 데이터 파이프라인을 만드는게 핵심
     * Publisher -> [Data1] -> Operator1 -> [Data2] -> Operator2 -> [Data3] -> Subscriber
     */

    public static void main(String[] args) {
        /**
         * pub -> data1 -> mapPub -> data2 -> logSub 구독 이전 까진 publisher의 파이프라인을 만드는게 중요  // 여기는 (->)다운스트림이라고 한다.
         *                        <- subscribe(logSub)  구독시작                                    // 여기는 (<-)업스트림
         *                        -> onSubscribe(s)
         *                        -> onNext
         *                        -> onNext
         *                        ..
         *                        -> onComplete
         */
        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
        Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);
        Publisher<Integer> map2Pub = mapPub(mapPub, s -> s * -1);
        Publisher<Integer> reducePub = reducePub(map2Pub, 0, (BiFunction<Integer, Integer, Integer>) (a, b) -> a + b);    // BiFunction 파라미터 2개 받는 것
        Publisher<Integer> sumPub = sumPub(reducePub);
        sumPub.subscribe(logSub());

        Publisher<Integer> pub2 = iterPub(Stream.iterate(1, a -> a + 1).limit(10).collect(Collectors.toList()));
        Publisher<String> reducePub2 = reducePub(pub2, "", (BiFunction<String, Integer, String>) (a, b) -> a + b + "");
        Publisher<String> mapPub2 = mapPub(reducePub2, s -> s + 10 + "");
        Publisher<StringBuilder> reducePub3 = reducePub(reducePub2, new StringBuilder(), (BiFunction<StringBuilder, String, StringBuilder>) (a, b) -> a.append(b + ", "));
        mapPub2.subscribe(logSub());
    }

    private static <T, R> Publisher<R> mapPub(Publisher<T> publisher, Function<T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> subscriber) {
                publisher.subscribe(new DelegateSub<T, R>(subscriber) {
                    @Override
                    public void onNext(T item) {
                        subscriber.onNext(f.apply(item));
                    }
                });
            }
        };
    }

    private static <T, R> Publisher<R> reducePub(Publisher<T> publisher, R init, BiFunction<R, T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> subscriber) {
                publisher.subscribe(new DelegateSub<T, R>(subscriber) {
                    R result = init;

                    @Override
                    public void onNext(T item) {
                        result = f.apply(result, item);
                    }

                    @Override
                    public void onComplete() {
                        subscriber.onNext(result);
                        subscriber.onComplete();
                    }
                });
            }
        };
    }

    private static Publisher<Integer> sumPub(Publisher<Integer> publisher) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                publisher.subscribe(new DelegateSub<Integer, Integer>(subscriber) {
                    int sum = 0;

                    @Override
                    public void onNext(Integer item) {
                        sum += item;
                    }

                    @Override
                    public void onComplete() {
                        subscriber.onNext(sum);
                        subscriber.onComplete();
                    }
                });
            }
        };
    }

    private static Publisher<Integer> iterPub(List<Integer> iterator) {
        return new Publisher<>() {
            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(
                        new Subscription() {
                            @Override
                            public void request(long n) {
                                try {
                                    iterator.forEach(s -> subscriber.onNext(s));
                                    subscriber.onComplete();
                                } catch (Exception e) {
                                    subscriber.onError(e);
                                }
                            }

                            @Override
                            public void cancel() {

                            }
                        }
                );
            }
        };
    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(":::::: onSubscribe");
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
                System.out.println(":::::: onNext = " + item);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(":::::: onError = " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println(":::::: onComplete");
            }
        };
    }
}
