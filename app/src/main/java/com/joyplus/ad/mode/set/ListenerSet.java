package com.joyplus.ad.mode.set;

import java.util.ArrayList;
import java.util.List;

import com.joyplus.ad.mode.inf.iListener;

public abstract class ListenerSet implements iListener {

    protected List<Object> mListeners = new ArrayList<Object>();

    public boolean addListener(Object listener) {
        synchronized (mListeners) {
            if (listener != null && !mListeners.contains(listener)) {
                return mListeners.add(listener);
            }
        }
        return false;
    }

    public boolean removeListener(Object listener) {
        synchronized (mListeners) {
            if (listener != null) {
                return mListeners.remove(listener);
            }
        }
        return false;
    }
}
