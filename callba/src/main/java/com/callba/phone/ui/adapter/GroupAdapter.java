/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.callba.phone.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.callba.R;
import com.callba.phone.util.Logger;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;


import java.util.List;

public class GroupAdapter extends ArrayAdapter<EMGroup> {

	private LayoutInflater inflater;
	private String newGroup;
	private String addPublicGroup;
    private int pos;
	private boolean is_filter;
	public GroupAdapter(Context context, int res, List<EMGroup> groups,int position) {
		super(context, res, groups);
		this.inflater = LayoutInflater.from(context);
		newGroup = context.getResources().getString(R.string.The_new_group_chat);
		addPublicGroup = context.getResources().getString(R.string.add_public_group_chat);
		this.pos=position;
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return 0;
		} else if (position == 1) {
			return 1;
		} else if (position == 2) {
			return 2;
		} else {
			return 3;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getItemViewType(position) == 0) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.em_search_bar_with_padding, null);
			}
			final EditText query = (EditText) convertView.findViewById(R.id.query);
			final ImageButton clearSearch = (ImageButton) convertView.findViewById(R.id.search_clear);
			query.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (s.length() > 0) {
						is_filter=true;
						clearSearch.setVisibility(View.VISIBLE);
					} else {
						is_filter=false;
						clearSearch.setVisibility(View.INVISIBLE);
					}
					getFilter().filter(s);
				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				public void afterTextChanged(Editable s) {
				}
			});
			clearSearch.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					query.getText().clear();
				}
			});
		} else if (getItemViewType(position) == 1) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.em_row_add_group, null);
			}
			((ImageView) convertView.findViewById(R.id.avatar)).setImageResource(R.drawable.em_create_group);
			((TextView) convertView.findViewById(R.id.name)).setText(newGroup);
		} else if (getItemViewType(position) == 2) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.em_row_add_group, null);
			}
			((ImageView) convertView.findViewById(R.id.avatar)).setImageResource(R.drawable.em_add_public_group);
			((TextView) convertView.findViewById(R.id.name)).setText(addPublicGroup);
			//((TextView) convertView.findViewById(R.id.header)).setVisibility(View.VISIBLE);

		} else {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.em_row_group, null);
			}
			Logger.i("is_filter",is_filter+"");
			if(is_filter)
			{convertView.findViewById(R.id.type).setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.header).setVisibility(View.GONE);
				if(getItem(position - 3).getOwner().equals(EMClient.getInstance().getCurrentUser()))
				((TextView) convertView.findViewById(R.id.type)).setText("我创建的");
            else ((TextView) convertView.findViewById(R.id.type)).setText("我加入的");}else
			{convertView.findViewById(R.id.type).setVisibility(View.GONE);}
			if(position==pos+3)
			{if(!is_filter){(convertView.findViewById(R.id.header)).setVisibility(View.VISIBLE);
				((TextView) convertView.findViewById(R.id.header)).setText("我加入的群("+(getCount()-3-pos)+")");}
			else convertView.findViewById(R.id.header).setVisibility(View.GONE);}
			if(position==3&&pos>0)
			{if(!is_filter){(convertView.findViewById(R.id.header)).setVisibility(View.VISIBLE);
				((TextView) convertView.findViewById(R.id.header)).setText("我创建的群("+pos+")");}
				else convertView.findViewById(R.id.header).setVisibility(View.GONE);
			}
			((TextView) convertView.findViewById(R.id.name)).setText(getItem(position - 3).getGroupName()
			);
		}

		return convertView;
	}

	@Override
	public int getCount() {
		return super.getCount() + 3;
	}

}
