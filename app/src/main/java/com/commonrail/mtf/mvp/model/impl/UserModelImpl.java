package com.commonrail.mtf.mvp.model.impl;

import android.annotation.SuppressLint;

import com.commonrail.mtf.AppClient;
import com.commonrail.mtf.R;
import com.commonrail.mtf.mvp.model.UserModel;
import com.commonrail.mtf.mvp.model.entity.Result;
import com.commonrail.mtf.mvp.model.entity.User;
import com.commonrail.mtf.mvp.presenter.OnUserListener;
import com.commonrail.mtf.util.Api.RtApi;
import com.commonrail.mtf.util.common.GlobalUtils;
import com.commonrail.mtf.util.common.L;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wengyiming on 2016/3/1.
 */
public class UserModelImpl implements UserModel {
    @Override
    public void loadUser(CompositeSubscription subscription, RtApi api, OnUserListener listener) {
        subscription.add(api.getUserInfo("")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Result<User>, User>() {
                    @Override
                    public User call(Result<User> t) {
                        L.e("getUserInfo： " + t.getStatus() + t.getMsg());
                        if (t.getStatus() != 200) {
                            GlobalUtils.showToastShort(AppClient.getInstance(), AppClient.getInstance().getString(R.string.net_error));
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
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        L.e("" + throwable.toString());
//                        GlobalUtils.showToastShort(MainActivity.this, getString(R.string.net_error));
                    }
                }));
    }
}
