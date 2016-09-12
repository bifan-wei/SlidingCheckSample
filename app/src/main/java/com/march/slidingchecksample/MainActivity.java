package com.march.slidingchecksample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.march.quickrvlibs.RvViewHolder;
import com.march.quickrvlibs.SimpleRvAdapter;
import com.march.quickrvlibs.inter.OnClickListener;
import com.march.quickrvlibs.inter.OnLongClickListener;
import com.march.slidingselect.SlidingSelectLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends MyBaseActivity {
    private RecyclerView mRv;
    private SlidingSelectLayout mScl;
    private List<Demo> demos;
    private int size;
    private float spanCount = 4f;
    private SimpleRvAdapter<Demo> adapter;


    @Override
    protected void onInitDatas() {
        super.onInitDatas();
        size = (int) (getResources().getDisplayMetrics().widthPixels / spanCount);
        demos = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            demos.add(new Demo(i, "this is " + i));
        }
    }


    @Override
    protected void onInitViews(final Bundle save) {
        super.onInitViews(save);
        mRv = getView(R.id.recyclerview);
        mScl = getView(R.id.scl);
        mRv.setLayoutManager(new GridLayoutManager(mContext, (int) spanCount));
        createAdapter();
    }

    @Override
    protected void onInitEvents() {
        super.onInitEvents();
        mScl.setOnSlidingSelectListener(new SlidingSelectLayout.OnSlidingSelectListener<Demo>() {
            @Override
            public void onSlidingSelect(int pos, View parentView, Demo data) {
                demos.get(pos).isChanged = !demos.get(pos).isChanged;
                adapter.notifyItemChanged(pos);
            }
        });
    }

    private void createAdapter() {
        adapter = new SimpleRvAdapter<Demo>(mContext, demos, R.layout.item_rv) {
            @Override
            public void onBindView(RvViewHolder holder, Demo data, int pos, int type) {
                ViewGroup.LayoutParams lp = holder.getParentView().getLayoutParams();
                lp.width = size;
                lp.height = size;
                TextView tv = (TextView) holder.getView(R.id.content);
                if (data.isChanged) {
                    tv.setTextColor(Color.RED);
                    tv.setText("change " + data.desc);
                } else {
                    tv.setTextColor(Color.WHITE);
                    tv.setText(data.desc);
                }
                mScl.markView(holder.getParentView(), pos, data);
            }
        };

        adapter.setOnChildClickListener(new OnClickListener<Demo>() {
            @Override
            public void onItemClick(int pos, RvViewHolder holder, Demo data) {
                data.isChanged = !data.isChanged;
                adapter.notifyItemChanged(pos);
            }
        });

        adapter.setOnItemLongClickListener(new OnLongClickListener<Demo>() {
            @Override
            public void onItemLongClick(int pos, RvViewHolder holder, Demo data) {
                startActivity(new Intent(mContext, ItemHeaderRuleActivity.class));
            }
        });

        mRv.setAdapter(adapter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected String[] getPermission2Check() {
        return new String[0];
    }

    @Override
    protected boolean isInitTitle() {
        return false;
    }
}
