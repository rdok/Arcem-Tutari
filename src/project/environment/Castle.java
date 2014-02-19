/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.environment;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.Random;
import Main.Main;
import Main.ExplosionThread;
import project.sounds.FreeTTS;

/**
 *
 * @author Riz
 */
public class Castle extends Node {

    private final Spatial castleSpatial;
    private final Node rootNode;
    private CollisionShape collisionCastleShape;
    private RigidBodyControl rigidBCcastle;
    private int health;
    private FireEffect fireEffect;
    private final AssetManager assetManager;
    private final Random random;
    private int totalFires = 10;
    private boolean addFire;
    private final RenderManager renderManager;
    private final Camera cam;
    private boolean castleIsActive;
    // private ArrayList<FireEffect> fireList;
    private final Main mainClass;
    private Vector3f lastKnownCAstleLocation;
    private boolean newExplosion;
    private ExplosionEffect explossionEffect;
    private int numbersOfExplosion;
    private ArrayList<ExplosionEffect> explosionsList;
    private int TOT_EXPLOSIONS = 10;
    private FireEffect fireControl;
    private Node explosionNode;
    private Vector3f location3f;
    private String WARNING_CASTLE_UNDER_ATTACK = "Warning! Your casle is under attack!";
    private FreeTTS freeTTS;
    private ExplosionThread explosionThread;
    private boolean castleIsDetached;

   
    public Castle(Spatial castleSpatial, Node rootNode, AssetManager assetManager, RenderManager renderManager, Camera cam, Main mainClass, FireEffect fireEffect, FreeTTS freeFreeTTS) {
        this.castleSpatial = castleSpatial;
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.renderManager = renderManager;
        this.cam = cam;
        this.mainClass = mainClass;
        this.freeTTS = freeFreeTTS;
        this.explosionThread = mainClass.getExplosionThread();
        fireControl = new FireEffect(rootNode, assetManager);

        totalFires = 0;
        addFire = false;
        random = new Random();

        explosionsList = new ArrayList<ExplosionEffect>();
        this.fireEffect = fireEffect;

        initModel();
        initStats();

    }

    public RigidBodyControl getCastleRigidBC() {
        return rigidBCcastle;
    }

    public Spatial getCastleSpatial() {
        return castleSpatial;
    }

    private void initModel() {

        castleSpatial.setLocalScale(3, 3, 3);
        rootNode.attachChild(castleSpatial);
        castleIsActive = true;

        //  aThis.getPhysicsSpace().add(tower);
        collisionCastleShape = CollisionShapeFactory.createMeshShape((Node) castleSpatial);
        rigidBCcastle = new RigidBodyControl(collisionCastleShape, 0);

        //getSceneShape().
        castleSpatial.addControl(rigidBCcastle);

        //  aThis.getPhysicsSpac
        rigidBCcastle.setPhysicsLocation(new Vector3f(180, -10, -213));
        lastKnownCAstleLocation = rigidBCcastle.getPhysicsLocation();

        // rotate it
        Quaternion q1 = new Quaternion();
        q1.fromAngleAxis(110 * FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
        rigidBCcastle.setPhysicsRotation(q1);
        // this.getTowerControl()   }
    }

    private void initStats() {
        health = 1000;
    }

    public void lowerHealthBy(int health) {
        this.health -= health;
        addFire = true;
        freeTTS.startSpeech(WARNING_CASTLE_UNDER_ATTACK);
    }

    public int getHealth() {
        return health;
    }

    public void update(float tpf) {
        updateSpeech();

        if (!isAlive()) {

            startFinalExplosionsThread(tpf);

            //           castleIsActive = false;
//            castleSpatial.getParent().detachChild(castleSpatial);
            //  fireControl.removeCastleFires();


        } else {
            createFires();

        }
    }

    private void initFire(int totFires) {
        float randX;
        float randY;
        float randZ;
        totFires = 5;
        int startSize = 2;
        int endSize = 2;
        // fireEffect = new FireEffect(rootNode, assetManager, location, 2, 2);
        for (int i = 0; i < totFires; i++) {

            location3f = rigidBCcastle.getPhysicsLocation();
            randX = 20 + getRandomSign() * 25 - random.nextInt(35);
            randX /= 2;
            randY = random.nextInt(35);
            randZ = 60 + getRandomSign() * 25 - random.nextInt(100);
            randZ /= 2;
            location3f.setX(location3f.getX() + randX);
            location3f.setY(location3f.getY() + randY);
            location3f.setZ(location3f.getZ() + randZ);


            fireEffect.addFireOnCastle(location3f, startSize, endSize);
        }

    }

    private int getRandomSign() {
        return random.nextBoolean() ? -1 : 1;
    }

    public final Vector3f getLastKnownLocCastle() {
        return lastKnownCAstleLocation;
    }

    private void startFinalExplosionsThread(float tpf) {
        if (!explosionThread.hasStarted()) {
            explosionThread.start();

        } else { // explosions has started
            if (!castleIsDetached) {
                // System.out.println("Created explosion");
                // disableNewExplosion();
                // explosion on castle
                //   newExplosionOnCastle();
                if (explosionsList.size() < numbersOfExplosion) {
                    newExplosionOnCastle();

                }
                if (explosionThread.isAlive()) {
                    updateExplosions(tpf);

                } else {
                    explosionNode.detachAllChildren();
                    rootNode.detachChild(explosionNode);
                    rootNode.detachChild(getCastleSpatial());
                    mainClass.removeFromShootables(castleSpatial);
                    mainClass.removePhysicalProp(getCastleSpatial());
                    System.out.println("explosion thread dead.");
                    fireEffect.setCastleDetached();
                    castleIsDetached = true;
                }
            }
        }

    }

    public void addExplosion() {
        numbersOfExplosion++;
    }

    public boolean explosionThreadAlive() {
        return explosionThread.isAlive();
    }

    private void disableNewExplosion() {
        newExplosion = false;
    }

    private void createFires() {
        if (addFire && castleIsActive) {
            initFire((totalFires - health / 10) * 10);
            addFire = false;
        }

    }

    private boolean explosionsAreDone() {
        if (numbersOfExplosion > TOT_EXPLOSIONS) {
            for (int i = 0; i < explosionsList.size(); i++) {
                if (explosionsList.get(i).getTime() < 5) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private void newExplosionOnCastle() {
        explosionNode = new Node("final_CastleExplosions");
        explosionNode.setLocalTranslation(getRandomLocRelativeToCastle());
        explossionEffect = new ExplosionEffect(mainClass, assetManager, renderManager, cam, explosionNode);
        explosionsList.add(explossionEffect);
        explosionNode.attachChild(explosionsList.get(explosionsList.size() - 1).getBigExplostion());
        rootNode.attachChild(explosionNode);
    }

    private void updateExplosions(float tpf) {
        for (int i = 0; i < explosionsList.size(); i++) {
            explosionsList.get(i).bigExplosionUpdate(tpf);
        }
    }

    private Vector3f getRandomLocRelativeToCastle() {
        float xLoc = lastKnownCAstleLocation.x + getRandomSign() * (random.nextInt(20) + 20);
        float yLoc = lastKnownCAstleLocation.y + random.nextInt(40);
        float zLoc = lastKnownCAstleLocation.z + getRandomSign() * (random.nextInt(20) + 20);
        return new Vector3f(xLoc, yLoc, zLoc);
    }

    public boolean isAlive() {
        return getHealth() > 0 ? true : false;
    }

    private void updateSpeech() {
    }
}
