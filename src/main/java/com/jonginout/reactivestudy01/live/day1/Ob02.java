package com.jonginout.reactivestudy01.live.day1;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecated")
public class Ob02 {
    /**
     자바안에 이미 Observable이라는 클래스가 이미 있다
     지금은 Deprecated 되긴 했음
     */

    /**
     * Observable -> Source -> Event -> Observer
     * 소스는 이벤트 소스를 의미
     * 이벤트 소스는 데이터를 담아(?) 하나의 이벤트를 Observer 즉 관찰자에게 보냄
     * <p>
     * Observer를 Observable에 등록시키는 것
     * Observable에는 Observer를 여러개 등록 가능
     */

    static class IntObservable extends Observable implements Runnable {

        @Override
        public void run() {
            for (int i = 1; i <= 10; i++) {
                setChanged();   // 변화가 생겼어!
                // 여기가 PUSH 하는 곳!
                notifyObservers(i); // 이벤트 쏴줘! 해줘! 사실상 브로드 캐스트

                // 우리가 리액티브에서 : 생각하는 Publisher 개념
                // 즉 데이터 만들어내는 소스쪽
            }
        }
    }

    public static void main(String[] args) {
        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // 이게 호출이 되면 현재 위 Observable 등록되어있는 모든 Observer에게
                System.out.println(
                        Thread.currentThread().getName() + " " + arg
                );

                // 우리가 리액티브에서 : 생각하는 Subscriber 개념
                // 데이터 받는 쪽
            }
        };

        IntObservable io = new IntObservable();
        // Observable에 observer 등록
        io.addObserver(observer);

        // 현재는 이게 다 같은 Main 쓰레드에서 동작
//        io.run();

        /*
        이벤트가 언제 일어날지 모르는데
        메인 쓰레드를 차단하면 안되니깐
        별도의 쓰레드에서 비동기적으로 동작을 시킬 수 도 있다!
         */

        // 쓰레드를 하나 할당 받아서
        ExecutorService es = Executors.newSingleThreadExecutor();
        // Runnable 구현했으니 바로 넣을 수 있다.
        es.execute(io);

        System.out.println(
                Thread.currentThread().getName() + " " + "EXIT"
        );

        es.shutdown();

        // 비동기 그 자체!!!!
    }

    /*
    Observer 패턴은 문제가 있다!
    1. 데이터가 다 왔는지 끝을 알 수 없다! Compete??
    2. Error 핸들링이 문제..
    --- notifyObservers 전후로 에러가 발생한다면??

    => 이런 문제의식에서 확장된게 리액티브 프로그래밍의 한축!
     */
}
