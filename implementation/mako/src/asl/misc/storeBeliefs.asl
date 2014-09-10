// Uncomment this line to receive debug output from storeBeliefs.asl
//debug.

+achievement(Identifier)[source(percept)]: debug <-
  .print("Received percept achievement(", Identifier, ").");
  +achievement(Identifier)[source(self)].
  
+achievement(Identifier)[source(percept)] <-
  +achievement(Identifier)[source(self)].

+bye[source(percept)]: debug <-
  .print("Received percept bye").

+deadline(Numeral)[source(percept)]: debug <-
  .print("Received percept deadline(", Numeral, ").");
  -+deadline(Numeral)[source(self)].
  
+deadline(Numeral)[source(percept)] <-
  -+deadline(Numeral)[source(self)].

+edges(Numeral)[source(percept)]: debug <-
  .print("Received percept edges(", Numeral, ").").

+energy(Numeral)[source(percept)]: debug <-
  .print("Received percept energy(", Numeral, ").");
  -+energy(Numeral)[source(self)].
  
+energy(Numeral)[source(percept)] <-
  -+energy(Numeral)[source(self)].

+health(Numeral)[source(percept)]: debug <-
  .print("Received percept health(", Numeral, ").");
  -+health(Numeral)[source(self)].
  
+health(Numeral)[source(percept)] <-
  -+health(Numeral)[source(self)].

+id(Numeral)[source(percept)]: debug <-
  .print("Received percept id(", Numeral, ").").
  
+lastAction(Identifier)[source(percept)]: debug <-
  .print("Received percept lastAction(", Identifier, ").");
  -+lastAction(Identifier).
  
+lastAction(Identifier)[source(percept)] <-
  -+lastAction(Identifier).

+lastActionResult(Identifier)[source(percept)]: debug <-
  .print("Received percept lastActionResult(", Identifier, ").");
  -+lastActionResult(Identifier).
  
+lastActionResult(Identifier)[source(percept)] <-
  -+lastActionResult(Identifier).

+lastStepScore(Numeral)[source(percept)]: debug <-
  .print("Received percept lastStepScore(", Numeral, ").").

+maxEnergy(Numeral)[source(percept)]: debug <-
  .print("Received percept maxEnergy(", Numeral, ").");
  -+maxEnergy(Numeral)[source(self)].
  
+maxEnergy(Numeral)[source(percept)] <-
  -+maxEnergy(Numeral)[source(self)].

+maxEnergyDisabled(Numeral)[source(percept)]: debug <-
  .print("Received percept maxEnergyDisabled(", Numeral, ").");
  -+maxEnergyDisabled(Numeral)[source(self)].
  
+maxEnergyDisabled(Numeral)[source(percept)] <-
  -+maxEnergyDisabled(Numeral)[source(self)].

+maxHealth(Numeral)[source(percept)]: debug <-
  .print("Received percept maxHealth(", Numeral, ").").
  
+maxHealth(Numeral)[source(percept)] <-
  .print("Received percept maxHealth(", Numeral, ").").

+money(Numeral)[source(percept)] <-
  .print("Received percept money(", Numeral, ").");
  -+money(Numeral)[source(self)].
  
+money(Numeral)[source(percept)] <-
  -+money(Numeral)[source(self)].

//myName is a custom percept
+myName(Name)[source(percept)] <-
    .my_name(JName);
    .print("My Server Name is ", Name, ". My Jason name is ", JName);
    -+myName(Name)[source(self)].

+position(Identifier)[source(percept)]: debug <-
	.print("Received percept position(", Identifier, ").");
	-+position(Identifier).
       
+position(Identifier)[source(percept)] <-
	-+position(Identifier).

+probedVertex(Vertex, Value)[source(percept)]: debug <-
  .print("Received percept probedVertex(", Vertex, ",", Value, ").").
  
+myTeam(Team)[source(percept)]: debug <-
	.print("Received percept myTeam(", Team, ").");
	-+myTeam(Team).

+myTeam(Team)[source(percept)] <-
	-+myTeam(Team).

+ranking(Numeral)[source(percept)]: debug <-
  .print("Received percept ranking(", Numeral, ").").

+requestAction[source(percept)]: debug <-
  .print("Received percept requestAction.");
  -+requestAction[source(self)].

+role(Identifier)[source(percept)]: debug <-
  .print("Received percept role(", Identifier, ").");
  -+role(Identifier).
  
+role(Identifier)[source(percept)] <-
  -+role(Identifier).

+score(Numeral)[source(percept)]: debug <-
  .print("Received percept score(", Numeral, ").").

+simEnd(Numeral)[source(percept)]: debug <-
  .print("Received percept simEnd(", Numeral, ").").

+simStart[source(percept)]: debug <-
  .print("Received percept simStart.").

+step(Numeral)[source(percept)] <-
  .print("Received percept step(", Numeral, ").");
  -+step(Numeral)[source(self)].
  
+step(Numeral)[source(percept)]: debug <-
  .print("Received percept step(", Numeral, ").");
  -+step(Numeral)[source(self)].

+steps(Numeral)[source(percept)]: debug <-
  .print("Received percept steps(", Numeral, ").").

+strength(Numeral)[source(percept)]: debug <-
  .print("Received percept strength(", Numeral, ").").

+timestamp(Numeral)[source(percept)]: debug <-
  .print("Received percept timestamp(", Numeral, ").").

+vertices(Numeral)[source(percept)]: debug <-
  .print("Received percept vertices(", Numeral, ").").

+visRange(Numeral)[source(percept)]: debug <-
  .print("Received percept visRange(", Numeral, ").").

+visibleEdge(Vertex1, Vertex2)[source(percept)]: debug <-
  .print("Received percept visibleEdge(", Vertex1, ",", Vertex2, ").").

+visibleEntity(Agent,Vertex,Team,State)[source(percept)]: debug <-
  .print("Received percept visibleEntity(", Agent, ",", Vertex, ",", Team, ",", State, ").").

+visibleVertex(Vertex, Team)[source(percept)]: debug <-
  .print("Received percept visibleVertex(", Vertex, ",", Team, ").").

+zoneScore(Numeral)[source(percept)]: debug <-
  .print("Received percept zoneScore(", Numeral, ").").

+zonesScore(Numeral)[source(percept)]: debug <-
  .print("Received percept zonesScore(", Numeral, ").").

