package com.scwang.smartrefresh.header.fungame;

import android.support.annotation.RequiresApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.scwang.smartrefresh.layout.api.RefreshContent;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;

import static android.view.MotionEvent.ACTION_MASK;

/**
 * 游戏 header
 * Created by SCWANG on 2017/6/17.
 */

public class FunGameBase extends FrameLayout implements RefreshHeader {

    //<editor-fold desc="Field">
    protected int mOffset;
    protected int mHeaderHeight;
    protected RefreshState mState;
    protected boolean mIsFinish;
    protected boolean mManualOperation;
    protected float mTouchY;
    protected RefreshKernel mRefreshKernel;
    protected RefreshContent mRefreshContent;
    //</editor-fold>

    //<editor-fold desc="View">
    public FunGameBase(Context context) {
        super(context);
    }

    public FunGameBase(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FunGameBase(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public FunGameBase(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setTranslationY(float translationY) {
        if (!isInEditMode()) {
            super.setTranslationY(translationY);
        }
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        params.height = -3;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mState == RefreshState.Refreshing || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mState == RefreshState.Refreshing) {
            if (!mManualOperation) {
                onManualOperationStart();
            }
            switch (event.getAction() & ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mTouchY = event.getRawY();
                    mRefreshKernel.moveSpinner(0, true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dy = event.getRawY() - mTouchY;
                    mRefreshKernel.moveSpinnerInfinitely(dy);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    onManualOperationRelease();
                    mTouchY = -1;
                    if (mIsFinish) {
                        mRefreshKernel.moveSpinner(mHeaderHeight, true);
                    }
                    break;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRefreshKernel = null;
        mRefreshContent = null;
    }

    //</editor-fold>

    //<editor-fold desc="abstract">
    boolean enableLoadmore;
    protected void onManualOperationStart() {
        if (!mManualOperation) {
            mManualOperation = true;
            mRefreshContent = mRefreshKernel.getRefreshContent();
            mRefreshContent.getView().offsetTopAndBottom(mHeaderHeight);
            enableLoadmore = mRefreshKernel.getRefreshLayout().isEnableLoadmore();
            mRefreshKernel.getRefreshLayout().setEnableLoadmore(false);
        }
    }

    protected void onManualOperationMove(float percent, int offset, int headHeight, int extendHeight) {

    }

    protected void onManualOperationRelease() {
        if (mIsFinish) {
            mManualOperation = false;
            mRefreshKernel.getRefreshLayout().setEnableLoadmore(enableLoadmore);
            mRefreshContent.getView().offsetTopAndBottom(-mHeaderHeight);
            if (mTouchY != -1) {//还没松手
                mRefreshKernel.getRefreshLayout().finishRefresh(0);
            } else {
                mRefreshKernel.moveSpinner(mHeaderHeight, true);
            }
        } else {
            mRefreshKernel.moveSpinner(0, true);
        }
    }
    //</editor-fold>

    //<editor-fold desc="RefreshHeader">
    @Override
    public void onPullingDown(float percent, int offset, int headHeight, int extendHeight) {
        if (mManualOperation) onManualOperationMove(percent, offset, headHeight, extendHeight);
        else {
            mOffset = offset;
            setTranslationY(mOffset - mHeaderHeight);
        }
    }

    @Override
    public void onReleasing(float percent, int offset, int headHeight, int extendHeight) {
        onPullingDown(percent, offset, headHeight, extendHeight);
    }

    @Override
    public void onStartAnimator(RefreshLayout layout, int headHeight, int extendHeight) {
        mIsFinish = false;
    }

    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        mState = newState;
    }

    @Override
    public void onInitialized(RefreshKernel kernel, int height, int extendHeight) {
        mRefreshKernel = kernel;
        mHeaderHeight = height;
        setTranslationY(mOffset - mHeaderHeight);
    }

    @Override
    public int onFinish(RefreshLayout layout) {
        if (!mIsFinish) {
            mIsFinish = true;
            if (mManualOperation) {
                if (mTouchY == -1) {//已经放手
                    onManualOperationRelease();
                    onFinish(layout);
                    return 0;
                }
                return Integer.MAX_VALUE;
            }
        }
        return 0;
    }

    @Override
    public void setPrimaryColors(int... colors) {
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.FixedFront;
    }
    //</editor-fold>
}
