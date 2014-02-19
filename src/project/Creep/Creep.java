/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.Creep;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.input.InputManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Random;
import Main.Main;
import java.util.ArrayList;

/**
 *
 * @author Riza
 */
public class Creep extends Node {

    private final Spatial creep;
    private CharacterControl creepControl;
    private float stepSize = .05f;
    private float jumpSpeed = 15;
    private float fallSpeed = 20;
    private float gravity = 25;
    private MotionPath path;
    private final Main mainClass;
    private final Node rootNode;
    private final AssetManager assetManager;
    private MotionEvent motionControl;
    private Vector3f heroPhyLocation;
    private Vector3f creepPhysLoc;
    private float PROXIMITY_TO_HERO = 5.0f;
    private boolean pursuitCastle;
    private Vector3f walkCreep = new Vector3f();
    private Random random = new Random();
    private Vector3f castlePhyLocation;
    private float PROXOMITY_TO_CASTLE = 30;
    private CapsuleCollisionShape creepShape;
    //private final CreepCollisionsListener creepCollisionsListener;
    private boolean pursuitHero;
    private float movementSpeed = 0.002f;
    private boolean heroHitsCreep;
    private float scaleModelSize;
    private float scaleHeight;
    private float scaleRadious;
    private int health;
    private boolean creepAttacks;
    private boolean attacksHero;
    private boolean attacksCastle;
    private final int MIN_HEALTH = 1;
    private boolean isRunning;
    private float PROXIMITY_TO_TARGET = 10;
    private Vector3f followTarget3f;
    private float DIFFICULTY_LEVEL;
    private final CreepAnimControl animControl;
    private final float CASTLE_HEIGHT = 58f;
    private boolean isAttacked;
    private boolean isInsideCastle;
    private boolean firstPhaseComplete;
    private boolean path1ToBridge;
    private boolean path2CrossBridge;
    private boolean path4ToHero;
    private boolean path3ToCastle;
    private boolean randomPaths;
    private ArrayList<Float> pathsList;
    private ArrayList<Vector3f> pathsList3f;
    private boolean output;
    private float PROXIMITY_TO_PATH = 10;
    private CreepLocState stateLocation = CreepLocState.CASTLE_OUTSIDE;
    private float height;

    public enum CreepLocState {

        CASTLE_OUTSIDE, CASTLE_INSIDE, CASTLE_BRIDGE
    }

    public Creep(Spatial creep, InputManager inputManager, int creepID, Main mainClass, AssetManager assetManager, Node rootNode) {
        super();
        this.name = "creep" + creepID;
        this.creep = creep;
        //this.inputManager = inputManager;
        this.mainClass = mainClass;
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        creep.setName(name);

        // level of difficult. the thigher the difficulty, the more these numbers incrase (not implemented lvl)
        setDifficultyParam(); // health, speed, and damage creep does
        createCreep();

        animControl = new CreepAnimControl(this); // register animations

        finalizeCreep(); // set to random location and attach it

        path1ToBridge = true;
        setPaths();
        followTarget3f = pathsList3f.get(0);

    }

    // creep is attacked by hero
    public void setAttacked() {
        lowerHealth();
        enableCreepIsAttacked();
//        if (creepIsAlive()) {
//            enableHeroPursuit();
//        } else { // if creeep is alive
//            disableAllPursuit();
//            mainClass.removePhysicalProp(this);
//        }
    }

    private CharacterControl getCharacterControl() {
        return creepControl;
    }

    private Spatial getModel() {
        return creep;
    }

    private void createCreep() {
        scaleModelSize = random.nextFloat() * 1f + .3f;
        scaleHeight = scaleModelSize * 4;
        scaleRadious = scaleModelSize * 3;
        creep.setLocalScale(scaleModelSize);
        attachChild(creep);

        creepShape = new CapsuleCollisionShape(scaleRadious, scaleHeight); // creep-specific
        creepControl = new CharacterControl(creepShape, stepSize);
        creepControl.setJumpSpeed(jumpSpeed);
        creepControl.setFallSpeed(fallSpeed);
        creepControl.setGravity(gravity);
        this.addControl(creepControl);
    }

    public void startSimplePath() {
        path = new MotionPath();

        Vector3f creepLocation = getPhysLoc();
//      Vector3f towerLocation = mainClass.getSceneNode().getCastleNode().getC
//      towerLocation.setY(creepLocation.y);
//      // float groundHeight = mainClass.getSceneNode().getRigBodControl().get
//      path.addWayPoint(creepLocation);
//      path.addWayPoint(towerLocation);
        path.enableDebugShape(assetManager, rootNode);
        path.setCurveTension(1.0f);


        path.addListener(new MotionPathListener() {
            public void onWayPointReach(MotionEvent motionControl, int wayPointIndex) {
            }
        });

        motionControl = new MotionEvent(this, path);
        motionControl.setSpeed(1f);
        motionControl.play();
        // motionControl.
    }

    public Vector3f getPhysLoc() {
        return getCharacterControl().getPhysicsLocation();
    }

    private void pursuitHero(float tpf) {
        walkCreep.set(0, 0, 0);
        // animationChannelNPC3.setAnim("Walk");
        creepPhysLoc = getPhysLoc();
        heroPhyLocation = mainClass.getHeroPhyLocat();
        //  creepControl.setViewDirection(heroPhyLocation); //to use with physic based characters
        if (creepPhysLoc.distance(heroPhyLocation) < PROXIMITY_TO_HERO) {
            walkCreep = new Vector3f(0f, 0f, 0f);
            //    pursuitHero = false;
            animControl.heroAttacked();
        } else {
            heroPhyLocation.setY(creepPhysLoc.getY());
            walkCreep = heroPhyLocation.subtract(creepPhysLoc).multLocal(0.01f);
            creepControl.setViewDirection(walkCreep.mult(-0.1f));
            animControl.enableAnimationRun();
        }
        creepControl.setWalkDirection(walkCreep);
    }

    public void pursuitCastle(float tpf) {
        walkCreep.set(0, 0, 0);

        if (isFollowingCasle()) {
            // animationChannelNPC3.setAnim("Walk");
            creepPhysLoc = getPhysLoc();
            castlePhyLocation = mainClass.getSceneNode().getCastleNode().getLastKnownLocCastle();
            //  creepControl.setViewDirection(heroPhyLocation); //to use with physic based characters
            if (creepPhysLoc.distance(castlePhyLocation) < PROXOMITY_TO_CASTLE) {
                walkCreep = new Vector3f(0f, 0f, 0f);
                //    pursuitHero = false;
                //	attackHero();
                castleAttacked();
            } else {
//				if (!animChannel.getAnimationName().equals("RunBase")) {
//					animChannel.setAnim("RunBase");
//				}
                walkCreep = castlePhyLocation.subtract(creepPhysLoc).multLocal(movementSpeed);
                creepControl.setViewDirection(walkCreep.mult(-0.1f));
                enableFollowCastle();
                enableRunning();
            }
            creepControl.setWalkDirection(walkCreep);
        }

    }

    /**
     * a function that makes the creep go at loc. returns true when creep
     * reaches target (proximity)
     *
     * @param loc
     */
    public boolean goToLoc(Vector3f loc3f) {

        creepPhysLoc = getPhysLoc();
        //System.out.println("current loc: " + creepPhysLoc + ". target loc: " + loc3f);
        if (path3ToCastle) { // substract 
            PROXIMITY_TO_TARGET = PROXOMITY_TO_CASTLE;
        } else {
            PROXIMITY_TO_TARGET = PROXIMITY_TO_PATH;
        }
        walkCreep.set(0, 0, 0);
        if (creepPhysLoc.distance(loc3f) > PROXIMITY_TO_TARGET) {
            // if creep is not at give location
            walkCreep = loc3f.subtract(creepPhysLoc).multLocal(movementSpeed);
            creepControl.setViewDirection(walkCreep.mult(-0.1f));
            output = false;
        } else { // reached target
            isRunning = false;
            updatePaths();
            output = true;
            if (path3ToCastle) {
                PROXIMITY_TO_TARGET = PROXIMITY_TO_PATH;
            }
            stopCreep();
        }
        creepControl.setWalkDirection(walkCreep);

        return output;
    }

    private void moveRandomLocCreep() {
        float randX = 0 + random.nextInt(150);
        float randZ = 100 - random.nextInt(20);

        getCharacterControl().setPhysicsLocation(new Vector3f(randX, 3f, randZ));
        float r = FastMath.DEG_TO_RAD * 180;
        getModel().rotate(0, r, 0);
    }

    public Spatial getCreepModel() {
        return creep;
    }

    public void update(float tpf) {
        // checks where creep is.
        //  updatePosition(); // outside, bridge, inside castles POSITION
   //     System.out.println(stateLocation);
        switch (stateLocation) {
            case CASTLE_OUTSIDE:
                //   System.out.println("CASTLE_OUTSIDE");

                if (isAttacked) {
                    pursuitHero(tpf);
                }
                break;
            case CASTLE_BRIDGE:
                break;
            case CASTLE_INSIDE:
                break;
        }




//        updateDeatAnimation();
//        animControl.update(tpf);
//        updatePaths();
//        updateMovement(tpf);





    }

    private void updatePosition() {
        height = getPhysLoc().y;
        if (height < -25) {
            stateLocation = CreepLocState.CASTLE_OUTSIDE;
        } else if (height < -6) {
            stateLocation = CreepLocState.CASTLE_BRIDGE;
        } else {
            stateLocation = CreepLocState.CASTLE_INSIDE;
        }
    }

    public void enableHeroPursuit() {
        pursuitCastle = false;
        pursuitHero = true;
    }

    public void enableCastlePursuit() {
        pursuitCastle = true;
        pursuitHero = false;
    }

    private void finalizeCreep() {
        moveRandomLocCreep();
        rootNode.attachChild(this);
    }

    private void disableAllPursuit() {
        pursuitCastle = false;
        pursuitHero = false;
        stopCreep();

    }

    private void lowerHealth() {
        health -= 10;
    }

    private boolean creepIsAlive() {
        return getHealth() > 0 ? true : false;
    }

    private void updateDeatAnimation() {
        // we put the expression "random.nextFloat() * 0.02f + 0.1f" on the second 
        // checker since java checks said checker only if first checker is true
        // We also did not store the above expression in order to avoid
        // do unecessary math, and lower processig power.
        if (!creepIsAlive() && animControl.getChannelDead().getTime() > (random.nextFloat() * 0.02f + 0.1f)) {
            animControl.getAnimControl().setEnabled(false);
        }
        if (!isRunning) {
            animControl.enableAnimIdle();

        } else {
            animControl.enableAnimationRun();
        }

    }

    private void stopCreep() {
        walkCreep.set(0, 0, 0); // stops creep
        creepControl.setWalkDirection(walkCreep);
        isRunning = false;
    }

    private void castleAttacked() {
        attacksCastle = true;
        attacksHero = false;
    }

    public void attackCastle() {
        mainClass.getSceneNode().getCastleNode().lowerHealthBy(10);
    }

    private boolean isFollowingCasle() {
        return pursuitCastle;
    }

    private void enableFollowCastle() {
        pursuitCastle = true;

    }

    // cheks if creep is attacking. used by creepAnimControl
    public boolean isAttacking() {
        if (attacksCastle || attacksHero) {
            return true;
        }
        return false;
    }

    // if creep is running. used by animationn
    public boolean isRunning() {
        return isRunning;
    }

    public boolean isAlive() {
        return getHealth() > 0 ? true : false;
    }

    /**
     * @return the health
     */
    public int getHealth() {
        return health;
    }

    private void enableRunning() {
        isRunning = true;
    }

    /**
     * castle is above y.
     *
     * @return
     */
    private boolean isInsideCastle() {
        if (getPhysLoc().y > CASTLE_HEIGHT) {
            enableCastlePursuit();
            return true;
        }
        return false;
    }

    private void setDifficultyParam() {
        DIFFICULTY_LEVEL = random.nextFloat() * (0.01f - 0.003f) + .003f;
        health = MIN_HEALTH + random.nextInt((int) (DIFFICULTY_LEVEL * 1000f)); // min 1.1, max 100.1
        movementSpeed = DIFFICULTY_LEVEL;

    }

    private void enableCreepIsAttacked() {
        isAttacked = true;

//        disableAllPaths();
//        path4ToHero = true;
    }

    public Vector3f getPhysLoc3f() {
        return creepPhysLoc;
    }

    private void updateMovement(float tpf) {

        if (isPath1ToBridge()) { // if path1 is active (outside coustle)
            if (goToLoc(followTarget3f)) {
                path1ToBridge = false;
                path2CrossBridge = true;
            }
            enableRunning();
        } else if (isPath2CrossBridge()) {
            // System.out.println("Path2 is active");
            if (goToLoc(followTarget3f)) {
                path2CrossBridge = false;
                pursuitCastle = true;
            }
            enableRunning();
        } else if (isPursuitCaslte()) {
            pursuitCastle(tpf);
        }
    }

    private boolean isPath1ToBridge() {
        return path1ToBridge;
    }

    private boolean isPath2CrossBridge() {
        return path2CrossBridge;
    }

    private void updatePaths() {

        if (path1ToBridge) {
            path2CrossBridge = false;
            path3ToCastle = false;
            path4ToHero = false;
            followTarget3f = pathsList3f.get(0);

        } else if (path2CrossBridge) {
            path1ToBridge = false;
            path3ToCastle = false;
            path4ToHero = false;
            followTarget3f = pathsList3f.get(1);
        } else if (path3ToCastle) {
            followTarget3f = mainClass.getSceneNode().getCastleNode().getLastKnownLocCastle();
            path1ToBridge = false;
            path2CrossBridge = false;
            path4ToHero = false;
        } else if (path4ToHero) {
            followTarget3f = mainClass.getHeroPhyLocat();
            path1ToBridge = false;
            path2CrossBridge = false;
            path3ToCastle = false;
        }
    }

    /**
     * 1st element, path to bridge. 2nd element, path to corss bridge.
     */
    private void setPaths() {
        randomPaths = random.nextBoolean();
        pathsList3f = new ArrayList<Vector3f>();

        if (randomPaths) { // left side
            pathsList3f.add(new Vector3f(21.019234f, -30.047817f, -208.1716f));
            pathsList3f.add(new Vector3f(103.39955f, -6.0646944f, -233.07976f));
        } else { // right side
            // 50.919415, 6.542686, -158.60593
            pathsList3f.add(new Vector3f(133.62988f, -31.126305f, -126.82768f));
            pathsList3f.add(new Vector3f(126.93414f, -6.012473f, -184.06625f));
        }
    }

    private boolean isPursuitCaslte() {
        return pursuitCastle;
    }

    public boolean isAnimDeadActive() {
        return animControl.getChannelDead().getAnimationName() == null ? false : true;
    }

    private void disableAllPaths() {
        path1ToBridge = false;
        path2CrossBridge = false;
        path3ToCastle = false;
        path4ToHero = false;
    }
}
