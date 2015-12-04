/* MOD_V2.0
 * Copyright (c) 2012 OpenDA Association
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

package org.openda.model_hspf;

import org.openda.exchange.timeseries.TimeUtils;
import org.openda.interfaces.IDataObject;
import org.openda.interfaces.IExchangeItem;
import org.openda.interfaces.IPrevExchangeItem;
import org.openda.utils.Results;
import org.openda.utils.io.AsciiFileUtils;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * DataObject to read/write state variables from/to a UCI state file of a HSPF model.
 *
 * The HSPF model can be installed as part of the BASINS package, which is available from:
 * http://water.epa.gov/scitech/datait/models/basins/index.cfm
 *
 * @author Arno Kockx
 */
public class UciStateDataObject implements IDataObject {

	private File uciFile = null;
	private Map<String, IExchangeItem> exchangeItems = new HashMap<>();
	private Map<String, ReachesInitTable> reachesInitTables = new HashMap<>();
	private Map<String, PerviousLandSegmentsInitTable> perviousLandSegmentsInitTables = new HashMap<>();
	private Map<String, ImperviousLandSegmentsInitTable> imperviousLandSegmentsInitTables = new HashMap<>();

	/**
	 * @param workingDir the working directory.
	 * @param arguments File pathname:
	 *                  The pathname of the .uci state file containing the data for this DataObject (relative to the working directory).
	 *                  Other arguments:
	 *                  The first argument should be the timestamp of the state in the .uci file.
	 */
	public void initialize(File workingDir, String[] arguments) {
		if (arguments == null || arguments.length < 1) {
			throw new IllegalArgumentException("No fileName argument specified for " + getClass().getSimpleName()
					+ ". The first argument should be the name of the .uci file containing the data for this DataObject (relative to the working directory).");
		}
		uciFile = new File(workingDir, arguments[0]);

		//get state time.
		if (arguments.length < 2) {
			throw new IllegalArgumentException("No stateTime argument specified for " + getClass().getSimpleName() + ". The second argument should be the timestamp of the state in the .uci file.");
		}
		double stateTime;
		try {
			stateTime = TimeUtils.date2Mjd(arguments[1]);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid stateTime argument specified for " + getClass().getSimpleName() + ". Cannot parse second argument '" + arguments[1]
					+ "'. The second argument should be the timestamp of the state in the .uci file.", e);
		}

		readUciFile(stateTime);
	}

	public String[] getExchangeItemIDs() {
		Set<String> ids = exchangeItems.keySet();
		return ids.toArray(new String[ids.size()]);
	}

	public String[] getExchangeItemIDs(IPrevExchangeItem.Role role) {
		//all exchange items have role InOut, so always return all items.
		return getExchangeItemIDs();
	}

	public IExchangeItem getDataObjectExchangeItem(String exchangeItemID) {
		return exchangeItems.get(exchangeItemID);
	}

	public void finish() {
		writeUciFile();
	}

	/**
	 * Read uci state file and create state exchangeItems for all state variables.
	 * Also read and store the values of all state variables into memory.
	 */
	private void readUciFile(double stateTime) {
		if (!uciFile.exists()) throw new RuntimeException(getClass().getSimpleName() + ": Cannot find uci state file " + uciFile.getAbsolutePath());

		//create exchangeItems.
		exchangeItems.clear();
		reachesInitTables.clear();
		perviousLandSegmentsInitTables.clear();
		imperviousLandSegmentsInitTables.clear();

		//read file.
		Results.putMessage(getClass().getSimpleName() + ": reading uci state file " + uciFile.getAbsolutePath());
		List<String> inputLines = AsciiFileUtils.readLines(uciFile);
		Iterator<String> inputIterator = inputLines.iterator();
		while (inputIterator.hasNext()) {
			String inputLine = inputIterator.next();
			String tableType = inputLine.trim().toUpperCase();

			if (ReachesInitTable.isReachesInitTable(tableType)) {
				ReachesInitTable initTable = new ReachesInitTable(tableType, inputIterator, stateTime, exchangeItems);
				ReachesInitTable previous = reachesInitTables.put(tableType, initTable);
				if (previous != null) throw new IllegalArgumentException("Multiple RCHRES init tables '" + tableType + "' found in uci state file.");
				continue;
			}

			if (PerviousLandSegmentsInitTable.isPerviousLandSegmentsInitTable(tableType)) {
				PerviousLandSegmentsInitTable initTable = new PerviousLandSegmentsInitTable(tableType, inputIterator, stateTime, exchangeItems);
				PerviousLandSegmentsInitTable previous = perviousLandSegmentsInitTables.put(tableType, initTable);
				if (previous != null) throw new IllegalArgumentException("Multiple PERLND init tables '" + tableType + "' found in uci state file.");
				continue;
			}

			if (ImperviousLandSegmentsInitTable.isImperviousLandSegmentsInitTable(tableType)) {
				ImperviousLandSegmentsInitTable initTable = new ImperviousLandSegmentsInitTable(tableType, inputIterator, stateTime, exchangeItems);
				ImperviousLandSegmentsInitTable previous = imperviousLandSegmentsInitTables.put(tableType, initTable);
				if (previous != null) throw new IllegalArgumentException("Multiple IMPLND init tables '" + tableType + "' found in uci state file.");
				continue;
			}
		}
	}

	/**
	 * Write the values of all state exchangeItems to the uci file.
	 * Read file, then replace values of all exchange items in the file, then write file again.
	 * This way the parts of the file for which there are no exchange items will remain the same.
	 */
	private void writeUciFile() {
		if (!uciFile.exists()) throw new RuntimeException(getClass().getSimpleName() + ": Cannot find uci state file " + uciFile.getAbsolutePath());

		//read file.
		Results.putMessage(getClass().getSimpleName() + ": reading uci state file " + uciFile.getAbsolutePath());
		List<String> inputLines = AsciiFileUtils.readLines(uciFile);

		//replace values in file.
		Results.putMessage(getClass().getSimpleName() + ": replacing values in uci state file " + uciFile.getAbsolutePath());
		Iterator<String> inputIterator = inputLines.iterator();
		List<String> outputLines = new ArrayList<>();
		while (inputIterator.hasNext()) {
			String inputLine = inputIterator.next();
			String tableType = inputLine.trim().toUpperCase();

			if (ReachesInitTable.isReachesInitTable(tableType)) {
				//skip reading to end of table.
				UciUtils.skipToEnd(inputIterator);
				//write new table.
				ReachesInitTable initTable = reachesInitTables.get(tableType);
				if (initTable == null) throw new IllegalStateException(ReachesInitTable.RCHRES_MODULE_NAME + " init table '" + tableType + "' not initialized during reading of uci state file.");
				initTable.write(exchangeItems, outputLines);
				continue;
			}

			if (PerviousLandSegmentsInitTable.isPerviousLandSegmentsInitTable(tableType)) {
				//skip reading to end of table.
				UciUtils.skipToEnd(inputIterator);
				//write new table.
				PerviousLandSegmentsInitTable initTable = perviousLandSegmentsInitTables.get(tableType);
				if (initTable == null) throw new IllegalStateException(PerviousLandSegmentsInitTable.PERLND_MODULE_NAME + " init table '" + tableType + "' not initialized during reading of uci state file.");
				initTable.write(exchangeItems, outputLines);
				continue;
			}

			if (ImperviousLandSegmentsInitTable.isImperviousLandSegmentsInitTable(tableType)) {
				//skip reading to end of table.
				UciUtils.skipToEnd(inputIterator);
				//write new table.
				ImperviousLandSegmentsInitTable initTable = imperviousLandSegmentsInitTables.get(tableType);
				if (initTable == null) throw new IllegalStateException(ImperviousLandSegmentsInitTable.IMPLND_MODULE_NAME + " init table '" + tableType + "' not initialized during reading of uci state file.");
				initTable.write(exchangeItems, outputLines);
				continue;
			}

			//copy inputLine to output unchanged.
			outputLines.add(inputLine);
		}

		//write file.
		AsciiFileUtils.writeLines(uciFile, outputLines);
	}
}