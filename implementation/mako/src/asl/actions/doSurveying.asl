// If the agent has enough energy, then survey. Otherwise recharge.
+!doSurveying:
    energy(Energy)
    & Energy < 1
    <- .print("I don't have enough energy to survey. I'll recharge first.");
        recharge.

+!doSurveying:
    position(Position)
    <-
    .print("Surveying ", Position);
    survey.