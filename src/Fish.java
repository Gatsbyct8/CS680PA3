/*
 * Fish.java
 * 
 * Tian Chen - ct970808@bu.edu 11/4/2020
 * 
 * Object for an OpenGL fish that moves along 3 axes of a tank,
 * avoids Sharks, and "eats" food.
 */

import com.jogamp.opengl.util.gl2.GLUT;

import javax.media.opengl.GL2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fish {
	private GLUT glut;
	public float x, y, z, last_x, last_y, last_z;
	private int fish_obj;
	private int tail_obj;
	private int body_obj;
	private Coord direction;
	private Coord orientation;

	public boolean dead;

	private float tail_angle;
	private float tail_speed;
	private float tail_direction;

	private float trans_speed_x;
	private float trans_speed_y;
	private float trans_speed_z;
	
	public float boundingSphereRadius;
	private float []rotationMatrix;
	private boolean flag = false;
	
	private List<Shark> predator = null;

	private Random rand;
	
	private Vivarium v;

	public boolean isFlag() {
		return flag;
	}

	public int getFish_obj() {
		return fish_obj;
	}

	public Fish(Vivarium _v) {
		glut = new GLUT();
		rand = new Random();
		x = last_x = rand.nextFloat() * 4 - 2;
		y = last_y = rand.nextFloat() * 4 - 2;
		z = last_z = rand.nextFloat() * 4 - 2;
		fish_obj = tail_obj = body_obj = 0;
		tail_speed = 1f;
		tail_angle = 0;
		tail_direction = 1;
		trans_speed_x = 0.005f;
		trans_speed_y = 0.005f;
		trans_speed_z = 0.005f;

		boundingSphereRadius = 0.35f;
		
		v = _v;
		predator = v.getShark();

		dead = false;
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
		fish_obj = gl.glGenLists(1);
		gl.glNewList(fish_obj, GL2.GL_COMPILE);
		gl.glEndList();
		flag= true;
	}

	public void draw(GL2 gl) {
		gl.glPushMatrix();
		gl.glPushAttrib(gl.GL_CURRENT_BIT);
		gl.glMultMatrixf(rotationMatrix, 0);
		if (dead) {
			gl.glRotatef(-90, 0, 0, 1);
		}

		// Rotate tail
		gl.glPushMatrix();
		gl.glRotatef(tail_angle, 0, 1, 0);
		gl.glColor3f( 0.85f, 0.55f, 0.20f);
		gl.glCallList(tail_obj);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glColor3f( 0.85f, 0.55f, 0.20f);
		gl.glCallList(body_obj);
		gl.glPopMatrix();

		gl.glPopAttrib();
		gl.glPopMatrix();
	}

	public void update(GL2 gl) {
		if (!dead) {
			if (v.getFoodList().size() != 0) {
				x = v.getFoodList().get(0).x;
				y = v.getFoodList().get(0).y;
				z = v.getFoodList().get(0).z;
				orientation.x = x - last_x;
				orientation.y = y - last_y;
				orientation.z = z - last_z;
			} else {
				if (v.getShark().size()!=0){
					potentialFunction(this, v.getFish(), v.getShark());
				}
				else if (distance(new Coord(x, y, z), new Coord(last_x, last_y, last_z)) < 0.5) {
					x = rand.nextFloat()*3.6f - 1.8f;
					y = rand.nextFloat()*3.6f - 1.8f;
					z = rand.nextFloat()*3.6f - 1.8f;
					orientation.x = x - last_x;
					orientation.y = y - last_y;
					orientation.z = z - last_z;
				}
			}
		}
		if (!dead) {
			calcDistances(gl);
		}
			translate();
		if (!dead){
			moveTail();
		}
		cal_angle_and_translate_matrix(gl);
	}

	private Coord potentialFunction(Coord p, Coord q1, float scale) {
		float x = (float) (scale*(p.x - q1.x)*Math.pow(Math.E,-1*(Math.pow((p.x-q1.x), 2) + Math.pow((p.y-q1.y), 2) + Math.pow((p.z-q1.z), 2)) ));
		float y = (float) (scale*(p.y - q1.y)*Math.pow(Math.E,-1*(Math.pow((p.x-q1.x), 2) + Math.pow((p.y-q1.y), 2) + Math.pow((p.z-q1.z), 2)) ));
		float z = (float) (scale*(p.z - q1.z)*Math.pow(Math.E,-1*(Math.pow((p.x-q1.x), 2) + Math.pow((p.y-q1.y), 2) + Math.pow((p.z-q1.z), 2)) ));
		Coord potential = new Coord(x, y, z);
		return potential;
	}

	private void potentialFunction(Fish fish, List<Fish> fish1, List<Shark> shark) {
		if (shark.size()!=0){
			float sumx = 0f;
		float sumy = 0f;
		float sumz = 0f;
		for (Shark s: shark){
			Coord coord = potentialFunction(new Coord(last_x,last_y,last_z),new Coord(s.last_x,s.last_y,s.last_z),1.25f);
			sumx += coord.x;
			sumy += coord.y;
			sumz += coord.z;
		}
			sumx /= shark.size();
			sumy /= shark.size();
			sumz /= shark.size();
			if (sumx+last_x >-2 && sumx+last_x <2)
			x = sumx+last_x;
			if (sumy+last_y >-2 && sumy+last_y <2)
			y = sumy+last_y;
			if (sumz+last_z >-2 && sumz+last_z <2)
			z = sumz+last_z;
			orientation.x = x - last_x;
			orientation.y = y - last_y;
			orientation.z = z - last_z;
			return;
		}

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
		if (dead){
			rotationMatrix = new float[]
							{1, 0, 0, 0.0f,
							0, 1, 0, 0.0f,
							0, 0, 1, 0.0f,
							last_x, last_y, last_z, 1.0f};
			return;
		}
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

	private void moveTail() {
		tail_angle += tail_speed * tail_direction;

		if (tail_angle > 15 || tail_angle < -15) {
			tail_direction *= -1;
		}
	}
	
	// Computes distances between food and sharks
	// If collision with shark, "dies" and floats to top of tank
	private void calcDistances(GL2 gl){
		boolean flag =false;
		List<Food> deleteFoodIdList = new ArrayList<>();
		Coord a = new Coord(last_x,last_y,last_z);
		// food
		for (Food f : v.getFoodList()) {
			Coord b = new Coord(f.x,f.y,f.z);
			if (distance(a, b) < 0.5) {
				gl.glDeleteLists(f.food_object, 1);
				deleteFoodIdList.add(f);
				flag=true;
				f.eaten();
			}
		}
		if (flag) {
			for (Food food : deleteFoodIdList){
				v.getFoodList().remove(food);
			}
		}
	}
	
	// Move the fish around the tank using a combination of potential functions
	// and flipping directions when about to leave tank.
	private void translate() {
//		for (Fish fish : v.getFish()) {
//			if (fish != this){
//				while (distance(new Coord(fish.last_x,fish.last_y,fish.last_z), new Coord(last_x,last_y,last_z)) < 0.5){
//					x = rand.nextFloat()*4 - 2;
//					y = rand.nextFloat()*4 - 2;
//					z = rand.nextFloat()*4 - 2;
//					fish.x = rand.nextFloat()*4 - 2;
//					fish.y = rand.nextFloat()*4 - 2;
//					fish.z = rand.nextFloat()*4 - 2;
//					last_x += trans_speed_x * (x - last_x)>0?1:-1;
//					last_y += trans_speed_y * (y - last_y)>0?1:-1;
//					last_z += trans_speed_z * (z - last_z)>0?1:-1;
//					fish.last_x += trans_speed_x * (fish.x - fish.last_x)>0?1:-1;
//					fish.last_y += trans_speed_y * (fish.y - fish.last_y)>0?1:-1;
//					fish.last_z += trans_speed_z * (fish.z - fish.last_z)>0?1:-1;
//				}
//			}
//		}
		if (dead) {
			if (last_y < 1.9) {
				last_y += trans_speed_y;
			}
		}
		else {
				last_x += trans_speed_x * (x - last_x);
				last_y += trans_speed_y * (y - last_y);
				last_z += trans_speed_z * (z - last_z);
		}
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
	
	// To access shark location
	public void addPredator(Shark s) {
		predator.add(s);
	}

}


