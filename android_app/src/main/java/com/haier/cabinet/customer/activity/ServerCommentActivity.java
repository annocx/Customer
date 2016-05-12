package com.haier.cabinet.customer.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.adapter.PhotoGridAdapter;
import com.haier.cabinet.customer.activity.adapter.ServerCommentKeyAdapter;
import com.haier.cabinet.customer.base.BaseActivity;
import com.haier.cabinet.customer.event.OrderEvent;
import com.haier.cabinet.customer.util.Bimp;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.FileUtils;
import com.haier.cabinet.customer.util.ImageItem;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.view.MyGridView;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnItemClick;
import de.greenrobot.event.EventBus;

/**
 * 服务评价类
 * Created by Administrator on 2015/11/26.
 */
public class ServerCommentActivity extends BaseActivity implements RatingBar.OnRatingBarChangeListener, AdapterView.OnItemClickListener, TextWatcher {

    private String mOrderNo;//商品id

    @Bind(R.id.et_comment)
    EditText et_comment;

    @Bind(R.id.tv_char_count)
    TextView tv_char_count;//剩余字数

    @Bind(R.id.gv_lable)
    MyGridView gv_lable;

    @Bind(R.id.rb_product)
    RatingBar rb_product;//产品品质
    @Bind(R.id.rb_logistics)
    RatingBar rb_logistics;//物流服务
    @Bind(R.id.rb_server)
    RatingBar rb_server;//服务态度

    @Bind(R.id.btn_send_comment)
    Button btn_send_comment;//发表评论
    @Bind(R.id.linear_normal)
    LinearLayout linear_normal;//评论默认布局
    @Bind(R.id.linear_result)
    LinearLayout linear_result;//评论结果布局

    @Bind(R.id.iv_comment_result)
    ImageView iv_comment_result;//评论结果图片

    @Bind(R.id.tv_comment_result)
    TextView tv_comment_result;//评论结果说明

    @Bind(R.id.btn_again)
    Button btn_again;//重新评论
    @Bind(R.id.btn_back)
    Button btn_back;//回到我的订单
    //拍照
    @Bind(R.id.noScrollgridview)
    MyGridView noScrollgridview;
    private ServerCommentKeyAdapter mAdapter;

    private ArrayList<String> data = new ArrayList<String>();//快捷按钮假数据

    InputMethodManager imm;
    private PhotoGridAdapter adapter;
    private View parentView;
    private PopupWindow pop = null;
    private LinearLayout ll_popup;
    public static Bitmap bimap;
    RelativeLayout parent;
    private Button item_popupwindows_camera, item_popupwindows_Photo, item_popupwindows_cancel;
    StringBuilder sb_imags;//返回图片名称，进行拼接

    private static final int ADD_DATA = 1001;//提交数据
    private static final int UPDATE_DATA = 1002;//数据成功
    private static final int NO_DATA = 1003;//数据失败
    private static final int ADD_IMAGE_DATA = 1004;//提交照片
    private static final int UPDATE_IMAGE_DATA = 1005;//提交照片成功

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_IMAGE_DATA:
                    for (int i = 0; i < Bimp.tempSelectBitmap.size(); i++) {
                        UploadImages(i, Bimp.tempSelectBitmap.get(i).getImagePath());
                    }
                    break;
                case UPDATE_IMAGE_DATA:
                    break;
                case ADD_DATA:
                    addCommentData(getIntent().getIntExtra("position", 0));
                    break;
                case UPDATE_DATA:
                    setResultView(true);
                    break;
                case NO_DATA:
                    setResultView(false);
                    break;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_server_comment;
    }

    public void initView() {
        mOrderNo = getIntent().getStringExtra("orderNo");
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mTitleText.setText("服务评价");
        mBackBtn.setVisibility(View.VISIBLE);
        iv_comment_result.setImageBitmap(Util.readBitMap(ServerCommentActivity.this, R.drawable.ic_comment_success));
        parentView = getLayoutInflater().inflate(R.layout.activity_server_comment, null);
        pop = new PopupWindow(ServerCommentActivity.this);
        View view = getLayoutInflater().inflate(R.layout.item_popupwindows, null);
        ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);

        parent = (RelativeLayout) view.findViewById(R.id.parent);
        item_popupwindows_camera = (Button) view
                .findViewById(R.id.item_popupwindows_camera);
        item_popupwindows_Photo = (Button) view
                .findViewById(R.id.item_popupwindows_Photo);
        item_popupwindows_cancel = (Button) view
                .findViewById(R.id.item_popupwindows_cancel);
        parent.setOnClickListener(this);
        item_popupwindows_camera.setOnClickListener(this);
        item_popupwindows_Photo.setOnClickListener(this);
        item_popupwindows_cancel.setOnClickListener(this);
        noScrollgridview = (MyGridView) findViewById(R.id.noScrollgridview);
        noScrollgridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
        adapter = new PhotoGridAdapter(this);
        adapter.notifyDataSetChanged();
        noScrollgridview.setAdapter(adapter);
        InitListener();
    }

    /**
     * 监听事件
     */
    private void InitListener() {
        et_comment.addTextChangedListener(this);
        rb_product.setOnRatingBarChangeListener(this);
        rb_logistics.setOnRatingBarChangeListener(this);
        rb_server.setOnRatingBarChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    @OnClick({R.id.back_img, R.id.btn_send_comment, R.id.btn_again, R.id.btn_back})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_img:
                finish();
                break;
            case R.id.btn_send_comment:
                sb_imags = new StringBuilder();
                if (TextUtils.isEmpty(et_comment.getText().toString())) {
                    AppToast.makeToast(ServerCommentActivity.this, "请输入评论内容");
                    return;
                }
                if (product_level == 0) {
                    AppToast.showShortText(ServerCommentActivity.this, "请对产品评价进行打分");
                    return;
                }
                if (logistics_level == 0) {
                    AppToast.showShortText(ServerCommentActivity.this, "请对物流服务进行打分");
                    return;
                }
                if (server_level == 0) {
                    AppToast.showShortText(ServerCommentActivity.this, "请对服务态度进行打分");
                    return;
                }
                mHandler.sendEmptyMessage(ADD_DATA);
//                if (Bimp.tempSelectBitmap.size() == 0) {
//                    mHandler.sendEmptyMessage(ADD_DATA);
//                } else {
//                    mHandler.sendEmptyMessage(ADD_IMAGE_DATA);
//                }
                break;
            case R.id.btn_again:
                linear_result.setVisibility(View.GONE);
                linear_normal.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_back:
                IntentUtil.startActivity(ServerCommentActivity.this, UserOrderListActivity.class);
                finish();
                break;
            case R.id.parent:
            case R.id.item_popupwindows_cancel:
                pop.dismiss();
                ll_popup.clearAnimation();
                break;
            case R.id.item_popupwindows_Photo:
                Intent intent = new Intent(ServerCommentActivity.this,
                        PhotoAlbumActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_translate_in, R.anim.activity_translate_out);
                pop.dismiss();
                ll_popup.clearAnimation();
                break;
            case R.id.item_popupwindows_camera:
                photo();
                pop.dismiss();
                ll_popup.clearAnimation();
                break;
        }
    }

    /**
     * 显示评价结果布局
     *
     * @param isSuccess 是否评价成功
     */
    private void setResultView(boolean isSuccess) {
        linear_normal.setVisibility(View.GONE);
        linear_result.setVisibility(View.VISIBLE);
        if (isSuccess) {
            btn_again.setVisibility(View.GONE);
            iv_comment_result.setImageDrawable(getResources().getDrawable(R.drawable.ic_comment_success));
            tv_comment_result.setText(getResources().getString(R.string.comment_success));
        } else {
            btn_again.setVisibility(View.VISIBLE);
            iv_comment_result.setImageDrawable(getResources().getDrawable(R.drawable.ic_comment_fail));
            tv_comment_result.setText(getResources().getString(R.string.comment_fail));
        }
    }

    private int product_level = 5, logistics_level = 5, server_level = 5;

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        switch (ratingBar.getId()) {
            case R.id.rb_product:
                product_level = (int) rating;
                break;
            case R.id.rb_logistics:
                logistics_level = (int) rating;
                break;
            case R.id.rb_server:
                server_level = (int) rating;
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        et_comment.setSelection(s.toString().length());
    }

    @Override
    public void afterTextChanged(Editable s) {
        tv_char_count.setText("(剩余"
                + (140 - s.toString().length())
                + "字数)");
    }

    /**
     * 热门搜索假数据
     */
    public void initData() {
        data.add("物流给力");
        data.add("特产正宗");
        data.add("服务nice");
        data.add("继续加油");
        data.add("质量一般");
        data.add("物流太怂");
        mAdapter = new ServerCommentKeyAdapter(ServerCommentActivity.this, data);
        gv_lable.setAdapter(mAdapter);
    }

    @Override
    @OnItemClick({R.id.gv_lable, R.id.noScrollgridview})
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.gv_lable:
                String str_input = et_comment.getText().toString();
                if (TextUtils.isEmpty(str_input)) {
                    et_comment.setText(data.get(position).toString());
                } else {
                    et_comment.setText(str_input + "," + data.get(position).toString());
                }
                break;
            case R.id.noScrollgridview:
                imm.hideSoftInputFromWindow(et_comment.getWindowToken(), 0); //强制隐藏键盘
                if (position == Bimp.tempSelectBitmap.size()) {
                    ll_popup.startAnimation(AnimationUtils.loadAnimation(ServerCommentActivity.this, R.anim.activity_translate_in));
                    pop.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
                } else {
                    Intent intent = new Intent(ServerCommentActivity.this,
                            PhotoGalleryActivity.class);
                    intent.putExtra("position", "1");
                    intent.putExtra("ID", position);
                    startActivity(intent);
                }
                break;
        }
    }

    /**
     * 提交评价
     *
     * @param position
     */
    private void addCommentData(final int position) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("order_id", mOrderNo);
        params.add("member_id", PushApplication.getInstance().getUserId());
        params.add("anony", "0");//是否匿名
        params.add("store_desccredit", "" + product_level);//描述相符评分
        params.add("store_servicecredit", "" + server_level);//服务态度评分
        params.add("store_deliverycredit", "" + logistics_level);//发货速度评分
        params.add("comment", et_comment.getText().toString());
        if (!TextUtils.isEmpty(sb_imags.toString())) {
            params.add("imgs", sb_imags.toString().substring(0, sb_imags.length() - 1).toString());
        } else {
            params.add("imgs", "");
        }
        client.post(Constant.URL_COMMENT_ADD, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(ServerCommentActivity.this, getString(R.string.loading), true);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                DialogHelper.stopProgressDlg();
                mHandler.sendEmptyMessage(NO_DATA);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            mHandler.sendEmptyMessage(UPDATE_DATA);
                            EventBus.getDefault().post(new OrderEvent(position, getIntent().getIntExtra("type", 2)));
                            break;
                        case 1002:
                        case 2001:
                            mHandler.sendEmptyMessage(NO_DATA);
                            break;
                        default:
                            break;
                    }

                }
            }
        });
    }

    /**
     * 上传图片接口
     *
     * @param imagePath
     */
    private void UploadImages(final int step_num, final String imagePath) {
        File file = new File(imagePath);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("goods_id", mOrderNo);
        try {
            params.put("img", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.post(Constant.URL_COMMENT_IMAGE_ADD, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                if (!isFinishing()) {
                    DialogHelper.showDialogForLoading(ServerCommentActivity.this, getString(R.string.loading), true);
                }
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                DialogHelper.stopProgressDlg();
                if (step_num == Bimp.tempSelectBitmap.size() - 1) {
                    mHandler.sendEmptyMessage(ADD_DATA);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
                if (200 == statusCode) {
                    switch (JsonUtil.getStateFromShopServer(json)) {
                        case 1001:
                            try {
                                JSONObject jsonObject = new JSONObject(json);
                                sb_imags.append(jsonObject.getString("result") + ",");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 1002:
                        case 2001:
                            break;
                        default:
                            break;
                    }

                }
                if (step_num == Bimp.tempSelectBitmap.size() - 1) {
                    mHandler.sendEmptyMessage(ADD_DATA);
                }
            }
        });
    }


    protected void onRestart() {
        adapter.notifyDataSetChanged();
        super.onRestart();
    }

    private static final int TAKE_PICTURE = 0x000001;

    public void photo() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(openCameraIntent, TAKE_PICTURE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PICTURE:
                if (Bimp.tempSelectBitmap.size() < 3 && resultCode == RESULT_OK) {

                    String fileName = String.valueOf(System.currentTimeMillis());
                    Bitmap bm = (Bitmap) data.getExtras().get("data");
                    FileUtils.saveBitmap(bm, fileName);

                    ImageItem takePhoto = new ImageItem();
                    takePhoto.setBitmap(bm);
                    takePhoto.setImagePath(FileUtils.getFilePath(fileName));
                    Bimp.tempSelectBitmap.add(takePhoto);
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        iv_comment_result.setImageResource(0);
        Bimp.tempSelectBitmap.clear();
        Bimp.max = 0;
        Intent intent = new Intent("data.broadcast.action");
        sendBroadcast(intent);
        System.gc();
    }
}
