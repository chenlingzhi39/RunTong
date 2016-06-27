package com.callba.phone.activity.more;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.cfg.CalldaGlobalConfig;
import com.callba.phone.view.CornerListView;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.CircleShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.weixin.controller.UMWXHandler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

@ActivityFragmentInject(
        contentViewId = R.layout.more_share,
        navigationId = R.drawable.press_back,
        toolbarTitle = R.string.share
)
public class ShareActivity extends BaseActivity implements
        OnItemClickListener {
    @InjectView(R.id.more_list_share)
    CornerListView lv_share;
    private UMSocialService controller;
    private String shareContent = "";
    // String appid = "wx768039c4858b912b";
    String appid = "wx768039c4858b912b";
    String appSecret = "6a65d0e4f4d731990c7d737c9683d23c";
    String contenturl = "http://wap.callba.cn/";
    private Context context = ShareActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        lv_share.setAdapter(new SimpleAdapter(this, getData1(),
                R.layout.more_list_item, new String[]{"icon", "text"},
                new int[]{R.id.iv_more_item_icon, R.id.tv_more_item_text}));
        lv_share.setOnItemClickListener(this);

        shareContent = getString(R.string.share_content)
                + CalldaGlobalConfig.getInstance().getUsername();
       controller = UMServiceFactory.getUMSocialService("com.umeng.share",
                RequestType.SOCIAL);
        // controller.getConfig().setSinaSsoHandler(new SinaSsoHandler());
       controller.setShareMedia(new UMImage(this, R.drawable.logo));
       controller.setShareContent(shareContent);
        contenturl = contenturl
                + CalldaGlobalConfig.getInstance().getUsername();
    }

    private List<? extends Map<String, ?>> getData1() {
        List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("icon", R.drawable.list_icon_6);
        map1.put("text", getString(R.string.share_wxpyq));
        lists.add(map1);

        map1 = new HashMap<String, Object>();
        map1.put("icon", R.drawable.list_icon_1);
        map1.put("text", getString(R.string.share_xlwb));
        lists.add(map1);

        map1 = new HashMap<String, Object>();
        map1.put("icon", R.drawable.list_icon_2);
        map1.put("text", getString(R.string.share_txwb));
        lists.add(map1);

        map1 = new HashMap<String, Object>();
        map1.put("icon", R.drawable.list_icon_3);
        map1.put("text", getString(R.string.share_renren));
        lists.add(map1);

        map1 = new HashMap<String, Object>();
        map1.put("icon", R.drawable.logo_select);
        map1.put("text", getString(R.string.share_fxgd));
        lists.add(map1);

        return lists;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        switch (position) {
            case 0:
                // 微信朋友圈
                //shareWXFriend();
			controller.getConfig()
					.supportWXCirclePlatform(this, appid, contenturl)
					.setCircleTitle(getString(R.string.share_title));
			controller.directShare(context, SHARE_MEDIA.WEIXIN_CIRCLE,
					new SocializeListeners.SnsPostListener() {

						@Override
						public void onStart() {
						}

						@Override
						public void onComplete(SHARE_MEDIA arg0, int arg1,
								SocializeEntity arg2) {
							if (arg1 == 200) {
								// calldaToast.showToast(context,
								// R.string.share_success);
							} else {
                                toast(R.string.share_failed);
							}

						}
					});
                break;

            case 4:
                // 微信好友
                String title = getString(R.string.share_title);
                String content = shareContent;

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");

                intent.putExtra(Intent.EXTRA_SUBJECT, title);
                intent.putExtra(Intent.EXTRA_TEXT, content);
                startActivity(intent);

                break;

            case 1:
                // sina 微博
                controller.directShare(this, SHARE_MEDIA.SINA, null);
                // controller.getConfig().setSsoHandler(new SinaSsoHandler());
                break;

            case 2:
                // 腾讯微博
                controller.directShare(this, SHARE_MEDIA.TENCENT, null);
                // controller.getConfig().setSsoHandler(new TencentWBSsoHandler());
                break;

            case 3:
                // 人人网
               controller.directShare(this, SHARE_MEDIA.RENREN, null);
                break;

            default:
                break;
        }
    }

    private void shareWXFriend() {
        UMWXHandler wxCircleHandler = new UMWXHandler(ShareActivity.this,
                appid, appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
        // 设置微信朋友圈分享内容
        CircleShareContent circleMedia = new CircleShareContent();
        circleMedia.setShareContent(shareContent);
        // 设置朋友圈title
        circleMedia.setTitle(getString(R.string.share_title));
        circleMedia.setShareImage(new UMImage(this, R.drawable.logo));
        circleMedia.setTargetUrl(contenturl);
        controller.setShareMedia(circleMedia);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 使用SSO必须添加，指定获取授权信息的回调页面，并传给SDK进行处理
         */
        // UMSsoHandler sinaSsoHandler = controller.getConfig()
        // .getSinaSsoHandler();
        // if (sinaSsoHandler != null
        // && requestCode == UMSsoHandler.DEFAULT_AUTH_ACTIVITY_CODE) {
        // sinaSsoHandler.authorizeCallBack(requestCode, resultCode, data);
     /*   UMSsoHandler ssoHandler = controller.getConfig().getSsoHandler(
                requestCode);
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }*/

        // }
    }


    @Override
    public void refresh(Object... params) {
        // TODO Auto-generated method stub

    }
}
