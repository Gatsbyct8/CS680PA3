/*
 * Food.java
 * 
 * Tian Chen - ct970808@bu.edu
 * 
 * OpenGL object that draws food as a brown sphere.
 * Dropped from a random x and z point and falls to the
 * bottom of the tank for fish to eat.
 */

import com.jogamp.opengl.util.gl2.GLUT;

import javax.media.opengl.GL2;
import java.util.Random;

public class Food {

	private Random rand;
	public float x, y, z;
	public int food_object;
	private float speed;
	public float radius;
	private boolean eaten;
	private boolean flag = false;
	
	public Food() {
		rand = new Random();
		x = rand.nextFloat()*4 - 2;
		y = 2.0f;
		z = rand.nextFloat()*4 - 2;
		speed = 0.01f;
		radius = 0.1f;
		eaten = false;
	}

	public boolean isFlag() {
		return flag;
	}

	public void init(GL2 gl) {
		food_object = gl.glGenLists(1);
		gl.glNewList(food_object, GL2.GL_COMPILE);
		GLUT glut = new GLUT();
		glut.glutSolidSphere(radius, 36, 28);
		gl.glEndList();
		flag = true;
	}
	
	public void update(GL2 gl) {
		if (y > -1.875f) {
			y -= speed;
		}
	}
	
	public void draw(GL2 gl) {
		gl.glPushMatrix();
	    gl.glPushAttrib( GL2.GL_CURRENT_BIT );
	    gl.glTranslatef(x, y, z);
	    gl.glScaled(2,1,1);
	    gl.glColor3f( 0.85f, 0.55f, 0.20f); // orange
	    gl.glCallList(food_object);
	    gl.glPopAttrib();
	    gl.glPopMatrix();
	}
	
	public boolean isEaten() {
		return eaten;
	}
	
	public void eaten() {
		eaten = true;
	}

}
