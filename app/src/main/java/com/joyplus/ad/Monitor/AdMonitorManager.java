package com.joyplus.ad.Monitor;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.joyplus.ad.AdBootDataUtil;
import com.joyplus.ad.AdSDKManager;
import com.joyplus.ad.AdSDKManagerException;
import com.joyplus.ad.HttpManager;
import com.joyplus.ad.config.Log;
import com.joyplus.ad.data.MD5Util;
import com.joyplus.ad.data.PhoneService;
import com.joyplus.ad.data.TRACKINGURL;
import com.joyplus.ad.db.AdBootImpressionInfo;
import com.joyplus.ad.db.AdBootReprtDao;
import com.joyplus.ad.mode.ReportMode;
import com.joyplus.ad.mode.ReportModeController;
import com.miaozhen.mzmonitor.MZMonitor;
import com.miaozhen.tvmonitor.MZTVMonitor;

import java.util.List;

import cn.com.mma.mobile.tracking.api.Countly;

public class AdMonitorManager extends ReportModeController {

    private Context mContext;
    private static AdMonitorManager mAdMonitorManager;
    private static PhoneService pService;

    public static void Init(Context context) throws AdSDKManagerException {
        pService = new PhoneService();
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
                    TRACKINGURLService(monitor.getReplaceTRACKINGURL(tr),monitor.GetInfo());
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void TRACKINGURLService(TRACKINGURL iTrackingurl, AdBootImpressionInfo info) {
        Log.d("MonitorService -->" + (iTrackingurl == null ? "" : iTrackingurl.toString()));
        if (Monitor.isUseableTRACKINGURL(iTrackingurl)) {
            switch (iTrackingurl.Type) {
                case ADMASTER: {
                    Countly.sharedInstance().onExpose(refactoryThridURL(iTrackingurl.URL));
                }
                break;
                case MIAOZHEN: {
                    if (mContext != null) {
                        MZTVMonitor.retryCachedRequests(mContext);
                        MZTVMonitor.adTrack(mContext, refactoryThridURL(iTrackingurl.URL));
//                        MZMonitor.retryCachedRequests(mContext);
//                        MZMonitor.adTrack(mContext, refactoryThridURL(iTrackingurl.URL));
                    }
                }
                break;
                case NIELSEN:
                case IRESEARCH:
                case JOYPLUS: {
                    if(!HttpManager.ReportServiceOneTime(refactoryURL(iTrackingurl.URL))){
                        ContentValues values = new ContentValues();
                        values.put("publisher_id",info.publisher_id);
                        values.put("report_url",refactoryURL(iTrackingurl.URL));
                        values.put("Count", 0);
                        values.put("type",1);
                        AdBootReprtDao.getInstance(mContext).InsertOneInfo(values);
                    }else{
//成功的上报
                    }
                }
                break;
            }
        }
    }
    /**
     * 对给第三方上报的连接做处理
     */
    private String refactoryThridURL(String url){

        if(url.indexOf("ns=__IP__") != -1){//如果不存在对应宏则在连接上加上该参数,存在则进行替换
            url.replace("__IP__", MD5Util.GetMD5Code(pService.getLocalIpAddress()));
        }else{
            url = url+"&ns="+MD5Util.GetMD5Code(pService.getLocalIpAddress());
        }
        //优先来匹配sn，如果没有sn则使用mac地址
        if(!TextUtils.isEmpty(AdBootDataUtil.getSN())) {
            if (url.indexOf("m6a=__MAC__") != -1) {
                url.replace("__MAC__", MD5Util.GetMD5Code(AdBootDataUtil.getSN()));
            } else if (url.indexOf("m6=__MAC1__") != -1) {
                url.replace("__MAC1__", MD5Util.GetMD5Code(AdBootDataUtil.getSN()));
            } else if (url.indexOf("m6a=") == -1) {
                url = url + "&m6a=" + MD5Util.GetMD5Code(AdBootDataUtil.getSN());
            }
        }else{//没有sn则使用加密的mac地址
            if (url.indexOf("m6a=__MAC__") != -1) {
                url.replace("__MAC__", MD5Util.GetMD5Code(pService.getMacAddress(mContext)));
            } else if (url.indexOf("m6=__MAC1__") != -1) {
                url.replace("__MAC1__", MD5Util.GetMD5Code(pService.getMacAddress(mContext)));
            } else if (url.indexOf("m6a=") == -1) {
                url = url + "&m6a=" + MD5Util.GetMD5Code(pService.getMacAddress(mContext));
            }
            //修改上报的mac地址和sn的地点
        }
        if(url.indexOf("m1a=__ANDROIDID__") != -1){
            url.replace("__ANDROIDID__",MD5Util.GetMD5Code(pService.getDeviceId(mContext)));
        }else if(url.indexOf("m1=__ANDROIDID1__") != -1){
            url.replace("__ANDROIDID1__",MD5Util.GetMD5Code(pService.getDeviceId(mContext)));
        }else if(url.indexOf("ma1=") == -1){
            url = url+"&ma1="+MD5Util.GetMD5Code(pService.getDeviceId(mContext));
        }
        return url;
    }

    private String refactoryURL(String url){
        if(url.indexOf("i=") != -1){
            if(url.charAt(url.indexOf("i=")+2) == '&' || url.endsWith("i=")){
                url = url.replace("i=","i="+MD5Util.GetMD5Code(pService.getMacAddress(mContext)));
            }
        }else{
            url = url+"&i="+MD5Util.GetMD5Code(pService.getMacAddress(mContext));
        }
        if(url.indexOf("sn=") == -1){
            url = url+"&sn="+MD5Util.GetMD5Code(AdBootDataUtil.getSN());
        }
        return url;
    }

}
