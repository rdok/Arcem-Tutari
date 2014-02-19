/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.sounds;

import project.environment.SceneNode;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import java.util.Random;
import Main.HeroNode;

/**
 *
 * @author Riza
 */
public class SoundNode {

    private AudioNode stepNode; // robot step
    private AudioNode musicSongOfStorms;
    private AudioNode shootNode;
    private HeroNode heroNode;
    private SceneNode sceneNode;
    private final String[] soundTracks = {"Song Of Storms.wav", "Daft Punk - Rinzler (Basic Slack Remix).wav", "Tron Legacy - The Grid Part II [Daft Punk] - New Bonus Track.wav", "Tron Rinzler.wav"};
    private Random rand = new Random();
    private final AssetManager assetManager;
    private AudioNode soundEvironmentAudioNode;
    private final AudioNode bigExplosionNode;

    public SoundNode(SceneNode sceneNode, AssetManager assetManager) {
        this.sceneNode = sceneNode;
        this.assetManager = assetManager;

        musicSongOfStorms = new AudioNode(assetManager, "Sound/SoundTracks/" + getRandomSoundTrack());
        shootNode = new AudioNode(assetManager, "Sound/Effects/Gun.wav");
        stepNode = new AudioNode(assetManager, "Sound/Effects/robot_step.wav");
        bigExplosionNode = new AudioNode(assetManager, "Sound/Effects/BigExplosionEffect.wav");
        bigExplosionNode.setPositional(false);

        playEnvironmentSound();

    }

    private void playEnvironmentSound() {
        soundEvironmentAudioNode = new AudioNode(assetManager, "Sound/Environment/Morning in the forest - over one hour of relaxing forest sounds.wav", true);
        // forest sound is more thatn 30min. we start said sound at a random
        // time
        soundEvironmentAudioNode.setPositional(false);
        soundEvironmentAudioNode.setTimeOffset((float) rand.nextInt(600));
        soundEvironmentAudioNode.play();

    }

    public void playSound(AudioNode audioNode, float volume, float pitch, boolean loopAudio) {
        audioNode.setVolume(volume);
        audioNode.setPitch(pitch);

        if (loopAudio) { // loop continously
            audioNode.play();
        } else {
            audioNode.playInstance();
        }

    }

    public AudioNode getStepNode() {
        return stepNode;
    }

    public AudioNode getMusicSongOfStorms() {
        return musicSongOfStorms;
    }

    public AudioNode getShootNode() {
        return shootNode;
    }

    private String getRandomSoundTrack() {
        return soundTracks[rand.nextInt(soundTracks.length)];
    }

    public AudioNode getBigExplosionNode() {
        return bigExplosionNode;
    }
}
