package com.summerain0.widget.editor;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/*
 * 剪贴板模块
 * class ClipboardPanel
 * author summerain0
 * email 2351602624@qq.com
 * date 2019-10-17
 */
public class ClipboardPanel
{
    public static final String TAG = "ClipboardPanel";
	// 编辑框
	private CodeEditorView mCodeEditorView;
	// 上下文
	private Context mContext;
	// 
	ActionMode mClipboardActionMode = null;

	// 构造方法
	public ClipboardPanel(CodeEditorView code)
	{
		this.mCodeEditorView = code;
		this.mContext = mCodeEditorView.getContext();
	}

	public void show()
	{
		if (mClipboardActionMode != null) return;
		mCodeEditorView.startActionMode(new ActionMode.Callback(){

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu)
				{
					mClipboardActionMode = mode;
					mode.setTitle(android.R.string.selectTextMode);
					TypedArray array = mContext.getTheme().obtainStyledAttributes(
						new int[] {  
							android.R.attr.actionModeSelectAllDrawable, 
							android.R.attr.actionModeCutDrawable, 
							android.R.attr.actionModeCopyDrawable, 
							android.R.attr.actionModePasteDrawable, 
						}); 
					// 全选
					menu.add(0, 0, 0, mContext.getString(android.R.string.selectAll))
						.setShowAsActionFlags(2)
						.setAlphabeticShortcut('a')
						.setIcon(array.getDrawable(0));
					// 剪切
					menu.add(0, 1, 0, mContext.getString(android.R.string.cut))
						.setShowAsActionFlags(2)
						.setAlphabeticShortcut('x')
						.setIcon(array.getDrawable(1));
					// 复制
					menu.add(0, 2, 0, mContext.getString(android.R.string.copy))
						.setShowAsActionFlags(2)
						.setAlphabeticShortcut('c')
						.setIcon(array.getDrawable(2));
					// 粘贴
					menu.add(0, 3, 0, mContext.getString(android.R.string.paste))
						.setShowAsActionFlags(2)
						.setAlphabeticShortcut('v')
						.setIcon(array.getDrawable(3));
					array.recycle();
					return true;
				}

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu)
				{
					return false;
				}

				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item)
				{
					switch (item.getItemId())
					{
						case 0:
							//_textField.selectAll();
							break;
						case 1:
							//_textField.cut();
							mode.finish();
							break;
						case 2:
							//_textField.copy();
							mode.finish();
							break;
						case 3:
							//_textField.paste();
							mode.finish();
					}
					return false;
				}

				@Override
				public void onDestroyActionMode(ActionMode p1)
				{
					//_textField.selectText(false);
					mClipboardActionMode = null;
				}
			});
	}

	// 隐藏
	public void dismiss()
	{
		if (mClipboardActionMode == null) return;
		mClipboardActionMode.finish();
	}
	
}

