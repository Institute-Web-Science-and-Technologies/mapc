package mas.agents0815;

public class FromTo {
	private String x;
	private String y;

	public FromTo(String vertex1, String vertex2) {
		this.x = vertex1;
		this.y = vertex2;
	}

	public String getX() {
		return x;
	}

	public String getY() {
		return y;
	}

	public boolean equals(FromTo f) {
		return (getX().equals(f.getX()) && getY().equals( f.getY()));
	}

}
