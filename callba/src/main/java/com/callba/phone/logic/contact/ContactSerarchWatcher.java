package com.callba.phone.logic.contact;

import java.util.ArrayList;
import java.util.List;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.BaseAdapter;

import com.callba.phone.view.QuickSearchBar;

/**
 * 监听联系人实时搜索
 * 
 * @author Zhang
 */
public class ContactSerarchWatcher implements TextWatcher {
	//ListView填充数据
	private List<ContactEntity> mFilterListContactEntities;
	//搜索的数据源
	private List<ContactEntity> mSearchContactEntities;
	
	private BaseAdapter mListViewAdapter;
	private ContactController mContactController;
	
	private QuickSearchBar mQuickSearchBar;
	
	/**
	 * @param listViewAdapter	listview适配器
	 * @param filterListContactList	ListView填充数据
	 */
	public ContactSerarchWatcher(BaseAdapter listViewAdapter,
				List<ContactEntity> filterListContactList, QuickSearchBar quickSearchBar) {
		super();
		
		this.mListViewAdapter = listViewAdapter;
		this.mFilterListContactEntities = filterListContactList;
		this.mQuickSearchBar = quickSearchBar;
		
		mSearchContactEntities = new ArrayList<ContactEntity>();
		mSearchContactEntities.addAll(filterListContactList);
		

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		List<ContactEntity> contactEntities;
		mContactController = new ContactController();
		if(!s.toString().equals(""))
	     contactEntities = mContactController.searchContact(s.toString(), mSearchContactEntities);
		else  contactEntities= mSearchContactEntities;
		mQuickSearchBar.setListSearchMap(mContactController.getSearchMap());
		
		mFilterListContactEntities.clear();
		mFilterListContactEntities.addAll(contactEntities);
		
		mListViewAdapter.notifyDataSetChanged();
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
}
