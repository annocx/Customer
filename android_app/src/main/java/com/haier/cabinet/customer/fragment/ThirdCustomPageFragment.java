package com.haier.cabinet.customer.fragment;

import com.cleveroad.slidingtutorial.PageFragment;
import com.cleveroad.slidingtutorial.TransformItem;
import com.haier.cabinet.customer.R;

/**
 * Created by Administrator on 2016/4/1.
 */
public class ThirdCustomPageFragment  extends PageFragment {

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_page_third;
    }

    @Override
    protected TransformItem[] provideTransformItems() {
        return new TransformItem[]{
                new TransformItem(R.id.ivThirdImage, true, 20)
        };
    }
}
