package com.joyplus.ad.mode.inf;

import com.joyplus.ad.addownload.Download;


public interface iDownloaderListener {

    void DownloaderStateChange(Download download, int state);

    void DownloaderProgress(Download download, long Dwonloaded, long TotleSize);

    void DownloaderFinish(Download download);

}
