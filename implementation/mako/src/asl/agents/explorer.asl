{ include("agent.asl") }

role(explorer).
// Store list of all agents except myself for broadcasting.
!generateExplorerAgentList.
+!generateExplorerAgentList:
    .my_name(Name)
    <-
    AgentList = [
    	explorer1,
    	explorer2,
    	explorer3,
    	explorer4,
    	explorer5,
    	explorer6
    ];
    .delete(Name, AgentList, BroadcastList); 
    +explorerAgentList(BroadcastList).

+probedVertex(Vertex, Value)[source(percept)]:
	not probedVertex(Vertex, Value)[source(self)]
	<-
	.print("I learned that the value of ", Vertex, " is ", Value, ".");
	.send(Vertex,tell,nodeValue(Vertex,Value)); // send node value info to respective node agent
    ?explorerAgentList(ExplorerList);
    .send(ExplorerList, tell, probedVertex(Vertex,Value));
	+probedVertex(Vertex, Value)[source(self)].
	
+probedVertex(Vertex, Value)[source(Agent)]
	:
	Agent \== self
	<-
	.print("I learned that the value of ", Vertex, " is ", Value, " from agent ", Agent, ".");
	.abolish(probedVertex(Vertex, Value));
	+probedVertex(Vertex, Value)[source(self)].
	
+probedVertex(Vertex, Value)[source(Agent)]
	:
	probedVertex(Vertex, Value)[source(self)]
	& Agent \== self
	<-
	.print("I already knew that the value of vertex ", Vertex, " is ", Value, ", ", Agent, "!");
	.abolish(Vertex, Value);
	+probedVertex(Vertex, Value)[source(self)].
	
// If the agent has enough energy than probe. Otherwise recharge.
+!doProbing:
	energy(Energy) & Energy > 1 & position(Position)
	<- .print("Probing Vertex ", Position);
		probe.
	
+!doProbing:
 energy(Energy) & Energy < 1
	<- .print("I have not enough energy to probe. I'll recharge first.");
    	recharge.

// Explorer has to flee from enemy agents.
+!dealWithEnemy <- !avoidEnemy.