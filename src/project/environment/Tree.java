/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.environment;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Random;

/**
 *
 * @author Riza
 */
public class Tree {

    private final Random random;
    private final String TREE_SIMPLE = "Models/Tree/Tree.mesh.j3o";
    private final String TREE_HIGH = "Models/Bush_High/Bush_High.j3o";
    private Spatial treeModel;
    private int id;
    private final AssetManager assetManager;

    public Tree(AssetManager assetManager) {
        this.assetManager = assetManager;
        random = new Random();
        id = 0;
    }

    private String getRandomForestObject() {
        return random.nextBoolean() ? TREE_HIGH : TREE_SIMPLE;
    }

    public Spatial getTree(float treeY) {
        id++;
        treeModel = assetManager.loadModel(getRandomForestObject());

        randomizeTree(treeY);
        treeModel.setName("Tree" + id);
        return treeModel.clone();
    }

    private void randomizeTree(float treeY) {
        // location
        int xLoc = -150 + random.nextInt(350);
        int zLoc = -160 + random.nextInt(350);
        float yLoc = treeY;

        treeModel.scale(1, 1, 1);
        // size
        float treeHeight = random.nextFloat() * (20f - 10f) + 10f;
        float treeWidth = random.nextFloat() * (20f - 5f) + 5f;
        float treeDepth = random.nextFloat() * (20f - 5f) + 5f;
        treeModel.scale(treeWidth, treeHeight, treeDepth);
        treeModel.setLocalTranslation(xLoc, yLoc, zLoc);
        float r = FastMath.DEG_TO_RAD * random.nextInt(180); // convert 45 degrees to radians
        // treeModel.rotate(r, 0.0f, 0.0f);                // relative rotation around x axis
        treeModel.rotate(0.0f, r, 0.0f);                // relative rotation around y axis
    }
}
