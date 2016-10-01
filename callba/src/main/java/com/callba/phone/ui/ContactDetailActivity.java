package com.callba.phone.ui;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.callba.R;
import com.callba.phone.annotation.ActivityFragmentInject;
import com.callba.phone.bean.ContactMutliNumBean;
import com.callba.phone.manager.ContactsManager;
import com.callba.phone.ui.base.BaseActivity;
import com.callba.phone.util.Logger;
import com.callba.phone.util.ScrimUtil;
import com.callba.phone.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by PC-20160514 on 2016/5/31.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.contact_detail2
)
public class ContactDetailActivity extends BaseActivity {
    @InjectView(R.id.header)
    ImageView image;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.tabs)
    TabLayout tabs;
    @InjectView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout content;
    @InjectView(R.id.appbar)
    AppBarLayout appbar;
    @InjectView(R.id.viewpager)
    ViewPager viewpager;
    @InjectView(R.id.main_content)
    CoordinatorLayout mainContent;
    @InjectView(R.id.shadow)
    View shadow;
    @InjectView(R.id.shadow_reverse)
    View shadowReverse;
    private ContactMutliNumBean bean;
    public Subscription subscription;
    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CAMERA = 3;
    private static final int RESULT_CAMERA_CROP_PATH_RESULT = 4;
    private Uri imageUri;
    private Uri imageCropUri;
    private int width, height;
    private int image_height, image_max_height, hideHeight,toolbarHeight,statusbarHeight;
    private DisplayMetrics displayMetrics;
    private Bitmap resource;
    private CollapsingToolbarLayout.LayoutParams lp;
    private CoordinatorLayout.LayoutParams lp1;
    SimpleFragmentPagerAdapter simpleFragmentPagerAdapter;
    private int index=1;
    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    private State mCurrentState = State.EXPANDED;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        if(Build.VERSION.SDK_INT>=16)
        { shadow.setBackground(
                ScrimUtil.makeCubicGradientScrimDrawable(
                        Color.parseColor("#aa000000"), //颜色
                        8, //渐变层数
                        Gravity.TOP));
        shadowReverse.setBackground(
                ScrimUtil.makeCubicGradientScrimDrawable(
                        Color.parseColor("#aa000000"), //颜色
                        8, //渐变层数
                        Gravity.BOTTOM));}
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        image_height = 250 * (int) displayMetrics.density;
        Logger.i("image_height", image_height + "");
        image_max_height = width;
        toolbarHeight=Utils.getToolbarHeight(this);
        statusbarHeight=Utils.getStatusBarHeight(this);
        bean = (ContactMutliNumBean) getIntent()
                .getSerializableExtra("contact");
        initToolbar();
        simpleFragmentPagerAdapter=new SimpleFragmentPagerAdapter(getSupportFragmentManager(),this);
        viewpager.setAdapter(simpleFragmentPagerAdapter);
        tabs.setupWithViewPager(viewpager);
        viewpager.setCurrentItem(index);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
               index=position;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        content.setTitleEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            );
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(Color.TRANSPARENT);
        }
        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                if (i == 0) {
                    if (mCurrentState != State.EXPANDED) {
                        shadow.setVisibility(View.VISIBLE);
                        shadowReverse.setVisibility(View.VISIBLE);
                    }
                        mCurrentState = State.EXPANDED;


                } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                    if (mCurrentState != State.COLLAPSED) {
                        shadow.setVisibility(View.GONE);
                        shadowReverse.setVisibility(View.GONE);
                    }
                    mCurrentState = State.COLLAPSED;
                } else {
                    if (mCurrentState != State.IDLE) {
                        shadow.setVisibility(View.GONE);
                        shadowReverse.setVisibility(View.GONE);
                    }
                     mCurrentState = State.IDLE;

                }

            }
        });
       subscription= Observable.create(new Observable.OnSubscribe<Bitmap>() {
           @Override
           public void call(Subscriber<? super Bitmap> subscriber) {

               resource=ContactsManager.getAvatar(ContactDetailActivity.this, bean.get_id(),true);
               subscriber.onNext(resource);
           }
       }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Bitmap>() {
           @Override
           public void call(Bitmap bitmap) {
               if (resource != null)
                   setImage();
           }
       });
        //image.setImageBitmap(resource);
        String path = getSDCardPath();
        File file = new File(path + "/temp.jpg");
        imageUri = Uri.fromFile(file);
        File cropFile = new File(getSDCardPath() + "/temp_crop.jpg");
        imageCropUri = Uri.fromFile(cropFile);
    }

    boolean IS_DOWN = true;
    boolean IS_PULL = false;
    boolean IS_RELEASE = false;
    boolean IS_BACK = true;
    int distance=0;
    float yDown=0, dy=0, yMove=0,y,x;

    /**
     * 分发触摸事件给所有注册了MyTouchListener的接口
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        // TODO Auto-generated method stub
        Log.i("touchevent", event.getAction() + "");
        if (mCurrentState == State.EXPANDED && lp != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    yDown = event.getRawY();
                    y = event.getRawY();
                    x = event.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("getx",event.getRawX()-x+"");
                    Log.i("gety",event.getRawY()-y+"");
                    if(event.getRawY()<toolbarHeight+statusbarHeight&&event.getRawX()<toolbarHeight)
                    return super.dispatchTouchEvent(event);
                    if(event.getRawY()>image_height-toolbarHeight)
                    if (Math.abs(event.getRawX() - x) > Math.abs(event.getRawY() - y))
                    {
                        return super.dispatchTouchEvent(event);
                     }

                    if (!IS_RELEASE) {
                        Log.i("hideheight", hideHeight + "");
                        Log.i("distance", distance + "");
                        Log.i("ydown", yDown + "");
                        if (IS_DOWN) yDown = event.getRawY();
                        IS_DOWN = false;
                        if (yMove > 0) dy = event.getRawY() - yMove;
                        Log.i("dy", dy + "");
                        yMove = event.getRawY();
                        Log.i("ymove", yMove + "");
                        distance = (int) (yMove - yDown);
                        if (distance <= 0 && dy < 0) IS_DOWN = true;
                        if (distance <= 0) {
                            IS_PULL = false;
                            return super.dispatchTouchEvent(event);
                        }
                        if (distance / 2 >= -hideHeight) {
                            if (dy > 0) IS_BACK = true;
                            if (dy <= 0 && IS_BACK) {
                                yDown = yMove - 2 * lp.topMargin + 2 * hideHeight;
                                IS_BACK = false;
                            }
                            shadow.setVisibility(View.GONE);
                            shadowReverse.setVisibility(View.GONE);
                            return true;
                        }
                        IS_PULL = true;
                        lp.setMargins(0, (distance / 2) + hideHeight,0, (distance / 2) + hideHeight);
                        lp1.setMargins(0, distance + hideHeight * 2, 0, 0);
                        image.setLayoutParams(lp);
                        //appbar.setLayoutParams(lp1);
                        shadow.setVisibility(View.GONE);
                        shadowReverse.setVisibility(View.GONE);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (IS_PULL) {
                        IS_RELEASE = true;
                        IS_BACK = true;
                        ValueAnimator mAnimator = ValueAnimator.ofInt(lp.topMargin, hideHeight);
                        mAnimator.setDuration(500*(hideHeight-lp.topMargin)/hideHeight);
                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                lp.setMargins(0, (int) animation.getAnimatedValue(), 0, (int) animation.getAnimatedValue());
                                image.setLayoutParams(lp);
                                lp1.setMargins(0, 2 * lp.topMargin, 0, 0);
                                if ((int) animation.getAnimatedValue() <= hideHeight) {
                                    IS_DOWN = true;
                                    IS_PULL = false;
                                    IS_RELEASE = false;
                                    distance = 0;
                                    shadow.setVisibility(View.VISIBLE);
                                    shadowReverse.setVisibility(View.VISIBLE);
                                }
                                //appbar.setLayoutParams(lp1);
                            }

                        });
                        mAnimator.start();
                        viewpager.setAdapter(simpleFragmentPagerAdapter);
                        tabs.setupWithViewPager(viewpager);
                        viewpager.setCurrentItem(index);
                        return true;
                    }
                    break;
            }

        }
        return super.dispatchTouchEvent(event);

    }



    public void setImage() {
        Log.i("bitmap_width", resource.getWidth() + "");
        Log.i("bitmap_height", resource.getHeight() + "");
        Log.i("width", width + "");
        Log.i("height", height + "");
        Log.i("x1", (float) resource.getHeight() / (float) resource.getWidth() + "");
        Log.i("x2", (float) image_height / (float) width + "");
        Log.i("original", resource.getHeight() * width / resource.getWidth() + "");
        if (resource.getHeight() * width / resource.getWidth() <= image_height) {
            //image.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, image_height));
            image.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(CollapsingToolbarLayout.LayoutParams.MATCH_PARENT, image_height));
        }
        if (resource.getHeight() * width / resource.getWidth() > image_height && resource.getHeight() * width / resource.getWidth() <= image_max_height) {
            Log.i("height1", resource.getHeight() * width / resource.getWidth() + "");
            lp = new CollapsingToolbarLayout.LayoutParams(CollapsingToolbarLayout.LayoutParams.MATCH_PARENT, resource.getHeight() * width / resource.getWidth());
            lp.setMargins(0, (image_height - resource.getHeight() * width / resource.getWidth()) / 2, 0, (image_height - resource.getHeight() * width / resource.getWidth()) / 2);
            hideHeight = (image_height - resource.getHeight() * width / resource.getWidth()) / 2;
            image.setLayoutParams(lp);
            lp1 = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, resource.getHeight() * width / resource.getWidth());
            lp1.setMargins(0, image_height - resource.getHeight() * width / resource.getWidth(), 0, 0);
            //appbar.setLayoutParams(lp1);
        }
        if (resource.getHeight() * width / resource.getWidth() > image_max_height) {
            Log.i("height2", resource.getHeight() * width / resource.getWidth() + "");
            lp = new CollapsingToolbarLayout.LayoutParams(CollapsingToolbarLayout.LayoutParams.MATCH_PARENT, image_max_height);
            lp.setMargins(0, (image_height - image_max_height) / 2, 0, (image_height - image_max_height) / 2);
            hideHeight = (image_height - image_max_height) / 2;
            Log.i("hide_height", hideHeight + "");
            image.setLayoutParams(lp);
            lp1 = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, image_max_height);
            lp1.setMargins(0, image_height - image_max_height, 0, 0);
            //appbar.setLayoutParams(lp1);
        }
        image.setImageBitmap(resource);

    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(bean.getDisplayName());


        }
    }

    @OnClick(R.id.header)
    public void onClick() {
        //startActivity(new Intent(ContactDetailActivity.this, MainActivity.class));
        uploadHeadPhoto();
    }

    private void uploadHeadPhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("更改头像");
        builder.setItems(new String[]{"拍照", "相册"},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Intent intent = null;
                                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//action is capture
                                intent.putExtra("return-data", false);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                                intent.putExtra("noFaceDetection", true);
                                startActivityForResult(intent, REQUESTCODE_CAMERA);
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"通话记录", "详细信息"};
        private Context context;

        public SimpleFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;

        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            switch (position) {

                case 0:
                    CalllogFragment calllogFragment = new CalllogFragment();
                    bundle.putSerializable("contact", bean);
                    calllogFragment.setArguments(bundle);
                    return calllogFragment;
                case 1:
                    ContactDetailFragment contactDetailFragment = new ContactDetailFragment();
                    bundle.putSerializable("contact", bean);
                    contactDetailFragment.setArguments(bundle);
                    return contactDetailFragment;
                default:
                    return null;

            }

        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                try{
                    cropImg(Uri.fromFile(new File(Utils.getPath(this,data))));}catch(Exception e){
                    toast("无法找到此图片");
                }
                break;
            case REQUESTCODE_CAMERA:
                cropImg(imageUri);
                break;
            case RESULT_CAMERA_CROP_PATH_RESULT:
                Bundle extras = data.getExtras();
                if (extras != null) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageCropUri));
                        updateAvatar(bitmap);
                        image.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void cropImg(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 700);
        intent.putExtra("outputY", 700);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCropUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, RESULT_CAMERA_CROP_PATH_RESULT);
    }


    public static String getSDCardPath() {
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
        try {
            Process p = run.exec(cmd);// 启动另一个进程来执行命令
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
                if (lineStr.contains("sdcard")
                        && lineStr.contains(".android_secure")) {
                    String[] strArray = lineStr.split(" ");
                    if (strArray != null && strArray.length >= 5) {
                        String result = strArray[1].replace("/.android_secure",
                                "");
                        return result;
                    }
                }
                // 检查命令是否执行失败。
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                }
            }
            inBr.close();
            in.close();
        } catch (Exception e) {

            return Environment.getExternalStorageDirectory().getPath();
        }

        return Environment.getExternalStorageDirectory().getPath();
    }

    public void updateAvatar(Bitmap bit) {
        try {
        Uri rawContactUri = null;
        Cursor rawContactCursor = managedQuery(
                RawContacts.CONTENT_URI,
                new String[]{RawContacts._ID},
                RawContacts.CONTACT_ID + " = " + bean.get_id(),
                null,
                null);
        if (!rawContactCursor.isAfterLast()) {
            rawContactCursor.moveToFirst();
            rawContactUri = RawContacts.CONTENT_URI.buildUpon().appendPath("" + rawContactCursor.getLong(0)).build();
        }
        rawContactCursor.close();

        ByteArrayOutputStream streamy = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.PNG, 0, streamy);
        byte[] photo = streamy.toByteArray();
        ContentValues values = new ContentValues();
        int photoRow = -1;
        String where = Data.RAW_CONTACT_ID + " == " +
                ContentUris.parseId(rawContactUri) + " AND " + Data.MIMETYPE + "=='" +
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
        Cursor cursor = managedQuery(
                Data.CONTENT_URI,
                null,
                where,
                null,
                null);
        int idIdx = cursor.getColumnIndexOrThrow(Data._ID);
        if (cursor.moveToFirst()) {
            photoRow = cursor.getInt(idIdx);
        }
        cursor.close();
        values.put(Data.RAW_CONTACT_ID,
                ContentUris.parseId(rawContactUri));
        values.put(Data.IS_SUPER_PRIMARY, 1);
        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, photo);
        values.put(Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        if (photoRow >= 0) {
            this.getContentResolver().update(
                    Data.CONTENT_URI,
                    values,
                    Data._ID + " = " + photoRow, null);
        } else {
            this.getContentResolver().insert(
                    Data.CONTENT_URI,
                    values);
        }
        resource = bit;
        setImage();}catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,"该联系人已被删除！",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if(resource!=null)resource.recycle();
        super.onDestroy();
    }
}
