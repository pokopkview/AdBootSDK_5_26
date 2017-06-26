package com.joyplus.ad.mode;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.joyplus.ad.config.Log;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class ReportModeController {
    private final static boolean DEBUG = true;

    public abstract int getReportControllerType();

    public abstract boolean handlerReport(ReportMode report);

    private AtomicBoolean tearDown = new AtomicBoolean();

    //	 private HandlerThread mHandlerThread;
    public ReportModeController() {
        this(null);
    }

    public ReportModeController(Context context) {
        tearDown.set(false);
//		 mHandlerThread = new HandlerThread("ReportController"
//				 	+ ReportMode.getReportTypeString(getReportControllerType()));
//		 mHandlerThread.start();
//		 mHandler = new MessageHandler(mHandlerThread.getLooper());
        Log.d("ReportModeController " + ReportMode.getReportTypeString(getReportControllerType()) + " start!!!!!");
    }

    ////////////////////////////////////////////////////
    public synchronized void tearDown() {
        Log.d("ReportModeController " + ReportMode.getReportTypeString(getReportControllerType()) + " tearDown");
        if (!tearDown.get()) {
            try {
                tearDown.set(true);
                tearDownController(mController);
                tearDownReportResource(mReportResource);
            } catch (Throwable e) {
                Log.e("ReportController tearDown fail -->" + e.toString());
            }
        }
    }

    public ReportModeResource getReportResource() {
        return mReportResource;
    }

    public boolean isTearDown() {
        return tearDown.get();
    }

    private ReportModeResource mReportResource = new ReportModeResource();

    private ReportController mController;

    ///////////////////////////////////////////////////////////////
    private void tearDownReportResource(ReportModeResource reportResource) {
        Log.d("ReportModeController " + ReportMode.getReportTypeString(getReportControllerType()) + " tearDownReportResource");
        if (reportResource != null) {
            try {
                reportResource.tearDown();
            } catch (Throwable e) {
            }
        }
        reportResource = null;
    }

    private void tearDownController(ReportController controller) {
        Log.d("ReportModeController " + ReportMode.getReportTypeString(getReportControllerType()) + " tearDownController");
        if (controller != null) {
            try {
                controller.changeState(safeThread.THREAD_STATE_STOP);
            } catch (Throwable e) {
            }
        }
        controller = null;
    }

    private synchronized void createController() {
        if (isTearDown()) return;
        Log.d("ReportModeController " + ReportMode.getReportTypeString(getReportControllerType()) + " createController");
        if (mReportResource != null) {//it must have resource.
            if (mController == null
                    || mController.isNeedReCreate()) {
                tearDownController(mController);
                mController = new ReportController(ReportModeController.this);
                mController.start();
            }
        }
    }

    /////////////////////////////////////////
    private final static int MSG_CREATE_CONTROLLER = 1;
    private MessageHandler mHandler = new MessageHandler();

    private class MessageHandler extends Handler {
        //		 public MessageHandler(Looper loop){
//			 super(loop);
//		 }
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CREATE_CONTROLLER: {
                    createController();
                }
                break;
            }
        }
    }


    /////////////////
    public boolean addReportUri(final ReportMode report) {
        if (isTearDown() || mReportResource == null) return false;
        checkController();
        return mReportResource.addReportUri(report);
    }

    public boolean addReportUri(Collection<? extends ReportMode> Reports) {
        if (isTearDown() || mReportResource == null) return false;
        checkController();
        return mReportResource.addReportUri(Reports);
    }

    //////////////////
    public void checkController() {
        if (mController == null
                || mController.isNeedReCreate()) {
            sendUnRepeatMessage(MSG_CREATE_CONTROLLER);
        }
    }

    public synchronized void sendUnRepeatMessage(int msg) {
        if (mHandler != null) {
            mHandler.removeMessages(msg);
            mHandler.sendEmptyMessage(msg);
        }
    }
}
