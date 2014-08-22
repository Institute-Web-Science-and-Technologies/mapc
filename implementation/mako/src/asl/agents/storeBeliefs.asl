// Agent storeBeliefs in project mako

/* Initial beliefs and rules */

/* Initial goals */


/* Plans */

+achievement(Identifier)[source(percept)] <-
	.print("Received percept achievement(", Identifier, ").");
	+achievement(Identifier)[source(self)].
	
+bye[source(percept)] <-
	.print("Received percept bye");
	true. //TODO
	
+deadline(Numeral)[source(percept)] <-
	.print("Received percept deadline(", Numeral, ").");
	-+deadline(Numeral)[source(self)].
	
+energy(Numeral)[source(percept)] <-
	.print("Received percept energy(", Numeral, ").");
	-+energy(Numeral)[source(self)].
	
+health(Numeral)[source(percept)] <-
	.print("Received percept health(", Numeral, ").");
	-+health(Numeral)[source(self)].

+id(Numeral)[source(percept)] <-
	.print("Received percept id(", Numeral, ").").
	
+lastAction(Identifier)[source(percept)] <-
	.print("Received percept lastAction(", Identifier, ").");
	-+lastAction(Identifier).
	
+lastActionResult(Identifier)[source(percept)] <-
	.print("Received percept lastActionResult(", Identifier, ").");
	-+lastActionResult(Identifier).
	
+maxEnergy(Numeral)[source(percept)] <-
	.print("Received percept maxEnergy(", Numeral, ").");
	-+maxEnergy(Numeral)[source(self)].

+maxEnergyDisabled(Numeral)[source(percept)] <-
	.print("Received percept maxEnergyDisabled(", Numeral, ").");
	-+maxEnergyDisabled(Numeral)[source(self)].
	
+maxHealth(Numeral)[source(percept)] <-
	.print("Received percept maxHealth(", Numeral, ").").
	
+money(Numeral)[source(percept)] <-
	.print("Received percept money(", Numeral, ").").
	
//myName is a custom percept
+myName(Name)[source(percept)] <-
    .print("My Server Name is: ", Name);
    .my_name(JName);
    .print("My Jason Name is: ", JName); //Why does this get printed twice?
    -+myName(Name)[source(self)].

+position(Identifier)[source(percept)]
    <- .print("Received percept position(", Vertex, ")."); 
       -+position(Identifier).
       
//+probedVertex(Vertex, Value)[source(percept)] <-
//	.print("Received percept probedVertex(", Vertex, ",", Value, ").");
//	-+probedVertex(Vertex, Value)[source(storeBeliefs)].

	
+ranking(Numeral)[source(percept)] <-
	.print("Received percept ranking(", Numeral, ").").
	
+role(Identifier)[source(percept)] <-
	.print("Received percept role(", Identifier, ").");
	-+role(Identifier).
	
+score(Numeral)[source(percept)] <-
	.print("Received percept score(", Numeral, ").").
	
+simEnd(Numeral)[source(percept)] <-
	.print("Received percept simEnd(", Numeral, ").").
	
+simStart[source(percept)] <-
	.print("Received percept simStart.").
	
+step(Numeral)[source(percept)] <-
	.print("Received percept step(", Numeral, ").");
	-+step(Numeral).
	
+steps(Numeral)[source(percept)] <-
	.print("Received percept steps(", Numeral, ").").

+strength(Numeral)[source(percept)] <-
	.print("Received percept strength(", Numeral, ").").
	
+timestamp(Numeral)[source(percept)] <-
	.print("Received percept timestamp(", Numeral, ").").

+visRange(Numeral)[source(percept)] <-
	.print("Received percept visRange(", Numeral, ").").
	
+visibleEntity(Agent,Vertex,Team,State)[source(percept)] <-
	.print("Received percept visibleEntity(", Agent, ",", Vertex, ",", Team, ",", State, ").").

+zoneScore(Numeral)[source(percept)] <-
	.print("Received percept zoneScore(", Numeral, ").").