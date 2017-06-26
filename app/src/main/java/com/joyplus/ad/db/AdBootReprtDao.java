package com.joyplus.ad.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.joyplus.ad.config.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghongwei on 2017/3/1.
 */

public class AdBootReprtDao {
    private static AdBootReprtDao dao = null;
    private Context mContext;

    private AdBootReprtDao(Context context){
        this.mContext = context;
    }
    public static AdBootReprtDao getInstance(Context context){
        if(dao == null){
            dao = new AdBootReprtDao(context);
        }
        return dao;
    }
    public SQLiteDatabase getConnection() {
        SQLiteDatabase sqliteDatabase = null;
        try {
            sqliteDatabase = new DBHelper(mContext).getReadableDatabase();
        } catch (Exception e) {
        }
        return sqliteDatabase;
    }
    public synchronized boolean InsertOneInfo(AdBootImpressionInfo info,int type){
        if (info == null || !info.IsAviable()) return false;
        SQLiteDatabase database = getConnection();
        List<AdBootReportInfo> reportInfos = getAllInfo();
        try {
            if (info != null) {
                if(reportInfos != null) {
                    for (AdBootReportInfo info1 : reportInfos) {
                        Log.d(info1.getReportInfo()+":"+info.mImpressionUrl);
                        if (info1.getReportInfo().equals(info.mImpressionUrl)) {
                            info1.setCount(info1.getCount() + 1);
                            return upDateOneInfo(info1);
                        }
                    }
                }
                Log.d("will insert to cache_link");
                return database.insert("cache_link",null,AdBootImpressionInfo.GetNewContentValues(info,type)) > 0;
        }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
        }
        return false;
    }
    public synchronized boolean InsertOneInfo(ContentValues values){
        if (values == null) return false;
        List<AdBootReportInfo> reportInfos = getAllInfo();
        SQLiteDatabase database = getConnection();
        String re = (String) values.get("report_url");
        try {
            for (AdBootReportInfo info : reportInfos){
                if(re.equals(info.getReportInfo())){
                    info.setCount(info.getCount()+1);
                    return upDateOneInfo(info);
                }
            }
            if (values != null) {
                return database.insert("cache_link",null,values) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
        }
        return false;
    }

    public synchronized List<AdBootReportInfo> getAllInfo(){
        SQLiteDatabase database = getConnection();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("select * from cache_link order by _id ASC", null);
            ArrayList<AdBootReportInfo> Info = new ArrayList<AdBootReportInfo>();
            if (cursor.moveToFirst()) {
                Info.add(AdBootImpressionInfo.GetAdBootReportInfo(cursor));
                while (cursor.moveToNext()) {
                    Info.add(AdBootImpressionInfo.GetAdBootReportInfo(cursor));
                }
                return Info;
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) cursor.close();
            if (database != null) database.close();
        }
        return null;
    }
    public synchronized boolean upDateOneInfo(AdBootReportInfo info){
        if (info == null) return false;
        SQLiteDatabase database = getConnection();
        try {
            ContentValues Value = AdBootImpressionInfo.GetNewContentValues(info);
            if (info != null) {
                return database.update("cache_link", Value, "report_url=?", new String[]{info.getReportInfo()}) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
        }
        return false;
    }
    public synchronized void reMove(AdBootReportInfo info){
        if (info == null) return;
        SQLiteDatabase database = getConnection();
        try {
            database.delete("cache_link", "report_url=?", new String[]{info.getReportInfo()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
        }
    }

}
