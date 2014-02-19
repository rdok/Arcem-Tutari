/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.environment;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Riz
 */
public class FireEffect {

    private Node rootNode;
    private ParticleEmitter fireParticle;
    private final Random random = new Random();
    private Node fireNoHealthObj, fireNodes;
    private float gravity;
    private ParticleEmitter smoke;
    private int SCALE_TIMES;
    private Node fireOnCasle;
    private float scaleSize = .015f;
    private final ArrayList<ParticleEmitter> firesOnEnvironment;
    private final ArrayList<ParticleEmitter> firesOnCastle;
    private float startSize;
    private float endSize;
    private final AssetManager assetManager;
    private boolean castleDetached;

    public FireEffect(Node rootNode, AssetManager assetManager) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        material.setFloat("Softness", 3f); // 
        firesOnEnvironment = new ArrayList<ParticleEmitter>();
        firesOnCastle = new ArrayList<ParticleEmitter>();
        fireNodes = new Node("Fire Node");
        SCALE_TIMES = 5;

    }

    private void createSmoke() {

        smoke = new ParticleEmitter("Smoke", ParticleMesh.Type.Triangle, 30);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        smoke.setMaterial(material);
        smoke.setShape(new EmitterSphereShape(Vector3f.ZERO, 5));
        smoke.setImagesX(1);
        smoke.setImagesY(1); // 2x2 texture animation
        smoke.setStartColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1f)); // dark gray
        smoke.setEndColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.3f)); // gray      
        smoke.setStartSize(5f);
        smoke.setEndSize(.1f);
        smoke.setGravity(0, -0.011f, 0);
        smoke.setLowLife(10f);
        smoke.setHighLife(40f);
        // smoke.setLocalTranslation(location3f);
        smoke.emitAllParticles();

        fireNoHealthObj.attachChild(smoke);

    }

    private void createFire(Vector3f location3f, float startSize, float endSize) {
        fireNoHealthObj = new Node();

        startSize += getRandomSign() * (random.nextFloat() * 4);
        gravity = ((random.nextFloat() * 3) + 1) * (-1);
//        startSize /= 10;
//        endSize /= 10;
//        gravity /= 10;

        // endSize += getRandomSign() * (random.nextFloat() * 5);
        // float endSize = random.nextFloat() * (4 - 2) + 2;
        //Fire
        fireParticle = new ParticleEmitter("Fire", ParticleMesh.Type.Triangle, 30);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));

        fireParticle.setMaterial(material);
        fireParticle.setShape(new EmitterSphereShape(Vector3f.ZERO, 0.1f));
        fireParticle.setImagesX(2);
        fireParticle.setImagesY(2); // 2x2 texture animation
        fireParticle.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f)); // red
        fireParticle.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fireParticle.setStartSize(startSize);
        fireParticle.setEndSize(endSize);
        fireParticle.setGravity(0, gravity, 0);
        fireParticle.setLowLife(0.5f);
        fireParticle.setHighLife(3f);

        fireParticle.setLocalTranslation(location3f);

    }

    public int getRandomSign() {
        return random.nextBoolean() ? -1 : 1;
    }

    public void addFireOnNoHealthObj(Vector3f location3f, float startSize, float endSize) {
        createFire(location3f, startSize, endSize);
        firesOnEnvironment.add(fireParticle);
        //    fireNodes.attachChild(fireParticle);
        rootNode.attachChild(firesOnEnvironment.get(firesOnEnvironment.size() - 1));

        //      fireNoHealthObj.attachChild(fire);
//      fireNoHealthObj.setLocalTranslation(location3f);
//      //createSmoke();
//      fireEffectNodeAll.attachChild(fireNoHealthObj);
    }

    public void addFireOnCastle(Vector3f location3f, float startSize, float endSize) {
        createFire(location3f, startSize, endSize);
        firesOnCastle.add(fireParticle);
        //  fireOnCasle.attachChild(fireParticle);
        rootNode.attachChild(firesOnCastle.get(firesOnCastle.size() - 1));

    }

    public ParticleEmitter getFire() {
        return fireParticle;
    }

    /**
     * if more than MAX_FIRES_ALLOWED particles are created, they are cradualy
     * scaled to 0, and then they are removed. possible, for better performance.
     *
     * @param tpf
     */
    public void update(float tpf) {
        update(firesOnEnvironment, 25);
        if (castleDetached) {
            System.out.println("removing fires)");

            update(firesOnCastle, 0);

        }

    }

    private void resetScaleTimes() {
        SCALE_TIMES = 5;
    }

    public Spatial getFireNodes() {
        return fireNodes;
    }

    private void update(ArrayList<ParticleEmitter> list, int maxFires) {
        if (list.size() > maxFires) {

            startSize = list.get(0).getStartSize();
            endSize = list.get(0).getEndSize();

            if (startSize > 0 && endSize > 0) {
                list.get(0).setStartSize(startSize - scaleSize);
                list.get(0).setEndSize(endSize - scaleSize);
//	    firesAllList.get(0).killAllParticles();
//	    firesAllList.get(0).emitAllParticles();
            } else {
                //    System.out.println(firesAllList.size());
                list.get(0).killAllParticles();
                rootNode.detachChild(list.get(0));
                list.remove(0);
                //    System.out.println("removing");
            }
        }
    }

    public void setCastleDetached() {
        castleDetached = true;
    }
}
