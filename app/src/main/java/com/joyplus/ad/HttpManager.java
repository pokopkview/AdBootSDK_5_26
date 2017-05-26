package com.joyplus.ad;

import com.joyplus.ad.config.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

public class HttpManager {

    public final static long VIDEO_LOAD_TIMEOUT = 1200000;
    public static final int CONNECTION_TIMEOUT = 10000; // = 10 sec
    public static final int SOCKET_TIMEOUT = 10000; // = 10 sec

    public static final String ENCODING = "UTF-8";
    public static final String RESPONSE_ENCODING = "ISO-8859-1";

    public static HttpResponse getHttpResponse(String uri) throws Throwable {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setSoTimeout(client.getParams(),
                HttpManager.SOCKET_TIMEOUT);
        HttpConnectionParams.setConnectionTimeout(client.getParams(),
                HttpManager.CONNECTION_TIMEOUT);
        HttpGet get = new HttpGet(uri);//uri maybe null
        return client.execute(get);
    }

    public static boolean ReportServiceOneTime(String url) {
        try {
            getHttpResponse(url);
            return true;
        } catch (Throwable e) {
            Log.d("ReportServiceOneTime fail -->" + e.toString());
            return false;
        }
    }
}
