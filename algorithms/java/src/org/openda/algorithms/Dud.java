/* OpenDA v2.3.1 
* Copyright (c) 2016 OpenDA Association 
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


package org.openda.algorithms;

import org.openda.interfaces.*;
import org.openda.utils.io.CalRestartSettings;

/**
 * OpenDA Dud algorithm
 */
public class Dud extends BaseDud {

	
	protected BaseDudCoreOptimizer InitializeDudCoreOptimizer() {
		return new DudCoreOptimizer(J);
	}


	
	public void releaseInternalState(IModelState savedInternalState) {
		if (!(savedInternalState instanceof CalRestartSettings)) {
			throw new IllegalArgumentException("Unexpected saved State type: " + savedInternalState.getClass().getName());
		}
		// no action needed
	}

}
