package ru.touchin.roboswag.core.observables.storable;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;

import ru.touchin.roboswag.core.observables.Changeable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Пул потоков для работы с {@link Changeable}.
 */
public final class StorableThreadPool {

    private static final int POOL = 4;

    @NonNull
    private static final StorableThreadPool instance = new StorableThreadPool();
    @NonNull
    private static final Random random = new Random();

    @NonNull
    public static StorableThreadPool getInstance() {
        return instance;
    }

    @NonNull
    private final ArrayList<Scheduler> schedulers = new ArrayList<>();

    private StorableThreadPool() {
        synchronized (schedulers) {
            for (int i = 0; i < POOL; i++) {
                schedulers.add(Schedulers.from(Executors.newSingleThreadScheduledExecutor()));
            }
        }
    }

    @NonNull
    public Scheduler getScheduler() {
        synchronized (random) {
            return schedulers.get(random.nextInt(schedulers.size()));
        }
    }
}
