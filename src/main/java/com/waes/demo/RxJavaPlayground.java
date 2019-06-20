package com.waes.demo;

import io.reactivex.Observable;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class RxJavaPlayground {

    public static void main(String[] args) throws InterruptedException {
        final Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(3);
            // emitter.onError(new Exception("ups!"));
            emitter.onComplete();
        });
        observable
            .doOnNext(i -> print("first", i))
            .map(i -> i * 2)
            .doOnNext(i -> print("second", i))
//            .observeOn(Schedulers.computation())
//            .delay(100, TimeUnit.MILLISECONDS)
            .doOnNext(i -> print("third", i))
//            .subscribeOn(Schedulers.single())
            .subscribe(
                i -> print("forth", i),
                failure -> err.format("error: %s on %s", failure.getMessage(), currentThread().getName()).println(),
                () -> out.format("completed on %s", currentThread().getName()).println());

        observable.subscribe(out::println);

        Thread.sleep(1000);
    }

    private static void print(String checkpoint, Integer i) {
        out.format(checkpoint + " %s on %s", i, currentThread().getName()).println();
    }

}
