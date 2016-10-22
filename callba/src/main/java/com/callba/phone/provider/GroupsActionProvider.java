package com.callba.phone.provider;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.callba.R;
import com.callba.phone.ui.NewGroupActivity;
import com.callba.phone.ui.PublicGroupsActivity;

/**
 * Created by PC-20160514 on 2016/10/20.
 */

public class GroupsActionProvider extends ActionProvider {
    Context context;

    public GroupsActionProvider(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public View onCreateActionView() {
        return null;
    }
    @Override
    public void onPrepareSubMenu(SubMenu subMenu) {
        subMenu.clear();
        subMenu.add(context.getString(R.string.The_new_group_chat))
                .setIcon(R.drawable.em_create_group)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        context.startActivity(new Intent(context,NewGroupActivity.class));
                        return true;
                    }
                });
        subMenu.add(context.getString(R.string.add_public_group_chat))
                .setIcon(R.drawable.em_add_public_group)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        context.startActivity(new Intent(context, PublicGroupsActivity.class));
                        return true;
                    }
                });
    }
    @Override
    public boolean hasSubMenu() {
        return true;
    }
}
