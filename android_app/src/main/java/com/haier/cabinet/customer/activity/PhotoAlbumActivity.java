package com.haier.cabinet.customer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.AlbumGridViewAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.util.AlbumHelper;
import com.haier.cabinet.customer.util.Bimp;
import com.haier.cabinet.customer.util.ImageBucket;
import com.haier.cabinet.customer.util.ImageItem;
import com.haier.cabinet.customer.util.PublicWay;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个是进入相册显示所有图片的界面
 *
 * @author jinbiao.wu
 */
public class PhotoAlbumActivity extends Activity implements OnClickListener {
    //显示手机里的所有图片的列表控件
    private GridView gridView;
    //当手机里没有图片时，提示用户没有图片的控件
    private TextView tv;
    //gridView的adapter
    private AlbumGridViewAdapter gridImageAdapter;
    private ImageView iv_back;
    //完成按钮
    private Button okButton;

    private Intent intent;

    private Context mContext;
    private ArrayList<ImageItem> dataList;
    private AlbumHelper helper;
    public static List<ImageBucket> contentList;
    public static Bitmap bitmap;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_camera_album);
        mContext = this;
        //注册一个广播，这个广播主要是用于在GalleryActivity进行预览时，防止当所有图片都删除完后，再回到该页面时被取消选中的图片仍处于选中状态
        IntentFilter filter = new IntentFilter("data.broadcast.action");
        registerReceiver(broadcastReceiver, filter);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_product_default);
        init();
        initListener();
        //这个函数主要用来控制预览和完成按钮的状态
        isShowOkBt();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //mContext.unregisterReceiver(this);
            // TODO Auto-generated method stub  
            gridImageAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }

    // 完成按钮的监听
    private class AlbumSendListener implements OnClickListener {
        public void onClick(View v) {
            overridePendingTransition(R.anim.activity_translate_in, R.anim.activity_translate_out);
            finish();
        }
    }

    // 初始化，给一些对象赋值
    private void init() {
        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

        contentList = helper.getImagesBucketList(false);
        dataList = new ArrayList<ImageItem>();
        for (int i = 0; i < contentList.size(); i++) {
            dataList.addAll(contentList.get(i).imageList);
        }

        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        intent = getIntent();
        Bundle bundle = intent.getExtras();
        gridView = (GridView) findViewById(R.id.myGrid);
        gridImageAdapter = new AlbumGridViewAdapter(this, dataList,
                Bimp.tempSelectBitmap);
        gridView.setAdapter(gridImageAdapter);
        tv = (TextView) findViewById(R.id.myText);
        gridView.setEmptyView(tv);
        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setText(getResources().getString(R.string.select) + "(" + Bimp.tempSelectBitmap.size()
                + "/" + PublicWay.num + ")");
    }

    private void initListener() {

        gridImageAdapter
                .setOnItemClickListener(new AlbumGridViewAdapter.OnItemClickListener() {

                    @Override
                    public void onItemClick(final ToggleButton toggleButton,
                                            int position, boolean isChecked, Button chooseBt) {
                        if (Bimp.tempSelectBitmap.size() >= PublicWay.num) {
                            toggleButton.setChecked(false);
                            chooseBt.setVisibility(View.GONE);
                            if (!removeOneData(dataList.get(position))) {
                                Toast.makeText(PhotoAlbumActivity.this, getResources().getString(R.string.only_choose_num),
                                        Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        if (isChecked) {
                            chooseBt.setVisibility(View.VISIBLE);
                            Bimp.tempSelectBitmap.add(dataList.get(position));
                            okButton.setText(getResources().getString(R.string.select) + "(" + Bimp.tempSelectBitmap.size()
                                    + "/" + PublicWay.num + ")");
                        } else {
                            Bimp.tempSelectBitmap.remove(dataList.get(position));
                            chooseBt.setVisibility(View.GONE);
                            okButton.setText(getResources().getString(R.string.select) + "(" + Bimp.tempSelectBitmap.size() + "/" + PublicWay.num + ")");
                        }
                        isShowOkBt();
                    }
                });

        okButton.setOnClickListener(new AlbumSendListener());

    }

    private boolean removeOneData(ImageItem imageItem) {
        if (Bimp.tempSelectBitmap.contains(imageItem)) {
            Bimp.tempSelectBitmap.remove(imageItem);
            okButton.setText(getResources().getString(R.string.select) + "(" + Bimp.tempSelectBitmap.size() + "/" + PublicWay.num + ")");
            return true;
        }
        return false;
    }

    public void isShowOkBt() {
        if (Bimp.tempSelectBitmap.size() > 0) {
            okButton.setText(getResources().getString(R.string.select) + "(" + Bimp.tempSelectBitmap.size() + "/" + PublicWay.num + ")");
            okButton.setTextColor(getResources().getColor(R.color.color_white));
            okButton.setEnabled(true);
            okButton.setClickable(true);
            okButton.setTextColor(Color.WHITE);
        } else {
            okButton.setText(getResources().getString(R.string.select) + "(" + Bimp.tempSelectBitmap.size() + "/" + PublicWay.num + ")");
            okButton.setTextColor(getResources().getColor(R.color.light_grey));
            okButton.setEnabled(false);
            okButton.setClickable(false);
            okButton.setTextColor(Color.parseColor("#E1E0DE"));
        }
    }

    @Override
    protected void onRestart() {
        isShowOkBt();
        super.onRestart();
    }

//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            ResetPhotoDate();
//        }
//        return true;
//    }

//    /**
//     * 默认按返回或者back键时，是不选择照片
//     */
//    private void ResetPhotoDate(){
//        Bimp.tempSelectBitmap.clear();
//        Bimp.max = 0;
//        Intent intent = new Intent("data.broadcast.action");
//        sendBroadcast(intent);
//        finish();
//    }
}
