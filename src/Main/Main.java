/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.ChaseCamera;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.water.SimpleWaterProcessor;
import java.util.ArrayList;
import java.util.Random;
import project.Creep.Creep;
import project.environment.FireEffect;
import project.environment.SceneNode;
import project.environment.SkyDomeControl;
import project.sounds.FreeTTS;
import project.sounds.SoundNode;

/**
 *
 * @author Rizart Dokollari
 */
public class Main extends SimpleApplication {

    private static final String GAME_TITLE = "Arce Aalvos";
    private Geometry lightSphere;
    private int creeStartingNumber = 0;
    private SceneNode sceneNode;
    private HeroNode heroNode;
    private ArrayList<Creep> creepNodesList;
    private BulletAppState bulletAppState;
    private ChaseCamera chaseCam;
    private SoundNode soundNode;
    private Spatial heroModel, creepModel;
    private Node shootables;
    private Random rand = new Random();
    private final int NUM_CREEPS = 3;
    private Creep creepNode;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle(GAME_TITLE); // lating for caslte survival
        settings.setSettingsDialogImage("Interface/splash.png");

        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }
    private boolean DEBUG_MODE;
    private GUI gui;
    private FireEffect fireControl;
    private Creep creep;
    private SimpleWaterProcessor waterProcessor;
    private Node waterSceneNode;
    private Material watMat;
    private Spatial waterPlane;
    private Vector3f lightPos;
    private SkyDomeControl skyDome;
    private FreeTTS freeTTS;
    private ExplosionThread explosionThread;

    /**
     * IMPORTANT: ORDER OF METHOD SHOULD NOT BE CHANGED. INITIALIZATION REASONS.
     */
    @Override
    public void simpleInitApp() {
//        FilterPostProcessor processor = assetManager.loadFilter("Filters/SceneFilter.j3f");
//        getViewPort().addProcessor(processor);


        // hide mouse, speed camera, prepare physics simulation
        mouseInput.setCursorVisible(false);
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(100);

        // shootables (creeps), future walls, etcc.
        shootables = new Node("Shootables");
        rootNode.attachChild(shootables);
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);//        

        // control fires of the game
        fireControl = new FireEffect(rootNode, assetManager);
        // a thread for in-game speech
        initSpeechThread();
        // create a sky with moveable moon and clouds
        createSky(); // done benchamrk,
        // final explosion thread
        explosionThread = new ExplosionThread();
        // create terrain, trees, leafs, sunlight
        sceneNode = new SceneNode(assetManager, rootNode, this, viewPort, renderManager, cam, fireControl, freeTTS); // done benchmark
        bulletAppState.getPhysicsSpace().add(sceneNode.getSceneModel());
        bulletAppState.getPhysicsSpace().add(sceneNode.getCastleNode().getCastleSpatial());
        shootables.attachChild(sceneNode.getCastleNode().getCastleSpatial());
        shootables.attachChild(sceneNode.getSceneModel());
        explosionThread.setStats(sceneNode.getCastleNode().getLastKnownLocCastle(), sceneNode);
        // create a surface of water at height
        createWater();
        soundNode = new SoundNode(sceneNode, assetManager);
        createHero();
        createCreeps();
        initGUI();
//        setDisplayStatView(true);

    }

    @Override
    public void simpleUpdate(float tpf) {
        //	lightSphere.setLocalTranslation(lightPos);
        waterProcessor.setLightPosition(sceneNode.getSunLocation());
        heroNode.update(tpf);
        sceneNode.update(tpf);
        updateCreeps(tpf);
        sceneNode.getCastleNode().update(tpf);
        updateGUI();
        fireControl.update(tpf);
    }

    private Creep createCreepNode() {
        creepModel = assetManager.loadModel("Models/Sinbad/Sinbad.mesh.j3o");
        creep = new Creep(creepModel, inputManager, creeStartingNumber++, this, assetManager, rootNode);
        return creep;
    }

    public PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

    public Node getShootables() {
        return shootables;
    }

    private void createHero() {
        // initialize hero character, add key listeners,  camera, animation, crosshairs.
        heroModel = assetManager.loadModel("Models/Oto/Oto.mesh.j3o");
        heroNode = new HeroNode(renderManager, soundNode, assetManager, heroModel, rootNode, this, flyCam, mouseInput, chaseCam, cam, guiFont, guiNode, settings, inputManager, sceneNode, fireControl);
        bulletAppState.getPhysicsSpace().add(heroNode);
    }

    public HeroNode getHeroNode() {
        return heroNode;
    }

    public SceneNode getSceneNode() {
        return sceneNode;
    }

    public Vector3f getHeroPhyLocat() {
        return heroNode.getCharacterControl().getPhysicsLocation();
    }


    public SoundNode getSoundNode() {
        return soundNode;
    }

    private void createCreeps() {
        creepNodesList = new ArrayList<Creep>();
        for (int i = 0; i < NUM_CREEPS; i++) {
            creepNode = createCreepNode();
            creepNodesList.add(creepNode);
            shootables.attachChild(creepNode);
            // creepNode.startSimplePath();
            bulletAppState.getPhysicsSpace().add(creepNode);
        }
    }

    private void updateCreeps(float tpf) {
        for (int i = 0; i < NUM_CREEPS; i++) {
            creepNodesList.get(i).update(tpf);
            if (!creepNodesList.get(i).isAlive() && creepNode.isAnimDeadActive()) {
                creepNodesList.remove(i);
                creepNode = createCreepNode();
                creepNodesList.add(createCreepNode());
                shootables.attachChild(creepNode);
                // creepNode.startSimplePath();
                bulletAppState.getPhysicsSpace().add(creepNode);
            }
        }
    }

    public void addToShootables(Spatial model) {
        shootables.attachChild(model);
    }

    void swapDebugger() {
        if (DEBUG_MODE) {
            bulletAppState.getPhysicsSpace().disableDebug();
            DEBUG_MODE = false;
        } else {
            bulletAppState.getPhysicsSpace().enableDebug(assetManager); //
            DEBUG_MODE = true;
        }
    }

    private void initGUI() {
        gui = new GUI(getHeroNode(), getSceneNode(), assetManager, this, settings.getHeight(), settings.getWidth());
        guiNode.attachChild(gui.getGUI());
    }

    private void updateGUI() {
        gui.update();
    }

    public Creep getCreepNode(String name) {
        for (int i = 0; i < creepNodesList.size(); i++) {
            if (name.equals(creepNodesList.get(i).getName())) {
                return creepNodesList.get(i);
            }
        }
        return null;
    }

    public void removePhysicalProp(Spatial spat) {
        bulletAppState.getPhysicsSpace().remove(spat);
    }

    public void removeFromShootables(Spatial spat) {
        shootables.detachChild(spat);
    }

    /**
     * All credits of the createSky and SkyDomeControl.java goes to:
     * https://code.google.com/p/jme-glsl-shaders/source/browse/?r=b95ecb902f20f443ddcde47a9c827a9306cc5313#hg%2Fassets%2FShaderBlow%2FTextures%2FSkyDome
     * I take the credit of combining, and making the below code work
     */
    private void createSky() {
        this.assetManager.registerLocator("assets", FileLocator.class);
        skyDome = new SkyDomeControl(assetManager, cam,
                "ShaderBlow/Models/SkyDome/SkyDome.j3o",
                "ShaderBlow/Textures/SkyDome/SkyNight_L.png",
                "ShaderBlow/Textures/SkyDome/Moon_L.png",
                "ShaderBlow/Textures/SkyDome/Clouds_L.png",
                "ShaderBlow/Textures/SkyDome/Fog_Alpha.png");
        Node sky = new Node();
        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        sky.addControl(skyDome);
        sky.setCullHint(Spatial.CullHint.Never);

// Either add a reference to the control for the existing JME fog filter or use the one I posted…
// But… REMEMBER!  If you use JME’s… the sky dome will have fog rendered over it.
// Sorta pointless at that point
//        FogFilter fog = new FogFilter(ColorRGBA.Blue, 0.5f, 10f);
//        skyDome.setFogFilter(fog, viewPort);

// Set some fog colors… or not (defaults are cool)
        skyDome.setFogColor(ColorRGBA.Blue);
        skyDome.setFogNightColor(new ColorRGBA(.5f, .5f, .5f, 1f));
        skyDome.setDaySkyColor(new ColorRGBA(0f, 0f, 0f, 1f));

// Enable the control to modify the fog filter
        skyDome.setControlFog(true);

        //Add the directional light you use for sun… or not
//		DirectionalLight sun = new DirectionalLight();
//		sun.setDirection(new Vector3f(-0.8f, -0.6f, -0.08f).normalizeLocal());
//		sun.setColor(new ColorRGBA(1, 1, 1, 1));
//		rootNode.addLight(sun);
//		skyDome.setSun(sun);

//		AmbientLight al = new AmbientLight();
//		al.setColor(new ColorRGBA(0.7f, 0.7f, 1f, 1.0f));
//		rootNode.addLight(al);

// Set some sunlight day/night colors… or not
//		skyDome.setSunDayLight(new ColorRGBA(1, 1, 1, 1));
//		skyDome.setSunNightLight(new ColorRGBA(0.5f, 0.5f, 0.9f, 1f));

// Enable the control to modify your sunlight
        skyDome.setControlSun(false);

// Enable the control
        skyDome.setEnabled(true);

// Add the skydome to the root… or where ever
        rootNode.attachChild(sky);

    }

    private void createWater() {

        lightPos = new Vector3f(-50, 30, 0);

        // scene of water
        waterSceneNode = new Node("Water Scene Node");
        waterSceneNode.attachChild(skyDome.getSkyModelNode());
        rootNode.attachChild(waterSceneNode);
//		//create processor
        waterProcessor = new SimpleWaterProcessor(assetManager);
        waterProcessor.setReflectionClippingOffset(-50f);
        waterProcessor.setReflectionScene(waterSceneNode);
        waterProcessor.setDebug(false);
        viewPort.addProcessor(waterProcessor);

        waterProcessor.setLightPosition(lightPos);

//        //create water quad
//        waterPlane = waterProcessor.createWaterGeometry(100, 100);
//        waterPlane = (Spatial) assetManager.loadModel("Models/WaterTest/WaterTest.mesh.j3o");
//        waterPlane.setMaterial(waterProcessor.getMaterial());
//        waterPlane.setLocalScale(50);
//        waterPlane.setLocalTranslation(-5, 0, 5);
//        rootNode.attachChild(waterPlane);

        //create water quad
        //waterPlane = waterProcessor.createWaterGeometry(100, 100);
        waterPlane = (Spatial) assetManager.loadModel("Models/WaterTest/WaterTest.mesh.j3o");
        waterPlane.setMaterial(waterProcessor.getMaterial());
        waterPlane.setLocalScale(600);
        waterPlane.setLocalTranslation(00, -35, 100);

        rootNode.attachChild(waterPlane);
    }

    public SkyDomeControl getSkyDomeControl() {
        return skyDome;
    }

    /**
     * @return the freeTTS
     */
    public FreeTTS getFreeTTS() {
        return freeTTS;
    }

    private void initSpeechThread() {
        freeTTS = new FreeTTS();
        freeTTS.start();
    }

    @Override
    public void destroy() {
        super.destroy();
        System.exit(0);
    }

    /**
     * @return the explosionThread
     */
    public ExplosionThread getExplosionThread() {
        return explosionThread;
    }
}