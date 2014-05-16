package mas.agentsHempelsSofa.data;

public class Strategy {

    /* --------------------------- Fields ------------------------ */

    /**
     * all available focuses which should be supported by the specific agents.
     */
    public static final String[] allFocuses = { "offensiveDestroyZone",
            "offensiveDestroyAgents", "offensiveDrawback", "defensiveParry",
            "defensiveRunAway", "defensiveRepair", "zoneExpand",
            "zoneDrawback", "zoneStability", "zoneMainZone", "buyBattery",
            "buySabotageDevice", "buySensor", "buyShield",
            "achievementsProbedVertices", "achievementsSurveyedEdges",
            "achievementsInspectedAgents", "achievementsSuccessfulAttacks",
            "achievementsSuccessfulParries", "achievementsAreaValue" };

    /* ------------------------- Preferences --------------------- */

    /**
     * the preference for offensive strategies.
     */
    public double offensivePref;
    /**
     * the preference of a zone-destroying focus, if an offensive strategy was
     * chosen.
     */
    public double offensiveDestroyZonesPref;
    /**
     * the preference of an agent-destroying focus, if an offensive action was
     * generated.
     */
    public double offensiveDestroyAgentsPref;
    /**
     * the preference of a drawback focus, if an offensive action was generated.
     */
    public double offensiveDrawbackPref;

    /**
     * the preference for defensive strategies.
     */
    public double defensivePref;
    /**
     * the preference of a parry focus, if an defensive strategy was chosen.
     */
    public double defensiveParryPref;
    /**
     * the preference of an run away focus, if an defensive action was
     * generated.
     */
    public double defensiveRunAwayPref;
    /**
     * the preference of an defensive repair focus, if an defensive action was
     * generated.
     */
    public double defensiveRepairPref;

    /**
     * the preference for zone strategies.
     */
    public double zonePref;
    /**
     * the preference of a zone-expanding focus, if an zone strategy was chosen.
     */
    public double zoneExpandPref;
    /**
     * the preference of a drawback focus, if an zone strategy was chosen. This
     * could be increased, if the zone should be expanded but if an agent is
     * nearing, the agent draws back to defend the zone.
     */
    public double zoneDrawbackPref;
    /**
     * the preference of a zone-stability focus, if an zone strategy was chosen.
     */
    public double zoneStabilityPref;
    /**
     * the preference of a main-zone focus, if an zone strategy was chosen.
     */
    public double zoneMainZonePref;

    /**
     * the preference for buy strategies.
     */
    public double buyPref;
    /**
     * the preference of a battery focus, if a buy strategy was chosen.
     */
    public double buyBatteryPref;
    /**
     * the preference of a sabotage device focus, if a buy strategy was chosen.
     */
    public double buySabotageDevicePref;
    /**
     * the preference of a sensor focus, if a buy strategy was chosen.
     */
    public double buySensorPref;
    /**
     * the preference of a shield focus, if a buy strategy was chosen.
     */
    public double buyShieldPref;

    /**
     * the preference for achievement-supporting strategies.
     */
    public double achievementsPref;
    /**
     * the preference of a probed-vertices focus, if an achievement-supporting
     * strategy was chosen.
     */
    public double achievementsProbedVerticesPref;
    /**
     * the preference of a surveyed-edges focus, if an achievement-supporting
     * strategy was chosen.
     */
    public double achievementsSurveyedEdgesPref;
    /**
     * the preference of an inspected-agents focus, if an achievement-supporting
     * strategy was chosen.
     */
    public double achievementsInspectedAgentsPref;
    /**
     * the preference of a successful-attacks focus, if an
     * achievement-supporting strategy was chosen.
     */
    public double achievementsSuccessfulAttacksPref;
    /**
     * the preference of a successful-parries focus, if an
     * achievement-supporting strategy was chosen.
     */
    public double achievementsSuccessfulParriesPref;
    /**
     * the preference of an area-value focus, if an achievement-supporting
     * strategy was chosen.
     */
    public double achievementsAreaValuePref;

    /* -------------------------- Goals -------------------------- */

    /**
     * The goal number of probed vertices.
     */
    public double achievementsProbedVerticesGoal;
    /**
     * The goal number of surveyed edges.
     */
    public double achievementsSurveyedEdgesGoal;
    /**
     * The goal number of inspected agents.
     */
    public double achievementsInspectedAgentsGoal;
    /**
     * The goal number of successful attacks.
     */
    public double achievementsSuccessfulAttacksGoal;
    /**
     * The goal number of successful parries.
     */
    public double achievementsSuccessfulParriesGoal;
    /**
     * The goal area value.
     */
    public double achievementsAreaValueGoal;

    /* --------------------- limits for buy actions -------------------- */
    /**
     * the limit for max energy
     */
    public double maxEnergyLimit;
    /**
     * the limit for max health
     */
    public double maxHealthLimit;
    /**
     * the limit for visiblity range
     */
    public double visRangeLimit;
    /**
     * the limit for strength
     */
    public double strengthLimit;

    /* --------------------- Provided Methods -------------------- */

    @Override
    public Strategy clone () {
        Strategy newStrategy = new Strategy();

        newStrategy.offensivePref = offensivePref;
        newStrategy.offensiveDestroyZonesPref = offensiveDestroyZonesPref;
        newStrategy.offensiveDestroyAgentsPref = offensiveDestroyAgentsPref;
        newStrategy.offensiveDrawbackPref = offensiveDrawbackPref;

        newStrategy.defensivePref = offensivePref;
        newStrategy.defensiveParryPref = defensiveParryPref;
        newStrategy.defensiveRunAwayPref = defensiveRunAwayPref;
        newStrategy.defensiveRepairPref = defensiveRepairPref;

        newStrategy.zonePref = zonePref;
        newStrategy.zoneExpandPref = zoneExpandPref;
        newStrategy.zoneDrawbackPref = zoneDrawbackPref;
        newStrategy.zoneStabilityPref = zoneStabilityPref;
        newStrategy.zoneMainZonePref = zoneMainZonePref;

        newStrategy.buyPref = buyPref;
        newStrategy.buyBatteryPref = buyBatteryPref;
        newStrategy.buySabotageDevicePref = buySabotageDevicePref;
        newStrategy.buySensorPref = buySensorPref;
        newStrategy.buyShieldPref = buyShieldPref;

        newStrategy.achievementsPref = achievementsPref;
        newStrategy.achievementsProbedVerticesPref = achievementsProbedVerticesPref;
        newStrategy.achievementsProbedVerticesGoal = achievementsProbedVerticesGoal;
        newStrategy.achievementsSurveyedEdgesPref = achievementsSurveyedEdgesPref;
        newStrategy.achievementsSurveyedEdgesGoal = achievementsSurveyedEdgesGoal;
        newStrategy.achievementsInspectedAgentsPref = achievementsInspectedAgentsPref;
        newStrategy.achievementsInspectedAgentsGoal = achievementsInspectedAgentsGoal;
        newStrategy.achievementsSuccessfulAttacksPref = achievementsSuccessfulAttacksPref;
        newStrategy.achievementsSuccessfulAttacksGoal = achievementsSuccessfulAttacksGoal;
        newStrategy.achievementsSuccessfulParriesPref = achievementsSuccessfulParriesPref;
        newStrategy.achievementsSuccessfulParriesGoal = achievementsSuccessfulParriesGoal;
        newStrategy.achievementsAreaValuePref = achievementsAreaValuePref;
        newStrategy.achievementsAreaValueGoal = achievementsAreaValueGoal;

        newStrategy.maxEnergyLimit = maxEnergyLimit;
        newStrategy.maxHealthLimit = maxHealthLimit;
        newStrategy.visRangeLimit = visRangeLimit;
        newStrategy.strengthLimit = strengthLimit;
        return newStrategy;
    }

    /**
     * randomly chooses an index for the focuses in this strategy.
     * @return an index for the focuses in this strategy.
     */
    public int chooseFocus () {
        double[] focusPrefs = new double[allFocuses.length];
        focusPrefs[0] = offensiveDestroyZonesPref;
        focusPrefs[1] = offensiveDestroyAgentsPref;
        focusPrefs[2] = offensiveDrawbackPref;

        focusPrefs[3] = defensiveParryPref;
        focusPrefs[4] = defensiveRunAwayPref;
        focusPrefs[5] = defensiveRepairPref;

        focusPrefs[6] = zoneExpandPref;
        focusPrefs[7] = zoneDrawbackPref;
        focusPrefs[8] = zoneStabilityPref;
        focusPrefs[9] = zoneMainZonePref;

        focusPrefs[10] = buyBatteryPref;
        focusPrefs[11] = buySabotageDevicePref;
        focusPrefs[12] = buySensorPref;
        focusPrefs[13] = buyShieldPref;

        focusPrefs[14] = achievementsProbedVerticesPref;
        focusPrefs[15] = achievementsSurveyedEdgesPref;
        focusPrefs[16] = achievementsInspectedAgentsPref;
        focusPrefs[17] = achievementsSuccessfulAttacksPref;
        focusPrefs[18] = achievementsSuccessfulParriesPref;
        focusPrefs[19] = achievementsAreaValuePref;

        // choose a strategy

        double[] highPrefs = new double[allFocuses.length];
        double[] strategyPrefs = new double[5];
        boolean higherThanOne = false;
        strategyPrefs[0] = offensivePref;
        strategyPrefs[1] = defensivePref;
        strategyPrefs[2] = zonePref;
        strategyPrefs[3] = buyPref;
        strategyPrefs[4] = achievementsPref;
        for ( int i = 0; i < strategyPrefs.length; i++ ) {
            if ( strategyPrefs[i] == 1 ) {
                for ( int j = 0; j < strategyPrefs.length; j++ )
                    if ( j != i )
                        strategyPrefs[j] = 0;
                break;
            }
            if ( strategyPrefs[i] > 1 ) {
                higherThanOne = true;
                highPrefs[i] = strategyPrefs[i] - 1;
            }
        }
        if ( higherThanOne )
            for ( int i = 0; i < strategyPrefs.length; i++ )
                strategyPrefs[i] = highPrefs[i];

        // normalize
        double sum = 0;
        for ( double p : strategyPrefs )
            sum += p;
        if ( sum != 1 )
            for ( int i = 0; i < strategyPrefs.length; i++ )
                strategyPrefs[i] /= sum;

        double higher = 0;
        int chosen = 0;
        double rand = Math.random();
        for ( int i = 0; i < strategyPrefs.length; i++ ) {
            higher += strategyPrefs[i];
            if ( higher >= rand ) {
                chosen = i;
                break;
            }
        }

        // choose a focus

        int left = 0, right = 2;
        if ( chosen == 1 )
            left = 3;
        if ( chosen == 2 )
            left = 6;
        if ( chosen == 3 )
            left = 10;
        if ( chosen == 4 )
            left = 14;

        if ( left > 2 )
            right = 5;
        if ( left > 5 )
            right = 9;
        if ( left > 9 )
            right = 13;
        if ( left > 13 )
            right = 19;

        higherThanOne = false;
        for ( int i = 0; i < highPrefs.length; i++ )
            highPrefs[i] = 0;
        for ( int i = 0; i < left; i++ )
            focusPrefs[i] = 0;
        if ( right != focusPrefs.length - 1 )
            for ( int i = right + 1; i < focusPrefs.length; i++ )
                focusPrefs[i] = 0;
        for ( int j = left; j <= right; j++ ) {
            if ( focusPrefs[j] > 1 ) {
                higherThanOne = true;
                highPrefs[j] = focusPrefs[j] - 1;
            }
            if ( focusPrefs[j] == 1 ) {
                for ( int k = 0; k < right; k++ )
                    if ( k != j )
                        focusPrefs[k] = 0;
                break;
            }
        }
        if ( higherThanOne )
            focusPrefs = highPrefs;

        // normalize

        sum = 0;
        for ( double p : focusPrefs )
            sum += p;
        if ( sum != 1 )
            for ( int i = 0; i < focusPrefs.length; i++ )
                focusPrefs[i] /= sum;

        higher = 0;
        chosen = 0;
        rand = Math.random();
        for ( int i = 0; i < focusPrefs.length; i++ ) {
            higher += focusPrefs[i];
            if ( higher >= rand ) {
                chosen = i;
                break;
            }
        }

        return chosen;
    }

}
