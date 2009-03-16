package edu.virginia.cs.storagedesk.storageserver;


import java.util.Hashtable;

import org.apache.log4j.Logger;

import edu.virginia.cs.storagedesk.common.ISCSI;


/**
 * @author hh4z
 *
 */
public class Parameter {
	
	private static Logger logger = Logger.getLogger(Parameter.class);
	
	public String key;                         // key
	public int  type;                            // type of parameter
	public Hashtable<String, String> validValues = new Hashtable<String, String>();;     // list of valid values
	public String defaultValue;       // default value
	public Hashtable<String, String> valueList = new Hashtable<String, String>();     // value list
	
	String rxOffer;  // outgoing offer
	String txOffer; // incoming offer
	String txAnswer;   // outgoing answer
	String rxAnswer;   // incoming answer
	String negotiated; // negotiated value
	boolean isTxOffer;                         // sent offer 
	boolean isRxOffer;                         // received offer 
	boolean isTxAnswer;                        // sent answer
	boolean isRxAnswer;                        // received answer
	boolean reset;                            // reset value list
	
	public Parameter(int t, String k, String dflt, String valid) {
//		Initilized parameter
		
		type = t;                 // type
		key = k;            // key
		defaultValue = dflt;          // default value
		String[] valids = valid.split(String.valueOf(','));
		for (int i = 0; i < valids.length; i++)
			validValues.put(valids[i],valids[i]);        // list of valid values
		isTxOffer = false;                // sent offer
		isRxOffer = false;                // received offer
		isTxAnswer = false;               // sent answer
		isRxAnswer = false;               // received answer
		reset = false;                   // used to erase value_l on next parse
		
		rxOffer = null;
		txOffer = null;
		txAnswer = null;
		rxAnswer = null;
		
		valueList.put(defaultValue, defaultValue);
		
		// Arg check
		
		switch(type) {
		case ISCSI.PARAM_TYPE_DECLARATIVE:
			break;
		case ISCSI.PARAM_TYPE_DECLARE_MULTI:
			break;
		case ISCSI.PARAM_TYPE_BINARY_OR:
		case ISCSI.PARAM_TYPE_BINARY_AND:
			if ((valid.compareToIgnoreCase("Yes,No") != 0) &&
					(valid.compareToIgnoreCase("No,Yes") != 0) &&
					(valid.compareToIgnoreCase("No") != 0) &&
					(valid.compareToIgnoreCase("Yes") != 0)){
				logger.error("Bad field " + valid + "for ISCSI.PARAM_TYPE_BINARY");
				return;
			}			
			break;
		case ISCSI.PARAM_TYPE_NUMERICAL:
			break;
		case ISCSI.PARAM_TYPE_NUMERICAL_Z:
			break;
		case ISCSI.PARAM_TYPE_LIST:
			break;
		default:
			logger.error("unknown parameter type " + type);
			return;
		}
		logger.debug("Parameter key " + key + ": " + 
					"value " + validValues + ", " +
					"default " + defaultValue + ", " + 
					"current " + valueList.get(0));
				
	}
}
