package com.example.sics.qr_scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class HuffMan {

    static String encMsg="";
    static String decMsg="";
    private HuffMan() {};

    private static class HuffmanNode {
        char ch;
        int frequency;
        HuffmanNode left;
        HuffmanNode right;

        HuffmanNode(char ch, int frequency,  HuffmanNode left,  HuffmanNode right) {
            this.ch = ch;
            this.frequency = frequency;
            this.left = left;
            this.right = right;
        }
    }

    private static class HuffManComparator implements Comparator<HuffmanNode> {
        @Override
        public int compare(HuffmanNode node1, HuffmanNode node2) {
            return node1.frequency - node2.frequency;
        }
    }

    /**
     * Compresses the string using huffman algorithm.
     * The huffman tree and the huffman code are serialized to disk
     *
     * @param sentence                  The sentence to be serialized
     * @throws FileNotFoundException    If file is not found
     * @throws IOException              If IO exception occurs.
     */
    public static String compress(String sentence) throws FileNotFoundException, IOException {
        if (sentence == null) {
            throw new NullPointerException("Input sentence cannot be null.");
        }
        if (sentence.length() == 0) {
            throw new IllegalArgumentException("The string should atleast have 1 character.");
        }

        final Map<Character, Integer> charFreq = getCharFrequency(sentence);
        final HuffmanNode root = buildTree(charFreq);
        final Map<Character, String> charCode = generateCodes(charFreq.keySet(), root);
        final String encodedMessage = encodeMessage(charCode, sentence);
        serializeTree(root);
        serializeMessage(encodedMessage);
        return encMsg;
    }

    private static Map<Character, Integer> getCharFrequency(String sentence) {
        final Map<Character, Integer> map = new HashMap<Character, Integer>();

        for (int i = 0; i < sentence.length(); i++) {
            char ch = sentence.charAt(i);
            if (!map.containsKey(ch)) {
                map.put(ch, 1);
            } else {
                int val = map.get(ch);
                map.put(ch, ++val);
            }
        }

        return map;
    }


    /**
     * Map<Character, Integer> map
     * Some implementation of that treeSet is passed as parameter.
     * @param map
     */
    private static HuffmanNode buildTree(Map<Character, Integer> map) {
        final Queue<HuffmanNode> nodeQueue = createNodeQueue(map);

        while (nodeQueue.size() > 1) {
            final HuffmanNode node1 = nodeQueue.remove();
            final HuffmanNode node2 = nodeQueue.remove();
            HuffmanNode node = new HuffmanNode('\0', node1.frequency + node2.frequency, node1, node2);
            nodeQueue.add(node);
        }

        // remove it to prevent object leak.
        return nodeQueue.remove();
    }

    private static Queue<HuffmanNode> createNodeQueue(Map<Character, Integer> map) {
        final Queue<HuffmanNode> pq = new PriorityQueue<HuffmanNode>(11, new HuffManComparator());
        for (Entry<Character, Integer> entry : map.entrySet()) {
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue(), null, null));
        }
        return pq;
    }

    private static Map<Character, String> generateCodes(Set<Character> chars, HuffmanNode node) {
        final Map<Character, String> map = new HashMap<Character, String>();
        doGenerateCode(node, map, "");
        return map;
    }


    private static void doGenerateCode(HuffmanNode node, Map<Character, String> map, String s) {
        if (node.left == null && node.right == null) {
            map.put(node.ch, s);
            return;
        }
        doGenerateCode(node.left, map, s + '0');
        doGenerateCode(node.right, map, s + '1' );
    }


    private static String encodeMessage(Map<Character, String> charCode, String sentence) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < sentence.length(); i++) {
            stringBuilder.append(charCode.get(sentence.charAt(i)));
        }
        System.out.println(stringBuilder);
        encMsg=stringBuilder.toString();
        return stringBuilder.toString();
    }

    private static void serializeTree(HuffmanNode node) throws FileNotFoundException, IOException {
        final BitSet bitSet = new BitSet();
        try (ObjectOutputStream oosTree = new ObjectOutputStream(new FileOutputStream("tree"))) {
            try (ObjectOutputStream oosChar = new ObjectOutputStream(new FileOutputStream("char"))) {
                IntObject o = new IntObject();
                preOrder(node, oosChar, bitSet, o);
                bitSet.set(o.bitPosition, true); // padded to mark end of bit set relevant for deserialization.
                oosTree.writeObject(bitSet);
            }
        }
    }

    private static class IntObject {
        int bitPosition;
    }


    private static void preOrder(HuffmanNode node, ObjectOutputStream oosChar, BitSet bitSet, IntObject intObject) throws IOException {
        if (node.left == null && node.right == null) {
            bitSet.set(intObject.bitPosition++, false);  // register branch in bitset
            oosChar.writeChar(node.ch);
            return;                                  // DONT take the branch.
        }
        bitSet.set(intObject.bitPosition++, true);           // register branch in bitset
        preOrder(node.left, oosChar, bitSet, intObject); // take the branch.

        bitSet.set(intObject.bitPosition++, true);               // register branch in bitset
        preOrder(node.right, oosChar, bitSet, intObject);    // take the branch.
    }

    private static void serializeMessage(String message) throws IOException {
        final BitSet bitSet = getBitSet(message);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("encodedMessage"))){

            oos.writeObject(bitSet);
        }
    }

    private static BitSet getBitSet(String message) {
        final BitSet bitSet = new BitSet();
        int i = 0;
        for (i = 0; i < message.length(); i++) {
            if (message.charAt(i) == '0') {
                bitSet.set(i, false);
            } else {
                bitSet.set(i, true);
            }
        }
        bitSet.set(i, true); // dummy bit set to know the length
        return bitSet;
    }

    /**
     * Retrieves back the original string.
     */
    public static String expand() throws FileNotFoundException, ClassNotFoundException, IOException {
        final HuffmanNode root = deserializeTree();
        return decodeMessage(root);
    }

    private static HuffmanNode deserializeTree() throws FileNotFoundException, IOException, ClassNotFoundException {
        try (ObjectInputStream oisBranch = new ObjectInputStream(new FileInputStream("tree"))) {
            try (ObjectInputStream oisChar = new ObjectInputStream(new FileInputStream("char"))) {
                final BitSet bitSet = (BitSet) oisBranch.readObject();
                return preOrder(bitSet, oisChar, new IntObject());
            }
        }
    }


    private static HuffmanNode preOrder(BitSet bitSet, ObjectInputStream oisChar, IntObject o) throws IOException {
        // created the node before reading whats registered.
        final HuffmanNode node = new HuffmanNode('\0', 0, null, null);

        // reading whats registered and determining if created node is the leaf or non-leaf.
        if (!bitSet.get(o.bitPosition)) {
            o.bitPosition++;              // feed the next position to the next stack frame by doing computation before preOrder is called.
            node.ch = oisChar.readChar();
            return node;
        }

        o.bitPosition = o.bitPosition + 1;  // feed the next position to the next stack frame by doing computation before preOrder is called.
        node.left = preOrder(bitSet, oisChar, o);

        o.bitPosition = o.bitPosition + 1; // feed the next position to the next stack frame by doing computation before preOrder is called.
        node.right = preOrder(bitSet, oisChar, o);

        return node;
    }

    private static String decodeMessage(HuffmanNode node) throws FileNotFoundException, IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("encodedMessage"))) {
            final BitSet bitSet = (BitSet) ois.readObject();
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < (bitSet.length() - 1);) {
                HuffmanNode temp = node;
                // since huffman code generates full binary tree, temp.right is certainly null if temp.left is null.
                while (temp.left != null) {
                    if (!bitSet.get(i)) {
                        temp = temp.left;
                    } else {
                        temp = temp.right;
                    }
                    i = i + 1;
                }
                stringBuilder.append(temp.ch);
            }
            System.out.println(stringBuilder);
            return stringBuilder.toString();
        }
    }

}
