package com.callba.phone.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ExpandableListView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.adapter.HelpAdapter;
import com.callba.phone.adapter.HelpListAdapter;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Help;
import com.callba.phone.widget.DividerItemDecoration;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PC-20160514 on 2016/6/11.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.help,
        navigationId = R.drawable.press_back,
        toolbarTitle = R.string.help
)
public class HelpActivity extends BaseActivity {
    @InjectView(R.id.list)
    ExpandableListView list;
    private HelpAdapter helpAdapter;
    private HelpListAdapter helpListAdapter;
    ArrayList<Help> helps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        helpListAdapter=new HelpListAdapter(this);
        list.setGroupIndicator(null);
        list.setAdapter(helpListAdapter);
        list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                for (int i = 0; i < helpListAdapter.getGroupCount(); i++) {
                    if (groupPosition != i) {
                        list.collapseGroup(i);
                    }
                }
            }
        });
       /* helpAdapter=new HelpAdapter(this);
        helps=new ArrayList<>();
        helps.add(new Help("使用Call吧要换卡吗？","    亲，Call是新一代网络通讯技术，不需要换卡！有WIFI或者4G网络就行！"));
        helps.add(new Help("使用Call吧有月租、漫游费么？","    亲，那是黑心三大运营商干的事，我们无月租、无漫游费、不分长短途！就四个字：随便打！"));
        helps.add(new Help("使用Call吧打电话会不会花费我的手机花费？","    亲，别闹，用Call吧还要花费您的手机花费，我们也不用干了！"));
        helps.add(new Help("使用Call吧打电话会不会消耗流量？","    亲，都说了，在有Wifi无线网络下，绝不耗一丁点流量！在4G网络情况下，每次拨打仅耗费1KB流量！什么概念呢？就是100M的流量可以拨打100乘以1000=10万次电话！（1M=1000KB）通话过程中不耗费任何流量！\n"));
        helps.add(new Help("使用Call吧充值的话费有有效期么？","    亲，我们的服务理念是：一张电话卡，服务永流传！"));
        helps.add(new Help("使用Call吧打电话会不会出现信号不好啊？","    亲，如果您地处荒山野岭，犄角旮旯我们还真不能保证信号一定好！"));
        helps.add(new Help("怎么联系你们","亲，想我就直接拨打400-8078-255，我们有热情的小妹子为您详细解答人生难题，但妹子很忙，谢绝调戏！"));
        list.setLayoutManager(new LinearLayoutManager(this));
        helpAdapter.addAll(helps);
        list.setAdapter(helpAdapter);
        list.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));*/
    }

    @Override
    public void init() {

    }

    @Override
    public void refresh(Object... params) {

    }
}
