/*
 * CopyRight© summerain0
 * 从0绘制大文本编辑器，支持高亮，自定义高亮
 * 同时具备一些基本功能
 ***************************
 * 公开方法使用文档
 * void setCanEditable(boolean boo) 设置是否可编辑
 * boolean isCanEditable() 获取是否可编辑
 * void setTextSize(int size) 设置字体大小
 * int getTextSize(int size) 获取字体大小
 * void showSoftInput(boolean show) 是否显示软键盘
 * void scrollTo(int x, int y) 移动至指定位置
 * void scrollBy(int x, int y) 移动至当前相对的位置
 * void setText(String text) 设置文本
 * String getText() 获取文本
 * void showLineNumber(boolean boo) 是否显示行号
 * 233
 */
package com.summerain0.widget.editor;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Scroller;
import com.summerain0.widget.editor.CodeEditorView;
import com.summerain0.widget.editor.listener.OnSelectionChangeListener;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.summerain0.R;

/*
 * 代码编辑器控件
 * class CodeEditorView
 * author summerain0
 * email 2351602624@qq.com
 * date 2019-8-10
 */
public class CodeEditorView extends View
{
	public static final String TAG = "CodeEditorView";
	// 各种的默认值
	public static final int DEFAULT_TEXT_SIZE = 48;

	// 上下文
	private Context mContext;
	// 手势管理
	private GestureDetector mGestureDetector;
	// 剪贴板
	private ClipboardPanel mClipboardPanel;
	// 选中变化监听
	private OnSelectionChangeListener mOnSelectionChangeListener;
	// 滑动管理
	private Scroller mScroller;
	// 行号画笔
	private Paint mLineTextPaint;
	// 正文画笔
	private Paint mTextPaint;
	// 是否可编辑
	private boolean mIsEditable = false;
	// 是否显示行号
	private boolean mIsShowLineNumber = true;
	// 原字体大小
	private int mLastTextSize = DEFAULT_TEXT_SIZE;
	// 字体大小
	private int mTextSize = DEFAULT_TEXT_SIZE;
	// 绘画原点坐标
	private float mScrollX = 0, mScrollY = 0;
	// 滑动最大值
	private float mMaxScrollX = 0, mMaxScrollY = 0;
	// 缓存行，在显示部分的上下再绘制一些，以免滑动过快出现绘制来不及的情况
	private int mCacheLine = 3;
	// 绘制起止点
	private int mDrawStartLine = 0, mDrawEndLine = 0;
	// 文字高度
	private float mTextHeight = 0;
	// 正文偏移
	private float mLeftOffset = 0;
	// 缩放锁
	private boolean mZooming = false;
	// 原两指间距离
	private double mPointersDistance = 0;
	
	// 正文
	ArrayList<String> mTextArrayList = new ArrayList<String>();
	// 光标左图片
	private Bitmap mCursorLeftBitmap;
	// 光标右图片
	private Bitmap mCursorRightBitmap;
	// 选中光标长宽
	private float mSelectCursorBitmapWidth = 0,mSelectCursorBitmapHeight = 0;
	// 指向光标长宽
	
	// 构造方法
	public CodeEditorView(Context context)
	{
		this(context, null);
	}

	public CodeEditorView(Context context, AttributeSet attrs)
	{
		super(context, attrs, 0);
		this.mContext = context;
		setLongClickable(true);
		setFocusableInTouchMode(true);
		setHapticFeedbackEnabled(true);
		// 初始化
		init();
		initPaint();
	}

	// View被创建时执行
	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
	}

	// View被销毁时执行
	@Override
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
	}

	// 重写 onCheckIsTextEditor()
	@Override
    public boolean onCheckIsTextEditor()
    {
        return true;
    }

	// 初始化
	private void init()
	{
		// 初始化图片
		mCursorLeftBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.abc_text_select_handle_left_mtrl_light);
		mCursorRightBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.abc_text_select_handle_right_mtrl_light);
		// 保存长宽
		mSelectCursorBitmapWidth = mCursorLeftBitmap.getWidth();
		mSelectCursorBitmapHeight = mCursorLeftBitmap.getHeight();
		
		// 手势管理
		mGestureDetector = new GestureDetector(mContext, new DefaultGestureListener(CodeEditorView.this));
		mGestureDetector.setIsLongpressEnabled(true);
		// 剪贴板
		mClipboardPanel = new ClipboardPanel(CodeEditorView.this);
		// 选中变化监听
		mOnSelectionChangeListener = new OnSelectionChangeListener(){
			@Override
			public void onChanged(boolean state, int startLine, int startPosition, int endLine, int endPosition)
			{
				if (state)
				{
					mClipboardPanel.show();
				}
				else
				{
					mClipboardPanel.dismiss();
				}
			}
		};
		// 滑动管理
		mScroller = new Scroller(mContext);
	}

	// 初始化画笔
	private void initPaint()
	{
		// 行号画笔
		mLineTextPaint = new Paint();
		mLineTextPaint.setTextSize(mTextSize);
		mLineTextPaint.setTypeface(Typeface.MONOSPACE);
		mLineTextPaint.setColor(Color.BLACK);
		mLineTextPaint.setAntiAlias(true);
		// 正文画笔
		mTextPaint = new Paint();
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setTypeface(Typeface.MONOSPACE);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setAntiAlias(true);
	}

	// 监听滚动
	@Override
	public void computeScroll()
	{
		if (mScroller.computeScrollOffset())
		{
			mScrollX = mScroller.getCurrX();
			mScrollY = mScroller.getCurrY();
			invalidate();
		}
		super.computeScroll();
	}

	// 监听屏幕触摸方法
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// 把事件交给 DefaultGestureListener 处理
		if (isFocusable()) // 判断是否获得焦点
		{
			// 拦截事件
			getParent().requestDisallowInterceptTouchEvent(true);
			// 缩放
			onZoomTouchEvent(event);
			// 手势
			mGestureDetector.onTouchEvent(event);
		}
		else
		{
			// 请求获得焦点
			if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP)
			{
				requestFocus();
			}
		}
		return super.onTouchEvent(event);
	}

	// 两指缩放
	// 原理，计算两指间距离差
	private void onZoomTouchEvent(MotionEvent e)
	{
		switch (e.getAction())
		{
			case MotionEvent.ACTION_MOVE:
				if (e.getPointerCount() == 2)
				{
					// 获取坐标
					float x1 = e.getX(0);
					float y1 = e.getY(0);
					float x2 = e.getX(1);
					float y2 = e.getY(1);
					// 判断
					if (!mZooming)
					{
						mLastTextSize = mTextSize;
						mPointersDistance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1 , 2));
						mZooming = true;
					}
					// 实时距离
					double mDistance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1 , 2));
					// 相减后换算为字体大小
					mTextSize = (int)(mLastTextSize + (mDistance - mPointersDistance) / 50);
					if (mTextSize < 10) mTextSize = 10;
					this.setTextSize(mTextSize);
				}
				break;

			case MotionEvent.ACTION_UP:
				mLastTextSize = mTextSize;
				mZooming = false;
				break;
		}
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		long a = System.currentTimeMillis();

		// 绘制行号
		drawLineText(canvas);
		// 绘制文本
		drawText(canvas);

		// 无用，查看一些参数而已
		long time = System.currentTimeMillis() - a;
		if (time == 0) time = 1;
		canvas.drawText(mLastTextSize + "  " + mTextSize + "  " + (1000 / time), 0, getHeight(), mTextPaint);
		super.onDraw(canvas);
	}

	// 绘制行号
	private void drawLineText(Canvas canvas)
	{
		// 总行数
		int allLine = mTextArrayList.size();
		// 计算偏移值
		if (mIsShowLineNumber)
			mLeftOffset = mLineTextPaint.measureText(mTextArrayList.size() + "  ");
		else
			mLeftOffset = 0;
		// 实时计算最大值
		calculateMaxScrollX();
		calculateMaxScrollY();
		// 计算起止点
		mDrawStartLine = (int)(mScrollY / mTextHeight) - mCacheLine;
		mDrawStartLine = mDrawStartLine < 0 ? 0 : mDrawStartLine;
		mDrawEndLine = (int)((mScrollY + getHeight()) / mTextHeight) + mCacheLine;
		mDrawEndLine = mDrawEndLine > allLine ? allLine : mDrawEndLine;
		// 绘制开始
		if (mIsShowLineNumber)
			for (int i = mDrawStartLine;i < mDrawEndLine;i++)
			{
				canvas.drawText(String.valueOf(i + 1), 0 - mScrollX, mTextHeight * (i + 1) - mScrollY, mLineTextPaint);
			}
	}

	// 绘制文本
	private void drawText(Canvas canvas)
	{
		for (int i = mDrawStartLine;i < mDrawEndLine;i++)
		{
			canvas.drawText(mTextArrayList.get(i), mLeftOffset - mScrollX, mTextHeight * (i + 1) - mScrollY, mTextPaint);
		}		
	}

	// 移动视图
	protected void scrollView(float distanceX, float distanceY)
	{
		mScrollX += distanceX;
		mScrollY += distanceY;
		checkScrollX();
		checkScrollY();
		invalidate();
	}

	// 检查X/Y轴
	private void checkScrollX()
	{
		if (mScrollX < 0) mScrollX = 0;
		if (mScrollX > mMaxScrollX) mScrollX = mMaxScrollX;
	}

	private void checkScrollY()
	{
		if (mScrollY < 0) mScrollY = 0;
		if (mScrollY > mMaxScrollY) mScrollY = mMaxScrollY;
	}

	// 计算X/Y最大值
	private void calculateMaxScrollX()
	{
		int allLine = mTextArrayList.size();
		//
		for (int i = 0;i < allLine;i++)
		{
			float width = mTextPaint.measureText(mTextArrayList.get(i) + "        ");
			if (width > mMaxScrollX) mMaxScrollX = width;
		}
	}

	private void calculateMaxScrollY()
	{
		int allLine = mTextArrayList.size();
		//
		mMaxScrollY = mTextHeight * allLine;
	}

	// 惯性滑动
	protected void flingScroll(int vx, int vy)
	{
		mScroller.fling((int)mScrollX, (int)mScrollY, vx, vy, 0, (int)mMaxScrollX, 0, (int)mMaxScrollY);
	}

	// 获取当前惯性滚动状态
	protected boolean isFlingScrollFinished()
	{
		return mScroller.isFinished();
	}

	// 停止当前惯性滑动
	protected void stopFlingScroll()
	{
		mScroller.forceFinished(true);
	}

	// **********开放方法**********

	/*
	 * 显示/隐藏软键盘
	 * @param boolean 是否显示软键盘
	 */
	public void showSoftInput(boolean show)
	{
		InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (show)
		{
			im.showSoftInput(CodeEditorView.this, 0);
		}
		else
		{
			im.hideSoftInputFromWindow(CodeEditorView.this.getWindowToken(), 0);
		}
	}

	/*
	 * 设置是否可编辑
	 * @param boolean 是否可编辑
	 */
	public void setCanEditable(boolean boo)
	{
		this.mIsEditable = boo;
	}

	/*
	 * 获取是否可编辑
	 * @return boolean 是否可编辑
	 */
	public boolean isCanEditable()
	{
		return this.mIsEditable;
	}

	/* 
	 * 是否显示行号
	 * @param boolean 是否显示
	 */
	public void showLineNumber(boolean boo)
	{
		this.mIsShowLineNumber = boo;
		invalidate();
	}

	/*
	 * 滚动到相对位置的什么地方
	 * @param int 移动的x距离
	 * @param int 移动的y距离
	 */
	@Override
	public void scrollBy(int x, int y)
	{
		mScroller.startScroll((int)mScrollX, (int)mScrollY, x, y);
	}

	/* 
	 * 滚动到指定左边，原点为屏幕左上角
	 * @param int x轴
	 * @param int y轴
	 */
	@Override
	public void scrollTo(int x, int y)
	{
		scrollBy((int)(x - mScrollX), (int)(y - mScrollY));
	}

	/*
	 * 选中指定文本 目前暂无具体功能
	 */
	public void seleteText()
	{
		mOnSelectionChangeListener.onChanged(true, 0, 0, 0, 0);
	}

	/*
	 * 设置字体大小
	 * @param int 字体大小
	 */
	public void setTextSize(int size)
	{
		this.mTextSize = size;
		// 更新字体大小
		Paint.FontMetrics fontMetrics = mLineTextPaint.getFontMetrics();
		mTextHeight = fontMetrics.descent - fontMetrics.ascent;
		// 更新数据
		mLineTextPaint.setTextSize(mTextSize);
		mTextPaint.setTextSize(mTextSize);
		// 刷新
		invalidate();
	}

	/*
	 * 获取字体大小
	 * @rerurn int 字体大小
	 */
	public int getTextSize()
	{
		return this.mTextSize;
	}

	/*
	 * 设置文字
	 * @param String 文字
	 */
	public void setText(String text)
	{
		// 切割文本
		String[] list = text.split("\n");
		// 添加进文本
		mTextArrayList = new ArrayList<String>();
		mTextArrayList.ensureCapacity(list.length);
		for (String str : list)
		{
			mTextArrayList.add(str);
		}
		// 初始化数据
		mScrollX = 0;
		mScrollY = 0;
		// 计算文字高度
		Paint.FontMetrics fontMetrics = mLineTextPaint.getFontMetrics();
		mTextHeight = fontMetrics.descent - fontMetrics.ascent;
		// 刷新
		invalidate();
	}

	/* 
	 * 获取文本
	 * @return String 文本
	 */
	public String getText()
	{
		// 记录行数
		int allLine = mTextArrayList.size();
		StringBuffer buffer = new StringBuffer();
		// 循环加值
		for (int i = 0;i < allLine;i++)
		{
			buffer.append(mTextArrayList.get(i));
			if (i + 1 != allLine) buffer.append("\n");
		}
		return buffer.toString();
	}

}
