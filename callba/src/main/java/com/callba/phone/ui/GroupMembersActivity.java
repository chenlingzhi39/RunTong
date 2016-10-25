package com.callba.phone.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.Member;
import com.callba.phone.pinyin.CharacterParser;
import com.callba.phone.pinyin.PinyinComparator;
import com.callba.phone.ui.adapter.GroupMemberAdapter;
import com.callba.phone.ui.adapter.expandRecyclerviewadapter.StickyRecyclerHeadersDecoration;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.EaseUserUtils;
import com.callba.phone.util.Logger;
import com.callba.phone.widget.DividerDecoration;
import com.callba.phone.widget.SideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PC-20160514 on 2016/10/25.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.activity_group_member,
        toolbarTitle = R.string.group_member,
        navigationId = R.drawable.press_back
)
public class GroupMembersActivity extends BaseActivity {

    @BindView(R.id.member_list)
    RecyclerView memberList;
    @BindView(R.id.sidebar)
    SideBar sidebar;
    @BindView(R.id.dialog)
    TextView dialog;
    private GroupMemberAdapter groupMemberAdapter;
    private ArrayList<Member> members;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        sidebar.setTextView(dialog);
        sidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = groupMemberAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    ((LinearLayoutManager)memberList.getLayoutManager()).scrollToPositionWithOffset(position,memberList.getLayoutManager().getChildCount());
                }
            }
        });
        groupMemberAdapter=new GroupMemberAdapter(this);
        members=new ArrayList<>();
        for(String s:getIntent().getStringArrayListExtra("members")){
            Member member=new Member();
            member.setName(s);
            String pinyin = CharacterParser.getInstance().getSelling(EaseUserUtils.getUserNick(s));
            String sortString = pinyin.substring(0, 1).toUpperCase();
            Logger.i("sortString",sortString.toUpperCase());
            if (sortString.matches("[A-Z]")) {
                member.setSortLetters(sortString.toUpperCase());
            } else {
                member.setSortLetters("#");
            }
            members.add(member);
        }
        Collections.sort(members, new Comparator<Member>() {
            @Override
            public int compare(Member member, Member t1) {
                if (member.getSortLetters().equals("@")
                        || t1.getSortLetters().equals("#")) {
                    return -1;
                } else if (member.getSortLetters().equals("#")
                        || t1.getSortLetters().equals("@")) {
                    return 1;
                } else {
                    return member.getSortLetters().compareTo(t1.getSortLetters());
                }
            }
        });
        groupMemberAdapter.addAll(members);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        memberList.setLayoutManager(layoutManager);
        memberList.setAdapter(groupMemberAdapter);
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(groupMemberAdapter);
        memberList.addItemDecoration(headersDecor);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        Logger.i("density",mDensity+"");
        memberList.addItemDecoration(new DividerDecoration(this,mDensity*50/160));

        //   setTouchHelper();
        groupMemberAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                headersDecor.invalidateHeaders();
            }
        });
    }
}
