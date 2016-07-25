package com.joyplus.ad.mode;

import com.joyplus.ad.config.Log;
import com.joyplus.ad.mode.inf.iStateListener;

public class ReportController extends safeThread implements iStateListener {
    private final static boolean DEBUG = true;
    private ReportModeController mReportController;

    public ReportController(ReportModeController reportController) {
        super();
        addListener(this);
        mReportController = reportController;
    }

    @Override
    public boolean progress() {
        // TODO Auto-generated method stub
        if (DEBUG) Log.d("ReportController progress start");
        while (getsafeThreadState() == THREAD_STATE_RUNNING) {
            if (DEBUG) Log.d("ReportController progress isalive ");
            ReportMode report = getReport();
            if (report != null) {
                report.setReported(true);//make sure it can be remove
                handlerReport(report);
            }
        }
        return true;
    }

    private synchronized ReportMode getReport() {
        if (mReportController != null) {
            ReportModeResource reportResource = mReportController.getReportResource();
            if (reportResource != null) {
                return reportResource.getReortUri();
            }
        }
        return null;
    }

    private boolean handlerReport(ReportMode report) {
        if (report != null && mReportController != null) {
            mReportController.handlerReport(report);
        }
        return false;
    }

    @Override
    public boolean onStateChange(int laststate, int newstate) {
        // TODO Auto-generated method stub
        if (newstate == THREAD_STATE_STOP) {
            if (mReportController != null) {
                mReportController.checkController();
            }
        }
        return false;
    }

    @Override
    public boolean notifyStateChange(int laststate, int newstate) {
        // TODO Auto-generated method stub
        return false;//nothing to do here.
    }

    public boolean isNeedReCreate() {
        int state = getsafeThreadState();
        return !(state == THREAD_STATE_PENDING || state == THREAD_STATE_RUNNING);
    }
}
