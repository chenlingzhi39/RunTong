package com.callba.phone.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Advertisement;
import com.callba.phone.bean.CalldaCalllogBean;
import com.callba.phone.bean.CalllogDetailBean;
import com.callba.phone.bean.QuickQueryContactBean;
import com.callba.phone.bean.SearchSortKeyBean;
import com.callba.phone.bean.SystemNumber;
import com.callba.phone.cfg.Constant;
import com.callba.phone.cfg.GlobalConfig;
import com.callba.phone.logic.contact.ContactPersonEntity;
import com.callba.phone.service.CalllogService;
import com.callba.phone.service.CalllogService.CalldaCalllogListener;
import com.callba.phone.service.DialCallListAdapter;
import com.callba.phone.service.NineKeyboardQuickSearch;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.ActivityUtil;
import com.callba.phone.util.CallUtils;
import com.callba.phone.util.DataAnalysis;
import com.callba.phone.util.Interfaces;
import com.callba.phone.util.KeyboardUtil;
import com.callba.phone.util.Logger;
import com.callba.phone.util.NumberAddressService;
import com.callba.phone.util.RxBus;
import com.callba.phone.util.SPUtils;
import com.callba.phone.util.SharedPreferenceUtil;
import com.callba.phone.view.BannerLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import rx.Observable;
import rx.functions.Action1;

@ActivityFragmentInject(
        contentViewId = R.layout.newdial,
        toolbarTitle = R.string.telephone
)
public class MainCallActivity extends BaseActivity implements OnClickListener,
        OnItemClickListener {
    private static final String TAG = "MainCallActivity";
    @BindView(R.id.et_dial_phonenum)
    EditText et_number;
    @BindView(R.id.tv_location)
    TextView tv_location;
    @BindView(R.id.show_title_two)
    LinearLayout ll_number;
    @BindView(R.id.diallistphonenum)
    ListView lv_filterNum;
    @BindView(R.id.dialoldcalllist)
    ListView lv_calllog;
    @BindView(R.id.banner)
    BannerLayout iv_ad;
    @BindView(R.id.ll_searchingcontact)
    LinearLayout llSearchingContact;
    @BindView(R.id.dial_layout)
    LinearLayout ll_diallayout;
    @BindView(R.id.delete_layout)
    LinearLayout ll_delete;
    @BindView(R.id.center_show)
    LinearLayout ll_call;
    /**
     * ll_title：标题栏 ll_number：接收号码输入栏 ll_call：底部拨打栏
     */
    private DialLayouReceiver receiver;

    private CalllogListAdapter adapter; // 通话记录List适配器
    /**
     * 所有的通话记录
     */
    private List<CalldaCalllogBean> allcalllists;
    /**
     * 合并后的通话记录
     */
    private List<CalllogDetailBean> mergecalllists;
    /**
     * 分组合并后并解析三种类型的通话记录
     */
    private List<Map<String, Object>> analysisCalllists;

    private boolean isFristSearchContact = true; // 开始查询联系人标记
    private DialCallListAdapter dcAdapter; // 搜索到的联系人list适配器
    private List<QuickQueryContactBean> filterContact = new ArrayList<QuickQueryContactBean>(); // 跟据输入号码检索到的联系人
    private NineKeyboardQuickSearch mNineKeyboardQuickSearch; // 九宫格快速查询


    private SharedPreferenceUtil mPreferenceUtil;

    private String callNum;
    private String callName;

    private CalldaCalllogBean beancallout;
    private CalllogService calllogService;
    private CallUtils callUtils;
    private KeyboardUtil keyboardUtil;


    private DataAnalysis dataAnalysis;
    private Context context;
    private ArrayList<Integer> localImages = new ArrayList<Integer>();
    private ArrayList<String> webImages = new ArrayList<>();
    private Gson gson;
    List<SystemNumber> list;
    PhoneNumTextWatcher phoneNumTextWatcher;
    private Observable<Boolean> mRefreshAdObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        init();
        registerRedpointReceiver();
        gson = new Gson();
        mPreferenceUtil = SharedPreferenceUtil.getInstance(this);
        allcalllists = new ArrayList<>();
        mergecalllists = new ArrayList<>();
        analysisCalllists = new ArrayList<>();
        calllogService = new CalllogService(this, new Listenrer());
        callUtils = new CallUtils();
        keyboardUtil = new KeyboardUtil();
        dataAnalysis = new DataAnalysis();
        context = this;
        if (savedInstanceState == null) {
            // 检测是否需要自动拨号
            checkMakePhoneCall();
        }
        // 注册监听MainTab页面onResume方法的广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ACTION_TAB_ONRESUME);
        mRefreshAdObservable = RxBus.get().register("refresh_ad", Boolean.class);
        mRefreshAdObservable.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                Logger.i("maincall", "refresh_ad");
                getAd();
            }
        });
    }


    public void init() {
        lv_calllog
                .setOnItemLongClickListener(new MyListLongClickListener(this));
        lv_calllog.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                beancallout = mergecalllists.get(position).getCalllogBean()
                        .get(0);
                Intent intent = new Intent(MainCallActivity.this, SelectDialPopupWindow.class);
                intent.putExtra("name", beancallout.getDisplayName());
                intent.putExtra("number", beancallout.getCallLogNumber());
                startActivity(intent);
            }
        });
        lv_filterNum.setOnItemClickListener(this);
        IntentFilter filter = new IntentFilter(
                "com.runtong.phone.diallayout.show");
        receiver = new DialLayouReceiver();
        registerReceiver(receiver, filter);
        phoneNumTextWatcher = new PhoneNumTextWatcher(et_number);
        et_number.addTextChangedListener(phoneNumTextWatcher);
        for (int position = 1; position <= 3; position++)
            localImages.add(getResId("ad" + position, R.drawable.class));
        iv_ad.setViewRes(localImages);
        ll_delete.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 清空输入的号码
                et_number.setText("");
                return true;
            }
        });
    }


    @Override
    protected void onResume() {
        // 查询通话记录
        calllogService.setQueryLocalCalllogCount(50);
        calllogService.startQueryCallLog(false);
        super.onResume();
    }


    /**
     * 检测是否有拦截的未呼叫的号码需要拨打
     *
     * @author zhw
     */
    private void checkMakePhoneCall() {
        long currentTime = System.currentTimeMillis();
        long recordCallTime = mPreferenceUtil.getLong(
                Constant.SYS_DIALER_CALLTIME, 0);

        // 在允许时间范围内
        if (currentTime - recordCallTime < Constant.SYS_DIALER_CALLEE_REMAIN_TIME) {
            String callee = mPreferenceUtil
                    .getString(Constant.SYS_DIALER_CALLEE);
            if (!TextUtils.isEmpty(callee)) {
                try {
                    callUtils.judgeCallMode(this, callee);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mPreferenceUtil.putString(Constant.SYS_DIALER_CALLEE, "");
            mPreferenceUtil.putLong(Constant.SYS_DIALER_CALLTIME, 0);
            mPreferenceUtil.commit();
        }
    }


    /**
     * 设置et_number输入的数字
     *
     * @param number
     */
    private void setSearchNumber(String number) {
        int startIndex = et_number.getSelectionStart();
        int endIndex = et_number.getSelectionEnd();

        if (startIndex == endIndex) {
            et_number.append(number);
        } else {
            et_number.setText("");
            et_number.setText(number);
        }
    }


    // 快速检索监听器
    NineKeyboardQuickSearch.NineKeyboardQuickSearchListener keyboardQuickSearchListener = new NineKeyboardQuickSearch.NineKeyboardQuickSearchListener() {

        @Override
        public void onSearchCompleted(
                Map<String, List<QuickQueryContactBean>> searchedContactBeanMap) {

            Logger.i(TAG,
                    "MainCallActivity receive NineKeyboardQuickSearch callback..");

            String searchNumber = et_number.getText().toString().trim();
            if (TextUtils.isEmpty(searchNumber)) {
                return;
            }

            List<QuickQueryContactBean> searchedContactBean = searchedContactBeanMap
                    .get(searchNumber);

            filterContact.clear();
            if (searchedContactBean != null) {
                filterContact.addAll(searchedContactBean);
                llSearchingContact.setVisibility(View.GONE);
            }
            if (searchNumber.length() > 10) {
                String address = NumberAddressService.getAddress(
                        searchNumber, Constant.DB_PATH,
                        MainCallActivity.this);
                tv_location.setText(address);
            } else if (searchNumber.length() < 8) {
                tv_location.setText("");
            }
            if (dcAdapter == null) {
                dcAdapter = new DialCallListAdapter(MainCallActivity.this,
                        filterContact);
                lv_filterNum.setAdapter(dcAdapter);
                if (filterContact.size() > 0)
                    lv_filterNum.setVisibility(View.VISIBLE);
                else lv_filterNum.setVisibility(View.GONE);
            } else {
                if (filterContact.size() > 0)
                    lv_filterNum.setVisibility(View.VISIBLE);
                else lv_filterNum.setVisibility(View.GONE);
                dcAdapter.notifyDataSetChanged();
            }
        }
    };

    @OnClick({R.id.num1, R.id.num2, R.id.num3, R.id.num4, R.id.num5, R.id.num6, R.id.num7, R.id.num8, R.id.num9, R.id.numxinghao, R.id.num0, R.id.numjinghao, R.id.dialcall, R.id.add_contact, R.id.send_message, R.id.add_to_contact, R.id.up_down_layout, R.id.delete_layout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.num0:
                keyboardUtil.startDTMF(0, this);
                setSearchNumber("0");
                break;
            case R.id.num1:
                keyboardUtil.startDTMF(1, this);
                setSearchNumber("1");
                break;
            case R.id.num2:
                keyboardUtil.startDTMF(2, this);
                setSearchNumber("2");
                break;
            case R.id.num3:
                keyboardUtil.startDTMF(3, this);
                setSearchNumber("3");
                break;
            case R.id.num4:
                keyboardUtil.startDTMF(4, this);
                setSearchNumber("4");
                break;
            case R.id.num5:
                keyboardUtil.startDTMF(5, this);
                setSearchNumber("5");
                break;
            case R.id.num6:
                keyboardUtil.startDTMF(6, this);
                setSearchNumber("6");
                break;
            case R.id.num7:
                keyboardUtil.startDTMF(7, this);
                setSearchNumber("7");
                break;
            case R.id.num8:
                keyboardUtil.startDTMF(8, this);
                setSearchNumber("8");
                break;
            case R.id.num9:
                keyboardUtil.startDTMF(9, this);
                setSearchNumber("9");
                break;
            case R.id.numxinghao:
                keyboardUtil.startDTMF(10, this);
                setSearchNumber("*");
                break;
            case R.id.numjinghao:
                keyboardUtil.startDTMF(11, this);
                setSearchNumber("#");
                break;
            case R.id.delete_layout:
                String text = et_number.getText().toString().trim();
                keyboardUtil.startDTMF(12, this);
                if (text != null && text.length() > 1) {
                    et_number.setText(text.substring(0, text.length() - 1));
                } else {
                    et_number.setText("");
                }
                break;
            case R.id.up_down_layout:
                ll_call.setVisibility(View.INVISIBLE);
                ll_diallayout.setVisibility(View.GONE);
                iv_ad.setVisibility(View.GONE);
                BaseActivity.flag = false;
                sendBroadcast(new Intent("toggle_tab"));
                break;

            case R.id.dialcall:
                callNum = et_number.getText().toString().trim();
                startActivityForResult(new Intent(MainCallActivity.this, SelectDialPopupWindow.class).putExtra("number", callNum), 0);
                break;
            case R.id.add_contact:
                String number = et_number.getText().toString();
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType("vnd.android.cursor.dir/person");
                intent.setType("vnd.android.cursor.dir/contact");
                intent.setType("vnd.android.cursor.dir/raw_contact");
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                if (!isIntentAvailable(this, intent)) {
                    break;
                } else {
                    startActivity(intent);
                }
                break;
            case R.id.send_message:
                String number1 = et_number.getText().toString();
                Uri smsToUri = Uri.parse("smsto://" + number1);
                Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
                startActivity(mIntent);
                break;
            case R.id.add_to_contact:
                String number2 = et_number.getText().toString();
                Intent intent1 = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                intent1.setType("vnd.android.cursor.item/person");
                intent1.setType("vnd.android.cursor.item/contact");
                intent1.setType("vnd.android.cursor.item/raw_contact");
                intent1.putExtra(ContactsContract.Intents.Insert.PHONE, number2);
                intent1.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, 2);
                if (!isIntentAvailable(this, intent1)) {
                    break;
                } else {
                    startActivity(intent1);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 监听键盘显示/隐藏广播
     *
     * @author Zhang
     */
    class DialLayouReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            if ("show".equals(action)) {
                // 显示
                ll_diallayout.setVisibility(View.VISIBLE);
                iv_ad.setVisibility(View.VISIBLE);
                if (!"".equals(et_number.getText().toString().trim())) {
                    // 存在号码 显示拨号栏
                    ll_call.setVisibility(View.VISIBLE);
                    iv_ad.setVisibility(View.GONE);
                }
            } else if ("hide".equals(action)) {
                // 隐藏
                ll_diallayout.setVisibility(View.GONE);
                iv_ad.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 监听号码输入EditText中号码改变
     *
     * @author Zhang
     */
    class PhoneNumTextWatcher implements TextWatcher {
        private EditText editText;

        public PhoneNumTextWatcher(EditText editText) {
            super();
            this.editText = editText;
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            String searchNumber = editText.getText().toString().trim();
            // 搜索的数字发生变化后，取消文字操作栏显示

            onInputNumberChanged(searchNumber, count);
        }
    }

    /**
     * 输入号码改变
     *
     * @param searchNumber
     */
    private synchronized void onInputNumberChanged(String searchNumber,
                                                   int numberCount) {
        if (mNineKeyboardQuickSearch == null) {
            mNineKeyboardQuickSearch = new NineKeyboardQuickSearch(
                    keyboardQuickSearchListener);
        }

        if ("".equals(searchNumber)) {
            ll_number.setVisibility(View.GONE);
            ll_call.setVisibility(View.INVISIBLE);
            iv_ad.setVisibility(View.VISIBLE);
            Logger.i("input", "change");
            //lv_calllog.setVisibility(View.VISIBLE);
            lv_filterNum.setVisibility(View.GONE);
            if (analysisCalllists.size() == 0 && analysisCalllists.size() == 0)
                lv_calllog.setVisibility(View.GONE);
            else lv_calllog.setVisibility(View.VISIBLE);
            isFristSearchContact = true;

            // 停止查询联系人
            if (mNineKeyboardQuickSearch != null) {
                mNineKeyboardQuickSearch.stopQuery();
            }
            // 取消查询进度显示
            llSearchingContact.setVisibility(View.GONE);

        } else {
            if (isFristSearchContact) {
                isFristSearchContact = false;

                ll_number.setVisibility(View.VISIBLE);
                ll_call.setVisibility(View.VISIBLE);
                iv_ad.setVisibility(View.GONE);
                // 显示查询进度条
                llSearchingContact.setVisibility(View.VISIBLE);

                // 开始查询
                mNineKeyboardQuickSearch.startQuery(allcalllists);


                lv_calllog.setVisibility(View.GONE);
            }

            if (numberCount > 1) {
                mNineKeyboardQuickSearch.setPastedSearchNumber(searchNumber);
            } else {
                mNineKeyboardQuickSearch.setSearchNumber(searchNumber);
            }
        }

        // 设置输入号码的大小，多余10个号码后 每输入一个 变小一号
        if (searchNumber.length() > 10) {
            et_number.setTextSize(44 - searchNumber.length());
        } else {
            et_number.setTextSize(34);
        }
    }


    /**
     * ListView监听器
     *
     * @author Zhang
     */
    class MyListLongClickListener implements OnItemLongClickListener {
        private Context context;

        public MyListLongClickListener(Context context) {
            super();
            this.context = context;
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            if (allcalllists == null)
                return false;
            CalldaCalllogBean bean = allcalllists.get(position);
            showDeleteDialog(context, mergecalllists.get(position));
            return true;
        }

    }

    /**
     * 删除通话记录对话框
     *
     * @param context
     */
    private void showDeleteDialog(Context context,
                                  final CalllogDetailBean bean) {
      /*  final Dialog dialog = new Dialog(context, R.style.MyDialog);
        View view = View.inflate(context, R.layout.calllog_delete_dialog_bg,
                null);
        dialog.setContentView(view);

        TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
        TextView tv_close = (TextView) view.findViewById(R.id.tv_close);
        TextView tv_delete_item = (TextView) view
                .findViewById(R.id.tv_delete_item);
        TextView tv_delete_all = (TextView) view
                .findViewById(R.id.tv_delete_all);
        tv_name.setText(name);

        // 关闭
        tv_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // 删除单条通话记录
        tv_delete_item.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calllogService.deleteSingleCallLog(bean);
                dialog.dismiss();
            }
        });

        // 删除所有通话记录
        tv_delete_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calllogService.deleteAllCallLog();
                dialog.dismiss();
            }
        });

        dialog.show();*/
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(bean.getCallLogNumber());
        builder.setItems(new String[]{"删除单条通话记录", "删除所有通话记录"},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                calllogService.deleteSingleCallLog(bean);
                                break;
                            case 1:
                                calllogService.deleteAllCallLog();
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        Logger.i("maincall", "destroy");
        RxBus.get().unregister("refresh_ad", mRefreshAdObservable);
        super.onDestroy();
        // 取消广播监听
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
        }
    }

    /**
     * 重写onkeyDown 捕捉返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 转到后台运行
            ActivityUtil.moveAllActivityToBack();
            return true;
        }
        return false;
    }

    /**
     * 检索到号码的listview
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        ContactPersonEntity bean = filterContact.get(position);
        et_number.setText(bean.getPhoneNumber());
        callName = bean.getDisplayName();
        Logger.v("检索列表拨号", bean.getPhoneNumber() + "callName:" + callName);
        callNum = et_number.getText().toString().trim();
    }

    /**
     * 通话记录查询监听器
     *
     * @author Administrator
     */
    class Listenrer implements CalldaCalllogListener {

        @Override
        public void onQueryCompleted(List<CalldaCalllogBean> calldaCalllogBeans) {
            allcalllists.clear();

            if (0 < calldaCalllogBeans.size()) {
                for (CalldaCalllogBean bean : calldaCalllogBeans) {
                    String tempPhoneNumber = "";
                    if (TextUtils.isEmpty(bean.getDisplayName())) {
                        tempPhoneNumber = bean.getCallLogNumber();
                        if (!"".equals(tempPhoneNumber)
                                && tempPhoneNumber.length() >= 11
                                && tempPhoneNumber.startsWith("86")) {
                            tempPhoneNumber = tempPhoneNumber.substring(2);
                        }
                        // bean.setDisplayName("未知");
                        bean.setDisplayName(tempPhoneNumber);
                        if (GlobalConfig.getInstance().getContactBeans() != null) {
                            for (ContactPersonEntity contactBean : GlobalConfig
                                    .getInstance().getContactBeans()) {
                                if (contactBean.getPhoneNumber().equals(
                                        tempPhoneNumber)) {
                                    // 通讯录中存在该记录
                                    bean.setDisplayName(contactBean
                                            .getDisplayName());

                                    bean.setSearchSortKeyBean(contactBean
                                            .getSearchSortKeyBean());
                                    break;
                                }
                            }
                        }
                    }
                    Constant.DB_PATH = SharedPreferenceUtil.getInstance(
                            MainCallActivity.this).getString(Constant.DB_PATH_);
                    // 归属地
                    String address = NumberAddressService.getAddress(
                            bean.getCallLogNumber(), Constant.DB_PATH,
                            MainCallActivity.this);
                    bean.setLocation(address);
                    // Logger.v("归属地", bean.getCallLogNumber()+":"+address);

                    if (bean.getSearchSortKeyBean() == null) {
                        if (GlobalConfig.getInstance().getContactBeans() != null) {
                            for (ContactPersonEntity contactBean : GlobalConfig
                                    .getInstance().getContactBeans()) {
                                if (contactBean.getPhoneNumber().equals(
                                        bean.getCallLogNumber())) {
                                    bean.setSearchSortKeyBean(contactBean
                                            .getSearchSortKeyBean());
                                    break;
                                }
                            }
                        }
                    }

                    // 如果非联系人 以上均过滤不到 则设个默认值
                    if (bean.getSearchSortKeyBean() == null) {
                        bean.setSearchSortKeyBean(new SearchSortKeyBean());
                    }
                }

                allcalllists.addAll(calldaCalllogBeans);
            }

            // 根据电话号码查询合并一天中的相邻的电话
            List<CalllogDetailBean> calllogDetailBeans = calllogService
                    .QueryContinueDayCalllog(allcalllists);
            List<Map<String, Object>> analysisData = dataAnalysis.mygetData(
                    context, calllogDetailBeans);

            mergecalllists.clear();
            mergecalllists.addAll(calllogDetailBeans);

            analysisCalllists.clear();
            analysisCalllists.addAll(analysisData);

            if (adapter == null) {
                adapter = new CalllogListAdapter(MainCallActivity.this,
                        analysisCalllists, mergecalllists);
                lv_calllog.setAdapter(adapter);
                if (analysisCalllists.size() == 0 && analysisCalllists.size() == 0)
                    lv_calllog.setVisibility(View.GONE);
                else lv_calllog.setVisibility(View.VISIBLE);
            } else {
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onDeleteCompleted() {
            calllogService.setQueryLocalCalllogCount(50);
            calllogService.startQueryCallLog(false);
        }
    }

    /**
     * 接收MainTab页面Onresume生命周期方法的广播
     *
     * @author Administrator
     */
    class OnMainTabOnResumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            calllogService.setQueryLocalCalllogCount(50);
            calllogService.startQueryCallLog(false);
        }
    }


    public static final String KEYBOARD_MESSAGE_RECEIVED_ACTION = "com.runtong.KEYBOARD_AD_ACTION";

    public void registerRedpointReceiver() {
        Logger.i("CallButtonChangeReceiver",
                "registerMessageReceiver-->CallButtonChangeReceiver");
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(KEYBOARD_MESSAGE_RECEIVED_ACTION);
    }

    public class LoginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getAd();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((boolean) SPUtils.get(this, Constant.PACKAGE_NAME, Constant.KeyboardSetting, true))
            getMenuInflater().inflate(R.menu.menu_open_ring, menu);
        else getMenuInflater().inflate(R.menu.menu_close_ring, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ring:
                boolean isKeyboardToneOn = (boolean) SPUtils.get(this, Constant.PACKAGE_NAME, Constant.KeyboardSetting, true);
                SPUtils.put(this, Constant.PACKAGE_NAME, Constant.KeyboardSetting, !isKeyboardToneOn);
                SharedPreferenceUtil.getInstance(this).putBoolean(
                        Constant.KeyboardSetting, !isKeyboardToneOn, true);
                if ((boolean) SPUtils.get(this, Constant.PACKAGE_NAME, Constant.KeyboardSetting, true)) {
                    item.setIcon(R.drawable.ic_volume_up_orange_24dp);
                    item.setTitle(R.string.close_ring);
                } else {
                    item.setIcon(R.drawable.ic_volume_off_orange_24dp);
                    item.setTitle(R.string.open_ring);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getAd() {
        addRequestCall(OkHttpUtils.post().url(Interfaces.GET_ADVERTICEMENT3)
                .addParams("loginName", getUsername())
                .addParams("loginPwd", getPassword())
                .addParams("softType", "android")
                .build()).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    Logger.i("ad3_result", response);
                    String[] result = response.split("\\|");
                    if (result[0].equals("0")) {
                        final ArrayList<Advertisement> list;
                        list = gson.fromJson(result[1], new TypeToken<ArrayList<Advertisement>>() {
                        }.getType());
                        webImages.clear();
                        for (Advertisement advertisement : list) {
                            webImages.add(advertisement.getImage());
                        }
                        iv_ad.setViewUrls(webImages);
                        iv_ad.setOnBannerItemClickListener(new BannerLayout.OnBannerItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                                intent1.setData(Uri.parse(list.get(position).getAdurl()));
                                startActivity(intent1);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            et_number.setText("");
        }
    }
}
