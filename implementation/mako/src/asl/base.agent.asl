// Agent baseAgent in project mako
{ include("actions.goto.asl") }
{ include("actions.repair.asl") }
{ include("actions.recharge.asl") }
{ include("storeBeliefs.asl") }


//Print all received beliefs. Used for debugging. (comment this out if your simulation crashes immediately)
//@debug[atomic] +Belief <-
//    .print("Received new belief from percept: ", Belief);
//    for (B) {
//        .print("    When ", Belief, " is added, this is another belief in the belief base: ", B);
//    }
//    -Belief;
//    .print("        Removed ", Belief, " from belief base.").
    

/* Initial beliefs and rules */
lowEnergy :- energy(Energy)[source(percept)] & Energy < 10.
//isVertexSurveyed(Vertex) :- .send(cartographer, askOne, surveyed(Vertex)).

/* Initial goals */

/* Events */
    
+position(Vertex)[source(percept)]
    <- .my_name(Name);
       .send(cartographer, tell, position(Name, Vertex));
       .print("New position(", Vertex, ")");
        -+position(Vertex)[source(self)].

+visibleEdge(VertexA, VertexB)[source(percept)]
    <- .send(cartographer, tell, edge(VertexA, VertexB, 1000)).

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
    <- .print("Surveyed Edge: ", VertexA, " -> ", VertexB, " Value: ", Weight);
       .send(cartographer, tell, edge(VertexA, VertexB, Weight)).

+edges(AmountEdges)[source(percept)]
    <- internalActions.setGlobalEdgesAmount(AmountEdges). //TODO: send to cartographer

+vertices(AmountVertices)[source(percept)]
    <- internalActions.setGlobalVerticesAmount(AmountVertices).

+probedVertex(Vertex, Value)[source(percept)]:
    .number(Value)
    <- .send(cartographer, tell, probed(Vertex, Value));
       .print("New probedVertex(", Vertex, " ", Value, ")");
       -+probedVertex(Vertex, Value)[source(self)]. // TODO: do we still want/need to set local knowledge?
        
+visibleVertex(Vertex, Team)[source(percept)]:
    .literal(Team)
    <- .send(cartographer, tell, visibleVertex(Vertex));
       .send(cartographer, tell, occupied(Vertex, Team)). // TODO: merge this with probed (minimum node value is 1)? Maybe also merge it with position

+visibleVertex(Vertex, Team)[source(percept)]
	<- .send(cartographer, tell, visibleVertex(Vertex)).
//TODO: visibleEntity, zoneScore
    
+simStart 
    <- .print("Simulation started."). 
   
+step(Step)[source(self)] 
    <- .print("Current step is ", Step);
       .send(cartographer, askAll, surveyed(Vertex));
       !walkAround.    

/* Plans */

+!getNextVertex:
    position(Vertex)[source(self)] & surveyedEdge(X, Y, Z) & X==Vertex
    <- .findall(Neighbor, surveyedEdge(Vertex, Neighbor, Cost),Neighbors);
       .findall(Cost,surveyedEdge(Vertex, Y, Cost),List);
       .findall(SurveyedNode, surveyed(SurveyedNode), SurveyedNodes);
       .findall(Member,(.member(Member, Neighbors) & not .member(Member, SurveyedNodes)), NotSurveyed);
       .print("How many not surveyed neighbors: ", .length(NotSurveyed));
	   .nth(0,NotSurveyed,NotSurveyedNode);
	   .print("not Surveyed Node: ",NotSurveyedNode);
       //.min(List, MinValue);
       //.print("Minimum Cost: ",MinValue);
       //?surveyedEdge(Vertex, NextNode, MinValue);
       //?surveyedEdge(NextNode, Vertex, MinValue);
       //.print("Going to ",NextNode," which has cost of ",MinValue);
       goto(NotSurveyedNode);
       .print("went to node ", NotSurveyedNode);
       !finaliseStep.

// TODO: at some point in time this (and/or the other) method tries to navigate the agent to the vertex he is currently located on
+!getNextVertex:
    position(Vertex)[source(self)] & surveyedEdge(X, Y, Z) & Y==Vertex
    <- .findall(Neighbor, surveyedEdge(Neighbor, Vertex, Cost),Neighbors);
       .findall(SurveyedNode, surveyed(SurveyedNode), SurveyedNodes);
       .findall(Member,(.member(Member, Neighbors) & not .member(Member, SurveyedNodes)), NotSurveyed);
       .print("How many not surveyed neighbors: ", .length(NotSurveyed));
       .nth(0,NotSurveyed,NotSurveyedNode);
	   .print("not Surveyed Node: ",NotSurveyedNode);
       .findall(Cost2,surveyedEdge(Y2, Vertex, Cost2), List2);
       //.min(List2, MinValue2);
       //.print("Minimum Cost (First Plan fails): ",MinValue2);
       //?surveyedEdge(NextNode2, Vertex, MinValue2);
       //.print("Going to ",NextNode2," which has cost of ",MinValue2);
       goto(NotSurveyedNode);
       .print("went to node ", NotSurveyedNode);
       !finaliseStep.

// offer applicable plans for all other states:
//-!getNextVertex
//    <- .print("Could not find next vertex.");
//       .fail_goal(getNextVertex).

+!walkAround:
    energy(E)[source(self)] & E<10
    <- .print("My energy is low, going to recharge.");
       recharge;
       -energy(E)[source(self)].

+!walkAround:
    position(Vertex)[source(self)] & surveyed(Vertex)
    <- .print("Surveyed ", Vertex, ". Now getNextVertex ");
       !getNextVertex.
       //goto(NextVertex).
   
+!walkAround:
    position(Vertex)[source(self)] & not surveyed(Vertex)
    <- .print("Not surveyed ", Vertex);
       survey;
       +surveyed(Vertex);
       .send(cartographer, tell, surveyed(Vertex)).

// if the earlier plans weren't applicable, cancel the plan:
+!walkAround
    <- .print("Could not walk around.");
       .fail_goal(walkAround).
       
-!walkAround
    <- .print("Could not walk around.").
   
+!finaliseStep: visibleEdge(VertexA, VertexB) & surveyedEdge(VertexA, VertexB, Weight) & visibleVertex(Vertex, Team)
	<-  -+visibleEdge(VertexA, VertexB);
		-+surveyedEdge(VertexA, VertexB, Weight);
		-+visibleVertex(Vertex, Team).