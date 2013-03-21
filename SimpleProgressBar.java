/**
 * LiuXiaoyuan at 2012-5-11
 */
package douzi.android.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import douzi.android.widgets.R;

/**
 * @author LiuXiaoyuan
 * 
 */
public class SimpleProgressBar extends View {

	private int mMax = 100;
	private int mProgress = 0;
	private float mProgressWidth = 0;
	private Paint mPaint;

	public SimpleProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleProgressBar);
		int color = typeArray.getColor(R.styleable.SimpleProgressBar_progressColor, 0x00ffffff);
		typeArray.recycle();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(color);
		mPaint.setStyle(Style.FILL);
	}

	public SimpleProgressBar(Context context) {
		this(context, null);
	}

	public void setMax(int max) {
		mMax = max;
		invalidate();
	}

	public void setProgressColor(int color) {
		mPaint.setColor(color);
		invalidate();
	}

	public void setProgress(int progress) {
		mProgress = progress;
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final float width = getWidth();
		final float height = getHeight();
	
		if (width > 0 && height > 0) {
			float padingRight = getPaddingRight();
			float padingLeft = getPaddingLeft();
			float padingTop = getPaddingTop();
			float padingBottom = getPaddingBottom();
			
			float l = padingLeft;
			float r = l + ((float) mProgress / (float) mMax) * (width - padingLeft - padingRight); 
			float t = padingTop;
			float b = height - padingBottom;
			canvas.drawRect(l, t, r, b, mPaint);
		}
	}

}
