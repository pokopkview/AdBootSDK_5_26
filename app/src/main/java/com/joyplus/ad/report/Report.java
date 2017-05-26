package com.joyplus.ad.report;

import android.content.Context;
import android.text.TextUtils;

import com.joyplus.ad.AdBootDataUtil;
import com.joyplus.ad.PhoneManager;
import com.joyplus.ad.PublisherId;
import com.joyplus.ad.data.IMPRESSIONURL;
import com.joyplus.ad.data.MD5Util;
import com.joyplus.ad.db.AdBootImpressionInfo;
import com.miaozhen.mzmonitor.MZMonitor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.com.mma.mobile.tracking.api.Countly;

public class Report extends com.joyplus.ad.mode.ReportMode {

    private PublisherId mId;
    private IMPRESSIONURL mURL;
    private Context mContext;
    private AdBootImpressionInfo mInfo;

    public Report(Context context){
        this.mContext = context;
    }

    public boolean CanReport() {
        if (!isAviable()) return false;
        return (des() > 0);
    }

    public boolean Check() {
        if (!isAviable()) return false;
        if (mId == null || !mId.CheckId()) return false;
        return true;
    }

    public AdBootImpressionInfo GetmInfo() {
        return mInfo;
    }

    public void SetmInfo(AdBootImpressionInfo mInfo) {
        this.mInfo = mInfo;
    }

    public void SetIMPRESSIONURL(IMPRESSIONURL i) {
        if (mURL != null) return;
        mURL = i;
    }

    public IMPRESSIONURL GetIMPRESSIONURL() {
        if (mURL == null) return new IMPRESSIONURL();
        return mURL;
    }

    public PublisherId GetPublisherId() {
        return mId;
    }

    public void SetPublisherId(PublisherId id) {
        if (mId != null) return;
        mId = id;
        if (mId == null || !mId.CheckId()) SetNUM(0);
        //NUM = AdFileManager.getInstance().GetNum(mId);
    }

    @Override
    public boolean isAviable() {
        // TODO Auto-generated method stub
        if (mURL == null || TextUtils.isEmpty(mURL.URL)) return false;
        return true;
    }
    public void reportOTH(final String info) {
        System.out.println(info);
        if (info.contains("miaozhen")) {
            MZMonitor.retryCachedRequests(mContext);
            MZMonitor.adTrack(mContext, refactoryThridURL(info));
        } else if (info.contains("admaster")) {
            Countly.sharedInstance().onExpose(refactoryThridURL(info));
        }else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(refactoryURL(info));
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();
                        int requestCode = connection.getResponseCode();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
    private String refactoryThridURL(String url){
        if(url.indexOf("ns=__IP__") != -1){//如果不存在对应宏则在连接上加上该参数,存在则进行替换
            url.replace("__IP__", MD5Util.GetMD5Code(PhoneManager.getInstance().GetIp()));
        }else{
            url = url+"&ns="+MD5Util.GetMD5Code(PhoneManager.getInstance().GetIp());
        }
        if(url.indexOf("m6a=__MAC__") != -1){
            url.replace("__MAC__", MD5Util.GetMD5Code(AdBootDataUtil.getSN()));
        }else if(url.indexOf("m6=__MAC1__")!= -1){
            url.replace("__MAC1__",MD5Util.GetMD5Code(AdBootDataUtil.getSN()));
        }else if(url.indexOf("m6a=") == -1){
            url = url+"&m6a="+MD5Util.GetMD5Code(AdBootDataUtil.getSN());
        }
        if(url.indexOf("m1a=__ANDROIDID__") != -1){
            url.replace("__ANDROIDID__",MD5Util.GetMD5Code(PhoneManager.getInstance().GetDeviceId1()));
        }else if(url.indexOf("m1=__ANDROIDID1__") != -1){
            url.replace("__ANDROIDID1__",MD5Util.GetMD5Code(PhoneManager.getInstance().GetDeviceId1()));
        }else if(url.indexOf("ma1=") == -1){
            url = url+"&ma1="+MD5Util.GetMD5Code(PhoneManager.getInstance().GetDeviceId1());
        }
        return url;
    }
    private String refactoryURL(String url){
        if(url.indexOf("i=") != -1){
            if(url.charAt(url.indexOf("i=")+2) == '&' || url.endsWith("i=")){
                url = url.replace("i=","i="+MD5Util.GetMD5Code(PhoneManager.getInstance().GetMac()));
            }
        }else{
            url = url+"&i="+MD5Util.GetMD5Code(PhoneManager.getInstance().GetMac());
        }
        if(url.indexOf("sn=") == -1){
            url = url+"&sn="+AdBootDataUtil.getSN();
        }else if(url.charAt(url.indexOf("i=")+2) == '&' || url.endsWith("sn=")){
            url = url.replace("sn=", "sn="+AdBootDataUtil.getSN());
        }
        return url;
    }
}
