import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {
		@Override
		public int compare(Node o1, Node o2) {
			if (o1.fx < o2.fx) {
				return -1;
			}
			if (o1.fx > o2.fx) {
				return 1;
			}
			return 0;
		}
	}