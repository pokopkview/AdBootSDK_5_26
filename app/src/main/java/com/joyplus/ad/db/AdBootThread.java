package com.joyplus.ad.db;

import android.content.Context;

import com.joyplus.ad.AdConfig;
import com.joyplus.ad.Monitor.AdMonitorManager;
import com.joyplus.ad.Monitor.Monitor;
import com.joyplus.ad.PublisherId;
import com.joyplus.ad.application.AdCustomManagerCompat;
import com.joyplus.ad.application.CUSTOMINFO;
import com.joyplus.ad.config.Log;
import com.joyplus.ad.data.TRACKINGURL;
import com.joyplus.ad.report.AdReportManager;
import com.joyplus.ad.report.Report;

import java.util.ArrayList;
import java.util.List;


public class AdBootThread {
    private AdBootDao mAdBootDao;
    private CUSTOMINFO Info;
    private Context mContext;
    private static boolean Reported = false;

    public AdBootThread(Context context, CUSTOMINFO info) {
        mContext = context;
        Info = info;
    }

    public synchronized void StartReport() {
        if (Reported) return;
        mAdBootDao = AdBootDao.getInstance(mContext);
        if (mAdBootDao != null) {
            Reported = true;//make sure only report once.
            ArrayList<AdBootImpressionInfo> Info = mAdBootDao.GetAllReport();
            Log.d("StartReport-->" + (Info == null ? "NULL" : Info.size()));
            if (Info != null && Info.size() > 0) {
                for (AdBootImpressionInfo info : Info) {
                    if (info.Count < AdConfig.GetMaxSize()) {
                        for (int j = 0; j <= info.Count; j++) {
                            Log.d("Report-->" + info.toString());
                            Report(info);
                            ReportThird(info);
                        }
                    }
                }
            }
            mAdBootDao.delAll();//make sure it work.
        }
    }

    private void ReportThird(AdBootImpressionInfo info) {
        // TODO Auto-generated method stub
        Monitor m = AdCustomManagerCompat.createMonitor(Info);
        List<TRACKINGURL> URL = new ArrayList<TRACKINGURL>();
        TRACKINGURL admaster = AdCustomManagerCompat.createTRACKINGURL(info.admaster, TRACKINGURL.TYPE.ADMASTER);
        if (admaster != null) URL.add(admaster);
        TRACKINGURL iresearch = AdCustomManagerCompat.createTRACKINGURL(info.iresearch, TRACKINGURL.TYPE.IRESEARCH);
        if (iresearch != null) URL.add(iresearch);
        TRACKINGURL miaozhen = AdCustomManagerCompat.createTRACKINGURL(info.miaozhen, TRACKINGURL.TYPE.MIAOZHEN);
        if (miaozhen != null) URL.add(miaozhen);
        TRACKINGURL nielsen = AdCustomManagerCompat.createTRACKINGURL(info.nielsen, TRACKINGURL.TYPE.NIELSEN);
        if (nielsen != null) URL.add(nielsen);
        m.SetTRACKINGURL(URL);
        m.SetInfo(info);
        if (URL.size() > 0) AdMonitorManager.getInstance().AddMonitor(m);
    }

    private void Report(AdBootImpressionInfo info) {
        // TODO Auto-generated method stub
        if (info == null || !info.IsAviable()) return;
        Report r = new Report(mContext);
        r.SetPublisherId(new PublisherId(info.publisher_id));
        r.SetIMPRESSIONURL(AdCustomManagerCompat.createIMPRESSIONURL(info.mImpressionUrl));
        r.SetmInfo(info);
        Log.d("impressionurl:"+info.mImpressionUrl);
        AdReportManager.getInstance().AddReport(r);
    }
}
