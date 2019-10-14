package com.summerain0.widget.editor;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.summerain0.widget.editor.CodeEditorView;

/*
 * 默认的手势检测
 * class DefaultGestureListener
 * author summerain0
 * email 2351602624@qq.com
 * date 2019-8-10
 */
public class DefaultGestureListener extends GestureDetector.SimpleOnGestureListener
{
	public static final String TAG = "DefaultGestureListener";

	// 编辑框主体
	private CodeEditorView mCodeEditorView;
	// 上下文
	private Context mContext;
	// 滑动方向 -1X 1Y
	private int direction = 0;

	// 构造方法
	public DefaultGestureListener(CodeEditorView code)
	{
		this.mCodeEditorView = code;
		this.mContext = mCodeEditorView.getContext();
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		// 判断是否可编辑
		if (mCodeEditorView.isCanEditable())
		{
			mCodeEditorView.showSoftInput(true);
		}
		return super.onSingleTapUp(e);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		// 滑动
		if (e2.getPointerCount() == 1)
		{
			if (Math.abs(distanceX) > Math.abs(distanceY))
			{
				direction = -1;
				distanceY = 0;
			}
			if (Math.abs(distanceX) < Math.abs(distanceY))
			{
				direction = 1;
				distanceX = 0;
			}
			mCodeEditorView.scrollView(distanceX, distanceY);
		}
		return super.onScroll(e1, e2, distanceX, distanceY);
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
		// 若正在惯性滑动，则强行停止
		if (!mCodeEditorView.isFlingScrollFinished())
		{
			mCodeEditorView.stopFlingScroll();
		}
		return super.onDown(e);
	}

	public void onUp(MotionEvent ev)
	{
		// 初始化数值
		direction = 0;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		onLongPress(e);
		return super.onDoubleTapEvent(e);
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
		super.onLongPress(e);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		// 惯性滑动
		if (direction == -1) velocityY = 0;
		if (direction == 1) velocityX = 0;
		mCodeEditorView.flingScroll((int)-velocityX, (int)-velocityY);
		onUp(e2);
		return super.onFling(e1, e2, velocityX, velocityY);
	}
}
