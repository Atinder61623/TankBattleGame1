import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


public class TankBattleGame extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int HUD_HEIGHT = 40;
    private static final int TILE = 40;

    private final Timer timer;
    private final Random random = new Random();

    private PlayerTank player;
    private Base base;

    private final ArrayList<EnemyTank> enemies = new ArrayList<>();
    private final ArrayList<Bullet> bullets = new ArrayList<>();
    private final ArrayList<TileObject> mapObjects = new ArrayList<>();
    private final ArrayList<PowerUp> powerUps = new ArrayList<>();
    private final ArrayList<Explosion> explosions = new ArrayList<>();
    private final ArrayList<Portal> portals = new ArrayList<>();

    private boolean up, down, left, right;
    private boolean paused = false;
    private boolean gameOver = false;
    private boolean gameWon = false;

    private int score = 0;
    private int level = 1;
    private int enemyShotsTimer = 0;
    private int powerUpTimer = 0;

    public TankBattleGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(25, 25, 25));
        setFocusable(true);
        addKeyListener(this);

        startLevel(1);
        timer = new Timer(20, this);
        timer.start();
    }

    private void startLevel(int levelNumber) {
        level = levelNumber;
        up = down = left = right = false;
        paused = false;
        gameOver = false;
        gameWon = false;

        bullets.clear();
        enemies.clear();
        mapObjects.clear();
        powerUps.clear();
        explosions.clear();
        portals.clear();

        player = new PlayerTank(380, 520);
        base = new Base(380, 480);

        createMap(level);
        createEnemies(level);
    }

    private void createMap(int level) {
        mapObjects.add(new BrickWall(340, 480));
        mapObjects.add(new BrickWall(420, 480));
        mapObjects.add(new BrickWall(340, 520));
        mapObjects.add(new BrickWall(420, 520));

        for (int x = 80; x <= 240; x += TILE) mapObjects.add(new BrickWall(x, 160));
        for (int x = 520; x <= 680; x += TILE) mapObjects.add(new BrickWall(x, 160));
        for (int x = 160; x <= 280; x += TILE) mapObjects.add(new SteelWall(x, 320));
        for (int x = 480; x <= 600; x += TILE) mapObjects.add(new SteelWall(x, 320));

        mapObjects.add(new Water(320, 240));
        mapObjects.add(new Water(360, 240));
        mapObjects.add(new Water(400, 240));

        mapObjects.add(new Bush(80, 400));
        mapObjects.add(new Bush(120, 400));
        mapObjects.add(new Bush(640, 400));
        mapObjects.add(new Bush(680, 400));

        if (level >= 2) {
            for (int y = 80; y <= 240; y += TILE) mapObjects.add(new BrickWall(360, y));
            for (int y = 80; y <= 240; y += TILE) mapObjects.add(new BrickWall(440, y));

            Portal p1 = new Portal(40, 520);
            Portal p2 = new Portal(720, 80);
            portals.add(p1);
            portals.add(p2);
            mapObjects.add(p1);
            mapObjects.add(p2);
        }

        if (level >= 3) {
            for (int x = 80; x <= 680; x += 120) mapObjects.add(new SteelWall(x, 440));
            mapObjects.add(new Water(520, 240));
            mapObjects.add(new Water(560, 240));
            mapObjects.add(new Water(600, 240));
        }
    }

    private void createEnemies(int level) {
        enemies.add(new BasicEnemy(80, 70));
        enemies.add(new BasicEnemy(220, 70));
        enemies.add(new FastEnemy(360, 70));
        enemies.add(new BasicEnemy(500, 70));
        enemies.add(new HeavyEnemy(640, 70));

        if (level >= 2) {
            enemies.add(new FastEnemy(120, 230));
            enemies.add(new HeavyEnemy(640, 230));
        }

        if (level >= 3) {
            enemies.add(new FastEnemy(260, 420));
            enemies.add(new HeavyEnemy(500, 420));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        drawHUD(g);

        for (TileObject obj : mapObjects) obj.draw(g);
        base.draw(g);
        player.draw(g);
        for (EnemyTank enemy : enemies) enemy.draw(g);
        for (PowerUp powerUp : powerUps) powerUp.draw(g);
        for (Bullet bullet : bullets) bullet.draw(g);
        for (Explosion explosion : explosions) explosion.draw(g);

        drawMessages(g);
    }

    private void drawBackground(Graphics g) {
        g.setColor(new Color(20, 22, 24));
        g.fillRect(0, HUD_HEIGHT, WIDTH, HEIGHT - HUD_HEIGHT);
        g.setColor(new Color(31, 34, 38));
        for (int x = 0; x < WIDTH; x += TILE) {
            for (int y = HUD_HEIGHT; y < HEIGHT; y += TILE) {
                g.drawRect(x, y, TILE, TILE);
            }
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(new Color(15, 15, 15));
        g.fillRect(0, 0, WIDTH, HUD_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Health: " + player.health, 15, 25);
        g.drawString("Shield: " + player.shield, 130, 25);
        g.drawString("Score: " + score, 250, 25);
        g.drawString("Level: " + level, 360, 25);
        g.drawString("Enemies: " + enemies.size(), 460, 25);
        g.drawString("P = Pause | R = Restart | Space = Shoot", 575, 25);
    }

    private void drawMessages(Graphics g) {
        if (paused) drawCenteredMessage(g, "PAUSED", Color.YELLOW, 54, HEIGHT / 2);
        if (gameOver) {
            drawCenteredMessage(g, "GAME OVER", Color.RED, 54, HEIGHT / 2);
            drawCenteredMessage(g, "Press R to restart", Color.WHITE, 22, HEIGHT / 2 + 45);
        }
        if (gameWon) {
            drawCenteredMessage(g, "YOU WIN!", Color.GREEN, 54, HEIGHT / 2);
            drawCenteredMessage(g, "Press R to play again", Color.WHITE, 22, HEIGHT / 2 + 45);
        }
    }

    private void drawCenteredMessage(Graphics g, String text, Color color, int size, int y) {
        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver && !gameWon) {
            movePlayer();
            moveEnemies();
            moveBullets();
            updateExplosions();
            updatePowerUps();
            checkCollisions();
            checkLevelComplete();
        }
        repaint();
    }

    private void movePlayer() {
        int oldX = player.x;
        int oldY = player.y;

        if (up) { player.y -= player.speed; player.direction = Direction.UP; }
        if (down) { player.y += player.speed; player.direction = Direction.DOWN; }
        if (left) { player.x -= player.speed; player.direction = Direction.LEFT; }
        if (right) { player.x += player.speed; player.direction = Direction.RIGHT; }

        keepInsideScreen(player);
        if (collidesWithSolidObject(player)) {
            player.x = oldX;
            player.y = oldY;
        }
        checkPortal(player);
    }

    private void moveEnemies() {
        for (EnemyTank enemy : enemies) {
            int oldX = enemy.x;
            int oldY = enemy.y;

            if (random.nextInt(100) < 2) enemy.changeDirection();
            enemy.move();
            keepInsideScreen(enemy);

            if (collidesWithSolidObject(enemy) || enemy.getBounds().intersects(base.getBounds())) {
                enemy.x = oldX;
                enemy.y = oldY;
                enemy.changeDirection();
            }
        }

        enemyShotsTimer++;
        if (enemyShotsTimer > 30) {
            for (EnemyTank enemy : enemies) {
                if (random.nextInt(100) < enemy.shootChance) bullets.add(enemy.shoot(false));
            }
            enemyShotsTimer = 0;
        }
    }

    private void moveBullets() {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.move();
            if (bullet.x < 0 || bullet.x > WIDTH || bullet.y < HUD_HEIGHT || bullet.y > HEIGHT) {
                iterator.remove();
            }
        }
    }

    private void updateExplosions() {
        explosions.removeIf(Explosion::isFinished);
        for (Explosion explosion : explosions) explosion.update();
    }

    private void updatePowerUps() {
        powerUpTimer++;
        if (powerUpTimer > 420 && powerUps.size() < 2) {
            int x = 40 + random.nextInt(18) * 40;
            int y = 80 + random.nextInt(11) * 40;
            PowerUpType type = PowerUpType.values()[random.nextInt(PowerUpType.values().length)];
            PowerUp p = new PowerUp(x, y, type);
            if (!collidesWithSolidObject(p) && !p.getBounds().intersects(base.getBounds())) powerUps.add(p);
            powerUpTimer = 0;
        }
    }

    private void checkCollisions() {
        ArrayList<Bullet> bulletsToRemove = new ArrayList<>();
        ArrayList<EnemyTank> enemiesToRemove = new ArrayList<>();
        ArrayList<TileObject> objectsToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            for (TileObject obj : mapObjects) {
                if (obj.blocksBullets && bullet.getBounds().intersects(obj.getBounds())) {
                    bulletsToRemove.add(bullet);
                    explosions.add(new Explosion(bullet.x, bullet.y));
                    if (obj instanceof BrickWall) objectsToRemove.add(obj);
                    break;
                }
            }

            if (bullet.getBounds().intersects(base.getBounds())) {
                bulletsToRemove.add(bullet);
                base.health -= 20;
                explosions.add(new Explosion(base.x + 10, base.y + 10));
                if (base.health <= 0) gameOver = true;
            }

            if (bullet.fromPlayer) {
                for (EnemyTank enemy : enemies) {
                    if (bullet.getBounds().intersects(enemy.getBounds())) {
                        bulletsToRemove.add(bullet);
                        enemy.health -= bullet.damage;
                        explosions.add(new Explosion(enemy.x + 10, enemy.y + 10));
                        if (enemy.health <= 0) {
                            enemiesToRemove.add(enemy);
                            score += enemy.scoreValue;
                            maybeDropPowerUp(enemy.x, enemy.y);
                        }
                    }
                }
            } else if (bullet.getBounds().intersects(player.getBounds())) {
                bulletsToRemove.add(bullet);
                explosions.add(new Explosion(player.x + 10, player.y + 10));
                int damage = bullet.damage;
                if (player.shield > 0) {
                    int shieldBlock = Math.min(player.shield, damage);
                    player.shield -= shieldBlock;
                    damage -= shieldBlock;
                }
                player.health -= damage;
                if (player.health <= 0) gameOver = true;
            }
        }

        for (PowerUp powerUp : powerUps) {
            if (player.getBounds().intersects(powerUp.getBounds())) {
                powerUp.apply(player);
                powerUp.collected = true;
                score += 25;
            }
        }

        bullets.removeAll(bulletsToRemove);
        enemies.removeAll(enemiesToRemove);
        mapObjects.removeAll(objectsToRemove);
        powerUps.removeIf(p -> p.collected);
    }

    private void maybeDropPowerUp(int x, int y) {
        if (random.nextInt(100) < 35) {
            PowerUpType type = PowerUpType.values()[random.nextInt(PowerUpType.values().length)];
            powerUps.add(new PowerUp(x, y, type));
        }
    }

    private void checkLevelComplete() {
        if (enemies.isEmpty()) {
            if (level < 3) startLevel(level + 1);
            else gameWon = true;
        }
    }

    private boolean collidesWithSolidObject(GameEntity entity) {
        Rectangle r = entity.getBounds();
        for (TileObject obj : mapObjects) {
            if (obj.blocksTanks && r.intersects(obj.getBounds())) return true;
        }
        return false;
    }

    private void checkPortal(Tank tank) {
        if (portals.size() < 2) return;
        if (tank.portalCooldown > 0) {
            tank.portalCooldown--;
            return;
        }

        Portal first = portals.get(0);
        Portal second = portals.get(1);
        if (tank.getBounds().intersects(first.getBounds())) {
            tank.x = second.x;
            tank.y = second.y + TILE;
            tank.portalCooldown = 50;
        } else if (tank.getBounds().intersects(second.getBounds())) {
            tank.x = first.x;
            tank.y = first.y - TILE;
            tank.portalCooldown = 50;
        }
    }

    private void keepInsideScreen(Tank tank) {
        if (tank.x < 0) tank.x = 0;
        if (tank.y < HUD_HEIGHT) tank.y = HUD_HEIGHT;
        if (tank.x + tank.width > WIDTH) tank.x = WIDTH - tank.width;
        if (tank.y + tank.height > HEIGHT) tank.y = HEIGHT - tank.height;
    }

    private void restartGame() {
        score = 0;
        startLevel(1);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) up = true;
        if (key == KeyEvent.VK_S) down = true;
        if (key == KeyEvent.VK_A) left = true;
        if (key == KeyEvent.VK_D) right = true;
        if (key == KeyEvent.VK_SPACE && !paused && !gameOver && !gameWon) bullets.add(player.shoot(true));
        if (key == KeyEvent.VK_P && !gameOver && !gameWon) paused = !paused;
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
        JFrame frame = new JFrame("Enhanced Tank Battle Game");
        TankBattleGame game = new TankBattleGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}

enum Direction { UP, DOWN, LEFT, RIGHT }
enum PowerUpType { HEALTH, SHIELD, SPEED }

abstract class GameEntity {
    int x, y, width, height;
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public abstract void draw(Graphics g);
}

abstract class Tank extends GameEntity {
    int speed = 3;
    int health = 100;
    int shield = 0;
    int portalCooldown = 0;
    Direction direction = Direction.UP;
    Color bodyColor;

    public Tank(int x, int y, Color bodyColor) {
        this.x = x; this.y = y; this.width = 36; this.height = 36; this.bodyColor = bodyColor;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(x - 3, y + 3, 8, height - 6, 4, 4);
        g2.fillRoundRect(x + width - 5, y + 3, 8, height - 6, 4, 4);
        g2.setColor(bodyColor);
        g2.fillRoundRect(x, y, width, height, 10, 10);
        g2.setColor(bodyColor.brighter());
        g2.fillOval(x + 8, y + 8, 20, 20);
        g2.setColor(Color.WHITE);
        if (direction == Direction.UP) g2.fillRect(x + 16, y - 14, 5, 18);
        if (direction == Direction.DOWN) g2.fillRect(x + 16, y + height - 2, 5, 18);
        if (direction == Direction.LEFT) g2.fillRect(x - 14, y + 16, 18, 5);
        if (direction == Direction.RIGHT) g2.fillRect(x + width - 2, y + 16, 18, 5);
        g2.setColor(Color.RED);
        g2.fillRect(x, y - 8, width, 4);
        g2.setColor(Color.GREEN);
        int bar = Math.max(0, Math.min(width, health * width / 120));
        g2.fillRect(x, y - 8, bar, 4);
        if (shield > 0) {
            g2.setColor(new Color(80, 180, 255, 120));
            g2.fillOval(x - 5, y - 5, width + 10, height + 10);
        }
    }

    public Bullet shoot(boolean fromPlayer) {
        int bx = x + width / 2 - 4;
        int by = y + height / 2 - 4;
        if (direction == Direction.UP) by -= 20;
        if (direction == Direction.DOWN) by += 20;
        if (direction == Direction.LEFT) bx -= 20;
        if (direction == Direction.RIGHT) bx += 20;
        return new Bullet(bx, by, direction, fromPlayer);
    }
}

class PlayerTank extends Tank {
    public PlayerTank(int x, int y) { super(x, y, new Color(40, 200, 80)); speed = 4; health = 120; }
}

abstract class EnemyTank extends Tank {
    private final Random random = new Random();
    int shootChance = 10;
    int scoreValue = 100;
    public EnemyTank(int x, int y, Color color) { super(x, y, color); direction = Direction.DOWN; }
    public void move() {
        if (direction == Direction.UP) y -= speed;
        if (direction == Direction.DOWN) y += speed;
        if (direction == Direction.LEFT) x -= speed;
        if (direction == Direction.RIGHT) x += speed;
    }
    public void changeDirection() {
        int choice = random.nextInt(4);
        if (choice == 0) direction = Direction.UP;
        if (choice == 1) direction = Direction.DOWN;
        if (choice == 2) direction = Direction.LEFT;
        if (choice == 3) direction = Direction.RIGHT;
    }
}

class BasicEnemy extends EnemyTank {
    public BasicEnemy(int x, int y) { super(x, y, new Color(220, 60, 60)); health = 60; speed = 2; shootChance = 12; scoreValue = 100; }
}
class FastEnemy extends EnemyTank {
    public FastEnemy(int x, int y) { super(x, y, new Color(255, 160, 40)); health = 45; speed = 3; shootChance = 16; scoreValue = 150; }
}
class HeavyEnemy extends EnemyTank {
    public HeavyEnemy(int x, int y) { super(x, y, new Color(160, 80, 220)); health = 100; speed = 1; shootChance = 10; scoreValue = 200; }
}

class Bullet extends GameEntity {
    int speed = 8;
    int damage = 20;
    Direction direction;
    boolean fromPlayer;
    public Bullet(int x, int y, Direction direction, boolean fromPlayer) {
        this.x = x; this.y = y; this.width = 8; this.height = 8; this.direction = direction; this.fromPlayer = fromPlayer;
        if (fromPlayer) damage = 25;
    }
    public void move() {
        if (direction == Direction.UP) y -= speed;
        if (direction == Direction.DOWN) y += speed;
        if (direction == Direction.LEFT) x -= speed;
        if (direction == Direction.RIGHT) x += speed;
    }
    public void draw(Graphics g) { g.setColor(fromPlayer ? Color.YELLOW : Color.ORANGE); g.fillOval(x, y, width, height); }
}

abstract class TileObject extends GameEntity {
    boolean blocksTanks;
    boolean blocksBullets;
    public TileObject(int x, int y, boolean blocksTanks, boolean blocksBullets) {
        this.x = x; this.y = y; this.width = 40; this.height = 40; this.blocksTanks = blocksTanks; this.blocksBullets = blocksBullets;
    }
}

class BrickWall extends TileObject {
    public BrickWall(int x, int y) { super(x, y, true, true); }
    public void draw(Graphics g) {
        g.setColor(new Color(160, 80, 35)); g.fillRect(x, y, width, height);
        g.setColor(new Color(90, 40, 20));
        for (int i = 0; i < 4; i++) g.drawLine(x, y + i * 10, x + width, y + i * 10);
        g.drawLine(x + 20, y, x + 20, y + 10);
        g.drawLine(x + 10, y + 10, x + 10, y + 20);
        g.drawLine(x + 30, y + 20, x + 30, y + 30);
        g.drawLine(x + 20, y + 30, x + 20, y + 40);
    }
}

class SteelWall extends TileObject {
    public SteelWall(int x, int y) { super(x, y, true, true); }
    public void draw(Graphics g) {
        g.setColor(Color.LIGHT_GRAY); g.fillRect(x, y, width, height);
        g.setColor(Color.GRAY); g.drawRect(x + 4, y + 4, 32, 32);
        g.drawLine(x, y, x + width, y + height); g.drawLine(x + width, y, x, y + height);
    }
}

class Water extends TileObject {
    public Water(int x, int y) { super(x, y, true, false); }
    public void draw(Graphics g) {
        g.setColor(new Color(30, 110, 210)); g.fillRect(x, y, width, height);
        g.setColor(new Color(120, 200, 255));
        g.drawArc(x + 4, y + 8, 30, 10, 0, 180);
        g.drawArc(x + 8, y + 22, 28, 10, 0, 180);
    }
}

class Bush extends TileObject {
    public Bush(int x, int y) { super(x, y, false, false); }
    public void draw(Graphics g) {
        g.setColor(new Color(20, 120, 45)); g.fillRect(x, y, width, height);
        g.setColor(new Color(40, 180, 70));
        g.fillOval(x + 2, y + 5, 18, 18);
        g.fillOval(x + 18, y + 4, 20, 20);
        g.fillOval(x + 8, y + 20, 24, 16);
    }
}

class Portal extends TileObject {
    public Portal(int x, int y) { super(x, y, false, false); }
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(80, 0, 160)); g2.fillOval(x + 4, y + 4, 32, 32);
        g2.setColor(new Color(200, 80, 255));
        g2.drawOval(x + 8, y + 8, 24, 24); g2.drawOval(x + 13, y + 13, 14, 14);
    }
}

class Base extends GameEntity {
    int health = 80;
    public Base(int x, int y) { this.x = x; this.y = y; this.width = 40; this.height = 40; }
    public void draw(Graphics g) {
        g.setColor(new Color(230, 210, 70)); g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        int[] xs = {x + 20, x + 8, x + 32};
        int[] ys = {y + 7, y + 32, y + 32};
        g.fillPolygon(xs, ys, 3);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 10)); g.drawString("BASE", x + 6, y + 38);
    }
}

class PowerUp extends GameEntity {
    PowerUpType type;
    boolean collected = false;
    public PowerUp(int x, int y, PowerUpType type) { this.x = x; this.y = y; this.width = 28; this.height = 28; this.type = type; }
    public void apply(PlayerTank player) {
        if (type == PowerUpType.HEALTH) player.health = Math.min(120, player.health + 35);
        if (type == PowerUpType.SHIELD) player.shield = Math.min(80, player.shield + 40);
        if (type == PowerUpType.SPEED) player.speed = Math.min(6, player.speed + 1);
    }
    public void draw(Graphics g) {
        if (type == PowerUpType.HEALTH) g.setColor(Color.PINK);
        if (type == PowerUpType.SHIELD) g.setColor(Color.CYAN);
        if (type == PowerUpType.SPEED) g.setColor(Color.MAGENTA);
        g.fillOval(x, y, width, height);
        g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 16));
        String letter = type == PowerUpType.HEALTH ? "H" : type == PowerUpType.SHIELD ? "S" : "F";
        g.drawString(letter, x + 8, y + 20);
    }
}

class Explosion extends GameEntity {
    int life = 18;
    public Explosion(int x, int y) { this.x = x; this.y = y; this.width = 30; this.height = 30; }
    public void update() { life--; }
    public boolean isFinished() { return life <= 0; }
    public void draw(Graphics g) {
        int size = 30 - life;
        g.setColor(Color.ORANGE); g.fillOval(x - size / 2, y - size / 2, size + 15, size + 15);
        g.setColor(Color.YELLOW); g.fillOval(x, y, Math.max(5, size), Math.max(5, size));
    }
}
