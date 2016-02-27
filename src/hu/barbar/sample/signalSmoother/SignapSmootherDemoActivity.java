package hu.barbar.sample.signalSmoother;

import hu.barbar.commonObjects.SignalSmoother;
import hu.barbar.commonObjects.graphView.GraphView;

import java.text.NumberFormat;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SignapSmootherDemoActivity extends Activity { 
    
	private static final double ZOOM_SCALE = 50;
	
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private SensorEventListener mAcceleroListener;
	
	public float acceleroX;
	public float acceleroY;
	public float acceleroZ;
	
	private Spinner prioritySpinner, prioritySpinner2;
	private TextView usedPriorityArrayTV, usedPriorityArrayTV2;
	private int[][] priorityArrays = {
						{1, 1, 1, 1, 1, 2, 2, 2}, 
						{1, 1, 2, 2, 2, 4, 4, 4},
						{1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4},
						SignalSmoother.PRIORITY_EXPONENTIAL(2, 5),
						SignalSmoother.PRIORITY_BELL_CURVE_1,
						SignalSmoother.PRIORITY_BELL_CURVE_2,
						SignalSmoother.PRIORITY_BELL_CURVE_3
					};
	

	private SignalSmoother[] mySignalSmoothers;
	private int[] priorityArray, priorityArray2;
	private double[] thresholds;
	
	
	private TextView[] data_org;
	private TextView[] data_smd;
	private TextView[] data_dsmd;
	private TextView[] others;
	
	private MyView myView1, myView2, myView3;
	
	private NumberFormat nf, nf2;
	
	// Graph views..
	
	private LinearLayout graphLayout1, graphLayout2;
	private GraphView graphView1, graphView2;
	float[] xValues, yValues;
	private int xv = 1000;
	
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initValueArrays();
        
        initUI();
        
        priorityArray = priorityArrays[0];
        priorityArray2 = priorityArrays[0];
        
        initSignalSmoothers();
        
        initAccelerometerSensor();
        
    }
    
    
    private void initValueArrays(){
    	
    	int aLength = 10;
    	
    	xValues = new float[aLength];
    	yValues = new float[aLength];
    	
    	for(int i=0; i<aLength; i++){
    		xValues[i] = i;
    		yValues[i] = 0;
    	}
    	
    	
    	
    	
    	
    }
    
    
    private void displayValues(){
    	
    	
    	
    	data_org[0].setText(nf.format(acceleroX));
    	data_org[1].setText(nf.format(acceleroY));
    	data_org[2].setText(nf.format(acceleroZ));
    	
    	
    	data_smd[X].setText(nf.format(
								mySignalSmoothers[X].refreshData(acceleroX)
							));
    	data_smd[Y].setText(nf.format(
								mySignalSmoothers[Y].refreshData(acceleroY)
							));
    	data_smd[Z].setText(nf.format(
								mySignalSmoothers[Z].refreshData(acceleroZ)
							));
    	
    	
    	others[X].setText(nf2.format(
							mySignalSmoothers[X].getRecommendedThreshold(1)
						));
    	others[Y].setText(nf2.format(
							mySignalSmoothers[Y].getRecommendedThreshold(1)
						));
    	others[Z].setText(nf2.format(
							mySignalSmoothers[Z].getRecommendedThreshold(1)
						));
    	
    	
    	
    	data_dsmd[X].setText("X: " + nf.format(
    						mySignalSmoothers[X+3].refreshData(mySignalSmoothers[X].getData()))
    						+ " \trTrsh: " 
    						+ nf2.format(
    								mySignalSmoothers[X+3].getRecommendedThreshold(1)
    							)
    					);
    	
    	data_dsmd[Y].setText("Y: " + nf.format(
							mySignalSmoothers[Y+3].refreshData(mySignalSmoothers[Y].getData()))
							+ " \trTrsh: "
							+ nf2.format(
    								mySignalSmoothers[Y+3].getRecommendedThreshold(1)
    							)
						);
    	
    	data_dsmd[Z].setText("Z: " + nf.format(
							mySignalSmoothers[Z+3].refreshData(mySignalSmoothers[Z].getData()))
							+ " \trTrsh: "
							+ nf2.format(
    								mySignalSmoothers[Z+3].getRecommendedThreshold(1)
    							)
						);
	    	
    	
    	// show values in myViews
    	
    	myView1.moveFromCenter(
    				(int)(-1 * acceleroX  * ZOOM_SCALE), 
    				(int)( 1 * acceleroY  * ZOOM_SCALE)
				);
    	
    	myView2.moveFromCenter(
    				(int)(-1 * mySignalSmoothers[X].getData() * ZOOM_SCALE), 
    				(int)( 1 * mySignalSmoothers[Y].getData() * ZOOM_SCALE)
    			);
    	
    	myView3.moveFromCenter(
    				(int)(-1 * mySignalSmoothers[X+3].getData() * ZOOM_SCALE), 
    				(int)( 1 * mySignalSmoothers[Y+3].getData() * ZOOM_SCALE)
    			);
    	
    	
    	graphView1.refreshYValue(acceleroY);
    	graphView2.refreshYValue((float) mySignalSmoothers[Y+3].getData());
    	
    	
    }
    

	private void initSignalSmoothers() {
		
		mySignalSmoothers = new SignalSmoother[6];
        
        thresholds = new double[6];
        for(int i=0; i<6; i++){
        	thresholds[i] = 0.6;
        }
        
        
        for(int i=0; i<3; i++){
	        mySignalSmoothers[i] = new SignalSmoother(
	        								priorityArray, 
	        								thresholds[i], 
	        								true
	    								);
        }
        
        for(int i=3; i<6; i++){
	        mySignalSmoothers[i] = new SignalSmoother(
	        								priorityArray2, 
	        								thresholds[i], 
	        								true
	    								);
        }
        
	}

	
	private void initUI() {
		
		nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(5);
        nf.setMinimumFractionDigits(5);
        
        nf2 = NumberFormat.getInstance();
        nf2.setMaximumFractionDigits(3);
        nf2.setMinimumFractionDigits(3);
		
		usedPriorityArrayTV = (TextView) findViewById(R.main.used_pa);
		usedPriorityArrayTV2 = (TextView) findViewById(R.main.used_pa2);
		
		
		
		prioritySpinner  = (Spinner) findViewById(R.main.spinner);
		prioritySpinner2 = (Spinner) findViewById(R.main.spinner2);
		
		OnItemSelectedListener myOISL = new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				
				// stop accelerometer while SignapSmoothers doesn't works.
				mSensorManager.unregisterListener(mAcceleroListener);
				
				if(parent == prioritySpinner){
					//Toast.makeText(getApplicationContext(), "Array 1: " + Integer.toString(pos), 0).show();
					
					priorityArray = priorityArrays[pos];
					
					usedPriorityArrayTV.setText(showArray("Used priority array:", priorityArray));
					
				}else{
					//Toast.makeText(getApplicationContext(), "Array 2: " + Integer.toString(pos), 0).show();
					
					priorityArray2 = priorityArrays[pos];
					
					usedPriorityArrayTV2.setText(showArray("Used priority array:", priorityArray2));
					
				}
				
				
				
				initSignalSmoothers();
				
				// Start accelerometer again
				mSensorManager.registerListener(mAcceleroListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			}

			public void onNothingSelected(AdapterView<?> parent) {}
		};
		
		prioritySpinner.setOnItemSelectedListener(myOISL);
		prioritySpinner2.setOnItemSelectedListener(myOISL);
		
		
		
		data_org  = new TextView[4];
		data_smd  = new TextView[4];
		data_dsmd = new TextView[3];
		others    = new TextView[4];
		
		data_org[0] = (TextView) findViewById(R.main.data1_org);
		data_org[1] = (TextView) findViewById(R.main.data2_org);
		data_org[2] = (TextView) findViewById(R.main.data3_org);
		
		data_smd[0] = (TextView) findViewById(R.main.data1_smd);
		data_smd[1] = (TextView) findViewById(R.main.data2_smd);
		data_smd[2] = (TextView) findViewById(R.main.data3_smd);
		
		data_dsmd[0] = (TextView) findViewById(R.main.dsmd0);
		data_dsmd[1] = (TextView) findViewById(R.main.dsmd1);
		data_dsmd[2] = (TextView) findViewById(R.main.dsmd2);
		
		others[0] = (TextView) findViewById(R.main.other1);
		others[1] = (TextView) findViewById(R.main.other2);
		others[2] = (TextView) findViewById(R.main.other3);
		
		
		myView1 = (MyView) findViewById(R.main.myView1);
		myView2 = (MyView) findViewById(R.main.myView2);
		myView3 = (MyView) findViewById(R.main.myView3);
		
		
		
		// Set width of this views equals to 1/2 of screen width
		
		int halfOfScreenWidth = (int)(getScreenSize().x / 2); 
		
		LinearLayout myViewFrame1 = (LinearLayout) findViewById(R.main.layout_myview1_frame);
		LinearLayout myViewFrame2 = (LinearLayout) findViewById(R.main.layout_myview2_frame);
		LinearLayout myViewFrame3 = (LinearLayout) findViewById(R.main.layout_myview3_frame);
		
		LayoutParams params;
		params = (LayoutParams) myViewFrame1.getLayoutParams();
		params.width = halfOfScreenWidth;
		myViewFrame1.setLayoutParams(params);
		
		params = (LayoutParams) myViewFrame2.getLayoutParams();
		params.width = halfOfScreenWidth;
		myViewFrame2.setLayoutParams(params);
		
		params = (LayoutParams) myViewFrame3.getLayoutParams();
		params.width = halfOfScreenWidth;
		myViewFrame3.setLayoutParams(params);
		
		LinearLayout doubleSmoothedValuesLayout = (LinearLayout)findViewById(R.main.doubleSmoothedValues);
		params = (LayoutParams) doubleSmoothedValuesLayout.getLayoutParams();
		params.width = halfOfScreenWidth;
		doubleSmoothedValuesLayout.setLayoutParams(params);
		
		
		
		params = (LayoutParams) prioritySpinner.getLayoutParams();
		params.width = halfOfScreenWidth;
		prioritySpinner.setLayoutParams(params);
		
		params = (LayoutParams) prioritySpinner2.getLayoutParams();
		params.width = halfOfScreenWidth;
		prioritySpinner2.setLayoutParams(params);
		
		params = (LayoutParams) usedPriorityArrayTV.getLayoutParams();
		params.width = halfOfScreenWidth;
		usedPriorityArrayTV.setLayoutParams(params);
		
		params = (LayoutParams) usedPriorityArrayTV2.getLayoutParams();
		params.width = halfOfScreenWidth;
		usedPriorityArrayTV2.setLayoutParams(params);
		
		
		// Set size of circle matching screen size
		
		myView1.setCircleSize((int)getScreenSize().x / 16);
		myView2.setCircleSize((int)getScreenSize().x / 16);
		myView3.setCircleSize((int)getScreenSize().x / 16);
		
		// Init graphs
		
		graphLayout1 = (LinearLayout) findViewById(R.main.layout_graphs1left_frame);
		graphLayout2 = (LinearLayout) findViewById(R.main.layout_graphs1right_frame);
		
		graphView1 = new GraphView(getApplicationContext(), 100);
		graphLayout1.addView(graphView1);
		graphView1.setScaleY(-2f, 2f);
		
		graphView2 = new GraphView(getApplicationContext(), 100);
		graphLayout2.addView(graphView2);
		graphView2.setScaleY(-2f, 2f);
	}
	
	
	private Point getScreenSize(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int weight = displaymetrics.widthPixels;
        
        return new Point(weight, height);
	}
	
	
	private String showArray(String title, int[] array){
		String toReturn = title + " ";
		
		for(int i=0; i<array.length; i++){
			toReturn += Integer.toString(array[i]) + ", ";
		}
		
		return toReturn;
	}
	
	
	private void initAccelerometerSensor(){
	
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
		mAcceleroListener = new SensorEventListener() {
			
			public void onSensorChanged(SensorEvent event) {

				acceleroX = event.values[0];
				acceleroY = event.values[1];
				acceleroZ = event.values[2];
				
				displayValues();
				
			}
			
			public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		};
		
	}
	
	
	@Override
    protected void onResume(){
		super.onResume();

		// Start accelerometer
		mSensorManager.registerListener(mAcceleroListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		
	}

	@Override
    protected void onPause() {
		super.onPause();
		
		// Stop accelerometer
		mSensorManager.unregisterListener(mAcceleroListener);
	}
	
	
}
