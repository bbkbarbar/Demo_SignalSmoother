package hu.barbar.commonObjects.graphView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class GraphView extends View {

	private int numberOfAxesLabels = 1;
	private float textSize = 20.0f;
	
	private Paint paint;
	private float[] xValues, yValues;
	int valueCount, maxValueCount;
	private long startTime; 
	
	private int maxLengthOfArrays;
	private float maxX, maxY, minX, minY, locxAxis, locyAxis;
	private float fixedMaxY, fixedMinY;
	private int vectorLength;
	private int axes = 1;
	
	
	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public GraphView(Context context) {
		super(context);
	}
	
	public GraphView(Context context, int maxValueC) {
		super(context);
		
		initFixedMinMaxValues();
		
		this.axes = 1;
		this.maxValueCount = maxValueC;
		
		this.valueCount = 0;
		xValues = new float[maxValueCount];
		yValues = new float[maxValueCount];
		
		vectorLength = 0;
		paint = new Paint();

		getAxes(xValues, yValues);
		
	}
	
	
	private void initFixedMinMaxValues() {
		this.fixedMinY = 0;
		this.fixedMaxY = 0;
	}

	public void refreshYValue(float newVal){
		
		if(valueCount == maxValueCount){
			
			for(int i=1; i<maxValueCount; i++){
				yValues[i-1] = yValues[i];
				xValues[i-1] = xValues[i];
			}
			
			yValues[maxValueCount-1] = newVal;
			
			xValues[maxValueCount-1] = ( (System.currentTimeMillis() - startTime)/1000 ); // kezdés óta eltelmt másodperc
			
		}else{
		
			if(valueCount == 0){
				yValues[valueCount] = newVal;
				
				this.startTime = System.currentTimeMillis();
				xValues[valueCount] = 0f;
			}
			else{
				yValues[valueCount] = newVal;
				
				xValues[valueCount] = ( (System.currentTimeMillis() - startTime)/1000 ); // kezdés óta eltelmt másodperc
			}
			valueCount++;
		}
		
		//vectorLength = xValues.length;
		vectorLength = valueCount;
		paint = new Paint();

		getAxes(xValues, yValues);
		
		this.invalidate();
	}
	
	
	public void setScaleY(float min, float max){
		this.fixedMinY = min;
		this.fixedMaxY = max;
	}
	
	
	public GraphView(Context context, float[] xvalues, float[] yvalues, int axes, int maximumLengthOfArrays) {
		super(context);
		
		initFixedMinMaxValues();
		
		this.xValues = xvalues;
		this.yValues = yvalues;
		this.axes = axes;
		vectorLength = xvalues.length;
		this.maxLengthOfArrays = maximumLengthOfArrays;
		paint = new Paint();

		getAxes(xvalues, yvalues);
		
	}
	
	
	public void setVals(float[] xvalues, float[] yvalues, int axes){
		this.xValues = xvalues;
		this.yValues = yvalues;
		this.axes = axes;
		vectorLength = xvalues.length;
		paint = new Paint();

		getAxes(xvalues, yvalues);
		
		this.invalidate();
	}
	
	
	public void refreshValues(float xvalue, float yvalue){
		
		if(xValues.length<maxLengthOfArrays){
			xValues[xValues.length] = xvalue;
			yValues[yValues.length] = yvalue;
		}else{
			for(int i=1; i<xValues.length; i++){
				xValues[i-1] = xValues[i];
				yValues[i-1] = yValues[i];
			}
			xValues[xValues.length-1] = xvalue;
			yValues[yValues.length-1] = yvalue;
		}
		
		vectorLength = xValues.length;
		paint = new Paint();

		getAxes(xValues, yValues);
		
		this.invalidate();
			
		
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		float canvasHeight = getHeight();
		float canvasWidth = getWidth();
		int[] xvaluesInPixels = toPixel(canvasWidth, minX, maxX, xValues); 
		int[] yvaluesInPixels = toPixel(canvasHeight, minY, maxY, yValues);
		int locxAxisInPixels = toPixelInt(canvasHeight, minY, maxY, locxAxis);
		int locyAxisInPixels = toPixelInt(canvasWidth, minX, maxX, locyAxis);
		String xAxis = "x-axis";
		String yAxis = "y-axis";

		paint.setStrokeWidth(2);
		canvas.drawARGB(255, 255, 255, 255);
		for (int i = 0; i < vectorLength-1; i++) {
			paint.setColor(Color.RED);
			canvas.drawLine(
						xvaluesInPixels[i],
						canvasHeight-yvaluesInPixels[i],
						xvaluesInPixels[i+1],
						canvasHeight-yvaluesInPixels[i+1],
						paint
					);
		}
		
		paint.setColor(Color.BLACK);
		canvas.drawLine(0,canvasHeight-locxAxisInPixels,canvasWidth,canvasHeight-locxAxisInPixels,paint);
		canvas.drawLine(locyAxisInPixels,0,locyAxisInPixels,canvasHeight,paint);
		
		//Automatic axes markings, modify n to control the number of axes labels
		if (axes!=0){
			float temp = 0.0f;
			
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(textSize);
			
			for(int i = 1;  i <= numberOfAxesLabels;  i++){
				temp = Math.round(10 * (minX + (i-1) * (maxX-minX) / numberOfAxesLabels) ) / 10;
				canvas.drawText(
							Float.toString(temp),  
							(float) toPixelInt (canvasWidth, minX, maxX, temp),
							canvasHeight - locxAxisInPixels + textSize,
							paint
						);
				
				temp = Math.round(10 * (minY + (i-1) * (maxY-minY) / numberOfAxesLabels) ) /10;
				canvas.drawText(
							Float.toString(temp), 
							locyAxisInPixels + textSize, 
							canvasHeight - (float) toPixelInt(canvasHeight, minY, maxY, temp),
							paint
						);
			}
			
			canvas.drawText(
					Float.toString(maxX),
					(float) toPixelInt(canvasWidth, minX, maxX, maxX),
					canvasHeight - locxAxisInPixels + textSize,
					paint
				);
			canvas.drawText(
					Float.toString(maxY), 
					locyAxisInPixels + textSize, 
					canvasHeight - (float) toPixelInt(canvasHeight, minY, maxY, maxY),
					paint
				);
			//canvas.drawText(xAxis, canvasWidth/2,canvasHeight-locxAxisInPixels+45, paint);
			//canvas.drawText(yAxis, locyAxisInPixels-40,canvasHeight/2, paint);
		}
		
		
	}
	
	private int[] toPixel(float pixels, float min, float max, float[] value) {
		
		double[] p = new double[value.length];
		int[] pint = new int[value.length];
		
		for (int i = 0; i < value.length; i++) {
			p[i] = .1*pixels+((value[i]-min)/(max-min))*.8*pixels;
			pint[i] = (int)p[i];
		}
		
		return (pint);
	}
	
	
	/**
	 * "Scale" axes
	 * @param xvalues
	 * @param yvalues
	 */
	private void getAxes(float[] xvalues, float[] yvalues) {
		
		minX = getMin(xvalues);
		maxX = getMax(xvalues);
		
		if( (fixedMinY == 0) && (fixedMaxY == 0) ){
			
			minY = getMin(yvalues);
			maxY = getMax(yvalues);	
			
		}else{
			
			minY = fixedMinY;
			maxY = fixedMaxY;
			
		}
		
		/**/
		
		/*minX = -1f;
		minY = -1f;
		maxX = 11f;
		maxY = 1f;				/**/
		
		locyAxis = 0;
		locxAxis = 0;
		
		/*if (minX>=0)
			locyAxis=minX;
		else if (minX<0 && maxX>=0)
			locyAxis=0;
		else
			locyAxis=maxX;
		
		if (minY>=0)
			locxAxis=minY;
		else if (minY<0 && maxY>=0)
			locxAxis=0;
		else
			locxAxis=maxY;
		/**/
	}
	
	
	
	private int toPixelInt(float pixels, float min, float max, float value) {
		
		double p;
		int pint;
		p = .1 * pixels + ((value-min)/(max-min)) * 0.8 * pixels;
		// TODO: itt megnézni, hogy ezek a konstans szorzók mit csinálnak
		pint = (int)p;
		
		return (pint);
		
	}

	
	/**
	 * @return the maximum value of array
	 */
	private float getMax(float[] array) {
		float largest = array[0];
		for (int i = 0; i < array.length; i++)
			if (array[i] > largest)
				largest = array[i];
		return largest;
	}

	
	/**
	 * @return the minimum value of array
	 */
	private float getMin(float[] array) {
		float smallest = array[0];
		for (int i = 0; i < array.length; i++)
			if (array[i] < smallest)
				smallest = array[i];
		return smallest;
	}

}

