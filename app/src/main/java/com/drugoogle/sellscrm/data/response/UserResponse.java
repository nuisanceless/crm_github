package com.drugoogle.sellscrm.data.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by wgh on 2016/5/4.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    public String ACTIVATED;
    public String AVATAR;
    public String CREATE_TIME;
    public String DEL_FLAG;
    public String ID;
    public String LAST_LOGIN_IP;
    public String MODIFY_TIME;
    public String PASSWORD;
    public String REF_ID;
}
