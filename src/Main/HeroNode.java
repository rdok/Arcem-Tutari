/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import project.Creep.Creep;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.input.ChaseCamera;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import java.util.List;
import java.util.Random;
import project.camera.HeroCamera;
import project.environment.Castle;
import project.environment.ExplosionEffect;
import project.environment.FireEffect;
import project.environment.SceneNode;
import project.sounds.SoundNode;

/**
 *
 * @author Riza
 */
public class HeroNode extends Node implements ActionListener, AnalogListener, AnimEventListener {

    private CharacterControl characterControl;
    private Spatial heroModel;
    private AnimChannel animationChannel;
    private AnimChannel shootingChannel;
    private AnimControl animationControl;
    private boolean left = false, right = false, up = false, down = false;
    private Vector3f walkDirection = new Vector3f();
    private Camera cam;
    private float airTime = 0;
    private InputManager inputManager;
    private final AssetManager assetManager;
    private final AppSettings settings;
    private final Node rootNode;
    private Main mainClass;
    private FlyByCamera flyCam;
    private MouseInput mouseInput;
    private ChaseCamera chaseCam;
    private BitmapFont guiFont;
    private Node guiNode;
    private HeroCamera characterCamera;
    private float mouselookSpeed = FastMath.PI;
    private boolean attacking, attack;
    private final String STAND_ANIM = "stand";
    private final String WALK_ANIM = "Walk";
    private final String PUSH_ANIM = "push";
    private final String JUMP_ANIM = "Dodge"; // jump
    private float stepSize = .5f;
    private float heroSpeed = .15f;
    private RigidBodyControl sceneBody;
    private Creep markCreep;
    private Node explosionNode;
    private float camDir_SPEED = .1f;
    private RigidBodyControl castleRigidBC;
    private int explosionSizePerformance = 20; // every 20 explosions appy 
//	Dodge
//Walk
//stand
//INFO: Child (null) attached to this node (Gui Node)
//pull
//push
    private float jumpSpeed = 15;
    private float fallSpeed = 20;
    private float gravity = 25;
    private final SceneNode sceneNode;
    private SoundNode soundNode;
    private RenderManager renderManager;
    private ExplosionEffect explossionEffect;
    private SpotLight spot;
    private boolean flashLight_Enabled = true;
    private float FLASH_LIGHT_RANGE = 150;
    private boolean sprint;
    private float WALK_SPEED = .2f;
    private float RUN_SPEED = .5f;
    private Castle castleNode;
    private final Random random;
    private int health;
    private final FireEffect fireControl;
    private Node fireAllFires; // fires started from here
    //private final Vector3f castleLoc;
    private float startSizeFire;
    private float endSizeFire;
    private List<Node> treeList;
    private float distanceBtnHeroAndObject;
    private final Vector3f castleLoc;

    public HeroNode(RenderManager renderManager, SoundNode soundNode, AssetManager assetManager, Spatial nodeModel, Node rootNode, Main aThis, FlyByCamera flyCam, MouseInput mouseInput, ChaseCamera chaseCam, Camera cam, BitmapFont guiFont, Node guiNode, AppSettings settings, InputManager inputManager, SceneNode sceneNode, FireEffect fireControl) {

        // managers
        super();
        this.renderManager = renderManager;
        this.cam = cam;
        this.assetManager = assetManager;
        this.settings = settings;
        this.heroModel = (Node) nodeModel;
        this.rootNode = rootNode;
        this.mainClass = aThis;
        this.inputManager = inputManager;
        this.flyCam = flyCam;
        this.mouseInput = mouseInput;
        this.chaseCam = chaseCam;
        this.cam = cam;
        this.guiFont = guiFont;
        this.guiNode = guiNode;
        this.sceneNode = sceneNode;
        this.soundNode = soundNode;

        // explosions 
        explossionEffect = new ExplosionEffect(mainClass, assetManager, renderManager, cam, markCreep);
        explosionNode = new Node("explosion from hero");
        explossionEffect = new ExplosionEffect(mainClass, assetManager, renderManager, cam, markCreep);
        rootNode.attachChild(explosionNode);

        castleNode = sceneNode.getCastleNode();
        castleLoc = mainClass.getSceneNode().getCastleNode().getLastKnownLocCastle();
        random = new Random();
        this.fireControl = fireControl;
        // health
        this.health = 100;

        createHero();
        setupKeys();
        //setupAnimationController();
        createFlashLight();
        finalizeHero(); // physical location, attahch to rootNode

    }

    private void createHero() {

        // camera
        characterCamera = new HeroCamera("CamNode", cam, this);
        flyCam.setEnabled(false);
        float r = FastMath.DEG_TO_RAD * 180f;          // convert 45 degrees to radians
        heroModel.rotate(0.0f, r, 0.0f);
        heroModel.setLocalScale(.8f);
        //heroModel.setLocalTranslation(0f, 0f, 0f);
        attachChild(heroModel);

        CapsuleCollisionShape playerShape = new CapsuleCollisionShape(2.8f, 2.2f);
        characterControl = new CharacterControl(playerShape, stepSize);
        characterControl.setJumpSpeed(jumpSpeed);
        characterControl.setFallSpeed(fallSpeed);
        characterControl.setGravity(gravity);
        addControl(characterControl);

        //characterControl.setPhysicsLocation(new Vector3f(100, 100, 100));
        // animation of hero
        animationControl = heroModel.getControl(AnimControl.class);
        animationControl.addListener(this);
        animationChannel = animationControl.createChannel();
        animationChannel.setAnim(STAND_ANIM);


    }

    public boolean isLeft() {
        return left;
    }

    public void setRight(boolean isRight) {
        right = isRight;
    }

    public void setLeft(boolean isLeft) {
        left = isLeft;
    }

    public CharacterControl getCharacterControl() {
        return characterControl;
    }

    public Spatial getModel() {
        return heroModel;
    }

    private void setupKeys() {
        // mouse mappings
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("FlashLight", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("Sprint", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("Debugger", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("Music", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("push", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("TurnLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("TurnRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("MouselookDown", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("MouselookUp", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("WheelUp", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("WheelDown", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "push");
        inputManager.addListener(this, "TurnLeft");
        inputManager.addListener(this, "TurnRight");
        inputManager.addListener(this, "MouselookDown");
        inputManager.addListener(this, "MouselookUp");
        inputManager.addListener(this, "FlashLight");
        inputManager.addListener(this, "Sprint");
        inputManager.addListener(this, "Debugger");
        inputManager.addListener(this, "Music");
        inputManager.addListener(this, "WheelUp");
        inputManager.addListener(this, "WheelDown");
        inputManager.addListener(this, "Exit");



        // shooting maps.
        inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "Shoot");

    }
    private ActionListener actionListener = new ActionListener() {
        private Node explosion;
        private Vector3f offSet3f;

        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Shoot") && !isPressed) {
                checkShootingCollisions();
                soundNode.playSound(soundNode.getShootNode(), .3f, 1, false); // node to play, volume, pithc, loop
            }
        }

        private void checkShootingCollisions() {
            // result list set
            CollisionResults results = new CollisionResults();
            // Aim the ray from cam loc to cam direction
            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
            // collect intersections between ray and shootables in results list
            mainClass.getShootables().collideWith(ray, results);

            // hit ray hit anything collsion
            if (results.size() > 0) {
                CollisionResult closest = results.getClosestCollision();
                Spatial s = closest.getGeometry();
                Vector3f contactPointClosestHitableObject = closest.getContactPoint();

                if (parentIsSinbad(s)) {
                    attackCreep(s, contactPointClosestHitableObject);

                } else if (parentIsCastle(s)) {
                    castleDamageProcess(contactPointClosestHitableObject);
                } else if (anyTreeEquals(s.getParent().getName())) {
                    createFireAt(contactPointClosestHitableObject);
                    createFaslEplostion(closest.getContactPoint());

                } else { // if (s.getName().equals("terrain-Helms DeepQuad1Quad4Patch4"))
                    //    System.out.println(s.getParent().getName());

                    createFaslEplostion(closest.getContactPoint());
                }


            }
        }

        private boolean parentIsSinbad(Spatial s) {
            return (s.getName().equals("Sinbad-geom-2") || s.getName().equals("Sinbad-geom-7") || s.getName().equals("Sinbad-geom-5")) ? true : false;
        }

        /**
         * @param s Spatial that a collision detection occured
         * @param contactPoint Vector3f of the collision detection
         *
         * takes grandparent of 's', finds the correspoinding 'Creep' class
         * creates explosion on given point, having it substracted
         */
        private void attackCreep(Spatial s, Vector3f contactPoint) {
            markCreep = (Creep) s.getParent().getParent();
            offSet3f = markCreep.getCreepModel().getWorldBound().getCenter();
            //contactPoint.subtract(contactPoint.subtract(offSet3f));
            explosionNode.attachChild(explossionEffect.getFaslExplosion());
            //	explosionNode.setLocalTranslation(contactPoint.subtract(contactPoint.subtract(offSet3f)));
            explosionNode.setLocalTranslation(contactPoint);
            markCreep.setAttacked();

        }

        private boolean parentIsCastle(Spatial s) {
            return s.getParent().getName().equals("Plane.006-ogremesh") ? true : false;
        }

        private void castleDamageProcess(Vector3f closest) {
            castleNode.lowerHealthBy(10);

            if (castleNode.getHealth() < 0) {
                createManySlowEXplosions(); // around theh castle
            }
            createFaslEplostion(closest);


            // lower health of castle
            //System.out.println("Castle Health: " + castleNode.getHealth());
        }

        private void createFaslEplostion(Vector3f closest) {
            updatePerformance();
            // explosion on surface of castle
            castleRigidBC = mainClass.getSceneNode().getCastleNode().getCastleRigidBC();
            // explosion on castle
            explosion = new Node("watchtower_explosion");
            explosion.setLocalTranslation(closest);
            explossionEffect = new ExplosionEffect(mainClass, assetManager, renderManager, cam, explosion);
            explosion.attachChild(explossionEffect.getFaslExplosion());
            rootNode.attachChild(explosion);
        }

        private void createFireAt(Vector3f closestPoint) {
            startSizeFire = 1 + random.nextFloat() * 2;
            endSizeFire = 1 + random.nextFloat() * 2;

            fireControl.addFireOnNoHealthObj(closestPoint, startSizeFire, endSizeFire);
        }

        private boolean anyTreeEquals(String possTree) {
            return possTree.equals("Tree-ogremesh") || possTree.equals("Bush_High-ogremesh");
        }
    };

    public Spatial getSpatialNode() {
        return heroModel;
    }

    public AnimChannel getAnimationChannel() {
        return animationChannel;
    }

    public AnimChannel getShootingChannel() {
        return shootingChannel;
    }

    private void createFlashLight() {
        spot = new SpotLight();
        spot.setSpotRange(100f); // distance
        spot.setSpotInnerAngle(25f * FastMath.DEG_TO_RAD); //inner light cone (central beam)
        spot.setSpotOuterAngle(30f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spot.setColor(ColorRGBA.White.mult(1.3f)); // light color
        spot.setPosition(this.getLocalTranslation()); // shine from location
        spot.setDirection(characterCamera.getCameraNode().getCamera().getDirection());


        rootNode.addLight(spot);
    }

    public void update(float tpf) {
        updateFlashLightLocation();
        updateCameraAndMovement();
        handleAnimations();
        updateSnowStormLocation(); // based on 
        // update explosion
        updateExplosions(tpf);
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Left")) {
            left = isPressed;
        } else if (name.equals("Right")) {
            right = isPressed;
        } else if (name.equals("Up")) {
            up = isPressed;
        } else if (name.equals("Down")) {
            down = isPressed;
        } else if (name.equals("Jump")) {
            if (characterControl.onGround()) {
                characterControl.jump();
                if (!attacking) {
                    animationChannel.setAnim(JUMP_ANIM, .3f);
                    animationChannel.setLoopMode(LoopMode.DontLoop);
                }
            }
        } else if (name.equals("push")) {
            attack = isPressed;
        } else if (name.equals("FlashLight") && !isPressed) {
            swapFlashLight();
        } else if (name.equals("Sprint") && !isPressed) {
            sprint = false;
        } else if (name.equals("Sprint") && isPressed) {
            sprint = true;
        } else if (name.equals("Debugger") && !isPressed) {
            mainClass.swapDebugger();
        } else if (name.equals("WheelUp")) {
            //wheelUp = true;
            characterCamera.increaseFollowDistance();
        } else if (name.equals("WheelDown")) {
            characterCamera.decreaseFollowDistance();
        } else if (name.equals("Exit") && isPressed) {
            mainClass.destroy();
        }
    }

    public void onAnalog(String name, float value, float tpf) {
        if (name.equals("TurnLeft")) {
            Quaternion turn = new Quaternion();
            turn.fromAngleAxis(mouselookSpeed * value, Vector3f.UNIT_Y);
            characterControl.setViewDirection(turn.mult(characterControl.getViewDirection()));
        } else if (name.equals("TurnRight")) {
            Quaternion turn = new Quaternion();
            turn.fromAngleAxis(-mouselookSpeed * value, Vector3f.UNIT_Y);
            characterControl.setViewDirection(turn.mult(characterControl.getViewDirection()));
        } else if (name.equals("MouselookDown")) {
            characterCamera.verticalRotate(mouselookSpeed * value);
        } else if (name.equals("MouselookUp")) {
            characterCamera.verticalRotate(-mouselookSpeed * value);
        }



    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        if (channel == getShootingChannel()) {
            channel.setAnim("stand");
        }
        if (channel == animationChannel && attacking && animName.equals(PUSH_ANIM)) {
            attacking = false;
        }
        if (channel == animationChannel && animName.equals(JUMP_ANIM)) {
            //	animationChannel.getControl().
        }

        if (channel == animationChannel && animName.equals(WALK_ANIM)) {
            //	animationChannel.getControl().
        }

    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

    private void handleAnimations() {
        if (attacking) {
            //waiting for attack animation to finish
        } else if (attack) {

            animationChannel.setAnim(PUSH_ANIM, .3f);
            animationChannel.setLoopMode(LoopMode.DontLoop);
            attack = false;
            attacking = true;
        } else {
            if (characterControl.onGround()) {
                if (left || right || up || down) {
                    if (!animationChannel.getAnimationName().equals(WALK_ANIM)) {
                        animationChannel.setAnim(WALK_ANIM, .3f);
                        animationChannel.setLoopMode(LoopMode.Loop);
                        soundNode.playSound(soundNode.getStepNode(), .5f, 0.9f, true);

                    } else { // if it's playing.
                        soundNode.playSound(soundNode.getStepNode(), .5f, 0.9f, true);

                    }
                    if (sprint) {
                        animationChannel.setSpeed(1f);
                    }
                    //System.out.println("sprint" + sprint);
                } else {
                    if (!animationChannel.getAnimationName().equals(STAND_ANIM)) {
                        animationChannel.setAnim(STAND_ANIM, .3f);
                        animationChannel.setLoopMode(LoopMode.Cycle);
                    }
                }
            } else {
            }
        }

    }

    public CharacterControl getCameraNode() {
        return characterControl;
    }

    public HeroCamera getCameraTrack() {
        return characterCamera;
    }

    private void updateFlashLightLocation() {

        spot.setPosition(this.getLocalTranslation()); // shine from location
        spot.setDirection(characterCamera.getCameraNode().getCamera().getDirection());

    }

    private void swapFlashLight() {
        if (flashLight_Enabled) {
            flashLight_Enabled = false;
            spot.setSpotRange(.0001f);
            //   rootNode.detachChild(spot);
        } else {
            flashLight_Enabled = true;
            spot.setSpotRange(FLASH_LIGHT_RANGE);
        }
    }

    private void finalizeHero() {
        getCharacterControl().setPhysicsLocation(new Vector3f(90, 20, -200));
        rootNode.attachChild(this);
    }

    public Vector3f getHeroLoc3f() {
        return getCharacterControl().getPhysicsLocation();
    }

    public void createManySlowEXplosions() {
    }

    /**
     * @return the health
     */
    public int getHealth() {
        return health;
    }

    /**
     * @param health the health to set
     */
    public void setHealth(int health) {
        this.health = health;
    }

    public void modifyHealthBy(int health) {
        this.health += health;
    }

    private void updateCameraAndMovement() {
        Vector3f camDir = cam.getDirection().clone().multLocal(camDir_SPEED); //speed
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.1f);
        camDir.y = 0;
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);

        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }

        if (sprint) {
            if (up || right || left) {
                heroSpeed = RUN_SPEED;
            } else if (down) {
                heroSpeed = RUN_SPEED / 2;
            }
        } else {
            heroSpeed = WALK_SPEED;
        }


        characterControl.setWalkDirection(walkDirection.normalize().multLocal(heroSpeed));
    }

    private void updateSnowStormLocation() {
        sceneNode.moveSnowStorm(getCharacterControl().getPhysicsLocation());
    }

    private void updateExplosions(float tpf) {
        if (explossionEffect.hasStarted()) {
            explossionEffect.fastExplosionUpdate(tpf);
        }
    }

    private void updatePerformance() {
        if (explosionSizePerformance < 0) {
            explosionNode.detachAllChildren();
            explosionSizePerformance = 100;
        } else {
            explosionSizePerformance--;
        }
        //	explosionNode = new Node("explosion Node");
    }
}
