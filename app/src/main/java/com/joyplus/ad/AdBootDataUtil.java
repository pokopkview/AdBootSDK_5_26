package com.joyplus.ad;

/**
 * Created by UPC on 2016/8/24.
 */
public class AdBootDataUtil {
    private static String SN = "";

    public static String pushmode = "pushnow";

    public static String getHtml5Url() {
        return HTML5_URL;
    }

    public static String getPushMode() {
        return pushmode;
    }

    public static void setPushMode(String pushMode) {
        AdBootDataUtil.pushmode = pushMode;
    }

    public static void setHtml5Url(String html5Url) {
        HTML5_URL = html5Url;
    }

    private static String HTML5_URL;

    public static String getSN() {
        return SN;
    }

    public static void setSN(String SN) {
        AdBootDataUtil.SN = SN;
    }
}
