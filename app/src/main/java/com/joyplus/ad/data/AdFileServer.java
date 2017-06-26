package com.joyplus.ad.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.joyplus.ad.AdConfig;
import com.joyplus.ad.AdSDKFeature;
import com.joyplus.ad.PublisherId;
import com.joyplus.ad.config.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

/*Define by Jas@20131125
 * this use to implementation Interface for AdBootFileManager*/
public class AdFileServer {

    private Context mContext;
    private Object mObject = new Object();
    private boolean USEABLE = false;//for this file server useable
    private File BASEPATH;
    private long BASEPATHSIZE;

    public AdFileServer(Context context) {
        mContext = context;
        InitResource(context);
    }

    private void InitResource(Context context) {
        // TODO Auto-generated method stub
        if (AdSDKFeature.EXTERNAL_CONFIG) {
            if (!(AdConfig.GetBasePath() == null || "".equals(AdConfig.GetBasePath()))) {
                BASEPATH = new File(AdConfig.GetBasePath());
                Log.d("Jas", "BASEPATH++++++++++" + BASEPATH);
                //if(Mkdir(BASEPATH)){
                //文件不存在则创建文件
                if(!BASEPATH.exists()){
                    BASEPATH.mkdirs();
                }
                BASEPATHSIZE = getBasePathSize(BASEPATH);
                USEABLE = true;
                return;
                // }
            }
        }
        if (FileUtils.SDExist()) {
            BASEPATH = MkSDCarddir(context);
            if (BASEPATH == null && AdSDKFeature.USE_EXTERNAL_SDCARD) {
                BASEPATH = MKDatadir(context);
            }
        } else if (AdSDKFeature.USE_EXTERNAL_SDCARD) {
            BASEPATH = MKDatadir(context);
        }
        if (BASEPATH == null)
            USEABLE = false;
        else USEABLE = true;
        if (USEABLE) BASEPATH.mkdirs();

    }
    private long getBasePathSize(File file){
        long size = 0;
        File [] files = file.listFiles();
        for(int i =0;i<files.length;i++){
            if(files[i].isFile()){
                size+=files[i].length();
            }
        }
        return size;
    }
    public void SetBasePathSize(long size){
        BASEPATHSIZE-=size;
    }

    private File MkSDCarddir(Context context) {
        if (!FileUtils.SDExist()) return null;
        File sdkpath = new File(context.getExternalFilesDir(null), AdConfig.GetBasePathName() + File.separator);
        boolean result = Mkdir(sdkpath);
        if (result) {//we should del ad resource which save in data/data/XXX
            File data = new File(context.getFilesDir(), AdConfig.GetBasePathName());
            FileUtils.deleteFiles(data.getAbsolutePath());
            return sdkpath;
        }
        return null;
    }

    private File MKDatadir(Context context) {
        // File  datapath = new File(context.getFilesDir(),AdConfig.GetBasePathName()+File.separator);
        File datapath = context.getFilesDir();
        if (Mkdir(datapath)) return datapath;
        return null;
    }

    private boolean Mkdir(File file) {
        if (file == null) return false;
        if (!file.exists()) file.mkdirs();
        if (file.canRead() && file.canWrite())
            return true;
        return false;
    }

    /*Interface for FileManager*/
    public File GetBasePath() {
        if (USEABLE && !BASEPATH.exists()) {

        }
        return BASEPATH;
    }

    public long getBASEPATHSIZE(){
        return BASEPATHSIZE;
    }

    public boolean UseAble() {
        return USEABLE;
    }

    public boolean writeSerializableData(String filename, Object o, PublisherId id) {
        if (!USEABLE || id == null || !id.CheckId()) return false;
        //Log.d("Server writeSerializableData() name="+filename);
        synchronized (mObject) {
            try {
                File dir = new File(GetBasePath(), id.GetPublisherId());
                dir.mkdirs();
                File data = new File(dir, filename);
                FileOutputStream fop = new FileOutputStream(data);
                ObjectOutputStream oos = new ObjectOutputStream(fop);
                oos.writeObject(o);
                oos.close();
                return true;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return false;
        }
    }

    public Object readSerializableData(String filename, PublisherId id) {
        if (!USEABLE || id == null || !id.CheckId()) return null;
        //Log.d("Server readSerializableData() name="+filename);
        synchronized (mObject) {
            try {
                File data = new File(GetBasePath(), id.GetPublisherId() + File.separator + filename);
                if(data.exists()){
                    data.mkdirs();
                }
                FileInputStream fis = new FileInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object file = (Object) ois.readObject();
                ois.close();
                return file;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (StreamCorruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
    }

    public boolean isCacheAviable(int cahcesize) {
        long size = FileUtils.getSystemFreeSpace();
        return (size > (cahcesize * 1024 * 1024));
    }

    public File getFile(String hashcode) {
        if (!TextUtils.isEmpty(hashcode)) {
            return new File(GetBasePath(), hashcode);
        }
        return null;
    }

    //add for report
    private static final String ADKEY_PUBLISHERID_CONFIG_XML = "adboot_config_xml";

    public void ReSetNum(PublisherId id) {
        synchronized (mObject) {
            if (id == null || !id.CheckId()) return;
            SharedPreferences sp = mContext.getSharedPreferences(ADKEY_PUBLISHERID_CONFIG_XML, Context.MODE_PRIVATE);
            Editor editor = sp.edit();
            editor.putInt(id.GetPublisherId(), 0);//must be 1
            editor.commit();
        }
    }

    public void AddReportNum(PublisherId id) {
        synchronized (mObject) {
            if (id == null || !id.CheckId()) return;
            SharedPreferences sp = mContext.getSharedPreferences(ADKEY_PUBLISHERID_CONFIG_XML, Context.MODE_PRIVATE);
            int num = sp.getInt(id.GetPublisherId(), 0);//min is 0
            Editor editor = sp.edit();
            editor.putInt(id.GetPublisherId(), ++num);//make is 1 first
            editor.commit();
        }
    }

    public int GetNum(PublisherId id) {
        synchronized (mObject) {
            if (id == null || !id.CheckId()) return 0;
            SharedPreferences sp = mContext.getSharedPreferences(ADKEY_PUBLISHERID_CONFIG_XML, Context.MODE_PRIVATE);
            return sp.getInt(id.GetPublisherId(), 0);
        }
    }
}
