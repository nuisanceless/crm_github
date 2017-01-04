package com.drugoogle.sellscrm.Utils;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by Lei on 2016/6/2.
 */
@SharedPref(value = SharedPref.Scope.UNIQUE)
interface PrefUtils {

    String name();//账号

    String password();//密码

    int logTag();

    String userName();//用户名

    String phone();

    String birth();

    String avatar();

    int userId();

    String email();

    int gender();

    String token();

    int customerCount();

    int visitCount();

    boolean withoutLogin();//直接启动不需要登录
}
