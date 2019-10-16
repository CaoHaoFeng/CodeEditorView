package com.summerain0;
import android.app.Activity;
import android.os.Bundle;
import com.summerain0.widget.editor.CodeEditorView;
import java.io.BufferedReader;
import java.io.FileReader;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		CodeEditorView mCodeEditorView = findViewById(R.id.code);
		mCodeEditorView.setText(fileread("/storage/emulated/0/AppProjects/大文本编辑器/CodeEditorView/app/src/main/java/com/summerain0/widget/editor/CodeEditorView.java"));
		
    }
	
	public static String fileread(String addr)
	{
		try
		{
			StringBuffer result= new StringBuffer();
			BufferedReader iO=new BufferedReader(new FileReader(addr));
			String str=iO.readLine();
			while (str != null)
			{
				result.append(str);
				result.append("\n");
				str = iO.readLine();
			}
			return result.toString();
		}
		catch (Exception e)
		{return "错误";}
    };
	
}
