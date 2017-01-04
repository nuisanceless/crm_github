package com.drugoogle.sellscrm.rest;


import android.support.v4.media.VolumeProviderCompat;

import com.drugoogle.sellscrm.data.request.AddAddressRequest;
import com.drugoogle.sellscrm.data.request.AddVisitPlanRequest;
import com.drugoogle.sellscrm.data.request.AddVisitRecordRequest;
import com.drugoogle.sellscrm.data.request.AddressListRequest;
import com.drugoogle.sellscrm.data.request.DealListRequest;
import com.drugoogle.sellscrm.data.request.EditAddressRequest;
import com.drugoogle.sellscrm.data.request.EditVisitPlanRequest;
import com.drugoogle.sellscrm.data.request.EditVisitRecordRequest;
import com.drugoogle.sellscrm.data.request.EndVisitRequest;
import com.drugoogle.sellscrm.data.request.FinishWorkOrderRequest;
import com.drugoogle.sellscrm.data.request.MisinformationRequest;
import com.drugoogle.sellscrm.data.request.SaleRecordListRequest;
import com.drugoogle.sellscrm.data.request.SearchVisitRequest;
import com.drugoogle.sellscrm.data.request.WorkOrderListRequest;
import com.drugoogle.sellscrm.data.response.AddressListResponse;
import com.drugoogle.sellscrm.data.response.AutoVisitCustomerListResponse;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.BeginAutoVisitResponse;
import com.drugoogle.sellscrm.data.response.ChangePasswordResponse;
import com.drugoogle.sellscrm.data.response.CreditRecordListResponse;
import com.drugoogle.sellscrm.data.response.CreditRecordResponse;
import com.drugoogle.sellscrm.data.response.CurrentVisitRecordResponse;
import com.drugoogle.sellscrm.data.response.CustomerDetailsResponse;
import com.drugoogle.sellscrm.data.response.CustomerVisitCountResponse;
import com.drugoogle.sellscrm.data.response.DealListResponse;
import com.drugoogle.sellscrm.data.response.LegalPersonDetailsResponse;
import com.drugoogle.sellscrm.data.response.LogTagResponse;
import com.drugoogle.sellscrm.data.response.LoginResponse;
import com.drugoogle.sellscrm.data.response.NewWorkOrderCountResponse;
import com.drugoogle.sellscrm.data.response.PlanDetailResponse;
import com.drugoogle.sellscrm.data.response.RecordDetailResponse;
import com.drugoogle.sellscrm.data.response.RemainingCreditResponse;
import com.drugoogle.sellscrm.data.response.SaleRecordDetailResponse;
import com.drugoogle.sellscrm.data.response.SaleRecordListResponse;
import com.drugoogle.sellscrm.data.response.SendVerifyResponse;
import com.drugoogle.sellscrm.data.response.ShowCustomerListResponse;
import com.drugoogle.sellscrm.data.response.SummaryCalendarResponse;
import com.drugoogle.sellscrm.data.response.UploadFileResponse;
import com.drugoogle.sellscrm.data.response.VerifyResponse;
import com.drugoogle.sellscrm.data.response.VisitPlanListResponse;
import com.drugoogle.sellscrm.data.response.WorkOrderDetailResponse;
import com.drugoogle.sellscrm.data.response.WorkOrderListResponse;

import org.androidannotations.rest.spring.annotations.Body;
import org.androidannotations.rest.spring.annotations.Delete;
import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Header;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.Post;
import org.androidannotations.rest.spring.annotations.RequiresCookie;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.annotations.SetsCookie;
import org.androidannotations.rest.spring.api.RestClientErrorHandling;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Created by wgh on 2016/3/31.
 */
//@Rest(rootUrl = "http://192.168.2.118:8088",//测试服务器
@Rest(rootUrl = "http://60.191.39.27:9999",//英特外网地址//账号008305密码123456
        converters = {MappingJackson2HttpMessageConverter.class,
                ByteArrayHttpMessageConverter.class,
                FormHttpMessageConverter.class,
                StringHttpMessageConverter.class})
public interface MyRestClient extends RestClientErrorHandling {
    public RestTemplate getRestTemplate();

    public void setRestTemplate(RestTemplate template);

    void setRootUrl(String rootUrl);

    String getRootUrl();

    void setCookie(String name, String value);

    String getCookie(String name);


    //登陆
    @Get("/api/account/login?account={account}&password={password}")
    @SetsCookie({"JSESSIONID"})
    LoginResponse login(@Path String account, @Path String password);

    //获取重置密码验证码
    @Get("/api/account/sendRestPasswordCode?phone={phone}")
    VerifyResponse getForgetPasswordVerifyCode(@Path String phone);

    //验证重置密码验证码
    @Get("/api/account/checkRestPwdSms?phone={phone}&code={code}")
    SendVerifyResponse verifyForgetPasswordAuthCode(@Path String phone, @Path String code);

//    //获取修改密码验证码
//    @Post("/member/changePwd/getAuthCode")
//    @RequiresCookie({"JSESSIONID"})
//    BaseResponse getChangePasswordVerifyCode();

    //修改密码
    @Post("/api/account/changePassword?phone={phone}&resetToken={resetToken}&newPwd={newPwd}")
    @RequiresCookie({"JSESSIONID"})
    ChangePasswordResponse ChangePasswordRequest(@Path String phone, @Path String resetToken, @Path String newPwd);

//    //获取个人信息
//    @Get("/api/ccCustomer/getCustomerAndVisitCount?token={token}")
//    SelfInfoResponse getCustomerInfo(@Path String userId,@Path String token);


    //删除拜访计划或者纪录
    @Get("/api/visitPlan/delete?visitPlanId={id}&token={token}")
    @RequiresCookie({"JSESSIONID"})
    BaseResponse deleteVisitPlan(@Path int id, @Path String token);


    //修改拜访计划
    @Post("/api/visitPlan/editVisitPlan?token={token}")
    @RequiresCookie({"JSESSIONID"})
    BaseResponse editVisitPlan(@Body EditVisitPlanRequest request, @Path String token);

    //添加拜访计划
    @Post("/api/visitPlan/save?token={token}")
    @RequiresCookie({"JSESSIONID"})
    BaseResponse addVisitPlan(@Body AddVisitPlanRequest request, @Path String token);

    //添加拜访记录
    @Post("/api/visitRecord/save?token={token}")
    @RequiresCookie({"JSESSIONID"})
    BaseResponse addVisitRecord(@Body AddVisitRecordRequest request, @Path String token);

    //获取日历概要
    @Get("/api/visitPlan/getAllPlanByDate?beginDate={beginDate}&endDate={endDate}&token={token}")
    @RequiresCookie({"JSESSIONID"})
    SummaryCalendarResponse getSummaryCalendar(@Path String beginDate, @Path String endDate, @Path String token);

    //获取当日计划记录列表
    @Get("/api/visitPlan/getVisitList?thisDate={date}&token={token}&flagId={flagId}")
    @RequiresCookie({"JSESSIONID"})
    VisitPlanListResponse getVisitPlanList(@Path String date, @Path String token, @Path int flagId);

    //获取拜访计划详情
    @Get("/api/visitPlan/getVisitPlan?visitPlanId={id}&token={token}")
    @RequiresCookie("JSESSIONID")
    PlanDetailResponse getPlanDetail(@Path int id, @Path String token);

    //获取拜访记录详情
    @Get("/api/visitRecord/getVisitRecord?visitRecordId={id}&token={token}")
    @RequiresCookie("JSESSIONID")
    RecordDetailResponse getRecordDetail(@Path int id, @Path String token);

    //获取当前位置周边的客户列表
    @Get("/api/ccCustomer/getCustomersByDistance?longitude={longitude}&latitude={latitude}&token={token}")
    @RequiresCookie("JSESSIONID")
    AutoVisitCustomerListResponse getAroundCustomerList(@Path double longitude, @Path double latitude, @Path String token);

    //开始自动拜访
    @Get("/api/visitRecord/autoVisitTwo?customerId={customerId}&longitude={longitude}&latitude={latitude}&token={token}")
    @RequiresCookie("JSESSIONID")
    BeginAutoVisitResponse beginAutoVisit(@Path long customerId, @Path double longitude, @Path double latitude, @Path String token);

    //结束拜访
    @Post("/api/visitRecord/endVisit?token={token}")
    @RequiresCookie("JSESSIONID")
    BaseResponse endVisit(@Body EndVisitRequest request, @Path String token);

    //获取当前正在拜访的计划
    @Get("/api/visitRecord/getNowPlan_VTwo?token={token}")
    @RequiresCookie("JSESSIONID")
    CurrentVisitRecordResponse getCurrentVisitPlan(@Path String token);

    //拜访搜索
    @Post("/api/visitPlan/queryVisit?pageNumber={pageNumber}&pageSize={pageSize}&token={token}")
    @RequiresCookie("JSESSIONID")
    VisitPlanListResponse visitSearch(@Path int pageNumber, @Path int pageSize, @Path String token, @Body SearchVisitRequest request);


    //系统误报
    @Post("/api/visitPlan/misinformation?token={token}")
    @RequiresCookie("JSESSIONID")
    BaseResponse locationMisinformation (@Body MisinformationRequest request, @Path String token);

    //获取客户列表
    @Get("/api/ccCustomer/getList?pageNumber={pageNumber}&pageSize={pageSize}&nameOrNum={nameOrNum}&sort={sort}&condition={condition}&longitude={longitude}&latitude={latitude}&token={token}")
    @RequiresCookie({"JSESSIONID"})
    ShowCustomerListResponse getClientList(@Path int pageNumber, @Path int pageSize, @Path String nameOrNum, @Path int sort,@Path int condition,@Path double longitude,@Path double latitude, @Path String token);

    //获取客户详情
    @Get("/api/ccCustomer/getCcCustomer?id={id}&token={token}")
    CustomerDetailsResponse getCustomerDetailsInfo(@Path String id, @Path String token);

    //编辑客户重要程度
    @Get("/api/ccCustomer/editGrade?id={id}&condition={condition}&token={token}")
    BaseResponse editCustomerLevel(@Path String id,@Path int condition, @Path String token);

    //获取法人详情
    @Get("/api/ccCustomer/getZxPubGrCustomer?ccCustomerId={ccCustomerId}&token={token}")
    LegalPersonDetailsResponse getLegalPersonDetailsInfo(@Path String ccCustomerId, @Path String token);

    //获取地址列表
    @Post("/api/customeraddress/getList?token={token}")
    @RequiresCookie({"JSESSIONID"})
    AddressListResponse getAddressList ( @Body AddressListRequest request,@Path String token);

    //删除地址
    @Post("/api/customeraddress/delAddress?token={token}&id={id}")
    @RequiresCookie("JSESSIONID")
    BaseResponse deleteAddress (@Path String token,@Path int id);

    //添加地址
    @Post("/api/customeraddress/addAddress?token={token}")
    @RequiresCookie("JSESSIONID")
    BaseResponse addAddress(@Body AddAddressRequest request,@Path String token);

    //修改地址
    @Post("/api/customeraddress/editAddress?token={token}")
    @RequiresCookie("JSESSIONID")
    BaseResponse editAddress(@Body EditAddressRequest request,@Path String token);

    //请求半径之内的客户
    //TODO ?
    @Get("/api/ccCustomer/getCustomersByDistance?longitude={longitude}&latitude={latitude}&token={token}")
    @RequiresCookie("JSESSIONID")
    BaseResponse getCustomersByDistance(@Path double longitude,@Path double latitude,@Path String token);

    //客户数/拜访剩余数
    @Get("/api/ccCustomer/getCustomerAndVisitCount?token={token}")
    @RequiresCookie("JSESSIONID")
    CustomerVisitCountResponse getCustomerAndVisitCount(@Path String token);

    //查询交易记录
    @Post("/api/sellorder/getList?token={token}")
    @RequiresCookie("JSESSIONID")
    SaleRecordListResponse getSaleRecordList(@Body SaleRecordListRequest request,@Path String token);

    //查询单笔交易记录
    @Get("/api/sellorder/getOrderDetail?token={token}&sellOrderId={sellOrderId}")
    @RequiresCookie("JSESSIONID")
    SaleRecordDetailResponse getSaleRecordDetail(@Path String token, @Path String sellOrderId);

    //查询交易条目列条
    @Post("/api/sellorder/getOrderItem?token={token}")
    @RequiresCookie("JSESSIONID")
    DealListResponse getDealList(@Body DealListRequest request, @Path String token);

    //获取剩余信用额度
    @Get("/api/itCredit/getRemainingCredits?customerId={customerId}&token={token}")
    @RequiresCookie("JSESSIONID")
    RemainingCreditResponse getRemainingCredits(@Path String customerId, @Path String token);

    //查询信用记录
    @Get("/api/itCredit/getItSellSettlementList?pageNumber={pageNumber}&pageSize={pageSize}&customerId={customerId}&token={token}")
    @RequiresCookie("JSESSIONID")
    CreditRecordListResponse getCrreditRecordList(@Path int pageNumber,@Path int pageSize,@Path int customerId, @Path String token);

    //查询信用记录详情
    @Get("/api/itCredit/getItSellSettlementDetail?id={id}&token={token}")
    @RequiresCookie("JSESSIONID")
    CreditRecordResponse getCreditRecordDetail(@Path int id,@Path String token);

    //获取工单列表(支持分页&筛选)
    @Post("/api/workorder/getList?token={token}")
    @RequiresCookie("JSESSIONID")
    WorkOrderListResponse getWorkOrderList(@Body WorkOrderListRequest request, @Path String token);

    //查看工单详情
    @Get("/api/workorder/getDetail?token={token}&workOrderId={workOrderId}")
    @RequiresCookie("JSESSIONID")
    WorkOrderDetailResponse getWorkOrderDetail(@Path String token, @Path String workOrderId);

    //设置工单为待处理
    @Get("/api/workorder/setViewed?token={token}&workOrderId={workOrderId}")
    @RequiresCookie("JSESSIONID")
    BaseResponse setWorkOrderViewed(@Path String token, @Path String workOrderId);

    //设置工单为处理中(我知道了按钮)
    @Get("/api/workorder/setProcessing?token={token}&workOrderId={workOrderId}")
    @RequiresCookie("JSESSIONID")
    BaseResponse setWorkOrderProcessing(@Path String token, @Path String workOrderId);

    //设置工单为已完成
    @Post("/api/workorder/setFinished?token={token}&workOrderId={workOrderId}")
    @RequiresCookie("JSESSIONID")
    BaseResponse setWorkOrderFinished(@Body FinishWorkOrderRequest request, @Path String token, @Path String workOrderId);

    @Post("/api/visitRecord/uploadFile?token={token}")
    @Header(name="Content-Type",value="multipart/form-data")
    @RequiresCookie("JSESSIONID")
    UploadFileResponse uploadVisitRecordFiles(@Body MultiValueMap<String, Object> data, @Path String token);

    @Post("/api/visitRecord/editVisitRecord?token={token}")
    @RequiresCookie("JSESSIONID")
    BaseResponse editVisitRecord(@Body EditVisitRecordRequest request, @Path String token);

    //获取未查看工单数
    @Get("/api/workorder/getNotViewedWorkOrderCount?token={token}")
    @RequiresCookie("JSESSIONID")
    NewWorkOrderCountResponse getNewWorkOrderCount(@Path String token);

    //获取是否上次log的tag
    @Get("/api/version/getLogTag?token={token}")
    @RequiresCookie("JSESSIONID")
    LogTagResponse getLogTag (@Path String token);

    @Post("/api/version/uploadClientLog?token={token}")
    @Header(name="Content-Type",value="multipart/form-data")
    @RequiresCookie("JSESSIONID")
    BaseResponse uploadLog(@Body MultiValueMap<String, Object> data, @Path String token);
}
