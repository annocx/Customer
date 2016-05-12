package com.haier.cabinet.customer.fragment;

import com.cleveroad.slidingtutorial.PageFragment;
import com.cleveroad.slidingtutorial.TransformItem;
import com.haier.cabinet.customer.R;

public class FirstCustomPageFragment extends PageFragment {

	@Override
	protected int getLayoutResId() {
		return R.layout.fragment_page_first;
	}

	@Override
	protected TransformItem[] provideTransformItems() {
		return new TransformItem[]{
				new TransformItem(R.id.ivFirstImage, true, 20)
		};
	}
}
