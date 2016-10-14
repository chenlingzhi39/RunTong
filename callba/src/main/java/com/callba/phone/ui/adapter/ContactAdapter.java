package com.callba.phone.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.bean.ContactMultiNumBean;
import com.callba.phone.manager.ContactsManager;
import com.callba.phone.ui.adapter.expandRecyclerviewadapter.StickyRecyclerHeadersAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by PC-20160514 on 2016/10/14.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private List<ContactMultiNumBean> contactMultiNumBeans;
    private Context context;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    public ContactAdapter(Context context) {
        this.context=context;
     contactMultiNumBeans=new ArrayList<>();
    }

    public List<ContactMultiNumBean> getItems() {
        return contactMultiNumBeans;
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        if(contactMultiNumBeans.get(position).getDisplayName().equals(""))
            holder.name.setText("未知");
        else holder.name.setText(contactMultiNumBeans.get(position).getDisplayName());
        Bitmap bitmap= ContactsManager.getAvatar(context,contactMultiNumBeans.get(position).get_id(),false);
        if(bitmap!=null)
            holder.avatar.setImageBitmap(bitmap);
        else holder.avatar.setImageResource(R.drawable.head_portrait);
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_listitem, parent, false);
        final ContactViewHolder viewHolder=new ContactViewHolder(view);
        if (mItemClickListener!=null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(viewHolder.getAdapterPosition());
                }
            });
        }

        if (mItemLongClickListener!=null){
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mItemLongClickListener.onItemClick(viewHolder.getAdapterPosition());
                }
            });
        }
        return viewHolder;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        String showValue = String.valueOf(contactMultiNumBeans.get(position).getTypeName().charAt(0));
        textView.setText(showValue);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    public long getHeaderId(int position) {
        return contactMultiNumBeans.get(position).getTypeName().charAt(0);
    }



    @Override
    public int getItemCount() {
        return contactMultiNumBeans.size();
    }



    public class ContactViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public CircleImageView avatar;

        public ContactViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_displayname);
            avatar=(CircleImageView)itemView.findViewById(R.id.avatar);
        }
    }
    public void add(ContactMultiNumBean object) {
        contactMultiNumBeans.add(object);
        notifyDataSetChanged();
    }

    public void add(int index, ContactMultiNumBean object) {
        contactMultiNumBeans.add(index, object);
        notifyDataSetChanged();
    }

    public void addAll(Collection<ContactMultiNumBean> collection) {
        if (collection != null) {
            contactMultiNumBeans.clear();
            contactMultiNumBeans.addAll(collection);
            notifyDataSetChanged();
        }
    }

    public void addAll(ContactMultiNumBean... items) {
        addAll(Arrays.asList(items));
    }

    public void clear() {
        contactMultiNumBeans.clear();
        notifyDataSetChanged();
    }

    public void remove(ContactMultiNumBean object) {
        contactMultiNumBeans.remove(object);
        notifyDataSetChanged();
    }
    public int getPositionForSection(char section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = contactMultiNumBeans.get(i).getTypeName();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;

    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mItemClickListener = listener;
        notifyDataSetChanged();
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.mItemLongClickListener = listener;
        notifyDataSetChanged();
    }
}
