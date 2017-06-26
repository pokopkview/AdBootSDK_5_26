package com.joyplus.ad.data;

import com.joyplus.ad.AdConfig;
import com.joyplus.ad.FASTTEST;
import com.joyplus.ad.HttpManager;
import com.joyplus.ad.PhoneManager;
import com.joyplus.ad.config.Log;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public abstract class RequestAd<T> {

    protected InputStream is = null;
    protected String mFileName = AdConfig.GetCompany();//default name

    public T sendRequest(AdRequest request)
            throws RequestException {
        if (is == null) {
            String urls = request.toString();
            Log.d("RequestAd url=" + urls);
            URL url = null;
            HttpURLConnection client = null;
            try {
                url = new URL(urls);
                client = (HttpURLConnection) url.openConnection();
                client.setConnectTimeout(HttpManager.CONNECTION_TIMEOUT);
                client.setRequestProperty("user-agent",PhoneManager.getInstance().GetUA1());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                FASTTEST.REQUEST++;//for test
                int responseCode = client.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    FASTTEST.REQUEST_SUCCESS++;//for test
                    InputStream is = client.getInputStream();
                    return parse(is);
                } else {
                    throw new RequestException("Server Error. Response code:"
                            + responseCode);
                }
            } catch (RequestException e) {
                throw e;
            } catch (ClientProtocolException e) {
                throw new RequestException("Error in HTTP request ClientProtocolException", e);
            } catch (IOException e) {
                throw new RequestException("Error in HTTP request IOException", e);
            } catch (Throwable t) {
                throw new RequestException("Error in HTTP request Throwable", t);
            }
        } else {
            return parseTestString();
        }
    }

    abstract T parseTestString() throws RequestException;

    abstract T parse(InputStream inputStream) throws RequestException;

    public String GetFileName() {
        return mFileName;
    }
}
