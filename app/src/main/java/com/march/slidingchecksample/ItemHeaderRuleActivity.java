package com.march.slidingchecksample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.march.quickrvlibs.ItemHeaderAdapter;
import com.march.quickrvlibs.RvAdapter;
import com.march.quickrvlibs.RvViewHolder;
import com.march.quickrvlibs.inter.ItemHeaderRule;
import com.march.quickrvlibs.inter.OnClickListener;
import com.march.quickrvlibs.inter.RvQuickItemHeader;
import com.march.quickrvlibs.model.RvQuickModel;
import com.march.slidingselect.SlidingSelectLayout;

import java.util.ArrayList;
import java.util.List;

public class ItemHeaderRuleActivity extends MyBaseActivity {

    private List<Content> contents;
    private SlidingSelectLayout ssl;
    private ItemHeaderAdapter<ItemHeader, Content> adapter;
    private List<Content> selects;
    private RecyclerView mRv;

    @Override
    protected void onInitDatas() {
        super.onInitDatas();
        selects = new ArrayList<>();
        contents = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            contents.add(new Content("this is new " + i, i));
        }
    }

    @Override
    protected void onInitViews(Bundle save) {
        super.onInitViews(save);
        mRv = getView(R.id.recyclerview);
        ssl = getView(R.id.ssl);
        mRv.setLayoutManager(new GridLayoutManager(this, 3));
        createAdapter();
    }

    @Override
    protected void onInitEvents() {
        super.onInitEvents();
        ssl.setOnSlidingSelectListener(new SlidingSelectLayout.OnSlidingSelectListener<Content>() {
            @Override
            public void onSlidingSelect(int pos, View parentView, Content data) {
                if (selects.contains(data)) {
                    selects.remove(data);
                } else {
                    selects.add(data);
                }
                adapter.notifyItemChanged(pos);
            }
        });
    }

    private void createAdapter() {
        adapter = new ItemHeaderAdapter<ItemHeader, Content>(
                this,
                contents,
                R.layout.item_header_header,
                R.layout.item_header_content) {
            @Override
            protected void onBindItemHeader(RvViewHolder holder, ItemHeader data, int pos, int type) {
                holder.setText(R.id.info1, data.getTitle());
            }

            @Override
            protected void onBindContent(RvViewHolder holder, Content data, int pos, int type) {
                ssl.markView(holder.getParentView(), pos, data);
                ViewGroup.LayoutParams layoutParams = holder.getParentView().getLayoutParams();
                layoutParams.height = (int) (getResources().getDisplayMetrics().widthPixels / 3.0f);
                holder.setText(R.id.tv, String.valueOf(data.index));

                if (selects.contains(data)) {
                    holder.getParentView().setBackgroundColor(Color.RED);
                } else {
                    holder.getParentView().setBackgroundColor(Color.GREEN);

                }
            }
        };

        adapter.addItemHeaderRule(new ItemHeaderRule<ItemHeader, Content>() {
            @Override
            public ItemHeader buildItemHeader(int currentPos, Content preData, Content currentData, Content nextData) {
                return new ItemHeader("pre is " + getIndex(preData) + " current is " + getIndex(currentData) + " next is " + getIndex(nextData));
            }

            @Override
            public boolean isNeedItemHeader(int currentPos, Content preData, Content currentData, Content nextData, boolean isCheckAppendData) {
                return currentPos == 0 && !isCheckAppendData || currentData.index % 7 == 1;
            }
        });

        adapter.setOnChildClickListener(new OnClickListener<RvQuickModel>() {
            @Override
            public void onItemClick(int pos, RvViewHolder holder, RvQuickModel data) {
                if (data.getRvType() == RvAdapter.TYPE_ITEM_DEFAULT) {
                    Content content = data.get();

                    if (selects.contains(content)) {
                        selects.remove(content);
                    } else {
                        selects.add(content);
                    }
                    adapter.notifyItemChanged(pos);
                }
            }
        });
        mRv.setAdapter(adapter);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.item_header_activity;
    }


    private String getIndex(Content content) {
        if (content == null)
            return "null";
        return content.index + "";
    }

    class ItemHeader extends RvQuickItemHeader {
        String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public ItemHeader(String title) {
            this.title = title;
        }
    }

    static class Content {
        String title;
        int index;


        public Content(String title, int index) {
            this.title = title;
            this.index = index;
        }
    }
}
