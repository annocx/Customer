package com.haier.cabinet.customer.fragment;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;

import com.cleveroad.slidingtutorial.PageFragment;
import com.cleveroad.slidingtutorial.PresentationPagerFragment;
import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.event.MainUIEvent;

import de.greenrobot.event.EventBus;

public class CustomPresentationPagerFragment extends PresentationPagerFragment {

	@Override
	public int getLayoutResId() {
		return R.layout.fragment_presentation;
	}

	@Override
	public int getViewPagerResId() {
		return R.id.viewPager;
	}

	@Override
	public int getIndicatorResId() {
		return R.id.indicator;
	}

	@Override
	public int getButtonSkipResId() {
		return R.id.tvSkip;
	}

	@Override
	protected int getPagesCount() {
		return 4;
	}

	@Override
	protected PageFragment getPage(int position) {
		if (position == 0)
			return new FirstCustomPageFragment();
		if (position == 1)
			return new SecondCustomPageFragment();
		if (position == 2)
			return new ThirdCustomPageFragment();
		if (position == 3)
			return new FourthCustomPageFragment();
		throw new IllegalArgumentException("Unknown position: " + position);
	}

	@ColorInt
	@Override
	protected int getPageColor(int position) {
		if (position == 0)
			return ContextCompat.getColor(getContext(), R.color.color_white);
		if (position == 1)
			return ContextCompat.getColor(getContext(), R.color.color_white);
		if (position == 2)
			return ContextCompat.getColor(getContext(), R.color.color_white);
		if (position == 3)
			return ContextCompat.getColor(getContext(), R.color.color_white);
		return Color.TRANSPARENT;
	}

	@Override
	protected boolean isInfiniteScrollEnabled() {
		return false;
	}

	@Override
	protected void goToNextPage() {
		PushApplication.getInstance().setProperty("user.isFirstOpenApp", "customer");
		EventBus.getDefault().post(new MainUIEvent(-1));
	}
}
