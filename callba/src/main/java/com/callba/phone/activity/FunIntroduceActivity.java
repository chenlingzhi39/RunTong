package com.callba.phone.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.callba.R;
import com.callba.phone.BaseActivity;
import com.callba.phone.annotation.ActivityFragmentInject;

@ActivityFragmentInject(
		contentViewId = R.layout.whats_new
)
public class FunIntroduceActivity extends BaseActivity implements OnPageChangeListener{
	private ViewPager mViewPager;
	private List<View> dots;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mViewPager = (ViewPager) this.findViewById(R.id.view_pager);
		mViewPager.setAdapter(new MyPagerAdapter());
		mViewPager.addOnPageChangeListener(this);
		
		dots = new ArrayList<View>();
		ImageView iv1 = (ImageView) this.findViewById(R.id.wn_iv1);
		ImageView iv2 = (ImageView) this.findViewById(R.id.wn_iv2);
		ImageView iv3 = (ImageView) this.findViewById(R.id.wn_iv3);
		dots.add(iv1);
		dots.add(iv2);
		dots.add(iv3);
	}

	@Override
	protected void onResume() {
		super.onResume();if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
			);
			window.getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		//当滑动到最后一页时，跳转到登录页面
		if(arg0 == 3) {
			Intent intent = new Intent(this, GuideActivity.class);
			this.startActivity(intent);
			this.finish();
		}else {
			for(int i=0; i<dots.size(); i++) {
				if(arg0 == i) 
					dots.get(i).setBackgroundResource(R.drawable.shape_corner_press);
				else 
					dots.get(i).setBackgroundResource(R.drawable.shape_corner_nor);
			}
		}
	}
	
	class MyPagerAdapter extends PagerAdapter {
		private List<View> views;
		
		public MyPagerAdapter() {
			views = new ArrayList<View>();
			View v1 = new View(FunIntroduceActivity.this);
			View v2 = new View(FunIntroduceActivity.this);
			View v3 = new View(FunIntroduceActivity.this);
			View v4 = new View(FunIntroduceActivity.this);
			
			v1.setBackgroundResource(R.drawable.introduce_bg1);
			v2.setBackgroundResource(R.drawable.introduce_bg2);
			v3.setBackgroundResource(R.drawable.introduce_bg3);
			v3.setClickable(true);
			v3.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(FunIntroduceActivity.this, GuideActivity.class);
					startActivity(intent);
					finish();
				}
			});
			views.add(v1);
			views.add(v2);
			views.add(v3);
			views.add(v4);

		}
		
		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return (arg0 == arg1);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(views.get(position));
			return views.get(position);
		}
		
		
	}

}
