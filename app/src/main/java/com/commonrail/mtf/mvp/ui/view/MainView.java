package com.commonrail.mtf.mvp.ui.view;

import com.commonrail.mtf.db.InjectorDb;
import com.commonrail.mtf.mvp.model.entity.FileUpload;
import com.commonrail.mtf.mvp.model.entity.Update;
import com.commonrail.mtf.mvp.model.entity.User;

import java.util.List;

/**
 * Created by wengyiming on 2016/3/1.
 */
public interface MainView {
    void showLoading();

    void hideLoading();

    void showUserError();

    void showInjectorsError();

    void showCheckUpdaterError();

    void showUpdateFileError();

    void setUserInfo(User user);

    void setInjectors(List<InjectorDb> injectors);

    void checkUpdate(Update mUpdate);

    void updateFile(FileUpload t);
}
