package com.joyplus.ad.db;

import android.content.Context;

import com.joyplus.ad.HttpManager;
import com.joyplus.ad.config.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhanghongwei on 2017/3/1.
 */

public class AdBootDoCacheRepot {

    private static AdBootDoCacheRepot cacheRepot = null;
    private Context mContext;
    private long mou = 1209600000l;//14天为周期来删除相应的过期上报数据
    private boolean once = true;

    private AdBootDoCacheRepot(Context context) {
        this.mContext = context;
    }

    public static AdBootDoCacheRepot getInstance(Context context) {
        if (cacheRepot == null) {
            cacheRepot = new AdBootDoCacheRepot(context);
        }
        return cacheRepot;
    }

    public void doCacheRepotWork(final int last) {
        final List<AdBootReportInfo> reportInfos = AdBootReprtDao.getInstance(mContext).getAllInfo();
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        if (null == reportInfos) {//如果数据库内没有数据的话直接不执行了
            return;
        }
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                for (AdBootReportInfo info : reportInfos) {
                    if (info.getType() != 2) {//type为2的类型为秒针的类型，这个类型目前先不去处理，只处理
                        //目前直接http请求的上报
                        if (!HttpManager.ReportServiceOneTime(info.getReportInfo())) {
                            Log.d("report failed");
                        } else {
                            Log.d("report success!");

                            //如果count为0则表示上报完成并删除该条上报链接
                            if (info.getCount() == 0) {
                                AdBootReprtDao.getInstance(mContext).reMove(info);
                            } else {//上报成功一次后将对count值进行一次操作
                                info.setCount(info.getCount() - 1);
                                AdBootReprtDao.getInstance(mContext).upDateOneInfo(info);
                            }
                        }
                    }
                }
            }
        });
        if (last == 5) {//每次执行完成任务之后将对缓存数据库内的链接进行检查，时长超过两周的链接将被删除掉
            deleteOverTime();
        }
    }

    public void setWork() {
        if (once) {
            final int[] count = {0};
            final Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    count[0]++;
                    if (count[0] <= 5) {
                        Log.d(count[0] + "次，进行缓存");
                        doCacheRepotWork(count[0]);
                    } else {
                        once = true;
                        timer.cancel();
                    }
                }
            };
            timer.schedule(task, 5000, 100000);
            once = false;
        }
    }

    /**
     * 对数据库内超时的链接进行删除操作
     */
    public void deleteOverTime() {
        List<AdBootReportInfo> infoList = AdBootReprtDao.getInstance(mContext).getAllInfo();
        long currentTime = System.currentTimeMillis();
        for (AdBootReportInfo info : infoList) {
            if (currentTime - info.getCreateTime().getTime() > mou) {
                AdBootReprtDao.getInstance(mContext).reMove(info);
            }
        }
    }
}
