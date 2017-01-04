package com.drugoogle.sellscrm.data.type;

import com.drugoogle.sellscrm.R;

/**
 * Created by ydwang on 2016/5/24.
 */
public class SaleRecordStatus {

    /**
     * 0：未装车
     */
    public static final int NOUPLOAD = 0;
    /**
     * 1：装车中
     */
    public static final int UPLOADING = 1;
    /**
     * 2：已装车
     */
    public static final int UPLOADED = 2;
    /**
     * 3：确认中
     */
    public static final int CONFIRMING = 3;
    /**
     * 4：已确认
     */
    public static final int CONFIRMED = 4;
    /**
     * 5：申请扣款
     */
    public static final int APPLYDEBIT = 5;
    /**
     * 6：扣款成功
     */
    public static final int DEBITSUCCESS = 6;
    /**
     * 7：扣款失败
     */
    public static final int DEBITFAIL = 7;

    public static int getRecordStatusStr(int status) {
        switch (status) {
            case NOUPLOAD:
                return R.string.no_upload;
            case UPLOADING:
                return R.string.uploading;
            case UPLOADED:
                return R.string.uploaded;
            case CONFIRMING:
                return R.string.confirming;
            case CONFIRMED:
                return R.string.confirmed;
            case APPLYDEBIT:
                return R.string.apply_debit;
            case DEBITSUCCESS:
                return R.string.debit_success;
            case DEBITFAIL:
                return R.string.debit_fail;
            default:
                return R.string.no_upload;
        }
    }

}
