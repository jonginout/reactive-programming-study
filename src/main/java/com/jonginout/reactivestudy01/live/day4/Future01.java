package com.jonginout.reactivestudy01.live.day4;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class Future01 {
    /**
     * Future
     * 비동기적인 작업을 수행하고난 결과를 가지고 있는 것
     * 내가 작업을 수행하는 스레드 말고 새로운 스레드를 열어서 작업을 수행한다
     * -> 그리고 그 스레드에서 작업할 결과를 가져오는 기본이되는 인터페이스
     * <p>
     * 자바 8이 나오기 이전에는 비동기 작업에 결과를 가져오는 작업 대한 수단을 주로 Future를 사용함
     * 그런데 이게 Blocking이라는 단점이 있다..
     * -> 스프링에 사용하다 보면 -> 굳이 Future를 받을 필요가 없을 것 같은데? 라는 부분이 있을 것
     * -> 그래서 나오는 개념이 콜백 개념..을 사용하게 된다.
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        /**
         * 쓰레드풀이 있는 이유?
         * 원하는 쓰레드가 있으면 그 쓰레드를 사용하면 됨
         * 근데 쓰레드를 만들고 폐기하는 것 자체가 매우 큰 비용 (CPU나 메모리를 잡아먹음)
         * 1000번을 사용해야하는데 동시에는 쓰레드를 10개정도 밖에 안쓸 것 같은데..?
         * 그럼 10개짜리 쓰레드 풀을 만들어놓고 쓰레드 10개를 사용하고 사용이 끝나면 그 쓰레드를 날리지 않고 -> 반납
         * 그리고 또 다음작업이 그 쓰레드를 재활용해서 사용하고
         * 새로운 쓰레드를 만들고 지우는데 드는 리소스 소비를 최소화하기 위해
         *
         * newCachedThreadPool: 기본적으로는 MAX제한이 없고 쓰레드가 만들어져 있지 않음
         * 처음에 요청시에 쓰레드를 만들고 -> 다 사용하고 -> 반납하고 -> 또 요청하면 아까 사용했던 캐시되어있는 쓰레드 사용
         */
        ExecutorService es = Executors.newCachedThreadPool();

//        es.execute(() -> {
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException ignored) {
//            }
//            // asynchronous = 비동기 => Async
//            log.debug("Async 비동기 작업이다.. 1");
//        });

        /**
         * submit은 응답이 Future
         */
        Future<String> submit = es.submit(() -> {
            Thread.sleep(2000);
            log.debug("Async 비동기 작업이다.. 2");
            return "TEST";
        });

        log.debug(submit.isDone() + "");    // 2초가 걸리니깐 아직 안끝났겠지

        Thread.sleep(2100);

        log.debug("EXIT");

        log.debug(submit.isDone() + "");

        log.debug(submit.get());    // Blocking

        /**
         * 이걸 한다고 해서 비동기 작업이 바로 끝나진 않는다.
         * 오히려 이걸 안하면 ExecutorService 스레드가 하나 떠있어서 프로세스가 죽지 않는다.
         */
        es.shutdown();
    }
}
