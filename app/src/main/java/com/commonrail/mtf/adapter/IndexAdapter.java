package com.commonrail.mtf.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commonrail.mtf.R;
import com.commonrail.mtf.po.Injector;
import com.commonrail.mtf.util.common.AppUtils;
import com.commonrail.mtf.util.common.L;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * 项目名称：railtool
 * 类描述：
 * 创建人：wengyiming
 * 创建时间：16/1/11 下午9:18
 * 修改人：wengyiming
 * 修改时间：16/1/11 下午9:18
 * 修改备注：
 */
public class IndexAdapter
        extends RecyclerView.Adapter<IndexAdapter.ViewHolder> {

    private final List<Injector> mInjectors;
    private Click mClick;

    public IndexAdapter(List<Injector> items) {
        mInjectors = items;
        L.e("IndexAdapter:" + mInjectors.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mInjectors.get(position);
        holder.mContentView.setText(mInjectors.get(position).getInjectorName());
        holder.mIdView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.mIdView.setImageURI(AppUtils.getFileFrescoUri("/images/logo0" + (position + 1) + ".png"));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mClick != null) {
                    mClick.itemClick(position);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mInjectors.size();
    }

    public void setClick(Click mClick) {
        this.mClick = mClick;
    }

    public interface Click {
        void itemClick(int p);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final SimpleDraweeView mIdView;
        public final TextView mContentView;
        public Injector mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (SimpleDraweeView) view.findViewById(R.id.image);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}