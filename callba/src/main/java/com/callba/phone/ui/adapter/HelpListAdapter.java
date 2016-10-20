package com.callba.phone.ui.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.R;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by Administrator on 2016/6/11.
 */
public class HelpListAdapter implements ExpandableListAdapter {
    @BindView(R.id.answer)
    TextView answer;
    private LayoutInflater mInflater;

    public HelpListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private String[] asks = new String[]{
            "使用Call吧要换卡吗？", "使用Call吧有月租、漫游费么？", "使用Call吧打电话会不会花费我的手机话费？", "使用Call吧打电话会不会消耗流量？",
            "使用Call吧充值的话费有有效期么？", "使用Call吧打电话会不会出现信号不好啊？", "怎么联系你们",
    };
    private String[] answers = new String[]{
            "        亲，Call是新一代网络通讯技术，不需要换卡！有WIFI或者4G网络就行！",
            "        亲，那是黑心三大运营商干的事，我们无月租、无漫游费、不分长短途！就四个字：随便打！",
            "        亲，别闹，用Call吧还要花费您的手机花费，我们也不用干了！",
            "        亲，都说了，在有Wifi无线网络下，绝不耗一丁点流量！在4G网络情况下，每次拨打仅耗费1KB流量！什么概念呢？就是100M的流量可以拨打100乘以1000=10万次电话！（1M=1000KB）通话过程中不耗费任何流量！",
            "        亲，我们的服务理念是：一张电话卡，服务永流传！",
            "        亲，如果您地处荒山野岭，犄角旮旯我们还真不能保证信号一定好！",
            "        亲，想我就直接拨打400-8078-255，我们有热情的小妹子为您详细解答人生难题，但妹子很忙，谢绝调戏！"
    };

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getGroupCount() {
        return asks.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
          GroupViewHolder holder = null;
            convertView = mInflater.inflate(R.layout.item_group, null);
            holder = new GroupViewHolder(convertView);
            holder.ask.setText(asks[groupPosition]);
            if(!isExpanded)
                holder.imageView.setImageResource(R.drawable.more);
            else holder.imageView.setImageResource(R.drawable.more_on);
            convertView.setTag(holder);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder = null;
            convertView = mInflater.inflate(R.layout.item_child, null);
            holder = new ChildViewHolder(convertView);
            holder.answer.setText(answers[groupPosition]);
            convertView.setTag(holder);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {

    }

    @Override
    public void onGroupCollapsed(int groupPosition) {

    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return 0;
    }

    static class GroupViewHolder {
        @BindView(R.id.ask)
        TextView ask;
        @BindView(R.id.indicator)
        ImageView imageView;
        GroupViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
    static class ChildViewHolder {
        @BindView(R.id.answer)
        TextView answer;

        ChildViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
