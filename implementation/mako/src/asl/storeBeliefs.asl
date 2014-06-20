// Agent storeBeliefs in project mako

/* Initial beliefs and rules */

/* Initial goals */


/* Plans */

+achievement(Identifier)[source(percept)] <-
	.print("New achievement(", Identifier, ")");
	+achievement(Identifier)[source(self)].
	
+bye[source(percept)] <-
	.print("New bye");
	true. //TODO
	
+deadline(Numeral)[source(percept)] <-
	.print("New deadline(", Numeral, ")");
	-+deadline(Numeral)[source(self)].
	
//+edges(Numeral)[source(percept)] <-
//	.print("New edges(", Numeral, ")");
//	-+edges(Numeral)[source(self)].
	
+energy(Numeral)[source(percept)] <-
	.print("New energy(", Numeral, ")");
	-+energy(Numeral)[source(self)].
	
+health(Numeral)[source(percept)] <-
	.print("New health(", Numeral, ")");
	-+health(Numeral)[source(self)].

//TODO: Overlord
+id(Identifier)[source(percept)] <-
	.print("New id(", Identifier, ")");
	-+id(Identifier)[source(self)].
	
+lastAction(Identifier)[source(percept)] <-
	.print("New lastAction(", Identifier, ")");
	-+lastAction(Identifier)[source(self)].
	
+lastActionParam(Identifier)[source(percept)] <-
	.print("New lastActionParam(", Identifier, ")");
	-+lastActionParam(Identifier)[source(self)].

+lastActionResult(Identifier)[source(percept)] <-
	.print("New lastActionResult(", Identifier, ")");
	-+lastActionResult(Identifier)[source(self)].

//TODO: this should probably be sent to an Overlord agent	
+lastStepScore(Numeral)[source(percept)] <-
	.print("New lastStepScore(", Numeral, ")");
	-+lastStepScore(Numeral)[source(self)].
	
+maxEnergy(Numeral)[source(percept)] <-
	.print("New maxEnergy(", Numeral, ")");
	-+maxEnergy(Numeral)[source(self)].

//TODO: Overlord
+money(Numeral)[source(percept)] <-
	.print("New money(", Numeral, ")");
	-+money(Numeral)[source(self)].

//Handled in BaseAgent: position, probedVertex, surveyedEdge, vertices, visibleEdge, visibleEntity(?), visibleVertex(?), zoneScore(?)


//TODO: Overlord
+ranking(Numeral)[source(percept)] <-
	.print("New ranking(", Numeral, ")");
	-+ranking(Numeral)[source(self)].
	
+requestAction[source(percept)] <-
	.print("New requestAction");
	-+requestAction[source(self)].
	
//Note: eismassim.pdf says this is a parameterless percept, but this seems to be different in practice
+role(Identifier)[source(percept)] <-
	.print("New role(", Identifier, ")");
	-+role(Identifier)[source(self)].
	
//TODO: Overlord
+score(Numeral)[source(percept)] <-
	.print("New score(", Numeral, ")");
	-+score(Numeral)[source(self)].
	
+simEnd[source(percept)] <-
	.print("New simEnd");
	-+simEnd[source(self)].
	
+simStart[source(percept)] <-
	.print("New simStart");
	-+simStart[source(self)].

//TODO: Overlord
+step(Numeral)[source(percept)] <-
	.print("New step(", Numeral, ")");
	-+step(Numeral)[source(self)].
	
//TODO: Overlord
+steps(Numeral)[source(percept)] <-
	.print("New steps(", Numeral, ")");
	-+steps(Numeral)[source(self)].
	
+strength(Numeral)[source(percept)] <-
	.print("New strength(", Numeral, ")");
	-+strength(Numeral)[source(self)].

//TODO: Overlord?
+timestamp(Numeral)[source(percept)] <-
	.print("New timestamp(", Numeral, ")");
	-+timestamp(Numeral)[source(self)].
	
+visRange(Numeral)[source(percept)] <-
	.print("New visRange(", Numeral, ")");
	-+visRange(Numeral)[source(self)].
	
//TODO: Overlord, Cartographer?
+zonesScore(Numeral)[source(percept)] <-
	.print("New zonesScore(", Numeral, ")");
	-+zonesScore(Numeral)[source(self)].
	
//myName is a custom percept
+myName(Name)[source(percept)] <-
    .print("My Server Name is: ", Name);
    .my_name(JName);
    .print("My Jason Name is: ", JName);
    -+myName(Name)[source(self)].