package com.joyplus.ad.config;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*Define by Jas@20131125
 * use to config adboot by custom*/
public class AdBootExternalConfig {
    private Context mContext;
    private boolean LoadOK = true;
    private Properties props;
    private final static String ConfigFile = "/com/joyplus/ad/config/adbootconfig.properties";
    /*key for ConfigFile*/
    private final static String AdBootDebugEnable = "AdBootDebugEnable";
    private final static String BaseURL = "BaseURL";
    private final static String mAdBootBasePath = "AdBootBasePath";
    private final static String MAXSIZE = "MAXSIZE";
    private final static String CACHESIZE = "CACHESIZE";
    private final static String COPYALWAYS = "COPYALWAYS";
    private final static String REQUESTALWAYS = "REQUESTALWAYS";

    private static AdBootAssertExternalConfig mAdBootAssertExternalConfig;
    private static AdBootExternalConfig mAdBootExternalConfig;

    public static AdBootExternalConfig getInstance(Context context) {
        if (mAdBootExternalConfig == null) {
            mAdBootExternalConfig = new AdBootExternalConfig(context);
        }
        return mAdBootExternalConfig;
    }

    private AdBootExternalConfig(Context context) {
        mContext = context;
        Load();
    }

    public boolean GetDebugEnable(boolean defineValue) {
        if (LoadOK) {
            return GetBooleanConfig(AdBootDebugEnable, defineValue);
        } else if (mAdBootAssertExternalConfig != null && mAdBootAssertExternalConfig.getUseable()) {
            return mAdBootAssertExternalConfig.getAdBootDebugEnable(defineValue);
        }
        return defineValue;
    }

    public String GetBasePath(String defineValue) {
        if (LoadOK) {
            return GetStringConfig(mAdBootBasePath, defineValue);
        } else if (mAdBootAssertExternalConfig != null && mAdBootAssertExternalConfig.getUseable()) {
            return mAdBootAssertExternalConfig.getAdBootBasePath(defineValue);
        }
        return defineValue;
    }

    public String GetBaseURL(String defineValue) {
        if (LoadOK) {
            return GetStringConfig(BaseURL, defineValue);
        } else if (mAdBootAssertExternalConfig != null && mAdBootAssertExternalConfig.getUseable()) {
            return mAdBootAssertExternalConfig.getBaseURL(defineValue);
        }
        return defineValue;
    }

    public String GetBaseConfigURL(String defineValue) {
        if (LoadOK) {
            return GetStringConfig("ConfigURL", defineValue);
        }
        return defineValue;
    }

    public boolean GetCOPYALWAYS(boolean defineValue) {
        if (LoadOK) {
            return GetBooleanConfig(COPYALWAYS, defineValue);
        } else if (mAdBootAssertExternalConfig != null && mAdBootAssertExternalConfig.getUseable()) {
            return mAdBootAssertExternalConfig.getCOPYALWAYS(defineValue);
        }
        return defineValue;
    }

    public int GetMAXSIZE(int defineValue) {
        if (LoadOK) {
            return GetIntConfig(MAXSIZE, defineValue);
        } else if (mAdBootAssertExternalConfig != null && mAdBootAssertExternalConfig.getUseable()) {
            return mAdBootAssertExternalConfig.getMAXSIZE(defineValue);
        }
        return defineValue;
    }

    public int GetCACHESIZE(int defineValue) {
        if (LoadOK) {
            return GetIntConfig(CACHESIZE, defineValue);
        } else if (mAdBootAssertExternalConfig != null && mAdBootAssertExternalConfig.getUseable()) {
            return mAdBootAssertExternalConfig.getCACHESIZE(defineValue);
        }
        return defineValue;
    }

    public boolean GetREQUESTALWAYS(boolean defineValue) {
        if (LoadOK) {
            return GetBooleanConfig(REQUESTALWAYS, defineValue);
        } else if (mAdBootAssertExternalConfig != null && mAdBootAssertExternalConfig.getUseable()) {
            return mAdBootAssertExternalConfig.getREQUESTALWAYS(defineValue);
        }
        return defineValue;
    }

    private void Load() {
        try {
            InputStream is = this.getClass().getResourceAsStream(ConfigFile);
            props = new Properties();
            props.load(is);
            LoadOK = true;
            android.util.Log.i("AdBootSDK", "External Config Load success !!!!");
            return;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            android.util.Log.i("AdBootSDK", "External Config IOException !!!!");
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            android.util.Log.i("AdBootSDK", "External Config no find !!!!");
        }
        mAdBootAssertExternalConfig = AdBootAssertExternalConfig.getInstance(mContext);//now load assert XML.
    }

    private int GetIntConfig(String key, int defineValue) {
        if (!LoadOK) return defineValue;
        try {
            int value = Integer.parseInt(GetStringConfig(key, Integer.toString(defineValue)));
            return value;
        } catch (NumberFormatException e) {
        }
        return defineValue;
    }

    private boolean GetBooleanConfig(String key, boolean defineValue) {
        if (!LoadOK) return defineValue;
        String value = GetStringConfig(key, Boolean.toString(defineValue));
        if (value == null) return defineValue;
        return Boolean.parseBoolean(value);
    }

    private String GetStringConfig(String key, String defineValue) {
        if (!LoadOK) return defineValue;
        String value = props.getProperty(key);
        if (value == null) return defineValue;
        return value;
    }

    public void setMaxSizeConfig(int value) {
        FileInputStream fin = null;
        FileOutputStream fos = null;
        try {
            File  files = new File("/com/joyplus/config/adbootconfig.properties");
            fin = new FileInputStream(files);
            fos = new FileOutputStream(files);
            Properties mProps = new Properties();
            mProps.load(fin);
            fin.close();
            mProps.setProperty(MAXSIZE,String.valueOf(value));
            mProps.store(fos,"change maxsize");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fin.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
