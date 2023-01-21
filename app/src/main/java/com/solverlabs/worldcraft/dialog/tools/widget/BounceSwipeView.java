package com.solverlabs.worldcraft.dialog.tools.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.solverlabs.worldcraft.dialog.tools.ui.SwipeView;
import com.solverlabs.worldcraft.dialog.tools.util.AnimationUtil;

public class BounceSwipeView extends SwipeView {
    private static final int ANIMATION_DURATION = 120;
    private static final boolean BOUNCING_ON_LEFT = true;
    private static final boolean BOUNCING_ON_RIGHT = false;
    private static final int FRAME_DURATION = 30;
    private static final int NUMBER_OF_FRAMES = 4;
    private final Context mContext;
    Handler mEaseAnimationFrameHandler;
    private boolean mAtEdge;
    private float mAtEdgePreviousPosition;
    private float mAtEdgeStartPosition;
    private boolean mBounceEnabled;
    private boolean mBouncingSide;
    private int mCurrentAnimationFrame;
    private View.OnTouchListener mOnTouchListener;
    private int mPaddingChange;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingStartValue;
    private SharedPreferences mSharedPreferences;

    public BounceSwipeView(Context context) {
        super(context);
        this.mAtEdge = false;
        this.mBounceEnabled = true;
        this.mContext = context;
        initBounceSwipeView();
    }

    public BounceSwipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mAtEdge = false;
        this.mBounceEnabled = true;
        this.mContext = context;
        initBounceSwipeView();
    }

    public BounceSwipeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAtEdge = false;
        this.mBounceEnabled = true;
        this.mContext = context;
        initBounceSwipeView();
    }

    @SuppressLint({"ClickableViewAccessibility", "HandlerLeak"})
    private void initBounceSwipeView() {
        super.setOnTouchListener(new BounceViewOnTouchListener());
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        this.mEaseAnimationFrameHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int newPadding = AnimationUtil.quadraticOutEase(mCurrentAnimationFrame, mPaddingStartValue, -mPaddingChange, 4.0f);
                if (mBouncingSide) {
                    BounceSwipeView.super.setPadding(newPadding, getPaddingTop(), getPaddingRight(), getPaddingBottom());
                } else {
                    BounceSwipeView.super.setPadding(getPaddingLeft(), getPaddingTop(), newPadding, getPaddingBottom());
                }
                mCurrentAnimationFrame += 1;
                if (mCurrentAnimationFrame <= 4) {
                    mEaseAnimationFrameHandler.sendEmptyMessageDelayed(0, 30L);
                }
            }
        };
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        this.mPaddingLeft = left;
        this.mPaddingRight = right;
        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mOnTouchListener = onTouchListener;
    }

    public boolean getBounceEnabled() {
        return this.mBounceEnabled;
    }

    public void setBounceEnabled(boolean enabled) {
        this.mBounceEnabled = enabled;
    }

    public void doBounceBackEaseAnimation() {
        if (this.mBouncingSide) {
            this.mPaddingChange = getPaddingLeft() - this.mPaddingLeft;
            this.mPaddingStartValue = getPaddingLeft();
        } else {
            this.mPaddingChange = getPaddingRight() - this.mPaddingRight;
            this.mPaddingStartValue = getPaddingRight();
        }
        this.mCurrentAnimationFrame = 0;
        this.mEaseAnimationFrameHandler.removeMessages(0);
        this.mEaseAnimationFrameHandler.sendEmptyMessage(0);
    }

    public void doAtEdgeAnimation() {
        if (getCurrentPage() == 0) {
            this.mBouncingSide = true;
            super.setPadding(getPaddingLeft() + 50, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        } else if (getCurrentPage() == getPageCount() - 1) {
            this.mBouncingSide = false;
            super.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight() + 50, getPaddingBottom());
            scrollTo(getScrollX() + 50, getScrollY());
        }
        doBounceBackEaseAnimation();
    }

    public class BounceViewOnTouchListener implements View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent ev) {
            if (mOnTouchListener == null || !mOnTouchListener.onTouch(view, ev)) {
                if (mBounceEnabled) {
                    switch (ev.getAction()) {
                        case 1:
                            if (mAtEdge) {
                                mAtEdge = false;
                                mAtEdgePreviousPosition = 0.0f;
                                mAtEdgeStartPosition = 0.0f;
                                doBounceBackEaseAnimation();
                                return true;
                            }
                            break;
                        case 2:
                            int maxScrollAmount = ((getPageCount() - 1) * getPageWidth()) - (getPageWidth() % 2);
                            if ((getScrollX() == 0 && !mAtEdge) || (getScrollX() == maxScrollAmount && !mAtEdge)) {
                                mAtEdge = true;
                                mAtEdgeStartPosition = ev.getX();
                                mAtEdgePreviousPosition = ev.getX();
                                break;
                            } else if (getScrollX() == 0) {
                                mAtEdgePreviousPosition = ev.getX();
                                mBouncingSide = true;
                                BounceSwipeView.super.setPadding(((int) (mAtEdgePreviousPosition - mAtEdgeStartPosition)) / 2, getPaddingTop(), getPaddingRight(), getPaddingBottom());
                                return true;
                            } else if (getScrollX() < maxScrollAmount) {
                                mAtEdge = false;
                                break;
                            } else {
                                mAtEdgePreviousPosition = ev.getX();
                                mBouncingSide = false;
                                int newRightPadding = ((int) (mAtEdgeStartPosition - mAtEdgePreviousPosition)) / 2;
                                BounceSwipeView.super.setPadding(getPaddingLeft(), getPaddingTop(), Math.max(newRightPadding, mPaddingRight), getPaddingBottom());
                                scrollTo((int) (maxScrollAmount + ((mAtEdgeStartPosition - mAtEdgePreviousPosition) / 2.0f)), getScrollY());
                                return true;
                            }
                        default:
                            break;
                    }
                }
                return false;
            }
            return true;
        }
    }
}
