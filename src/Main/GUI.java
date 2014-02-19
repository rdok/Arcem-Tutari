/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import project.environment.Castle;
import project.environment.SceneNode;

/**
 *
 * @author Riz
 */
public class GUI {

    private final AssetManager assetManager;
    private final SimpleApplication simpApp;
    private BitmapFont guiCrossHair;
    private final float width;
    private final float height;
    private Node GUI;
    private final HeroNode heroNode;
    private Picture healthBarGUI;
    private final String[] healthBarLoc = {"0.png", "1.png", "2.png", "3.png", "4.png", "5.png", "6.png", "7.png", "8.png", "9.png",
        "10.png"};
    private Picture[] healthBarList;
    private int guiHealth;
    private Node nodeGUIHealth;
    private BitmapText hudText;
    private final Castle castleNode;
    private int castleHealth;

    public GUI(HeroNode heroNode, SceneNode sceneNode, AssetManager assetManager, SimpleApplication simpApp, float height, float width) {
        this.heroNode = heroNode;
        this.castleNode = sceneNode.getCastleNode();
        this.assetManager = assetManager;
        this.simpApp = simpApp;
        this.height = height;
        this.width = width;

        GUI = new Node();
        guiHealth = 100; // gui will update if guiHealth != heroHealth
        prepareHealthBar();
        initCrossHairs();
        initHealthBar();
        initCastleInfo();
    }

    /**
     * A centred plus sign to help the player aim.
     */
    private void initCrossHairs() {
        simpApp.setDisplayStatView(false);
        guiCrossHair = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText crossHair = new BitmapText(guiCrossHair, false);
        crossHair.setSize(guiCrossHair.getCharSet().getRenderedSize() * 2);
        crossHair.setText("+"); // crosshairs
        crossHair.setLocalTranslation( // center
                width / 2 - crossHair.getLineWidth() / 2, height / 2 + crossHair.getLineHeight() / 2, 0);
        GUI.attachChild(crossHair);
    }

    public void update() {
        checkHealth();
        updateCastleGUI();
    }

    /**
     * @return the GUI
     */
    public Node getGUI() {
        return GUI;
    }

    /**
     * @param GUI the GUI to set
     */
    public void setGUI(Node GUI) {
        this.GUI = GUI;
    }

    private void initHealthBar() {
        nodeGUIHealth = new Node("Node for Health Bar");
        healthBarGUI = new Picture("Health Bar");
        healthBarGUI.setImage(assetManager, "Textures/GUI/Health_Bar/" + healthBarLoc[10], true);
        healthBarGUI.setWidth(getWidth() / 2.5f);
        healthBarGUI.setHeight(getHeight() / 2.5f);
        healthBarGUI.setPosition(getWidth() / 270, getHeight() / 1.735f);
        nodeGUIHealth.attachChild(healthBarGUI);
        GUI.attachChild(nodeGUIHealth);
    }

    /**
     * @return the width
     */
    public float getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public float getHeight() {
        return height;
    }

    private void checkHealth() {
        int heroHealth = heroNode.getHealth();

        if (heroHealth / 10 != guiHealth) {
            if (heroHealth >= 0) {
                guiHealth = heroHealth / 10;
                updateHealth(guiHealth);
            }

        }
    }

    private void updateHealth(int health) {
        nodeGUIHealth.detachAllChildren();
        nodeGUIHealth.attachChild(healthBarList[health]);
    }

    private void prepareHealthBar() {
        healthBarList = new Picture[healthBarLoc.length];

        for (int i = 0; i < healthBarList.length; i++) {
            healthBarList[i] = new Picture("Health Bar");

            healthBarList[i].setImage(assetManager, "Textures/GUI/Health_Bar/" + i + ".png", true);
            healthBarList[i].setWidth(getWidth() / 2.5f);
            healthBarList[i].setHeight(getHeight() / 2.5f);
            healthBarList[i].setPosition(getWidth() / 270, getHeight() / 1.735f);
        }
    }

    private void initCastleInfo() {
        BitmapFont castleFont = assetManager.loadFont("Interface/Fonts/Orbitron.fnt");
        hudText = new BitmapText(castleFont, false);
        hudText.setSize(castleFont.getCharSet().getRenderedSize());      // font size
        hudText.setColor(ColorRGBA.Blue);                             // font color
        hudText.setText("Castle Health");             // the text
        hudText.setLocalTranslation(getWidth() / 2, getHeight() - 15, 0); // position
        GUI.attachChild(hudText);

    }

    private void updateCastleGUI() {
        castleHealth = castleNode.getHealth();

        if (castleHealth > 0) {
            if (castleHealth > 500) {
                hudText.setColor(ColorRGBA.Green);
            } else if (castleHealth > 200) {
                hudText.setColor(ColorRGBA.Orange);
            } else if (castleHealth > 0) {
                hudText.setColor(ColorRGBA.Red);
            }
            hudText.setText("Castle Health: " + castleHealth);
        } else {
            hudText.setText("Castle Destroyed.");
        }
    }
}
