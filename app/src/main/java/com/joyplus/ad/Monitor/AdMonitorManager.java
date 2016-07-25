package com.joyplus.ad.Monitor;

import java.util.List;

import cn.com.mma.mobile.tracking.api.Countly;

import com.joyplus.ad.AdSDKManager;
import com.joyplus.ad.AdSDKManagerException;
import com.joyplus.ad.HttpManager;
import com.joyplus.ad.config.Log;
import com.joyplus.ad.data.TRACKINGURL;
import com.joyplus.ad.mode.ReportMode;
import com.joyplus.ad.mode.ReportModeController;
import com.miaozhen.mzmonitor.MZMonitor;

import android.content.Context;

public class AdMonitorManager extends ReportModeController {

    private Context mContext;
    private static AdMonitorManager mAdMonitorManager;

    public static void Init(Context context) throws AdSDKManagerException {
        if (AdSDKManager.IsInited()) return;
        if (context == null)
            throw new AdSDKManagerException("AdMonitorManager context is null !!!!!");
        mAdMonitorManager = new AdMonitorManager(context);
    }

    @Override
    public synchronized void tearDown() {
        // TODO Auto-generated method stub
        super.tearDown();
        mAdMonitorManager = null;
    }

    public static AdMonitorManager getInstance() {
        return mAdMonitorManager;
    }

    private AdMonitorManager(Context context) {
        mContext = context;
    }

    //Interface for Application
    public void AddMonitor(Monitor url) {
        if (url == null) return;
        addReportUri(url);
    }

    public void AddMonitor(List<Monitor> url) {
        if (url == null) return;
        addReportUri(url);
    }

    @Override
    public int getReportControllerType() {
        // TODO Auto-generated method stub
        return ReportMode.TYPE_MONITOR;
    }

    @Override
    public boolean handlerReport(ReportMode report) {
        // TODO Auto-generated method stub
        if (report instanceof Monitor) {
            MonitorService((Monitor) report);
        }
        return false;
    }

    private void MonitorService(Monitor monitor) {
        while (monitor != null && monitor.CanMonitor()) {
            try {
                List<TRACKINGURL> trs = monitor.GetTRACKINGURL();
                for (TRACKINGURL tr : trs) {
                    TRACKINGURLService(monitor.getReplaceTRACKINGURL(tr));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void TRACKINGURLService(TRACKINGURL iTrackingurl) {
        Log.d("MonitorService -->" + (iTrackingurl == null ? "" : iTrackingurl.toString()));
        if (Monitor.isUseableTRACKINGURL(iTrackingurl)) {
            switch (iTrackingurl.Type) {
                case ADMASTER: {
                    Countly.sharedInstance().onExpose(iTrackingurl.URL);
                }
                break;
                case MIAOZHEN: {
                    if (mContext != null) {
                        MZMonitor.retryCachedRequests(mContext);
                        MZMonitor.adTrack(mContext, iTrackingurl.URL);
                    }
                }
                break;
                case NIELSEN:
                case IRESEARCH:
                case JOYPLUS: {
                    HttpManager.ReportServiceOneTime(iTrackingurl.URL);
                }
                break;
            }
        }
    }
}
