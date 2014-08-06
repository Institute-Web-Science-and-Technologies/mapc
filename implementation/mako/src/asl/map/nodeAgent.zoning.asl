//author: sewell

//Initial zone value for this node is 0.
zoneValue(0).

@calculateZone[atomic]
+!calculateZone:
	twoHopNeighbours(TwoHopNeighbourList)
	<-
	.print("Entering calculateZone plan.");
	-+zoneValue(0);
	.findall([Vertex, Value], nodeValue(Vertex, Value), NeighbourList);
	for (.member([Neighbour, NeighbourValue], NeighbourList)) {
		!addToZoneValue(Neighbour, NeighbourValue);
	}
	?zoneValue(ZoneValue);
	DividedZoneValue = ZoneValue / (.length(TwoHopNeighbourList) + 1);
	.print("Finished calculateZone plan. My zone value is ", ZoneValue, ". My divided zone value is ", DividedZoneValue)
	.
	
+!addToZoneValue(Neighbour, NodeValue):
	zoneValue(CurrentZoneValue)
	<-
	NewZoneValue = CurrentZoneValue + NodeValue;
	.print("My current zone value is ", CurrentZoneValue, ". The node value of my two-hop neighbour ", Neighbour, " is ", NodeValue, ". If I add this neighbour to my zone, my new zone value will be ", NewZoneValue, ".");
	-+zoneValue(NewZoneValue).