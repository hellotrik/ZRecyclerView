package cc.ibooker.zrecyclerviewlib;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * RecyclerView加载更多滚动事件
 * Created by 邹峰立 on 2017/4/30 0030.
 */
public class RvScrollListener extends RecyclerView.OnScrollListener {
    private enum LAYOUT_MANAGER_TYPE {
        LINEAR,
        GRID,
        STAGGERED_GRID
    }

    /**
     * layoutManager的类型（枚举）
     */
    private LAYOUT_MANAGER_TYPE layoutManagerType;

    /**
     * 最后一个的位置
     */
    private int[] lastPositions;

    /**
     * 最后一个可见的item的位置
     */
    private int lastVisibleItemPosition;

    /**
     * 第一个可见的item的位置
     */
    private int firstVisibleItemPosition;

    /**
     * 是否正在加载
     */
    private boolean isLoadingMore = false;
    /**
     * 记录y轴滚动距离（>0代表向下滚动，<0代表向上滚动）
     */
    private int dy;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        // 记录y轴滚动距离（>0代表向下滚动）
        this.dy = dy;
        // 获取RecyclerView布局管理器
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null) {
            if (layoutManagerType == null) {
                if (layoutManager instanceof GridLayoutManager) {
                    layoutManagerType = LAYOUT_MANAGER_TYPE.GRID;
                } else if (layoutManager instanceof LinearLayoutManager) {
                    layoutManagerType = LAYOUT_MANAGER_TYPE.LINEAR;
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    layoutManagerType = LAYOUT_MANAGER_TYPE.STAGGERED_GRID;
                } else {
                    throw new RuntimeException("Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
                }
            }
            // 最后一个可见的item的位置
            switch (layoutManagerType) {
                case LINEAR:
                    lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
                    break;
                case GRID:
                    lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    firstVisibleItemPosition = ((GridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
                    break;
                case STAGGERED_GRID:
                    StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                    if (lastPositions == null)
                        lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                    staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                    lastVisibleItemPosition = findMax(lastPositions);
                    staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(lastPositions);
                    firstVisibleItemPosition = findMin(lastPositions);
                    break;
            }

            /**
             * 设置滚动距离事件
             */
            if (onScrollDistanceListener != null) {
                onScrollDistanceListener.onScrollDistance(dy);
            }

            /**
             * 设置指定位置到顶部距离事件
             */
            if (targetPosition >= 0 && targetPosition < layoutManager.getItemCount() && onPositionTopDistanceListener != null) {
                View view = layoutManager.findViewByPosition(targetPosition);
                if (view != null) {
                    onPositionTopDistanceListener.onPositionTopDistance(view.getTop());
                }
            }
        }
    }

    // 滚动状态改变
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        // 获取RecyclerView布局管理器
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null) {
            // 获取RecyclerView可见Item的数量
            int visibleItemCount = layoutManager.getChildCount();
            // 获取RecyclerView总Item的数量
            int totalItemCount = layoutManager.getItemCount();
            // 判断是否需要加载更多。
            // 1、可见性大于0。
            // 2、当前处于滚动停止状态。
            // 3、最后一个可见项大于或等于Item的总数（滚动到最底部）
            if (dy > 0 && visibleItemCount > 0
                    && newState == RecyclerView.SCROLL_STATE_IDLE
                    && (lastVisibleItemPosition >= totalItemCount - 1)) {
                /**
                 * 加载更多 事件
                 */
                if (!isLoadingMore && onLoadListener != null) {
                    isLoadingMore = true;
                    onLoadListener.onLoad();
                }
            }

            /**
             * 滚动状态改变事件
             */
            if (onScrollStateChangedListener != null)
                onScrollStateChangedListener.onScrollStateChanged(newState);

            /**
             * 是否滚动到最顶部监听
             */
            if (onIsTopListener != null)
                onIsTopListener.isTop(firstVisibleItemPosition == 0);
        }
    }

    // 计算最大值
    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    // 计算最小值
    private int findMin(int[] firstPositions) {
        int min = firstPositions[0];
        for (int value : firstPositions) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    public void setLoadingMore(boolean loadingMore) {
        isLoadingMore = loadingMore;
    }

    // 加载更多接口
    public interface OnLoadListener {
        void onLoad();
    }

    private OnLoadListener onLoadListener;

    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    // 滚动距离接口
    public interface OnScrollDistanceListener {
        void onScrollDistance(int dy);
    }

    private OnScrollDistanceListener onScrollDistanceListener;

    public void setOnScrollDistanceListener(OnScrollDistanceListener onScrollDistanceListener) {
        this.onScrollDistanceListener = onScrollDistanceListener;
    }

    // 滚动状态改变接口
    public interface OnScrollStateChangedListener {
        void onScrollStateChanged(int newState);
    }

    private OnScrollStateChangedListener onScrollStateChangedListener;

    public void setOnScrollStateChangedListener(OnScrollStateChangedListener onScrollStateChangedListener) {
        this.onScrollStateChangedListener = onScrollStateChangedListener;
    }

    // 指定位置position到顶部距离接口
    public interface OnPositionTopDistanceListener {
        void onPositionTopDistance(int dy);
    }

    private OnPositionTopDistanceListener onPositionTopDistanceListener;
    private int targetPosition;

    public void setOnPositionTopDistanceListener(OnPositionTopDistanceListener onPositionTopDistanceListener, int position) {
        this.onPositionTopDistanceListener = onPositionTopDistanceListener;
        this.targetPosition = position;
    }

    // 是否滚动到顶部监听
    public interface OnIsTopListener {
        void isTop(boolean isTop);
    }

    private OnIsTopListener onIsTopListener;

    public void setOnIsTopListener(OnIsTopListener onIsTopListener) {
        this.onIsTopListener = onIsTopListener;
    }
}
