package com.jonginout.reactivestudy01.live.day2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscription;

@RestController
public class Reactor01Controller {

    /**
     * spring5 부터는 Publisher로 리턴 가능
     */
    @GetMapping("/reactor01/hello")
    public Publisher<String> hello(String name) {
        return new Publisher<String>() {
            /**
             * 구독하는 코드(subscribe)가 없는데도 응답을 한다?
             * Spring MVC가 알아서 subscribe를 해서 응답을 넘겨줌
             */
            @Override
            public void subscribe(Flow.Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        subscriber.onNext("hello " + name);
                        subscriber.onComplete();
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }
}
