package com.joyplus.ad.mode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class ReportModeResource {
    private List<ReportMode> mReportUri = new ArrayList<ReportMode>();
    private boolean isUseable = true;
    private final static int MAXSIZE = 100;
    //////////////////////////////////////////////////////////////
    private Object mWaitObject = new Object();

    private boolean waitReport() {
        synchronized (mWaitObject) {
            try {
                mWaitObject.wait();
            } catch (Throwable e) {
                return false;
            }
            return true;
        }
    }

    public void notifyReport() {
        synchronized (mWaitObject) {
            try {
                mWaitObject.notify();
            } catch (Throwable e) {
            }
        }
    }

    ////////////////////////////////////////////////////////////////
    public boolean addReportUri(ReportMode reportMode) {
        System.out.println("prepDownload8");
        if (addReportMode(reportMode)) {
            notifyReport();
            return true;
        }
        return false;
    }

    public boolean addReportUri(Collection<? extends ReportMode> Reports) {
        if (Reports != null && Reports.size() > 0) {
            for (ReportMode report : Reports) {
                addReportMode(report);
            }
            notifyReport();
            return true;
        }
        return false;
    }

    private boolean addReportMode(ReportMode reportMode) {
        if (reportMode != null) {
            if (reportMode.isAviable() && !reportMode.isReported() && isUseable
                    && mReportUri.size() < MAXSIZE) {
                //synchronized (mReportUri) {
                mReportUri.add(reportMode);
                //}
                return true;
            }
        }
        return false;
    }

    public synchronized ReportMode getReortUri() {
        ReportMode report = null;
        while (isUseable && (report = getOneReportUri()) == null) {
            waitReport();
        }
        return report;
    }

    private ReportMode getOneReportUri() {
        if (!isUseable) return null;
        //synchronized (mReportUri) {
        for (Iterator<ReportMode> iterator = mReportUri.iterator(); iterator.hasNext(); ) {
            ReportMode report = iterator.next();
            if (report.isReported()) {
                iterator.remove();
                continue;
            }
            return report;
        }
        //}
        return null;
    }

    public synchronized void tearDown() {
        isUseable = false;
        notifyReport();
    }
}
