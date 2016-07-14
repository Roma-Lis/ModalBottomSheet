package ua.com.test.modalbottomsheet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import lombok.Getter;
import lombok.Setter;

public abstract class BaseModal extends LinearLayoutCompat {

    public static final String EMPTY_STR = "";

    public static final int ANIM_DURATION = 300;

    protected boolean isShowing = false;
    private GestureDetectorCompat mDetector;
    @Setter
    private Listener stateChangeListener;
    private boolean showNextModal = false;
    private ViewGroup modalContent;
    private MyGestureListener myGestureListener;
    private int collapsedShadowColor;
    private int expandedShadowColor;
    @Getter
    @Setter
    private int measuredContentHeight;

    public BaseModal(Context context) {
        super(context);
        init(context);
    }

    public BaseModal(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseModal(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected abstract int getViewLayout();

    protected void init(Context context) {
        setOrientation(VERTICAL);
        modalContent = (ViewGroup) LayoutInflater.from(context).inflate(getViewLayout(), this, false);
        modalContent.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        setMeasuredContentHeight(modalContent.getMeasuredHeight());
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        addView(modalContent, linearParams);

        //init background shadow animators
        collapsedShadowColor = ContextCompat.getColor(context, R.color.transparent);
        expandedShadowColor = ContextCompat.getColor(context, R.color.modal_shadow);
        ButterKnife.bind(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        modalContent.setOnTouchListener(new onlyScrollListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mDetector != null)
            this.mDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP && myGestureListener.isScrollDetected()) {
            if (myGestureListener.getCollapseFraction() > 0.5f) hide(false);
            else show();
        }
        return true;
    }

    public void hide(boolean showNextModal) {
        this.showNextModal = showNextModal;
        long animDuration = (long) ((float)ANIM_DURATION * (1f-myGestureListener.getCollapseFraction()));

        ValueAnimator shadowDownAnimation = ValueAnimator.ofObject
                (new ArgbEvaluator(), Utils.getBackgroundColor(this), collapsedShadowColor);
        shadowDownAnimation.setDuration(animDuration);
        shadowDownAnimation.addUpdateListener(animator ->
                setBackgroundColor((int) animator.getAnimatedValue()));
        shadowDownAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                ValueAnimator slideDown = ValueAnimator.ofFloat
                        (modalContent.getTranslationY(), modalContent.getHeight());
                slideDown.addUpdateListener(animation1 -> {
                    modalContent.setTranslationY((Float) animation1.getAnimatedValue());
                });
                slideDown.setInterpolator(new DecelerateInterpolator());
                slideDown.setDuration(animDuration);
                slideDown.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(GONE);
                if (stateChangeListener != null && !showNextModal) {
                    stateChangeListener.onModalCollapsed();
                    UiUtils.hideKeyBoard(modalContent);
                }
                mDetector = null;
            }
        });
        shadowDownAnimation.start();
        isShowing = false;
    }

    public void show() {
        //Check if modal is already fully expanded
        if (isShowing() && myGestureListener != null && myGestureListener.getCollapseFraction() == 0f) return;

        long animDuration = (long) (ANIM_DURATION * (myGestureListener == null
                || myGestureListener.getCollapseFraction() == 0f ? 1 : myGestureListener.getCollapseFraction()));

        ValueAnimator shadowUpAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                Utils.getBackgroundColor(this), expandedShadowColor);
        shadowUpAnimation.setDuration(animDuration);
        shadowUpAnimation.addUpdateListener(animator ->
                setBackgroundColor((int) animator.getAnimatedValue()));
        shadowUpAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                setVisibility(VISIBLE);
                ValueAnimator slideTop = ValueAnimator.ofFloat
                        (modalContent.getTranslationY() == 0f ? getMeasuredContentHeight() : modalContent.getTranslationY(), 0f);
                slideTop.addUpdateListener(animation1 -> {
                    modalContent.setTranslationY((Float) animation1.getAnimatedValue());
                });
                slideTop.setInterpolator(new DecelerateInterpolator());
                slideTop.setDuration(animDuration);
                slideTop.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myGestureListener = new MyGestureListener(collapsedShadowColor,
                        expandedShadowColor, modalContent.getHeight());
                mDetector = new GestureDetectorCompat(getContext(), myGestureListener);
            }
        });
        shadowUpAnimation.start();
        isShowing = true;
    }

    public boolean isShowing() {
        return isShowing;
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private int scrollDetected = 0;
        private int expandedContentHeight;
        private int collapsedColor;
        private int expandedColor;

        ArgbEvaluator argbEvaluator = new ArgbEvaluator();

        @Getter
        private float collapseFraction = 0f;

        public MyGestureListener(int collapsedColor, int expandedColor, int expandedContentHeight) {
            this.expandedContentHeight = expandedContentHeight;
            this.collapsedColor = collapsedColor;
            this.expandedColor = expandedColor;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            scrollDetected = 0;
            hide(false);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            scrollDetected += distanceY;
            if (Math.abs(scrollDetected) > expandedContentHeight) {
                if (scrollDetected > 0) scrollDetected = expandedContentHeight;
                else scrollDetected = -expandedContentHeight;
            }
            if (scrollDetected > 0) scrollDetected = 0;
            updateCollapsingFraction();
            setBackgroundColor((int) argbEvaluator.evaluate(collapseFraction, expandedColor, collapsedColor));
            modalContent.setTranslationY(expandedContentHeight * collapseFraction);
            return true;
        }

        private void updateCollapsingFraction() {
            collapseFraction = 1f - (float) (expandedContentHeight - Math.abs(scrollDetected)) / expandedContentHeight;
        }

        public boolean isScrollDetected() {
            return scrollDetected != 0;
        }
    }

    public interface Listener {
        void onModalCollapsed();
    }

    private class onlyScrollListener implements OnTouchListener {

        float startY = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    myGestureListener.onScroll(event, event, 0, startY - event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    if (myGestureListener.getCollapseFraction() > 0.5f) hide(false);
                    else show();
                    startY = 0;
                    break;
            }
            return true;
        }
    }
}