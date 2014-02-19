/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import com.jme3.math.Vector3f;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import project.environment.SceneNode;

/**
 *
 * @author Riz
 */
public class ExplosionThread extends Thread {

    private long oldTime;
    private volatile Thread threadActive;
    private Vector3f castleLoc;
    private SceneNode sceneNode;
    private int TOT_EXPLOSIONS;
    private boolean proccessStarted;
    private Random random;
    private boolean castleAlive;
    private long newTime;
    private long SECONDS_10 = 10000000000L;

    ExplosionThread() {
        TOT_EXPLOSIONS = 20;
        random = new Random();
    }

    public void setStats(Vector3f castleLoc, SceneNode sceneNode) {
        this.castleLoc = castleLoc;
        this.sceneNode = sceneNode;
        castleAlive = false;
    }

    @Override
    public void run() {
        proccessStarted = true;

        oldTime = System.nanoTime();
        //  System.out.println("Sleeping for one second");

        Thread thisThread = Thread.currentThread();
        threadActive = ExplosionThread.currentThread();

        while (thisThread == threadActive) {
            newTime = System.nanoTime();
            createExplosion();
            TOT_EXPLOSIONS--;

            if (explosionsDone()) {
                stopThread();
            }

            try {
                // System.out.println("Sleeping for one second");
                ExplosionThread.sleep(random.nextInt(500));
            } catch (InterruptedException ex) {
                Logger.getLogger(ExplosionThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void stopThread() {
        threadActive = null;
    }

    private void createExplosion() {
        sceneNode.getCastleNode().addExplosion();
    }

    public boolean hasStarted() {
        return proccessStarted;
    }

    public void setCastleAlive(boolean isAlive) {
        castleAlive = isAlive;
    }

    private boolean explosionsDone() {
        return newTime - oldTime > SECONDS_10 ? true : false;
    }
}