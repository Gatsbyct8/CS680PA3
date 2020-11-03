/*
 * Fish.java
 * 
 * William Kranich - wkranich@bu.edu
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
	private int box_obj;

	private float scale;
	public boolean dead;

	private float tail_angle;
	private float tail_speed;
	private float tail_direction;

	private float trans_speed_x;
	private float trans_speed_y;
	private float trans_speed_z;
	
	public float boundingSphereRadius;
	private boolean showBoundingSphere;
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
		scale = 0.35f;
		tail_speed = 1f;
		tail_angle = 0;
		tail_direction = 1;
		trans_speed_x = 0.005f;
		trans_speed_y = 0.005f;
		trans_speed_z = 0.005f;

		boundingSphereRadius = 0.35f;
		showBoundingSphere = false;
		
		v = _v;
		predator = v.getShark();

		dead = false;
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
		//gl.glRotatef(90,1f,0f,0f);
		gl.glRotatef(90,0f,1f,0f);
		//gl.glRotatef(90,0f,0f,1f);
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
			} else {
				if (distance(new Coord(x, y, z), new Coord(last_x, last_y, last_z)) < 0.5) {
					potentialFunction(this, v.getFish(), v.getShark());
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

	private void potentialFunction(Fish fish, List<Fish> fish1, List<Shark> shark) {
		float sumx = 0f;
		float sumy = 0f;
		float sumz = 0f;
		for (Shark s: shark){
			sumx += (s.last_x);
			sumy += (s.last_y);
			sumz += (s.last_z);
		}
		if (shark.size()!=0) {
			sumx /= shark.size();
			sumy /= shark.size();
			sumz /= shark.size();
			x = sumx >0?-2:2;
			y = sumy >0?-2:2;
			z = sumz >0?-2:2;
			return;
		}else{
			x = rand.nextFloat()*3.6f - 1.8f;
			y = rand.nextFloat()*3.6f - 1.8f;
			z = rand.nextFloat()*3.6f - 1.8f;
		}
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
		float dx = last_x - x;
		float dy = 0f;
		float dz = last_z - z;

		float mag = (float) Math.sqrt(dx * dx + dz * dz);
		float[] v = new float[3];
		v[0] = dx / mag;
		v[1] = 0;
		v[2] = dz / mag;

		rotationMatrix = new float[]
				{v[0], 0, v[2], 0.0f,
				0, 1, 0, 0.0f,
				-v[2], 0, v[0], 0.0f,
				last_x, last_y, last_z, 1.0f};
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


