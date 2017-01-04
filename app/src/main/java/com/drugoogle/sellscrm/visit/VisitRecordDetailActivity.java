package com.drugoogle.sellscrm.visit;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CacheUtils;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.Utils.ImageDecodeType;
import com.drugoogle.sellscrm.Utils.ImageUtils;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.customer.CustomerDetailsActivity_;
import com.drugoogle.sellscrm.data.RecordDetailInfo;
import com.drugoogle.sellscrm.data.request.EditVisitRecordRequest;
import com.drugoogle.sellscrm.data.request.EndVisitRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.RecordDetailResponse;
import com.drugoogle.sellscrm.data.response.UploadFileResponse;
import com.drugoogle.sellscrm.data.type.ImageUrl;
import com.drugoogle.sellscrm.data.type.PlanStatus;
import com.drugoogle.sellscrm.data.type.VisitModel;
import com.drugoogle.sellscrm.data.type.VisitType;
import com.drugoogle.sellscrm.selfinfo.Account;
import com.facebook.drawee.view.SimpleDraweeView;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wgh on 2016/4/26.
 */
@EActivity(R.layout.activity_visit_record_detail)
public class VisitRecordDetailActivity extends BaseActivity
{
    private static final int REQUEST_CODE_EDIT = 1;
    private static final int REQUEST_CODE_DELETE_IMAGE = 2;
    private static final int REQUEST_CODE_CAMERA = 3;
    private static final int REQUEST_CODE_PICK_IMAGE = 4;
    RecordDetailInfo mInfo;
    Uri mImageUri;
    private String mImageFilePath;

    ArrayList<String> files = new ArrayList<>();
    List<Integer> addedFileId = new ArrayList<>();
    Set<Integer> deletedFileId = new HashSet<>();
    private boolean hasImage = false;
    private Bitmap thumbnail = null;
    private boolean finishAfterEnd = false;
    private boolean changed = false;

    @Extra
    int mRecordId;


    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    //    @ViewById(R.id.user_admin)
//    TextView mUserAdminTv;
    @ViewById(R.id.plan_status_tv)
    TextView mPlanStatusTv;
    @ViewById(R.id.plan_status_icon)
    ImageView mPlanStatusIcon;
    @ViewById(R.id.customer_name_tv)
    TextView mCustomerNameTv;

    @ViewById(R.id.visit_time_tv)
    TextView mVisitTimeTv;
    @ViewById(R.id.visit_duration_tv)
    TextView mVisitDurationTv;

    @ViewById(R.id.visit_model_icon_iv)
    ImageView mVisitModelIconIv;
    @ViewById(R.id.visit_model_tv)
    TextView mVisitModelTv;
    @ViewById(R.id.visit_type_icon_iv)
    ImageView mVisitTypeIconIv;
    @ViewById(R.id.visit_type_tv)
    TextView mVisitTypeTv;
    @ViewById(R.id.plan_remark_tv)
    TextView mRemarkTv;

    @ViewById(R.id.visit_location_tv)
    TextView mVisitLocationTv;
    @ViewById(R.id.visit_location_layout)
    View mVisitLocationLayout;


    @ViewById(R.id.remark_et)
    EditText mRemarkEt;
    @ViewById(R.id.icon_add_remark)
    ImageView mAddRemarkIcon;

    @ViewById(R.id.operation_btn)
    Button mOperationBtn;

    @ViewById(R.id.img_result)
    SimpleDraweeView mImageView;
    @ViewById(R.id.layout_result)
    View mLayoutResult;

    @Click(R.id.customer_name)
    void onClickCustomerName()
    {
        if (mInfo != null)
        {
            CustomerDetailsActivity_.intent(this).extra("id", mInfo.customerId).start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BaseResponse.REQUEST_CODE_EXPIRED && resultCode == BaseResponse.RESULT_CODE_EXPIRED)
        {
            getRecordDetail(mRecordId);
        }
        else if (requestCode == REQUEST_CODE_EDIT && RESULT_OK == resultCode)
        {
            getRecordDetail(mRecordId);
        }
        else if (requestCode == REQUEST_CODE_DELETE_IMAGE && RESULT_OK == resultCode)
        {
            removeFile();
        }
        else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CAMERA)
        {
            addFile();
        }
        else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE)
        {
            mImageUri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(mImageUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mImageFilePath = cursor.getString(columnIndex);
            cursor.close();
            addFile();
        }
    }

    void releaseCacheThumbnail()
    {
        if (thumbnail != null && !thumbnail.isRecycled())
        {
            thumbnail.recycle();
            thumbnail = null;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        releaseCacheThumbnail();
        unbindService(sConnection);
        unregisterReceiver(mReceiver);
    }

    @AfterViews
    protected void init()
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //toolbar.setOnMenuItemClickListener(onMenuItemClickListener);
        toolbar.setNavigationIcon(R.drawable.back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        Intent bindIntent = new Intent(this, VisitService.class);
        bindService(bindIntent, sConnection, BIND_AUTO_CREATE);

        findViewById(R.id.item_group).setVisibility(View.GONE);
        mRecordId = getIntent().getIntExtra("mRecordId", -1);
        getRecordDetail(mRecordId);
    }


    /***/
    @Background
    void getRecordDetail(int id)
    {
        mActivityHelper.showLoadingDialog(getString(R.string.loading_record_detail));

        RecordDetailResponse resp = mRestClient.getRecordDetail(id, Account.getInstance().getToken());

        mActivityHelper.dismissLoadingDialog();
        onGetRecordDetail(resp);
    }

    @UiThread
    void onGetRecordDetail(RecordDetailResponse resp)
    {
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mInfo = resp.visitRecord;
            initData(resp.visitRecord);
            findViewById(R.id.item_group).setVisibility(View.VISIBLE);
        }
    }


    private VisitService.ServiceBinder mBinder;
    private ServiceConnection sConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mBinder = (VisitService.ServiceBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {

        }
    };


    /**
     * 显示详情
     */
    void initData(final RecordDetailInfo info)
    {
//        if (info.status != PlanStatus.FINISHED) {
//            toolbar.getMenu().getItem(0).setVisible(false);
//        }

//        mUserAdminTv.setText(AdminPlan.getAdminPlanStrRes(info.is_admin));
        mPlanStatusTv.setText(PlanStatus.getPlanStatusStrRes(info.status));
        mPlanStatusTv.setTextColor(getResources().getColor(PlanStatus.getPlanStatusColorRes(info.status)));
        mPlanStatusIcon.setImageResource(PlanStatus.getPlanStatusImgRes(info.status));

        mCustomerNameTv.setText(info.customerName);
        mVisitTimeTv.setText(info.visitStartDate);
        if (info.lengthStr != null)
        {
            mVisitDurationTv.setText(info.lengthStr);
        }
        else
        {
            mVisitDurationTv.setText("进行中");
        }

        mVisitModelTv.setText(VisitModel.getVisitModelStrRes(info.visitWay));
        mVisitModelIconIv.setImageResource(VisitModel.getVisitModelYellowImgRes(info.visitWay));

        mVisitTypeTv.setText(VisitType.getVisitTypeStrRes(info.business));
        mVisitTypeIconIv.setImageResource(VisitType.getVisitTypeYellowImgRes(info.business));

        mVisitLocationLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (info.latitude == null && info.longitude == null)
                {
                    mActivityHelper.showNormalToast("没有经纬度信息");
                    return;
                }
                ShowLocationActivity_.intent(VisitRecordDetailActivity.this).latitude(Double.parseDouble(info.latitude)).longitude(Double.parseDouble(info.longitude)).start();
            }
        });

        String addressStr = "";
        if (CommonUtils.IsNullOrEmpty(info.address))
        {
            addressStr = info.oldAddress;
        }
        else
        {
            addressStr = info.address;
        }
        mVisitLocationTv.setText(addressStr);

        if (info.planRemark != null)
        {
            mRemarkTv.setText(info.planRemark);
        }


        if (info.status == PlanStatus.UNDERWAY)
        {
            mOperationBtn.setVisibility(View.VISIBLE);
        }
        else if (info.status == PlanStatus.FINISHED)
        {
            mOperationBtn.setVisibility(View.INVISIBLE);
            mOperationBtn.setText(R.string.submit);
        }

        mOperationBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                saveRecord();
            }
        });

        if (info.fileList != null && info.fileList.size() > 0)
        {
            hasImage = true;
            Uri thumbnail = Uri.parse(mRestClient.getRootUrl() + info.fileList.get(0).filePath);
            String uri = String.format(ImageUrl.IMAGE_URL_FORMATTER, mRestClient.getRootUrl(), info.fileList.get(0).id, Account.getInstance().getToken());
            mImageUri = Uri.parse(uri);
            mImageView.setImageURI(thumbnail);
        }
        else
        {
            mImageView.setImageResource(R.drawable.add_img_result);
        }
        mImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (hasImage)
                {
                    if (mInfo.status == PlanStatus.FINISHED)
                    {
                        ViewImageActivity_.intent(VisitRecordDetailActivity.this).imageUri(mImageUri).canDelete(true).startForResult(REQUEST_CODE_DELETE_IMAGE);
                    }
                    else
                    {
                        ViewImageActivity_.intent(VisitRecordDetailActivity.this).imageUri(mImageUri).start();
                    }
                }
                else
                {
                    showPopup(v);
                }
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(VisitService.ACTION_VISIT_ENDED);
        registerReceiver(mReceiver, intentFilter);

        initRemark();
        loadSavedData();
    }

    void initRemark()
    {
        if (mInfo.remark != null && mInfo.remark.length() > 0)
        {
            mRemarkEt.setText(mInfo.remark);
            mAddRemarkIcon.setVisibility(View.GONE);
        }

        mRemarkEt.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    mAddRemarkIcon.setVisibility(View.GONE);
                }
                else if (((TextView) v).getText().length() == 0)
                {
                    mAddRemarkIcon.setVisibility(View.VISIBLE);
                }
            }
        });

        mRemarkEt.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                changed = true;
                if (s.length() == 0)
                {
                    mAddRemarkIcon.setVisibility(View.VISIBLE);
                }
                else
                {
                    mAddRemarkIcon.setVisibility(View.GONE);
                }

                showSaveButton();

                if (!autoSaveHandler.hasMessages(0))
                {
                    autoSaveHandler.sendEmptyMessageDelayed(0, 2000);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
    }
    void loadSavedData()
    {
        CacheUtils.VisitAttachment attachment = CacheUtils.GetVisitSavedAttachment(mInfo.id);
        if (attachment != null)
        {
            if (attachment.file != null)
            {
                mImageFilePath = attachment.file;
                addFile();
            }
            mRemarkEt.setText(attachment.text);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (changed)
        {
            new AlertDialog.Builder(this).setMessage("是否要放弃您的修改?").setPositiveButton("放弃修改", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    CacheUtils.DeleteVisitSavedAttachment(mInfo.id);
                    VisitRecordDetailActivity.super.onBackPressed();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            }).create().show();
        }
        else
        {
            super.onBackPressed();
        }
    }

    void editVisitRecord()
    {
        mActivityHelper.showLoadingDialog(getString(R.string.submitting_visit_record));
        EditVisitRecordRequest request = new EditVisitRecordRequest();
        request.id = mInfo.id;
        request.remark = mRemarkEt.getText().toString();
        request.filePathIds = addedFileId;
        request.deleteFileIds = new ArrayList<>(deletedFileId);

        BaseResponse resp = mRestClient.editVisitRecord(request, Account.getInstance().getToken());

        onEditResult(resp);
    }

    @UiThread
    void endAutoVisit()
    {
        mActivityHelper.showLoadingDialog(getString(R.string.submitting_visit_record));
        EndVisitRequest request = new EndVisitRequest();
        request.id = mInfo.id;
        //Service 会自己去读取文件,此处不需要传
//        request.filePathIds = addedFileId;
//        request.remark = mRemarkEt.getText().toString();

        CacheUtils.setVisitText(mInfo.id, mRemarkEt.getText().toString());
        if (mBinder != null)
        {
            finishAfterEnd = true;
            mBinder.endVisit(request);
        }
        else
        {
            mActivityHelper.dismissLoadingDialog();
            mActivityHelper.showToast("结束拜访出错,请尝试重启程序并重试");
        }
    }
    @UiThread
    void onEditResult(BaseResponse resp)
    {
        if (!BaseResponse.hasErrorWithOperation(resp, this))
        {
            mActivityHelper.showToast(getString(R.string.edit_visit_plan_success));
            CacheUtils.DeleteVisitSavedAttachment(mInfo.id);
            setResult(RESULT_OK);
            finish();
        }
        else
        {
            onEditFailed();
        }
    }

    void onEditFailed()
    {
        mActivityHelper.showToast(getString(R.string.edit_visit_record_fail));
    }


    void addFile()
    {
        changed = true;
        releaseCacheThumbnail();
        thumbnail = ImageUtils.decodeBitmap(mImageFilePath, ImageDecodeType.SIZE_FIT_IN, 600, 600, Bitmap.Config.ARGB_8888, null, null);
        if (thumbnail != null)
        {
            hasImage = true;
            files.add(mImageFilePath);
            mImageView.setImageBitmap(thumbnail);
            CacheUtils.setVisitImage(mInfo.id, mImageFilePath);
        }
        showSaveButton();
    }

    void removeFile()
    {
        changed = true;
        hasImage = false;
        if ( mInfo.fileList.size() > 0)
        {
            deletedFileId.add(mInfo.fileList.get(0).id);
        }
        addedFileId.clear();
        files.clear();
        mImageView.setImageResource(R.drawable.add_img_result);
        showSaveButton();
        CacheUtils.setVisitImage(mInfo.id, null);
    }

    void showSaveButton()
    {
        mOperationBtn.setVisibility(View.VISIBLE);
    }



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

    @Background
    void saveRecord()
    {
        mActivityHelper.showLoadingDialog(getString(R.string.submitting_visit_record));
//        if (!uploadFiles())
//        {
//            mActivityHelper.dismissLoadingDialog();
//            mActivityHelper.showToast(getString(R.string.upload_image_failed));
//        }
//        else
        if (mInfo.status == PlanStatus.UNDERWAY)
        {
            endAutoVisit();
        }
        else
        {
            if (!uploadFiles())
            {
                mActivityHelper.dismissLoadingDialog();
                mActivityHelper.showToast(getString(R.string.upload_image_failed));
            }
            editVisitRecord();
        }
    }

    protected boolean uploadFiles()
    {
        if (files.size() > 0)
        {
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
            for (int i = 0; i < files.size(); i++)
            {
                parts.add("file" + i, new FileSystemResource(ImageUtils.compressImg(getApplicationContext(), files.get(i))));
            }
            UploadFileResponse response = mRestClient.uploadVisitRecordFiles(parts, Account.getInstance().getToken());
            if (BaseResponse.hasErrorWithOperation(response, this))
            {
                return false;
            }
            else
            {
                files.clear();
                addedFileId = response.data;
            }
        }

        return true;
    }

    public void showPopup(View v)
    {
        PopupMenu popup = new PopupMenu(this, v);
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(VisitService.ACTION_VISIT_ENDED))
            {
                int id = intent.getIntExtra(VisitService.EXTRA_VISIT_ID, 0);
                if (id != mInfo.id)
                {
                    return;
                }
                mActivityHelper.dismissLoadingDialog();
                boolean success = intent.getBooleanExtra(VisitService.EXTRA_RESULT, false);
                if (success)
                {
                    if (finishAfterEnd)
                    {
                        finish();
                    }
                    else
                    {
                        Toast.makeText(context, "拜访已结束", Toast.LENGTH_SHORT).show();
                        getRecordDetail(mRecordId);
                    }
                }
            }
        }
    };

    private void ChangeStatusToFinish()
    {

    }
    private Handler autoSaveHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            removeMessages(msg.what);
            String text = mRemarkEt.getText().toString();
            CacheUtils.setVisitText(mInfo.id, text);
        }
    };
}
