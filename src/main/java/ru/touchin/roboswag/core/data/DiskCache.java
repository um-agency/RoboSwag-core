package ru.touchin.roboswag.core.data;

import android.support.annotation.NonNull;

import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 16/03/16.
 * TODO: description
 */
public interface DiskCache {

    @NonNull
    Observable<CacheEntry> get(@NonNull String key);

    void put(@NonNull String key, @NonNull Object data);

}
