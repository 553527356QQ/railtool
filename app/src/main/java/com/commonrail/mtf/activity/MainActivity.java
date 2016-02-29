package com.commonrail.mtf.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.commonrail.mtf.AppClient;
import com.commonrail.mtf.R;
import com.commonrail.mtf.adapter.IndexAdapter;
import com.commonrail.mtf.base.BaseActivity;
import com.commonrail.mtf.db.Files;
import com.commonrail.mtf.db.FilesDao;
import com.commonrail.mtf.db.InjectorDb;
import com.commonrail.mtf.po.FileListItem;
import com.commonrail.mtf.po.FileUpload;
import com.commonrail.mtf.po.Result;
import com.commonrail.mtf.po.Update;
import com.commonrail.mtf.po.User;
import com.commonrail.mtf.util.Api.Config;
import com.commonrail.mtf.util.Api.RtApi;
import com.commonrail.mtf.util.DbHelp;
import com.commonrail.mtf.util.IntentUtils;
import com.commonrail.mtf.util.common.AppUtils;
import com.commonrail.mtf.util.common.Constant;
import com.commonrail.mtf.util.common.DateTimeUtil;
import com.commonrail.mtf.util.common.GlobalUtils;
import com.commonrail.mtf.util.common.L;
import com.commonrail.mtf.util.common.NetUtils;
import com.commonrail.mtf.util.common.SDCardUtils;
import com.commonrail.mtf.util.common.SPUtils;
import com.commonrail.mtf.util.retrofit.RxUtils;
import com.yw.filedownloader.BaseDownloadTask;
import com.yw.filedownloader.FileDownloadListener;
import com.yw.filedownloader.FileDownloadQueueSet;
import com.yw.filedownloader.FileDownloader;
import com.yw.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.dao.query.Query;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    @Bind(R.id.wendu)
    TextView wendu;
    @Bind(R.id.item_list)
    RecyclerView itemList;
    @Bind(R.id.uname)
    TextView uname;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.callFb)
    TextView callFb;
    @Bind(R.id.dateTime)
    TextView dateTime;


    private IndexAdapter mIndexAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        subscription = RxUtils.getNewCompositeSubIfUnsubscribed(subscription);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RxUtils.unsubscribeIfNotNull(subscription);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.title_activity_main);
        dateTime.setText(DateTimeUtil.format(DateTimeUtil.withYearFormat, new Date(System.currentTimeMillis())));


        api = RxUtils.createApi(RtApi.class, Config.BASE_URL);
        
        mIndexAdapter = new IndexAdapter(new ArrayList<InjectorDb>());
        itemList.setAdapter(mIndexAdapter);


      
        doLogin("");
        getIndexList("zh_CN");//"zh_CN";//en_US
        checkUpdate();
        updateFile();
        callFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AppUtils.callPhone(MainActivity.this, callFb.getText().toString().trim());
            }
        });
//        final float scale = getActivity().getResources().getDisplayMetrics().density;
//        L.e("scale:" + scale + "");

//        String url = "http://dl.game.qidian.com/apknew/game/dzz/dzz.apk";
//        String savePath1 = FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "tmp1";
//        L.e("savePath"+savePath1);
//        downloadApkAndUpdate(url, savePath1);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    public Activity getActivity() {
        return this;
    }


    private void doLogin(final String username) {
        subscription.add(api.getUserInfo(username)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Result<User>, User>() {
                    @Override
                    public User call(Result<User> t) {
                        L.e("getUserInfo： " + t.getStatus() + t.getMsg());
                        if (t.getStatus() != 200) {
                            GlobalUtils.showToastShort(AppClient.getInstance(), getString(R.string.net_error));
                            return null;
                        }

                        GlobalUtils.showToastShort(AppClient.getInstance(), t.getMsg());
                        return t.getData();
                    }
                })
                .subscribe(new Action1<User>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void call(User t) {
                        if (t == null) return;
                        String name = t.getUname();
                        uname.setText(name + "你好，欢迎！");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        L.e("" + throwable.toString());
                        GlobalUtils.showToastShort(MainActivity.this, getString(R.string.net_error));
                    }
                }));
    }


    private void getIndexList(final String language) {
        subscription.add(api.getIndexList(AppUtils.getMap("language", language))
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Result<List<InjectorDb>>, List<InjectorDb>>() {
                    @Override
                    public List<InjectorDb> call(Result<List<InjectorDb>> t) {
                        L.e("getIndexList： " + t.getStatus() + t.getMsg());
                        if (t.getStatus() != 200) {
                            GlobalUtils.showToastShort(AppClient.getInstance(), getString(R.string.net_error));
                            return null;
                        }
                      
                        DbHelp.getInstance(MainActivity.this).saveInjectorLists(t.getData());
                        GlobalUtils.showToastShort(AppClient.getInstance(), t.getMsg());
                        return t.getData();
                    }
                })
                .subscribe(new Action1<List<InjectorDb>>() {
                    @Override
                    public void call(final List<InjectorDb> t) {
                        if (t == null) return;
                        if (!t.isEmpty()) {
                            fillRvData(t, language);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        L.e("" + throwable.toString());
                        GlobalUtils.showToastShort(MainActivity.this, getString(R.string.net_error));
                        L.e("getIndexList： load from db", DbHelp.getInstance(MainActivity.this).loadAllInjector().size()+"");
                        fillRvData(DbHelp.getInstance(MainActivity.this).loadAllInjector(), language);

                    }
                }));
    }

    private void fillRvData(final List<InjectorDb> t, final String language) {
        mIndexAdapter.setInjectors(t);
        mIndexAdapter.notifyDataSetChanged();
        mIndexAdapter.setClick(new IndexAdapter.Click() {
            @Override
            public void itemClick(int p) {
                IntentUtils.enterModuleListActivity(MainActivity.this, t.get(p).getInjectorType(), language);
            }
        });
    }

    private void checkUpdate() {
        subscription.add(api.appVersion("")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Result<Update>, Update>() {
                    @Override
                    public Update call(Result<Update> t) {
                        L.e("checkUpdate： " + t.getStatus() + t.getMsg());
                        if (t.getStatus() != 200) {
                            GlobalUtils.showToastShort(AppClient.getInstance(), getString(R.string.net_error));
                            return null;
                        }
                        GlobalUtils.showToastShort(AppClient.getInstance(), t.getMsg());
                        return t.getData();
                    }
                })
                .subscribe(new Action1<Update>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void call(Update t) {
                        if (t == null) return;
                        final String vc = t.getAppVersionCode();
                        boolean forced = t.getForced();
                        final String url = t.getUrl();
                        L.e(t.toString());
                        if (!AppUtils.checkVersion(vc)) return;
                        GlobalUtils.ShowDialog(MainActivity.this, "提示", "发现新版本，是否更新", !forced, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                //download and update
                                String savePath1 = FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "railtool" + vc + ".apk";
                                downloadApkAndUpdate(url, savePath1);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog != null) dialog.dismiss();
                            }
                        });
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        L.e("checkUpdate" + throwable.toString());
                        GlobalUtils.showToastShort(MainActivity.this, getString(R.string.net_error));
                    }
                }));
    }

    private void downloadApkAndUpdate(String url, String savePath) {
        FileDownloader.getImpl().create(url)
                .setPath(savePath)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        L.e("pending:" + "已下载：" + soFarBytes + " 文件总大小：" + totalBytes);

                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        L.e("connected:" + "已下载：" + soFarBytes + " 文件总大小：" + totalBytes + "  百分比:" + (float) soFarBytes / (float) totalBytes * 100 + "%");
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        L.e("progress:" + "已下载：" + soFarBytes + " 文件总大小：" + totalBytes + "  百分比" + (float) soFarBytes / (float) totalBytes * 100 + "%");
                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                        L.e("blockComplete:");
                    }

                    @Override
                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                        L.e("progress:" + soFarBytes + "" + ex.toString() + " 已下载:" + soFarBytes);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        L.e("completed:");
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        L.e("paused:" + "已下载：" + soFarBytes + " 文件总大小：" + totalBytes + "百分比:" + (float) soFarBytes / (float) totalBytes * 100 + "%");
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        L.e("error:" + e.toString());
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        L.e("warn:");
                    }
                }).start();
    }


    private void updateFile() {
        final int localFileVersion = (int) SPUtils.get(this, Constant.FILE_VERSION, 0);
        HashMap<String, Integer> mHashMap = new HashMap<>();
        mHashMap.put(Constant.FILE_VERSION, localFileVersion);
        L.e("updateFile", mHashMap.toString());
        subscription.add(api.updateFile(mHashMap)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Result<FileUpload>, FileUpload>() {
                    @Override
                    public FileUpload call(Result<FileUpload> t) {
                        L.e("updateFile： " + t.getStatus() + t.getMsg());
                        if (t.getStatus() != 200) {
                            GlobalUtils.showToastShort(AppClient.getInstance(), getString(R.string.net_error));
                            return null;
                        }
                        GlobalUtils.showToastShort(AppClient.getInstance(), t.getMsg());
                        return t.getData();
                    }
                })
                .subscribe(new Action1<FileUpload>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void call(FileUpload t) {
                        if (t == null) {
                            L.e("updateFile", "请求结果为空");
                            return;
                        }
                        L.e("updateFile", "请求结果不为空" + t.toString());
                        //wifi网络下自动下载最新图片和视频资源
                        if (NetUtils.isWifi(MainActivity.this)) {
                            if (t.getFileList() == null || t.getFileList().isEmpty()) {
                                L.e("updateFile", " 文件列表为空,当前版本即最新版本:" + localFileVersion + " 最新文件版本号为:" + t.getVersionCode());
                                return;
                            }
                            L.e("updateFile", "本地文件版本号:" + localFileVersion + " 服务器文件版本号" + t.getVersionCode());
                            if (localFileVersion < t.getVersionCode()) {//如果服务器文件版本大于本地文件版本,则有新的更新
                                L.e("updateFile", "发现新的文件");
                                downloadFiles(t.getFileList(), t.getVersionCode());
                            }

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        L.e("updateFile", throwable.toString());
                        GlobalUtils.showToastShort(MainActivity.this, getString(R.string.net_error));
                    }
                }));
    }


    private void downloadFiles(final List<FileListItem> fileList, int latestVersion) {
        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(queueTarget);
        final List<BaseDownloadTask> tasks = new ArrayList<>();

        List<Files> mFilesDbQuene =  DbHelp.getInstance(this).getFilesDao().loadAll();
        List<Files> mFileTmpQuene = new ArrayList<>();//创建一个临时的待下载队列
        if (mFilesDbQuene == null || mFilesDbQuene.size() <= 0) {
            //本地没有任何记录,说明需要更新
            if (mFilesDbQuene == null || mFilesDbQuene.isEmpty()) {//如果本地没有下载记录,创建记录,并开始下载
                for (int i = 0; i < fileList.size(); i++) {
                    FileListItem mFileListItem = fileList.get(i);
                    Files localFile = new Files();
                    localFile.setFileStatus(0);
                    localFile.setFileLen(mFileListItem.getFileLength());
                    localFile.setFileLocalUrl(mFileListItem.getLocalUrl());
                    localFile.setFileType(mFileListItem.getFileType());
                    localFile.setFileUrl(mFileListItem.getUrl());
                    mFileTmpQuene.add(localFile);//加入待下载任务
                }
                DbHelp.getInstance(MainActivity.this).saveFileLists(mFileTmpQuene);//保存队列
            }
        } else {//如果本地有未完成的记录,将未完成的任务加入待下载队列
            Query query =  DbHelp.getInstance(this).getFilesDao().queryBuilder()
                    .where(FilesDao.Properties.FileStatus.eq(0))
                    .build();
            List mFiles = query.list();
            if (mFiles != null && mFiles.size() > 0) {//且未完成数大于0,则继续下载
                mFileTmpQuene.addAll(mFiles);
            } else {//本地有完整的记录,未完成的为0,即全部都已完成,保存最新文件版本号
                SPUtils.put(MainActivity.this, Constant.FILE_VERSION, latestVersion);
            }
        }
        //循环创建下载任务
        for (int i = 0; i < mFileTmpQuene.size(); i++) {
            Files mFilesDb = mFileTmpQuene.get(i);
            tasks.add(FileDownloader.
                    getImpl()
                    .create(mFilesDb.getFileUrl())
                    .setPath(SDCardUtils.getSDCardPath() + File.separator + "Download" + File.separator + "railTool" + mFilesDb.getFileLocalUrl())
                    .setTag(fileList.get(i).getLocalUrl()));
        }
        // 由于是队列任务, 这里是我们假设了现在不需要每个任务都回调`FileDownloadListener#progress`, 我们只关系每个任务是否完成, 所以这里这样设置可以很有效的减少ipc.
        queueSet.disableCallbackProgressTimes();
        // 所有任务在下载失败的时候都自动重试一次
        queueSet.setAutoRetryTimes(1);
        // 串行执行该任务队列
        queueSet.downloadSequentially(tasks);
        // 并行执行该任务队列
        queueSet.downloadTogether(tasks);
    }

    final FileDownloadListener queueTarget = new FileDownloadListener() {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void blockComplete(BaseDownloadTask task) {
        }

        @Override
        protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            String localUrl = (String) task.getTag();
            if (localUrl.endsWith(".mp4")) {

            } else if (localUrl.endsWith(".jpg")) {

            }
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
        }

        @Override
        protected void warn(BaseDownloadTask task) {
        }
    };
}
