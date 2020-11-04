

public class Coord
{
  public double x, y, z;

  public Coord()
  {
    x = y = z = 0.0;
  }

  public Coord( double _x, double _y , double _z)
  {
    x = _x; 
    y = _y;
    z = _z;
  }

  public void normalize() {
    double sum = Math.sqrt(x*x+y*y+z*z);
    x= x/sum;
    y= y/sum;
    z= z/sum;
  }
}
