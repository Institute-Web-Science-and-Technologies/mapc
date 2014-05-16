package mas.agentsHempelsSofa.util;

// TODO dateien nach role laden?
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mas.agentsHempelsSofa.data.Strategy;
import massim.javaagents.ParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StrategyInterpreter {
	
    /**
     * Parses a config-file.
     * @param filename the path to the config file
     * @throws ParseException is thrown when parsing fails
     */
    public Strategy parseConfig ( String filename ) throws ParseException {
    	
        Strategy strategy = new Strategy();

        File file = new File(filename);

        // parse the XML document
        Document doc = null;
        try {
            DocumentBuilderFactory documentbuilderfactory = DocumentBuilderFactory
                    .newInstance();
            doc = documentbuilderfactory.newDocumentBuilder().parse(file);
        }
        catch ( SAXException e ) {
            throw new ParseException(file.getPath(), "error parsing "
                    + e.getMessage());

        }
        catch ( IOException e ) {
            throw new ParseException(file.getPath(), "error parsing "
                    + e.getMessage());

        }
        catch ( ParserConfigurationException e ) {
            throw new ParseException(file.getPath(), "error parsing "
                    + e.getMessage());
        }

        // get the root
        Element root = doc.getDocumentElement();
        if ( root.getNodeName().equalsIgnoreCase("strategyConfig") == false )
            throw new ParseException(file.getPath(),
                    "root-element must be strategyConfig");

        // process root's children
        NodeList rootChildren = root.getChildNodes();
        for ( int a = 0; a < rootChildren.getLength(); a++ ) {

            Node rootChild = rootChildren.item(a);

            // ignore text and comment
            if ( rootChild.getNodeName().equals("#text")
                    || rootChild.getNodeName().equals("#comment") )
                continue;

            // parse the agents list
            if ( rootChild.getNodeName().equalsIgnoreCase("strategies") ) {

                NodeList rootChildChildren = rootChild.getChildNodes();
                for ( int b = 0; b < rootChildChildren.getLength(); b++ ) {

                    Node rootChildChild = rootChildChildren.item(b);

                    // ignore text and comment
                    if ( rootChildChild.getNodeName().equals("#text")
                            || rootChildChild.getNodeName().equals("#comment") )
                        continue;

                    if ( rootChildChild.getNodeName().equalsIgnoreCase(
                            "strategy") ) {

                        Element e1 = (Element) rootChildChild;

                        String strategyName = e1.getAttribute("name");
                        if ( strategyName == null || strategyName.equals("") )
                            throw new ParseException(file.getPath(),
                                    "missing name-attribute of strategy-tag");
                        String strategyPreference = e1
                                .getAttribute("preference");

                        double preference = 0;
                        if ( !strategyName.equals("buyLimits") ) {
                            if ( strategyPreference == null
                                    || strategyPreference.equals("") )
                                throw new ParseException(file.getPath(),
                                        "missing preference-attribute of strategy-tag");
                            try {
                                preference = Double
                                        .parseDouble(strategyPreference);
                            }
                            catch ( Exception ex ) {
                                throw new ParseException(file.getPath(),
                                        "preference-attribute must be of double value");
                            }
                        }

                        if ( strategyName.equals("offensive") )
                            strategy.offensivePref = preference;
                        if ( strategyName.equals("defensive") )
                            strategy.defensivePref = preference;
                        if ( strategyName.equals("zone") )
                            strategy.zonePref = preference;
                        if ( strategyName.equals("buy") )
                            strategy.buyPref = preference;
                        if ( strategyName.equals("achievements") )
                            strategy.achievementsPref = preference;

                        NodeList rootChildChildChildren = rootChildChild
                                .getChildNodes();
                        for ( int c = 0; c < rootChildChildChildren.getLength(); c++ ) {

                            Node rootChildChildChild = rootChildChildChildren
                                    .item(c);

                            // ignore text and comment
                            if ( rootChildChildChild.getNodeName().equals(
                                    "#text")
                                    || rootChildChildChild.getNodeName()
                                            .equals("#comment") )
                                continue;

                            if ( rootChildChildChild.getNodeName()
                                    .equalsIgnoreCase("focus") ) {

                                Element e2 = (Element) rootChildChildChild;

                                String name = e2.getAttribute("name");
                                if ( name == null || name.equals("") )
                                    throw new ParseException(file.getPath(),
                                            "missing name-attribute of strategy- or limit-tag");
                                String focusPreference = e2
                                        .getAttribute("preference");

                                String focusGoal = e2.getAttribute("goal");
                                String limitValue = e2.getAttribute("value");

                                int goal = 0;
                                int limit = 0;
                                try {
                                    preference = Double
                                            .parseDouble(focusPreference);
                                }
                                catch ( Exception ex ) {
                                    throw new ParseException(file.getPath(),
                                            "preference-attribute must be of double value");
                                }

                                if ( focusGoal != null && !focusGoal.equals("") )
                                    try {
                                        goal = Integer.parseInt(focusGoal);
                                    }
                                    catch ( Exception ex ) {
                                        throw new ParseException(
                                                file.getPath(),
                                                "goal-attribute must be of int value");
                                    }

                                if ( limitValue != null
                                        && !limitValue.equals("") )
                                    try {
                                        limit = Integer.parseInt(limitValue);
                                    }
                                    catch ( Exception ex ) {
                                        throw new ParseException(
                                                file.getPath(),
                                                "value-attribute must be of int value");
                                    }

                                if ( strategyName.equals("offensive") ) {
                                    if ( name.equals("destroyZones") )
                                        strategy.offensiveDestroyZonesPref = preference;
                                    if ( name.equals("destroyAgents") )
                                        strategy.offensiveDestroyAgentsPref = preference;
                                    if ( name.equals("drawback") )
                                        strategy.offensiveDrawbackPref = preference;
                                }
                                if ( strategyName.equals("defensive") ) {
                                    if ( name.equals("parry") )
                                        strategy.defensiveParryPref = preference;
                                    if ( name.equals("runAway") )
                                        strategy.defensiveRunAwayPref = preference;
                                    if ( name.equals("defensiveRepair") )
                                        strategy.defensiveRepairPref = preference;
                                }
                                if ( strategyName.equals("zone") ) {
                                    if ( name.equals("expand") )
                                        strategy.zoneExpandPref = preference;
                                    if ( name.equals("stability") )
                                        strategy.zoneStabilityPref = preference;
                                    if ( name.equals("drawback") )
                                        strategy.zoneDrawbackPref = preference;
                                    if ( name.equals("mainZone") )
                                        strategy.zoneMainZonePref = preference;
                                }
                                if ( strategyName.equals("buy") ) {
                                    if ( name.equals("battery") )
                                        strategy.buyBatteryPref = preference;
                                    if ( name.equals("sabotageDevice") )
                                        strategy.buySabotageDevicePref = preference;
                                    if ( name.equals("sensor") )
                                        strategy.buySensorPref = preference;
                                    if ( name.equals("shield") )
                                        strategy.buyShieldPref = preference;
                                }
                                if ( strategyName.equals("achievements") ) {
                                    if ( name.equals("probedVertices") ) {
                                        strategy.achievementsProbedVerticesPref = preference;
                                        strategy.achievementsProbedVerticesGoal = goal;
                                    }
                                    if ( name.equals("surveyedEdges") ) {
                                        strategy.achievementsSurveyedEdgesPref = preference;
                                        strategy.achievementsSurveyedEdgesGoal = goal;
                                    }
                                    if ( name.equals("inspectedAgents") ) {
                                        strategy.achievementsInspectedAgentsPref = preference;
                                        strategy.achievementsInspectedAgentsGoal = goal;
                                    }
                                    if ( name.equals("successfulAttacks") ) {
                                        strategy.achievementsSuccessfulAttacksPref = preference;
                                        strategy.achievementsSuccessfulAttacksGoal = goal;
                                    }
                                    if ( name.equals("successfulParries") ) {
                                        strategy.achievementsSuccessfulParriesPref = preference;
                                        strategy.achievementsSuccessfulParriesGoal = goal;
                                    }
                                    if ( name.equals("areaValue") ) {
                                        strategy.achievementsAreaValuePref = preference;
                                        strategy.achievementsAreaValueGoal = goal;
                                    }
                                }
                                if ( strategyName.equals("buyLimits") ) {
                                    if ( name.equals("maxEnergy") )
                                        strategy.maxEnergyLimit = limit;
                                    if ( name.equals("maxHealth") )
                                        strategy.maxHealthLimit = limit;
                                    if ( name.equals("visRange") )
                                        strategy.visRangeLimit = limit;
                                    if ( name.equals("sabotageDevice") )
                                        strategy.strengthLimit = limit;
                                }

                            }
                            if ( rootChildChildChild.getNodeName()
                                    .equalsIgnoreCase("limit") ) {

                                Element e2 = (Element) rootChildChildChild;

                                String limitName = e2.getAttribute("name");
                                if ( limitName == null || limitName.equals("") )
                                    throw new ParseException(file.getPath(),
                                            "missing name-attribute of limit-tag");
                                String limitValue = e2.getAttribute("value");
                                if ( limitValue == null
                                        || limitValue.equals("") )
                                    throw new ParseException(file.getPath(),
                                            "missing valze-attribute of limit-tag");

                                try {
                                    preference = Double.parseDouble(limitValue);
                                }
                                catch ( Exception ex ) {
                                    throw new ParseException(file.getPath(),
                                            "limit-attribute must be of double value");
                                }
                                if ( strategyName.equals("buyLimits") ) {
                                    if ( limitName.equals("maxEnergy") )
                                        strategy.maxEnergyLimit = preference;
                                    if ( limitName.equals("maxHealth") )
                                        strategy.maxHealthLimit = preference;
                                    if ( limitName.equals("visRange") )
                                        strategy.visRangeLimit = preference;
                                    if ( limitName.equals("sabotageDevice") )
                                        strategy.strengthLimit = preference;
                                }
                            }

                        }

                    }
                    else {
                        System.out.println("unrecognized xml-tag "
                                + rootChild.getNodeName());
                    }

                }

            }
            else {
                System.out.println("unrecognized xml-tag "
                        + rootChild.getNodeName());
            }

        }
        return strategy;

    }

}
