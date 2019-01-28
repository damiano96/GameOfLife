import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MainPanel extends JPanel implements Runnable {
    private static final Color pointColor = new Color(0,153,0);
    private static final int sizeOfGrid = 13; //rozmiar pojedynczej komorki

    private Set<Point> points = new HashSet<>(); //zbior punktow, zywych komorek
    private Dimension sizeOfGameBoard;          //obiekt panelu gdzie odbrywac sie bedzie rozgrywka

    /* poczatkowe wartosci */
    private int delayMontion = 50; //opoznienie -> szybkosc powstawania nowych komorek. Im mniej tym wolniej
    private int deadCellsAlive = 3;
    private int aliveCellsSurvive1 = 2;
    private int aliveCellsSurvive2 = 3;
    private double fillSet = 0.25; //procent wypelnienia 0<x<1

    public int getDelayMontion() { return delayMontion; }
    public int getDeadCellsAlive() { return deadCellsAlive; }
    public int getAliveCellsSurvive1() { return aliveCellsSurvive1; }
    public int getAliveCellsSurvive2() { return aliveCellsSurvive2; }
    public double getFillSet() { return fillSet; }

    public void setDelayMontion(int delayMontion) {
        this.delayMontion = delayMontion;
        repaint();
    }
    public void setRules(int deadCellsAlive, int aliveCellsSurvive1, int aliveCellsSurvive2) {
        this.deadCellsAlive = deadCellsAlive;
        this.aliveCellsSurvive1 = aliveCellsSurvive1;
        this.aliveCellsSurvive2 = aliveCellsSurvive2;
    }
    public void setAutoFill(double fillSet) { this.fillSet = fillSet; }

    public MainPanel(Dimension firstSize) {
        sizeOfGameBoard =  firstSize;

        this.addMouseListener(new MouseAdapter() { //rysowanie komorek
            @Override
            public void mouseReleased(MouseEvent e) { drawPoint(e); }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() { //rysowanie komorek
            @Override
            public void mouseDragged(MouseEvent e) { drawPoint(e); }
        });
        this.addComponentListener(new ComponentAdapter() {  //roszerzanie sie planszy
            @Override
            public void componentResized(ComponentEvent e) {
                sizeOfGameBoard = new Dimension(getWidth()/sizeOfGrid-2, getHeight()/sizeOfGrid-2);
                updateArraySize();
            }
        });
    }
    private void updateArraySize() {  //zaktualowanie wielkosc planszy do gry
        Set<Point> pointsToDelete = new HashSet<>();
        points.forEach(point -> {
            if(point.y > sizeOfGameBoard.height-1 || point.x > sizeOfGameBoard.width-1) Collections.addAll(pointsToDelete, point);
        });
        points.removeAll(pointsToDelete); //usuniecie punktow z glownego zbioru
        repaint();
    }

    public void generateCells() { //automatyczne generowanie komorek
        clearBoard();
        for (int r = 0; r < sizeOfGameBoard.width-1; r++) {
            for (int c = 0; c < sizeOfGameBoard.height-1; c++)
                if((Math.random() < fillSet)) drawPoint(r,c);
        }
    }
    private void drawPoint(int x, int y) { //dodawanie komorek do zbioru
        points.add(new Point(x,y));
        repaint();
    }

    private void drawPoint(MouseEvent e) { //wybor tych punktow ktore uzytkownik wyznaczy za pomaca myszki
        int xValue = e.getPoint().x/sizeOfGrid-1;
        int yValue = e.getPoint().y/sizeOfGrid-1;
        if(yValue>=0 && yValue < sizeOfGameBoard.height && xValue>=0 && xValue<sizeOfGameBoard.width) drawPoint(xValue,yValue);
    }

    public void clearBoard() { //wyczyszczenie aktualnego obszaru gry
        points.clear();
        repaint();
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            points.forEach(point -> {
                g.setColor(pointColor);
                g.fillRect((point.x * sizeOfGrid) + sizeOfGrid, (point.y * sizeOfGrid) + sizeOfGrid, sizeOfGrid, sizeOfGrid);
            });
        } catch (ConcurrentModificationException e) {}
        repaint();
    }
    private int getNumberLivingNeighbours(int i, int j, boolean[][] gameBoard) {
        int aliveCellsNeighboor = 0; //liczba zywych komorek w 8 sasiednich komorkach
        if (gameBoard[i-1][j-1]) {
            aliveCellsNeighboor++; //lewy dol
        }
        if (gameBoard[i-1][j])  {
            aliveCellsNeighboor++; // lewy srodek
        }
        if (gameBoard[i-1][j+1]) {
            aliveCellsNeighboor++; //lewy gora
        }
        if (gameBoard[i][j-1]) {
            aliveCellsNeighboor++; //srodek dol
        }
        if (gameBoard[i][j+1])  {
            aliveCellsNeighboor++; //sordek gora
        }
        if (gameBoard[i+1][j-1]) {
            aliveCellsNeighboor++; //prawy dol
        }
        if (gameBoard[i+1][j])  {
            aliveCellsNeighboor++; //prawy srodek
        }
        if (gameBoard[i+1][j+1]){
            aliveCellsNeighboor++; //prawy gora
        }

        return aliveCellsNeighboor;
    }
    @Override
    public void run() {
        boolean[][] gameBoard = new boolean[sizeOfGameBoard.width+2][sizeOfGameBoard.height+2];

        try {
            points.forEach(current->gameBoard[current.x+1][current.y+1] = true);
        } catch (ConcurrentModificationException e) {}

        Set<Point> survivingCells = new HashSet<>(); //zbior komorek ktorze przezyly

        for (int i=1; i<gameBoard.length-1; i++) {
            for (int j=1; j<gameBoard[0].length-1; j++) {
                int surrounding = getNumberLivingNeighbours(i, j, gameBoard); //liczba zywych komorek w 8 sasiednich komorkach

                if (gameBoard[i][j]) { //gdy komorka poaczatkowa sprawdzamy warunki
                    if ((surrounding == aliveCellsSurvive1) || (surrounding == aliveCellsSurvive2)) survivingCells.add(new Point(i-1,j-1));
                } else {
                    if (surrounding == deadCellsAlive) survivingCells.add(new Point(i-1,j-1));
                }
            }
        }
        clearBoard();
        points.addAll(survivingCells); //dodanie punktow ktore przezyly do calosci
        repaint();
        try {
            Thread.sleep(10000/delayMontion);
            run();
        } catch (InterruptedException ex) {}
    }
}
