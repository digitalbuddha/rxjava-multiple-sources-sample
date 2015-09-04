package net.danlew.sample;

import rx.Observable;

/**
 * Simulates three different sources - one from memory, one from disk,
 * and one from network. In reality, they're all in-memory, but let's
 * play pretend.
 *
 * Observable.create() is used so that we always return the latest data
 * to the subscriber; if you use just() it will only return the data from
 * a certain point in time.
 */
public class Sources<T> {

    // Memory cache of data
    private T memory = null;

    // What's currently "written" on disk
    private T disk = null;

    // Each "network" response is different
    private int requestNumber = 0;
    
    private BehaviorSubject<T> updateStream;
    
    public Observable<T> get()
    {
         Observable<Data> source = Observable.concat(
                memory(),
                disk(),
                network()
            )
            .first();
    }
    
    public Observable<T> fresh()
    {
        network();    
    }
       

    // In order to simulate memory being cleared, but data still on disk
    public void clearMemory() {
        System.out.println("Wiping memory...");
        memory = null;
        //disk = null; not sure why you would ever want to delete instead of replace
    }

    private Observable<T> memory() {
        return Observable.create(subscriber -> {
            subscriber.onNext(memory);
            subscriber.onCompleted();
        });
    }

    private Observable<T> disk() {
        Observable<Data> observable = Observable.create(subscriber -> {
            subscriber.onNext(disk);
            subscriber.onCompleted();
        });

        // Cache disk responses in memory
        return observable.doOnNext(t -> memory = t)
    }

    private Observable<T> network() {
        Observable<Data> observable = Observable.create(subscriber -> {
            requestNumber++;
            subscriber.onNext(new NetworkRequest());
            subscriber.onCompleted();
        });

        // Save network responses to disk and cache in memory
        return observable.doOnNext(data -> {
                disk = data;
                memory = data;
            }).doOnNext(t -> updateStream.onNext(t);
    }
    
    public  Observable<T> update()
    {
       return updateStream.asObservable();
    }
    }
}
