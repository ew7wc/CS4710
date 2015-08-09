import java.awt.Point;

public class Node {

	int xCor = 0;
	int yCor = 0;
	double fx = 0;
	double gx = 0;
	Node parent;

	public Node(int xCor, int yCor, double fx, double gx) {
		super();
		this.xCor = xCor;
		this.yCor = yCor;
		this.fx = fx;
		this.gx = gx;

	}

	public Node(int xCor, int yCor, double fx, double gx, Node parent) {
		super();
		this.xCor = xCor;
		this.yCor = yCor;
		this.fx = fx;
		this.gx = gx;
		this.parent = parent;

	}

	public Node() {
		super();
		this.xCor = 0;
		this.yCor = 0;
		this.fx = 0.0;
		this.gx = 0.0;

	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Node)) {
			return false;
		}
		Node n = (Node) obj;
		if ((n.xCor == this.xCor) && (n.yCor == this.yCor)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		/*return "Node [xCor=" + xCor + ", yCor=" + yCor + ", fx=" + fx + ", gx="
				+ gx + ", parent=" + parent.xCor + "," + parent.yCor + "]";*/
		return "Node [xCor=" + xCor + ", yCor=" + yCor + ", fx=" + fx + ", gx="
				+ gx + "]";
	}
}
