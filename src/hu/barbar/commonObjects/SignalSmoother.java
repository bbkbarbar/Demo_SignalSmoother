package hu.barbar.commonObjects;

import java.io.Serializable;

/**
 * It dynamically helps to process to the "shaking" input values (for example accelerometer's data or orientation sensor's data, etc.)
	and give an "priority based" average value, if necessary with threshold using to avoid in the case of sudden changes the slow response. 
 * 
 * @author Barbár
 */

@SuppressWarnings("serial")
public class SignalSmoother implements Serializable{
	
	// Public default values
	
	public static final boolean THRESHOLD_STATE_ENABLED = true;
	public static final boolean THRESHOLD_STATE_DISABLED = false;
	
	/**
	 * The default safety factor for getRecommendedThreshold method
	 */
	public static final double DEFAULT_SAFETY_FACTOR = 2;
	
	/**
	 * getRecommendedThreshold method returns with this if it can not calculate the recommended threshold (it able to calculate only if have minimum 2 previous input data).
	 */
	public static final double NO_RECOMMENDED_THRESHOLD = (-1);
	
	/**
	 * Default #1 "bell curve" priority array
	 *  {1, 2, 4, 6, 8, 10} 
	 */
	public static final int[] PRIORITY_BELL_CURVE_1 = {1, 2, 4, 6, 8, 10};
	
	/**
	 * Default #2 "bell curve" priority array
	 *  {1, 2, 4, 6, 6, 8, 8, 10}
	 */
	public static final int[] PRIORITY_BELL_CURVE_2 = {1, 2, 4, 6, 6, 8, 8, 10};
	
	/** 
	 * Default #3 "bell curve" priority array
	 * {1, 2, 4, 6, 8, 10, 10, 10}
	 */
	public static final int[] PRIORITY_BELL_CURVE_3 = {1, 2, 4, 6, 8, 10, 10, 10};
	
	
	
	// Internal variables
	 
	private int maxValueCount;					// The count of values what used in calaulating average
	private int[] priorities;					// containts the priorities of gift values
	private int valueCount;						// the count of values even since the last discarding of previous values
	private double[] values;					// contains the last [valueCount] gift value
	private double resultValue;					// represents the priority based average
	private double lastResultValue;				// contains the previous resultValue so that we shoudn't have to call "onDataChanged()" method if the new data equals the last 
	private boolean isThresholdUsingEnabled;	// contains the threshold function current using state
	private double threshold;					// the threshold what we use (if enabled)
	
	
	
	/**
	 * Constructor
	 * @param priorityArray this contains the priorities of gift values (data). The first value means the priority of the N. value, and the N. value means the priority of the newest data.
	 * @param Note: DataProcessor use only the last N data, N equals the length of priorityArray.  
	 * @param Note: If you use this constructor threshold is disabled by default (You can change it later).
	 */
	public SignalSmoother(int[] priorityArray){
		init(priorityArray, false, 0);
	}
	
	
	/**
	 * Constructor
	 * @param priorityArray this contains the priorities of gift values (data). The first value means the priority of the N. value, and the N. value means the priority of the newest data.
	 * @param Note: DataProcessor use only the last N data, N equals the length of priorityArray.  
	 * @param threshold if the distance between the newest data and the current average higher then threshold, then the previous average data is discarded
	 * @param Note: If you use this constructor threshold is enabled by default (You can change it later). 
	 */
	public SignalSmoother(int[] priorityArray, double threshold){
		init(priorityArray, true, threshold);
	}
	
	
	/**
	 * Constructor
	 * @param priorityArray this contains the priorities of gift values (data). The first value means the priority of the N. value, and the N. value means the priority of the newest data.
	 * @param Note: DataProcessor use only the last N data, N equals the length of priorityArray.  
	 @param threshold if the distance between the newest data and the current average higher then threshold, then the previous average data is discarded
	 * @param enableThreshold we can enable or disable using threshold function
	 */
	public SignalSmoother(int[] priorityArray, double threshold, boolean enableThreshold){
		init(priorityArray, enableThreshold, threshold);
	}
	
	
	/**
	 *  Construct copy of DataProcessor with given parameters
	 */
	private final void init(int[] priorityArray, boolean useThreshold, double giftThreshold){
			
		this.maxValueCount = priorityArray.length;
		this.priorities = new int[maxValueCount];
		
		priorities = priorityArray;
		
		this.valueCount = 0;
		this.values = new double[this.maxValueCount];
		this.resultValue = 0;
		this.lastResultValue = 0;
		isThresholdUsingEnabled = useThreshold;
		this.threshold = giftThreshold;
				
	}
	
	
	
	/**
	 * Parameterizable static input array for constructor
	 * @return static priority array with given length (this means the DataProcessor going to calculate the average from last N value (N = length).
	 */
	public static final int[] PRIORITY_CONSTANT(int length){
		
		int[] toReturn = new int[length];
		
		for(int i=0; i<length; i++)
			toReturn[i] = 1;
		
		return toReturn;
	}
	
	
	/**
	 * Parameterizable static input array for constructor
	 * @param exponentialBase the base of exponential increasing
	 * @param length the length of array
	 * @return an priority array with given length, what is exponential increasing. Note: the first array element is always equal to 1 (for example {1, 2, 4,...}
	 */
	public static final int[] PRIORITY_EXPONENTIAL(int exponentialBase, int length){
		
		int[] toReturn = new int[length];
		
		for(int i=0; i<length; i++){
		
			if(i==0)
				toReturn[i] = 1;
			else{
				
				toReturn[i] = exponentialBase;
				for(int j = 0; j<(i-1); j++){
					toReturn[i] *= exponentialBase;
				}
			}
			
		}
		
		return toReturn;
		
	}
	
	
	/**
	 * Calculates a recommended threshold from the overshoot of previous data.
	 * @param safetyFactor determinate the "safety" value, and the calculated threshold will be multiplied with this
	 * @return a value that can be used to threshold the following input values (This recommended !!! ONLY !!! if the next input values expected will be similar to the previous data). It able to calculate only if have minimum 2 previous data.
	 * If the number of previous data, less than two than this returns NO_RECOMMENDED_THRESHOLD constant.
	 */
	public final double getRecommendedThreshold(double safetyFactor){
		
		double toReturn = 0;
		
		// if there is no value, then the recommended threshold calculation is meaningless
		if(values.length < 2)
			return NO_RECOMMENDED_THRESHOLD;

		
		
		// get the minimum and the maximum value from last [priorityArray.length] data
		double min = values[0], max = values[0];
		
		for(int i = 0; i < values.length; i++){
			if(values[i] < min)
				min = values[i];
			if(values[i] > max)
				max = values[i];
		}
		
		if( (Math.abs(lastResultValue - max)) > (Math.abs(lastResultValue - min)) )
			toReturn = safetyFactor * (Math.abs((lastResultValue - max)));
		else
			toReturn = safetyFactor * (Math.abs((lastResultValue - min)));
		
		
		return toReturn;
	}
	
	
	/**
	 * Set new threshold for data processor. 
	 * Note: this change is the next new value will be affected first.
	 * @param newThreshold the new value. If it equals 0 then it disable using of threshold (in this case the new threshold (0) will NOT stored). 
	 */
	public final void updateThreshold(double newThreshold){
		
		if(newThreshold != SignalSmoother.NO_RECOMMENDED_THRESHOLD){
			
			if(newThreshold == Double.valueOf(0)){
				this.isThresholdUsingEnabled = THRESHOLD_STATE_DISABLED;
			}else
				this.threshold = Math.abs(newThreshold);
			
		}
		
	}
	
	
	
	/**
	 * Set new threshold for data processor
	 * 	and set the threshold using state (enabled or disabled)
	 * 	Note: this change is the next new value will be affected first.
	 * @param newThreshold the new value
	 * @param thresholdState can be DataProcessor.THRESHOLD_STATE_ENABLED (true) or DataProcessor.THRESHOLD_STATE_DISABLED (false) using threshold; 
	 */
	public final void updateThreshold(double newThreshold, boolean thresholdState){
		this.updateThreshold(newThreshold);
		this.isThresholdUsingEnabled = thresholdState;
	}
	
	
	
	/**
	 * @return value of current threshold
	 */
	public final double getThreshold(){
		return this.threshold;
	}
	
	
	/**
	 *  Able to subsequently enable or disable using of threshold
	 *  Note: this change is the next new value will be affected first.
	 *  @param thresholdState can be DataProcessor.THRESHOLD_STATE_ENABLED or  DataProcessor.THRESHOLD_STATE_DISABLED.
	 */
	public final void setThresholdEnable(boolean thresholdState){
		this.isThresholdUsingEnabled = thresholdState;
	}
	
	
	/**
	 * @return the current state of using threshold
	 */
	public final boolean getThresholdEnabled(){
		return this.isThresholdUsingEnabled;
	}
	
	
	
	/**
	 * @return some information of the current state of copy 
	 * 	(Priorities, threshold using state (enabled or disabled), threshold, and the current average value)
	 */
	public String toString(){
		String toReturn = "";
		
		toReturn += "Priorities: ";
		for(int i=0; i<priorities.length; i++){
			toReturn += Integer.toString(priorities[i]);
			if( i < (priorities.length-1) )
				toReturn += ", ";
			else
				toReturn += "\n";
		}
		if(this.isThresholdUsingEnabled)
			toReturn += "Threshold using ENABLED\n";
		else
			toReturn += "Threshold using DISABLED\n";
		toReturn += "Threshold: " + Double.toString(this.threshold) + "\n";
		toReturn += "Current average value: " + Double.toString(resultValue);
		
		return toReturn;
	}
	
	
	
	/**
	 * Adding new data, calculate the new average value, and (if the new average not equals the last average) call onDataChanged() method. 
	 * @param newValue contains the data what you want to add 
	 * @return the new result value after new data processed (priority based average).
	 */
	public double refreshData(double newValue){
		
		lastResultValue = resultValue;
		
		
		
		/*
		 *	Adding new value 
		 */
		if( (isThresholdUsingEnabled) && ( Math.abs((resultValue - newValue)) > threshold) ){	// If the newValue is much different then previous resultValues then we discard the previous values..
			this.valueCount = 0;
			this.values = new double[this.maxValueCount];
		}
		
		if(valueCount == maxValueCount){
			
			for(int i=1; i<maxValueCount; i++)
				values[i-1] = values[i]; 
			
			values[maxValueCount-1] = newValue;
			
		}else{
			
			values[valueCount] = newValue;
			
			valueCount++;
		}
			
		
		
		
		/*
		 * 	Calculating result value
		 */
		
		// Not need calculating if the new value equals the last result...
		if(resultValue == newValue)	
			return resultValue;
		
		// If the last result is not equal to the value of the new input value...
		double sum = 0;
		int div = 0;
		for (int i = 0; i < valueCount; i++){
			
			sum += (values[i] * priorities[ ( (maxValueCount-valueCount ) + i) ]);
			div += priorities[ ( (maxValueCount-valueCount ) + i) ];
			
		}
		
		this.resultValue = sum / div;
		
		
		
		
		/*
		 *  If we have new result (not equals the last) than we call onDataChanged method what can be implemented on parent
		 */
		if(resultValue != lastResultValue)
			this.onDataChanged();
		
		
		
		// Returns the new result
		return this.resultValue;
	}
	
	
	
	/**
	 * 	@return the current processed (priority based average) value.
	 */
	public final double getData(){
		return this.resultValue;
	}
	
	
	
	/**
	 *	Called every time when the result value has changed (by entering new values).
	 * Note: this can be overridden, where construct instance
	 */
	public void onDataChanged(){};
	
	
}


