package com.drugoogle.sellscrm.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Lei on 2015/6/9.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse extends BaseResponse {

    public Info info;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Info
    {
        public int logTag;
        public String phone;
        public String name;
        public String birth;
        public String avatar;
        public String userId;
        public String email;
        public String sex;
    }
    public String token;


}
