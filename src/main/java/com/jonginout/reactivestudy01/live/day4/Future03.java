package com.jonginout.reactivestudy01.live.day4;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * 비동기 작업의 결과를 가져오는 방법 2가지
 * 1. Future 같은 결과를 담고 있는 핸들러를 호출하는 방법
 * 2. Callback을 만드는 Callable
 */

@Slf4j
public class Future03 {
    interface SuccessCallback {
        void onSuccess(String result);
    }

    interface ExceptionCallback {
        void onError(Throwable throwable);
    }

    public static class CallbackFutureTask extends FutureTask<String> {
        SuccessCallback successCallback;
        ExceptionCallback exceptionCallback;

        public CallbackFutureTask(Callable<String> callable, SuccessCallback successCallback, ExceptionCallback exceptionCallback) {
            super(callable);
            this.successCallback = Objects.requireNonNull(successCallback);
            this.exceptionCallback = Objects.requireNonNull(exceptionCallback);
        }

        @Override
        protected void done() {
            try {
                successCallback.onSuccess(get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                exceptionCallback.onError(e.getCause());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        /**
         * FutureTask : Future 자체를 object로 받을 수 있게 하는
         * 약간 콜백 느낌
         */
        ExecutorService es = Executors.newCachedThreadPool();

        CallbackFutureTask futureTask = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            if (1 == 1) {
                throw new RuntimeException("Async Error");
            }
            log.debug("Async");
            return "Hello";
        },
                result -> log.debug("DONE!! ::::::::::: " + result),
                result -> log.debug("ERROR!! ::::::::::: " + result)
        );

        es.execute(futureTask);

        log.debug("EXIT");

        es.shutdown();
    }
}
