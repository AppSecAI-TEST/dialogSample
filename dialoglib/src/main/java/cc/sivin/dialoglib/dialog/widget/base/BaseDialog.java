package cc.sivin.dialoglib.dialog.widget.base;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.Animator;

import cc.sivin.dialoglib.animation.BaseAnimatorSet;
import cc.sivin.dialoglib.dialog.utils.StatusBarUtils;

public abstract class BaseDialog<T extends BaseDialog<T>> extends Dialog {

    protected String TAG;

    protected Context context;

    protected DisplayMetrics dm;

    protected boolean cancel;

    protected float widthScale = 1;

    protected float heightScale;

    private BaseAnimatorSet showAnim;


    private BaseAnimatorSet dismissAnim;

    protected LinearLayout ll_top;

    protected LinearLayout ll_control_height;

    /**
     * 是否正在执行显示动画
     */
    private boolean isShowAnim;

    /**
     * 是否正在执行消失动画
     */
    private boolean isDismissAnim;

    protected float maxHeight;

    /**
     * method execute order:
     * show:constructor---show---onCreate---onStart---onAttachToWindow
     * dismiss:dismiss---onDetachedFromWindow---onStop
     */
    public BaseDialog(Context context) {
        super(context);
        setDialogTheme();
        this.context = context;
        this.TAG = this.getClass().getSimpleName();
        Log.d(TAG, "constructor");
    }

    /**
     * set dialog theme(设置对话框主题)
     */
    private void setDialogTheme() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.addFlags(LayoutParams.FLAG_DIM_BEHIND);
        }


    }


    public abstract View onCreateView();

    /**
     * set Ui data or logic opreation before attatched window(在对话框显示之前,设置界面数据或者逻辑)
     */
    public abstract void setUiBeforeShow();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        dm = context.getResources().getDisplayMetrics();
        ll_top = new LinearLayout(context);
        ll_top.setGravity(Gravity.CENTER);

        ll_control_height = new LinearLayout(context);
        ll_control_height.setOrientation(LinearLayout.VERTICAL);

        ll_control_height.addView(onCreateView());
        ll_top.addView(ll_control_height);

        maxHeight = dm.heightPixels - StatusBarUtils.getHeight(context);
        // maxHeight = dm.heightPixels;
        setContentView(ll_top, new ViewGroup.LayoutParams(dm.widthPixels, (int) maxHeight));
        setCanceledOnTouchOutside(true);

        ll_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancel) {
                    dismiss();
                }
            }
        });
    }

    /**
     * when dailog attached to window,set dialog width and height and show anim
     * (当dailog依附在window上,设置对话框宽高以及显示动画)
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");

        setUiBeforeShow();

        int width;
        if (widthScale == 0) {
            width = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            width = (int) (dm.widthPixels * widthScale);
        }

        int height;
        if (heightScale == 0) {
            height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else if (heightScale == 1) {
            height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            height = (int) (maxHeight * heightScale);
        }

        ll_control_height.setLayoutParams(new LinearLayout.LayoutParams(width, height));

        if (showAnim != null) {
            showAnim.listener(new BaseAnimatorSet.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    isShowAnim = true;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isShowAnim = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    isShowAnim = false;
                }
            }).playOn(ll_control_height);
        } else {
            BaseAnimatorSet.reset(ll_control_height);
        }
    }


    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        this.cancel = cancel;
        super.setCanceledOnTouchOutside(cancel);
    }

    @Override
    public void show() {
        Log.d(TAG, "show");
        super.show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
    }

    @Override
    public void dismiss() {
        Log.d(TAG, "dismiss");
        if (dismissAnim != null) {
            dismissAnim.listener(new BaseAnimatorSet.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    isDismissAnim = true;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isDismissAnim = false;
                    superDismiss();
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    isDismissAnim = false;
                    superDismiss();
                }
            }).playOn(ll_control_height);
        } else {
            superDismiss();
        }
    }

    /**
     * dismiss without anim(无动画dismiss)
     */
    public void superDismiss() {
        super.dismiss();
    }

    /**
     * dialog anim by styles(动画弹出对话框,style动画资源)
     */
    public void show(int animStyle) {
        Window window = getWindow();
        if (window != null)
            window.setWindowAnimations(animStyle);
        show();
    }

    /**
     * set window dim or not(设置背景是否昏暗)
     */
    public T dimEnabled(boolean isDimEnabled) {
        Window window = getWindow();
        if (window == null) return (T) this;
        if (isDimEnabled ) {
            window.addFlags(LayoutParams.FLAG_DIM_BEHIND);
        } else {
            window.clearFlags(LayoutParams.FLAG_DIM_BEHIND);
        }
        return (T) this;
    }

    /**
     * set dialog width scale:0-1(设置对话框宽度,占屏幕宽的比例0-1)
     */
    public T widthScale(float widthScale) {
        this.widthScale = widthScale;
        return (T) this;
    }

    /**
     * set dialog height scale:0-1(设置对话框高度,占屏幕宽的比例0-1)
     */
    public T heightScale(float heightScale) {
        this.heightScale = heightScale;
        return (T) this;
    }

    /**
     * set show anim(设置显示的动画)
     */
    public T showAnim(BaseAnimatorSet showAnim) {
        this.showAnim = showAnim;
        return (T) this;
    }

    /**
     * set dismiss anim(设置隐藏的动画)
     */
    public T dismissAnim(BaseAnimatorSet dismissAnim) {
        this.dismissAnim = dismissAnim;
        return (T) this;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isDismissAnim || isShowAnim) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (isDismissAnim || isShowAnim) {
            return;
        }
        super.onBackPressed();
    }


    protected int dp2px(float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
