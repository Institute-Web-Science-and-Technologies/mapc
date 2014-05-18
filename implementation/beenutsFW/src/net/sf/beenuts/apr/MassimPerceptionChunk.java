package net.sf.beenuts.apr;

import java.util.*;
import eis.iilang.*;

/**
 * a wrapper class used for passing perceptions from
 * an eis massim relay to an agent process. this is
 * required because instanceof does not work for
 * generics at runtime.
 *  
 * @author Thomas Vengels
 *
 */
public class MassimPerceptionChunk {

	/** wrapped perceptions */
	public final Collection<Percept> perceptions;
	
	/** default constructor */
	public MassimPerceptionChunk(Collection<Percept> perceptions) {
		this.perceptions = perceptions;
	}
}
