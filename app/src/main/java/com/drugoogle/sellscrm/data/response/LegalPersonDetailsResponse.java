package com.drugoogle.sellscrm.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by ydwang on 2016/5/10.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegalPersonDetailsResponse extends BaseResponse {

    public Data data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data{
        public String CHARGEPERSON;
        public String COMPANYNO;
        public int DISTRICTID;
        public int EASSTATE;
        public int GRCOMPANYTYPEID;

        public int GRCUSTOMID;
        public String GRCUSTOMNAME;
        public String GRCUSTOMOPCODE;
        public String GRSHORTCUSTOMNAME;
        public String INPUTDATE;

        public String INPUTMANID;
        public String LEGALPERSON;
        public String MEMO;
        public String ORGNIZECODE;
        public String QAPERSON;

        public String REGADDRESS;
        public String SPECIALNAME;
        public String SPECIALNO;
        public String TAXNUMBER;
        public String TEL;

        public int USESTATUS;
        public String XKZH;
        public String YYZZ;
        public String ZXBZD;
        public String ZXCOLUMN1;

        public String ZXCOLUMN10;
        public String ZXCOLUMN2;
        public String ZXCOLUMN3;
        public String ZXCOLUMN4;
        public String ZXCOLUMN5;

        public String ZXCOLUMN6;
        public String ZXCOLUMN7;
        public String ZXCOLUMN8;
        public String ZXCOLUMN9;
    }
}
