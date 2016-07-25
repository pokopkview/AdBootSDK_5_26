package com.joyplus.ad.report;

import android.text.TextUtils;

import com.joyplus.ad.PublisherId;
import com.joyplus.ad.data.IMPRESSIONURL;

public class Report extends com.joyplus.ad.mode.ReportMode {

    private PublisherId mId;
    private IMPRESSIONURL mURL;

    public boolean CanReport() {
        if (!isAviable()) return false;
        return (des() > 0);
    }

    public boolean Check() {
        if (!isAviable()) return false;
        if (mId == null || !mId.CheckId()) return false;
        return true;
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
}
