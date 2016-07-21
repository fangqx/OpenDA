/* MOD_V1.0
 * Copyright (c) 2013 OpenDA Association
 * All rights reserved.
 *
 * This file is part of OpenDA.
 *
 * OpenDA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * OpenDA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with OpenDA.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openda.model_openfoam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.joda.time.DateTime;
import org.openda.exchange.DoubleExchangeItem;
import org.openda.exchange.timeseries.TimeUtils;
import org.openda.interfaces.IDataObject;
import org.openda.interfaces.IExchangeItem;
import org.openda.interfaces.IPrevExchangeItem;
import org.openda.utils.IMyObservable;
import org.openda.utils.IMyObserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
//import java.math.BigDecimal;
import java.util.*;


/**
 *
 * Read an OpenFOAM dictionary file that contains lines of the format:
 *
 * keyword value;\\#exchangeItemID
 *
 * and
 *
 * keyword (value1 value2 ...);\\#exchangeItemID
 *
 * For each value found in a line indicated by \\#exchangeItemID a DoubleExchangeItem is created.
 * All other lines are kept as-is.
 *
 * A referenceDate (ISO 8601 format) can be specified as a second argument. When a referenceDate is specified,
 * the value for exchangeItemId's "oda:startTime" and "oda:endTime" is converted from seconds to modified Julian days.
 *
 *  @author Werner Kramer (VORtech)
 */

@SuppressWarnings("unused")
public class DictionaryNoisyDataObject implements IDataObject{


    private class ParameterObserver implements IMyObserver {
        public void update(IMyObservable object, Object arg){
            DoubleExchangeItem itemChanged = (DoubleExchangeItem) object;
            String id = itemChanged.getId();
            IExchangeItem noiseItem = DictionaryNoisyDataObject.this.getDataObjectExchangeItem(id + ".noise");
            noiseItem.setValues(0.0);
            logger.debug("Resetting noise after exchange item  '" + id + "' is changed.");
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DictionaryNoisyDataObject.class);
    private String fileName = null;

	private static final String keyWordPrefix =";//#";
	private static final String multiplexId ="@";
    private static final String noiseId =".noise";
    private static final String noiseLineKeyword ="//#oda:generatedNoise";
    private static final double SECONDS_TO_DAYS = 1.0 / 24.0 / 60.0 / 60.0;
    private static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";
    private static final List<String> timeExchangeItemIds =  Arrays.asList("oda:startTime","oda:endTime");

    private HashMap<String,IExchangeItem> items = new LinkedHashMap<>();
    private HashMap<String,Double> parameterValues = new LinkedHashMap<>();


//    private HashMap<String,IExchangeItem> noise = new LinkedHashMap<>();

    private HashMap<String,Integer> multiplexColumn = new LinkedHashMap<>();
    private ArrayList<String> fileContent = new ArrayList<>();

    private double referenceMjd = 0.0;
	private boolean convertTime = false;

    private ParameterObserver observer = new ParameterObserver();



    //private String arrayBrackets = "\\(\\)";
	//private String arrayDelimiter = " ";


    /**
     * Reads OpenFoam results generated by the sample utility.
     *
     * @param workingDir the working directory.
     * @param arguments list of other arguments:
     * <ol>
     * <li>The name of the file containing the data
     *      for this IoObject (relative to the working directory).</li>
     * <li>Optional, a referenceDate in ISO 8601 notatation, e.g
     *      for this IoObject (relative to the working directory).</li>
     *
     * </ol>
     */
	public void initialize(File workingDir, String[] arguments) {

		if ( arguments.length == 0 ) {
			throw new RuntimeException("No arguments are given when initializing.");
		} else if (arguments.length == 2) {
			Date date = new DateTime( arguments[1] ).toDate();
			this.referenceMjd = TimeUtils.date2Mjd(date);
			this.convertTime = true;
		}
		this.fileName = arguments[0];

		logger.info("Filename = " + this.fileName);
		File inputFile;
		// check file
		try{
			inputFile = new File(workingDir,fileName);
			if(!inputFile.isFile()){
				throw new IOException("Can not find file " +  inputFile);
			}
			this.fileName = inputFile.getCanonicalPath();
		}catch (Exception e) {
			throw new RuntimeException("Trouble opening file " + this.fileName);
		}
		//read file and parse to hash
		try {
			Scanner scanner = new Scanner(inputFile);
			scanner.useLocale(Locale.US);
			String line;
			while (scanner.hasNext()) {

				line = scanner.nextLine();
				fileContent.add(line);
				int locationIndex = line.indexOf(keyWordPrefix);
				//Scanner lineScanner = new Scanner(line);
				if (locationIndex > 0) {
					logger.debug("Line: "+ line);
					String valueString="";
                    if ( scanner.hasNext("//#oda:generatedNoise") ) {
                        String lineWithNoise = scanner.nextLine();
                        //fileContent.add(lineWithNoise);
                        logger.debug("Next line contains noise item: " + lineWithNoise);
                    }
					String key = line.substring(locationIndex + keyWordPrefix.length() );
                    logger.debug("Key " + key);
					line = line.substring(0,locationIndex);

					String[] parts = line.split(String.format(WITH_DELIMITER, "\\(|\\)|\\s"));
					Vector<Double> values = new Vector<>();
					Vector<Integer> column = new Vector<>();
					for ( int index=0; index < parts.length ;index++) {
					//for ( String part : parts) {
						if (!parts[index].isEmpty()) {
							try {
								Double value = Double.parseDouble(parts[index]);
								values.add(value);
								column.add(index);
								logger.debug("Found part " + parts[index]);
							} catch (NumberFormatException e) {
                                logger.trace("Skipping " + parts[index]);
							}
						}
					}
					if ( values.size() == 1 ) {
						double value =  values.firstElement();
						if (timeExchangeItemIds.contains(key) && convertTime) {
							logger.debug("Converting to MJD: " + key);
							value = value * SECONDS_TO_DAYS + referenceMjd;
						}
                        this.parameterValues.put(key,value);
                        DoubleExchangeItem exchangeItem = new DoubleExchangeItem(key,value);
                        exchangeItem.addObserver(observer);
                        items.put(key, exchangeItem);
						multiplexColumn.put(key,column.firstElement());
                        DoubleExchangeItem noiseExchangeItem = new DoubleExchangeItem(key + noiseId,0.0);
                        items.put(key + noiseId, noiseExchangeItem);
					} else {
						if (timeExchangeItemIds.contains(key)) {
							throw new RuntimeException("A line designated by keyword '" + key + "' cannot contain multiple values: " + line);
						}
						for (int index=0 ; index < values.size() ; index++  ) {
							String id = key + multiplexId + (index+1);
                            this.parameterValues.put(id,values.elementAt(index));
                            DoubleExchangeItem exchangeItem = new DoubleExchangeItem(id, values.elementAt(index));
                            exchangeItem.addObserver(observer);
                            items.put(id, exchangeItem);
							multiplexColumn.put(id,column.elementAt(index));
                            DoubleExchangeItem noiseExchangeItem = new DoubleExchangeItem(id + noiseId,0.0);
                            items.put(id + noiseId, noiseExchangeItem);
						}
					}
                } else {

                }
			}
			scanner.close();
		} catch (Exception e) {
			throw new RuntimeException("Problem reading from file " + fileName+" : "+e.getClass());
		}
    }

	/** {@inheritDoc}
	 */
	public IPrevExchangeItem[] getExchangeItems() {

		int n = this.items.size();
        IPrevExchangeItem[] result=new IPrevExchangeItem[n];

		int i=0;
		for(String key : this.items.keySet()){
			result[i]=this.items.get(key);
			i++;
		}

		return result;
	}

	/** {@inheritDoc}
	 */
	public IExchangeItem getDataObjectExchangeItem(String exchangeItemID) {
        return items.get(exchangeItemID);
	}

	/** {@inheritDoc}
	 */
	public String[] getExchangeItemIDs() {
        return this.items.keySet().toArray(new String[this.items.size()]);
    }

	/** {@inheritDoc}
	 */
	public String[] getExchangeItemIDs(IPrevExchangeItem.Role role) {
		//TODO: select on role
		return this.getExchangeItemIDs();
	}

	/** {@inheritDoc}
	 */
	public void finish() {
        	//write to file

        logger.debug("finish");
        File outputFile = new File(fileName);
		try{
			if(outputFile.isFile()){
				if ( ! outputFile.delete() ) throw new RuntimeException("Cannot delete " + outputFile);
			}
		}catch (Exception e) {
			logger.error("DictionaryDataObject: trouble removing file " + this.fileName +" :\n" + e.getMessage());
		}
		try {
			FileWriter writer = new FileWriter(outputFile);
			BufferedWriter out = new BufferedWriter(writer);
            for (String line: fileContent){
				int locationIndex = line.indexOf(keyWordPrefix);
				//Scanner lineScanner = new Scanner(line);
				if (locationIndex > 0) {
					logger.debug("Line: " + line);
					//String valueString = "";
                    String key = line.substring(locationIndex + keyWordPrefix.length());
					logger.debug("Key " + key);
					line = line.substring(0, locationIndex);
					String[] parts = line.split(String.format(WITH_DELIMITER, "\\(|\\)|\\s"));
                    String[] noiseParts = parts.clone();
					if (multiplexColumn.containsKey(key)) {
						int index = multiplexColumn.get(key);
                        Double noise = (Double) items.get(key + noiseId).getValues();
                        Double paramValue = (Double) items.get(key).getValues();
                        if (timeExchangeItemIds.contains(key) && convertTime) {
                            logger.debug("Converting to MJD: " + key);
                            paramValue = (paramValue - referenceMjd ) / SECONDS_TO_DAYS;
                        }
//                        BigDecimal parameter = new BigDecimal(paramValue + noise);
//                        parts[index] = parameter.setScale(3, BigDecimal.ROUND_HALF_EVEN).toString();
                        parts[index] = Double.toString(paramValue + noise);
                        noiseParts[index] = Double.toString(noise);
					}
					int nr = 1;
					while (multiplexColumn.containsKey(key + multiplexId + nr)) {
						String id = key + multiplexId + nr;
						int index = multiplexColumn.get(id);
                        Double noise = (Double) items.get(id + noiseId).getValues();
                        Double paramValue = (Double) items.get(id).getValues();
//                        BigDecimal parameter = new BigDecimal(paramValue + noise);
//                        parts[index] = parameter.setScale(3, BigDecimal.ROUND_HALF_EVEN).toString();
                        parts[index] = Double.toString(paramValue + noise);
                        noiseParts[index] = Double.toString(noise);
                        nr++;
					}


                    // write value +
                    StringBuilder builder = new StringBuilder();
					for(String part : parts) {
						builder.append(part);
					}
					String outputLine = builder.toString() + keyWordPrefix + key + "\n";
					logger.debug("Write line: " + outputLine);
					out.write(outputLine);

                    // write noise component as comment
//                    StringBuilder builder2 = new StringBuilder();
//                    for(String part : noiseParts) {
//                        builder2.append(part);
//                    }
//                    String noiseString = noiseLineKeyword + builder2.toString() + "\n";
//                    logger.debug("Write noise line: " + noiseString);
//                    out.write(noiseString);
				}
                else {
                    //Write Line
                    out.write(line + "\n");
                }
            }
			out.close();
		} catch (Exception e) {
			throw new RuntimeException("Problem writing to file " + this.fileName+" :\n" + e.getMessage());
		}
    }
}
