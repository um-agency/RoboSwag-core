package ru.touchin.roboswag.core.data;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by Gavriil Sitnikov on 16/03/16.
 * TODO: description
 */
public class CacheEntry {

    @NonNull
    private final String key;
    private final long cachedTime;
    @NonNull
    private final Object data;

    public CacheEntry(@NonNull final String key, final long cachedTime, @NonNull final Object data) {
        this.key = key;
        this.cachedTime = cachedTime;
        this.data = data;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public long getCachedTime() {
        return cachedTime;
    }

    public boolean isExpired(final long expirationPeriod) {
        final long storeTime = new Date().getTime() - cachedTime;
        return storeTime >= 0 && storeTime < expirationPeriod;
    }

    @NonNull
    public Object getData() {
        return data;
    }

}
