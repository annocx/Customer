package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haier.cabinet.customer.PushApplication;
import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ProductDetailsActivity;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.entity.Bracket;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.event.ProductEvent;
import com.haier.cabinet.customer.fragment.ShopCartFragment;
import com.haier.cabinet.customer.util.Constant;
import com.haier.cabinet.customer.util.DialogHelper;
import com.haier.cabinet.customer.util.JsonUtil;
import com.haier.cabinet.customer.util.MathUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.common.util.AppToast;
import com.haier.common.util.IntentUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ShopCartSubListAdapter extends BaseAdapter {
    private Context mContext;
    private int index;
    private List<Product> dataList = new ArrayList<Product>();
    private LayoutInflater inflater;
    private Handler mHandler;

    private HashMap<Integer, String> mHashMap = new HashMap<>();

    public ShopCartSubListAdapter(Context context, int index, List<Product> dataList, Handler handler) {
        this.mContext = context;
        this.index = index;
        this.inflater = LayoutInflater.from(mContext);
        this.mHandler = handler;
        this.dataList = dataList;

        for (int i = 0; i < dataList.size(); i++) {
            mHashMap.put(i, dataList.get(i).thumbUrl);
        }
    }

    @Override
    public int getCount() {
        return dataList != null ? dataList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        SpannableString msp = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_my_shopping_cart_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.relative_goods_item = (RelativeLayout) convertView.findViewById(R.id.relative_goods_item);
            viewHolder.thumbImage = (ImageView) convertView.findViewById(R.id.thumb_image);
            viewHolder.titleText = (TextView) convertView.findViewById(R.id.pro_title_text);
            viewHolder.priceText = (TextView) convertView.findViewById(R.id.pro_price_text);
            viewHolder.proTotalPriceText = (TextView) convertView.findViewById(R.id.product_total_price_text);
            viewHolder.countText = (EditText) convertView.findViewById(R.id.pro_count_text);
            viewHolder.selectCbx = (CheckBox) convertView.findViewById(R.id.select_checkbox);
            viewHolder.addBtn = (Button) convertView.findViewById(R.id.addBtn);
            viewHolder.minusBtn = (Button) convertView.findViewById(R.id.minusBtn);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Product product = dataList.get(position);

        viewHolder.selectCbx.setOnCheckedChangeListener(null);
        viewHolder.titleText.setText(""+product.title);
//        if (TextUtils.isEmpty(product.boutique) || product.boutique.equals("null")) {
//            viewHolder.titleText.setText(product.title);
//        } else {
//            msp = new SpannableString("12" + product.title);
//            Drawable drawable = mContext.getResources().getDrawable(R.drawable.bg_boutique);
//            drawable.setBounds(0, 0, drawable.getIntrinsicWidth() + 5, drawable.getIntrinsicHeight() + 5);
//            msp.setSpan(new ImageSpan(drawable), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            viewHolder.titleText.setText(msp);
//        }
        if (product.cid == 99) {//是增值延保产品
            if (PushApplication.getInstance().isAuthenticated()) {//是认证用户
                Bracket bracket = Util.getDiscount(product);
                if (bracket == null) {
                    viewHolder.priceText.setText("￥" + product.discountPrice);//折扣价格
                } else {
                    viewHolder.priceText.setText("￥" + bracket.price);//折扣价格

                }
            } else {//非认证用户
                viewHolder.priceText.setText("￥" + product.discountPrice);//折扣价格
            }

            initMinusBtnState(product, viewHolder.minusBtn);
        } else {
            viewHolder.priceText.setText("￥" + product.discountPrice);//折扣价格
            initMinusBtnState(product, viewHolder.minusBtn);
        }

        viewHolder.proTotalPriceText.setText("￥" + calculatingTotalPrice(product));
        viewHolder.countText.setText(product.count + "");

        final Button reduseBtn = viewHolder.minusBtn;
        if(product.pay_state==0){
            product.isChecked = false;
            viewHolder.selectCbx.setChecked(product.isChecked);
            viewHolder.selectCbx.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    AppToast.showLongText(mContext,"商品已下架或已售完");
                    viewHolder.selectCbx.setChecked(product.isChecked);
                }
            });
        }else{
            viewHolder.selectCbx.setChecked(product.isChecked);
            viewHolder.selectCbx.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    product.isChecked = isChecked;
                    updateList(position, product);
                    mHandler.sendEmptyMessage(ShopCartFragment.REFRESH_VIEW);

                }
            });
        }
        viewHolder.addBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Util.isFastClick()) {
                    return;
                }
                if (product.count >= product.goods_storage) {
                    AppToast.showShortText(mContext, "对不起，库存不足!");
                    return;
                }
                if (product.count<999){
                    product.count++;
                }else {
                    AppToast.showShortText(mContext, "对不起，购买数量已达上限，另请下单!");
                }
                if (!product.isChecked) {
                    product.isChecked = true;
                }
                modifyShopCartData(position,product,true);
//                mHandler.obtainMessage(ShopCartFragment.EDIT_PRODUCT, product).sendToTarget();
            }
        });

        viewHolder.minusBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Util.isFastClick()) {
                    return;
                }

                if (product.cid == 99) {
                    if (product.count > product.minNumber) {
                        product.count--;
                        if (!product.isChecked) {
                            product.isChecked = true;
                        }
                        modifyShopCartData(position,product,false);
//                        mHandler.obtainMessage(ShopCartFragment.EDIT_PRODUCT, product).sendToTarget();
                    }
                } else {
                    if (product.count > 1) {
                        product.count--;
                        if (!product.isChecked) {
                            product.isChecked = true;
                        }
                        modifyShopCartData(position,product,false);
//                        mHandler.obtainMessage(ShopCartFragment.EDIT_PRODUCT, product).sendToTarget();
                    } else if (product.count == 1) {
                        Message msg = new Message();
                        msg.what = ShopCartFragment.DELETE_PRODUCT;
                        Bundle data = new Bundle();
                        data.putInt("shopCardId", product.shopCardId);
                        data.putInt("position", position);
                        data.putInt("index", index);
                        msg.setData(data);
                        mHandler.sendMessage(msg);
                    }
                }
            }
        });
        if (null != mHashMap && mHashMap.containsKey(position)) {
            ImageLoader.getInstance().displayImage(mHashMap.get(position), viewHolder.thumbImage, PushApplication.getInstance().getDefaultOptions());
        }

        return convertView;
    }


    class ViewHolder {
        RelativeLayout relative_goods_item;
        ImageView thumbImage;
        TextView titleText;
        TextView priceText;
        TextView proTotalPriceText;//单品小计
        EditText countText;
        Button addBtn;
        Button minusBtn;
        CheckBox selectCbx;
    }

    private void updateList(int position, Product product) {
        getDataList().set(position, product);
//        initMinusBtnState(product, button);
        notifyDataSetChanged();
    }


    private void initMinusBtnState(Product product, Button button) {
        if (product.cid == 99) {
            if (product.count > product.minNumber) {
                button.setEnabled(true);
            } else {
                button.setEnabled(false);
            }
        }/*else {
            if(product.count > 1){
				button.setEnabled(true);
			}else {
				button.setEnabled(false);
			}
		}*/

    }

    public final List<Product> getDataList() {
        return this.dataList;
    }

    /**
     * 计算单个商品总价
     *
     * @return
     */
    public double calculatingTotalPrice(Product product) {
        double total = 0.00;
        if (product.cid == 99) {
            Bracket bracket = Util.getDiscount(product);
            if (bracket == null) {
                total += MathUtil.mul(product.count, product.discountPrice);
            } else {
                total += MathUtil.mul(product.count, bracket.price);
            }
        } else {
            total += MathUtil.mul(product.count, product.discountPrice);
        }
        //double值保留 2 位小数,使用银行家舍入法
        return MathUtil.round(total, 2, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * @param position
     * @param index
     */
    public void requestDeleteProduct(int position, int index) {
        if (position < getDataList().size()) {
            Product product = getDataList().get(position);
            Message msg = new Message();
            msg.what = ShopCartFragment.DELETE_PRODUCT;
            Bundle data = new Bundle();
            data.putInt("shopCardId", product.shopCardId);
            data.putInt("position", position);
            data.putInt("index", index);
            msg.setData(data);
            mHandler.sendMessage(msg);
        }

    }

    private void modifyShopCartData(final int position,final Product product,final boolean isAdded) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("cart_id", product.shopCardId);
        params.put("quantity", product.count);
        params.put("member_id", PushApplication.getInstance().getUserId());
        client.get(Constant.URL_SHOPPING_CART_MODIFY, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                    DialogHelper.showDialogForLoading(mContext, mContext.getString(R.string.loading), true);
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                DialogHelper.stopProgressDlg();
                if(isAdded){
                    product.count--;
                }else{
                    product.count++;
                }
                updateList(position,product);
                AppToast.showShortText(mContext, "修改购物车数量失败");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                DialogHelper.stopProgressDlg();
                String json = new String(response);
//				Log.d(TAG, "json " + json);
                if (200 == statusCode) {
                    if (1001 == JsonUtil.getStateFromShopServer(json)) {
                        Log.d("wjb", "修改商品数量成功");
                        mHandler.sendEmptyMessage(ShopCartFragment.REFRESH_VIEW);
                        EventBus.getDefault().post(new ProductEvent(product.id, product.count));
                    } else {
                        if(isAdded){
                            product.count--;
                        }else{
                            product.count++;
                        }
                        AppToast.showShortText(mContext, "修改购物车数量失败");
                        Log.d("wjb", "修改商品数量失败");
                    }
                    updateList(position,product);
                }
            }

        });

    }
}
