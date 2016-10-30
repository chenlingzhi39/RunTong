package com.callba.phone.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.Tab;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/10/30.
 */

public class TabAdapter extends RecyclerArrayAdapter<Tab> {
    public int mSelectedItem = 2;

    public TabAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new TabViewHolder(parent);
    }

    class TabViewHolder extends BaseViewHolder<Tab> {
        @BindView(R.id.tab_bar)
        TextView tabBar;

        TabViewHolder(ViewGroup parent) {
            super(parent, R.layout.item_tab);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(Tab data) {
            tabBar.setText(data.getTitle());
            tabBar.setCompoundDrawablesWithIntrinsicBounds(null, getContext().getResources().getDrawable(data.getImage()), null, null);
            tabBar.setSelected(getAdapterPosition() == mSelectedItem);
        }

        @OnClick(R.id.tab_bar)
        public void onClick() {
            mSelectedItem=getAdapterPosition();
            notifyDataSetChanged();
        }
    }

    public int getmSelectedItem() {
        return mSelectedItem;
    }

    public void setmSelectedItem(int mSelectedItem) {
        this.mSelectedItem = mSelectedItem;
        notifyDataSetChanged();
    }
}
