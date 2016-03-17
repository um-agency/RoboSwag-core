package roboswag.org.storable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import roboswag.org.core.ObjectUtils;
import roboswag.org.core.exceptions.ObjectIsMutableException;
import roboswag.org.storable.exceptions.ConversionException;
import roboswag.org.storable.exceptions.MigrationException;
import roboswag.org.storable.exceptions.StoreException;
import roboswag.org.storable.exceptions.ValidationException;
import rx.Observable;
import rx.functions.Actions;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * TODO
 */
public abstract class Storable<TKey, TObject, TStoreObject> {

    private static final String LOG_TAG = "Storable";

    private static int globalLogLevel = Log.ERROR;
    private static boolean isInDebugMode = false;

    public static void setLogLevel(int logLevel) {
        globalLogLevel = logLevel;
    }

    public static void setDebugMode(boolean debugMode) {
        isInDebugMode = debugMode;
    }

    @NonNull
    private final String name;
    @NonNull
    private final TKey key;
    @NonNull
    private final Class<TObject> objectClass;
    @NonNull
    private final Class<TStoreObject> storeObjectClass;
    @NonNull
    private final Store<TKey, TStoreObject> store;
    @NonNull
    private final Converter<TObject, TStoreObject> converter;
    private final boolean cloneOnGet;
    @Nullable
    private final Migration<TKey> migration;
    @Nullable
    private final Validator<TObject> validator;
    @Nullable
    private final TObject defaultValue;

    @NonNull
    private final PublishSubject<TObject> valueSubject = PublishSubject.create();
    @NonNull
    private final Observable<TObject> valueObservable = Observable.<TObject>create(subscriber -> {
        try {
            subscriber.onNext(get());
        } catch (Exception throwable) {
            if (globalLogLevel <= Log.ERROR) {
                Log.e(LOG_TAG, "Error during get: " + Log.getStackTraceString(throwable));
            }
            subscriber.onError(throwable);
        }
    }).subscribeOn(Schedulers.io())
            .concatWith(valueSubject)
            .replay(1).autoConnect();

    @Nullable
    private CachedValue<TStoreObject> cachedStoreDefaultValue;
    @Nullable
    private CachedValue<TStoreObject> cachedStoreValue;
    @Nullable
    private CachedValue<TObject> cachedValue;

    protected Storable(@NonNull String name,
                       @NonNull TKey key,
                       @NonNull Class<TObject> objectClass,
                       @NonNull Class<TStoreObject> storeObjectClass,
                       @NonNull Store<TKey, TStoreObject> store,
                       @NonNull Converter<TObject, TStoreObject> converter,
                       boolean cloneOnGet,
                       @Nullable Migration<TKey> migration,
                       @Nullable Validator<TObject> validator,
                       @Nullable TObject defaultValue) {
        this.name = name;
        this.key = key;
        this.objectClass = objectClass;
        this.storeObjectClass = storeObjectClass;
        this.store = store;
        this.converter = converter;
        this.cloneOnGet = cloneOnGet;
        this.migration = migration;
        this.validator = validator;
        this.defaultValue = defaultValue;

        if (isInDebugMode && !cloneOnGet) {
            try {
                ObjectUtils.checkIfIsImmutable(objectClass);
            } catch (ObjectIsMutableException throwable) {
                Log.w(LOG_TAG, Log.getStackTraceString(throwable));
            }
        }
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public TKey getKey() {
        return key;
    }

    @NonNull
    public Store<TKey, TStoreObject> getStore() {
        return store;
    }

    @NonNull
    public Converter<TObject, TStoreObject> getConverter() {
        return converter;
    }

    @Nullable
    public TObject getDefaultValue() {
        return defaultValue;
    }

    @Nullable
    public Validator<TObject> getValidator() {
        return validator;
    }

    @NonNull
    private CachedValue<TStoreObject> getCachedStoreDefaultValue() throws ConversionException {
        if (cachedStoreDefaultValue == null) {
            cachedStoreDefaultValue = new CachedValue<>(converter.toStoreObject(objectClass, storeObjectClass, defaultValue));
        }
        return cachedStoreDefaultValue;
    }

    @Nullable
    private TStoreObject getStoreValue() throws StoreException, ConversionException, MigrationException {
        synchronized (this) {
            if (cachedStoreValue == null) {
                if (migration != null) {
                    try {
                        migration.migrateToLatestVersion(key);
                    } catch (MigrationException throwable) {
                        if (isInDebugMode) {
                            throw throwable;
                        } else if (globalLogLevel <= Log.ERROR) {
                            Log.e(LOG_TAG, "Error during migration: " + Log.getStackTraceString(throwable));
                        }
                    }
                }
                TStoreObject storeObject = store.loadObject(storeObjectClass, key);
                cachedStoreValue = storeObject == null && defaultValue != null
                        ? getCachedStoreDefaultValue()
                        : new CachedValue<>(storeObject);
            }

            return cachedStoreValue.value;
        }
    }

    @Nullable
    private TObject getDirectValue() throws StoreException, ConversionException {
        synchronized (this) {
            if (cachedValue == null) {
                TStoreObject storeObject = store.loadObject(storeObjectClass, key);
                cachedValue = storeObject == null && defaultValue != null
                        ? new CachedValue<>(defaultValue)
                        : new CachedValue<>(converter.toObject(objectClass, storeObjectClass, storeObject));
            }
            return cachedValue.value;
        }
    }

    @Nullable
    public TObject get() throws StoreException, ConversionException, MigrationException {
        synchronized (this) {
            if (cloneOnGet) {
                TStoreObject storeValue = getStoreValue();
                return storeValue != null ? converter.toObject(objectClass, storeObjectClass, storeValue) : null;
            } else {
                return getDirectValue();
            }
        }
    }

    private void updateCachedValue(@Nullable TObject value, @Nullable TStoreObject storeObject) throws ConversionException {
        cachedValue = null;
        cachedStoreValue = null;
        if (cloneOnGet) {
            cachedStoreValue = storeObject == null && defaultValue != null
                    ? getCachedStoreDefaultValue()
                    : new CachedValue<>(storeObject);
        } else {
            cachedValue = storeObject == null && defaultValue != null
                    ? new CachedValue<>(defaultValue)
                    : new CachedValue<>(value);
        }
    }

    public void set(@Nullable TObject value)
            throws ValidationException, ConversionException, StoreException, MigrationException {
        synchronized (this) {
            if (validator != null) {
                validator.validate(value);
            }

            TObject oldValue = null;
            if (!cloneOnGet && cachedValue != null) {
                oldValue = cachedValue.value;
                if (ObjectUtils.equals(oldValue, value)) {
                    return;
                }
            }

            TStoreObject valueToStore = converter.toStoreObject(objectClass, storeObjectClass, value);
            try {
                TStoreObject storedValue = getStoreValue();
                if (ObjectUtils.equals(storedValue, valueToStore)) {
                    return;
                }
                if (oldValue == null) {
                    oldValue = converter.toObject(objectClass, storeObjectClass, storedValue);
                }
            } catch (Exception throwable) {
                // some invalid value in store
                if (globalLogLevel <= Log.WARN) {
                    Log.w(LOG_TAG, "Can't get current store value: " + Log.getStackTraceString(throwable));
                }
            }

            store.storeObject(storeObjectClass, key, valueToStore);
            updateCachedValue(value, valueToStore);
            onValueChanged(get(), oldValue);
        }
    }

    public void setAsync(@Nullable TObject value) {
        setObservable(value).subscribe(Actions.empty(), this::onSetError);
    }

    private void onSetError(Throwable throwable) {
        if (globalLogLevel <= Log.ERROR) {
            Log.e(LOG_TAG, "Error during set: " + Log.getStackTraceString(throwable));
        }
    }

    public Observable<?> setObservable(@Nullable TObject value) {
        return Observable.create(subscriber -> {
            try {
                set(value);
            } catch (Exception throwable) {
                if (globalLogLevel <= Log.ERROR) {
                    Log.e(LOG_TAG, "Error during set: " + Log.getStackTraceString(throwable));
                }
                subscriber.onError(throwable);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());
    }

    public Observable<TObject> observe() {
        return valueObservable;
    }

    protected void onValueChanged(@Nullable TObject newValue, TObject oldValue) {
        valueSubject.onNext(newValue);
        if (globalLogLevel <= Log.INFO) {
            Log.w(LOG_TAG, "Value changed from '" + oldValue + "' to '" + newValue + "'");
        }
    }

    private class CachedValue<T> {

        @Nullable
        private final T value;

        private CachedValue(@Nullable T value) {
            this.value = value;
        }

    }

}
