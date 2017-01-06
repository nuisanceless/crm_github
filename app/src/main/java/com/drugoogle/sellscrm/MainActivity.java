package com.drugoogle.sellscrm;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.drugoogle.sellscrm.Utils.CommonUtils;
import com.drugoogle.sellscrm.data.response.BaseResponse;
import com.drugoogle.sellscrm.data.response.NewWorkOrderCountResponse;
import com.drugoogle.sellscrm.data.type.Gender;
import com.drugoogle.sellscrm.experiment.ExperimentActivity;
import com.drugoogle.sellscrm.selfinfo.Account;
import com.drugoogle.sellscrm.selfinfo.Selfinfo_info_Activity_;
import com.drugoogle.sellscrm.selfinfo.Selfinfo_setting_Activity_;
import com.drugoogle.sellscrm.visit.AddTempVisitRecordActivity_;
import com.drugoogle.sellscrm.visit.AddVisitPlanActivity_;
import com.drugoogle.sellscrm.visit.MainFragment;
import com.drugoogle.sellscrm.visit.VisitPlanDetailActivity;
import com.drugoogle.sellscrm.visit.VisitSearchActivity_;
import com.drugoogle.sellscrm.workorder.CheckNewOrder;
import com.drugoogle.sellscrm.workorder.OrderCheckCallback;
import com.drugoogle.sellscrm.workorder.WorkOrderActivity;
import com.jauker.widget.BadgeView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Date;

@EActivity(R.layout.main_activity)
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final int ADD_PLAN_RECORD_REQUEST = 11;

    private Account account;
    private static MainActivity instance = null;
    /**
     * 记录当前fragment的编号
     */
    private int mCurrentModule = -1;

    @ViewById(R.id.container)
    ViewGroup container;

    @ViewById(R.id.tabBar)
    TabLayout tabBar;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    TextView mSelfName;
    ImageView mHeadPortrait;
    TextView mCustomerCount;
    TextView mVisitRestCount;
    BadgeView bageView;
    Context context;
    IntentFilter stateToWaitFilter;
    OrderStateChangeReceiver orderStateChangeReceiver;
    CheckNewOrder checkNewOrder;
    int newOrderCount;

    public static void finishInstance() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult( requestCode, resultCode, intent );
        if (requestCode == BaseResponse.REQUEST_CODE_EXPIRED && resultCode == BaseResponse.RESULT_CODE_EXPIRED) {
            // TODO: 2016/5/10 登陆过期后，在登陆页面再次登陆后回来，可以向fragment分发消息
            MODULES[mCurrentModule].fragment.onActivityResult( requestCode, resultCode, intent );
        }
        if (requestCode == ADD_PLAN_RECORD_REQUEST) {
            MODULES[mCurrentModule].fragment.onActivityResult( requestCode, resultCode, intent );
        }
        if (requestCode == MainFragment.REQUEST_CODE_VISIT_PLAN_DETAIL && resultCode == VisitPlanDetailActivity.RESULT_CODE_DELETE_PLAN) {
            MODULES[mCurrentModule].fragment.onActivityResult( requestCode, resultCode, intent );
        }
        if (requestCode == com.drugoogle.sellscrm.workorder.MainFragment.REQUEST_CODE_STATUS_CHANGE
                && resultCode == WorkOrderActivity.RESULT_CODE_STATUS_CHANGE) {
            MODULES[mCurrentModule].fragment.onActivityResult( requestCode, resultCode, intent );
        }
        if (requestCode == com.drugoogle.sellscrm.customer.MainFragment.REQUEST_LEVEL_EDIT
                && resultCode == com.drugoogle.sellscrm.customer.MainFragment.RESULT_LEVEL_CHANGED) {
            MODULES[mCurrentModule].fragment.onActivityResult( requestCode, resultCode, intent );
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
    }

    @AfterViews
    protected void init() {

        account = Account.getInstance();
        if (account ==  null)
        {
            finish();
            return;
        }
        instance = this;
        context = this;
        stateToWaitFilter = new IntentFilter();
        stateToWaitFilter.addAction( "com.drugoogle.sellcrm.workorder.STATUS_BROADCAST" );
        orderStateChangeReceiver = new OrderStateChangeReceiver();
        registerReceiver( orderStateChangeReceiver, stateToWaitFilter );

        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );
        toolbar.setOnMenuItemClickListener( onMenuItemClickListener );

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawer.addDrawerListener( toggle );
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener( this );
        View headerView = navigationView.getHeaderView( 0 );
        mSelfName = (TextView) headerView.findViewById( R.id.self_name );
        mSelfName.setText( account.getName() );
        mHeadPortrait = (ImageView) headerView.findViewById( R.id.head_portrait );
        mHeadPortrait.setImageResource( Gender.getPortraitImgRes( account.getGender() ) );
        mCustomerCount = (TextView) headerView.findViewById( R.id.customer_count_tv );
        mCustomerCount.setText( String.valueOf( account.getCustomerCount() ) );
        mVisitRestCount = (TextView) headerView.findViewById( R.id.visit_rest_count_tv );
        mVisitRestCount.setText( String.valueOf( account.getVisitCount() ) );

        headerView.findViewById( R.id.self_info ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelfInfo();
            }
        } );

        headerView.findViewById( R.id.work_order ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWorkOrder();
            }
        } );
        headerView.findViewById( R.id.setting ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetting();
            }
        } );
        headerView.findViewById(R.id.experiment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExperiment();
            }
        });
        for (int i = 0; i < MODULES.length; i++) {
            TabLayout.Tab tab = tabBar.newTab();
            View v = LayoutInflater.from( MainActivity.this ).inflate( R.layout.main_tabitem, null );
            TextView tv = (TextView) v.findViewById( R.id.title );
            tv.setText( MODULES[i].name );
            ImageView img = (ImageView) v.findViewById( R.id.icon );
            img.setImageResource( MODULES[i].icon );
            tab.setCustomView( v );
            tabBar.addTab( tab );
        }

        checkNewWorkOrder();

        tabBar.setOnTabSelectedListener( new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                changeToModule( tab.getPosition() );
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        } );
        changeToModule( 0 );
    }

    private void checkNewWorkOrder() {
        bageView = new BadgeView( this );
        bageView.setBackgroundResource( R.drawable.point_red );
        bageView.setBadgeGravity( Gravity.RIGHT | Gravity.TOP );
        bageView.setTargetView( tabBar.getTabAt( 2 ).getCustomView().findViewById( R.id.icon ) );
        checkNewOrder = new CheckNewOrder();
        checkNewOrder.startCheck( new OrderCheckCallback() {
            @Override
            public void onFinish(NewWorkOrderCountResponse resp) {
                if (!BaseResponse.hasErrorWithOperation( resp,context )) {
                    if (resp.code == BaseResponse.SUCCESS_CODE) {
                        newOrderCount = resp.data;
                        if (newOrderCount > 0) {
                            bageView.setText( " " );
                        }
                    }
                }
            }
        } );
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver( orderStateChangeReceiver );
        checkNewOrder.stopCheck();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        CommonUtils.checkUpdateIfNeed(this, false);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.main_menu, menu );

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService( Context.SEARCH_SERVICE );
        SearchView searchView =
                (SearchView) menu.findItem( R.id.view_search ).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo( getComponentName() ) );
        return true;
    }


    /**
     * 根据fragment的不同显示不同的optionMenu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (mCurrentModule) {
            case 0:
                menu.findItem( R.id.view_search ).setVisible( false );
                menu.findItem( R.id.search ).setVisible( true );
                menu.findItem( R.id.add_visit_plan ).setVisible( true );
                menu.findItem( R.id.add_temp_visit_record ).setVisible( true );
                ((TextView) toolbar.findViewById( R.id.toolbar_title )).setText( getString( R.string.today_schedule ) );
                break;
            case 1:
                menu.findItem( R.id.view_search ).setVisible( true );
                menu.findItem( R.id.search ).setVisible( false );
                menu.findItem( R.id.add_visit_plan ).setVisible( false );
                menu.findItem( R.id.add_temp_visit_record ).setVisible( false );
                ((TextView) toolbar.findViewById( R.id.toolbar_title )).setText( getString( R.string.mine_client ) );
                break;
            //先去掉消息
//            case 2:
//                menu.findItem( R.id.view_search ).setVisible( false );
//                menu.findItem( R.id.search ).setVisible( false );
//                menu.findItem( R.id.add_visit_plan ).setVisible( false );
//                menu.findItem( R.id.add_temp_visit_record ).setVisible( false );
//                ((TextView) toolbar.findViewById( R.id.toolbar_title )).setText( getString( R.string.message ) );
//                break;
            case 2:
                menu.findItem( R.id.view_search ).setVisible( false );
                menu.findItem( R.id.search ).setVisible( false );
                menu.findItem( R.id.add_visit_plan ).setVisible( false );
                menu.findItem( R.id.add_temp_visit_record ).setVisible( false );
                ((TextView) toolbar.findViewById( R.id.toolbar_title )).setText( getString( R.string.mine_workorder ) );
                break;
            default:
                menu.findItem( R.id.view_search ).setVisible( false );
                menu.findItem( R.id.search ).setVisible( false );
                menu.findItem( R.id.add_visit_plan ).setVisible( false );
                menu.findItem( R.id.add_temp_visit_record ).setVisible( false );
                break;
        }
        return super.onPrepareOptionsMenu( menu );
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Date date = ((MainFragment) MODULES[0].fragment).getSelectDate();
            switch (item.getItemId()) {
                case R.id.view_search:
                    break;
                case R.id.search:
                    VisitSearchActivity_.intent( MainActivity.this ).start();
                    break;
                case R.id.add_visit_plan:
                    AddVisitPlanActivity_.intent( MainActivity.this ).selectDate( date ).startForResult( ADD_PLAN_RECORD_REQUEST );
                    break;
                case R.id.add_temp_visit_record:
                    AddTempVisitRecordActivity_.intent( MainActivity.this ).selectDate( date ).startForResult( ADD_PLAN_RECORD_REQUEST );
                    break;
            }
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        if (drawer.isDrawerOpen( GravityCompat.START )) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @Click(R.id.self_info)
    void onSelfInfo() {
        Selfinfo_info_Activity_.intent( this ).start();

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
    }

    @Click(R.id.work_order)
    void onWorkOrder() {
        tabBar.getTabAt( 2 ).select();
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
    }

    @Click(R.id.setting)
    void onSetting() {
        Selfinfo_setting_Activity_.intent( this ).start();
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
    }

    @Click(R.id.experiment)
    void onExperiment() {
        startActivity(new Intent(this, ExperimentActivity.class));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    private static class ModuleInfo {
        ModuleInfo(String name, int icon, Fragment fragment) {
            this.name = name;
            this.icon = icon;
            this.fragment = fragment;
        }

        String name;
        int icon;
        Fragment fragment;
    }

    private ModuleInfo[] MODULES = new ModuleInfo[]
            {
                    new ModuleInfo( "拜访", R.drawable.visit_tab_icon, new com.drugoogle.sellscrm.visit.MainFragment_() ),
                    new ModuleInfo( "客户", R.drawable.customer_tab_icon, new com.drugoogle.sellscrm.customer.MainFragment_() ),
                    //先去掉消息
//                    new ModuleInfo( "消息", R.drawable.ic_menu_manage, new com.drugoogle.sellscrm.message.MainFragment_() ),
                    new ModuleInfo( "工单", R.drawable.order_tab_icon, new com.drugoogle.sellscrm.workorder.MainFragment_() ),
            };


    private void changeToModule(int index) {
//        if (index == 2){
//            bageView.setBadgeCount( 0 );
//        }
        if (index == mCurrentModule)
            return;

        FragmentManager fmgr = getSupportFragmentManager();
        FragmentTransaction ft = fmgr.beginTransaction();
        if (mCurrentModule >= 0) {
            ft.detach( MODULES[mCurrentModule].fragment );
        }
        String tag = MODULES[index].fragment.getClass().getCanonicalName();
        if (fmgr.findFragmentByTag( tag ) == null) {
            ft.add( R.id.container, MODULES[index].fragment, tag );
        } else {
            ft.attach( MODULES[index].fragment );
        }
        mCurrentModule = index;
        ft.commit();

        //为了在切换tab的时候让上一个optionMenu失效
        invalidateOptionsMenu();
    }


    class OrderStateChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra( "hasNew" )) {
                bageView.setText( " " );
            }else {
                --newOrderCount;
                if (newOrderCount <= 0) {
                    bageView.setVisibility( View.GONE );
                }
            }
        }
    }
}
