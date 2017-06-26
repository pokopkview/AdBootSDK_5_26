package com.joyplus.ad.report;

import java.util.List;

import com.joyplus.ad.AdSDKManager;
import com.joyplus.ad.AdSDKManagerException;
import com.joyplus.ad.HttpManager;
import com.joyplus.ad.config.Log;
import com.joyplus.ad.db.AdBootReprtDao;
import com.joyplus.ad.mode.ReportModeController;

import android.content.Context;

public class AdReportManager extends ReportModeController {

    private Context mContext;
    private static AdReportManager mInstance;

    public static AdReportManager getInstance() {
        return mInstance;
    }

    public static void Init(Context context) throws AdSDKManagerException {
        if (AdSDKManager.IsInited()) return;
        if (context == null)
            throw new AdSDKManagerException("AdReportManager context is null !!!!!");
        mInstance = new AdReportManager(context);
    }

    @Override
    public synchronized void tearDown() {
        // TODO Auto-generated method stub
        super.tearDown();
        mInstance = null;
    }

    private AdReportManager(Context context) {
        mContext = context;
    }

    //interface for application
    public void AddReport(Report url) {
        if (url == null) return;
        Log.d("AdReportManager AddReport");
        addReportUri(url);
    }

    public void AddReport(List<Report> url) {
        if (url == null) return;
        addReportUri(url);
    }

    @Override
    public int getReportControllerType() {
        // TODO Auto-generated method stub
        return Report.TYPE_IMPTRACKINT;
    }

    @Override
    public boolean handlerReport(com.joyplus.ad.mode.ReportMode report) {
        // TODO Auto-generated method stub
        if (report instanceof Report) {
            ReportService((Report) report);
        }
        return false;
    }

    private void ReportService(Report report) {

        while ((report != null) && report.CanReport()) {
            try {
                String url = report.GetIMPRESSIONURL().URL;
                Log.d("ReportService-->" + url);
                boolean isSuccess = HttpManager.ReportServiceOneTime(url);
                if(!isSuccess){
                    Log.d("push_fail");
                    //放入新的数据库，来对失败的链接进行管理并完成上报
                    AdBootReprtDao.getInstance(mContext).InsertOneInfo(report.GetmInfo(),0);
                }
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                Log.d("ReportService fail-->" + e.toString());
                e.printStackTrace();
            }
        }
    }
}
