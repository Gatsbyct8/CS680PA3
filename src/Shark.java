/*
 * Shark.java
 * 
 * Tian Chen - ct970808@bu.edu 11/4/2020
 * 
 * Object for an OpenGL shark that moves along 3 axes of a tank,
 * attacks fish and "eats" food, but doesn't go after food.
 */

import com.jogamp.opengl.util.gl2.GLUT;

import javax.media.opengl.GL2;
import java.util.List;
import java.util.Random;

public class Shark {
	private GLUT glut;
	public float x, y, z, last_x, last_y, last_z;
	private int shark_obj;
	private int tail_obj;
	private int body_obj;
	private int fin_obj;
	private Coord direction;
	private Coord orientation;

	public float scale;

	private float tail_angle;
	private float tail_speed;
	private float tail_direction;
	private float body_speed;
	private float body_angle;

	private float trans_speed_x;
	private float trans_speed_y;
	private float trans_speed_z;
	
	public float boundingSphereRadius;

	private Random rand;
	
	private Vivarium v;
	
	private List<Fish> prey = null;
	private float []rotationMatrix;
	private boolean flag =false;

	public Shark(Vivarium _v) {
		glut = new GLUT();
		rand = new Random();
		x = last_x = rand.nextFloat() * 3.6f - 1.8f;
		y = last_y = rand.nextFloat() * 3.6f - 1.8f;
		z = last_z = rand.nextFloat() * 3.6f - 1.8f;
		shark_obj = tail_obj = body_obj = 0;
		scale = 1.5f;
		tail_speed = 1f;
		tail_angle = body_angle = 0;
		tail_direction = 1;
		body_speed = tail_speed / 4;
		trans_speed_x = 0.005f;
		trans_speed_y = 0.005f;
		trans_speed_z = 0.005f;

		boundingSphereRadius = 0.35f * scale;
		
		v = _v;
		prey = v.getFish();

		direction = new Coord(0,0,-1);
		orientation = new Coord(0,0,-1);
		rotationMatrix = new float[]
				{1, 0, 0, 0.0f,
						0, 1, 0, 0.0f,
						0, 0, 1, 0.0f,
						last_x, last_y, last_z, 1.0f};
	}

	public void init(GL2 gl) {
		createBody(gl);
		createTail(gl);
		createFin(gl);
		shark_obj = gl.glGenLists(1);
		gl.glNewList(shark_obj, GL2.GL_COMPILE);
		cal_angle_and_translate_matrix(gl);
		gl.glEndList();
		flag = true;
	}

	public boolean isFlag() {
		return flag;
	}

	public void draw(GL2 gl) {
		gl.glPushMatrix();
		gl.glPushAttrib(gl.GL_CURRENT_BIT);
		gl.glMultMatrixf(rotationMatrix, 0);
		// Rotate tail
		gl.glPushMatrix();
		gl.glRotatef(tail_angle, 0, 1, 0);
		gl.glColor3f(0.453125f, 0.546875f, 0.625f);
		gl.glCallList(tail_obj);
		gl.glPopMatrix();

		// Rotate body
		gl.glPushMatrix();
		gl.glRotatef(body_angle, 0, 1, 0);
		gl.glColor3f(0.453125f, 0.546875f, 0.625f);
		gl.glCallList(body_obj);
		gl.glPopMatrix();

		// Draw top dorsal fin
		gl.glPushMatrix();
		gl.glColor3f(0.453125f, 0.546875f, 0.625f);
		gl.glCallList(fin_obj);
		gl.glPopMatrix();

		gl.glPopAttrib();
		gl.glPopMatrix();
	}

	public void update(GL2 gl) {
		for (Shark shark : v.getShark()) {
			if (shark != this){
				if (distance(new Coord(shark.last_x,shark.last_y,shark.last_z), new Coord(last_x,last_y,last_z)) < 0.5){
					x = -x;
					y = -y;
					z = -z;
					orientation.x = x - last_x;
					orientation.y = y - last_y;
					orientation.z = z - last_z;
					shark.x = -shark.x;
					shark.y = -shark.y;
					shark.z = -shark.z;
					shark.orientation.x = x - last_x;
					shark.orientation.y = y - last_y;
					shark.orientation.z = z - last_z;
				}
			}
		}
		boolean hasFish = false;
		if (prey.size()!=0) {
			for (Fish fish: prey) {
				if (fish.dead == false) {
					x = fish.last_x;
					y = fish.last_y;
					z = fish.last_z;
					orientation.x = x - last_x;
					orientation.y = y - last_y;
					orientation.z = z - last_z;
					hasFish = true;
				}
			}
		}
		if (hasFish == false){
			if(distance(new Coord(x,y,z),new Coord(last_x,last_y,last_z))<0.2) {
				x = rand.nextFloat()*4-2;
				y = rand.nextFloat()*4-2;
				z = rand.nextFloat()*4-2;
				orientation.x = x - last_x;
				orientation.y = y - last_y;
				orientation.z = z - last_z;
			}
		}
		calcDistances(gl);
		translate();
		moveTail();
		cal_angle_and_translate_matrix(gl);
	}

	private void changeOrientation() {
		// use the cross product to get rotation axis
		Coord axis = new Coord();
		direction.normalize();
		orientation.normalize();
		axis.x = - orientation.y * direction.z + orientation.z * direction.y;
		axis.y = - orientation.z * direction.x + orientation.x * direction.z;
		axis.z = - orientation.x * direction.y + orientation.y * direction.x;
		if (axis.x == 0 && axis.y == 0 && axis.z == 0){
			rotationMatrix[12] = last_x;
			rotationMatrix[13] = last_y;
			rotationMatrix[14] = last_z;
			return;
		}
		axis.normalize();

		// use the dot product to get the rotation angle
		double theta = Math.acos(orientation.x * direction.x + orientation.y * direction.y + orientation.z * direction.z);

		// Create the rotation quaternion
		float cosTheta2 = (float) Math.cos(theta / 2);
		float sinTheta2 = (float) Math.sin(theta / 2);
		Quaternion rotation = new Quaternion(cosTheta2, sinTheta2 * (float) axis.x, sinTheta2 * (float) axis.y, sinTheta2 * (float) axis.z);

		// get the rotation matrix and rotate
		rotationMatrix = rotation.to_matrix();
		rotationMatrix[12] = last_x;
		rotationMatrix[13] = last_y;
		rotationMatrix[14] = last_z;
	}

	private void cal_angle_and_translate_matrix(GL2 gl) {
		changeOrientation();
	}
	
	private void createBody(GL2 gl) {
		// body
		body_obj = gl.glGenLists(1);
		gl.glNewList(body_obj, GL2.GL_COMPILE);
		gl.glPushMatrix();
		gl.glScalef(0.4f, 0.6f, 1);
		gl.glTranslatef(0, 0, -0.09f);
		glut.glutSolidSphere(0.2, 36, 24);
		gl.glPopMatrix();
		gl.glEndList();
	}

	private void createTail(GL2 gl) {
		// tail
		tail_obj = gl.glGenLists(1);
		gl.glNewList(tail_obj, GL2.GL_COMPILE);
		gl.glPushMatrix();
		gl.glScalef(0.5f, 1, 1);
		gl.glTranslatef(0, 0, 0.35f);
		gl.glRotatef(-180, 0, 1, 0);
		glut.glutSolidCone(0.1, 0.35, 20, 20);
		gl.glPopMatrix();
		gl.glEndList();
	}

	private void createFin(GL2 gl) {
		// fin
		fin_obj = gl.glGenLists(1);
		gl.glNewList(fin_obj, GL2.GL_COMPILE);
		gl.glPushMatrix();
		gl.glScalef(0.5f, 1, 1);
		gl.glTranslatef(0, 0, -0.1f);
		gl.glRotatef(-75, 1, 0, 0);
		glut.glutSolidCone(0.1, 0.27, 20, 20);
		gl.glPopMatrix();
		gl.glEndList();
	}

	private void moveTail() {
		tail_angle += tail_speed * tail_direction;
		body_angle += body_speed * tail_direction * -1;

		if (tail_angle > 10 || tail_angle < -10) {
			tail_direction *= -1;
		}
	}
	
	// Computes distances between food and itself
	private void calcDistances(GL2 gl) {
		if (prey.size() != 0) {
			Coord a = new Coord(last_x, last_y, last_z);
			// food
			for (Fish fish: prey){
				if (fish.dead == false) {
					Coord b = new Coord(fish.last_x, fish.last_y, fish.last_z);
					if (distance(a, b) < 0.5) {
						gl.glDeleteLists(prey.get(0).getFish_obj(), 1);
						fish.dead = true;
					}
				}
			}
		}
	}

	
	// Move the fish around the tank using a combination of potential functions
	// and flipping directions when about to leave tank.
	private void translate() {
			last_x += trans_speed_x * (x - last_x);
			last_y += trans_speed_y * (y - last_y);
			last_z += trans_speed_z * (z - last_z);
	}
	
	// Coord helper functions
		private Coord add(Coord a, Coord b) {
			a.x += b.x;
			a.y += b.y;
			a.z += b.z;
			return a;
		}
		
		private Coord add(Coord[] b) {
			Coord ret = new Coord();
			for (Coord a : b) {
				ret.x += a.x;
				ret.y += a.y;
				ret.z += a.z;
			}
			return ret;
		}
		
		private float distance(Coord a, Coord b) {
			return (float) Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2) + Math.pow(a.z - b.z, 2));
		}
		
		// To keep track of fish coordinates
		public void addPrey(Fish f) {
			prey.add(f);
		}
		
		// If fish has been eaten, stop attacking
		public void removePrey() {
			prey = null;
		}

}
