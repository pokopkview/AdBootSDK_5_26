package com.joyplus.ad.mode;

import java.util.concurrent.atomic.AtomicInteger;


public abstract class ReportMode {

    public final static int TYPE_UNKNOW = 0;
    public final static int TYPE_CLKTRACKING = 1;
    public final static int TYPE_IMPTRACKINT = 2;
    public final static int TYPE_MONITOR = 3;
    public final static int TYPE_DOWNLOAD = 4;
    public final static int TYPE_MAX = 5;

    public static String getReportTypeString(int type) {
        switch (type) {
            case TYPE_UNKNOW:
                return "TYPE_UNKNOW";
            case TYPE_CLKTRACKING:
                return "TYPE_CLKTRACKING";
            case TYPE_IMPTRACKINT:
                return "TYPE_IMPTRACKINT";
            case TYPE_MONITOR:
                return "TYPE_MONITOR";
            case TYPE_MAX:
                return "TYPE_MAX";
        }
        return "UNKNOW";
    }

    private boolean Reported = false;
    private int type = TYPE_UNKNOW;

    public ReportMode() {
    }

    public ReportMode(int type, String uri) {
        this.type = type;
        Reported = false;
    }

    public abstract boolean isAviable();

    public boolean isAviableType() {
        return TYPE_UNKNOW < type && type < TYPE_MAX;
    }

    public boolean isReported() {
        return Reported;
    }

    public boolean setReported(boolean re) {
        return (Reported = re);
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuffer sb = new StringBuffer();
        sb.append("Report{")
                .append("Reported=" + Reported)
                .append(", type=" + type)
                .append(", NUM=" + NUM.get())
                .append("}");
        return sb.toString();
    }

    private AtomicInteger NUM = new AtomicInteger(1);

    public void SetNUM(int num) {
        NUM.set(num);
    }

    public int GetNUM() {
        return NUM.get();
    }

    public int des() {
        return NUM.getAndDecrement();
    }
}
