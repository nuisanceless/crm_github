package com.drugoogle.sellscrm.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by wgh on 2016/5/9.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordDetailInfo implements Serializable {
    private static final long serialVersionUID = 10L;

    public String address;//具体地址
    public int business;
    public String endDate;
    public String city;//城市
    public String latitude;
    public int rule;
    public String planRemark;
    public String remark;
    public String customerName;
    public int is_admin;
    public String oldAddress;
    public String lengthStr;
    public String province;//省
    public int visitWay;
    public String subaddr;//详细楼层信息
    public String customerId;
    public String customerLatitude;
    public int id;
    public String region;//区
    public String customerLongitude;
    public String visitStartDate;
    public String longitude;
    public int status;
    public List<VisitRecordFileResponse> fileList;


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VisitRecordFileResponse implements Serializable {
        private static final long serialVersionUID = 10L;

        public int id;
        public String filePath; //thumbnail file path
    }

}
