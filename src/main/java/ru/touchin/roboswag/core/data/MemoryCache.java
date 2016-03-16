package ru.touchin.roboswag.core.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Gavriil Sitnikov on 16/03/16.
 * TODO: description
 */
public interface MemoryCache {

    @Nullable
    CacheEntry get(@NonNull String key);

    void put(@NonNull String key, @NonNull Object data);

}
