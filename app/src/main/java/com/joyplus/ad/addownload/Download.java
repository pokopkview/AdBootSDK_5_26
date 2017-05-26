package com.joyplus.ad.addownload;

import android.text.TextUtils;
import android.webkit.URLUtil;

import com.joyplus.ad.mode.inf.iDownloaderListener;
import com.joyplus.ad.mode.set.DownloaderListenerSet;


public class Download extends com.joyplus.ad.mode.ReportMode implements iDownloaderListener {
    public final static int STATE_IDLE = 1;
    public final static int STATE_DOWNLOAD = 2;
    public final static int STATE_SUCCESS = 3;
    public final static int STATE_FAIL = 4;
    public final static int STATE_FINISH = 5;

    public String URL;

    public boolean WriteToTargetFile = true;
    public String TargetFile;
    public String filehashCode;
    public int State = STATE_IDLE;

    public Download() {
    }

    public Download(Download down) {
        if (down != null) {
            URL = down.URL;
            WriteToTargetFile = down.WriteToTargetFile;
            TargetFile = down.TargetFile;
            State = down.State;
            filehashCode = down.filehashCode;
        }
    }

    public Download CreateNew() {
        return new Download(this);
    }

    public boolean Check() {
        System.out.println("Check__"+URL+"_"+TargetFile+"_"+filehashCode);
        if (!URLUtil.isNetworkUrl(URL)
                || TextUtils.isEmpty(TargetFile)
                || TextUtils.isEmpty(filehashCode)) return false;
        return true;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuffer ap = new StringBuffer();
        ap.append("Download={")
                .append(" URL=" + URL)
                .append(" ,WriteToTargetFile=" + WriteToTargetFile)
                .append(" ,TargetFile=" + TargetFile)
                .append(" ,filehashCode=" + filehashCode)
                .append(" ,State=" + State)
                .append(" }");
        return ap.toString();
    }

    @Override
    public boolean isAviable() {
        // TODO Auto-generated method stub
        return Check();
    }

    //////////////////////////
    private DownloaderListenerSet mDownloaderListenerSet = new DownloaderListenerSet();

    @Override
    public void DownloaderStateChange(Download download, int state) {
        // TODO Auto-generated method stub
        mDownloaderListenerSet.DownloaderStateChange(this, state);
    }

    @Override
    public void DownloaderProgress(Download download, long Dwonloaded,
                                   long TotleSize) {
        // TODO Auto-generated method stub
        mDownloaderListenerSet.DownloaderProgress(this, Dwonloaded, TotleSize);
    }

    @Override
    public void DownloaderFinish(Download download) {
        // TODO Auto-generated method stub
        mDownloaderListenerSet.DownloaderFinish(this);
    }

    public boolean addDownloaderListener(iDownloaderListener listener) {
        return mDownloaderListenerSet.addListener(listener);
    }

    public boolean removeDownloaderListener(iDownloaderListener listener) {
        return mDownloaderListenerSet.removeListener(listener);
    }
}
