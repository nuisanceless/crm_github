package com.drugoogle.sellscrm.data.type;

import com.drugoogle.sellscrm.R;

/**
 * Created by wgh on 2016/4/13.
 * 客户重要程度
 */
public class ImportantLevel
{
    /**高*/
    public static final int HIGH = 3;

    /**中*/
    public static final int MIDDLE = 2;

    /**低*/
    public static final int LOW = 1;

    public static int getImportantLevelStrRes (int level)
    {
        switch (level)
        {
            case HIGH:
                return R.string.level_high;
            case MIDDLE:
                return R.string.level_middle;
            case LOW:
                return R.string.level_low;
            default:
                return R.string.level_high;
        }
    }

    public static int getImportantLevelImgRes (int level)
    {
        switch (level)
        {
            case HIGH:
                return R.drawable.icon_important_level_high;
            case MIDDLE:
                return R.drawable.icon_important_level_middle;
            case LOW:
                return R.drawable.icon_important_level_low;
            default:
                return R.drawable.icon_important_level_low;
        }
    }
}
