package com.joyplus.ad.application;

import android.net.Uri;
import android.text.TextUtils;

import com.joyplus.ad.AdConfig;
import com.joyplus.ad.AdSDKManager;
import com.joyplus.ad.AdSDKManagerException;
import com.joyplus.ad.PhoneManager;
import com.joyplus.ad.Monitor.Monitor;
import com.joyplus.ad.collect.AdCollectManager;
import com.joyplus.ad.data.IMPRESSIONURL;
import com.joyplus.ad.data.MD5Util;
import com.joyplus.ad.data.TRACKINGURL;

public class AdCustomManagerCompat {

    public static void addPowerReport(CUSTOMINFO info, long poweron, long poweroff, long delay) throws AdSDKManagerException {
        if (isReadly()) {
            AdCollectManager.getInstance().addPowerReport(info, poweron, poweroff, delay);
        }
    }

    public static void addInputReport(CUSTOMINFO info, COLLECTINFO.INPUTTYPE type, long time) throws AdSDKManagerException {
        if (isReadly()) {
            AdCollectManager.getInstance().addInputReport(info, type, time);
        }
    }

    public static boolean isSDKInited() {
        return AdSDKManager.IsInited();
    }

    public static boolean isReadly() throws AdSDKManagerException {
        if (!isSDKInited())
            throw new AdSDKManagerException("SDK no inited, Pls init it first");
        return true;
    }

    public static boolean checkCustomInfo(CUSTOMINFO info) {
        if (info == null) {
            throw new IllegalArgumentException("dm cannot be null or empty");
        }
        return info.check();
    }

    ////////// Monitor
    public static Monitor createMonitor() {
        return createMonitor(null);
    }

    public static Monitor createMonitor(CUSTOMINFO info) {
        try {
            Monitor m = new Monitor();
            CUSTOMINFO mCUSTOMINFO = info;
            if (mCUSTOMINFO == null) mCUSTOMINFO = PhoneManager.getInstance().GetCUSTOMINFO();
            if (mCUSTOMINFO != null) {
                if (!(TextUtils.isEmpty(mCUSTOMINFO.GetDEVICEMOVEMENT()))) {//dm
                    m.SetPM(mCUSTOMINFO.GetDEVICEMOVEMENT());
                } else if (!(TextUtils.isEmpty(mCUSTOMINFO.GetDEVICEMUMBER()))) {//ds
                    m.SetPM(mCUSTOMINFO.GetDEVICEMUMBER());
                }
                if (!(TextUtils.isEmpty(mCUSTOMINFO.GetMAC()))) {//mac
                    m.SetMAC(mCUSTOMINFO.GetMAC());
                }
                if (!(TextUtils.isEmpty(mCUSTOMINFO.GetSN()))) {//for sn
                    m.setADMASTER_SN(mCUSTOMINFO.GetSN());
                }
                m.setADMASTER_IMEI(PhoneManager.getInstance().GetDeviceId1());
            }
            return m;
        } catch (Throwable e) {
        }
        return new Monitor();
    }

    ////////////////// TRACKINGURL
    public static TRACKINGURL createTRACKINGURL(String url, TRACKINGURL.TYPE Type) {
        return createTRACKINGURL(null, url, Type);
    }

    public static TRACKINGURL createTRACKINGURL(CUSTOMINFO info, String url, TRACKINGURL.TYPE Type) {
        TRACKINGURL URL = new TRACKINGURL();
        URL.Type = Type;
        URL.URL = url;
        return URL;
    }

    //////////////// IMPRESSIONURL
    public static IMPRESSIONURL createIMPRESSIONURL(String url) {
        return createIMPRESSIONURL(null, url);
    }

    public static IMPRESSIONURL createIMPRESSIONURL(CUSTOMINFO info, String url) {
        IMPRESSIONURL impressionurl = new IMPRESSIONURL();
        impressionurl.URL = url;
        return impressionurl;
    }

    /////////////// createBaseReportURL
    public static Uri.Builder createBaseReportURL() {
        return createBaseReportURL(PhoneManager.getInstance().GetCUSTOMINFO());
    }

    public static Uri.Builder createBaseReportURL(CUSTOMINFO custominfo) {
        final Uri.Builder b = Uri.parse(AdConfig.GetBaseReportURL()).buildUpon();
        if (custominfo == null)
            custominfo = PhoneManager.getInstance().GetCUSTOMINFO();//last check.
        b.appendQueryParameter("sdkversion", AdConfig.GetSDKVersion());
        b.appendQueryParameter("sdk", "open");
        b.appendQueryParameter("b", AdSDKManager.GetCustomType().toString());
        b.appendQueryParameter("sn", (custominfo == null ? "" : custominfo.GetSN()));
        b.appendQueryParameter("ds", (custominfo == null ? "" : custominfo.GetDEVICEMUMBER()));
        b.appendQueryParameter("dm", (custominfo == null ? "" : custominfo.GetDEVICEMOVEMENT()));
        b.appendQueryParameter("i", (custominfo == null ? "" :
                (TextUtils.isEmpty(custominfo.GetMAC()) ? "" : MD5Util.GetMD5Code(custominfo.GetMAC().toUpperCase()))));
        return b;
    }
}
