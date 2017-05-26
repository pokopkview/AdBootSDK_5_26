package com.joyplus.ad.application;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.joyplus.ad.AdBootDataUtil;
import com.joyplus.ad.AdBootDownloadManager;
import com.joyplus.ad.AdConfig;
import com.joyplus.ad.AdManager.AD;
import com.joyplus.ad.AdMode;
import com.joyplus.ad.PhoneManager;
import com.joyplus.ad.PublisherId;
import com.joyplus.ad.config.Log;
import com.joyplus.ad.data.ADBOOT;
import com.joyplus.ad.data.AdBootRequest;
import com.joyplus.ad.data.CODE;
import com.joyplus.ad.data.RequestException;
import com.joyplus.ad.data.TRACKINGURL;
import com.joyplus.ad.data.URLModel;
import com.joyplus.ad.db.AdBootDao;
import com.joyplus.ad.db.AdBootImpressionInfo;
import com.joyplus.ad.db.AdBootTempDao;
import com.joyplus.ad.db.AdBootThread;
import com.joyplus.ad.report.Report;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class AdBootManager extends AdMode {

    private Context mContext;
    private AdBoot mAdBoot;//
    private AdBootRequest mAdBootRequest;
    private Thread mAdBootRequestThread;
    private AdBootDownloadManager mDownloadManager;
    private final static int TIME = 10;

    private AdBootManager() {
        super(AD.ADBOOT);
    }

    //can't instance this by no param.
    public AdBootManager(Context context, AdBoot info) {
        super(AD.ADBOOT);
        if (info == null) throw new IllegalArgumentException("AdBoot cannot be null or empty");
        if (context == null) throw new IllegalArgumentException("Context cannot be null or empty");
        mContext = context;
        mAdBoot = new AdBoot(info.GetCUSTOMINFO(), info.GetAdBootInfo(), info.GetPublisherId());
        mPublisherId = new PublisherId(mAdBoot.GetPublisherId().GetPublisherId());
        if (mAdBoot.GetCUSTOMINFO() == null || mAdBoot.GetCUSTOMINFO().GetDEVICEMOVEMENT() == null || "".equals(mAdBoot.GetCUSTOMINFO().GetDEVICEMOVEMENT()))
            throw new IllegalArgumentException("dm cannot be null or empty");
        PhoneManager.getInstance().SetCUSTOMINFO(mAdBoot.GetCUSTOMINFO());//update custom info.
        mDownloadManager = new AdBootDownloadManager(mContext, this, mAdBoot);
    }


    @Override
    public void RequestAD() {
        // TODO Auto-generated method stub
        Log.d("AdBootManager RequestAD start ++++++++++++++++++++++++++" + (mAdBootRequestThread == null));
        if (mAdBootRequestThread == null) {
            mAdBootRequestThread = new Thread() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    super.run();
                    Request();// for request ad.
                    mAdBootRequestThread = null;
                }
            };
//            mAdBootRequestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
//                                                                 @Override
//                                                                 public void uncaughtException(Thread thread, Throwable ex) {
//                                                                     // TODO Auto-generated method stub
//                                                                     mAdBootRequest = null;
//                                                                     mAdBootRequestThread = null;
//                                                                     System.out.println(ex.getMessage()+"====test="+thread.getId());
//                                                                     //notifyNoAn();
//                                                                 }
//                                                             }
//            );
            mAdBootRequestThread.start();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void  Request() {
        Log.d("Request start ++++++++++++++++++++++++++");
        if (mAdBootRequest != null) return;
        if (!CheckRequestAble()) {//can't request AD now.so we can return.
            mAdBootRequest = null;
            return;
        }
        mAdBootRequest = new AdBootRequest(AdBootManager.this, mAdBoot);
        ADBOOT mADBOOT = null;
        int Count = TIME;

//        while ((Count--) > 0) {
//            try {
//                Thread.sleep(500);
//                mADBOOT = null;
//                mADBOOT = mAdBootRequest.sendRequest();
//                break;
//            } catch (Throwable e) {
//                // TODO Auto-generated catch block
//                mADBOOT = null;
//                e.printStackTrace();
//            }
//        }
        try{
            mADBOOT = null;
            mADBOOT = mAdBootRequest.sendRequest();
        } catch (RequestException e){
            System.out.println(e.getMessage());
            mADBOOT = null;
        }

        if (null != AdBootDataUtil.getHtml5Url()) {
            final String html5_url = AdBootDataUtil.getHtml5Url();
            Random random = new Random();
            int count = (random.nextInt(60)+1)*1000;
            System.out.println(count);
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    getHtmlwork(html5_url, new getModel() {
                        @Override
                        public void getURLModel(URLModel model) {
                            sendToJoy(model);//给自己服务器的上报
                            List<URLModel.UrlInfoEntity> list = model.getUrl_info();
                            for (int i = 0; i < list.size(); i++) {
                                Report report = new Report(mContext);
                                report.reportOTH(list.get(i).getUrl());
                            }
                        }
                    });
                }
            };
            Timer timer = new Timer();
            timer.schedule(task,count);
        }
        if (mDownloadManager != null && mADBOOT != null) {
            Log.d(AdBootDataUtil.getPushMode()+"__"+AdConfig.GetMaxSize());
            if(AdBootDataUtil.getPushMode().equals(AdConfig.PUSH_NOW)){
                //上报本次请求的数据



                Log.d("push_now");
                SetReportInfoNow(mADBOOT);
                new AdBootThread(mContext, ((mAdBoot != null && mAdBoot.GetCUSTOMINFO() != null) ? mAdBoot.GetCUSTOMINFO() : null)).StartReport();
                mDownloadManager.UpdateADBOOT(mADBOOT, mAdBootRequest.GetFileName(), mPublisherId);
            }else {
                SetReportInfo();
                AdBootTempDao.getInstance(mContext).Remove(mPublisherId.GetPublisherId());//remove it first.
                new AdBootThread(mContext, ((mAdBoot != null && mAdBoot.GetCUSTOMINFO() != null) ? mAdBoot.GetCUSTOMINFO() : null)).StartReport();
                mDownloadManager.UpdateADBOOT(mADBOOT, mAdBootRequest.GetFileName(), mPublisherId);
            }
        } else {
            notifyNoAn();
        }
        mAdBootRequest = null;
    }

    private void SetReportInfo(){
        AdBootTempDao tempDao = AdBootTempDao.getInstance(mContext);
        AdBootDao dao = AdBootDao.getInstance(mContext);
        List<AdBootImpressionInfo> infos = tempDao.GetAllTemp();
        if(dao.GetLast()!=null){
            return;
        }
        System.out.println("infos="+infos);
        if(null != infos){
            for (AdBootImpressionInfo info : infos) {
                dao.InsertOneInfo(info);
            }
        }
    }

    private void SetReportInfoNow(ADBOOT mADBOOTs){
        int count = 0;
        AdBootTempDao tempDao = AdBootTempDao.getInstance(mContext);
//        tempDao.delAll();
        List<AdBootImpressionInfo> infos = tempDao.GetAllTemp();
        if(infos!=null){
            for(AdBootImpressionInfo info : infos){
                System.out.println("info.mImpressionUrl="+info.mImpressionUrl+",mADBOOTs.video.impressionurl="+mADBOOTs.video.impressionurl);
                if(info.mImpressionUrl.equals(mADBOOTs.video.impressionurl.URL)){
                    System.out.println("info.Count+1");
                    count = info.Count+1;
                }else {
                    System.out.println("InsertOneInfo(info, 1)");
                    AdBootDao.getInstance(mContext).InsertOneInfo(info, 1);
                }
            }
        }
        AdBootImpressionInfo info = new AdBootImpressionInfo();
        info.publisher_id = mPublisherId.GetPublisherId();
        System.out.println("count="+count);
        info.Count= count;
        if(mAdBoot.GetAdBootInfo()!=null) {
            info.FirstSource = mAdBoot.GetAdBootInfo().GetFirstSource();
            info.SecondSource = mAdBoot.GetAdBootInfo().GetSecondSource();
            info.ThirdSource = mAdBoot.GetAdBootInfo().GetThirdSource();
        }
        if (mADBOOTs.video.impressionurl != null) {
            info.mImpressionUrl = mADBOOTs.video.impressionurl.URL;
        }
        if(CODE.AD_NO.equals(mADBOOTs.code)){
            return;
        }
        if(null != mADBOOTs.video.trackingurl){
            for (TRACKINGURL url : mADBOOTs.video.trackingurl) {
                if (TRACKINGURL.TYPE.MIAOZHEN == url.Type) {
                    info.miaozhen = url.URL;
                } else if (TRACKINGURL.TYPE.ADMASTER == url.Type) {
                    info.admaster = url.URL;
                } else if (TRACKINGURL.TYPE.IRESEARCH == url.Type) {
                    info.iresearch = url.URL;
                } else if (TRACKINGURL.TYPE.NIELSEN == url.Type) {
                    info.nielsen = url.URL;
                }
            }
        }
        AdBootDao.getInstance(mContext).InsertOneInfo(info,1);
        tempDao.delAll();
    }

    private void sendToJoy(final URLModel model) {
        for (int i = 0; i < model.getUrl_info().size(); i++) {
            getResponse(model.getUrl_info().get(i));
        }
    }

    private void getResponse(final URLModel.UrlInfoEntity url) {
        try {
            //System.out.println("toJoyPlus:"+url.getUrl() + "&url_id=" + url.getId());
            HttpURLConnection conn = (HttpURLConnection) (new URL(url.getUrl() + "&url_id=" + url.getId())).openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean CheckRequestAble() {
        if (AdConfig.GetREQUESTALWAYS()) return true;
        AdBootImpressionInfo info = AdBootTempDao.getInstance(mContext).GetOne(mPublisherId.GetPublisherId());
        if (info != null && info.Count < 1) {
            if (mAdBootListener != null) {//last AD no show.so we return finish this request.

                mAdBootListener.Finish();
            }
            return false;
        }
        return true;
    }

    public void getHtmlwork(String url, getModel models) {
        try {
            //System.out.println("geturl:" + url);
           // String strs = url.replace("http://42.62.50.187", "http://106.75.55.63");
            URL url1 = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream is = connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            System.out.println(buffer.toString());
            JSONArray jsonObject = new JSONObject(buffer.toString()).getJSONArray("url_info");
            URLModel model = new URLModel();
            List<URLModel.UrlInfoEntity> lists = new ArrayList<URLModel.UrlInfoEntity>();
            for (int j = 0; j < jsonObject.length(); j++) {
                JSONObject object = (JSONObject) jsonObject.get(j);
                String id = object.getString("id");
                String str = object.getString("url");
                URLModel.UrlInfoEntity entity = new URLModel.UrlInfoEntity();
                entity.setId(id);
                entity.setUrl(str);
                lists.add(entity);
            }
            model.setUrl_info(lists);
            models.getURLModel(model);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface getModel {
        void getURLModel(URLModel model);
    }

    ////////////////////////////////////////////////////////////////////////////
    public void notifyNoAD() {
        if (mAdBootListener != null) {
            mAdBootListener.NoAd();
        }
    }

    public void notifyNoAn() {
        if (mAdBootListener != null) {
            mAdBootListener.NoAn();
        }
    }

    public void notifyDownLoadProgress(String TargetFile, long complete, long Totle) {
        if (mAdBootListener != null) {
            mAdBootListener.DownLoadProgress(TargetFile, complete, Totle);
        }
    }

    public void notifyfinish() {
        if (mAdBootListener != null) {
            mAdBootListener.Finish();
        }
    }

    private AdBootListener mAdBootListener;

    public void SetAdBootListener(AdBootListener listener) {
        mAdBootListener = listener;
    }
}
