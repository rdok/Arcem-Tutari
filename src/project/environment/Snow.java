/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.environment;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import java.util.Random;

/**
 *
 * @author Riza
 */
public class Snow {

    private final ParticleEmitter snow;
    private final AssetManager assetManager;
    private final Random rand;

    public Snow(AssetManager assetManager) {
        this.assetManager = assetManager;
        rand = new Random();

        snow =
                new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material mat_red = new Material(assetManager,
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
    }

    public ParticleEmitter getSnowPart() {
        // random
        int randomX = 150 - rand.nextInt(300);
        int randomZ = 150 - rand.nextInt(300);
        int randomY = 80 - rand.nextInt(30);
        // snow.setLocalTranslation(randomX, SNOW_HEIGHT, randomZ);
        snow.setLocalTranslation(randomX, randomY, randomZ);

        return snow.clone();
    }
}
