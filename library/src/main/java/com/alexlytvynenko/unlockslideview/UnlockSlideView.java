package com.alexlytvynenko.unlockslideview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import static com.alexlytvynenko.unlockslideview.UnlockSlideView.ResetSpeed.FAST;
import static com.alexlytvynenko.unlockslideview.UnlockSlideView.ResetSpeed.NORMAL;
import static com.alexlytvynenko.unlockslideview.UnlockSlideView.ResetSpeed.SLOW;
import static com.alexlytvynenko.unlockslideview.UnlockSlideView.TextGravity.CENTER_IN_PARENT;
import static com.alexlytvynenko.unlockslideview.UnlockSlideView.TextGravity.CENTER_OF_THUMB;
import static com.alexlytvynenko.unlockslideview.UnlockSlideView.TextGravity.NONE;

/**
 * Created by alex_lytvynenko on 05.01.17.
 *
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_resetSpeed
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_slideBackground
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_thumb
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_thumb_width
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_thumb_height
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_thumbPadding
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_text
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_textBold
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_textSize
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_textColor
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_textPadding
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_textGravity
 * @attr ref com.alexlytvynenko.unlockslideview.R.styleable#UnlockSlideView_limitProgress
 */
public class UnlockSlideView extends View {

    private final int DEFAULT_TEXT_SIZE = 16;

    @IntDef({SLOW, NORMAL, FAST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ResetSpeed {
        int SLOW = 25;
        int NORMAL = 50;
        int FAST = 100;
    }

    @IntDef({NONE, CENTER_IN_PARENT, CENTER_OF_THUMB})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TextGravity {
        int NONE = 1;
        int CENTER_IN_PARENT = 2;
        int CENTER_OF_THUMB = 3;
    }

    private Drawable mThumb;
    private Drawable mBackground;
    private int mBackgroundWidth;
    private int mBackgroundHeight;
    private int mThumbWidth;
    private int mThumbHeight;
    private int mThumbPadding;
    private @ResetSpeed int mResetSpeed;
    private String mText;
    private boolean mIsTextBold;
    private int mTextColor;
    private int mTextSize;
    private int mTextPadding;
    private @TextGravity int mTextGravity;
    private int mLimitProgressForSuccess;

    private TextPaint mTextPaint;
    private Rect mTextRect = new Rect();
    private int mTextWidth;
    private int mStartTextPosition;
    private int mEndTextPosition;

    private int mDragProgressX;
    private int mStartTouchedX;
    private boolean mIsTouched;
    private boolean mIsResetting;
    private boolean mIsUnlocked;

    private WeakReference<OnUnlockListener> mOnUnlockListenerReference;

    public UnlockSlideView(Context context) {
        super(context);
        init(context, null);
    }

    public UnlockSlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Interface definition for a callback to be invoked when the dragging progress has reached
     * to the specified limit {@link #mLimitProgressForSuccess}
     *
     * @author Alex Lytvynenko
     */
    public interface OnUnlockListener {
        /**
         * Callback when the dragging progress has reached
         * to the specified limit {@link #mLimitProgressForSuccess}
         */
        void onUnlock();
    }

    private void init(Context context, AttributeSet attrs) {
        float density = getResources().getDisplayMetrics().density;

        mThumb = ContextCompat.getDrawable(context, R.drawable.thumb);
        mBackground = ContextCompat.getDrawable(context, R.drawable.bg);
        mThumbWidth = mThumb.getIntrinsicWidth();
        mThumbHeight = mThumb.getIntrinsicHeight();
        mResetSpeed = ResetSpeed.NORMAL;
        mTextGravity = TextGravity.NONE;
        mText = "";
        mEndTextPosition = -1;
        mStartTextPosition = -1;
        mLimitProgressForSuccess = 95;

        mTextColor = ContextCompat.getColor(context, android.R.color.black);
        mTextSize = (int) (DEFAULT_TEXT_SIZE * density);

        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.UnlockSlideView, 0, 0);

            Drawable background = a.getDrawable(R.styleable.UnlockSlideView_slideBackground);
            if (background != null)
                mBackground = background;

            Drawable thumb = a.getDrawable(R.styleable.UnlockSlideView_thumb);
            if (thumb != null) {
                mThumb = thumb;
                mThumbWidth = thumb.getIntrinsicWidth();
                mThumbHeight = thumb.getIntrinsicHeight();
            }

            int resetSpeedAttr = a.getInt(R.styleable.UnlockSlideView_resetSpeed, 2);
            mResetSpeed = convertAttrValueToResetSpeed(resetSpeedAttr);

            mThumbWidth = a.getDimensionPixelSize(R.styleable.UnlockSlideView_thumb_width, mThumbWidth);
            mThumbHeight = a.getDimensionPixelSize(R.styleable.UnlockSlideView_thumb_height, mThumbHeight);

            mThumbPadding = a.getDimensionPixelSize(R.styleable.UnlockSlideView_thumbPadding, 0);

            mThumbHeight = mThumbHeight - mThumbPadding * 2;
            mThumbWidth = mThumbWidth - mThumbPadding * 2;

            int textGravityAttr = a.getInt(R.styleable.UnlockSlideView_textGravity, 1);
            mTextGravity = convertAttrValueToTextGravity(textGravityAttr);

            String text = a.getString(R.styleable.UnlockSlideView_text);
            if (!TextUtils.isEmpty(text))
                mText = text;

            mTextSize = (int) a.getDimension(R.styleable.UnlockSlideView_textSize, mTextSize);
            mTextColor = a.getColor(R.styleable.UnlockSlideView_textColor, mTextColor);
            mTextPadding = a.getDimensionPixelSize(R.styleable.UnlockSlideView_textPadding, 0);
            mIsTextBold = a.getBoolean(R.styleable.UnlockSlideView_textBold, false);

            mLimitProgressForSuccess = a.getInt(R.styleable.UnlockSlideView_limitProgress, mLimitProgressForSuccess);
            if (mLimitProgressForSuccess > 100)
                mLimitProgressForSuccess = 99;
            if (mLimitProgressForSuccess < 10)
                mLimitProgressForSuccess = 10;

            a.recycle();
        }

        mTextPaint = new TextPaint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTypeface(mIsTextBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mTextWidth = (int) mTextPaint.measureText(mText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mBackgroundWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        mBackgroundHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // init start and end position for text
        if (mEndTextPosition == -1)
            mEndTextPosition = getEndTextPosition();
        if (mStartTextPosition == -1)
            mStartTextPosition = getStartTextPosition();

        // draw the background
        if (mBackground != null) {
            mBackground.setBounds(0, 0, mBackgroundWidth, mBackgroundHeight);
            mBackground.draw(canvas);
        }

        // calculate thumb bounds
        int startTop = mBackgroundHeight / 2 - mThumbHeight / 2;

        mThumb.setBounds(mDragProgressX + mThumbPadding,
                startTop,
                mDragProgressX + mThumbWidth + mThumbPadding,
                startTop + mThumbHeight);

        // draw the text
        String ellipsizedText = String.valueOf(TextUtils.ellipsize(mText, mTextPaint,
                mEndTextPosition - mDragProgressX - mThumbWidth / 3,
                TextUtils.TruncateAt.START));

        if (!ellipsizedText.isEmpty()
                && !ellipsizedText.equals(mText)
                && String.valueOf(ellipsizedText.charAt(0)).equals("…")) {
            ellipsizedText = ellipsizedText.substring(1);
        }

        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextRect);

        int xPos = (int) (mEndTextPosition - mTextPaint.measureText(ellipsizedText));
        int yPos = (int) ((mThumb.getBounds().centerY()) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));

        canvas.drawText(ellipsizedText, xPos, yPos, mTextPaint);

        // draw thumb
        mThumb.draw(canvas);

        resetProgressIfNeeded();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled() && !mIsResetting) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // check if thumb area is touched
                    if (isThumbTouched(event)) {
                        this.getParent().requestDisallowInterceptTouchEvent(true);
                        mIsTouched = true;
                        mStartTouchedX = (int) event.getX();
                    } else {
                        mIsTouched = false;
                        this.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    // drag thumb if it's touched
                    if (mIsTouched) {
                        // call OnUnlockListener.onUnlock event if is already reached the progress
                        if (!mIsUnlocked && isReachProgressToUnlock()) {
                            if (mOnUnlockListenerReference != null) {
                                OnUnlockListener unlockListener = mOnUnlockListenerReference.get();
                                if (unlockListener != null) {
                                    mIsUnlocked = true;
                                    unlockListener.onUnlock();
                                }
                            }
                        }
                        // calculate dragging progress
                        mDragProgressX = (int) event.getX() - mStartTouchedX;
                        // avoid to reach a negative progress
                        if (event.getX() < mStartTouchedX) {
                            mDragProgressX = 0;
                        }
                        // avoid to reach a progress more than maximum
                        if (event.getX() > (mBackgroundWidth - mThumbWidth - mThumbPadding + mStartTouchedX)) {
                            mDragProgressX = mBackgroundWidth - mThumbWidth - mThumbPadding;
                        }
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    // end drag action
                    mIsTouched = false;
                    mIsUnlocked = false;
                    mStartTouchedX = 0;
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    invalidate();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    // end drag action
                    mIsTouched = false;
                    mIsUnlocked = false;
                    mStartTouchedX = 0;
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    invalidate();
                    break;
            }
            return true;
        }
        return false;
    }

    /**
     * Whether progress reaches to specified value {@link #mLimitProgressForSuccess} to call
     * {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.OnUnlockListener} event
     */
    private boolean isReachProgressToUnlock() {
        return (mDragProgressX + mThumbWidth + mThumbPadding) >= (mBackgroundWidth) * mLimitProgressForSuccess / 100;
    }

    /**
     * Get X position for start of text
     */
    private int getStartTextPosition() {
        int startTextPosition = mThumbWidth + mThumbPadding + mTextPadding;
        switch (mTextGravity) {
            case TextGravity.NONE:
                startTextPosition = mThumbWidth + mThumbPadding + mTextPadding;
                break;
            case TextGravity.CENTER_IN_PARENT:
                startTextPosition = mBackgroundWidth / 2 - mTextWidth / 2;
                break;
            case TextGravity.CENTER_OF_THUMB:
                startTextPosition = (mBackgroundWidth + mThumbWidth) / 2 - mTextWidth / 2;
                break;
        }
        if (mStartTextPosition != -1 && mStartTextPosition < mDragProgressX + mThumbWidth / 2) {
            startTextPosition = startTextPosition + mDragProgressX - mStartTextPosition + mThumbWidth / 2;
        }
        return startTextPosition;
    }

    /**
     * Get X position for end of text
     */
    private int getEndTextPosition() {
        return getStartTextPosition() + mTextWidth;
    }

    /**
     * Check whether touch is on thumb area.
     */
    private boolean isThumbTouched(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int startTop = mBackgroundHeight / 2 - mThumbHeight / 2;
        return (x > mDragProgressX && x < mDragProgressX + mThumbWidth)
                && (y > startTop && y < startTop + mThumbHeight);
    }

    /**
     * Reset progress if thumb was unpressed and was not reached to the finish.
     */
    private void resetProgressIfNeeded() {
        if (!mIsTouched && mDragProgressX > 0) {
            mIsResetting = true;
            mDragProgressX -= mResetSpeed;
            if (mDragProgressX < 0) {
                mDragProgressX = 0;
            }
            invalidate();
        } else {
            mIsResetting = false;
        }
    }

    /**
     * Convert value from attrs to {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.ResetSpeed}
     */
    private @ResetSpeed int convertAttrValueToResetSpeed(int attrValue) {
        @ResetSpeed int resetSpeed = ResetSpeed.NORMAL;
        switch (attrValue) {
            case 1:
                resetSpeed = ResetSpeed.SLOW;
                break;
            case 2:
                resetSpeed = ResetSpeed.NORMAL;
                break;
            case 3:
                resetSpeed = ResetSpeed.FAST;
                break;
        }
        return resetSpeed;
    }

    /**
     * Convert value from attrs to {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.TextGravity}
     */
    private @TextGravity int convertAttrValueToTextGravity(int attrValue) {
        @TextGravity int textGravity = TextGravity.NONE;
        switch (attrValue) {
            case 1:
                textGravity = TextGravity.NONE;
                break;
            case 2:
                textGravity = TextGravity.CENTER_IN_PARENT;
                break;
            case 3:
                textGravity = TextGravity.CENTER_OF_THUMB;
                break;
        }
        return textGravity;
    }

    /**
     * Set {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.OnUnlockListener} listener
     */
    public void setOnUnlockListener(OnUnlockListener listener) {
        if (listener == null) {
            removeOnUnlockListener();
        } else {
            mOnUnlockListenerReference = new WeakReference<>(listener);
        }
    }

    /**
     * Remove {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.OnUnlockListener} listener
     */
    public void removeOnUnlockListener() {
        if (mOnUnlockListenerReference != null) {
            mOnUnlockListenerReference.clear();
        }
    }

    /**
     * Get background drawable
     *
     * @return background drawable
     */
    public Drawable getUnlockBackgroundDrawable() {
        return mBackground;
    }

    /**
     * Set background drawable
     *
     * @param background background drawable
     */
    public void setUnlockBackgroundDrawable(Drawable background) {
        mBackground = background;
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        requestLayout();
    }

    /**
     * Set background drawable resource
     *
     * @param backgroundRes background drawable resource
     */
    public void setUnlockBackgroundDrawableResource(@DrawableRes int backgroundRes) {
        mBackground = ContextCompat.getDrawable(getContext(), backgroundRes);
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        requestLayout();
    }

    /**
     * Get thumb drawable
     *
     * @return thumb drawable
     */
    public Drawable getThumbDrawable() {
        return mThumb;
    }

    /**
     * Set thumb drawable
     *
     * @param thumb thumb drawable
     */
    public void setThumbDrawable(@NonNull Drawable thumb) {
        mThumb = thumb;
        mThumbWidth = mThumb.getIntrinsicWidth();
        mThumbHeight = mThumb.getIntrinsicHeight();
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        invalidate();
    }

    /**
     * Set thumb drawable resource
     *
     * @param drawableRes thumb drawable resource
     */
    public void setThumbDrawableResource(@DrawableRes int drawableRes) {
        mThumb = ContextCompat.getDrawable(getContext(), drawableRes);
        mThumbWidth = mThumb.getIntrinsicWidth();
        mThumbHeight = mThumb.getIntrinsicHeight();
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        invalidate();
    }

    /**
     * Get speed for resetting
     *
     * @return reset speed {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.ResetSpeed}
     */
    public @ResetSpeed int getResetSpeed() {
        return mResetSpeed;
    }

    /**
     * Set speed for resetting
     *
     * @param resetSpeed reset speed {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.ResetSpeed}
     */
    public void setResetSpeed(@ResetSpeed int resetSpeed) {
        mResetSpeed = resetSpeed;
    }

    /**
     * Get text
     *
     * @return text
     */
    @NonNull
    public String getText() {
        return mText;
    }

    /**
     * Set text
     *
     * @param text text
     */
    public void setText(@NonNull String text) {
        mText = text;
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        invalidate();
    }

    /**
     * Whether text is bold
     *
     * @return true if text style is bold
     */
    public boolean isTextBold() {
        return mIsTextBold;
    }

    /**
     * Set text style bold
     *
     * @param isTextBold bold text style
     */
    public void setTextBold(boolean isTextBold) {
        mIsTextBold = isTextBold;
        mTextPaint.setTypeface(mIsTextBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        invalidate();
    }

    /**
     * Get thumb padding
     *
     * @return thumb padding
     */
    public int getThumbPadding() {
        return mThumbPadding;
    }

    /**
     * Set thumb padding
     *
     * @param thumbPadding thumb padding
     */
    public void setThumbPadding(int thumbPadding) {
        mThumbPadding = thumbPadding;
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        invalidate();
    }

    /**
     * Get thumb width
     *
     * @return thumb width
     */
    public int getThumbWidth() {
        return mThumbWidth;
    }

    /**
     * Set thumb width
     *
     * @param thumbWidth thumb width
     */
    public void setThumbWidth(int thumbWidth) {
        mThumbWidth = thumbWidth;
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        invalidate();
    }

    /**
     * Get thumb height
     *
     * @return thumb height
     */
    public int getThumbHeight() {
        return mThumbHeight;
    }

    /**
     * Set thumb height
     *
     * @param thumbHeight thumb height
     */
    public void setThumbHeight(int thumbHeight) {
        mThumbHeight = thumbHeight;
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        invalidate();
    }

    /**
     * Get background drawable width
     *
     * @return background drawable width
     */
    public int getBackgroundWidth() {
        return mBackgroundWidth;
    }

    /**
     * Get background drawable height
     *
     * @return background drawable height
     */
    public int getBackgroundHeight() {
        return mBackgroundHeight;
    }

    /**
     * Get text padding
     *
     * @return text padding
     */
    public int getTextPadding() {
        return mTextPadding;
    }

    /**
     * Set text padding
     *
     * @param textPadding text padding
     */
    public void setTextPadding(int textPadding) {
        mTextPadding = textPadding;
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        invalidate();
    }

    /**
     * Get text gravity {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.TextGravity}
     *
     * @return text gravity {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.TextGravity}
     */
    public @TextGravity int getTextGravity() {
        return mTextGravity;
    }


    /**
     * Get text color
     *
     * @return text color
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * Set text color
     *
     * @param textColor text color
     */
    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mTextPaint.setColor(textColor);
        invalidate();
    }

    /**
     * Get text size
     *
     * @return text size
     */
    public int getTextSize() {
        return mTextSize;
    }

    /**
     * Set text size
     *
     * @param textSize text size
     */
    public void setTextSize(int textSize) {
        mTextSize = textSize;
        mTextPaint.setTextSize(textSize);
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        invalidate();
    }

    /**
     * Set text gravity {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.TextGravity}
     *
     * @param textGravity text gravity {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.TextGravity}
     */
    public void setTextGravity(@TextGravity int textGravity) {
        mTextGravity = textGravity;
        mStartTextPosition = -1;
        mEndTextPosition = -1;
        invalidate();
    }

    /**
     * Get reset speed {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.ResetSpeed}
     *
     * @return reset speed {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.ResetSpeed}
     */
    @IntRange(from = 10, to = 100)
    public int getLimitProgressForSuccess() {
        return mLimitProgressForSuccess;
    }

    /**
     * Set reset speed {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.ResetSpeed}
     *
     * @param limitProgressForSuccess reset speed {@link com.alexlytvynenko.unlockslideview.UnlockSlideView.ResetSpeed}
     */
    public void setLimitProgressForSuccess(@IntRange(from = 10, to = 100) int limitProgressForSuccess) {
        mLimitProgressForSuccess = limitProgressForSuccess;
    }
}
