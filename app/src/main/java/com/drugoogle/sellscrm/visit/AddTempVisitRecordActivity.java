package com.drugoogle.sellscrm.visit;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.ImageDecodeType;
import com.drugoogle.sellscrm.Utils.ImageUtils;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.common.Consts;
import com.drugoogle.sellscrm.common.DateTimePickPopupWindow;
import com.drugoogle.sellscrm.common.PopupWindowListAdapter;
import com.drugoogle.sellscrm.common.TimePickPopupWindow;
import com.drugoogle.sellscrm.customer.AddressManagementActivity_;
import com.drugoogle.sellscrm.customer.ClientListActivity;
import com.drugoogle.sellscrm.customer.ClientListActivity_;
import com.drugoogle.sellscrm.data.CustomerInfoItem;
import com.drugoogle.sellscrm.data.Model.VisitRecordModel;
import com.drugoogle.sellscrm.data.RecordDetailInfo;
import com.drugoogle.sellscrm.data.request.AddVisitRecordRequest;
import com.drugoogle.sellscrm.data.request.AddressListRequest;
import com.drugoogle.sellscrm.data.request.EditVisitRecordRequest;
import com.drugoogle.sellscrm.data.response.AddressListResponse;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.UploadFileResponse;
import com.drugoogle.sellscrm.data.type.AddressInfo;
import com.drugoogle.sellscrm.data.type.ImageUrl;
import com.drugoogle.sellscrm.data.type.VisitModel;
import com.drugoogle.sellscrm.data.type.VisitType;
import com.drugoogle.sellscrm.selfinfo.Account;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by wgh on 2016/3/29.
 * 添加临时拜访记录
 */
@EActivity(R.layout.activity_add_temp_visit_record)
public class AddTempVisitRecordActivity extends BaseActivity
{
    private static final int REQUEST_CODE_CHOOSE_CLIENT = 1;
    private static final int REQUEST_CODE_CHOOSE_ADDRESS = 2;
    private static final int REQUEST_CODE_CAMERA = 3;
    private static final int REQUEST_CODE_PICK_IMAGE = 4;
    private static final int REQUEST_CODE_DELETE_IMAGE = 5;

    VisitRecordModel mVisitRecordModel;
    Context mContext;
    ArrayList<String> files = new ArrayList<>();
    ArrayList<Integer> deletedFileId = new ArrayList<>();
    private LocationClient mLocationClient;
    private int fileIdToBeDeleted = -1;
    private boolean hasImage = false;
    private Bitmap thumbnail = null;

    @Extra
    Date selectDate;

    @Extra
    RecordDetailInfo recordInfo;

    @Extra
    boolean isEditMode = false;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.toolbar_title)
    TextView title;

    @ViewById(R.id.client_arrow)
    ImageView mClientArrow;
    @ViewById(R.id.address_arrow)
    ImageView mAddressArrow;

    @ViewById(R.id.visit_time_layout)
    View mVisitTimeLayout;
    @ViewById(R.id.visit_time_tv)
    TextView mVisitTimeTv;
    @ViewById(R.id.time_arrow)
    ImageView mTimeArrow;


    @ViewById(R.id.visit_duration_layout)
    View mVisitDurationLayout;
    @ViewById(R.id.visit_duration_tv)
    TextView mVisitDurationTv;
    @ViewById(R.id.duration_arrow)
    ImageView mDurationArrow;


    @ViewById(R.id.visit_type_tv)
    TextView mVisitTypeTv;
    @ViewById(R.id.visit_type_layout)
    View mVisitTypeLayout;
    @ViewById(R.id.type_arrow)
    ImageView mTypeArrow;
    @ViewById(R.id.visit_type_icon_iv)
    ImageView mVisitTypeIconIv;


    @ViewById(R.id.visit_model_layout)
    View mVisitModelLayout;
    @ViewById(R.id.visit_model_tv)
    TextView mVisitModelTv;
    @ViewById(R.id.model_arrow)
    ImageView mModelArrow;
    @ViewById(R.id.visit_model_icon_iv)
    ImageView mVisitModelIconIv;

    @ViewById(R.id.remark_et)
    EditText mRemarkEt;
    @ViewById(R.id.icon_add_remark)
    ImageView mAddRemarkIcon;


    @ViewById(R.id.client_name_tv)
    TextView mClientNameTv;
    @ViewById(R.id.client_address)
    TextView mClientAddress;

    @Click(R.id.select_client_layout)
    void onClickSelectClientLayout()
    {
        if (!isEditMode)
            ClientListActivity_.intent(this).startForResult(REQUEST_CODE_CHOOSE_CLIENT);
    }

    @Click(R.id.select_client_address)
    void onSelectAddress()
    {
        if (!isEditMode)
            AddressManagementActivity_.intent(this).mChooseAddress(true).extra("companyId", mVisitRecordModel.erpCode).startForResult(REQUEST_CODE_CHOOSE_ADDRESS);
    }

    @ViewById(R.id.add_img_result)
    ImageView mAddImgResult;

    @ViewById(R.id.save_btn)
    Button mButtonSave;

    @Click(R.id.save_btn)
    void onSave()
    {
        mVisitRecordModel.remark = mRemarkEt.getText().toString();
        if (!isEditMode) {
            String msg = mVisitRecordModel.checkValidity();
            if (!CommonUtils.IsNullOrEmpty(msg))
            {
                mActivityHelper.showToast(msg);
                return;
            }
        }
        if (!isEditMode)
            getGPS();
        else
            uploadFiles();
    }


    @Override
    protected void onResume ()
    {
        super.onResume();
        if (mVisitRecordModel != null && !CommonUtils.IsNullOrEmpty(mVisitRecordModel.addressId)) {
            getAddressListTwo(mVisitRecordModel.erpCode);
        }
    }

    @Override
    protected void onDestroy()
    {
        releaseCacheThumbnail();
        super.onDestroy();
    }

    void releaseCacheThumbnail()
    {
        if (thumbnail != null && !thumbnail.isRecycled())
        {
            thumbnail.recycle();
            thumbnail = null;
        }
    }
    @Background
    void getAddressListTwo(String companyId) {
        AddressListRequest request = new AddressListRequest();
        request.page = 1;//页码
        request.pageSize = 20;//每页大小
        request.companyId = companyId;//公司id
        AddressListResponse resp = mRestClient.getAddressList(request, Account.getInstance().getToken());
        updateAddressListTwo(resp);
    }


    @UiThread
    void updateAddressListTwo(AddressListResponse resp) {
        if (!BaseResponse.hasErrorWithOperation(resp, this)) {
            boolean hasAddress = false;
            if (resp.dataList == null) return;
            for (AddressInfo item :
                    resp.dataList) {
                if (item.ID.equals(mVisitRecordModel.addressId))
                {
                    hasAddress = true;
                }
            }
            if (!hasAddress)
            {
                mVisitRecordModel.addressId = "";
                mClientAddress.setText("");
            }
        }
    }

    @Background
    protected void uploadFiles()
    {
        mActivityHelper.showLoadingDialog(getString(R.string.submitting_visit_record));
        if (files.size() > 0)
        {
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
            for (int i = 0 ; i < files.size(); i++)
            {
                parts.add("file" + i, new FileSystemResource(compressImg(files.get(i))));
            }
            UploadFileResponse response = mRestClient.uploadVisitRecordFiles(parts, Account.getInstance().getToken());
            if (BaseResponse.hasErrorWithOperation(response, this))
            {
                onUploadFilesFailed();
                return;
            }
            else
            {
                mVisitRecordModel.filePathIds = response.data;
            }
        }

        if (isEditMode)
            editVisitRecord();
        else
            saveVisitRecord();
    }

    @UiThread
    void onUploadFilesFailed() {;
        mActivityHelper.dismissLoadingDialog();
        if (isEditMode)
            mActivityHelper.showToast(getString(R.string.edit_visit_record_fail));
        else
            mActivityHelper.showToast(getString(R.string.save_visit_plan_fail));
    }


    @AfterViews
    void init()
    {
        mContext = this;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });


        mVisitRecordModel = new VisitRecordModel();

        if (isEditMode)
        {
            title.setText(R.string.edit_temp_visit_record);
            mVisitRecordModel.clientId = recordInfo.customerId;
            mVisitRecordModel.clientName = recordInfo.customerName;
            mVisitRecordModel.remark = recordInfo.remark;
            mVisitRecordModel.visitModel = recordInfo.visitWay;
            mVisitRecordModel.visitType = recordInfo.business;


            if (recordInfo.visitStartDate != null)
            {
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    mVisitRecordModel.visitTime = sdf.parse(recordInfo.visitStartDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }

            if (recordInfo.lengthStr != null)
            {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                try {
                    date = sdf.parse(recordInfo.lengthStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                mVisitRecordModel.visitDuration = calendar.get(Calendar.HOUR) * 60 + calendar.get(Calendar.MINUTE);
            }

            mClientArrow.setVisibility(View.INVISIBLE);
            mAddressArrow.setVisibility(View.INVISIBLE);
            mDurationArrow.setVisibility(View.INVISIBLE);
            mTimeArrow.setVisibility(View.INVISIBLE);
            mModelArrow.setVisibility(View.INVISIBLE);
            mTypeArrow.setVisibility(View.INVISIBLE);

            if (recordInfo.fileList != null && recordInfo.fileList.size() > 0) {
                hasImage = true;
                Uri thumbnail = Uri.parse(mRestClient.getRootUrl() + recordInfo.fileList.get(0).filePath);
                String uri = String.format(ImageUrl.IMAGE_URL_FORMATTER, mRestClient.getRootUrl(), recordInfo.fileList.get(0).id, Account.getInstance().getToken());
                mImageUri = Uri.parse(uri);
                mAddImgResult.setImageURI(thumbnail);
            }
            else {
                mAddImgResult.setImageResource(R.drawable.add_img_result);
            }
            if (!TextUtils.isEmpty(recordInfo.address)) {
                mClientAddress.setText(recordInfo.address);
            }
            else {
                mClientAddress.setText(recordInfo.oldAddress);
            }
            mClientNameTv.setText(recordInfo.customerName);
            if (recordInfo.remark != null && recordInfo.remark.length() > 0) {
                mRemarkEt.setText(recordInfo.remark);
                mAddRemarkIcon.setVisibility(View.GONE);
            }
            mButtonSave.setVisibility(View.GONE);
        }
        else {
            mAddImgResult.setImageResource(R.drawable.add_img_result);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectDate);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.setTime(new Date());
            calendar.set(year, month, day);
            mVisitRecordModel.visitTime = calendar.getTime();

            initLocationMgr();
        }

        initTimeSelect();
        initDuration();
        initVisitTypeSelect();
        initVisitModelSelect();
        initRemark();

        mAddImgResult.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (hasImage)
                {
                    ViewImageActivity_.intent(AddTempVisitRecordActivity.this).imageUri(mImageUri).canDelete(true).startForResult(REQUEST_CODE_DELETE_IMAGE);
                    if (isEditMode && recordInfo != null && recordInfo.fileList != null && recordInfo.fileList.size() > 0)
                    {
                        fileIdToBeDeleted =  recordInfo.fileList.get(0).id;
                    }
                }
                else
                    showPopup(v);
            }
        });
    }

    private void initLocationMgr()
    {
        mLocationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);

        option.setPriority(LocationClientOption.GpsFirst);
        option.setAddrType("all");//返回的定位结果包含地址信息
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setScanSpan(300);//设置发起定位请求的间隔时间为5000ms
        option.disableCache(true);//禁止启用缓存定位
        option.setPriority(LocationClientOption.GpsFirst);  //设置GPS优先
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(mDBLocationListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_CLIENT)
        {
            if (data.hasExtra(ClientListActivity.CLIENT_EXTRA_STRING))
            {
                    CustomerInfoItem client = (CustomerInfoItem) data.getSerializableExtra(ClientListActivity.CLIENT_EXTRA_STRING);
                mClientNameTv.setText(client.name);
                mVisitRecordModel.clientId = client.id;
                mVisitRecordModel.erpCode = client.erpCode;
                mVisitRecordModel.addressId = null;
                mVisitRecordModel.logitude = null;
                mVisitRecordModel.latitude = null;
                mClientAddress.setText("");
                findViewById(R.id.address_group).setVisibility(View.VISIBLE);
                getAddressList(mVisitRecordModel.erpCode);// 填充单个地址
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_ADDRESS)
        {
            if (data.hasExtra("address"))
            {
                AddressInfo address = (AddressInfo) data.getSerializableExtra("address");
                mVisitRecordModel.addressId = address.ID;
                String addressStr = "";
                if (!TextUtils.isEmpty(address.ADDRESS))
                {
                    if (TextUtils.isEmpty(address.SUBADDRESS))
                        mClientAddress.setText(address.ADDRESS);
                    else
                        mClientAddress.setText(address.ADDRESS + address.SUBADDRESS);
                } else
                {
                    mClientAddress.setText(address.ADDRESS_OLD);
                }
            }
        }else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAMERA)
        {
            addFile();
        }else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE)
        {
            mImageUri = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(mImageUri,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mImageFilePath = cursor.getString(columnIndex);
            cursor.close();
            addFile();
        }else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_DELETE_IMAGE)
        {
            removeFile();
        }
    }

    void addFile() {
        releaseCacheThumbnail();
        thumbnail = ImageUtils.decodeBitmap(mImageFilePath, ImageDecodeType.SIZE_FIT_IN, 600, 600, Bitmap.Config.ARGB_8888, null, null);
        if (thumbnail != null)
        {
            hasImage = true;
            files.add(mImageFilePath);
            mAddImgResult.setImageBitmap(thumbnail);
        }
        showSaveButton();
    }

    void removeFile() {
        hasImage = false;
        dealWithFileDelete();
        files.clear();
        mAddImgResult.setImageResource(R.drawable.add_img_result);
        showSaveButton();
    }

    void dealWithFileDelete() {
        if (fileIdToBeDeleted != -1) {
            boolean insert = true;
            for (Integer id : deletedFileId) {
                if (id == fileIdToBeDeleted) {
                    insert = false;
                    break;
                }
            }
            if (insert) {
                deletedFileId.add(fileIdToBeDeleted);
            }
            fileIdToBeDeleted = -1;
        }
    }


    /**
     * 初始化拜访开始时间选择
     * */
    void initTimeSelect()
    {
        final SimpleDateFormat sdf = new SimpleDateFormat(Consts.DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mVisitRecordModel.visitTime);
        int weekNum = calendar.get(Calendar.DAY_OF_WEEK);
        String weekStr = CommonUtils.GetWeekStr(weekNum - 1);

        mVisitTimeTv.setText(sdf.format(mVisitRecordModel.visitTime) + " " + weekStr);
        mVisitTimeLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isEditMode)
                    return;

                DateTimePickPopupWindow.PopupWindowBuilder.getInstance(AddTempVisitRecordActivity.this, sdf.format(mVisitRecordModel.visitTime), new DateTimePickPopupWindow.ChangeTimeCallback()
                {
                    @Override
                    public void onChange(Date date)
                    {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        mVisitTimeTv.setText(sdf.format(date) + " " + CommonUtils.GetWeekStr(calendar.get(Calendar.DAY_OF_WEEK) - 1));
                        mVisitRecordModel.visitTime = date;
                    }
                }).setOnDismissListener(new PopupWindow.OnDismissListener()
                {
                    @Override
                    public void onDismiss()
                    {
                        mTimeArrow.setImageResource(R.drawable.blue_arrow_bottom);
                    }
                }).getPopupWindow(sdf.format(mVisitRecordModel.visitTime)).showAsDropDown(mVisitTimeLayout);
                mTimeArrow.setImageResource(R.drawable.yellow_arrow_top);
            }
        });
    }


    /**
     * 初始化拜访时长选择
     * */
    void initDuration()
    {
        mVisitDurationTv.setText(String.format(getString(R.string.duration_format), mVisitRecordModel.visitDuration / 60, mVisitRecordModel.visitDuration % 60));
        mVisitDurationLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isEditMode)
                    return;

                TimePickPopupWindow tppw = new TimePickPopupWindow(AddTempVisitRecordActivity.this, new TimePickPopupWindow.ChangeTimeCallback()
                {
                    @Override
                    public void onChange(int hourOfDay, int minute)
                    {
                        mVisitDurationTv.setText(String.format(getString(R.string.duration_format), hourOfDay, minute));
                        mVisitRecordModel.visitDuration = hourOfDay * 60 + minute;
                    }
                }, mVisitRecordModel.visitDuration);
                tppw.setOnDismissListener(new PopupWindow.OnDismissListener()
                {
                    @Override
                    public void onDismiss()
                    {
                        mDurationArrow.setImageResource(R.drawable.blue_arrow_bottom);
                    }
                });
                tppw.showAsDropDown(mVisitDurationLayout, CommonUtils.getScreenWidth(AddTempVisitRecordActivity.this) / 2, 0);
                mDurationArrow.setImageResource(R.drawable.yellow_arrow_top);
            }
        });
    }


    private ListPopupWindow mTypeListPopupWindow;
    private PopupWindowListAdapter mTypeListPopupWindowAdapter;

    /**
     * 初始化拜访类型选择
     * */
    void initVisitTypeSelect()
    {
        mVisitTypeTv.setText(VisitType.getVisitTypeStrRes(mVisitRecordModel.visitType));
        mVisitTypeIconIv.setImageResource(VisitType.getVisitTypeYellowImgRes(mVisitRecordModel.visitType));
        mTypeListPopupWindow = new ListPopupWindow(mContext);
        mTypeListPopupWindowAdapter = new PopupWindowListAdapter(mContext, VisitType.getStringArray(), mVisitRecordModel.visitType);
        mTypeListPopupWindow.setAdapter(mTypeListPopupWindowAdapter);
        mTypeListPopupWindow.setModal(true);
        mTypeListPopupWindow.setWidth(CommonUtils.getScreenWidth(mContext) / 2);
        mTypeListPopupWindow.setHorizontalOffset(CommonUtils.getScreenWidth(mContext) / 2);
        mTypeListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                position++;
                mVisitRecordModel.visitType = position;
                mVisitTypeTv.setText(VisitType.getVisitTypeStrRes(position));
                mVisitTypeIconIv.setImageResource(VisitType.getVisitTypeYellowImgRes(position));
                mTypeListPopupWindowAdapter.setSelectItemIndex(position);
                mTypeListPopupWindow.dismiss();
            }
        });
        mTypeListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
        {
            @Override
            public void onDismiss()
            {
                mTypeArrow.setImageResource(R.drawable.blue_arrow_bottom);
            }
        });

        mVisitTypeLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isEditMode)
                    return;

                mTypeListPopupWindow.setAnchorView(v);
                mTypeListPopupWindow.show();
                mTypeArrow.setImageResource(R.drawable.yellow_arrow_top);
            }
        });
    }


    private ListPopupWindow mModelListPopupWindow;
    private PopupWindowListAdapter mModelListPopupWindowAdapter;

    /**
     * 初始化拜访方式选择
     * */
    void initVisitModelSelect()
    {
        mVisitModelTv.setText(VisitModel.getVisitModelStrRes(mVisitRecordModel.visitModel));
        mVisitModelIconIv.setImageResource(VisitModel.getVisitModelYellowImgRes(mVisitRecordModel.visitModel));
        mModelListPopupWindow = new ListPopupWindow(mContext);
        mModelListPopupWindowAdapter = new PopupWindowListAdapter(mContext, VisitModel.getStringArray(), mVisitRecordModel.visitModel);
        mModelListPopupWindow.setAdapter(mModelListPopupWindowAdapter);
        mModelListPopupWindow.setModal(true);
        mModelListPopupWindow.setWidth(CommonUtils.getScreenWidth(mContext) / 2);
        mModelListPopupWindow.setHorizontalOffset(CommonUtils.getScreenWidth(mContext) / 2);
        mModelListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                position++;
                mVisitRecordModel.visitModel = position;
                mVisitModelTv.setText(VisitModel.getVisitModelStrRes(position));
                mVisitModelIconIv.setImageResource(VisitModel.getVisitModelYellowImgRes(position));
                mModelListPopupWindowAdapter.setSelectItemIndex(position);
                mModelListPopupWindow.dismiss();
            }
        });
        mModelListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
        {
            @Override
            public void onDismiss()
            {
                mModelArrow.setImageResource(R.drawable.blue_arrow_bottom);
            }
        });

        mVisitModelLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isEditMode)
                    return;

                mModelListPopupWindow.setAnchorView(v);
                mModelListPopupWindow.show();
                mModelArrow.setImageResource(R.drawable.yellow_arrow_top);

            }
        });
    }

    void showSaveButton() {
        mButtonSave.setVisibility(View.VISIBLE);
    }

    /**
     * 初始化备注
     * */
    void initRemark()
    {
        mRemarkEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAddRemarkIcon.setVisibility(View.GONE);
                } else if (((TextView) v).getText().length() == 0) {
                    mAddRemarkIcon.setVisibility(View.VISIBLE);
                }
            }
        });

        mRemarkEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0)
                    mAddRemarkIcon.setVisibility(View.VISIBLE);
                else
                    mAddRemarkIcon.setVisibility(View.GONE);

                showSaveButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void saveVisitRecord()
    {

        AddVisitRecordRequest request = new AddVisitRecordRequest();
        request.visitStartDate = CommonUtils.DateFormatTwo(mVisitRecordModel.visitTime);
        request.length = mVisitRecordModel.visitDuration * 60;
        request.business = mVisitRecordModel.visitType;
        request.customerId = mVisitRecordModel.clientId;
        request.remark = mVisitRecordModel.remark;
        request.visitWay = mVisitRecordModel.visitModel;

        request.addressId = mVisitRecordModel.addressId;
        request.logitude = mVisitRecordModel.logitude;
        request.latitude = mVisitRecordModel.latitude;

        request.filePathIds = mVisitRecordModel.filePathIds;


        BaseResponse resp = mRestClient.addVisitRecord(request, Account.getInstance().getToken());

        onSaveResult(resp);
    }

    @UiThread
    void onSaveResult(BaseResponse resp)
    {
        mActivityHelper.dismissLoadingDialog();
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mActivityHelper.showToast(getString(R.string.save_visit_plan_success));
            setResult(RESULT_OK);
            finish();
        }
    }

    void editVisitRecord()
    {
        EditVisitRecordRequest request = new EditVisitRecordRequest();
        request.id = recordInfo.id;
        request.remark = mVisitRecordModel.remark;
        request.filePathIds = mVisitRecordModel.filePathIds;
        request.deleteFileIds = deletedFileId;

        BaseResponse resp = mRestClient.editVisitRecord(request, Account.getInstance().getToken());

        onEditResult(resp);
    }

    @UiThread
    void onEditResult(BaseResponse resp)
    {
        mActivityHelper.dismissLoadingDialog();
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mActivityHelper.showToast(getString(R.string.edit_visit_plan_success));
            setResult(RESULT_OK);
            finish();
        }
    }


    @Background
    void getAddressList(String companyId) {
        AddressListRequest request = new AddressListRequest();
        request.page = 1;//页码
        request.pageSize = 3;//每页大小
        request.companyId = companyId;//公司id
        AddressListResponse resp = mRestClient.getAddressList(request, Account.getInstance().getToken());
        updateAddressList(resp);
    }

    @UiThread
    void updateAddressList(AddressListResponse resp) {
        if (!BaseResponse.hasErrorWithOperation(resp, this)) {
            if (resp.dataList != null && resp.dataList.size() == 1)
            {
                AddressInfo address = resp.dataList.get(0);
                mVisitRecordModel.addressId = address.ID;
                if (!TextUtils.isEmpty(address.ADDRESS))
                {
                    if (TextUtils.isEmpty(address.SUBADDRESS))
                        mClientAddress.setText(address.ADDRESS);
                    else
                        mClientAddress.setText(address.ADDRESS + address.SUBADDRESS);
                } else
                {
                    mClientAddress.setText(address.ADDRESS_OLD);
                }
            }
        }
    }




















    public void showPopup(View v)
    {
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.menu_capture:
                        pickFromCamera();
                        return true;
                    case R.id.menu_gallery:
                        getImageFromAlbum();
                        return true;
                    default:
                }
                return false;
            }
        });
        inflater.inflate(R.menu.menu_change_image, popup.getMenu());
        popup.show();
    }


    private Uri mImageUri;
    private String mImageFilePath;

    /** 从相机取图 */
    private void pickFromCamera()
    {
        File tempFile = new File(Environment.getExternalStorageDirectory(), "tmp_portrait_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        mImageFilePath = tempFile.getPath();
        mImageUri = Uri.fromFile(tempFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageUri);

        try
        {
            intent.putExtra("return-data", true);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        }
        catch (ActivityNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    protected void getImageFromAlbum()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private int MSG_GPS_TIMEOUT = 1;
    Handler mSaveHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == MSG_GPS_TIMEOUT)
            {
                removeMessages(MSG_GPS_TIMEOUT);
                getGPSFinish(true, null, null);
            }
        }
    };

    public void getGPSFinish(boolean error, String longitude, String latitude)
    {
        mActivityHelper.dismissLoadingDialog();
        mSaveHandler.removeMessages(MSG_GPS_TIMEOUT);

        if (mLocationClient != null && mLocationClient.isStarted())
        {
            mLocationClient.stop();
        }
        if (error)
        {
            Toast.makeText(this, R.string.gps_error, Toast.LENGTH_LONG).show();
        }
        else if (longitude != null && latitude != null)
        {
            mVisitRecordModel.logitude = longitude;
            mVisitRecordModel.latitude = latitude;

            uploadFiles();
        }
    }
    public void getGPS()
    {
        mActivityHelper.showLoadingDialog(getString(R.string.getting_location), new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                getGPSFinish(false, null, null);
            }
        });
        if (!mLocationClient.isStarted())
        {
            mLocationClient.start();
        }
        mSaveHandler.sendEmptyMessageDelayed(MSG_GPS_TIMEOUT, 5000);
    }

    BDLocationListener mDBLocationListener = new BDLocationListener()
    {
        @Override
        public void onReceiveLocation(BDLocation bdLocation)
        {
            getGPSFinish(false, bdLocation.getLongitude() + "", bdLocation.getLatitude() + "");
        }
    };

    String compressImg (String originalFile)
    {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(originalFile, opt);

        final int minSize = 2000;
        if (opt.outWidth <= minSize && opt.outHeight <= minSize)
        {
            return originalFile;
        }
        int width = opt.outWidth;
        int height = opt.outHeight;

        do {
            width /= 2;
            height /= 2;
        }
        while (width > minSize || height > minSize);



        File tempFile = new File(getApplicationContext().getCacheDir(), "event_upload_temp.png");
        if (tempFile.exists())
            tempFile.delete();

        FileOutputStream out = null;
        Bitmap bmpSrc = null;
        try
        {
            out = new FileOutputStream(tempFile);
            bmpSrc = ImageUtils.decodeBitmap(originalFile, ImageDecodeType.SIZE_FIT_IN, width, height, Bitmap.Config.ARGB_8888, null, null);
            bmpSrc.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        catch (Exception e)
        {
            return null;
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (Exception e)
            {

            }
            if (bmpSrc != null)
            {
                bmpSrc.recycle();
            }
        }
        return  tempFile.getAbsolutePath();
    }

//    /** 从相册取图 */
//    private void pickFromGallery()
//    {
//        Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
//        startActivityForResult(i, REQUEST_CODE_GALLERY);
//
//    }
}
