package com.joyplus.ad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.joyplus.ad.application.AdBoot;
import com.joyplus.ad.application.AdBootInfo;
import com.joyplus.ad.application.AdBootListener;
import com.joyplus.ad.application.AdBootManager;
import com.joyplus.ad.application.AdCustomManagerCompat;
import com.joyplus.ad.application.COLLECTINFO;
import com.joyplus.ad.application.CUSTOMINFO;
import com.joyplus.ad.data.PhoneService;
import com.joyplus.ad.db.AdBootDoCacheRepot;
import com.joyplus.ad.db.AdBootReportInfo;
import com.joyplus.ad.db.AdBootReprtDao;
import com.joyplus.adbootsdk.R;

import java.io.File;
import java.util.List;
import java.util.jar.Manifest;

import cn.com.mma.mobile.tracking.api.Countly;

public class MainActivity extends Activity implements AdBootListener, OnClickListener {
	public static CUSTOMINFO mCUSTOMINFO;
	public static AdBootInfo mAdBootInfo;
	public static PublisherId mPublisherId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startservice();

        try {
			AdSDKManager.Init(this, AdSDKManager.CUSTOMTYPE.JOYPLUS);
		} catch (AdSDKManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        findViewById(R.id.setting).setOnClickListener(this);
        findViewById(R.id.request).setOnClickListener(this);
		findViewById(R.id.bt_test).setOnClickListener(this);
        initLocation();
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	initLocation();
    	findViewById(R.id.request).setClickable(mCUSTOMINFO != null && mPublisherId != null);
    }
    private void initLocation(){
    	TextView zipTe = ((TextView)findViewById(R.id.zip));
    	TextView videoTe = ((TextView)findViewById(R.id.video));
    	if(mAdBootInfo != null){
    		File zip = new File(mAdBootInfo.GetThirdSource());
    		if(zip.exists()){
    			zipTe.setText(zip.length() + " - "+ zip.toString());
    		}else{
    			zipTe.setText("");
    		}
    		File video = new File(mAdBootInfo.GetSecondSource());
    		if(video.exists()){
    			videoTe.setText(video.length() + " - "+ video.toString());
    		}else{
    			videoTe.setText("");
    		}
    	}else{
    		zipTe.setText("");
    		videoTe.setText("");
    	}
    }
    public void onButton(View v){
    	if(start){
    		start = false;
    	}else{
    		startAD(); 
    		start = true;
    	}
    }
    private boolean start = false;
    private void startAD(){
	    if(mAdBootInfo == null){
	    	mAdBootInfo = new AdBootInfo();
	    	File file = new File("./mnt/sdcard/AD/");file.mkdirs();
	    	mAdBootInfo.SetSecondSource("./mnt/sdcard/AD/animation.zip");
	    	mAdBootInfo.SetThirdSource("./mnt/sdcard/AD/ad.mp4");
	    }
	      
	    //now we can start ad
	    AdBoot adBoot = new AdBoot(mCUSTOMINFO, mAdBootInfo, mPublisherId);
	    AdBootManager adBootManager = new AdBootManager(this, adBoot);
	    adBootManager.SetAdBootListener(this);
	    mCUSTOMINFO.SetSN("Joyplus");
	    adBootManager.RequestAD();
	    addText("开始请求广告");
	    //startReportCollectInfo(mCUSTOMINFO);
    }

    private void addText(final String text){
    	MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				TextView mtextView = (TextView) findViewById(R.id.msg);
		    	mtextView.setText(mtextView.getText()+("\n" + text));
			}
		});
    	
    }
    private void clearText(){
    	MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				TextView mtextView = (TextView) findViewById(R.id.msg);
		    	mtextView.setText("");
			}
		});
    }
    
	@Override
	public void DownLoadProgress(String arg0, long arg1, long arg2) {
		// TODO Auto-generated method stub
		addText("下载： " + arg0 +" : " + arg1 +"-"+arg2 + " - "+ (float)(arg1*1.0/arg2));
	}


	@Override
	public void Finish() {
		// TODO Auto-generated method stub
		AdSDKManager.tearDown();
		addText("完成");
		//initLocation();
	}


	@Override
	public void NoAd() {
		// TODO Auto-generated method stub
		AdSDKManager.tearDown();
		addText("无广告返回");
		//initLocation();
	}

	@Override
	public void NoAn() {
		// TODO Auto-generated method stub
		AdSDKManager.tearDown();
		addText("服务器无响应");
		//initLocation();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.setting:{
			//Countly.sharedInstance().init(MainActivity.this,"http://42.62.105.254/advapi/v1/mdrequest");
			//Countly.sharedInstance().onExpose("http://g.dtv.cn.miaozhen.com/x/k=4001904&p=2hfJ1&ns=__IP__&m1=__ANDROIDID1__&m1a=__ANDROIDID__&m2=__IMEI__&m4=__AAID__&m6=__MAC1__&m6a=__MAC__&rt=2&nd=__DRA__&nt=__TIME__&o=");
			startActivity(new Intent(MainActivity.this, SettingActivity.class));
		}break;
		case R.id.request:{
			clearText();
			startAD();
		}break;
			case  R.id.bt_test:{
//				AdSDKManager.tearDown();
				AdBootDoCacheRepot.getInstance(MainActivity.this).setWork();
			}break;
		}
	}
    
	private void startReportCollectInfo(CUSTOMINFO info){
		Log.d("AdBootSDK", "startReportCollectInfo+++++");
		//AdCustomManagerCompat.addPowerReport(poweron, poweroff, delay)
//		try {
//			AdCustomManagerCompat.addPowerReport(info,10,45,35);
//		} catch (AdSDKManagerException e) {
//			// TODO Auto-generated catch block
//			Log.d("AdBootSDK", "addPowerReport fail-->"+e.toString());
//			e.printStackTrace();
//		}
		//report inputtype is TV  channel
//		try {
//			//AdCustomManagerCompat.addInputReport(info, COLLECTINFO.INPUTTYPE.TV, 12);
//			AdCustomManagerCompat.addInputReport(info, COLLECTINFO.INPUTTYPE.HDMI, 19);
//		} catch (AdSDKManagerException e) {
//			// TODO Auto-generated catch block
//			Log.d("AdBootSDK", "addInputReport fail-->"+e.toString());
//			e.printStackTrace();
//		}
	}
  public void startservice(){
	  Intent mIntent = new Intent(MainActivity.this, AdBootServer.class);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
				| Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		MainActivity.this.startService(mIntent);
	}
}
