

import javax.media.opengl.*;
import com.jogamp.opengl.util.*;
import java.util.*;

public class Vivarium
{
  private Tank tank;
  private List<Food> foodList;
  private List<Fish> fishList;
  private List<Shark> sharkList;
  public GL2 gl;

  public List<Food> getFoodList() {
    return foodList;
  }

  public List<Shark> getShark() {
    return sharkList;
  }

  public List<Fish> getFish() {
    return fishList;
  }

  public Vivarium()
  {
    tank = new Tank( 4.0f, 4.0f, 4.0f );
    foodList = new ArrayList<>();
    fishList = new ArrayList<>();
    sharkList = new ArrayList<>();
  }

  public void init( GL2 gl )
  {
    tank.init( gl );
    this.gl = gl;
  }

  public void update( GL2 gl )
  {
    tank.update( gl );
    for (Food food : foodList){
      if (food.isFlag() == false){
        food.init( gl );
      }
      food.update( gl );
    }
    for (Fish fish : fishList) {
      if (fish.isFlag() == false) {
        fish.init( gl );
      }
      fish.update(gl);
    }
    for (Shark shark : sharkList) {
      if (shark.isFlag() == false){
        shark.init(gl);
      }
      shark.update(gl);
    }
  }

  public void draw( GL2 gl )
  {
    tank.draw( gl );
    for (Food food : foodList){
      food.draw( gl );
    }
    for (Fish fish : fishList) {
      fish.draw(gl);
    }
    for (Shark shark : sharkList) {
      shark.draw(gl);
    }
  }
}
