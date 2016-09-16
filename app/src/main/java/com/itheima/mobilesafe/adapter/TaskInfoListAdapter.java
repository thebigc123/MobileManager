package com.itheima.mobilesafe.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.ui.recycler_view.OnItemTouch;
import com.itheima.mobilesafe.utils.SystemInfoUtils;
import com.itheima.mobilesafe.utils.objects.TaskInfo;

import java.util.Collections;
import java.util.List;

/**
 * Created by Catherine on 2016/8/19.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */
public class TaskInfoListAdapter extends RecyclerView.Adapter<TaskInfoListAdapter.MyViewHolder> implements OnItemTouch {

    private Context ctx;
    private List<TaskInfo> taskInfos;
    private MyViewHolder holder;
    private OnItemClickLitener mOnItemClickLitener;

    public TaskInfoListAdapter(Context ctx, List<TaskInfo> taskInfos, OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
        this.ctx = ctx;
        this.taskInfos = taskInfos;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        holder = new MyViewHolder(LayoutInflater.from(
                ctx).inflate(R.layout.list_item_task_info, parent,
                false));
        return holder;
    }

    @Override
    public int getItemCount() {
        return taskInfos.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(taskInfos, fromPosition, toPosition);
        //非常重要，调用后Adapter才能知道发生了改变。
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        taskInfos.remove(position);
        //非常重要，调用后Adapter才能知道发生了改变。
        notifyItemRemoved(position);
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }


    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.iv_icon.setImageDrawable(taskInfos.get(position).icon);
        holder.tv_name.setText(taskInfos.get(position).name);
        holder.tv_memory_info.setText(SystemInfoUtils.formatFileSize(taskInfos.get(position).memSize));

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_icon;
        TextView tv_name;
        TextView tv_memory_info;

        public MyViewHolder(View view) {
            super(view);
            iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            tv_name = (TextView) view.findViewById(R.id.tv_name);
            tv_memory_info = (TextView) view.findViewById(R.id.tv_memory_info);
        }
    }

}