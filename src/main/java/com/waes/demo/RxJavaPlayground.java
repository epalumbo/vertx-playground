package com.waes.demo;

import io.reactivex.Observable;

import static java.lang.Thread.currentThread;

public class RxJavaPlayground {

    public static void main(String[] args) throws InterruptedException {
        final Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(3);
            emitter.onComplete();
        });
        observable
            .doOnNext(i -> System.out.println("first " + i + " - " + currentThread().getName()))
            .map(i -> i * 2)
            .doOnNext(i -> System.out.println("second " + i + " - " + currentThread().getName()))
            //.observeOn(Schedulers.computation())
            .doOnNext(i -> System.out.println("third " + i + " - " + currentThread().getName()))
            //.subscribeOn(Schedulers.single())
            .subscribe(i -> System.out.println("forth " + i + " - " + currentThread().getName()));
        Thread.sleep(1000);
    }

}
