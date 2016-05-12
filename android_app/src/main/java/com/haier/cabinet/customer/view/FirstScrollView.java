package com.haier.cabinet.customer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;


public class FirstScrollView extends ScrollView{
	
	private ScrollViewListener scrollViewListener;

	public FirstScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setScrollViewListener(ScrollViewListener scrollViewListener) {  
        this.scrollViewListener = scrollViewListener;  
    }  
  
    @Override  
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {  
        super.onScrollChanged(x, y, oldx, oldy); 
        if(scrollViewListener != null) {
        	scrollViewListener.scrollMove(y);
        }
    }  
    
    public interface ScrollViewListener {  
    	  void scrollMove(int move);
    } 
}

