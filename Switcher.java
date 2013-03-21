/**
 * @author LiuXiaoyuan
 * @creation_date 2012-8-22
 */
package douzi.android.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Scroller;
import android.widget.TextView;


/**
 * 
 * 实现滑动左右选择效果
 * @author LiuXiaoyuan
 *
 */
public class Switcher extends FrameLayout 
	implements OnClickListener{
	
	public final static String TAG = "Switcher";
	
	private View   				mRoot;
	private Button 				mSlideBlock;
	private FrameLayout 		mSlideBlockContainer;
	private TextView 			mLeftTextView;
	private TextView 			mRightTextView;
	private Scroller 			mScroller;
	private int 				mSlideBlockMarginLeft = 0;
	private OnSwitcherChangedListener mOnSwitcherChangedListener;
	private int 				mSwitcherButtonMarginEdge = 2;
	private boolean 			mIsShowSlideBlockText = true;
	private boolean 			mAnimationStarted = false;
	private boolean 			mClickEnable = true;
	
	
	/**
	 * 滑块点击监听
	 */
	private OnClickListener mOnSlideBlockClickListener;

	public Switcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupViews();
	}
	
	/**
	 * 设置Switcher状态改变监听
	 */
	public void setOnSwitcherChangedListener(OnSwitcherChangedListener l){
		mOnSwitcherChangedListener = l;
	}
	
	
	/**
	 * 设置Switcher的基本信息
	 */
	public void setSwitchInfo(String leftText,String rightText){
		mLeftTextView.setText(leftText);
		mRightTextView.setText(rightText);
		if(mIsShowSlideBlockText){
		    mSlideBlock.setText(mCurrentButtonDirection == DIRECTION_LEFT ? leftText : rightText);
		}
	}
	
	public TextView getLeftText(){
	    return mLeftTextView;
	}
	
	public TextView getRightText(){
	    return mRightTextView;
	}
	
	public int getShowDiretion(){
	    return mCurrentButtonDirection;
	}
	
	@Override
	public void setClickable(boolean clickable) {
	    mClickEnable = clickable;
	}
	
	private boolean mSlideable = true;;
	/**
	 * 设置是否可以滑动，此时滑块可以点击
	 */
	public void setSlideable(boolean slideable){
	    mSlideable = slideable;
	}
	
	/**
	 * 设置滑块背景
	 */
	public void setSlideBlockBackground(int resId){
	    mSlideBlock.setBackgroundResource(resId);
	}
	
	/**
	 * 设置滑块是否显示对应文字
	 */
	public void setSlideBlockShowText(boolean showText){
	    mIsShowSlideBlockText = showText;
	    if(!showText){
	        mSlideBlock.setText("");
	    }
	}
	
	/**
	 * 设置滑块点击事件
	 */
	public void setSlideBlcokClickListener(OnClickListener l){
	    mOnSlideBlockClickListener = l;
	}
	
	private void setupViews(){
	    mRoot = inflate(getContext(), R.layout.switcher, null);
		mScroller = new Scroller(getContext());
		addView(mRoot);
		
		// 滑块
		mSlideBlockContainer = new FrameLayout(getContext());
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addView(mSlideBlockContainer, lp);
		mSlideBlock = new Button(getContext());
		mSlideBlock.setBackgroundResource(R.drawable.ic_switcher_button_bg);
		lp = new FrameLayout.LayoutParams(getWidth() / 2, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER_VERTICAL;
		mSlideBlock.setLayoutParams(lp);
		mSlideBlock.setTextColor(Color.WHITE);
		mSlideBlockContainer.addView(mSlideBlock);
		
		// End 滑块
		mLeftTextView = (TextView) mRoot.findViewById(R.id.switcher_leftText);
		mRightTextView = (TextView) mRoot.findViewById(R.id.switcher_rightText);
		mLeftTextView.setOnClickListener(this);
		mRightTextView.setOnClickListener(this);
		mSlideBlock.setText(mLeftTextView.getText());
		mSlideBlock.setOnClickListener(this);
		setBackgroundResource(R.drawable.ic_switcher_bg);
		
//		mSlideBlock.setOnTouchListener(this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    //Log.e(TAG,"onMeasure width:" + getWidth() / 2 + " height:" + getHeight());
	    //mSlideBlock.getLayoutParams().width = getWidth() / 2;
	    //mSlideBlock.getLayoutParams().height = getHeight();
	      mSlideBlock.measure(getWidth() / 2, mSlideBlock.getHeight());
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		//Log.d(TAG,"onLayout");
		super.onLayout(changed, left, top, right, bottom);
		mSlideBlock.layout(0, 0, getWidth() / 2, getHeight());
	}
	
	@Override
	public void onClick(View v) {
	    
	    if(!mClickEnable){
	        return;
	    }
	    
	    if(v == mSlideBlock){
	        if(mOnSlideBlockClickListener != null){
	            mOnSlideBlockClickListener.onClick(mSlideBlock);
	        }
	        return;
	    }
	    
	    if(!mSlideable){
	        return;
	    }
	    
	    int direction;
	    
	    if(v == mLeftTextView ){
	    	direction = DIRECTION_LEFT;
		}else{
			direction = DIRECTION_RIGHT;
		}
	    
		if(!mScroller.isFinished()){
			return;
		}
		
		if(mOnSwitcherChangedListener != null){
			boolean intecepted = mOnSwitcherChangedListener.onSwitchChangeBegin(direction);
			if(intecepted){
				return;
			}
		}
		
		moveSlideBlockTo(direction);
	}
	
	@Override
	public void computeScroll() {
		super.computeScroll();
//		if(mIsFingerMove){
//		    return;
//		}
		if(mScroller.computeScrollOffset()){
			mSlideBlockMarginLeft = mScroller.getCurrX();
			com.qvod.player.utils.Log.d(TAG, "Animating slide block margin left:" + mSlideBlockMarginLeft);
			mSlideBlockContainer.scrollTo(-mSlideBlockMarginLeft, 0);
		    postInvalidate();
		}else if(mAnimationStarted){
		    // 动画结束
		    Log.d(TAG,"animation end slide block margin left:" + mSlideBlockMarginLeft + " totoal w: " + getWidth() +
		            " scroll X: " + mSlideBlock.getScrollX());
	    	if(mOnSwitcherChangedListener != null){
	    		mOnSwitcherChangedListener.onSwitchChanged(mCurrentButtonDirection);
	    	}
	    	mAnimationStarted = false;
		    
		}
	}
	
	
	public final static int DIRECTION_LEFT = 0;
	public final static int DIRECTION_RIGHT = 1;
	private int mCurrentButtonDirection = DIRECTION_LEFT;
	
	/**
	 * 移动滑块到
	 */
	public void moveSlideBlockTo(int direction){
	    if(!mSlideable){
	        return;
	    }
	    
		Log.d(TAG, "buttonMoveTo : " + direction);
		if(direction == mCurrentButtonDirection){
			Log.d(TAG, "buttonMoveTo , current direction equals new , return");
			return;
		}
		CharSequence text = "";
        if( DIRECTION_LEFT == direction ){
            text = mLeftTextView.getText();
        }else{
            text = mRightTextView.getText();
        }
        if(mIsShowSlideBlockText){
            mSlideBlock.setText(text);
        }else{
            mSlideBlock.setText("");
        }
		mAnimationStarted = true;
		if(direction == DIRECTION_LEFT){
			mScroller.startScroll(mRightTextView.getLeft(), 0 , -mRightTextView.getLeft(), 0);
		}else{
			mScroller.startScroll(0, 0, mRightTextView.getLeft() - mSwitcherButtonMarginEdge, 0);
		}
		mCurrentButtonDirection = direction;
		invalidate();
	}
	
	/**
	 * 移动滑块到
	 * @param direction
	 */
	public void moveSlideBlockToImmediate(int direction){
//		 if(!mSlideable){
//			 return;
//		 }
		    
		Log.d(TAG, "buttonMoveTo : " + direction);
		if(direction == mCurrentButtonDirection){
			Log.d(TAG, "buttonMoveTo , current direction equals new , return");
			return;
		}
		CharSequence text = "";
        if( DIRECTION_LEFT == direction ){
            text = mLeftTextView.getText();
        }else{
            text = mRightTextView.getText();
        }
        if(mIsShowSlideBlockText){
            mSlideBlock.setText(text);
        }else{
            mSlideBlock.setText("");
        }
		if(direction == DIRECTION_LEFT){
			mSlideBlockContainer.scrollTo(0, 0);
		}else{
			mSlideBlockContainer.scrollTo(-mRightTextView.getLeft(), 0);
		}
		mCurrentButtonDirection = direction;
		if(mOnSwitcherChangedListener != null){
            mOnSwitcherChangedListener.onSwitchChanged(mCurrentButtonDirection);
        }
	}
	
	
	public static interface OnSwitcherChangedListener{
		/**
		 * 当switch的选择发生改变时回调
		 * @param direction 新位置 {@link Switcher#DIRECTION_LEFT} or {@link Switcher#DIRECTION_RIGHT}
		 */
		public void onSwitchChanged(int direction);
		
		/**
         * 当switch的选择发生改变时回调
         * @param direction 将要到达的新位置 {@link Switcher#DIRECTION_LEFT} or {@link Switcher#DIRECTION_RIGHT}
         * @return 返回true不再终止滚动
         */
        public boolean onSwitchChangeBegin(int direction);
	}

//	private float mTouchDownX;
//	private boolean mIsFingerMove = false;
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        Log.d("douzi",UIUtils.getActionString(event));
//        switch (event.getAction()) {
//        case MotionEvent.ACTION_DOWN:
//            mTouchDownX = event.getX();
//            break;
//        case MotionEvent.ACTION_MOVE:
//            mIsFingerMove = true;
//            float offsetX = mTouchDownX - event.getX();
//            mSlideBlockContainer.scrollTo((int)offsetX, 0);
//        case MotionEvent.ACTION_UP:
//        case MotionEvent.ACTION_CANCEL:
//            mIsFingerMove = false;
//            break;
//        default:
//            break;
//        }
//        return true;
//    }
	
}
