package blockgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	final int width = 10;
	final int height = 22;
	final int squereSize = 16;

	int score = 0;

	JLabel statusbar;

	Tetrominoes[][] board;

	Shape droping;
	int t = 0;

	Timer timer;
	float time = 0f;

	Boolean fastDrop = false;

	private InputHandler handler;

	Boolean isRunning;

	Shape ghostShape;

	NextPiece nextPiece;
	Tetrominoes nextTetramino;

	public Board(Tetris tetris, NextPiece nextP) {
		timer = new Timer(32, this);

		nextPiece = nextP;

		droping = new Shape();
		droping.setRandomShape();
		droping.setY(5);
		droping.setX(2);
		nextTetramino = droping.getType();
		nextPiece.nextTetromino(nextTetramino);
		droping.setRandomShape();

		ghostShape = new Shape();
		ghostShape.setType(droping.getType());

		handler = new InputHandler(this);

		board = new Tetrominoes[width][height];
		clear();

		statusbar = tetris.getStatusBar();

		setFocusable(true);
		requestFocus();
		requestFocusInWindow();
		addKeyListener(handler);

		timer.start();
		isRunning = true;
	}



	@Override
	public void actionPerformed(ActionEvent e) {

		if (!isRunning) {
			repaint();
			return;
		}
		time += 0.032;

		if (fastDrop)
			time = 0.4f;

		if (time > 0.4) {
			drop();

			time = 0f;
		}

		fastDrop(false);
		updateGhost();
		repaint();
	}

	public void pause() {
		isRunning = !isRunning;
	}

	private void updateGhost() {
		ghostShape.setX(droping.getX());
		ghostShape.setY(droping.getY());
		ghostShape.setPixels(droping.getPixels());
		while (canMove(ghostShape, ghostShape.getX(), ghostShape.getY() + 1)) {
			ghostShape.setY(ghostShape.getY() + 1);
		}

	}

	private void clear() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				board[x][y] = Tetrominoes.NoShape;
			}
		}
	}

	public void drop() {
		if (canMove(droping, droping.getX(), droping.getY() + 1)) {
			droping.setY(droping.getY() + 1);
		} else {
			if (canMove(droping, droping.getX(), droping.getY())) {
				shapeToBoard();
			} else {
				System.out.println("THE END");
				pause();
			}
		}
	}

	public void moveRight() {
		if (canMove(droping, droping.getX() + 1, droping.getY())) {
			droping.setX(droping.getX() + 1);
		}
	}

	public void moveLeft() {
		if (canMove(droping, droping.getX() - 1, droping.getY())) {
			droping.setX(droping.getX() - 1);
		}
	}

	public void rotate() {

		if (canMove(droping.rotateRight(), droping.getX(), droping.getY())) {
			droping = droping.rotateRight();
			ghostShape.setPixels(droping.getPixels());
			updateGhost();
		}
	}

	public void fastDrop(Boolean state) {
		fastDrop = state;
	}

	private void shapeToBoard() {

		for (int i = 0; i < 4; i++) {
			board[droping.getX() + droping.getX(i)][droping.getY() - droping.getY(i)] = droping.getType();
		}

		removeLines();
		newShape();
	}

	private void removeLines() {
		int s = 0;
		for (int y = 0; y < height; y++) {
			Boolean t = true;

			for (int x = 0; x < width; x++) {
				if (isEmpty(x, y)) {
					t = false;
					break;
				}
			}
			if (t) {
				s++;
				for (int k = y - 1; k > 0; k--) {
					for (int x = 0; x < width; x++) {
						board[x][k + 1] = board[x][k];
					}
				}

			}
		}

		score += s;
		statusbar.setText(Integer.toString(score));

	}

	private void newShape() {
		droping.setRandomShape();
		droping.setX(5);
		droping.setY(2);
		Tetrominoes p = droping.getType();
		droping.setType(nextTetramino);
		nextPiece.nextTetromino(p);
		nextTetramino = p;
	}

	private Boolean canMove(Shape shape, int nX, int nY) {
		if (nX < 0 || nX >= width)
			return false;
		if (nY >= height)
			return false;

		for (int i = 0; i < 4; i++) {
			int x = nX + shape.getX(i);
			int y = nY - shape.getY(i);

			if (x < 0 || x >= width)
				return false;

			if (y >= height)
				return false;

			if (y < 0)
				continue;

			if (!isEmpty(x, y)) {
				return false;
			}
		}

		return true;
	}

	private Boolean isEmpty(int x, int y) {
		return board[x][y] == Tetrominoes.NoShape;
	}

	public void paint(Graphics g) {
		super.paint(g);

		drawGrid(g);
		drawGhostShape(droping, ghostShape, g);
		drawFallenShapes(g);
		drawShape(droping, g);
	}

	private void drawFallenShapes(Graphics g) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (board[x][y] != Tetrominoes.NoShape) {
					Color color = shapeToColor(board[x][y]);
					drawShapePixel(g, x, y, color);
				}
			}
		}
	}

	private void drawGhostShape(Shape droping, Shape ghostShape, Graphics g) {
		Color c = shapeToColor(droping.getType());

		for (int i = 0; i < 4; i++) {
			g.setColor(c);
			g.drawRect((ghostShape.getX() + ghostShape.getX(i)) * (squereSize) + 1, (ghostShape.getY() - ghostShape.getY(i)) * squereSize + 1,
					squereSize - 2, squereSize - 2);
			c = c.darker();
			g.setColor(c);
			g.drawRect((ghostShape.getX()  + ghostShape.getX(i)) * squereSize, (ghostShape.getY() - ghostShape.getY(i)) * squereSize, squereSize,
					squereSize);
			c = c.brighter();
		}
	}

	private void drawShape(Shape droping, Graphics g) {
		Color c = shapeToColor(droping.getType());
		for (int i = 0; i < 4; i++) {
			drawShapePixel(g, (droping.getX() + droping.getX(i)), (droping.getY() - droping.getY(i)), c);
		}
	}

	private void drawGrid(Graphics g) {

		g.setColor(new Color(180, 180, 180));
		for (int x = 0; x <= height; x++) {
			g.drawLine(0, x * squereSize, width * squereSize, x * squereSize);
		}
		for (int y = 0; y <= width; y++) {
			g.drawLine(y * squereSize, 0, y * squereSize, height * squereSize);
		}
	}

	public void drawShapePixel(Graphics g, int x, int y, Color c) {

		c = c.darker();
		g.setColor(c);
		g.fillRect(x * squereSize, y * squereSize, squereSize, squereSize);

		c = c.brighter();
		g.setColor(c);
		g.fillRect((x * squereSize) + 1, (y * squereSize) + 1, squereSize - 2, squereSize - 2);

	}

	public static Color shapeToColor(Tetrominoes shape) {
		if (shape == Tetrominoes.LineShape)
			return Color.CYAN;
		if (shape == Tetrominoes.SquareShape)
			return Color.YELLOW;
		if (shape == Tetrominoes.TShape)
			return Color.MAGENTA;
		if (shape == Tetrominoes.SShape)
			return Color.GREEN;
		if (shape == Tetrominoes.ZShape)
			return Color.RED;
		if (shape == Tetrominoes.MirroredLShape)
			return Color.BLUE;
		if (shape == Tetrominoes.LShape)
			return Color.ORANGE;

		return Color.BLACK;
	}

}
