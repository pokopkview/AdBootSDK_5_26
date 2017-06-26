package com.joyplus.ad;

import android.content.Context;
import android.text.TextUtils;

import com.joyplus.ad.addownload.AdDownloadManager;
import com.joyplus.ad.addownload.Download;
import com.joyplus.ad.application.AdBoot;
import com.joyplus.ad.application.AdBootInfo;
import com.joyplus.ad.application.AdBootManager;
import com.joyplus.ad.config.Log;
import com.joyplus.ad.data.ADBOOT;
import com.joyplus.ad.data.CODE;
import com.joyplus.ad.data.FileUtils;
import com.joyplus.ad.data.TRACKINGURL;
import com.joyplus.ad.db.AdBootImpressionInfo;
import com.joyplus.ad.db.AdBootTempDao;
import com.joyplus.ad.mode.inf.iDownloaderListener;

import java.util.ArrayList;
import java.util.List;


public class AdBootDownloadManager implements iDownloaderListener {

    private AdBoot mAdBoot;
    private Context mContext;

    private AdBootInfo mAdBootInfo;     //file use want to save
    //private AdBootInfo mLocalAdBootInfo;//file save in local

    private ADBOOT mLastADBOOT;     //last adbootresponse
    private ADBOOT mCurrentADBOOT;  //current adbootresponse

    private PublisherId mPublisherId;
    private AdBootManager mAdBootManager;

    public AdBootDownloadManager(Context context, AdBootManager adbootmanager, AdBoot info) {
        if (context == null || adbootmanager == null)
            throw new IllegalArgumentException("AdBootDownloadManager only be instance by AdBootManager!!!");
        mContext = context;
        mAdBoot = info;
        mAdBootManager = adbootmanager;
        mAdBootInfo = mAdBoot.GetAdBootInfo();
        mPublisherId = new PublisherId(mAdBoot.GetPublisherId().GetPublisherId());
    }

    public void UpdateADBOOT(ADBOOT adboot, String name, PublisherId id) {
        Log.d("AdBootDownloadManager UpdateADBOOT(" + (adboot != null) + " ," + name + ")" + " " + (mAdBootInfo != null));
        if (TextUtils.isEmpty(name) || mAdBootInfo == null
                || (!(mAdBootInfo.CheckFirstImageUsable() || mAdBootInfo.CheckSecondImageUsable() || mAdBootInfo.CheckBootAnimationZipUsable()))) {
            mAdBootManager.notifyfinish();//nothing to do here.
            return;
        }
        mLastADBOOT = (ADBOOT) AdFileManager.getInstance().readSerializableData(name, id);
        //mLastADBOOT = null;
        mCurrentADBOOT = adboot;
        AdFileManager.getInstance().writeSerializableData(name, mCurrentADBOOT, id);
        //AdFileManager.getInstance().ReSetNum(id);
        if (mCurrentADBOOT == null) return;
        if (mCurrentADBOOT.code != null && CODE.AD_NO.equals(mCurrentADBOOT.code.VALUE)) {
            if (mAdBootInfo.CheckFirstImageUsable())
                FileUtils.deleteFile(mAdBootInfo.GetFirstSource());
            if (mAdBootInfo.CheckSecondImageUsable())
                FileUtils.deleteFile(mAdBootInfo.GetSecondSource());
            if (mAdBootInfo.CheckBootAnimationZipUsable())
                FileUtils.deleteFile(mAdBootInfo.GetThirdSource());
            mAdBootManager.notifyNoAD();//notify no AD.
            mAdBootManager.notifyfinish();//nothing to do here.
            return;
        }
        if (mCurrentADBOOT == null || mCurrentADBOOT.video == null) {
            mAdBootManager.notifyfinish();//nothing to do here.
            return;
        }
        if (mCurrentADBOOT != null) Log.d("mCurrentADBOOT=" + mCurrentADBOOT.toString());
        else Log.d("mCurrentADBOOT == null");
        if (mLastADBOOT != null) Log.d("mLastADBOOT=" + mLastADBOOT.toString());
        else Log.d("mLastADBOOT == null");
        //add by Jas for report
        if(!AdBootDataUtil.getPushMode().equals(AdConfig.PUSH_NOW)) {
            prepReport(id.GetPublisherId());
        }
        prepDownload();
    }

    private void prepReport(String publisherId) {
        if (mCurrentADBOOT != null && mCurrentADBOOT.video != null) {
            AdBootImpressionInfo Info = new AdBootImpressionInfo();
            Info.publisher_id = publisherId;
            if (mCurrentADBOOT.video.impressionurl != null) {
                Info.mImpressionUrl = mCurrentADBOOT.video.impressionurl.URL;
            }
            if (mAdBootInfo != null) {
                Info.FirstSource = mAdBootInfo.GetFirstSource();
                Info.SecondSource = mAdBootInfo.GetSecondSource();
                Info.ThirdSource = mAdBootInfo.GetThirdSource();
            }
            if (mCurrentADBOOT.video.trackingurl != null) {
                for (TRACKINGURL url : mCurrentADBOOT.video.trackingurl) {
                    if (TRACKINGURL.TYPE.MIAOZHEN == url.Type) {
                        Info.miaozhen = url.URL;
                    } else if (TRACKINGURL.TYPE.ADMASTER == url.Type) {
                        Info.admaster = url.URL;
                    } else if (TRACKINGURL.TYPE.IRESEARCH == url.Type) {
                        Info.iresearch = url.URL;
                    } else if (TRACKINGURL.TYPE.NIELSEN == url.Type) {
                        Info.nielsen = url.URL;
                    }
                }
            }
            Info.Count = 0;
            AdBootTempDao.getInstance(mContext).InsertOneInfo(Info);
        }
    }

    private void prepDownload() {
        prepDownloadFirst();
        prepDownloadSecond();
        prepDownloadThird();
        checkDownloadfinish();
    }

    private void prepDownloadFirst() {
        // TODO Auto-generated method stub
        if (mAdBootInfo == null) return;
        if (mAdBootInfo.CheckFirstImageUsable()) {
            if (!(mCurrentADBOOT == null || mCurrentADBOOT.video == null
                    || mCurrentADBOOT.video.creative == null)) {
                adddownload(mCurrentADBOOT.video.creative.URL,
                        mCurrentADBOOT.video.creative.Hash,
                        mAdBootInfo.GetFirstSource());
            }
        }
    }

    private void prepDownloadSecond() {
        // TODO Auto-generated method stub
        if (mAdBootInfo == null) return;
        if (mAdBootInfo.CheckSecondImageUsable()) {
            if (!(mCurrentADBOOT == null || mCurrentADBOOT.video == null
                    || mCurrentADBOOT.video.creative2 == null)) {
                adddownload(mCurrentADBOOT.video.creative2.URL,
                        mCurrentADBOOT.video.creative2.Hash,
                        mAdBootInfo.GetSecondSource());
            }
        }
    }

    private void prepDownloadThird() {
        // TODO Auto-generated method stub
        if (mAdBootInfo == null) return;
        if (mAdBootInfo.CheckBootAnimationZipUsable()) {
            if (!(mCurrentADBOOT == null || mCurrentADBOOT.video == null
                    || mCurrentADBOOT.video.creative3 == null)) {
                adddownload(mCurrentADBOOT.video.creative3.URL,
                        mCurrentADBOOT.video.creative3.Hash,
                        mAdBootInfo.GetThirdSource());
            }
        }
    }

    private void adddownload(String url, String hashCode, String TargetFile) {
        Download download = new Download();
        download.URL = url;
        download.filehashCode = hashCode;
        download.TargetFile = TargetFile;
        if (download.isAviable()) {
            try {
                mDownload.add(download);
                download.addDownloaderListener(this);
                AdDownloadManager.getInstance().AddDownload(download);
            }catch (Exception e){
               e.printStackTrace();
            }
        }
    }

////	private void CheckDownLoadFirst(){
////	if(mAdBootInfo == null)return;
////	if(mAdBootInfo.CheckFirstImageUsable() ){
////	 File first = new File(mLocalAdBootInfo.GetFirstSource());
////	 if(IsFirstSame() && first.exists()){//URL same and localfile is exists.
////	  if(IsFirstHashSame()){
////	   Log.d("Hash check success "+mAdBootInfo.GetFirstSource()+" then going to copy file to Target file !!!");
////	   File TargetFirst = new File(mAdBootInfo.GetFirstSource());
////	   if(!AdConfig.GetCOPYALWAYS()){
////		   long t = TargetFirst.length();
////		   if(t>0 && t== first.length()){
////			   FileUtils.Chmod(TargetFirst);
////			   return;
////		   }
////	   }
////	   if(FileUtils.copyFile(first, TargetFirst)){
////		   FileUtils.Chmod(TargetFirst);
////	   }
////	  }else{
////	   Log.d("Hash check fail  "+mAdBootInfo.GetFirstSource()+" then going to download file to Target file !!!");
////	   DownloadFirst();
////	  }
////	 }else if((mCurrentADBOOT.video.creative != null)
////		  && (URLUtil.isHttpsUrl(mCurrentADBOOT.video.creative.URL)||URLUtil.isHttpUrl(mCurrentADBOOT.video.creative.URL))){
////	  if(first.exists())first.delete();//remove it first
////	  DownloadFirst();
////	 }else{//now server not get
////	  if(first.exists())first.delete();//remove it first
////	  FileUtils.deleteFile(mAdBootInfo.GetFirstSource());
////	 }
////	}
////	
////	rivate void CheckDownLoadSecond(){
////       if(mAdBootInfo == null)return;
////	if(mAdBootInfo.CheckSecondImageUsable()){
////	 File second = new File(mLocalAdBootInfo.GetSecondSource());
////	 if(IsSecondSame() && second.exists()){
////	  if(IsSecondHashSame()){
////	   Log.d("Hash check success "+mAdBootInfo.GetSecondSource()+" then going to copy file to Target file !!!");
////	   File TagetSecond = new File(mAdBootInfo.GetSecondSource());
////	   if(!AdConfig.GetCOPYALWAYS()){
////		   long t = TagetSecond.length();
////		   if(t>0 && t== second.length()){
////			   FileUtils.Chmod(TagetSecond);
////			   return;
////		   }
////	   }
////	   if(FileUtils.copyFile(second, TagetSecond)){
////		   FileUtils.Chmod(TagetSecond);
////	   }
////	  }else{
////	   Log.d("Hash check fail  "+mAdBootInfo.GetSecondSource()+" then going to download file to Target file !!!");
////	   DownloadSecond();
////	  }
////	 }else if((mCurrentADBOOT.video.creative2 != null)
////		  && (URLUtil.isHttpsUrl(mCurrentADBOOT.video.creative2.URL)||URLUtil.isHttpUrl(mCurrentADBOOT.video.creative2.URL))){
////	  if(second.exists())second.delete();//remove it first
////	  DownloadSecond();
////	 }else{//now server not get
////	  if(second.exists())second.delete();//remove it first
////	  FileUtils.deleteFile(mAdBootInfo.GetSecondSource());
////	 }
////	}
////	
////	rivate void CheckDownLoadZIP(){
////	if(mAdBootInfo == null)return;
////	if(mAdBootInfo.CheckBootAnimationZipUsable()){
////	 File zip = new File(mLocalAdBootInfo.GetThirdSource());
////	 if(IsBootAnimationSame() && zip.exists()){
////	  if(IsBootAnimationHashSame()){
////	   Log.d("Hash check success "+mAdBootInfo.GetThirdSource()+" then going to copy file to Target file !!!");
////	   File TargetThird = new File(mAdBootInfo.GetThirdSource());
////	   if(!AdConfig.GetCOPYALWAYS()){
////		   long t = TargetThird.length();
////		   if(t>0 && t== zip.length()){
////			   FileUtils.Chmod(TargetThird);
////			   return;
////		   }
////	   }
////	   if(FileUtils.copyFile(zip, TargetThird)){
////		   FileUtils.Chmod(TargetThird);
////	   }
////	  }else{
////	   Log.d("Hash check fail  "+mAdBootInfo.GetThirdSource()+" then going to download file to Target file !!!");
////	   DownloadBootAnimation();
////	  }
////	 }else if((mCurrentADBOOT.video.creative3 != null)
////		  && (URLUtil.isHttpsUrl(mCurrentADBOOT.video.creative3.URL)||URLUtil.isHttpUrl(mCurrentADBOOT.video.creative3.URL))){
////	  if(zip.exists())zip.delete();//remove it first
////	  DownloadBootAnimation();
////	 }else{//now server not get
////	  if(zip.exists())zip.delete();//remove it first
////	  FileUtils.deleteFile(mAdBootInfo.GetThirdSource());
////	 }
////	}
////	
////	rivate void InitResource() {
////	// TODO Auto-generated method stub
////	mLocalAdBootInfo = new AdBootInfo();
////	(new File(AdFileManager.getInstance().GetBasePath().toString()+File.separator+mPublisherId.GetPublisherId().toString()+File.separator)).mkdirs();
////	mLocalAdBootInfo.SetThirdSource(AdFileManager.getInstance().GetBasePath().toString()+File.separator+mPublisherId.GetPublisherId().toString()+File.separator+"AdBootManager_bootanimation");
////	mLocalAdBootInfo.SetFirstSource(AdFileManager.getInstance().GetBasePath().toString()+File.separator+mPublisherId.GetPublisherId().toString()+File.separator+"AdBootManager_first");
////	mLocalAdBootInfo.SetSecondSource(AdFileManager.getInstance().GetBasePath().toString()+File.separator+mPublisherId.GetPublisherId().toString()+File.separator+"AdBootManager_second");
////       //mDownload = new ArrayList<Download>();
////	
//	  //	  rivate boolean IsBootAnimationSame(){
//		 if(!(mLastADBOOT!=null && mCurrentADBOOT!=null))
//			 return false;
//		 if(!(mLastADBOOT.video!=null && mCurrentADBOOT.video!=null))
//			 return false;
//		 if(!(mLastADBOOT.video.creative3!=null && mCurrentADBOOT.video.creative3!=null))
//			 return false; 
//		 String mLastbootanimation = mLastADBOOT.video.creative3.URL;
//		 String mbootanimation = mCurrentADBOOT.video.creative3.URL;
//		 if(mLastbootanimation ==null || mbootanimation == null)return false;
//		 return TextUtils.equals(mbootanimation, mLastbootanimation);
//	  
//	  rivate boolean IsBootAnimationHashSame(){
//		 if(mCurrentADBOOT==null||mCurrentADBOOT.video==null||mCurrentADBOOT.video.creative3==null)return false;
//		 String filehash = AdHash.getFileHash(mLocalAdBootInfo.GetThirdSource());
//		 if(TextUtils.isEmpty(filehash.trim()))return false;//file no exsits
//		 return TextUtils.equals(filehash, mCurrentADBOOT.video.creative3.Hash);
//	  
//	  rivate void DownloadBootAnimation(){
//		 Download zipdownload   = new Download();
//		 zipdownload.URL        = mCurrentADBOOT.video.creative3.URL;
//		 zipdownload.LocalFile  = mLocalAdBootInfo.GetThirdSource();
//		 zipdownload.TargetFile = mAdBootInfo.GetThirdSource();
//		 //zipdownload.SetDownLoadListener(this);
//		 mDownload.add(zipdownload);
//		 //DownLoadManager.getInstance().AddDownload(zipdownload);
//	  
//	  rivate boolean IsFirstSame(){
//		 if(!(mLastADBOOT!=null && mCurrentADBOOT!=null))
//			 return false;
//		 if(!(mLastADBOOT.video!=null && mCurrentADBOOT.video!=null))
//			 return false;
//		 if(!(mLastADBOOT.video.creative!=null && mCurrentADBOOT.video.creative!=null))
//			 return false;
//		 String mLastbootanimation = mLastADBOOT.video.creative.URL;
//		 String mbootanimation = mCurrentADBOOT.video.creative.URL;
//		 if(mLastbootanimation ==null || mbootanimation == null)return false;
//		 return TextUtils.equals(mLastbootanimation, mbootanimation);
//	  
//	  rivate boolean IsFirstHashSame(){
//		 if(mCurrentADBOOT==null||mCurrentADBOOT.video==null||mCurrentADBOOT.video.creative==null)return false;
//		 String filehash = AdHash.getFileHash(mLocalAdBootInfo.GetFirstSource());
//		 if(TextUtils.isEmpty(filehash.trim()))return false;//file no exsits
//		 return TextUtils.equals(filehash, mCurrentADBOOT.video.creative.Hash);
//	  
//	  rivate void DownloadFirst(){
//		 Download firstdownload   = new Download();
//		 firstdownload.URL        = mCurrentADBOOT.video.creative.URL;
//		 firstdownload.LocalFile  = mLocalAdBootInfo.GetFirstSource();
//		 firstdownload.TargetFile = mAdBootInfo.GetFirstSource();
//		 //firstdownload.SetDownLoadListener(this);
//		 mDownload.add(firstdownload);
//		 //DownLoadManager.getInstance().AddDownload(firstdownload);
//	  
//	  rivate boolean IsSecondSame(){
//		 if(!(mLastADBOOT!=null && mCurrentADBOOT!=null))
//			 return false;
//		 if(!(mLastADBOOT.video!=null && mCurrentADBOOT.video!=null))
//			 return false;
//		 if(!(mLastADBOOT.video.creative2!=null && mCurrentADBOOT.video.creative2!=null))
//			 return false;
//		 String mLastbootanimation = mLastADBOOT.video.creative2.URL;
//		 String mbootanimation = mCurrentADBOOT.video.creative2.URL;
//		 if(mLastbootanimation ==null || mbootanimation == null)return false;
//		 return TextUtils.equals(mLastbootanimation, mbootanimation);
//	  
//	  rivate boolean IsSecondHashSame(){
//		 if(mCurrentADBOOT==null||mCurrentADBOOT.video==null||mCurrentADBOOT.video.creative2==null)return false;
//		 String filehash = AdHash.getFileHash(mAdBootInfo.GetSecondSource());
//		 if(TextUtils.isEmpty(filehash.trim()))return false;//file no exsits
//		 return TextUtils.equals(filehash, mCurrentADBOOT.video.creative2.Hash);
//	  
//	  rivate void DownloadSecond(){
//		 Download seconddownload   = new Download();
//	     seconddownload.URL        = mCurrentADBOOT.video.creative2.URL;
//	     seconddownload.LocalFile  = mLocalAdBootInfo.GetSecondSource();
//	     seconddownload.TargetFile = mAdBootInfo.GetSecondSource();
//	     //seconddownload.SetDownLoadListener(this);
//	     mDownload.add(seconddownload);
//		 //DownLoadManager.getInstance().AddDownload(seconddownload);



    //for Listener
    private List<Download> mDownload = new ArrayList<Download>();;

    private void checkDownloadfinish() {
        if (mDownload != null && mDownload.size() <= 0) {
            mAdBootManager.notifyfinish();
        }
    }

    @Override
    public void DownloaderStateChange(Download download, int state) {
        // TODO Auto-generated method stub

    }

    @Override
    public void DownloaderProgress(Download download, long Dwonloaded, long TotleSize) {
        // TODO Auto-generated method stub
        mAdBootManager.notifyDownLoadProgress(download.TargetFile, Dwonloaded, TotleSize);
    }

    @Override
    public void DownloaderFinish(Download download) {
        // TODO Auto-generated method stub
        mDownload.remove(download);
        checkDownloadfinish();
    }

}
