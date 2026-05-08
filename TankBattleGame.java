import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class TankBattleGame extends JPanel implements ActionListener, KeyListener {
    private final Timer timer;
    private final PlayerTank player;
    private final ArrayList<EnemyTank> enemies;
    private final ArrayList<Bullet> bullets;
    private final ArrayList<Wall> walls;
    private boolean up, down, left, right;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private int score = 0;

    public TankBattleGame() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        player = new PlayerTank(380, 520);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        walls = new ArrayList<>();

        createEnemies();
        createWalls();

        timer = new Timer(20, this);
        timer.start();
    }

    private void createEnemies() {
        enemies.add(new EnemyTank(80, 60));
        enemies.add(new EnemyTank(220, 60));
        enemies.add(new EnemyTank(360, 60));
        enemies.add(new EnemyTank(500, 60));
        enemies.add(new EnemyTank(640, 60));
    }

    private void createWalls() {
        walls.add(new Wall(120, 180, 120, 30));
        walls.add(new Wall(330, 230, 140, 30));
        walls.add(new Wall(560, 180, 120, 30));
        walls.add(new Wall(180, 360, 120, 30));
        walls.add(new Wall(500, 360, 120, 30));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Health: " + player.health + "   Score: " + score + "   Enemies Left: " + enemies.size(), 20, 25);

        for (Wall wall : walls) wall.draw(g);
        player.draw(g);
        for (EnemyTank enemy : enemies) enemy.draw(g);
        for (Bullet bullet : bullets) bullet.draw(g);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", 260, 300);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press R to restart", 320, 340);
        }

        if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("YOU WIN!", 300, 300);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press R to restart", 320, 340);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !gameWon) {
            movePlayer();
            moveEnemies();
            moveBullets();
            checkCollisions();
        }
        repaint();
    }

    private void movePlayer() {
        int oldX = player.x;
        int oldY = player.y;

        if (up) { player.y -= player.speed; player.direction = "UP"; }
        if (down) { player.y += player.speed; player.direction = "DOWN"; }
        if (left) { player.x -= player.speed; player.direction = "LEFT"; }
        if (right) { player.x += player.speed; player.direction = "RIGHT"; }

        keepInsideScreen(player);

        for (Wall wall : walls) {
            if (player.getBounds().intersects(wall.getBounds())) {
                player.x = oldX;
                player.y = oldY;
                break;
            }
        }
    }

    private void moveEnemies() {
        Random random = new Random();

        for (EnemyTank enemy : enemies) {
            int oldX = enemy.x;
            int oldY = enemy.y;

            if (random.nextInt(100) < 2) enemy.changeDirection();

            enemy.move();
            keepInsideScreen(enemy);

            for (Wall wall : walls) {
                if (enemy.getBounds().intersects(wall.getBounds())) {
                    enemy.x = oldX;
                    enemy.y = oldY;
                    enemy.changeDirection();
                    break;
                }
            }

            if (random.nextInt(100) < 1) bullets.add(enemy.shoot(false));
        }
    }

    private void moveBullets() {
        ArrayList<Bullet> removeList = new ArrayList<>();

        for (Bullet bullet : bullets) {
            bullet.move();

            if (bullet.x < 0 || bullet.x > 800 || bullet.y < 0 || bullet.y > 600) removeList.add(bullet);

            for (Wall wall : walls) {
                if (bullet.getBounds().intersects(wall.getBounds())) removeList.add(bullet);
            }
        }

        bullets.removeAll(removeList);
    }

    private void checkCollisions() {
        ArrayList<Bullet> bulletsToRemove = new ArrayList<>();
        ArrayList<EnemyTank> enemiesToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            if (bullet.fromPlayer) {
                for (EnemyTank enemy : enemies) {
                    if (bullet.getBounds().intersects(enemy.getBounds())) {
                        enemy.health -= 20;
                        bulletsToRemove.add(bullet);

                        if (enemy.health <= 0) {
                            enemiesToRemove.add(enemy);
                            score += 100;
                        }
                    }
                }
            } else {
                if (bullet.getBounds().intersects(player.getBounds())) {
                    player.health -= 10;
                    bulletsToRemove.add(bullet);

                    if (player.health <= 0) gameOver = true;
                }
            }
        }

        bullets.removeAll(bulletsToRemove);
        enemies.removeAll(enemiesToRemove);

        if (enemies.isEmpty()) gameWon = true;
    }

    private void keepInsideScreen(Tank tank) {
        if (tank.x < 0) tank.x = 0;
        if (tank.y < 35) tank.y = 35;
        if (tank.x + tank.width > 800) tank.x = 800 - tank.width;
        if (tank.y + tank.height > 600) tank.y = 600 - tank.height;
    }

    private void restartGame() {
        player.x = 380;
        player.y = 520;
        player.health = 100;
        player.direction = "UP";
        bullets.clear();
        enemies.clear();
        walls.clear();
        createEnemies();
        createWalls();
        score = 0;
        gameOver = false;
        gameWon = false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) up = true;
        if (key == KeyEvent.VK_S) down = true;
        if (key == KeyEvent.VK_A) left = true;
        if (key == KeyEvent.VK_D) right = true;
        if (key == KeyEvent.VK_SPACE && !gameOver && !gameWon) bullets.add(player.shoot(true));
        if (key == KeyEvent.VK_R) restartGame();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) up = false;
        if (key == KeyEvent.VK_S) down = false;
        if (key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_D) right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tank Battle Game");
        TankBattleGame game = new TankBattleGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Tank {
    int x, y;
    int width = 40;
    int height = 40;
    int speed = 3;
    int health = 100;
    String direction = "UP";
    Color color;

    public Tank(int x, int y, Color color) { this.x = x; this.y = y; this.color = color; }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
        g.setColor(Color.WHITE);
        if (direction.equals("UP")) g.fillRect(x + 17, y - 12, 6, 15);
        else if (direction.equals("DOWN")) g.fillRect(x + 17, y + height, 6, 15);
        else if (direction.equals("LEFT")) g.fillRect(x - 12, y + 17, 15, 6);
        else if (direction.equals("RIGHT")) g.fillRect(x + width, y + 17, 15, 6);
    }

    public Bullet shoot(boolean fromPlayer) {
        int bulletX = x + width / 2 - 4;
        int bulletY = y + height / 2 - 4;
        return new Bullet(bulletX, bulletY, direction, fromPlayer);
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
}

class PlayerTank extends Tank {
    public PlayerTank(int x, int y) { super(x, y, Color.GREEN); speed = 4; }
}

class EnemyTank extends Tank {
    private final Random random = new Random();
    public EnemyTank(int x, int y) { super(x, y, Color.RED); health = 40; speed = 2; direction = "DOWN"; }
    public void move() {
        if (direction.equals("UP")) y -= speed;
        if (direction.equals("DOWN")) y += speed;
        if (direction.equals("LEFT")) x -= speed;
        if (direction.equals("RIGHT")) x += speed;
    }
    public void changeDirection() {
        int choice = random.nextInt(4);
        if (choice == 0) direction = "UP";
        if (choice == 1) direction = "DOWN";
        if (choice == 2) direction = "LEFT";
        if (choice == 3) direction = "RIGHT";
    }
}

class Bullet {
    int x, y;
    int size = 8;
    int speed = 7;
    String direction;
    boolean fromPlayer;
    public Bullet(int x, int y, String direction, boolean fromPlayer) { this.x = x; this.y = y; this.direction = direction; this.fromPlayer = fromPlayer; }
    public void move() {
        if (direction.equals("UP")) y -= speed;
        if (direction.equals("DOWN")) y += speed;
        if (direction.equals("LEFT")) x -= speed;
        if (direction.equals("RIGHT")) x += speed;
    }
    public void draw(Graphics g) { g.setColor(fromPlayer ? Color.YELLOW : Color.ORANGE); g.fillOval(x, y, size, size); }
    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
}

class Wall {
    int x, y, width, height;
    public Wall(int x, int y, int width, int height) { this.x = x; this.y = y; this.width = width; this.height = height; }
    public void draw(Graphics g) { g.setColor(Color.GRAY); g.fillRect(x, y, width, height); }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
}
