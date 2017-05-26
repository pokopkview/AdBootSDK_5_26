package com.joyplus.ad;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Spinner;

import com.joyplus.ad.application.CUSTOMINFO;
import com.joyplus.ad.application.CUSTOMINFO.SCREEN;
import com.joyplus.ad.application.CUSTOMINFO.SOURCETYPE;
import com.joyplus.adbootsdk.R;

public class SettingActivity extends Activity implements OnClickListener{
	private Spinner  url;
	private Spinner  publicid;
	private Spinner  ds;
	private Spinner  dm;
	private Spinner  mac;
	private Spinner  screen;
	private Spinner  source;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		url = (Spinner) findViewById(R.id.url);
		publicid = (Spinner) findViewById(R.id.publicid);
		ds = (Spinner) findViewById(R.id.ds);
		dm = (Spinner) findViewById(R.id.dm);
		mac = (Spinner) findViewById(R.id.mac);
		screen = (Spinner) findViewById(R.id.screen);
		source = (Spinner) findViewById(R.id.source);
		findViewById(R.id.request).setOnClickListener(this);
		findViewById(R.id.setting).setOnClickListener(this);
		initresource();
	}
	
	private void initresource(){
		url.setSelection(getStringIndex(AdConfig.GetBaseURL(), R.array.url_value));
		publicid.setSelection(getStringIndex((MainActivity.mPublisherId == null ? "" : MainActivity.mPublisherId.GetPublisherId()), R.array.publicid_value));
		ds.setSelection(getStringIndex((MainActivity.mCUSTOMINFO == null ? "" : MainActivity.mCUSTOMINFO.GetDEVICEMOVEMENT()), R.array.ds_value));
		dm.setSelection(getStringIndex((MainActivity.mCUSTOMINFO == null ? "" : MainActivity.mCUSTOMINFO.GetDEVICEMUMBER()), R.array.dm_value));
		
		mac.setSelection(getStringIndex(MainActivity.mCUSTOMINFO == null ? "" : MainActivity.mCUSTOMINFO.GetMAC(), R.array.mac_value));
		screen.setSelection(getScreenIndex(getScreen()));
		source.setSelection(getSourceIndex(getSourceType()));
	}
    
    private String getScreen(){
    	if(MainActivity.mCUSTOMINFO != null
    			 && MainActivity.mCUSTOMINFO.GetSCREEN() != null){
    		return MainActivity.mCUSTOMINFO.GetSCREEN().toString();
    	}
    	return null;
    }
    private String getSourceType(){
    	if(MainActivity.mCUSTOMINFO != null
    			 && MainActivity.mCUSTOMINFO.GetSOURCETYPE() != null){
    		return MainActivity.mCUSTOMINFO.GetSOURCETYPE().toString();
    	}
    	return null; 
    }
	private void save(){
		AdConfig.setBaseURL(getValue(url.getSelectedItemPosition(), R.array.url_value));
		MainActivity.mPublisherId = new PublisherId(getValue(publicid.getSelectedItemPosition(), R.array.publicid_value));
		CUSTOMINFO custominfo = new CUSTOMINFO();
		custominfo.SetMAC(getValue(mac.getSelectedItemPosition(), R.array.mac_value));
		custominfo.SetDEVICEMOVEMENT(getValue(dm.getSelectedItemPosition(), R.array.dm_value));
		custominfo.SetDEVICEMUMBER(getValue(ds.getSelectedItemPosition(), R.array.ds_value));
		custominfo.SetSCREEN(getSCREEN(getValue(screen.getSelectedItemPosition(), R.array.screen_value)));
		custominfo.SetSOURCETYPE(getSOURCE(getValue(source.getSelectedItemPosition(), R.array.source_value)));
		MainActivity.mCUSTOMINFO = custominfo;
	}
	
	private int getScreenIndex(String screen){
		if(TextUtils.isEmpty(screen))return 0;
		String[] values = SettingActivity.this.getResources().getStringArray(R.array.screen_value);
		for(int index =0 ; index < values.length ; index++){
			if(TextUtils.equals(values[index], screen)){
				return index;
			}
		}
		return 0;
	}
	
	private int getSourceIndex(String screen){
		if(TextUtils.isEmpty(screen))return 0;
		String[] values = SettingActivity.this.getResources().getStringArray(R.array.source_value);
		for(int index =0 ; index < values.length ; index++){
			if(TextUtils.equals(values[index], screen)){
				return index;
			}
		}
		return 0;
	}
	private SOURCETYPE getSOURCE(String source){
		if(TextUtils.equals(SOURCETYPE.IMAGE.toString(), source)){
			return SOURCETYPE.IMAGE;
		} else if(TextUtils.equals(SOURCETYPE.MRAID.toString(), source)){
			return SOURCETYPE.MRAID;
		} else if(TextUtils.equals(SOURCETYPE.VIDEO.toString(), source)){
			return SOURCETYPE.VIDEO;
		} else if(TextUtils.equals(SOURCETYPE.ZIPANDVIDEO.toString(), source)){
			return SOURCETYPE.ZIPANDVIDEO;
		} else if(TextUtils.equals(SOURCETYPE.ZIP.toString(), source)){
			return SOURCETYPE.ZIP;
		}
		return null;
	}
	private SCREEN getSCREEN(String screen){
		if(TextUtils.equals(screen, SCREEN.S_4K.toString())){
			return SCREEN.S_4K;
		}else if(TextUtils.equals(screen, SCREEN.S_HD.toString())){
			return SCREEN.S_HD;
		}else if(TextUtils.equals(screen, SCREEN.S_SD.toString())){
			return SCREEN.S_SD;
		}
		return null;
	}
	private String getValue(int index, int id){
		String[] values = SettingActivity.this.getResources().getStringArray(id);
		if(index <0 || index > values.length){
			return values[0];
		}
		return values[index];
	}
	private int getStringIndex(String value, int id){
		String[] values = SettingActivity.this.getResources().getStringArray(id);
		for(int index =0; index <values.length ; index ++){
			if(TextUtils.equals(value, values[index])){
				return index;
			}
		}
		return 0;
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.request:{
				save();
				finish();
		}break;
		}
	}
}
