//author: sewell

//Initial zone value for this node is 0.
zoneValue(0).

@calculateZone[atomic]
+!calculateZone
	<-
	.print("Entering calculateZone plan.");
	-+zoneValue(0);
	.findall(Vertex, neighbour(Vertex, _), OneHopNeighbourList);
	.print("My one-hop neighbour list is ", OneHopNeighbourList);
	.findall(Vertex, minStepsPath(Vertex, _, 2, _), TwoHopNeighbourList);
	-+twoHopNeighbourList(TwoHopNeighbourList); // We need to save the list because we need to know where to position the agents to make our zone.
	.print("My two-hop neighbour list is ", TwoHopNeighbourList);
	.concat(OneHopNeighbourList, TwoHopNeighbourList, OneAndTwoHopNeighbourList);
	.print("My one and two-hop neighbour list is ", OneAndTwoHopNeighbourList);
	for (.member(Neighbour, OneAndTwoHopNeighbourList)) {
		?nodeValue(Neighbour, NodeValue);
		!addToZoneValue(Neighbour, NodeValue);
	}
	?zoneValue(ZoneValue); .print("Finished calculateZone plan. My zone value is ", ZoneValue, ".")
	.
	
+!addToZoneValue(Neighbour, NodeValue):
	zoneValue(CurrentZoneValue)
	<-
	NewZoneValue = CurrentZoneValue + NodeValue;
	.print("My current zone value is ", CurrentZoneValue, ". The node value of my two-hop neighbour ", Neighbour, " is ", NodeValue, ". If I add this neighbour to my zone, my new zone value will be ", NewZoneValue, ".");
	-+zoneValue(NewZoneValue).