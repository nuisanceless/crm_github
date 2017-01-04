package com.drugoogle.sellscrm.data.Model;

/**
 * Created by ydwang on 2016/4/18.
 */
public class CustomerModel {

//    //按按名称首字母
//    public static final int CUSTOMER_SELECTTYPE_DEFAULT = 0;
//    //按级别
//    public static final int CUSTOMER_SELECTTYPE_LEVEL = 1;
//    //按离我最近
//    public static final int CUSTOMER_SELECTTYPE_NEAREST = 2;
//    //按最近拜访记录
//    public static final int CUSTOMER_SELECTTYPE_LATEST = 3;
//    //按拜访次数
//    public static final int CUSTOMER_SELECTTYPE_TIMES = 4;

    //所有级别
    public static final int CUSTOMER_LEVEL_DEFAULT = 0;
    //非常重要
    public static final int CUSTOMER_LEVEL_HIGH = 1;
    //重要
    public static final int CUSTOMER_LEVEL_MIDDLE = 2;
    //普通
    public static final int CUSTOMER_LEVEL_LOW = 3;

    private int customerName;
    private int customerLevel;
    private String customerInLegal;
    private String contactPeople;
    private String phone;
    private String mail;

}
