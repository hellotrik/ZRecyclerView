package cc.ibooker.zrecyclerviewlib;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * 自定义LinearLayoutManager，将RecylerView的IndexOutOfBoundsException抛出
 * Created by 邹峰立 on 2017/6/2.
 */
public class ZLinearLayoutManager extends LinearLayoutManager {
    public ZLinearLayoutManager(Context context) {
        super(context);
    }

    public ZLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public ZLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
