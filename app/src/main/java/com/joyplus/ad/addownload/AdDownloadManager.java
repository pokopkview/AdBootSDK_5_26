package com.joyplus.ad.addownload;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.joyplus.ad.AdConfig;
import com.joyplus.ad.AdFileManager;
import com.joyplus.ad.AdSDKManager;
import com.joyplus.ad.AdSDKManagerException;
import com.joyplus.ad.HttpManager;
import com.joyplus.ad.config.Log;
import com.joyplus.ad.data.AdHash;
import com.joyplus.ad.data.FileUtils;
import com.joyplus.ad.mode.ReportMode;
import com.joyplus.ad.mode.ReportModeController;
import com.joyplus.ad.report.Report;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdDownloadManager extends ReportModeController {
    private Context mContext;
    private static AdDownloadManager mInstance;

    public static AdDownloadManager getInstance() {
        return mInstance;
    }

    public static void Init(Context context) throws AdSDKManagerException {
        if (AdSDKManager.IsInited()) return;
        if (context == null)
            throw new AdSDKManagerException("AdReportManager context is null !!!!!");
        mInstance = new AdDownloadManager(context);
    }

    private AdDownloadManager() {

    }

    private AdDownloadManager(Context context) {
        mContext = context;
    }

    @Override
    public synchronized void tearDown() {
        // TODO Auto-generated method stub
        super.tearDown();
        mInstance = null;
    }

    //interface for application
    public boolean AddDownload(Download url) {
        System.out.println("prepDownload6");
        if (url == null) return false;
        return addReportUri(url);
    }

    public boolean AddDownload(List<Download> url) {
        if (url == null) return false;
        return addReportUri(url);
    }

    @Override
    public int getReportControllerType() {
        // TODO Auto-generated method stub
        return Report.TYPE_DOWNLOAD;
    }

    @Override
    public boolean handlerReport(ReportMode report) {
        // TODO Auto-generated method stub
        if (report instanceof Download) {
            dispatcherDoanload((Download) report);
        }
        return false;
    }

    private void dispatcherDoanload(Download download) {
        try {
            download.DownloaderStateChange(download, Download.STATE_DOWNLOAD);//notify state change first.
            if (download.isAviable()) {
                File filedir = AdFileManager.getInstance().GetBasePath();
                File adcache = new File(filedir, download.filehashCode);//local cache file
                if (!(adcache.exists() && //first we check local ad file
                        TextUtils.equals(download.filehashCode, AdHash.getFileHash(adcache)))) {
                    //we download resource first
                    File downloaded = downloadFile(download, adcache);
                    if (downloaded != null && downloaded.exists()) {//here rename to load
                        downloaded.renameTo(new File(filedir, AdHash.getFileHash(downloaded)));
                        download.DownloaderStateChange(download, Download.STATE_SUCCESS);
                    } else {
                        download.DownloaderStateChange(download, Download.STATE_FAIL);
                    }
                }
                /////////////////////////
                adcache = new File(filedir, download.filehashCode);//local cache file
                if ((adcache.exists() && //second we check local ad file and copy to target
                        TextUtils.equals(download.filehashCode, AdHash.getFileHash(adcache)))) {
                    File target = new File(download.TargetFile);
                    if (target.exists() && !AdConfig.GetCOPYALWAYS()) {//we check target file
                        //here we only comp size. but the best is hashcode
                        long t = target.length();
                        if (t > 0 && t == adcache.length()) {
                            FileUtils.Chmod(target);
                            return;
                        }
                    }
                    if (FileUtils.copyFile(adcache, target)) {
                        FileUtils.Chmod(target);
                    }
                }
            }
        } catch (Throwable e) {
            Log.d("dispatcherDoanload fail -->" + e.toString());
        } finally {
            if (download != null) {
                download.DownloaderFinish(download);
            }
        }
    }

    private File downloadFile(Download download, File adcache) {
        System.out.println("downloadFile");
        HttpURLConnection connection = null;
        RandomAccessFile randomAccessFile = null;
        InputStream inputstream = null;
        try {
            if (!URLUtil.isNetworkUrl(download.URL)) {
                android.util.Log.d(Log.TAG, "why use unaviable url " + download.URL);
                return null;
            }
            //////////// prep
            File local = new File(AdFileManager.getInstance().GetBasePath(), DOWNLOAD_DOWNLOAD);
            if (local.exists()) {//make sure local file is not exists.
                Log.d("Downloader remove temp first!!!!");
                FileUtils.Chmod(local);
                FileUtils.deleteFile(local.toString());
            }
            /////////// start download
            connection = (HttpURLConnection) (new URL(download.URL)).openConnection();
            connection.setConnectTimeout(HttpManager.SOCKET_TIMEOUT);//
            connection.setRequestMethod("GET");
                randomAccessFile = new RandomAccessFile(local, "rwd");
            inputstream = connection.getInputStream();
            byte[] buffer = new byte[1024 * 50];
            int length = -1;
            long compeleteSize = 0;
            long downloadfileSize = connection.getContentLength();
            checkdiskspace(downloadfileSize);
            ///download began
            while (((length = inputstream.read(buffer)) != -1)) {
                randomAccessFile.write(buffer, 0, length);
                compeleteSize += length;
                download.DownloaderProgress(download, compeleteSize, downloadfileSize);//notify pregress
                if (compeleteSize == downloadfileSize) {//download success
                    randomAccessFile.close();
                    File filetemp = new File(local.toString());
                    if (filetemp.exists()) {//check download local file
                        File filedone = new File(AdFileManager.getInstance().GetBasePath(), DOWNLOAD_FINISH);
                        if (filedone.exists()) filedone.delete();
                        Log.d("Dowerload complete and rename to filedone !!!!");
                        filetemp.renameTo(filedone);
                        FileUtils.Chmod(filedone);
                        return filedone;
                    }
                }
            }
        } catch (Throwable e) {
            Log.d("downloadFile fail -->" + e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.disconnect();
            } catch (Throwable e) {
            }
            try {
                if (randomAccessFile != null) randomAccessFile.close();
            } catch (Throwable e) {
            }
            try {
                if (inputstream != null) inputstream.close();
            } catch (Throwable e) {
            }
        }
        return null;
    }

    private void checkdiskspace(long downloadfileSize) {
        // TODO Auto-generated method stub
        //here we check disk space
        try {
            File dir = AdFileManager.getInstance().GetBasePath();
            long maxcachesize = AdConfig.GetCacheSize() * 1024 * 1024;//MB-->B
            while ((dir.length() + downloadfileSize) > maxcachesize) {//we should del some thing before
                if (dellastModifiedfile(dir) <= 0) {//make sure its break;
                    break;
                }
            }
        } catch (Throwable e) {

        }
    }

    private int dellastModifiedfile(File dir) {
        try {
            File[] list = dir.listFiles();
            if (list != null && list.length > 0) {
                ArrayList<File> files = (ArrayList<File>) Arrays.asList(list);
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File arg0, File arg1) {
                        // TODO Auto-generated method stub
                        return (int) (arg0.lastModified() - arg1.lastModified());
                    }
                });
                files.get(0).delete();
                return 1;
            }
            return 0;
        } catch (Throwable e) {

        }
        return -1;
    }
    private final static String DOWNLOAD_DOWNLOAD = "download_downloading";
    private final static String DOWNLOAD_FINISH = "download_finish";
}
