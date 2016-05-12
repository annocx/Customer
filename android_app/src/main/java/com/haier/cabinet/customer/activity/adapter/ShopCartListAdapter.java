package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ProductDetailsActivity;
import com.haier.cabinet.customer.activity.SortResultActivity;
import com.haier.cabinet.customer.entity.Bracket;
import com.haier.cabinet.customer.entity.Product;
import com.haier.cabinet.customer.entity.ShopCartItem;
import com.haier.cabinet.customer.util.MathUtil;
import com.haier.cabinet.customer.util.Util;
import com.haier.cabinet.customer.viewholder.ShopCartViewHolder;
import com.haier.common.util.IntentUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShopCartListAdapter extends RecyclerView.Adapter {

    private Context context = null;
    private Handler handler;
    private ArrayList<ShopCartItem> dataList = new ArrayList<>();

    public ShopCartListAdapter(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_shopping_cart_list_item, parent, false);
        return new ShopCartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int index) {
        ShopCartViewHolder viewHolder = (ShopCartViewHolder) holder;
        ShopCartItem cartItem = dataList.get(index);
        viewHolder.shopNameText.setText(cartItem.shop.name);
        final ShopCartSubListAdapter adapter = new ShopCartSubListAdapter(context, index, cartItem.products, handler);
        viewHolder.listView.setAdapter(adapter);
        viewHolder.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putInt(SortResultActivity.RESULT_ID, dataList.get(index).products.get(position).id);
                IntentUtil.startActivity((Activity) context, ProductDetailsActivity.class, bundle);
            }
        });
        viewHolder.listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int position, long arg3) {

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setMessage("确认要删除该商品吗?")
                        .setPositiveButton("确定",
                                new AlertDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        adapter.requestDeleteProduct(position, index);

                                    }
                                })
                        .setNegativeButton("取消",
                                new AlertDialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                    }
                                }).create();
                // 显示对话框
                dialog.show();

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setDataList(Collection<ShopCartItem> list) {
        this.dataList.clear();
        this.dataList.addAll(list);
        notifyDataSetChanged();
    }

    public void addAll(Collection<ShopCartItem> list) {
        int lastIndex = this.dataList.size();
        if (this.dataList.addAll(list)) {
            notifyItemRangeInserted(lastIndex, list.size());
        }
    }

    public void clear() {
        this.dataList.clear();
        notifyDataSetChanged();
    }

    public double calculatingTotalPrice() {
        double total = 0.00;
        if (getItemCount() > 0) {
            for (ShopCartItem cartItem : dataList) {
                for (Product product : cartItem.products) {
                    if (product.isChecked) {
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
                    }

                }
            }
        }

        //double值保留 2 位小数,使用银行家舍入法
        return MathUtil.round(total, 2, BigDecimal.ROUND_HALF_EVEN);
    }

    public void setSelectedAll(boolean isChecked) {
        for (ShopCartItem cartItem : dataList) {
            for (Product product : cartItem.products) {
                if (product.pay_state != 0) {
                    product.isChecked = isChecked;
                }
            }
        }
        notifyDataSetChanged();
    }

    public boolean isSelectedAll() {
        if (0 == dataList.size()) {
            return false;
        }
        for (ShopCartItem cartItem : dataList) {
            for (Product product : cartItem.products) {
                if (!product.isChecked)
                    return false;
            }
        }

        return true;
    }

    public List<ShopCartItem> getSelectedProducts() {
        List<ShopCartItem> cartItems = new ArrayList<>();
        for (ShopCartItem cartItem : dataList) {
            ShopCartItem shopCartItem = new ShopCartItem();
            shopCartItem.shop = cartItem.shop;
            for (Product product : cartItem.products) {

                if (product.isChecked) {
                    shopCartItem.products.add(product);
                }
            }
            if (shopCartItem.products.size() > 0) {
                cartItems.add(shopCartItem);
            }
        }
        return cartItems;
    }

    public int getShopCartNumber() {
        int total = 0;
        for (ShopCartItem cartItem : dataList) {
            total += cartItem.products.size();
        }
        Log.d("lzx", "getShopCartNumber total = " + total);
        return total;
    }

    public ArrayList getShopCartDelete() {
        ArrayList id = new ArrayList();
        for (ShopCartItem cartItem : dataList) {
            for (Product product : cartItem.products) {
                if (product.isChecked) {
                    id.add(product.shopCardId);
                }
            }
        }
        return id;
    }

    /**
     * 删除购物车商品
     *
     * @param index    注意：购物车列表索引
     * @param position 注意：店铺商品列表
     */

    public void deleteProduct(int index, int position, RecyclerView recyclerView) {
        if (index < dataList.size()) {
            ShopCartItem cartItem = dataList.get(index);
            int firstItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            View view = recyclerView.getChildAt(index - firstItemPosition);
            ShopCartViewHolder viewHolder = (ShopCartViewHolder) recyclerView.getChildViewHolder(view);
            ShopCartSubListAdapter adapter = (ShopCartSubListAdapter) viewHolder.listView.getAdapter();
            if (adapter.getDataList().size() > position) {
                adapter.getDataList().remove(position);
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyDataSetChanged();
            }

            notifyItemChanged(index);
            deleteShopCart(cartItem, index);//can't use position
        } else {
            notifyDataSetChanged();
        }

    }

    private void deleteShopCart(ShopCartItem cartItem, int position) {
        if (cartItem.products.size() == 0) {
            dataList.remove(position);
            notifyDataSetChanged();// don't modify
        }

    }
}
