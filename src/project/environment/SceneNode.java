/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.environment;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.light.AmbientLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Random;
import Main.Main;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.SkyFactory;
import project.sounds.FreeTTS;

/**
 *
 * @author Riza
 */
public class SceneNode {

    private Spatial sceneModel;
    private RigidBodyControl rigidBCscene;
    private CollisionShape collisiosceneShape;
    private Node rootNode;
    private final AssetManager assetManager;
    private final Main aThis;
    private final String SCENE_MODEL_LOC = "Scenes/ArenaScene_4.j3o";
    private static int SNOW_STORM_SIZE = 50;
    private static final float SNOW_HEIGHT = 50f;
    private Random rand = new Random();
    private int randomX;
    private int randomZ;
    private int randomY;
    private Node snowStorm;
    private Spatial castleSpatial;
    private CollisionShape collisionTowerShape;
    private RigidBodyControl rigidBCtower;
    private final float SUN_SPEED_MOVEMENT = 1f;
    private SpotLight sunSpotLight;
    private final float MAX_SUN_X = 2000f;
    private int FIRE_SIZE = 100;
    private Spatial treeModel;
    private int FOREST_SIZE = 150;
    private Spatial leafModel;
    private Spatial redRootModel;
    private final ViewPort viewPort;
    private FilterPostProcessor fpp;
    private FogFilter fog;
    private Castle castleNode;
    private final Camera cam;
    private final RenderManager renderManager;
    private int LEAF_TOT_NUM = 200;
    private Node fireNodeAll;
    private FireEffect bigFire;
    private final FireEffect fireControl;
    private Node treeNode;
    private Node leafsAllNode;
    private Node treesForestNode;
    private Geometry sphereGeo;
    // sun variables
    private final float SUN_X = 600f;
    private final float SUN_Y = 600f;
    private final float SUN_Z = -34.1017f;
    private boolean isDay = true;
    private float angle = 6f; // controls when the day/night starts. careful!!
    // isDay AND angle must represent day both when app starts. do carefull
    // with modifications.
    private FreeTTS freeTTS;
    private ParticleEmitter snow;
    private Material mat_red;
    private final String CASTLE_MODEL_LOCATION = "Models/25790_house_by_yazjack/ogre/Castle.j3o";

    /**
     *
     * @param assetManager
     * @param rootNode
     * @param aThis
     */
    public SceneNode(AssetManager assetManager, Node rootNode, Main aThis, ViewPort viewPort, RenderManager renderManager, Camera cam, FireEffect fireControl, FreeTTS freeTTS) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.aThis = aThis;
        this.viewPort = viewPort;
        this.renderManager = renderManager;
        this.cam = cam;
        this.fireControl = fireControl;
        this.freeTTS = freeTTS;

        createScene(); // done benchmark
        createSun(); // done benchmark
        createSnowStorm(rootNode); // 6000 Vertices, 3000 Triagnles
        createCastle(rootNode); // done benchmark
//
//        
//        /**
//         * performance issues. But, its 100% implemented, working..
//         */
//        //	 createFire();
        createForest();
        //createFog(); 100% implemented. But ir removes the day/light effect. still gameplay does not get broken. (day/night behavior)
    }

    public RigidBodyControl getRigBodControl() {
        return rigidBCscene;
    }

    private void createScene() {
        // create scene
        sceneModel = assetManager.loadModel(SCENE_MODEL_LOC);
        //     sceneModel.setLocalScale(1f);
        collisiosceneShape = CollisionShapeFactory.createMeshShape((Node) sceneModel);
        rigidBCscene = new RigidBodyControl(collisiosceneShape, 0);
        // rigidBCscene.setPhysicsLocation(new Vector3f(0, -10, 0));
        sceneModel.addControl(rigidBCscene);
        rootNode.attachChild(sceneModel);
    }

    private void createSun() {


        // simple sphere with color of sun
        Sphere sphereMesh = new Sphere(32, 32, 2f);
        sphereGeo = new Geometry("Sun", sphereMesh);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1f, 0.7f, 0.06f, 1));
        sphereGeo.setMaterial(mat);
        sphereGeo.scale(10);
        sphereGeo.setLocalTranslation(new Vector3f(SUN_X, SUN_Y, SUN_Z));
        rootNode.attachChild(sphereGeo);


        // actuall source of light
        sunSpotLight = new SpotLight();
        sunSpotLight.setSpotRange(100000f); // distance
        sunSpotLight.setSpotInnerAngle(100 * FastMath.DEG_TO_RAD); //inner light cone (central beam)
        sunSpotLight.setSpotOuterAngle(150 * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)

        sunSpotLight.setColor(ColorRGBA.White.mult(1f)); // light color
        //  sunSpotLight.setPosition(new Vector3f(-500, 400f, 0f)); // shine from location
        //    aThis.getS
        rootNode.addLight(sunSpotLight);

        // a little more color
        AmbientLight ambLight = new AmbientLight();
        ambLight.setColor(ColorRGBA.White.mult(.04f));
        rootNode.addLight(ambLight);
    }

    private void createSky() {
        //rootNode.attachChild(assetManager.loadModel("Textures/Sky/sky.j3o"));
        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", false);
        sky.setLocalScale(350);
        rootNode.attachChild(sky);
    }

    public CollisionShape getSceneShape() {
        return collisiosceneShape;
    }

    public Spatial getSceneModel() {
        return sceneModel;
    }

    private Spatial createSnowPart() {
        snow =
                new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        mat_red = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", assetManager.loadTexture(
                "Effects/Explosion/flame.png"));
        snow.setMaterial(mat_red);
        snow.setImagesX(2);
        snow.setImagesY(2); // 2x2 texture animation
        snow.setEndColor(ColorRGBA.White);   // cyan
        snow.setStartColor(ColorRGBA.White); // blue
        snow.getParticleInfluencer().setInitialVelocity(new Vector3f(0, -5, 0));
        snow.setStartSize(0.12f);
        snow.setEndSize(.24f);
        snow.setGravity(0, 1, 0);
        snow.setLowLife(1f);
        snow.setHighLife(8f);
        snow.getParticleInfluencer().setVelocityVariation(0.3f);

        // random
        randomX = 150 - rand.nextInt(300);
        randomZ = 150 - rand.nextInt(300);
        randomY = 80 - rand.nextInt(30);
        // snow.setLocalTranslation(randomX, SNOW_HEIGHT, randomZ);
        snow.setLocalTranslation(randomX, randomY, randomZ);

        return snow;
    }

    private void createSnowStorm(Node rootNode) {
        snowStorm = new Node();
        Snow snowGenerator;
        snowGenerator = new Snow(assetManager);

        for (int i = 0; i < SNOW_STORM_SIZE; i++) {
            snowStorm.attachChild(snowGenerator.getSnowPart());
        }
        rootNode.attachChild(snowStorm);
    }

    public void moveSnowStorm(Vector3f loc) {
        // randomly rotate the storm based on y axis, realism...
        float r = 10 + FastMath.DEG_TO_RAD * rand.nextInt(70);          // convert 45 degrees to radians
        snowStorm.rotate(0.0f, r, 0.0f);                // relative rotation around y axis

        // move storm to always follow the hero.
//		snowStorm.setLocalTranslation(randomX, randomY, randomZ);
//		snowStorm.getLocalTranslation().x = randomX;
//		snowStorm.getLocalTranslation().y = randomY;
//		snowStorm.getLocalTranslation().z = randomZ;
        snowStorm.setLocalTranslation(loc);

    }

    private void createCastle(Node rootNode) {
        castleSpatial = assetManager.loadModel(CASTLE_MODEL_LOCATION);
        castleNode = new Castle(castleSpatial, rootNode, assetManager, renderManager, cam, aThis, fireControl, freeTTS);
    }

    public Castle getCastleNode() {
        return castleNode;
    }

    public void update(float tpf) {
        // sun / night
        angle += tpf / 40;
        angle %= FastMath.TWO_PI;
        sunSpotLight.setPosition(new Vector3f(FastMath.sin(angle) * SUN_X, FastMath.cos(angle) * SUN_Y, SUN_Z));
        sunSpotLight.setDirection(new Vector3f(sunSpotLight.getPosition().x, sunSpotLight.getPosition().y - 10, sunSpotLight.getPosition().x));

        //  sunSpotLight.setDirection(getCastleNode().getLastKnownLocCastle());
        sphereGeo.setLocalTranslation(sunSpotLight.getPosition()); // sun/sphere follows light

        if (isDay && sunSpotLight.getPosition().y + 50 < 0) {
            isDay = false;
        } else if (!isDay && (sunSpotLight.getPosition().y + 50 > 0)) {
            isDay = true;
        }
    }

    private void createFire() {
        Random random = new Random();
        int x, xOffset = 800; // done
        int y, yOffset = 7;
        int z, zOffset = 400;
        float startSize = 13;
        float endSize = 10;
        Vector3f location3f;

        //  new FireEffect(rootNode, assetManager, new Vector3f(-350 + randX, randY + 2, i * 10 - 500), 13, 10);

        for (int i = 0; i < FIRE_SIZE; i++) {
            z = random.nextInt(20) + zOffset;
            y = random.nextInt(10) + yOffset;
            x = i * 10 - xOffset;
            location3f = new Vector3f(x, y, z);
            fireControl.addFireOnNoHealthObj(location3f, startSize, endSize);
            // new FireEffect(rootNode, assetManager, new Vector3f(-350 + randX, randY + 2, i * 10 - 500), 13, 10);

        }
    }

    private void createForest() {
        createTrees();
        createGroundLeafs();
//      createRedRoots();
    }

    private void createGroundLeafs() {
        float leafHeight, leafWidth, leafDepth;
        float leafX, leafZ, leafY = getRigBodControl().getPhysicsLocation().y;
        leafsAllNode = new Node();
        for (int i = 0; i < LEAF_TOT_NUM; i++) {
            leafHeight = rand.nextFloat() + .05f;
            leafWidth = rand.nextFloat() + .05f;
            leafDepth = rand.nextFloat() + .05f;
            leafX = -300 + rand.nextInt(650);
            leafZ = -120 + rand
                    .nextInt(350);

            leafModel = assetManager.loadModel("Models/Bushes_59269_low_poly_foliege_blend_by_EugeneKiver/4/Plane.1988.mesh.j3o");
            leafModel.scale(leafWidth, leafHeight, leafDepth);
            leafModel.setLocalTranslation(leafX, leafY +1, leafZ);
            leafModel.setLocalRotation(new Quaternion().fromAngleAxis(-90 * FastMath.DEG_TO_RAD, new Vector3f(0, 0, 1)));
            leafsAllNode.attachChild(leafModel);
        }
        rootNode.attachChild(leafsAllNode);

    }

    private void createTrees() {
        float yLoc = getRigBodControl().getPhysicsLocation().y;

        treesForestNode = new Node("Forest Node");
        Tree tree = new Tree(assetManager);
        for (int i = 0; i < FOREST_SIZE; i++) {
            treesForestNode.attachChild(tree.getTree(yLoc));
        }
        aThis.addToShootables(treesForestNode);
    }

    public Node getForestNode() {
        return treesForestNode;
    }

    private void createFog() {
        fpp = new FilterPostProcessor(assetManager);
        fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
        fog.setFogDistance(200);
        fog.setFogDensity(.7f);
        fpp.addFilter(fog);
        viewPort.addProcessor(fpp);
    }

    public Node getLeaftAllNodes() {
        return leafsAllNode;
    }

    public Vector3f getSunLocation() {
        return sunSpotLight.getPosition();
    }
}
