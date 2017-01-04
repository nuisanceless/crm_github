package com.drugoogle.sellscrm.customer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drugoogle.sellscrm.R;
import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.common.BaseActivity;
import com.drugoogle.sellscrm.data.ContactChildrenInfo;
import com.drugoogle.sellscrm.data.ContactParentInfo;
import com.drugoogle.sellscrm.data.CustomerInfo;
import com.drugoogle.sellscrm.data.request.CustomerDetailsRequest;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.CustomerDetailsResponse;
import com.drugoogle.sellscrm.data.response.RemainingCreditResponse;
import com.drugoogle.sellscrm.data.type.CustomerLevel;
import com.drugoogle.sellscrm.selfinfo.Account;
import com.drugoogle.sellscrm.visit.AddVisitPlanActivity_;
import com.drugoogle.sellscrm.visit.VisitSearchActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ydwang on 2016/4/27.
 */
@EActivity(R.layout.activity_customer_details)
public class CustomerDetailsActivity extends BaseActivity implements View.OnClickListener {

    public static final int RESULT_LEVEL_CHANGED = 223;
    public Context context;
    public Account account;
    public String userId;
    public String token;//登录返回的token
    CustomerDetailsRequest request;
    public CustomerDetailsResponse Resp;
    public List<ContactParentInfo> contactParentInfos = new ArrayList<>();
    public LayoutInflater inflater;
    public ContactListAdapter adapter;
    private ProgressDialog pdLoading;
    private PopupWindow mPopupWindow;
    public int popSelect;//下拉菜单选中
    public int levelCondition;//编辑级别参数

    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById(R.id.title)
    TextView title;
    @ViewById(R.id.content_lay)
    View contentView;
    @ViewById(R.id.customer_id_tv)
    TextView customerIdTv;
    @ViewById(R.id.customer_level_lay)
    View customerLevelLay;
    @ViewById(R.id.customer_level_im)
    ImageView customerLevelIm;
    @ViewById(R.id.customer_level_arrow_im)
    ImageView customerLevelArrowIm;
    @ViewById(R.id.customer_level_tv)
    TextView customerLevelTv;
    @ViewById(R.id.customer_legal_tv)
    TextView customerLegalTv;
    @ViewById(R.id.contact_people_lay)
    View contactPeopleLay;
    @ViewById(R.id.no_contact_lay)
    View noContactLay;
    @ViewById(R.id.expand_lv)
    ContactExpandListView expandLv;
    @ViewById(R.id.left_amount_text)
    TextView leftAmountText;
    @ViewById(R.id.left_amount_tv)
    TextView leftAmountTv;
    @ViewById(R.id.credit_record)
    View creditRecord;
    @ViewById(R.id.record_detail)
    ImageView recordDetailIm;

    @Click(R.id.sale_record_lay)
    void saleRecordList() {
        SaleRecordListActivity_.intent( this )._CustomerInfo( Resp.ccCustomer ).start();
    }

    @Click(R.id.customer_legal_lay)
    void customerLegal() {
        CustomerInfo customerInfo = Resp.ccCustomer;
        LegalPersonActivity_.intent( this ).extra( "ccCustomer", customerInfo ).start();
    }

    @Click(R.id.address)
    void customerAddresses() {

        CustomerInfo customerInfo = Resp.ccCustomer;
        AddressManagementActivity_.intent( this )
                .mCustomerInfo( customerInfo ).mChooseAddress( false ).start();
    }

    @Click(R.id.visit_plan)
    void visitPlan() {
        //添加拜访计划
        CustomerInfo customerInfo = Resp.ccCustomer;
        VisitSearchActivity_.intent( this ).isFromCustomer( true )
                .mCustomerInfo( customerInfo ).start();
    }

    @Click(R.id.add_visit_plan)
    void addVisitPlan() {
        //添加拜访计划
        CustomerInfo customerInfo = Resp.ccCustomer;
        AddVisitPlanActivity_.intent( this ).isFromCustomer( true )
                .mCustomerInfo( customerInfo ).start();
    }

    @AfterViews
    void init() {
        context = this;
        account = Account.getInstance();
        inflater = getLayoutInflater();
        adapter = new ContactListAdapter();

        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );
        toolbar.setNavigationIcon( R.drawable.back_white );
        toolbar.setNavigationOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );
        customerLevelLay.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerLevelArrowIm.setImageResource( R.drawable.yellow_arrow_top );
                showPopLevelWindow();
            }
        } );
        //初始化客户详情页面
        request = new CustomerDetailsRequest();
        request.userId = getIntent().getStringExtra( "id" );
        request.token = account.getToken();
        showProgressDialog();
        initView( request );
        expandLv.setAdapter( adapter );

    }

    void initPopSelect() {
        switch (Resp.ccCustomer.GRADE) {
            case CustomerLevel.CUSTOMER_LEVEL_HIGH:
                popSelect = 0;
                break;
            case CustomerLevel.CUSTOMER_LEVEL_MIDDLE:
                popSelect = 1;
                break;
            case CustomerLevel.CUSTOMER_LEVEL_LOW:
                popSelect = 2;
                break;
            default:
                popSelect = 0;
                break;
        }
    }

    @Background
    void initView(CustomerDetailsRequest request) {

        userId = request.userId;
        token = request.token;
        Resp = mRestClient.getCustomerDetailsInfo( userId, token );
        afterInitView( Resp );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == BaseResponse.REQUEST_CODE_EXPIRED
                && resultCode == BaseResponse.RESULT_CODE_EXPIRED) {
            request.token = Account.getInstance().getToken();
            showProgressDialog();
            initView( request );
        }
    }

    @UiThread
    void afterInitView(CustomerDetailsResponse Resp) {
//        dismissProgressDialog();
        if (!BaseResponse.hasErrorWithOperation( Resp, this )) {
            //数据请求成功
            if (Resp.code == BaseResponse.SUCCESS_CODE) {
                initPopSelect();
                if (Resp.ccCustomer == null) {
                    Toast.makeText( this, R.string.no_customer, Toast.LENGTH_SHORT ).show();
                } else {
                    title.setText( Resp.ccCustomer.NAME );
                    //客户号
                    customerIdTv.setText( String.valueOf( Resp.ccCustomer.ERP_CODE ) );
                    customerLevelIm.setImageResource( CustomerLevel
                            .getCustomerImRes( Resp.ccCustomer.GRADE ) );
                    customerLevelTv.setText( CustomerLevel.getCustomerLevel( Resp.ccCustomer.GRADE ) );
                }

                if (Resp.linkmanList.size() != 0) {
                    for (int i = 0; i < Resp.linkmanList.size(); i++) {
                        ContactParentInfo contactParentInfo = new ContactParentInfo();
                        List<ContactChildrenInfo> contactChildrenInfos = new ArrayList<>();
                        contactParentInfo.name = Resp.linkmanList.get( i ).NAME;
                        contactParentInfo.tittle = Resp.linkmanList.get( i ).POSITION;
                        ContactChildrenInfo childrenInfo = new ContactChildrenInfo();
                        childrenInfo.phone = Resp.linkmanList.get( i ).PHONE;
                        childrenInfo.email = Resp.linkmanList.get( i ).EMAIL;
                        contactChildrenInfos.add( childrenInfo );
                        contactParentInfo.contactChildrenInfos = contactChildrenInfos;
                        contactParentInfos.add( contactParentInfo );
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    //没有联系人时
                    contactPeopleLay.setVisibility( View.GONE );
                    noContactLay.setVisibility( View.VISIBLE );
                }
            }
        }
        getRemainingCredit();
    }

    @Background
    void getRemainingCredit() {
        userId = request.userId;
        token = request.token;
        RemainingCreditResponse creditResponse = mRestClient.getRemainingCredits( userId, token );
        afterGetRemainingCredit( creditResponse );
    }

    @UiThread
    void afterGetRemainingCredit(RemainingCreditResponse creditResponse) {
        dismissProgressDialog();
        if (!BaseResponse.hasErrorWithOperation( creditResponse, this )) {
            //数据请求成功
            if (creditResponse.code == BaseResponse.SUCCESS_CODE) {
                if (creditResponse.data != null) {
                    leftAmountTv.setText( String.valueOf( creditResponse.data ) + "元" );
                    creditRecord.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CreditRecordListActivity_.intent( context )._CustomerInfo( Resp.ccCustomer ).start();
                        }
                    } );
                }else {
                    leftAmountText.setVisibility( View.INVISIBLE  );
                    leftAmountTv.setText( "未授信" );
                    recordDetailIm.setVisibility( View.INVISIBLE );
                }
            }
        }
    }

    class ContactListAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            //获取当前父item个数
            return contactParentInfos.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            //获取当前父item下的子item的个数
            int size = contactParentInfos.get( groupPosition ).contactChildrenInfos.size();
            return size;
        }

        @Override
        public Object getGroup(int groupPosition) {
            //获取当前父item的数据
            return contactParentInfos.get( groupPosition );
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            //得到子item需要关联的数据
            return (contactParentInfos.get( groupPosition ).contactChildrenInfos.get( childPosition ));
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            //得到子item的ID
            return childPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ParentHolder parenrHolder;
            if (convertView == null) {
                convertView = inflater.inflate( R.layout.list_item_contact_parent, null );
                parenrHolder = new ParentHolder();
                parenrHolder.portraitIm = (ImageView) convertView.findViewById( R.id.portrait );
                parenrHolder.nameTv = (TextView) convertView.findViewById( R.id.name );
                parenrHolder.tittleTv = (TextView) convertView.findViewById( R.id.tittle );
                parenrHolder.expandIm = (ImageView) convertView.findViewById( R.id.expand_iv );
                convertView.setTag( parenrHolder );
            } else {
                parenrHolder = (ParentHolder) convertView.getTag();
            }
            //联系人姓名
            String name = contactParentInfos.get( groupPosition ).name;
            parenrHolder.nameTv.setText( name );
            //联系人职位
            String title = contactParentInfos.get( groupPosition ).tittle;
            if (TextUtils.isEmpty( title )) {
                parenrHolder.tittleTv.setVisibility( View.GONE );
            } else {
                parenrHolder.tittleTv.setVisibility( View.VISIBLE );
                parenrHolder.tittleTv.setText( title );
            }
            if (isExpanded) {
                parenrHolder.expandIm.setImageResource( R.drawable.yellow_arrow_up );
            } else {
                parenrHolder.expandIm.setImageResource( R.drawable.blue_arrow_bottom );
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final ChildrenHolder childrenHolder;
            if (convertView == null) {
                convertView = inflater.inflate( R.layout.list_item_contact_children, null );
                childrenHolder = new ChildrenHolder();
                childrenHolder.phoneTv = (TextView) convertView.findViewById( R.id.phone );
                childrenHolder.emailTv = (TextView) convertView.findViewById( R.id.email );
                childrenHolder.phoneIm = (ImageView) convertView.findViewById( R.id.phoneIm );
                childrenHolder.emailIm = (ImageView) convertView.findViewById( R.id.emailIm );
                childrenHolder.phoneLay = convertView.findViewById( R.id.phone_lay );
                childrenHolder.emailLay = convertView.findViewById( R.id.emailIm );
                convertView.setTag( childrenHolder );
            } else {
                childrenHolder = (ChildrenHolder) convertView.getTag();
            }
            //联系人电话信息
            final String phone = contactParentInfos.get( groupPosition )
                    .contactChildrenInfos.get( childPosition ).phone;
            if (TextUtils.isEmpty( phone )) {
                childrenHolder.phoneTv.setText( getString( R.string.no_phone ) );
            } else {
                childrenHolder.phoneTv.setText( phone );
            }
            //联系人邮件信息
            final String emailTv = contactParentInfos.get( groupPosition )
                    .contactChildrenInfos.get( childPosition ).email;
            if (TextUtils.isEmpty( emailTv )) {
                childrenHolder.emailTv.setText( getString( R.string.no_email ) );
            } else {
                childrenHolder.emailTv.setText( emailTv );
            }
            childrenHolder.phoneLay.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //打电话或保存到通讯录
                    telDoWhat( childrenHolder.phoneTv.getText().toString() );
                }
            } );
            childrenHolder.emailLay.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //发邮件
                    if (!TextUtils.isEmpty( emailTv )) {
                        sendMail( emailTv );
                    }
                }
            } );
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    class ParentHolder {

        public ImageView portraitIm;
        public TextView nameTv;
        public TextView tittleTv;
        public ImageView expandIm;

    }

    class ChildrenHolder {
        public TextView phoneTv;
        public TextView emailTv;
        public ImageView phoneIm;
        public ImageView emailIm;
        public View phoneLay;
        public View emailLay;
    }

    /**
     * 显示状态菜单栏
     */
    void showPopLevelWindow() {
        //设置contentView
        View contentView = LayoutInflater.from( context ).inflate( R.layout.customer_pop_level_edit, null );
        mPopupWindow = new PopupWindow( contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true );
        mPopupWindow.setContentView( contentView );

        //设置各个控件的点击响应
        RelativeLayout RelativeLayout01 = (RelativeLayout) contentView.findViewById( R.id.pop_level_high_lay );
        RelativeLayout RelativeLayout02 = (RelativeLayout) contentView.findViewById( R.id.pop_level_middle_lay );
        RelativeLayout RelativeLayout03 = (RelativeLayout) contentView.findViewById( R.id.pop_level_low_lay );
        RelativeLayout01.setOnClickListener( this );
        RelativeLayout02.setOnClickListener( this );
        RelativeLayout03.setOnClickListener( this );

        //实例化圆点imgeview
        ImageView imageView01 = (ImageView) contentView.findViewById( R.id.pop_level_high_im );
        ImageView imageView02 = (ImageView) contentView.findViewById( R.id.pop_level_middle_im );
        ImageView imageView03 = (ImageView) contentView.findViewById( R.id.pop_level_low_im );

        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add( imageView01 );
        imageViews.add( imageView02 );
        imageViews.add( imageView03 );
        List<RelativeLayout> layouts = new ArrayList<>();
        layouts.add( RelativeLayout01 );
        layouts.add( RelativeLayout02 );
        layouts.add( RelativeLayout03 );

        for (int i = 0; i < imageViews.size(); i++) {
            imageViews.get( i ).setVisibility( View.GONE );
            layouts.get( i ).setBackgroundColor( 0 );
        }
        imageViews.get( popSelect ).setVisibility( View.VISIBLE );
        layouts.get( popSelect ).setBackgroundColor( getResources().getColor( R.color.light_blue ) );

        //设置点击空白消失
        mPopupWindow.setBackgroundDrawable( new BitmapDrawable() );
        mPopupWindow.setOutsideTouchable( true );
        //显示PopupWindow
        mPopupWindow.setWidth( CommonUtils.getScreenWidth( context ) / 2 );
        mPopupWindow.showAsDropDown( customerLevelLay, (CommonUtils.getScreenWidth( context ) / 2), 0 );
        //设置PopupWindow消失时的监听
        mPopupWindow.setOnDismissListener( new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                customerLevelArrowIm.setImageResource( R.drawable.blue_arrow_bottom );
            }
        } );
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.pop_level_high_lay: {
                popSelect = 0;
                levelCondition = CustomerLevel.CUSTOMER_LEVEL_HIGH;
                customerLevelIm.setImageResource(
                        CustomerLevel.getCustomerImRes( CustomerLevel.CUSTOMER_LEVEL_HIGH ) );
                customerLevelTv.setText(
                        CustomerLevel.getCustomerLevel( CustomerLevel.CUSTOMER_LEVEL_HIGH ) );
                mPopupWindow.dismiss();
            }
            break;
            case R.id.pop_level_middle_lay: {
                popSelect = 1;
                levelCondition = CustomerLevel.CUSTOMER_LEVEL_MIDDLE;
                customerLevelIm.setImageResource(
                        CustomerLevel.getCustomerImRes( CustomerLevel.CUSTOMER_LEVEL_MIDDLE ) );
                customerLevelTv.setText(
                        CustomerLevel.getCustomerLevel( CustomerLevel.CUSTOMER_LEVEL_MIDDLE ) );
                mPopupWindow.dismiss();
            }
            break;
            case R.id.pop_level_low_lay: {
                popSelect = 2;
                levelCondition = CustomerLevel.CUSTOMER_LEVEL_LOW;
                customerLevelIm.setImageResource(
                        CustomerLevel.getCustomerImRes( CustomerLevel.CUSTOMER_LEVEL_LOW ) );
                customerLevelTv.setText(
                        CustomerLevel.getCustomerLevel( CustomerLevel.CUSTOMER_LEVEL_LOW ) );
                mPopupWindow.dismiss();
            }
            default:
                break;
        }
        editCustomerLevel();
    }

    @Background
    void editCustomerLevel() {
        CustomerInfo customerInfo = Resp.ccCustomer;
        BaseResponse editLevelResp = mRestClient.editCustomerLevel( customerInfo.ID, levelCondition
                , account.getToken() );
        afterEditLevel( editLevelResp );

    }

    @UiThread
    void afterEditLevel(BaseResponse editLevelResp) {
        dismissProgressDialog();
        if (!BaseResponse.hasErrorWithOperation( editLevelResp, this )) {
            if (editLevelResp.code == BaseResponse.SUCCESS_CODE) {
                Toast.makeText( this, R.string.level_changed, Toast.LENGTH_SHORT ).show();
                setResult( RESULT_LEVEL_CHANGED );
            }
        }
    }

    /**
     * 选择电话处理方式
     *
     * @param phoneNumber
     */
    void telDoWhat(final String phoneNumber) {
        final View contentView = inflater.inflate( R.layout.tel_do_what, null );
        final PopupWindow popupWindow = new PopupWindow( contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true );
        popupWindow.setContentView( contentView );

        TextView callTel = (TextView) contentView.findViewById( R.id.call_tel );
        TextView saveTel = (TextView) contentView.findViewById( R.id.save_tel );

        //设置点击空白消失
        popupWindow.setBackgroundDrawable( new BitmapDrawable() );
        popupWindow.setOutsideTouchable( true );
        //显示PopupWindow
        View rootview = inflater.inflate( R.layout.activity_customer_details, null );
        popupWindow.showAtLocation( rootview, Gravity.CENTER, 0, 0 );
        callTel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打电话
                popupWindow.dismiss();
                Intent intent = new Intent( Intent.ACTION_CALL );
                intent.setData( Uri.parse( "tel:" + phoneNumber ) );
                if (ActivityCompat.checkSelfPermission( context, Manifest.permission.CALL_PHONE ) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity( intent );
            }
        } );
        saveTel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存到通讯录
                popupWindow.dismiss();
                Intent intent = new Intent( Intent.ACTION_INSERT_OR_EDIT,
                        Uri.parse( "content://com.android.contacts/contacts/" + "1" ) );
                startActivity( intent );
            }
        } );

    }

    /**
     * 选择合适的应用发送邮件
     *
     * @param emailAddress
     */
    void sendMail(final String emailAddress) {

        View contentView = inflater.inflate( R.layout.send_mail, null );
        final PopupWindow popupWindow = new PopupWindow( contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true );
        popupWindow.setContentView( contentView );

        TextView sendMail = (TextView) contentView.findViewById( R.id.send_mail );
        //设置点击空白消失
        popupWindow.setBackgroundDrawable( new BitmapDrawable() );
        popupWindow.setOutsideTouchable( true );
        //显示PopupWindow
        View rootview = inflater.inflate( R.layout.activity_customer_details, null );
        popupWindow.showAtLocation( rootview, Gravity.CENTER, 0, 0 );
        sendMail.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Uri uri = Uri.parse( "mailto:" + emailAddress );
                String[] email = {emailAddress};
                Intent intent = new Intent( Intent.ACTION_SENDTO, uri );
                startActivity( Intent.createChooser( intent, "请选择邮件类应用" ) );
            }
        } );
    }


    void showProgressDialog() {
        contentView.setVisibility( View.INVISIBLE );
        pdLoading = new ProgressDialog( this );
        pdLoading.setTitle( "" );
        pdLoading.setMessage( getString( R.string.loading ) );
        pdLoading.setIndeterminate( false );
        pdLoading.setCancelable( true );
        pdLoading.show();
    }

    void dismissProgressDialog() {
        contentView.setVisibility( View.VISIBLE );
        pdLoading.dismiss();
    }

}
