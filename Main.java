import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JPanel implements ActionListener {

    private static final Dimension defaultWindowSize = new Dimension(800, 600); //poczatkowa wielkosc okna
    private static final Dimension minimumWindowSize = new Dimension(500, 400); //minimalna wielkosc okna

    private boolean playGame;       //zmienna sprawdzajaca czy gra jestwlczona

    private MainPanel displayPanel; //obiekt klasy opowiadajcej za rysowanie komorek
    private Thread game;

    private JButton startStopButton;
    private JButton clearBoardButton;
    private JButton autoFillButton;
    private JButton optionsButton;

    public static void main(String args[]) {
        JFrame game = new JFrame("Gra w zycie");
        JPanel panel = new Main();

        game.setContentPane(panel);
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.pack();
        game.setSize(defaultWindowSize);
        game.setMinimumSize(minimumWindowSize);
        game.setVisible(true);
    }
    Main() { //tworzenie glownego okna
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout(3,3));
        setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
        displayPanel = new MainPanel(defaultWindowSize);

        JPanel bottomMenu = new JPanel();
        add(bottomMenu, BorderLayout.NORTH);

        startStopButton = new JButton("Start");
        clearBoardButton = new JButton("Wyczysc");
        autoFillButton = new JButton("Autowypełnienie");
        optionsButton = new JButton("Opcje gry");

        bottomMenu.add(startStopButton);
        bottomMenu.add(clearBoardButton);
        bottomMenu.add(autoFillButton);
        bottomMenu.add(optionsButton);

        startStopButton.addActionListener(this);
        clearBoardButton.addActionListener(this);
        autoFillButton.addActionListener(this);
        optionsButton.addActionListener(this);
        displayPanel.setBackground(Color.DARK_GRAY);
        add(displayPanel);

    }
    private void isGamePlay(boolean playGame) { //sterowanie rozgrywka, uruchomienie gry, wylaczenie wlaczenie przyciskow
        this.playGame = playGame;

        if(!playGame) {
            clearBoardButton.setEnabled(true);
            autoFillButton.setEnabled(true);
            optionsButton.setEnabled(true);
            game.interrupt();
        } else {
            clearBoardButton.setEnabled(false);
            autoFillButton.setEnabled(false);
            optionsButton.setEnabled(false);
            game = new Thread(displayPanel);
            game.start();
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) { //metoda oblsugujaca zdarzenia menu
        if(e.getSource() == startStopButton) {
            if(!playGame) {
                isGamePlay(true);
                startStopButton.setText("Stop");
            } else {
                startStopButton.setText("Start");
                isGamePlay(false);
            }
        }
        else if(e.getSource() == clearBoardButton) { displayPanel.clearBoard(); }
        else if(e.getSource() == autoFillButton) {
            final JFrame autoFillFrame = new JFrame();
            autoFillFrame.setTitle("Autowypelnienie");
            autoFillFrame.setSize(500, 150);

            JPanel fillSetPanel = new JPanel();
            fillSetPanel.add(new JLabel("Szansa na powstanie zywej komorki(autowypelnienie): "));
            JTextField fillSetPercent = new JTextField(Double.toString(displayPanel.getFillSet()), 4);
            fillSetPanel.add(fillSetPercent);

            JPanel buttonPanel = new JPanel();
            JButton okOptionsButton = new JButton("Wypelnij");
            buttonPanel.add(okOptionsButton);

            okOptionsButton.addActionListener(e12 -> {
                if(!fillSetPercent.getText().isEmpty()) {
                    displayPanel.setAutoFill(Double.parseDouble(fillSetPercent.getText()));
                    displayPanel.generateCells();
                }
                autoFillFrame.dispose();
            });

            autoFillFrame.add(fillSetPanel,BorderLayout.NORTH);
            autoFillFrame.add(buttonPanel, BorderLayout.SOUTH);
            autoFillFrame.setVisible(true);
        }
        else if(e.getSource() == optionsButton) {
            final JFrame gameOptionsFrame = new JFrame();
            gameOptionsFrame.setTitle("Zmien opcje");
            gameOptionsFrame.setSize(500, 200);

            JPanel delayMontionsPanel = new JPanel();
            delayMontionsPanel.add(new JLabel("Opoznienie:"));
            JTextField delayMontionsType = new JTextField(Integer.toString(displayPanel.getDelayMontion()), 3);
            delayMontionsPanel.add(delayMontionsType);

            JPanel rulesGamePanel = new JPanel();
            rulesGamePanel.add(new JLabel("Zasady gry: "));
            JTextField aliveCellsSurvive1Set = new JTextField(Integer.toString(displayPanel.getAliveCellsSurvive1()), 2);
            JTextField aliveCellsSurvive2Set = new JTextField(Integer.toString(displayPanel.getAliveCellsSurvive2()), 2);
            JTextField deadCellsAliveSet = new JTextField(Integer.toString(displayPanel.getDeadCellsAlive()), 2);
            rulesGamePanel.add(aliveCellsSurvive1Set);
            rulesGamePanel.add(aliveCellsSurvive2Set);
            rulesGamePanel.add(new JLabel("/"));
            rulesGamePanel.add(deadCellsAliveSet);


            JPanel buttonPanel = new JPanel();
            JButton okOptionsButton = new JButton("Zatwierdz");
            buttonPanel.add(okOptionsButton);

            okOptionsButton.addActionListener(e12 -> {
                if(!delayMontionsType.getText().isEmpty()) {
                    displayPanel.setDelayMontion(Integer.parseInt(delayMontionsType.getText()));
                    displayPanel.setRules(Integer.parseInt(deadCellsAliveSet.getText()), Integer.parseInt(aliveCellsSurvive1Set.getText()), Integer.parseInt(aliveCellsSurvive2Set.getText()));
                }
                gameOptionsFrame.dispose();
            });

            gameOptionsFrame.add(delayMontionsPanel, BorderLayout.NORTH);
            gameOptionsFrame.add(rulesGamePanel, BorderLayout.CENTER);
            gameOptionsFrame.add(buttonPanel, BorderLayout.PAGE_END);
            gameOptionsFrame.setVisible(true);
        }

    }
}
