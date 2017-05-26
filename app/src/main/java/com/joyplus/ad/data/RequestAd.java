package com.joyplus.ad.data;

import com.joyplus.ad.AdConfig;
import com.joyplus.ad.FASTTEST;
import com.joyplus.ad.HttpManager;
import com.joyplus.ad.PhoneManager;
import com.joyplus.ad.config.Log;

import org.apache.http.client.ClientProtocolException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public abstract class RequestAd<T> {

    protected InputStream is = null;
    protected String mFileName = AdConfig.GetCompany();//default name
    private boolean Debug = true;

    public T sendRequest(AdRequest request)
            throws RequestException {
        if (is == null) {
            String urls = request.toString();
 //           urls = "http://advapikj.ijoyplus.com/advapi/v1/mdrequest?rt=android_app&v=4.4.2&i=71f803f156033fd7748999a0611d8f87&u=Mozilla%2F5.0%20(Linux%3B%20Android%204.4.2%3B%20Konka%20Android%20TV%202992%20Build%2FKOT49H)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F30.0.0.0%20Safari%2F537.36&u2=Mozilla%2F5.0%20(Linux%3B%20U%3B%20Android%204.4.2%3B%20zh-cn%3B%20Konka%20Android%20TV%202992%20Build%2FKOT49H)%20AppleWebKit%2F533.1%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Mobile%20Safari%2F533.1&s=5422c8d7afaab8aaf8d0a0b7f757b64d&o=&o2=63819b4dca9b4130&t=1484706793229&connection_type=UNKNOW&listads=&sdkversion=3.0&sdk=open&ds=KONKA_6a828&sn=KONKA0000000798T1A03&dt=1&up=0&lp=0&dm=6a828&b=KONKA&ot=0&screen=002&mt=2&os=&osv=&dss=0&dsr=&ri=&u_wv=Mozilla%2F5.0%20(Linux%3B%20Android%204.4.2%3B%20Konka%20Android%20TV%202992%20Build%2FKOT49H)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F30.0.0.0%20Safari%2F537.36";
//			String device_name = "V8";
//			try {
//				device_name = URLEncoder.encode(PhoneManager.getInstance().GetDeviceName(), "utf-8");
//			} catch (UnsupportedEncodingException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			url = url+"&ds="+device_name;
            //urls = "http://106.75.53.57/advapi/v1/mdrequest?rt=android_app&v=4.4.2&i=095e033d35675bac07db019846fcd8e49&u=Mozilla%2F5.0%20(Linux%3B%20Android%204.4.2%3B%20Konka%20Android%20TV%202992%20Build%2FKOT49H)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F30.0.0.0%20Safari%2F537.36&u2=Mozilla%2F5.0%20(Linux%3B%20U%3B%20Android%204.4.2%3B%20zh-cn%3B%20Konka%20Android%20TV%202992%20Build%2FKOT49H)%20AppleWebKit%2F533.1%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Mobile%20Safari%2F533.1&s=9ab2f5d63e14214e37937a5deecdf631&o=&o2=cb445f8d3b0b4b86&t=1356998553868&connection_type=UNKNOW&listads=&sdkversion=3.0&sdk=open&ds=KONKA_realtek2969d&sn=KONKA0000000074T1A03&dt=1&up=0&lp=0&dm=realtek2969d&b=KONKA&ot=0&screen=002&mt=2&os=&osv=&dss=0&dsr=&ri=&u_wv=Mozilla%2F5.0%20(Linux%3B%20Android%204.4.2%3B%20Konka%20Android%20TV%202992%20Build%2FKOT49H)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F30.0.0.0%20Safari%2F537.36";
//            urls = "http://106.75.59.170/advapi/v1/mdrequest?rt=android_app&v=4.4.2&i=934e69e221e1df668964da983ff434f5&u=Mozilla%2F5.0%20(Linux%3B%20Android%204.4.2%3B%20konka%20KKHi3751AV500%20Build%2FKOT49H)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F30.0.0.0%20Safari%2F537.36&u2=Mozilla%2F5.0%20(Linux%3B%20U%3B%20Android%204.4.2%3B%20zh-cn%3B%20konka%20KKHi3751AV500%20Build%2FKOT49H)%20AppleWebKit%2F533.1%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Mobile%20Safari%2F533.1&s=03a6e602b1116f1c8f871fafa43314be&o=1152&o2=000000008fff8967&t=1479366588142&connection_type=WIFI&listads=&sdkversion=3.0&sdk=open&ds=K2_ds1&sn=&dt=1&up=0&lp=0&dm=rtd2995d&b=Joyplus&ot=0&screen=002&mt=2&os=&osv=&dss=0&dsr=&u_wv=Mozilla%2F5.0%20(Linux%3B%20Android%204.4.2%3B%20konka%20KKHi3751AV500%20Build%2FKOT49H)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Version%2F4.0%20Chrome%2F30.0.0.0%20Safari%2F537.36";
            if (Debug) Log.d("RequestAd url=" + urls);
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
