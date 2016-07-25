package com.joyplus.ad.Monitor;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.joyplus.ad.AdSDKFeature;
import com.joyplus.ad.data.MD5Util;
import com.joyplus.ad.data.TRACKINGURL;
import com.joyplus.ad.data.TRACKINGURL.TYPE;
import com.joyplus.ad.mode.ReportMode;

public class Monitor extends ReportMode {

    public final static String REPLACE_MAC = "%mac%";
    public final static String REPLACE_DM = "%dm%";
    public final static String REPLACE_IP = "%ip%";
    public final static String REPLACE_EX = "%ex%";

    public final static String REPLACE_ADMASTER_MAC = "_MAC_";
    public final static String REPLACE_ADMSATER_SN = "_SN_";
    public final static String REPLACE_ADMSATER_IMEI = "_IMEI_";
    public final static String REPLACE_ADMASTER_OS = "_OS_";


    private List<TRACKINGURL> mTrackingUrl = new ArrayList<TRACKINGURL>();

    private String MAC = "";//mac , mac , null
    private String PM = "";//ds . dm . null
    private String IP = "";//ip
    private String EX = "";//ex
    private String ADMASTER_SN = "";//
    private String ADMASTER_IMEI = "";//imei
    private String ADMASTER_OS = "0";//os

    public boolean CheckMonitor() {
        if (mTrackingUrl == null || mTrackingUrl.size() <= 0) return false;
        for (TRACKINGURL url : mTrackingUrl) {
            if (isSupportTRACKINGURL(url)) {
                return true;
            }
        }
        return false;
    }


    public String getADMASTER_SN() {
        return ADMASTER_SN;
    }

    public void setADMASTER_SN(String aDMASTER_SN) {
        ADMASTER_SN = aDMASTER_SN;
    }

    public String getADMASTER_IMEI() {
        return ADMASTER_IMEI;
    }

    public void setADMASTER_IMEI(String aDMASTER_IMEI) {
        ADMASTER_IMEI = aDMASTER_IMEI;
    }

    public void SetPM(String pm) {
        PM = pm;
    }

    public String GetPM() {
        if (TextUtils.isEmpty(PM)) return "";
        return PM;
    }

    public void SetMAC(String mac) {
        MAC = mac;
    }

    public String GetMAC() {
        if (TextUtils.isEmpty(MAC)) return "";
        return MD5Util.GetMD5Code(MAC);
    }

    public void SetIP(String ip) {
        IP = ip;
    }

    public String GetIP() {
        if (TextUtils.isEmpty(IP)) return "";
        return IP;
    }

    public void SetEX(String ex) {
        EX = ex;
    }

    public String GetEX() {
        if (TextUtils.isEmpty(EX)) return "";
        return EX;
    }

    public void SetTRACKINGURL(List<TRACKINGURL> urls) {
        mTrackingUrl = new ArrayList<TRACKINGURL>();
        if (urls != null && urls.size() > 0) {
            for (TRACKINGURL url : urls) {
                if (isUseableTRACKINGURL(url)) {
                    mTrackingUrl.add(url);//now we can sure it useable.
                }
            }
        }
    }

    public List<TRACKINGURL> GetTRACKINGURL() {
        if (mTrackingUrl == null)
            return new ArrayList<TRACKINGURL>();
        return mTrackingUrl;
    }

    private String Replace(String s, String d, String di) {
        String result = s;
        try {
            result = s.replaceAll(d, di);
        } catch (NullPointerException e) {
            e.printStackTrace();
            result = s;
        }
        return result;
    }

    public TRACKINGURL getReplaceTRACKINGURL(TRACKINGURL url) {
        if (isSupportTRACKINGURL(url)) {
            if ((TYPE.IRESEARCH == url.Type)
                    //||(TYPE.ADMASTER == url.Type)
                    //||(TYPE.MIAOZHEN == url.Type)
                    || (TYPE.NIELSEN == url.Type)
                    || TYPE.JOYPLUS == url.Type) {//we shoule replace url first.
                if (!(TextUtils.isEmpty(url.URL) || url.Monitored)) {
                    url.URL = Replace(url.URL, REPLACE_MAC, TextUtils.isEmpty(MAC) ? "" : MD5Util.GetMD5Code(MAC.toUpperCase()));
                    url.URL = Replace(url.URL, REPLACE_DM, TextUtils.isEmpty(PM) ? "" : PM);
                    url.URL = Replace(url.URL, REPLACE_IP, TextUtils.isEmpty(IP) ? "" : IP);
                    url.URL = Replace(url.URL, REPLACE_EX, TextUtils.isEmpty(EX) ? "" : EX);
                    ////for admaster
                    url.URL = Replace(url.URL, REPLACE_ADMASTER_MAC, TextUtils.isEmpty(MAC) ? "" : MD5Util.GetMD5Code(MAC.toUpperCase()));
                    url.URL = Replace(url.URL, REPLACE_ADMASTER_OS, ADMASTER_OS);
                    url.URL = Replace(url.URL, REPLACE_ADMSATER_IMEI, TextUtils.isEmpty(ADMASTER_IMEI) ? "" : ADMASTER_IMEI);
                    url.URL = Replace(url.URL, ADMASTER_SN, TextUtils.isEmpty(ADMASTER_SN) ? "" : ADMASTER_SN);
                }
            }
        }
        return url;
    }

    public static boolean isSupportTRACKINGURL(TRACKINGURL url) {
        if (url != null) {
            return ((AdSDKFeature.MONITOR_MIAOZHEN && TYPE.MIAOZHEN == url.Type)
                    || (AdSDKFeature.MONITOR_IRESEARCH && TYPE.IRESEARCH == url.Type)
                    || (AdSDKFeature.MONITOR_ADMASTER && TYPE.ADMASTER == url.Type)
                    || (AdSDKFeature.MONITOR_NIELSEN && TYPE.NIELSEN == url.Type)
                    || TYPE.JOYPLUS == url.Type);
        }
        return false;
    }

    public static boolean isUseableTRACKINGURL(TRACKINGURL url) {
        return (isSupportTRACKINGURL(url)
                && !TextUtils.isEmpty(url.URL) && !url.Monitored);
    }

    @Override
    public boolean isAviable() {
        // TODO Auto-generated method stub
        return CheckMonitor();
    }

    public boolean CanMonitor() {
        if (!isAviable()) return false;
        return (des() > 0);
    }
}
