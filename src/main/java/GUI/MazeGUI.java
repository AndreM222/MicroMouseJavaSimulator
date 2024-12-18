package GUI;

import Maze.Maze;
import Maze.MazeNode;
import Mouse.Mouse;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MazeGUI extends Component {
    public static final double MAZE_DEFAULT_PROPORTION = 0.50;
    public static final File DATAFILE = new File("../MazeFiles/16_maze01.data.dat");

    private Maze ref_maze;
    private Maze mouse_maze;
    private Mouse mouse;

    private Timer animationCLK;
    private Timer mouseRunTimer;
    private int elapsedTime;
    private JLabel timerLabel;
    private JPanel northPanel;
    private JPanel southPanel;
    private RenderPanel renderPanel;
    private JPanel northButtonPanel;
    private JPanel southButtonPanel;
    private JPanel algoPanel1;
    private JPanel algoPanel2;

    private JButton animateButton;
    private JButton saveMazeButton;
    private JButton newMazeButton;
    private JButton nextButton;
    private JButton selectAlgoButton;
    private JButton loadMazeButton;

    private JComboBox<String> algoComboBox;
    private JComboBox<String> loadMazeComboBox;

    private boolean runDijkstra;
    private boolean runDFS;
    private boolean runAStar;
    private boolean outputStats = true;
    private final MazeController controller;
    private MazeNode endNode;

    /**
     * Constructor: Creates and sets up MazeGUI
     *
     * @param dimension       number of unit cells per side of square maze.
     * @param non_tree_edges  number of no tree edges in maze graph (adds multiple path solutions).
     * @param dijkstra        color the dijkstra path on the reference maze in DIJKSTRA_PATH_COLOR.
     * @param dfs             color the dfs path on the reference maze in DFS_PATH_COLOR.
     * @param astar           color the astar path on the reference maze in ASTAR_PATH_COLOR.
     */
    public MazeGUI(int dimension, int non_tree_edges, boolean dijkstra, boolean dfs, boolean astar) {
        if (dimension < 1) dimension = 1;
        ref_maze = new Maze(dimension);
        mouse_maze = new Maze(dimension);
        try {
            if(ref_maze.getMazeSerializer().loadMaze(DATAFILE) == false) {
                /* load datafile - otherwise create new random maze if that didn't work */
                ref_maze.getMazeGenerator().createRandomMaze( 3);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mouse = new Mouse(dimension - 1, 0, ref_maze, mouse_maze);
        runDijkstra = dijkstra;
        runDFS = dfs;
        runAStar = astar;

        controller = new MazeController(this);
        elapsedTime = 0;
        setupTimers();
        begin();
    }

    private void setupTimers() {
        // Mouse run timer updates elapsed time every second
        mouseRunTimer = new Timer(1000, e -> {
            elapsedTime++;
            timerLabel.setText("Time: " + elapsedTime + " sec");
        });
        // Animation timer for other GUI animations
        animationCLK = new Timer(MazeController.ANIMATION_DELAY, controller);
    }

    private void begin() {
        JFrame main_frame = new JFrame("MicroMouse Simulator");
        main_frame.setSize(800, 800);
        main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main_frame.setBackground(Color.BLACK);
        main_frame.setResizable(true);

        /* main sections of layout, north, south, center */
        northPanel = new JPanel();
        southPanel = new JPanel();
        renderPanel = new RenderPanel(this);
        /* sub-panels for cosmetic needs */
        northButtonPanel = new JPanel();
        southButtonPanel = new JPanel();

        /* set layout for button panels */
        northButtonPanel.setLayout(new BoxLayout(northButtonPanel, BoxLayout.LINE_AXIS));
        southButtonPanel.setLayout(new BoxLayout(southButtonPanel, BoxLayout.LINE_AXIS));

        /* set names of new buttons */
        animateButton = new JButton("Animate");
        selectAlgoButton = new JButton("Select Algo");
        newMazeButton = new JButton("New Maze");
        nextButton = new JButton("Next");
        saveMazeButton = new JButton("Save");
        loadMazeButton = new JButton("Load");

        /* JComboBox for algorithm selection */
        String[] algorithms = { "Select an Algo", "DFS", "Dijkstra", "A*" };
        algoComboBox = new JComboBox<>(algorithms);
        algoComboBox.setSelectedIndex(0);
        algoComboBox.setVisible(false);
        algoComboBox.addActionListener(controller);

        // Timer initialization (initially just "Time:")
        timerLabel = new JLabel("Time: 0 sec");  // Initially just "Time:"
        timerLabel.setForeground(Color.BLACK);  // Set text color to black

        renderPanel.add(timerLabel);


        /* JComboBox for different saved maze selection */
        String[] loadMazes = { "Select a Maze", "Maze 1", "Maze 2", "Maze 3" };
        loadMazeComboBox = new JComboBox<>(loadMazes);
        loadMazeComboBox.setSelectedIndex(0);
        loadMazeComboBox.setVisible(false);
//        loadMazeComboBox.addActionListener(e -> handleLoadMazeSelection());

        /* Panel to hold the JComboBox for algorithm */
        algoPanel1 = new JPanel();
        algoPanel1.add(algoComboBox);

        /* Panel to hold the JComboBox for load maze */
        algoPanel2 = new JPanel();
        algoPanel2.add(loadMazeComboBox);
        JPanel loadMazePanel = new JPanel();
        loadMazePanel.add(loadMazeComboBox);

        /* Activates button/comboBox to register state change */
        saveMazeButton.addActionListener(controller);
        animateButton.addActionListener(controller);
        newMazeButton.addActionListener(controller);
        nextButton.addActionListener(controller);
        selectAlgoButton.addActionListener(controller);
        loadMazeButton.addActionListener(controller);

        /* north button panel buttons */
        northButtonPanel.add(animateButton);
        northButtonPanel.add(Box.createHorizontalGlue());
        northButtonPanel.add(selectAlgoButton);
        northButtonPanel.add(Box.createHorizontalGlue());
        northButtonPanel.add(newMazeButton);

        /* south button panel buttons */
        southButtonPanel.add(nextButton);
        southButtonPanel.add(Box.createHorizontalGlue());
        southButtonPanel.add(loadMazeButton);
        southButtonPanel.add(Box.createHorizontalGlue());
        southButtonPanel.add(saveMazeButton);

        /* background color of button panels */
        northButtonPanel.setBackground(Color.BLACK);
        southButtonPanel.setBackground(Color.BLACK);

        /* set up north panel */
        northPanel.setLayout(new GridLayout(0, 1));
        northPanel.add(northButtonPanel);
        northPanel.add(algoPanel1);

        /* set up south panel  */
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(southButtonPanel);
        southPanel.add(loadMazePanel);

        /* add panels with their buttons on the final window */
        Container contentPane = main_frame.getContentPane();
        contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(southPanel, BorderLayout.SOUTH);
        contentPane.add(renderPanel, BorderLayout.CENTER);
        contentPane.validate();

        main_frame.setVisible(true);
        animationCLK = new Timer(MazeController.ANIMATION_DELAY, controller);
    }


//    private void toggleLoadMazeDropDown() {
//        loadMazeComboBox.setVisible((!loadMazeComboBox.isVisible()));
//    }
//
//    public void handleLoadMazeSelection() {
//        File[] savedMazeFiles = controller.getSavedMazeFiles();
//        String selectedMaze = (String) loadMazeComboBox.getSelectedItem();
//
//        if ("Select a Maze".equals(selectedMaze)) {
//            System.out.println("No maze selected");
//            return;
//        }
//
//        if (selectedMaze != null) {
//            switch (selectedMaze) {
//                case "Maze 1":
//                    ref_maze.getMazeSerializer().loadMaze(savedMazeFiles[0]);
//                    System.out.println("Loading Maze 1");
//                    break;
//                case "Maze 2":
//                    ref_maze.getMazeSerializer().loadMaze(savedMazeFiles[1]);
//                    System.out.println("Loading Maze 2");
//                    break;
//                case "Maze 3":
//                    ref_maze.getMazeSerializer().loadMaze(savedMazeFiles[2]);
//                    System.out.println("Loading Maze 3");
//                    break;
//                default:
//                    System.out.println("Invalid selection");
//            }
//        }
//
//        loadMazeComboBox.setVisible(false);
//    }

    public Maze getRefMaze() {
        return ref_maze;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public JButton getAnimateButton() {
        return animateButton;
    }

    public JButton getLoadMazeButton() {
        return loadMazeButton;
    }

    public JButton getSaveMazeButton() {
        return saveMazeButton;
    }

    public JButton getLoadSaveMazeButton() {
        return loadMazeButton;
    }

    public JButton getNewMazeButton() {
        return newMazeButton;
    }

    public JButton getSelectAlgoButton() {
        return selectAlgoButton;
    }

    public JButton getNextButton() {
        return nextButton;
    }

    public void setRunDFS(boolean runDFS) {
        this.runDFS = runDFS;
    }

    public void setRunDijkstra(boolean runDijkstra) {
        this.runDijkstra = runDijkstra;
    }

    public void setRunAStar(boolean runAStar) {
        this.runAStar = runAStar;
    }

    public boolean isRunDFS() {
        return runDFS;
    }

    public boolean isRunDijkstra() {
        return runDijkstra;
    }

    public boolean isRunAStar() {
        return runAStar;
    }

    public boolean isOutputStats() {
        return outputStats;
    }

    public void setOutputStats(boolean outputStats) {
        this.outputStats = outputStats;
    }

    public RenderPanel getRenderPanel() {
        return renderPanel;
    }

    public MazeNode getEndNode() {
        return endNode;
    }

    public JComboBox<String> getAlgoComboBox() {
        return algoComboBox;
    }

    public JComboBox<String> getLoadMazeComboBox() {
        return loadMazeComboBox;
    }

    public Maze getMouseMaze() {
        return mouse_maze;
    }

    public void setRefMaze(Maze refMaze) {
        this.ref_maze = refMaze;
    }

    public void setMouseMaze(Maze mouseMaze) {
        this.mouse_maze = mouseMaze;
    }

    public void setEndNode(MazeNode endNode) {
        this.endNode = endNode;
    }

    public void setMouse(Mouse mouse) {
        this.mouse = mouse;
    }

    public void startMouseRunTimer() {
        elapsedTime = 0;
        timerLabel.setText("Time: 0 sec");
        mouseRunTimer.start();
    }

    public void stopMouseRunTimer() {
        mouseRunTimer.stop();
    }

    public void resetMouseRunTimer() {
        stopMouseRunTimer();
        elapsedTime = 0;
        timerLabel.setText("Time: 0 sec");
    }

    public Timer getAnimationCLK() {
        return animationCLK;
    }

    public void setElapsedTime(int seconds) {
        // This updates the timerLabel with the elapsed time in seconds
        timerLabel.setText("Time: " + seconds + " sec");
    }
}