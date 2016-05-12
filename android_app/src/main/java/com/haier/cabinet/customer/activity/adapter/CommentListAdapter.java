package com.haier.cabinet.customer.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.haier.cabinet.customer.R;
import com.haier.cabinet.customer.activity.ImagePagerActivity;
import com.haier.cabinet.customer.entity.BCommentList;
import com.haier.cabinet.customer.util.DateUtil;
import com.haier.cabinet.customer.view.MyGridView;
import com.haier.common.widget.CircleImageView;
import com.hp.hpl.sparta.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论列表适配
 */
public class CommentListAdapter extends RecyclerView.Adapter {

    private Context context;
    private LayoutInflater inflater;
    private List<BCommentList> commentLists;
//    private PopupWindow pop = null;

    public CommentListAdapter(Context context, List<BCommentList> list) {
        this.context = context;
        this.commentLists = list;
        this.inflater = LayoutInflater.from(context);
//        pop = new PopupWindow(context);
//        View view = inflater.inflate(R.layout.activity_image_page, null);
//        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
//        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        pop.setBackgroundDrawable(new BitmapDrawable());
//        pop.setFocusable(true);
//        pop.setOutsideTouchable(true);
//        pop.setContentView(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.layout_comment_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;

        if (!commentLists.get(position).geval_frommemberid.equals("") && commentLists.get(position).geval_frommemberid.length() == 11) {
            viewHolder.tv_phone.setText(commentLists.get(position).geval_frommemberid.substring(0, 3) + "****" +
                    commentLists.get(position).geval_frommemberid.substring(7, 11));
        }else{
            viewHolder.tv_phone.setText("微信用户");
        }

        viewHolder.tv_comment_content.setText(commentLists.get(position).geval_content);
        viewHolder.tv_comment_time.setText(DateUtil.getDateToString(commentLists.get(position).geval_addtime));
        if(!TextUtils.isEmpty(commentLists.get(position).geval_explain)){
            viewHolder.tv_recomment_content.setVisibility(View.VISIBLE);
            viewHolder.tv_recomment_content.setText(String.format(context.getString(R.string.platform_reply),commentLists.get(position).geval_explain));
        }else{
            viewHolder.tv_recomment_content.setVisibility(View.GONE);
        }
//        if (commentLists.get(position).img_list.size() != 0) {
//            viewHolder.gv_img_lable.setVisibility(View.VISIBLE);
//            PhotoGridAdapter2 adapter2 = new PhotoGridAdapter2(context, commentLists.get(position).img_list);
//            viewHolder.gv_img_lable.setAdapter(adapter2);
//            viewHolder.gv_img_lable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
//                    Intent intent = new Intent(context,
//                            ImagePagerActivity.class);
//                    intent.putStringArrayListExtra(ImagePagerActivity.COMMENT_LIST_DATA, (ArrayList<String>) commentLists.get(position).img_list);
//                    intent.putExtra(ImagePagerActivity.STATE_POSITION, pos);
//                    context.startActivity(intent);
//                    ((Activity)context).overridePendingTransition(R.anim.activity_translate_in, R.anim.activity_translate_out);
//                }
//            });
//        } else {
//            viewHolder.gv_img_lable.setVisibility(View.GONE);
//        }
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void addAll(ArrayList<BCommentList> list) {
        int lastIndex = this.commentLists.size();
        if (this.commentLists.addAll(list)) {
            notifyItemRangeInserted(lastIndex, list.size());
        }
    }

    public void clear() {
        if (commentLists.size() > 0) {
            commentLists.clear();
        }
    }

    @Override
    public int getItemCount() {
        return commentLists != null ? commentLists.size() : 0;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView business_avtar_image;
        private TextView tv_phone, tv_comment_content, tv_comment_time;
        private LinearLayout linear_recontent;//平台回复布局
        private TextView tv_reshop_name;//名称
        private TextView tv_recomment_content;//回复内容
        private MyGridView gv_img_lable;

        public ViewHolder(View itemView) {
            super(itemView);
            business_avtar_image = (CircleImageView) itemView.findViewById(R.id.business_avtar_image);
            tv_phone = (TextView) itemView.findViewById(R.id.tv_phone);
            tv_comment_content = (TextView) itemView.findViewById(R.id.tv_comment_content);
            tv_comment_time = (TextView) itemView.findViewById(R.id.tv_comment_time);
            gv_img_lable = (MyGridView) itemView.findViewById(R.id.gv_img_lable);
            tv_recomment_content = (TextView) itemView.findViewById(R.id.tv_recomment_content);
        }
    }

}
