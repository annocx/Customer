package com.haier.cabinet.customer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.cleveroad.slidingtutorial.PageFragment;
import com.cleveroad.slidingtutorial.TransformItem;
import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.event.MainUIEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/4/1.
 */
public class FourthCustomPageFragment  extends PageFragment {

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_page_fourth;
    }

    @Override
    protected TransformItem[] provideTransformItems() {
        return new TransformItem[]{
                new TransformItem(R.id.ivFourthImage, true, 20)
        };
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView ivFourthImage = (ImageView) view.findViewById(R.id.ivFourthImage);
        ivFourthImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushApplication.getInstance().setProperty("user.isFirstOpenApp", "customer");
                EventBus.getDefault().post(new MainUIEvent(-1));
            }
        });
    }
}
