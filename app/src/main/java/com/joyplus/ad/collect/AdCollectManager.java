package com.joyplus.ad.collect;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.joyplus.ad.AdSDKManager;
import com.joyplus.ad.AdSDKManagerException;
import com.joyplus.ad.PhoneManager;
import com.joyplus.ad.application.AdCustomManagerCompat;
import com.joyplus.ad.application.COLLECTINFO;
import com.joyplus.ad.application.CUSTOMINFO;
import com.joyplus.ad.config.Log;
import com.joyplus.ad.report.AdReportManager;
import com.joyplus.ad.report.Report;

public class AdCollectManager {
    private Context mContext;
    private static AdCollectManager mInstance;

    public static AdCollectManager getInstance() {
        return mInstance;
    }

    public static void Init(Context context) throws AdSDKManagerException {
        if (AdSDKManager.IsInited()) return;
        if (context == null)
            throw new AdSDKManagerException("AdReportManager context is null !!!!!");
        mInstance = new AdCollectManager(context);
    }

    public synchronized void tearDown() {
        // TODO Auto-generated method stub
        mInstance = null;
    }

    private AdCollectManager(Context context) {
        mContext = context;
    }

    public void addPowerReport(CUSTOMINFO info, long poweron, long poweroff, long delay) throws AdSDKManagerException {
        if (poweron >= poweroff
                || poweroff <= 0 || delay <= 0 || (delay > (poweroff - poweron)) || !AdCustomManagerCompat.checkCustomInfo(info))
            throw new AdSDKManagerException("why report invalid power info !!!!!");
        PhoneManager.getInstance().SetCUSTOMINFO(info);//save it first
        reportURL(getReportURL(info, poweron, poweroff, delay));
    }

    public void addInputReport(CUSTOMINFO info, COLLECTINFO.INPUTTYPE type, long time) throws AdSDKManagerException {
        if (type == null || time <= 0 || !AdCustomManagerCompat.checkCustomInfo(info))
            throw new AdSDKManagerException("why report invalid INPUTTYPE info !!!!!");
        PhoneManager.getInstance().SetCUSTOMINFO(info);//save it first
        reportURL(getReportURL(info, type, time));
    }

    private boolean reportURL(String url) {
        Log.d("AdCollectManager reportURL " + url);
        if (!TextUtils.isEmpty(url)) {
            AdReportManager ad = AdReportManager.getInstance();
            if (ad != null) {
                Report report = new Report();
                report.SetIMPRESSIONURL(AdCustomManagerCompat.createIMPRESSIONURL(url));
                ad.AddReport(report);
                return true;
            }
        }
        return false;
    }

    private String getReportURL(CUSTOMINFO info, COLLECTINFO.INPUTTYPE type, long time) {
        Uri.Builder b = AdCustomManagerCompat.createBaseReportURL(info);
        b.appendQueryParameter("inputtype", Integer.toString(type.toInt()));
        b.appendQueryParameter("inputtime", Long.toString(time));
        return b.build().toString();
    }

    private String getReportURL(CUSTOMINFO info, long poweron, long poweroff, long delay) {
        Uri.Builder b = AdCustomManagerCompat.createBaseReportURL(info);
        b.appendQueryParameter("poweron", Long.toString(poweron));
        b.appendQueryParameter("poweroff", Long.toString(poweroff));
        b.appendQueryParameter("powerdelay", Long.toString(delay));
        return b.build().toString();
    }
}
