import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class JeuBalle extends JPanel implements Runnable {

    // Position et vitesse de la balle
    private int x = 50;
    private int y = 70;
    private int vitesse = 6;
    private int sensX = 1;
    private int sensY = 1;
    private static final int DIAMETRE = 60;

    // Raquette
    private int raquetteX = 260;
    private static final int LARGEUR_RAQUETTE = 120;
    private static final int HAUTEUR_RAQUETTE = 14;
    private static final int MARGE_RAQUETTE = 40;
    private boolean gauche = false;
    private boolean droite = false;

    // Etat du jeu
    private volatile boolean enMarche = false;
    private boolean partiePerdue = false;
    private int totalGenere = 0;
    private int rebonds = 0;

    // Thread pour animer la balle
    private Thread animateur;
    private Runnable quandPerdu = () -> {};

    private final AtomicBoolean miseAJourEnAttente = new AtomicBoolean(false);

    public JeuBalle() {
        setPreferredSize(new Dimension(640, 380));
        setBackground(new Color(24, 29, 40));
        configurerClavier();
    }

    // Configurer une touche
    private void lierTouche(String touche, String nom, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(touche), nom);
        getActionMap().put(nom, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void configurerClavier() {
        lierTouche("pressed LEFT", "gaucheActive", () -> gauche = true);
        lierTouche("released LEFT", "gaucheArret", () -> gauche = false);
        lierTouche("pressed RIGHT", "droiteActive", () -> droite = true);
        lierTouche("released RIGHT", "droiteArret", () -> droite = false);
    }

    public void setQuandPerdu(Runnable action) {
        quandPerdu = action;
    }

    // Demarrer le jeu
    public void demarrer() {
        x = 50;
        y = 70;
        sensX = 1;
        sensY = 1;
        raquetteX = Math.max(0, (getWidth() - LARGEUR_RAQUETTE) / 2);
        gauche = false;
        droite = false;
        partiePerdue = false;
        rebonds = 0;
        totalGenere++;
        enMarche = true;

        if (animateur == null) {
            animateur = new Thread(this, "Thread-Animation-Balle");
            animateur.setDaemon(true);
            animateur.start();
        }
        repaint();
    }

    public void pause() {
        enMarche = false;
        gauche = false;
        droite = false;
    }

    public void reprendre() {
        if (!partiePerdue) {
            gauche = false;
            droite = false;
            enMarche = true;
        }
    }

    public void setVitesse(int valeur) {
        vitesse = Math.max(1, Math.min(12, valeur));
    }

    // Boucle du thread
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (enMarche && miseAJourEnAttente.compareAndSet(false, true)) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (enMarche) {
                            deplacerBalleEtRaquette();
                        }
                    } finally {
                        miseAJourEnAttente.set(false);
                    }
                });
            }
        }
    }

    // Detecter une collision
    static boolean cercleToucheRectangle(int bx, int by, int diametre,
                                         int rx, int ry, int largeur, int hauteur) {
        double centreX = bx + diametre / 2.0;
        double centreY = by + diametre / 2.0;
        double rayon = diametre / 2.0;
        double procheX = Math.max(rx, Math.min(centreX, rx + largeur));
        double procheY = Math.max(ry, Math.min(centreY, ry + hauteur));
        double ecartX = centreX - procheX;
        double ecartY = centreY - procheY;
        return ecartX * ecartX + ecartY * ecartY <= rayon * rayon;
    }

    static boolean balleDoitRebondir(int bx, int by, int diametre,
                                    int rx, int ry, int largeur, int hauteur,
                                    int directionVerticale) {
        double centreY = by + diametre / 2.0;
        return directionVerticale > 0
                && centreY <= ry + hauteur
                && cercleToucheRectangle(bx, by, diametre, rx, ry, largeur, hauteur);
    }

    // Deplacer les objets
    private void deplacerBalleEtRaquette() {
        if (gauche && !droite) {
            raquetteX -= 8;
        } else if (droite && !gauche) {
            raquetteX += 8;
        }
        raquetteX = Math.max(0,
                Math.min(raquetteX, getWidth() - LARGEUR_RAQUETTE));

        int pasY = Math.max(1, vitesse * 2 / 3);
        x += vitesse * sensX;
        y += pasY * sensY;

        if (x <= 0) {
            x = 0;
            sensX = 1;
        } else if (x + DIAMETRE >= getWidth()) {
            x = getWidth() - DIAMETRE;
            sensX = -1;
        }
        if (y <= 0) {
            y = 0;
            sensY = 1;
        }

        int raquetteY = getHeight() - MARGE_RAQUETTE;
        if (balleDoitRebondir(x, y, DIAMETRE,
                raquetteX, raquetteY, LARGEUR_RAQUETTE, HAUTEUR_RAQUETTE, sensY)) {
            y = raquetteY - DIAMETRE;
            sensY = -1;
            rebonds++;
        }

        if (y + DIAMETRE >= getHeight()) {
            y = getHeight() - DIAMETRE;
            enMarche = false;
            partiePerdue = true;
            gauche = false;
            droite = false;
            quandPerdu.run();
        }
        repaint();
    }

    // Dessiner le jeu
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(240, 78, 78));
            g2.fillOval(x, y, DIAMETRE, DIAMETRE);

            g2.setColor(new Color(70, 202, 182));
            g2.fillRoundRect(raquetteX, getHeight() - MARGE_RAQUETTE,
                    LARGEUR_RAQUETTE, HAUTEUR_RAQUETTE, 12, 12);

            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(Color.WHITE);
            g2.drawString("Total généré : " + totalGenere, 16, 24);
            g2.drawString("Rebonds : " + rebonds, 16, 46);

            if (partiePerdue) {
                String texte = "Perdu ! Cliquez sur Démarrer";
                g2.setFont(new Font("SansSerif", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                int texteX = (getWidth() - fm.stringWidth(texte)) / 2;
                int texteY = getHeight() / 2;
                g2.drawString(texte, texteX, texteY);
            }
        } finally {
            g2.dispose();
        }
    }

    private static void appliquerIcone(JFrame fenetre) {
        URL url = JeuBalle.class.getResource("/jeu_balle_icon.png");
        if (url != null) {
            try {
                fenetre.setIconImage(ImageIO.read(url));
            } catch (IOException e) {
                System.err.println("Icône indisponible : " + e.getMessage());
            }
        }
    }

    // Lancer la fenetre
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame fenetre = new JFrame("Jeu de balle - Raquette et Threads");
            appliquerIcone(fenetre);
            JeuBalle jeu = new JeuBalle();

            JButton boutonDemarrer = new JButton("Démarrer");
            JButton boutonPause = new JButton("Pause");
            JButton boutonReprendre = new JButton("Reprendre");
            JSlider slider = new JSlider(1, 12, 6);
            JLabel labelVitesse = new JLabel("Vitesse : 6");
            JLabel aide = new JLabel("Raquette : flèches ← →");
            JLabel etat = new JLabel("Prêt à jouer");

            boutonPause.setEnabled(false);
            boutonReprendre.setEnabled(false);
            boutonDemarrer.setFocusable(false);
            boutonPause.setFocusable(false);
            boutonReprendre.setFocusable(false);
            slider.setFocusable(false);
            slider.setPreferredSize(new Dimension(155, 35));
            aide.setForeground(new Color(66, 75, 91));
            etat.setForeground(new Color(54, 105, 145));

            boutonDemarrer.addActionListener(e -> {
                jeu.demarrer();
                boutonDemarrer.setEnabled(false);
                boutonPause.setEnabled(true);
                boutonReprendre.setEnabled(false);
                etat.setText("En cours");
            });
            boutonPause.addActionListener(e -> {
                jeu.pause();
                boutonDemarrer.setEnabled(true);
                boutonPause.setEnabled(false);
                boutonReprendre.setEnabled(true);
                etat.setText("En pause");
            });
            boutonReprendre.addActionListener(e -> {
                jeu.reprendre();
                boutonDemarrer.setEnabled(false);
                boutonPause.setEnabled(true);
                boutonReprendre.setEnabled(false);
                etat.setText("En cours");
            });
            slider.addChangeListener(e -> {
                int valeur = slider.getValue();
                jeu.setVitesse(valeur);
                labelVitesse.setText("Vitesse : " + valeur);
            });
            jeu.setQuandPerdu(() -> {
                boutonDemarrer.setEnabled(true);
                boutonPause.setEnabled(false);
                boutonReprendre.setEnabled(false);
                etat.setText("Partie perdue");
            });

            JPanel ligne1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 5));
            ligne1.add(boutonDemarrer);
            ligne1.add(boutonPause);
            ligne1.add(boutonReprendre);
            ligne1.add(aide);

            JPanel ligne2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 4));
            ligne2.add(labelVitesse);
            ligne2.add(slider);
            ligne2.add(Box.createHorizontalStrut(18));
            ligne2.add(etat);

            JPanel outils = new JPanel();
            outils.setLayout(new BoxLayout(outils, BoxLayout.Y_AXIS));
            outils.add(ligne1);
            outils.add(ligne2);

            fenetre.setLayout(new BorderLayout());
            fenetre.add(outils, BorderLayout.NORTH);
            fenetre.add(jeu, BorderLayout.CENTER);
            fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fenetre.pack();
            fenetre.setMinimumSize(new Dimension(490, 340));
            fenetre.setLocationRelativeTo(null);
            fenetre.setVisible(true);
        });
    }
}
