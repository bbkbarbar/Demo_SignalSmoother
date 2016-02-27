package hu.barbar.sample.signalSmoother;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class MyView extends View{

	public int CIRCLE_SIZE = 30;
	public int CROSS_LINES_LONG = CIRCLE_SIZE * 2;	//means the size of one line of four
	
	private int width, height;
	private int x,y;
	
	private int crossColor = Color.RED;
	
	public MyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public MyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyView(Context context) {
		super(context);
	}
	

	@Override
	public void onMeasure(int w, int h){
		super.onMeasure(w, h);
		
		onResize();
	}
	
	private void onResize(){
		width = this.getMeasuredWidth();
		height = this.getMeasuredHeight();
		
		x = width / 2;
		y = height / 2;
	}
	
	@Override
	public void onDraw(Canvas c){
		
		// "Celkereszt" kirajzolasa
		Paint p1 = new Paint();
		p1.setColor(crossColor);
		p1.setStyle(Paint.Style.STROKE);
		//c.drawLine((width/2)-CIRCLE_SIZE, (height/2)-CIRCLE_SIZE, (width/2)+CIRCLE_SIZE, (height/2)+CIRCLE_SIZE, p1);
		c.drawLine((width/2), (height/2)-CROSS_LINES_LONG, (width/2), (height/2)+CROSS_LINES_LONG, p1);
		c.drawLine((width/2)-CROSS_LINES_LONG, (height/2), (width/2)+CROSS_LINES_LONG, (height/2), p1);
		
		// Kor kirajzolasa
		Paint p2 = new Paint();
		p2.setColor(getResources().getColor(R.color.color_main_display));
		p2.setStyle(Paint.Style.FILL_AND_STROKE);
		
		RectF oval = new RectF();
		oval.top = y - (CIRCLE_SIZE / 2);
		oval.bottom = y + (CIRCLE_SIZE / 2);
		oval.left = x - (CIRCLE_SIZE / 2);
		oval.right = x + (CIRCLE_SIZE / 2);
		
		c.drawOval(oval, p2);
	}
	
	public void moveBy(int xDif, int yDif) {
		x += xDif;
		y += yDif;
		
		
		if(x > width){ 
			x = width;
		}
		else if(x < 0){
			x = 0;
		}
		
		if(y > height)
			y = height;
		else if(y < 0)
			y = 0;
		
		// ha teljesen a szélén van akkor minek ujrarajzlni
		//if(! ( ((y==0) || (y==height)) && ((x==0) || (x==height)) ) ){
			this.invalidate();
		//}
		
		
	}
	
	public void moveFromCenter(int toX, int toY) {
		x = (width / 2) + toX;
		y = (height / 2) + toY;
		
		if(x > width){ 
			x = width;
		}
		else if(x < 0){
			x = 0;
		}
		
		if(y > height)
			y = height;
		else if(y < 0)
			y = 0;
		
		this.invalidate();
		
		//ha mind a 2 szélén van akkor egy bool true lesz és csak akkor hagy 
		// ujra rajzolni ha elmozdúl a szélérõl
		// és ha elmozdul a bool false lesz
	}
	
	public void setCircleSize(int size){
		this.CIRCLE_SIZE = size;
	}

}
