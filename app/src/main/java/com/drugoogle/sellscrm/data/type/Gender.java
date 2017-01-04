package com.drugoogle.sellscrm.data.type;

import com.drugoogle.sellscrm.R;

/**
 * Created by ydwang on 2016/5/13.
 */
public class Gender {
    /**男*/
    public static final int MALE = 1;

    /**女*/
    public static final int FEMALEMALE = 2;

    /**
     * 得到头像
     * @param status
     * @return
     */
    public static int getPortraitImgRes (int status)
    {
        switch (status)
        {
            case MALE:
                return R.drawable.icon_portrait_male;
            case FEMALEMALE:
                return R.drawable.icon_portrait_female;
            default:
                return R.drawable.icon_portrait_male;
        }
    }
}
