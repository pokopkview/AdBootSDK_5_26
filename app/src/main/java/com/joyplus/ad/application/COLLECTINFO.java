package com.joyplus.ad.application;

public class COLLECTINFO {

    public enum INPUTTYPE {
        TV(1),
        HDMI(2),
        HDMI1(3),
        HDMI2(4),
        AV(5),
        VGA(6),
        DVI(7),
        YPVPR(8);
        private int TYPE;

        INPUTTYPE(int type) {
            TYPE = type;
        }

        public int toInt() {
            return TYPE;
        }
    }
}
