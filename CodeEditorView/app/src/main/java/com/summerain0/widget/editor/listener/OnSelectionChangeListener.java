package com.summerain0.widget.editor.listener;

/*
 * 选中变化监听
 * class OnSelectionChangeListener
 * author summerain0
 * email 2351602624@qq.com
 * date 2019-10-17
 */
public interface OnSelectionChangeListener
{
	/*
	 * @param boolean 选中状态 未选中时后四个参数均为-1
	 * @param int 开始行
	 * @param int 开始位置
	 * @param int 结束行
	 * @param int 结束位置
	 */
	public void onChanged(boolean state, int startLine, int startPosition, int endLine, int endPosition);
}
