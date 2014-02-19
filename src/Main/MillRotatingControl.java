/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author Riz
 */
public class MillRotatingControl extends AbstractControl {

	// rotating speed of mill
   private float speed = 3;

   @Override
   protected void controlUpdate(float tpf) {
      spatial.rotate( tpf * speed,0, 0);
   }

   @Override
   protected void controlRender(RenderManager rm, ViewPort vp) {
   }

   public Control cloneForSpatial(Spatial spatial) {
      MillRotatingControl control = new MillRotatingControl();
      control.setSpeed(speed);
      control.setSpatial(spatial);
      return control;
   }

   /**
    * @return the speed
    */
   public float getSpeed() {
      return speed;
   }

   /**
    * @param speed the speed to set
    */
   public void setSpeed(float speed) {
      this.speed = speed;
   }
}
