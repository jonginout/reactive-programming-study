package com.jonginout.reactivestudy01.live.day1;

import java.util.Arrays;
import java.util.Iterator;

public class Ob01 {
    /**
     * 외부에 이벤트가 발생하면 거기에 대응을 하는 방식으로 코드가 작성되는것
     * 이게 통틀어서 -> 리액티브 프로그래밍
     * <p>
     * Duality (쌍대성)
     * Observer Pattern (리스터, 이벤트)
     * Reactive Streams (표준, JVM 기반 생태계에서 만든 표준)
     * ==> JAVA9 부터는 JDK에 들어감
     */
    public static void main(String[] args) {

        // List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // 여러개 있는 데이터를 사용하는 방법?
        // List 등.. Iterable
        // List 타입은 Iterable 인터페이스의 하나의 서브 타입이다
        // List -> Collection -> Iterable
        /**
         * Iterable의 특이점
         * 이 인터페이스를 사용하는 경우에는 for-each loop를 사용할 수 있다!
         * 아래와 같이 (우리가 생각하는 ForEach 말고)
         */
//        for (Integer i : iter) {
//            System.out.println(i);
//        }
        // Iterable이기 때문에 위 loop를 사용할 수 있다.

        // Collection이 아니더라도
        // Iterable이라는건 여러개의 원소를 하나씩 순회 할 수 있다라는 것

        // Iterable안에 있는 여러 원소들을 순회 할 수 있게 해주는 도구 : Iterator

        Iterable<Integer> iterCustom = () -> new Iterator() {
            int i = 0;
            final static int MAX = 10;

            @Override
            public boolean hasNext() {
                return i < MAX;
            }

            @Override
            public Integer next() {
                // 여기가 PULL 하는 곳!
                return ++i;
            }
        };

        for (Integer i : iterCustom) {
            System.out.println(i);
        }
    }

    /**
     Iterable(Pulling 방식) <--쌍대성--> Observable(Push 방식)
     : 개리마이어
     사실은 두가지의 궁극적 기능은 똑같은데 반대로 구현한 것?

     Pulling : 내껄 줘! 소스를 작성하는 쪽에서 하나를 끌어오는 것
     PusH : 여기 줄께! 소스 쪽에서 밀어 넣어 주는 것
     */
}
