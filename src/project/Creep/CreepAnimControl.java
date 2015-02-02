/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project.Creep;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Node;
import java.util.Random;

/**
 *
 * @author Riza
 */
public class CreepAnimControl implements AnimEventListener {

    // animcation controls and channels
    private final AnimControl animControl;
    private final AnimChannel channelAttack;
    private final AnimChannel channelDead;
    private final AnimChannel channelRunBase;
    private final AnimChannel channelRunTop;
    // possible movement of creep
    public final String IDLE_BASE_ANIM = "IdleBase", IDLE_TOP_ANIM = "IdleTop";
    private final String ATTACK_SLICE_HORIZONTAL = "SliceHorizontal",
            ATTACK_SLICE_VERTICAL = "SliceVertical";
    private final String RUN_TOP_ANIM = "RunTop", RUN_BASE_ANIM = "RunBase";
    private final String DANCE_ANIM = "Dance";
    private final String STAND_UP_BACK_ANIM = "StandUpBack", STAND_UP_FRONT_ANIM = "StandUpFront";
    // time of creep getting killed
    private float SPEED_DEAD_ANIM = 0.11f;
    private final Random random;
    private String tempAnimName;
    private String animName;
    private Creep creep;
    private String randAttack;
    private float SPEED_ANIM_ATTACK = .5f;
    private boolean heroAttacked;
    private boolean castleAttacked;

    public CreepAnimControl(Creep creep) {
        this.creep = creep;

        // animations of creep
        animControl = creep.getCreepModel().getControl(AnimControl.class);
        animControl.addListener(this);

        // chanels
        channelAttack = animControl.createChannel();
        channelDead = animControl.createChannel();
        channelRunBase = animControl.createChannel();
        channelRunTop = animControl.createChannel();

        // random tool
        random = new Random();
    }

    public void update(float tpf) {

        if (creep.isRunning() && !creep.isAttacking()) {
            enableAnimationRun();
        } else if (creep.isRunning() && creep.isAttacking()) {
            if (!isBaseAnimIdle()) { // verify that channel of feet idle is not null
                enableAnimIdle(); // stop moving feet.
            }
            if (!isAnimationFightActive()) { // if attack has not started
                enableAnimationFightRandom();
            }
        } else if (!creep.isAlive()) { // kreep died
            //		System.out.println("creep.isAttacking(): dead!!");
            enableAnimDead();
        } else {
            enableAnimIdle();
        }

    }

    // verify that both channels are set for run animation 
    private boolean isAnimationRunActive() {
        return channelRunBase.getAnimationName() == null || channelRunTop.getAnimationName() == null
                ? false : true;
    }

    // verify feet are not moving
    private boolean isBaseAnimIdle() {
        return channelRunBase.getAnimationName() != null
                ? true : false;
    }

    private boolean isAnimationFightActive() {
        if (channelAttack.getAnimationName() != null) {
            return true;
        }
        return false;
    }

    public void enableAnimationRun() {
        if (!isAnimationRunActive()) {
            channelRunBase.setAnim(RUN_BASE_ANIM, .5f);
            channelRunTop.setAnim(RUN_TOP_ANIM, .5f);
            channelRunBase.setLoopMode(LoopMode.Loop);
            channelRunTop.setLoopMode(LoopMode.Loop);
        }
    }

    // stop movement of feet.
    public void enableAnimIdle() {
        channelRunBase.setAnim(IDLE_BASE_ANIM);
        channelRunBase.setLoopMode(LoopMode.Loop);
    }

    private void enableAnimDead() {
        channelDead.setAnim(STAND_UP_BACK_ANIM, SPEED_DEAD_ANIM);
        channelDead.setLoopMode(LoopMode.DontLoop);

        // disable all other animations
        channelRunBase.setAnim(IDLE_BASE_ANIM);
        channelRunTop.setAnim(IDLE_TOP_ANIM);
        //channel_attack.set
    }

    private void enableAnimFightRandom() {
        tempAnimName = random.nextBoolean() ? ATTACK_SLICE_HORIZONTAL : ATTACK_SLICE_VERTICAL;
        channelAttack.setAnim(tempAnimName);
    }

    /**
     * @return the channel_dead
     */
    public AnimChannel getChannelDead() {
        return channelDead;
    }

    /**
     * @return the animControl
     */
    public AnimControl getAnimControl() {
        return animControl;
    }

    public void enableAnimationFightRandom() {
        randAttack = random.nextBoolean() ? ATTACK_SLICE_HORIZONTAL : ATTACK_SLICE_VERTICAL;
        if (channelAttack.getAnimationName() == null) {
            channelAttack.setAnim(randAttack, SPEED_ANIM_ATTACK);
            channelAttack.setLoopMode(LoopMode.Loop);
        }

    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        //  System.out.println(animName);
        if (isAnimationFightActive()) {
            if (castleAttacked) {
                creep.attackCastle();
            }
            channelRunBase.setAnim(IDLE_BASE_ANIM);

        }
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        System.out.println("anime name: " + animName);
        if (animName.equals(IDLE_BASE_ANIM) || animName.equals(DANCE_ANIM)
                || animName.equals(STAND_UP_BACK_ANIM) || animName.equals(STAND_UP_FRONT_ANIM)) {
            channelRunBase.getControl().setEnabled(false);
            try {
                if (channel.getAnimationName().equals(IDLE_BASE_ANIM)) {
                    channel.setAnim(IDLE_BASE_ANIM, 0.50f);
                    channel.setLoopMode(LoopMode.Loop);
                    channel.setSpeed(1f);
                    return;
                }
                if (channel.getAnimationName().equals(DANCE_ANIM)) {
                    channel.setAnim(DANCE_ANIM, 0.50f);
                    channel.setLoopMode(LoopMode.Loop);
                    channel.setSpeed(1f);
                    return;
                }
                if (channel.getAnimationName().equals(STAND_UP_BACK_ANIM)) {
                    channel.setAnim(STAND_UP_BACK_ANIM, -0.50f);
                    channel.setLoopMode(LoopMode.DontLoop);
                    channel.setSpeed(1f);
                    return;
                }
                if (channel.getAnimationName().equals(STAND_UP_FRONT_ANIM)) {
                    channel.setAnim(STAND_UP_FRONT_ANIM, 0.50f);
                    channel.setLoopMode(LoopMode.Loop);
                    channel.setSpeed(1f);
                    return;
                }

            } catch (NullPointerException nPE) {
                System.out.println(nPE.getMessage());
            }
        }
    }

    void heroAttacked() {
        enableAnimationFightRandom();
        heroAttacked = true;
    }
}
