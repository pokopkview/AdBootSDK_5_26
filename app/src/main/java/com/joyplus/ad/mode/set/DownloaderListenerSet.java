package com.joyplus.ad.mode.set;

import com.joyplus.ad.addownload.Download;
import com.joyplus.ad.mode.inf.iDownloaderListener;

public class DownloaderListenerSet extends ModeListenerSet implements iDownloaderListener {
    public final static int FLAG_STATE_CHANGE = 10000;
    public final static int FLAG_PROGRESS_CHANGE = 10001;
    public final static int FLAG_STATE_FINISH = 10002;

    @Override
    public boolean notifyListener(int flag, Object listener, Object... objects) {
        // TODO Auto-generated method stub
        switch (flag) {
            case FLAG_STATE_CHANGE: {
                ((iDownloaderListener) listener).DownloaderStateChange((Download) objects[0], (Integer) objects[1]);
            }
            break;
            case FLAG_PROGRESS_CHANGE: {
                ((iDownloaderListener) listener).DownloaderProgress((Download) objects[0], (Integer) objects[1], (Integer) objects[2]);
            }
            break;
            case FLAG_STATE_FINISH: {
                ((iDownloaderListener) listener).DownloaderFinish((Download) objects[0]);
            }
            break;
        }
        return false;
    }

    @Override
    public void DownloaderStateChange(Download download, int state) {
        // TODO Auto-generated method stub
        pendNotifyListener(FLAG_STATE_CHANGE, download, state);
    }

    @Override
    public void DownloaderProgress(Download download, long Dwonloaded, long TotleSize) {
        // TODO Auto-generated method stub
        pendNotifyListener(FLAG_PROGRESS_CHANGE, download, Dwonloaded, TotleSize);
    }

    @Override
    public void DownloaderFinish(Download download) {
        // TODO Auto-generated method stub
        pendNotifyListener(FLAG_STATE_FINISH, download);
    }

}
