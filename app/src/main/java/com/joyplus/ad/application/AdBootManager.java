package com.joyplus.ad.application;

import java.lang.Thread.UncaughtExceptionHandler;

import com.joyplus.ad.AdBootDownloadManager;
import com.joyplus.ad.AdConfig;
import com.joyplus.ad.AdMode;
import com.joyplus.ad.AdManager.AD;
import com.joyplus.ad.PhoneManager;
import com.joyplus.ad.PublisherId;
import com.joyplus.ad.config.Log;
import com.joyplus.ad.data.ADBOOT;
import com.joyplus.ad.data.AdBootRequest;
import com.joyplus.ad.db.AdBootImpressionInfo;
import com.joyplus.ad.db.AdBootTempDao;
import com.joyplus.ad.db.AdBootThread;

import android.content.Context;

public class AdBootManager extends AdMode {

    private Context mContext;
    private AdBoot mAdBoot;//
    private AdBootRequest mAdBootRequest;
    private Thread mAdBootRequestThread;
    private AdBootDownloadManager mDownloadManager;
    private final static int TIME = 10;

    private AdBootManager() {
        super(AD.ADBOOT);
    }

    //can't instance this by no param.
    public AdBootManager(Context context, AdBoot info) {
        super(AD.ADBOOT);
        if (info == null) throw new IllegalArgumentException("AdBoot cannot be null or empty");
        if (context == null) throw new IllegalArgumentException("Context cannot be null or empty");
        mContext = context;
        mAdBoot = new AdBoot(info.GetCUSTOMINFO(), info.GetAdBootInfo(), info.GetPublisherId());
        mPublisherId = new PublisherId(mAdBoot.GetPublisherId().GetPublisherId());
        if (mAdBoot.GetCUSTOMINFO() == null || mAdBoot.GetCUSTOMINFO().GetDEVICEMOVEMENT() == null || "".equals(mAdBoot.GetCUSTOMINFO().GetDEVICEMOVEMENT()))
            throw new IllegalArgumentException("dm cannot be null or empty");
        PhoneManager.getInstance().SetCUSTOMINFO(mAdBoot.GetCUSTOMINFO());//update custom info.
        mDownloadManager = new AdBootDownloadManager(mContext, this, mAdBoot);
    }


    @Override
    public void RequestAD() {
        // TODO Auto-generated method stub
        Log.d("AdBootManager RequestAD start ++++++++++++++++++++++++++" + (mAdBootRequestThread == null));
        if (mAdBootRequestThread == null) {
            mAdBootRequestThread = new Thread() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    super.run();
                    Request();// for request ad.
                    mAdBootRequestThread = null;
                }
            };
            mAdBootRequestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                                                                 @Override
                                                                 public void uncaughtException(Thread thread, Throwable ex) {
                                                                     // TODO Auto-generated method stub
                                                                     mAdBootRequest = null;
                                                                     mAdBootRequestThread = null;
                                                                     notifyNoAn();
                                                                 }
                                                             }
            );
            mAdBootRequestThread.start();
        }
    }

    private void Request() {
        Log.d("Request start ++++++++++++++++++++++++++");
        if (mAdBootRequest != null) return;
        if (!CheckRequestAble()) {//can't request AD now.so we can return.
            mAdBootRequest = null;
            return;
        }
        mAdBootRequest = new AdBootRequest(AdBootManager.this, mAdBoot);
        ADBOOT mADBOOT = null;
        int Count = TIME;
        while ((Count--) > 0) {
            try {
                Thread.sleep(500);
                mADBOOT = null;
                mADBOOT = mAdBootRequest.sendRequest();
                break;
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                mADBOOT = null;
                e.printStackTrace();
            }
        }
        if (mDownloadManager != null && mADBOOT != null) {
            AdBootTempDao.getInstance(mContext).Remove(mPublisherId.GetPublisherId());//remove it first.
            new AdBootThread(mContext, ((mAdBoot != null && mAdBoot.GetCUSTOMINFO() != null) ? mAdBoot.GetCUSTOMINFO() : null)).StartReport();
            mDownloadManager.UpdateADBOOT(mADBOOT, mAdBootRequest.GetFileName(), mPublisherId);
        } else {
            notifyNoAn();
        }
        mAdBootRequest = null;
    }

    private boolean CheckRequestAble() {
        if (AdConfig.GetREQUESTALWAYS()) return true;
        AdBootImpressionInfo info = AdBootTempDao.getInstance(mContext).GetOne(mPublisherId.GetPublisherId());
        if (info != null && info.Count < 1) {
            if (mAdBootListener != null) {//last AD no show.so we return finish this request.
                mAdBootListener.Finish();
            }
            return false;
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    public void notifyNoAD() {
        if (mAdBootListener != null) {
            mAdBootListener.NoAd();
        }
    }

    public void notifyNoAn() {
        if (mAdBootListener != null) {
            mAdBootListener.NoAn();
        }
    }

    public void notifyDownLoadProgress(String TargetFile, long complete, long Totle) {
        if (mAdBootListener != null) {
            mAdBootListener.DownLoadProgress(TargetFile, complete, Totle);
        }
    }

    public void notifyfinish() {
        if (mAdBootListener != null) {
            mAdBootListener.Finish();
        }
    }

    private AdBootListener mAdBootListener;

    public void SetAdBootListener(AdBootListener listener) {
        mAdBootListener = listener;
    }
}
