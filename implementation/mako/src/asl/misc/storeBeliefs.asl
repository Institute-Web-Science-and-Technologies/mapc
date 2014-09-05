+achievement(Identifier)[source(percept)] <-
  //.print("Received percept achievement(", Identifier, ").");
  +achievement(Identifier)[source(self)].

//+bye[source(percept)] <-
  //.print("Received percept bye").

+deadline(Numeral)[source(percept)] <-
  //.print("Received percept deadline(", Numeral, ").");
  -+deadline(Numeral)[source(self)].

//+edges(Numeral)[source(percept)] <-
//  .print("Received percept edges(", Numeral, ").").

+energy(Numeral)[source(percept)] <-
  //.print("Received percept energy(", Numeral, ").");
  -+energy(Numeral)[source(self)].

+health(Numeral)[source(percept)] <-
  //.print("Received percept health(", Numeral, ").");
  -+health(Numeral)[source(self)].

//+id(Numeral)[source(percept)] <-
  //.print("Received percept id(", Numeral, ").").
  
//+inspectedEntity(Name, Team, Role, Vertex, Energy, MaxEnergy, Health, MaxHealth, Strength, VisRange)

+lastAction(Identifier)[source(percept)] <-
  //.print("Received percept lastAction(", Identifier, ").");
  -+lastAction(Identifier).

+lastActionResult(Identifier)[source(percept)] <-
  //.print("Received percept lastActionResult(", Identifier, ").");
  -+lastActionResult(Identifier).

//+lastStepScore(Numeral)[source(percept)] <-
//  .print("Received percept lastStepScore(", Numeral, ").").

+maxEnergy(Numeral)[source(percept)] <-
  //.print("Received percept maxEnergy(", Numeral, ").");
  -+maxEnergy(Numeral)[source(self)].

+maxEnergyDisabled(Numeral)[source(percept)] <-
  //.print("Received percept maxEnergyDisabled(", Numeral, ").");
  -+maxEnergyDisabled(Numeral)[source(self)].

//+maxHealth(Numeral)[source(percept)] <-
  //.print("Received percept maxHealth(", Numeral, ").").

//+money(Numeral)[source(percept)] <-
  //.print("Received percept money(", Numeral, ").".

//myName is a custom percept
+myName(Name)[source(percept)] <-
    .my_name(JName);
//    .print("My Server Name is ", Name, ". My Jason name is ", JName);
    -+myName(Name)[source(self)].

+position(Identifier)[source(percept)]
    <- //.print("Received percept position(", Vertex, ").");
       -+position(Identifier).

//+probedVertex(Vertex, Value)[source(percept)] <-
//  .print("Received percept probedVertex(", Vertex, ",", Value, ").");
//  -+probedVertex(Vertex, Value)[source(storeBeliefs)].

+myTeam(Team)[source(percept)]
<- -+myTeam(Team).

//+ranking(Numeral)[source(percept)] <-
//  .print("Received percept ranking(", Numeral, ").").

//The requestAction belief is handled by agent.asl.
//+requestAction[source(percept)] <-
//  .print("Received percept requestAction.").

+role(Identifier)[source(percept)] <-
//  .print("Received percept role(", Identifier, ").");
  -+role(Identifier).

//+score(Numeral)[source(percept)] <-
//  .print("Received percept score(", Numeral, ").").
//
//+simEnd(Numeral)[source(percept)] <-
//  .print("Received percept simEnd(", Numeral, ").").
//
//+simStart[source(percept)] <-
//  .print("Received percept simStart.").

// Wake up all coaches and tell them to destroy their current zone properly by
// also informing JavaMap and their minions.
+step(Numeral)[source(percept)]:
    isCoach(true)
    & plannedZoneTimeInSteps(Steps)
    & Numeral mod Steps == 0
    <- .print("[zoning] periodic trigger for coaches to destroy their zones.");
       -+step(Numeral);
       !cancelledZoneBuilding.

// Make coaches check for enemies in range at every step to be able to call
// saboteurs for help.
+step(Numeral)[source(percept)]:
    isCoach(true)
    <- -+step(Numeral);
       !checkZoneUnderAttack.

+step(Numeral)[source(percept)] <-
//  .print("Received percept step(", Numeral, ").");
  -+step(Numeral).

//+steps(Numeral)[source(percept)] <-
//  .print("Received percept steps(", Numeral, ").").
//
//+strength(Numeral)[source(percept)] <-
//  .print("Received percept strength(", Numeral, ").").
//
//+timestamp(Numeral)[source(percept)] <-
//  .print("Received percept timestamp(", Numeral, ").").

//+vertices(Numeral)[source(percept)] <-
//  .print("Received percept vertices(", Numeral, ").").

//+visRange(Numeral)[source(percept)] <-
//  .print("Received percept visRange(", Numeral, ").").

//+visibleEdge(Vertex1, Vertex2)[source(percept)] <-
//  .print("Received percept visibleEdge(", Vertex1, ",", Vertex2, ").").

+visibleEntity(Agent,Vertex,Team,State)[source(percept)] <-
  .print("@@@@@@@@@@@@@@@@@@Received percept visibleEntity(", Agent, ",", Vertex, ",", Team, ",", State, ").").

//+visibleVertex(Vertex, Team)[source(percept)] <-
//  .print("Received percept visibleVertex(", Vertex, ",", Team, ").").

//+zoneScore(Numeral)[source(percept)] <-
//  .print("Received percept zoneScore(", Numeral, ").").

//+zonesScore(Numeral)[source(percept)] <-
//  .print("Received percept zonesScore(", Numeral, ").").

