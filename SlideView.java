/*
 * Author:douzifly@gmail.com 
 */
package com.qvod.player.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.qvod.player.R;
import com.qvod.player.core.utils.Log;

/**
 * Slide Menu implementation 
 * @author LiuXiaoyuan
 * @date 2012-8-20
 */
public class SlideView extends FrameLayout {
    
    public static final String TAG = "SlideView";
    
    /** 隐藏左面板的显示模式 */
    public static final int SHOW_MODE_HIDE_LFET = 0;
    /** 显示左面板的显示模式 */
    public static final int SHOW_MODE_SHOW_LEFT = 1;

    private static final int SNAP_VELOCITY = 600;
    
    private ViewGroup   mLeftPanel;
    private ViewGroup   mRightPanel;
    private Context     mContext;
    private float       mTouchDownX = 0;                        // 触摸时的X坐标
    private float       mLastMotinX = 0;                        // 上次位移时的X坐标
    private int		    mRightPanelOffsetX = 0;                 // 右侧面板在X坐标上的位移
    private int       	mRightPanelMaxOffsetX;
    private float       mRightPanelMinPaddingRight = 300;       // 右边面板隐藏后的最小PaddingRight
    private int         mShowMode = SHOW_MODE_HIDE_LFET;        // 当前显示模式
    private float       mTouchSlop = 30;                        // 多少像素才认为有所移动
    private int         mShowLeftThreshold = 100;               // 多少像素的偏移认为应该进入到DISPLAY_MODE_SHOW_LEFT模式
    
    private Scroller    mScroller;
    private VelocityTracker mVelocityTracker;
    private View		mLeftContent;
    private View 		mRightContent;
    private OnSlideViewLisenter mListener;
    private boolean     mNeedHandleTouch = true;
    
    private float 		mInteceptedSlopY;
    private Drawable    mLeftShadowDrawable;
    private int 		mLeftShadowWidth;
    private int 		mCanMoveSlideSectionX = 20;	// 距屏幕左边多少可以拉开SlideView
    
    private boolean mIsShowed;
    
    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        float density = getResources().getDisplayMetrics().density;
        mTouchSlop =(int)( ViewConfiguration.get(getContext()).getScaledTouchSlop()) 
                + 35 * density;
        
        mInteceptedSlopY = (int)( ViewConfiguration.get(getContext()).getScaledTouchSlop()) 
                + 50 * density;
        init();
        mLeftShadowDrawable = getResources().getDrawable(R.drawable.ic_menu_shadow_right);
        mLeftShadowWidth = (int)(density * 6);
        
        mCanMoveSlideSectionX = (int)(20 * density + 0.5);
    }
    
    public void setOnSlideModeListener(OnSlideViewLisenter l){
        mListener = l;
    }
    
    private boolean mIsScrollable = true;
    public void setScrollable(boolean scrollable){
        mIsScrollable = scrollable;
    }
    
    public void setRightViewPaddingRight(int padding){
        mRightPanelMinPaddingRight = padding;
        mRightPanelMaxOffsetX = (int)(getResources().getDisplayMetrics().widthPixels - 
                mRightPanelMinPaddingRight + 
                getResources().getDisplayMetrics().density * 12);
        Log.d(TAG,"mRightPanelMaxOffsetX" + mRightPanelMaxOffsetX);
    }
    
    private boolean isEventInLeftPanel(MotionEvent ev){
    	if(ev.getX() < getWidth() - mRightPanelMinPaddingRight 
    			&& mShowMode == SHOW_MODE_SHOW_LEFT){
//    		Log.d(TAG,"event in left");
			return true;
		}
    	mNeedHandleTouch = true;
    	return false;
    }
    
    public int getOpendX(){
    	return mRightPanelOffsetX;
    }
 
    private void init(){
        mLeftPanel = new FrameLayout(mContext);
        mRightPanel = new FrameLayout(mContext);
        FrameLayout.LayoutParams lp = 
                new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mLeftPanel, lp);
        addView(mRightPanel, lp);
        mScroller = new Scroller(mContext);
        mRightPanel.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.d(TAG, "rightPanel onTouch display mode:" + mShowMode);
				if(mShowMode == SHOW_MODE_SHOW_LEFT){
					if(isEventInLeftPanel(event)){
					    if(event.getAction() == MotionEvent.ACTION_DOWN){
					        mNeedHandleTouch = false;
					    }
					    return false;
					}
					return true;
				}else if(mShowMode == SHOW_MODE_HIDE_LFET){
					return true;
				}
				return false;
			}
		});
    }
    
    /**
     * 设置左边视图的内容
     */
    public void setLeftContent(View view){
    	if(mLeftContent == view){
    		return;
    	}
    	if(mLeftContent != null){
    		mLeftPanel.removeView(mLeftContent);
    	}
        mLeftPanel.addView(view);
        mLeftContent = view;
    }
    
    /**
     * 设置右边数图的内容
     */
    public void setRightContent(View view){
    	if(mRightContent == view){
    		return;
    	}
    	if(mRightContent != null){
    		mRightPanel.removeView(mRightContent);
    	}
    	mRightPanel.addView(view);
    	mRightContent = view;
    }
    
    private boolean mShoudDrawTouchSection = false;
    private Paint mPaint = new Paint();
    {
    	mPaint.setColor(Color.argb(100, 0, 0, 0));
    	mPaint.setAntiAlias(true);
    	mPaint.setStyle(Style.FILL);
    }
    
    private void invalidate(boolean shouldDrawTouchSection){
//    	mShoudDrawTouchSection = shouldDrawTouchSection;
//    	invalidate();
    }
    
    /** 
     * 设置显示模式 
     */
    public void setDisplayMode(int mode){
        Log.d(TAG,"setDisplayMode:"+mode);
        if(!mScroller.isFinished()){
            Log.d(TAG,"scroller not finished renturn");
            return;
        }
//        if(mShowMode != mode && mListener != null){
//            mListener.onSlideViewShowModeChanged(mode);
//        }
        if(mListener != null){
            mListener.onSlideViewShowModeChanged(mode);
        }
        mShowMode = mode;
        beginAnim(mode);
    }
    
    public int getDisplayMode(){
    	return mShowMode;
    }
    
    private void beginAnim(int mode){
        float finalX = 0;
        if(mode == SHOW_MODE_HIDE_LFET){
           finalX = 0 - mRightPanelOffsetX;
        }else{
            finalX = getWidth() - mRightPanelMinPaddingRight - mRightPanelOffsetX;
        }
        Log.d(TAG, " startScroll , from : " + mRightPanelOffsetX + " to :" + finalX);
        mScroller.startScroll((int)mRightPanelOffsetX, 0, (int)finalX, 0, 200);
        invalidate();
    }
    
    @Override
    public void computeScroll() {
       
        super.computeScroll();
        if(mScroller.computeScrollOffset()){
            mRightPanelOffsetX = mScroller.getCurrX();
          //  Log.d(TAG, "computeScroll scrolling x :" + mRightPanelOffsetX);
            mRightPanel.scrollTo(-(int)mRightPanelOffsetX, 0);
            postInvalidate();
        }
    }
    
    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {

    	if(child == mLeftPanel){
    		if(mRightPanelOffsetX > 0){
//    			Log.d("douzi" ,"drawLeft");
    		}else{
//    			Log.d("douzi" ,"do not drawLeft");
    			return true;
    		}
    	}
    	
//    	Log.d(TAG,"mRightPanelOffsetX:" + mRightPanelOffsetX);
        if(child == mRightPanel && mRightPanelOffsetX >= 0){
//        	Log.d("douzi" ,"drawRight");
            int count1 = canvas.save();
            if(mRightPanelOffsetX > 0){
        		// 绘制阴影
        		if(mLeftShadowDrawable.getBounds().width() == 0){
        			mLeftShadowDrawable.setBounds(0, 0, mLeftShadowWidth, getHeight());
        		}
//        		Log.d("douzi","drawShadow");
        		int count2 = canvas.save();
        		canvas.translate(mRightPanelOffsetX, 0);
        		mLeftShadowDrawable.draw(canvas);
        		
        		canvas.restoreToCount(count2);
        		canvas.translate(mLeftShadowWidth, 0);
        	}
            
           
            
            canvas.clipRect(mRightPanelOffsetX, 0, getWidth(), getHeight());
            boolean ret = super.drawChild(canvas, child, drawingTime);
            canvas.restoreToCount(count1);
            
            ///------ 绘制拖动区域
//            if(mShoudDrawTouchSection){
//        		Log.d(TAG,"onDraw");
//        		canvas.save();
//        		int offsetx = mRightPanelOffsetX == 0 ? 0 :  mRightPanelOffsetX + mLeftShadowDrawable.getIntrinsicWidth();
//        		canvas.translate(offsetx, 0);
//        		canvas.drawRect(0, 0, mCanMoveSlideSectionX, getHeight(), mPaint);
//        		canvas.restore();
//        	}
    		//----------------
            
            return ret;
        }
        
        boolean ret =  super.drawChild(canvas, child, drawingTime);
        return ret;
    }
    
    @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		Log.i(TAG, "onMeasure");
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
//		Log.i(TAG, "onLayout");
		
		if (! mIsShowed) {
			mIsShowed = true;
			if(mListener != null){
	    		mListener.onSlideViewShowed();
	    	}
		}
	}

	boolean firstMove = true;
   
    boolean moveToLeftAlign = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//       Log.i(TAG, "onTouchEvent "  + UIUtils.getActionString(event));
       
       if (!mIsScrollable) {
           return false;
       }
       
       if(!mNeedHandleTouch){
           return super.onTouchEvent(event);
       }
        switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE:
            if(firstMove){
                firstMove = false;
                if(mListener != null){
                    mListener.onSlideViewBeginMove();
                }
            }
            
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
            float x = event.getX();
            float offsetX = x - mLastMotinX ;
            mRightPanelOffsetX += offsetX;
            mLastMotinX = x;
//            Log.i(TAG, "touch move , mRightPanelOffsetX : " + mRightPanelOffsetX);
            mRightPanelOffsetX = mRightPanelOffsetX < 0 ? 0 : mRightPanelOffsetX;
            mRightPanelOffsetX = mRightPanelOffsetX > mRightPanelMaxOffsetX ? mRightPanelMaxOffsetX : mRightPanelOffsetX;
            if(mRightPanelOffsetX == 0){
                if(!moveToLeftAlign){
                    mListener.onSlideViewMoveAlignLeft();
                    moveToLeftAlign = true;
                }
            }else{
                if(moveToLeftAlign){
                    mListener.onSlideViewMoveLeaveLeft();
                    moveToLeftAlign = false;
                }
            }
            mRightPanel.scrollTo(-(int)mRightPanelOffsetX, 0);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
        	invalidate(false);
            firstMove = true;
            if(mListener != null){
                mListener.onSlideViewEndMove();
            }
            if(mShowMode == SHOW_MODE_SHOW_LEFT && mTouchDownX > (getWidth() - mRightPanelMinPaddingRight) 
                && Math.abs((event.getX() - mTouchDownX)) <= 10){
                // 如果UP时，左面板打开，此时按下和弹起都在右面板上时，折叠左面板
                setDisplayMode(SHOW_MODE_HIDE_LFET);
                
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                return true;
            }
            
            final VelocityTracker velocityTracker = mVelocityTracker;
            int velocityX = 0;
            if(velocityTracker != null){
                velocityTracker.computeCurrentVelocity(1000);
                velocityX = (int) velocityTracker.getXVelocity();
            }
//            Log.d(TAG,"move up, veloctiyX:" + velocityX);
            if(velocityX > SNAP_VELOCITY){
                setDisplayMode(SHOW_MODE_SHOW_LEFT);
            }else if(velocityX < - SNAP_VELOCITY){
                setDisplayMode(SHOW_MODE_HIDE_LFET);
            }else if(mRightPanelOffsetX > mShowLeftThreshold){
                setDisplayMode(SHOW_MODE_SHOW_LEFT);
            }else{
                setDisplayMode(SHOW_MODE_HIDE_LFET);
            }
            
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            break;
        default:
            break;
        }
        return true;
    }
    
    private boolean mDispatchEventToDragController = false;
    public void setDispatchEventToDragController(boolean enable){
        mDispatchEventToDragController = enable;
    }
    private float mDownXForDrag = 0;
    private float mDownYForDrag = 1;
    public float getDownXForDrag(){
        return mDownXForDrag;
    }
    
    public float getDownYForDrag(){
        return mDownYForDrag;
    }

    float mDownY = 0;
    float mDownX = 0;
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	return super.dispatchTouchEvent(ev);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
//        Log.e(TAG,UIUtils.getActionString(event));
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            mDownXForDrag = event.getX();
            mDownYForDrag = event.getY();
        }
        if(mDispatchEventToDragController){
//            Log.d(TAG,"d:" + UIUtils.getActionString(event));
            return true;
        }
        
//        Log.d(TAG, "onInterceptTouchEvent " + UIUtils.getActionString(event) + " x:" + event.getX() );
        if (!mIsScrollable){
//            Log.d(TAG, " can not scroll mode  return false ");
            return false;
        }
        
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mTouchDownX = event.getX();
            mLastMotinX = event.getX();
            mDownY = event.getY();
            mDownX = event.getX();
            if(isEventInLeftPanel(event)){
                // 当左面板展开，在左面板区域点击时，不再截获事件
                return false;
            }
            if(mShowMode == SHOW_MODE_SHOW_LEFT && mTouchDownX >getWidth() - mRightPanelMinPaddingRight){
                return true;
            }
            break;
        case MotionEvent.ACTION_MOVE:
            boolean intercepted = false;
            float x = event.getX();
            float yDiff = Math.abs(mDownY - event.getY());
            if(isEventInLeftPanel(event)){
                // 当左面板展开，在左面板区域点击时，不再截获事件
            	intercepted = false;
            }else{
            	if(yDiff > mInteceptedSlopY){
            		intercepted = false;
            		return intercepted;
            	}
	            mLastMotinX = x;
	            float offsetX = x - mTouchDownX;
	            Log.d(TAG, "itouch move offsexX:" + offsetX + " downX:" + mTouchDownX + " curX:" + x);
	            if(mShowMode == SHOW_MODE_HIDE_LFET){
	                Log.d(TAG,"hide left mode");
//	            	if(offsetX <= 0){
//	            		// 往左滑，不截获
//	            		intercepted = false;
//	            	}else if(Math.abs(offsetX) > mTouchSlop){
//	            		intercepted = true;
//	            	}else{
//	            		intercepted = false;
//	            	}
	                // 如果按下点在左侧滑动区域内，那么截获事件，否则不截获
	                if(mDownX < mCanMoveSlideSectionX){
	                	intercepted = true;
	                	invalidate(true);
	                }else{
	                	intercepted = false;
	                }
	                
	            }else{ 
	            	intercepted = true;
	            }
            }
            Log.d(TAG, "move intercepted:" + intercepted);
            return intercepted;
        case MotionEvent.ACTION_UP:
        	invalidate(false);
        	if(mShowMode == SHOW_MODE_SHOW_LEFT && mTouchDownX > (getWidth() - mRightPanelMinPaddingRight) 
        			&& Math.abs((event.getX() - mTouchDownX)) <= 10){
        		// 如果UP时，左面板打开，此时按下和弹起都在右面板上时，折叠左面板
        		setDisplayMode(SHOW_MODE_HIDE_LFET);
        		return true;
        	}
        	break;
        default:
            break;
        }
        
       return super.onInterceptTouchEvent(event);
    }
    
    @Override
    protected void onAttachedToWindow() {
    	super.onAttachedToWindow();
    }

    public static interface OnSlideViewLisenter{
        public void onSlideViewShowModeChanged(int mode);
        public void onSlideViewBeginMove();
        public void onSlideViewEndMove();
       
        /**
         * 左边移动到最边缘
         */
        public void onSlideViewMoveAlignLeft();
        
        /**
         * 离开最左边
         */
        public void onSlideViewMoveLeaveLeft();
        
        public void onSlideViewShowed();
    }
}
