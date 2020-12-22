import java.util.*;
import java.io.*;

public class hashtagcounter {

    BufferedReader br;
    PrintWriter out;
    StringTokenizer st;
    boolean eof;

    void solve() throws IOException {
        long start = System.currentTimeMillis();
        Map<String, maxFibonacciHeap.Node> map = new HashMap<>();
        maxFibonacciHeap heap = new maxFibonacciHeap();
        while (true) {
            String params[] = nextString().split(" ");
            if (params[0].equalsIgnoreCase("stop")) break;
            if (params.length == 2) {
                String hashTag = params[0].substring(1);
                Double priority = Double.parseDouble(params[1]);
                if (map.containsKey(hashTag)) {
                    heap.increaseKey(map.get(hashTag), map.get(hashTag).priority + priority);
                } else {
                    map.put(hashTag, heap.insert(hashTag, priority));
                }
            } else {
                int n = Integer.parseInt(params[0]);
                maxFibonacciHeap.Node[] toAdd = new maxFibonacciHeap.Node[n];
                for (int i = 0; i < n; i++) {
                    maxFibonacciHeap.Node node = heap.extractMax();
                    toAdd[i] = new maxFibonacciHeap.Node(node.hashTag, node.priority);
                    out.print(node.hashTag);
                    if (i != n - 1) out.print(',');
                }
                out.println();
                for (maxFibonacciHeap.Node x : toAdd) {
                    map.put(x.hashTag, heap.insert(x.hashTag, x.priority));
                }
            }
        }
        long end = System.currentTimeMillis();
        long totalTime = end - start;
        System.out.println("Total time in Milli Seconds: " + totalTime);
    }

    hashtagcounter(String inputFilePath, String outputFilePath) throws IOException {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath)));
        if(outputFilePath.length()>0)
            out = new PrintWriter(new FileWriter(outputFilePath));
        else out = new PrintWriter(System.out);
        solve();
        out.close();
    }

    public static void main(String[] args) throws IOException {
        new hashtagcounter(args[0], args.length > 1 ? args[1] : "");
    }

    String nextToken() {
        while (st == null || !st.hasMoreTokens()) {
            try {
                st = new StringTokenizer(br.readLine());
            } catch (Exception e) {
                eof = true;
                return null;
            }
        }
        return st.nextToken();
    }

    String nextString() {
        try {
            return br.readLine();
        } catch (IOException e) {
            eof = true;
            return null;
        }
    }

    int nextInt() throws IOException {
        return Integer.parseInt(nextToken());
    }

    long nextLong() throws IOException {
        return Long.parseLong(nextToken());
    }

    double nextDouble() throws IOException {
        return Double.parseDouble(nextToken());
    }

}

final class maxFibonacciHeap {
    static final class Node {
        private int degree = 0;
        private boolean isMarked = false;
        private Node next, prev, parent, child;
        String hashTag;
        double priority;

        Node(String tag, double priority) {
            next = prev = this;
            hashTag = tag;
            this.priority = priority;
        }
    }

    private Node max = null;
    private int size = 0;

    public Node insert(String hashTag, double priority) {
        if (Double.isNaN(priority)) {
            throw new IllegalArgumentException(priority + " is invalid!");
        }
        Node node = new Node(hashTag, priority);
        max = mergeLists(max, node);
        size++;
        return node;
    }

    private static Node mergeLists(Node a, Node b) {
        if (a == null && b == null) {
            return null;
        } else if (a != null && b == null) {
            return a;
        } else if (a == null && b != null) {
            return b;
        } else {
            Node aNext = a.next;
            a.next = b.next;
            a.next.prev = a;
            b.next = aNext;
            b.next.prev = b;
            return a.priority > b.priority ? a : b;
        }
    }

    public boolean isEmpty() {
        return max == null;
    }

    public Node max() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return max;
    }

    public maxFibonacciHeap merge(maxFibonacciHeap x, maxFibonacciHeap y) {
        maxFibonacciHeap res = new maxFibonacciHeap();
        res.max = mergeLists(x.max, y.max);
        res.size = x.size + y.size;
        x.size = y.size = 0;
        x.max = y.max = null;
        return res;
    }

    public Node extractMax() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty.");
        }
        Node maxNode = max;
        if (max.next == max) {
            max = null;
        } else {
            max.prev.next = max.next;
            max.next.prev = max.prev;
            max = max.next;
        }
        if (maxNode.child != null) {
            Node cur = maxNode.child;
            do {
                cur.parent = null;
                cur = cur.next;
            } while (cur != maxNode.child);
        }
        max = mergeLists(max, maxNode.child);
        if (max == null) return maxNode;
        //degree wise merging
        List<Node> degreeTable = new ArrayList<>();
        List<Node> toVisit = new ArrayList<>();
        for (Node cur = max; toVisit.isEmpty() || toVisit.get(0) != cur; cur = cur.next) {
            toVisit.add(cur);
        }
        for (Node cur : toVisit) {
            while (true) {
                while (cur.degree >= degreeTable.size()) {
                    degreeTable.add(null);
                }
                if (degreeTable.get(cur.degree) == null) {
                    degreeTable.set(cur.degree, cur);
                    break;
                }
                Node other = degreeTable.get(cur.degree);
                degreeTable.set(cur.degree, null);
                Node min = other.priority < cur.priority ? other : cur;
                Node max = other.priority < cur.priority ? cur : other;

                min.next.prev = min.prev;
                min.prev.next = min.next;
                min.next = min.prev = min;
                max.child = mergeLists(max.child, min);
                min.parent = max;
                min.isMarked = false;
                ++max.degree;
                cur = max;
            }
            if (cur.priority >= max.priority) max = cur;
        }
        --size;
        return maxNode;
    }

    public void increaseKey(Node node, double newPriority) {
        if (Double.isNaN(newPriority)) {
            throw new IllegalArgumentException(newPriority + " is invalid!");
        }
        increaseKeyUnchecked(node, newPriority);
    }

    private void increaseKeyUnchecked(Node node, double priority) {
        node.priority = priority;
        if (node.parent != null && node.priority >= node.parent.priority) {
            cutNode(node);
        }
        if (node.priority >= max.priority) {
            max = node;
        }
    }

    private void cutNode(Node node) {
        node.isMarked = false;
        if (node.parent == null) return;
        if (node.next != node) {
            node.next.prev = node.prev;
            node.prev.next = node.next;
        }
        if (node.parent.child == node) {
            if (node.next != node) {
                node.parent.child = node.next;
            } else {
                node.parent.child = null;
            }
        }
        --node.parent.degree;
        node.prev = node.next = node;
        max = mergeLists(max, node);
        if (node.parent.isMarked) {
            cutNode(node.parent);
        } else {
            node.parent.isMarked = true;
        }
        node.parent = null;
    }
}



