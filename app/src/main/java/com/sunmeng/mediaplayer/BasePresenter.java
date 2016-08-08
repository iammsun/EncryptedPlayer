package com.sunmeng.mediaplayer;

import android.support.annotation.NonNull;

/**
 * Created by sunmeng on 16/8/8.
 */
public interface BasePresenter<V> {

    void attachView(@NonNull V view);

    void detachView();
}