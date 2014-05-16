package mas.agentsHempelsSofa.tools;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import mas.agentsHempelsSofa.StrategyBasedAgent;
import mas.agentsHempelsSofa.data.AgentToken;
import mas.agentsHempelsSofa.data.Environment;
import mas.agentsHempelsSofa.data.graph.Graph;
import mas.agentsHempelsSofa.data.graph.Vertex;
import mas.agentsHempelsSofa.data.zone.Zone;
import mas.agentsHempelsSofa.data.zone.ZoneManager;

@SuppressWarnings("serial")
public class GraphWindow extends JFrame {

    public static GraphWindow graphWindow;
    public static LinkedList<StrategyBasedAgent> choosableAgents;

    private GraphPanel gPanel;
    private DetailPanel dPanel;
    private AgentChooserPanel acPanel;
    private VertexPanel vPanel;
    private InteractionPanel iPanel;

    private final int ZONES_MODE = 0;
    private final int NEIGHBOR_MODE = 1;
    private final int EXPLORE_MODE = 2;

    private AgentToken followedAgent;
    private Graph graph;
    private Environment environment;
    private ZoneManager zoneManager;
    private Color[] colors;
    private Vertex chosenVertex;
    private boolean onlyMarked = false;
    private boolean paused = false;
    private int mode = EXPLORE_MODE;
    private JPanel choosingPanel;
    private JComboBox viewOfAgentBox;
    private JComboBox followingAgentBox;

    /**
     * Creates a new Graph Window.
     * @param g the graph
     * @param zm the zone manager
     * @param env the environment
     * @param name the agent name.
     */
    public GraphWindow ( Graph g, ZoneManager zm, Environment env ) {
        super("Graph Window");
        graphWindow = this;
        choosableAgents = new LinkedList<StrategyBasedAgent>();
        graph = g;
        zoneManager = zm;
        environment = env;
        this.setLayout(new BorderLayout());
        setSize(1024, 768);
        choosingPanel = new JPanel();
        viewOfAgentBox = new JComboBox();
        viewOfAgentBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed ( ActionEvent e ) {
                int index = viewOfAgentBox.getSelectedIndex();
                graph = choosableAgents.get(index).getGraph();
                zoneManager = choosableAgents.get(index).getZoneManager();
                repaintGraph();
            }
        });
        followingAgentBox = new JComboBox();
        viewOfAgentBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed ( ActionEvent e ) {
                try {
                    int index = followingAgentBox.getSelectedIndex();
                    followedAgent = graph.getTokens().get(index);
                    repaintGraph();
                }
                catch ( Exception ex ) {
                }
            }
        });
        viewOfAgentBox.setSize(300, viewOfAgentBox.getHeight());
        viewOfAgentBox.setBackground(Color.WHITE);
        choosingPanel.add(viewOfAgentBox);
        choosingPanel.add(followingAgentBox);
        choosingPanel.setBackground(Color.WHITE);
        gPanel = new GraphPanel();
        dPanel = new DetailPanel();
        acPanel = new AgentChooserPanel();
        vPanel = new VertexPanel();
        iPanel = new InteractionPanel();
        JPanel south = new JPanel();
        south.setLayout(new BorderLayout());
        south.add(vPanel, BorderLayout.NORTH);
        south.add(iPanel, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
        JPanel east = new JPanel();
        east.setLayout(new BorderLayout());
        east.add(acPanel, BorderLayout.NORTH);
        east.add(dPanel, BorderLayout.CENTER);
        add(choosingPanel, BorderLayout.NORTH);
        add(east, BorderLayout.EAST);
        add(gPanel, BorderLayout.CENTER);
        repaint();
        setVisible(true);
    }

    /**
     * repaints the graph.
     */
    public void repaintGraph () {
        if ( paused )
            return;
        acPanel.repaint();
        dPanel.repaint();
        gPanel.repaint();
        vPanel.repaint();
    }

    private void repaintAll () {
        acPanel.repaint();
        dPanel.repaint();
        gPanel.repaint();
        vPanel.repaint();
    }

    private class GraphPanel extends JPanel {

        private int[] xPos;
        private int[] yPos;

        public GraphPanel () {
            this.setBackground(Color.white);
            // A listener for the movement over vertices
            this.addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseMoved ( MouseEvent e ) {
                }

                @Override
                public void mouseDragged ( MouseEvent e ) {
                    if ( graph.getNumberOfEdges() == 0 )
                        return;
                    double mx = getMousePosition().getX();
                    double my = getMousePosition().getY();
                    for ( int i = 0; i < xPos.length; i++ )
                        if ( mx >= xPos[i] && mx <= xPos[i] + 20
                                && my >= yPos[i] - 10 && my <= yPos[i] ) {
                            chosenVertex = graph.getVertices().get(i);
                            followedAgent = null;
                            vPanel.repaint();
                            gPanel.repaint();
                            dPanel.repaint();
                            return;
                        }
                }
            });
            this.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved ( MouseWheelEvent e ) {
                    if ( e.getWheelRotation() > 0 )
                        mode = (mode + 1) % 3;
                    else
                        mode = (mode - 1) % 3;
                    repaintAll();
                    iPanel.updateToggleButtons();
                }
            });
            this.addMouseListener(new MouseListener() {
                @Override
                public void mouseReleased ( MouseEvent e ) {
                }

                @Override
                public void mousePressed ( MouseEvent e ) {
                }

                @Override
                public void mouseExited ( MouseEvent e ) {
                }

                @Override
                public void mouseEntered ( MouseEvent e ) {
                }

                @Override
                public void mouseClicked ( MouseEvent e ) {
                    if ( e.getButton() == MouseEvent.BUTTON2 ) {
                        onlyMarked = !onlyMarked;
                        repaintAll();
                        iPanel.updateToggleButtons();
                    }
                }
            });
        }

        /**
         * paints the graph
         * @param g the graphics from paint()
         */
        private void paintGraph ( Graphics g ) {
            if ( graph.getVertices().isEmpty()
                    || zoneManager.getTeams() == null )
                return;
            int x, y;
            // Graph output
            if ( followedAgent != null )
                chosenVertex = followedAgent.getPosition();
            if ( xPos == null || xPos.length != graph.getNumberOfVertices() ) {
                xPos = new int[graph.getNumberOfVertices()];
                yPos = new int[graph.getNumberOfVertices()];
            }
            Graphics2D g2d = (Graphics2D) g;
            LinkedList<String> teams = graph.getAllTeams();
            if ( colors == null ) {
                colors = new Color[teams.size()];
                colors[0] = new Color(0, 255, 0);
                if ( colors.length > 1 )
                    colors[1] = new Color(0, 0, 255);
                for ( int i = 2; i < colors.length; i++ )
                    colors[i] = new Color((int) Math.random() * 256,
                            (int) Math.random() * 256,
                            (int) Math.random() * 256);
            }
            y = 0;
            g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 20));
            for ( int i = 0; i < colors.length; i++ ) {
                g.setColor(colors[i]);
                g.drawString("Team " + teams.get(i), 20, y += 20);
            }
            int n = graph.getNumberOfVertices();
            double ankle = 2 * Math.PI / n;
            int h = getHeight() / 2 + 10;
            int w = getWidth() / 2;
            double hr = h - 50;
            double wr = w - 80;
            // draw vertices
            for ( int i = 0; i < n; i++ ) {
                Vertex v = graph.getVertices().get(i);
                Color c = Color.lightGray;
                if ( mode == ZONES_MODE ) {
                    Zone z = zoneManager.getZone(v);
                    if ( !(z == null) )
                        for ( int j = 0; j < colors.length; j++ )
                            if ( z.getTeam().equals(teams.get(j)) )
                                c = colors[j];
                    g.setColor(c);
                    // draw bold, if on frontier
                    if ( z != null && z.getCriticalFrontier() != null
                            && z.isOnCriticalFrontier(v) )
                        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
                    else
                        g.setFont(new Font(Font.DIALOG_INPUT, Font.PLAIN, 12));
                }
                else {
                    if ( v.isProbed() ) {
                        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
                        g.setColor(Color.black);
                    }
                    else {
                        g.setFont(new Font(Font.DIALOG_INPUT, Font.PLAIN, 12));
                        g.setColor(Color.lightGray);
                    }
                }
                xPos[i] = (int) (w + wr * Math.cos(ankle * i));
                yPos[i] = (int) (h + hr * Math.sin(ankle * i));
                if ( v == chosenVertex )
                    g.setColor(Color.magenta);
                g.drawString("" + graph.getVertices().get(i).getNumber(),
                        xPos[i], yPos[i]);
            }
            // scale down a bit
            h -= 5;
            w += 10;
            hr *= 0.95;
            wr *= 0.95;
            // draw edges
            LinkedList<Vertex> vertices = graph.getVertices();
            g2d.setStroke(new BasicStroke(1));
            g.setColor(Color.lightGray);
            if ( !onlyMarked ) {
                for ( Vertex v : vertices ) {
                    double x1 = w + wr * Math.cos(ankle * vertices.indexOf(v));
                    double y1 = h + hr * Math.sin(ankle * vertices.indexOf(v));
                    for ( Vertex neigh : v.getAdjacentVertices() ) {
                        double x2 = w + wr
                                * Math.cos(ankle * vertices.indexOf(neigh));
                        double y2 = h + hr
                                * Math.sin(ankle * vertices.indexOf(neigh));
                        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
                    }
                }
            }
            g2d.setStroke(new BasicStroke(2));
            switch ( mode ) {
            case ZONES_MODE:
                for ( Vertex v : vertices ) {
                    Zone z = zoneManager.getZone(v);
                    double x1 = w + wr * Math.cos(ankle * vertices.indexOf(v));
                    double y1 = h + hr * Math.sin(ankle * vertices.indexOf(v));
                    for ( Vertex neigh : v.getAdjacentVertices() ) {
                        double x2 = w + wr
                                * Math.cos(ankle * vertices.indexOf(neigh));
                        double y2 = h + hr
                                * Math.sin(ankle * vertices.indexOf(neigh));
                        if ( z != null && z.contains(neigh) ) {
                            for ( int k = 0; k < colors.length; k++ )
                                if ( z.getTeam().equals(teams.get(k)) )
                                    g.setColor(colors[k]);
                            if ( z == zoneManager.getZone(chosenVertex) )
                                g.setColor(Color.magenta);
                            g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
                        }

                    }
                }
                break;
            case EXPLORE_MODE:
                g2d.setStroke(new BasicStroke(1));
                for ( Vertex v : vertices ) {
                    double x1 = w + wr * Math.cos(ankle * vertices.indexOf(v));
                    double y1 = h + hr * Math.sin(ankle * vertices.indexOf(v));
                    for ( Vertex neigh : v.getAdjacentVertices() ) {
                        double x2 = w + wr
                                * Math.cos(ankle * vertices.indexOf(neigh));
                        double y2 = h + hr
                                * Math.sin(ankle * vertices.indexOf(neigh));
                        if ( graph.getEdge(v, neigh).isSurveyed() ) {
                            g.setColor(Color.black);
                            g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
                        }
                    }
                }
                break;
            case NEIGHBOR_MODE:
                if ( chosenVertex != null ) {
                    double x1 = w + wr
                            * Math.cos(ankle * vertices.indexOf(chosenVertex));
                    double y1 = h + hr
                            * Math.sin(ankle * vertices.indexOf(chosenVertex));
                    g.setColor(Color.magenta);
                    for ( Vertex neigh : chosenVertex.getAdjacentVertices() ) {
                        double x2 = w + wr
                                * Math.cos(ankle * vertices.indexOf(neigh));
                        double y2 = h + hr
                                * Math.sin(ankle * vertices.indexOf(neigh));
                        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
                    }
                }
            }
            g.setColor(Color.black);
            g.setFont(new Font(Font.SERIF, Font.ITALIC, 14));
            // Mode output
            String modeName = "EXPLORE MODE";
            if ( mode == ZONES_MODE )
                modeName = "ZONES MODE";
            if ( mode == NEIGHBOR_MODE )
                modeName = "NEIGHBOR MODE";
            g.drawString(modeName, 300, 20);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            // Environment output
            String[] details = new String[4];
            details[0] = "step:      " + environment.getStep();
            details[1] = "score:     " + environment.getScore();
            details[2] = "money:     " + environment.getMoney();
            details[3] = "zones val: " + environment.getZonesValue();
            x = (int) getWidth() - 120;
            y = 20;
            for ( String detail : details ) {
                g.drawString(detail, x, y);
                y += 20;
            }
            // Achievements output
            details = new String[7];
            details[0] = "ACHIEVEMENTS";
            details[1] = "probed vertices:  "
                    + graph.getNumberOfProbedVertices();
            details[2] = "surveyed edges:   "
                    + graph.getNumberOfSurveyedEdges();
            details[3] = "inspected agents: "
                    + environment.getNumberOfInspectedAgents();
            details[4] = "succ. attacks:    "
                    + environment.getNumberOfSuccessfulAttacks();
            details[5] = "succ. parries:    "
                    + environment.getNumberOfSuccessfulParries();
            details[6] = "area value:       " + environment.getMaxAreaValue();
            x = 10;
            y = getHeight() - 120;
            for ( String detail : details ) {
                g.drawString(detail, x, y);
                y += 20;
            }
        }

        @Override
        public void paint ( Graphics g ) {
            paintComponent(g);
            paintGraph(g);
        }
    }

    private class VertexPanel extends JPanel {

        public VertexPanel () {
            this.setPreferredSize(new Dimension(getWidth(), 30));
            this.setBackground(Color.white);
        }

        public void paint ( Graphics g ) {
            paintComponent(g);
            if ( chosenVertex == null )
                return;
            g.setColor(Color.black);
            String s = chosenVertex.toString() + ": ";
            for ( int i = 0; i < chosenVertex.getTokens().size(); i++ ) {
                if ( i != 0 )
                    s += ", ";
                s += chosenVertex.getTokens().get(i).getName();
            }
            g.drawString(s, 20, 20);
            if ( mode == ZONES_MODE ) {
                s = "no zone";
                Zone z = zoneManager.getZone(chosenVertex);
                if ( z != null )
                    s = z.toString();
            }
            else {
                s = "neighbors: ";
                s += chosenVertex.getAdjacentVertices().toString();
            }
            g.drawString(s, 300, 20);
        }
    }

    /**
     * A panel for interaction with the graph panel.
     * @author Hemples-Sofa
     */
    private class InteractionPanel extends JPanel {

        public JButton toggleOnlyMarked;
        public JButton toggleNeighborsZones;
        private JButton pause;

        public InteractionPanel () {
            this.setSize(getWidth(), 100);
            this.setBackground(Color.white);
            this.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            JButton toggleColors = new JButton("toggle colors");
            toggleColors.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed ( ActionEvent e ) {
                    if ( colors == null )
                        return;
                    Color tmp = colors[0];
                    for ( int i = 0; i < colors.length - 1; i++ )
                        colors[i] = colors[i + 1];
                    colors[colors.length - 1] = tmp;
                    gPanel.repaint();
                }
            });
            c.gridx = 0;
            c.gridy = 0;
            add(toggleColors, c);

            toggleOnlyMarked = new JButton("hide edges");
            toggleOnlyMarked.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed ( ActionEvent e ) {
                    onlyMarked = !onlyMarked;
                    updateToggleButtons();
                    gPanel.repaint();
                }
            });
            c.gridx = 1;
            add(toggleOnlyMarked, c);

            c.gridx = 2;
            toggleNeighborsZones = new JButton("zones mode");
            toggleNeighborsZones.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed ( ActionEvent e ) {
                    mode = (mode + 1) % 3;
                    updateToggleButtons();
                    gPanel.repaint();
                }
            });
            add(toggleNeighborsZones, c);

            c.gridx = 3;
            pause = new JButton("pause");
            pause.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed ( ActionEvent e ) {
                    paused = !paused;
                    updateToggleButtons();
                }
            });
            add(pause, c);

            c.gridx = 4;
            JButton exit = new JButton("stop agents");
            exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed ( ActionEvent e ) {
                    System.exit(0);
                }
            });
            add(exit, c);
        }

        public void updateToggleButtons () {
            if ( mode == ZONES_MODE )
                toggleNeighborsZones.setText("neighbor mode");
            else if ( mode == NEIGHBOR_MODE )
                toggleNeighborsZones.setText("explore mode");
            else
                toggleNeighborsZones.setText("zones mode");
            if ( onlyMarked )
                toggleOnlyMarked.setText("show edges");
            else
                toggleOnlyMarked.setText("hide edges");
            if ( paused )
                pause.setText("resume");
            else
                pause.setText("pause");
        }
    }

    private class DetailPanel extends JPanel {

        public DetailPanel () {
            this.setBackground(Color.white);
            this.setPreferredSize(new Dimension(300, 350));
        }

        @Override
        public void paint ( Graphics g ) {
            paintComponent(g);
            if ( graph.getTotalNumberOfEdges() == 0 )
                return;
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            String[] attributes;
            if ( followedAgent == null ) {
                attributes = new String[11];
                attributes[0] = "GRAPH DETAIL";
                attributes[1] = "Total vertices:     "
                        + graph.getTotalNumberOfVertices();
                attributes[2] = "explored vertices:  "
                        + graph.getNumberOfExploredVertices();
                attributes[3] = "                    ("
                        + Math.round(graph.getRatioOfExploredVertices() * 1000.)
                        / 10. + "%)";
                attributes[4] = "probed vertices:    "
                        + graph.getNumberOfProbedVertices();
                attributes[5] = "                    ("
                        + Math.round(graph.getRatioOfProbedVertices() * 1000.)
                        / 10. + "%)";
                attributes[6] = "Total edges:        "
                        + graph.getTotalNumberOfEdges();
                attributes[7] = "explored edges:     "
                        + graph.getNumberOfExploredEdges();
                attributes[8] = "                    ("
                        + Math.round(graph.getRatioOfExploredEdges() * 1000.)
                        / 10. + "%)";
                attributes[9] = "surveyed edges:     "
                        + graph.getNumberOfSurveyedEdges();
                attributes[10] = "                    ("
                        + Math.round(graph.getRatioOfSurveyedEdges() * 1000.)
                        / 10. + "%)";
            }
            else {
                chosenVertex = followedAgent.getPosition();
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                attributes = new String[18];
                attributes[0] = "AGENT DETAIL";
                attributes[1] = "Name:              " + followedAgent.getName();
                attributes[2] = "Team:              " + followedAgent.getTeam();
                attributes[3] = "Position:          "
                        + followedAgent.getPosition();
                attributes[4] = "Role:              " + followedAgent.getRole();
                attributes[5] = "State:             "
                        + followedAgent.getState();
                attributes[6] = "Energy:            "
                        + followedAgent.getEnergy();
                attributes[7] = "MaxEnergy:         "
                        + followedAgent.getMaxEnergy();
                attributes[8] = "MaxEnergyDisabled: "
                        + followedAgent.getMaxEnergyDisabled();
                attributes[9] = "Health:            "
                        + followedAgent.getHealth();
                attributes[10] = "MaxHealth:         "
                        + followedAgent.getMaxHealth();
                attributes[11] = "Strength:          "
                        + followedAgent.getStrength();
                attributes[12] = "VisibilityRange:   "
                        + followedAgent.getVisibilityRange();
                attributes[13] = "TargetVertex:      "
                        + followedAgent.getTargetVertex();
                attributes[14] = "TargetAgent:       "
                        + followedAgent.getTargetAgent();
                attributes[15] = "LastUpdate:        "
                        + followedAgent.getLastUpdate();
                attributes[16] = "LastInspection:    "
                        + followedAgent.getLastInspection();
                attributes[17] = "Strategy-Type:     "
                        + followedAgent.getStrategyTypeString();
            }
            int y = 0;
            for ( String attribute : attributes ) {
                y += 20;
                g.drawString(attribute, 0, y);
            }
        }

    }

    /**
     * A panel to choose agents to keep track of.
     * @author Hempels-Sofa
     */
    private class AgentChooserPanel extends JPanel {

        public AgentChooserPanel () {
            this.setBackground(Color.white);
            this.setLayout(new GridBagLayout());
            this.setPreferredSize(new Dimension(200, 300));
        }

        public void paint ( Graphics g ) {
            paintComponent(g);
            if ( graph.getNumberOfEdges() == 0 )
                return;
            this.removeAll();
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            LinkedList<String> teams = graph.getAllTeams();
            if ( teams == null )
                return;
            int[] count = new int[teams.size()];
            for ( int i = 0; i < graph.getTokens().size(); i++ ) {
                AgentToken chosen = graph.getTokens().get(i);
                for ( int j = 0; j < teams.size(); j++ )
                    if ( chosen.getTeam().equals(teams.get(j)) ) {
                        c.gridx = j;
                        c.gridy = count[j];
                        JButton b = new JButton(chosen.getName());
                        b.addActionListener(new Listener(chosen));
                        add(b, c);
                        count[j]++;
                        break;
                    }
            }
            validate();
        }

        private class Listener implements ActionListener {

            private AgentToken token;

            public Listener ( AgentToken t ) {
                token = t;
            }

            @Override
            public void actionPerformed ( ActionEvent e ) {
                followedAgent = token;
                chosenVertex = token.getPosition();
                gPanel.repaint();
                vPanel.repaint();
                dPanel.repaint();
            }
        }
    }

    public static void registerAgent ( StrategyBasedAgent a ) {
        choosableAgents.add(a);
        graphWindow.viewOfAgentBox.addItem(a.getName());
    }
}
