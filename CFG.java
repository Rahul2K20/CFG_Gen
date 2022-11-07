import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CFG {
    private static class Node {
        public char name;
        public List<Node> children = new ArrayList<>();
        public Set<Integer> lines = new HashSet<Integer>();
        public Node(char c, int line) {
            this.name = c;
            this.lines.add(line);
        }
    }

    public List<String>lines;
    public static char name = 'A';
    public static int flag =0;
    private Map<Character, ArrayList<Character>> traverse(Node start, List<Node>visited, Map<Character, ArrayList<Character>> table) {
        visited.add(start);
        List<Node>children = start.children;
        Node current;
        table.put(start.name, printChild(start));
        for(int i=0;i<children.size();i++){
            current = children.get(i);
            if(!visited.contains(current)) traverse(current,visited,table);
        }
        return table;
    }
    public ArrayList<Character> printChild(Node node){
        ArrayList<Character> nodes = new ArrayList<>();
        System.out.print(node.name+"-->(");
        for(int i=0;i<node.children.size();i++){
            System.out.print(node.children.get(i).name);
            if(node.children.size() - i >1)System.out.print(",");
            nodes.add(node.children.get(i).name);
        }
        System.out.println(")");
        System.out.print("node "+node.name +" contains ");
        System.out.println(node.lines);
        return nodes;
    }
    public Node generateCFG(){
        Node root = new Node(name,0);
        name++;
        createChild(root, true);
        ArrayList<Node>visited = new ArrayList<>();
        Map<Character, ArrayList<Character>> table = new HashMap<>();
        traverse(root,visited,table);
        System.out.println();
        return root;
    }
    private Node createChild(Node parent, boolean isMultipleLine) {
        Node statement_node = null ;
        ArrayList<Node> leafNode = new ArrayList<>();
        flag++;
        while(true){
            if(flag>=lines.size())return null;
            if(this.lines.get(flag).contains("else if")){
                Node newNode = new Node(name,flag);
                name++;
                parent.children.add(newNode);
                newNode = createChild(newNode, blockScan(newNode));
                if(newNode == null) return null;
                leafNode.add(newNode);
            }
            else if(this.lines.get(flag).contains("if")){
                Node newNode = new Node(name,flag);
                name++;

                if(!leafNode.isEmpty()){
                    addParents(newNode, leafNode);
                    leafNode.clear();
                }
                else{
                    parent.children.add(newNode);
                }
                parent = newNode;
                newNode = createChild(newNode, blockScan(newNode));
                if(newNode == null) return null;
                leafNode.add(newNode);

                statement_node = null;

            }
            else if(this.lines.get(flag).contains("else")){
                Node newNode = new Node(name,flag);
                name++;
                parent.children.add(newNode);
                newNode = createChild(newNode, blockScan(newNode));
                if(newNode == null) return null;
                leafNode.add(newNode);

            }
            else if(this.lines.get(flag).contains("for") ||
                    ( this.lines.get(flag).contains("while") && !this.lines.get(flag).contains("}while"))){
                Node newNode = new Node(name,flag);
                name++;

                if(!leafNode.isEmpty()){
                    addParents(newNode, leafNode);
                    leafNode.clear();
                }
                else{
                    parent.children.add(newNode);
                }
                parent = newNode;
                leafNode.add(parent);
                newNode = createChild(newNode, blockScan(newNode));
                if(newNode == null) return null;
                newNode.children.add(parent);
                parent = newNode;
                statement_node = null;
            }

            else if(this.lines.get(flag).contains("do")){
                Node newNode = new Node(name,flag);
                name++;

                if(!leafNode.isEmpty()){
                    addParents(newNode, leafNode);
                    leafNode.clear();
                }
                else{
                    parent.children.add(newNode);
                }
                parent = newNode;
                newNode = createChild(newNode, blockScan(newNode));
                if(newNode == null) return null;
                newNode.children.add(parent);
                parent = newNode;
                statement_node = null;
            }
            else{
                if(lines.get(flag).trim().isEmpty()){
                    flag++; continue;
                }
                if(statement_node == null){
                    statement_node = new Node(name, flag);
                    name ++;
                    if(!leafNode.isEmpty()){
                        addParents(statement_node, leafNode);
                        leafNode.clear();
                    }
                    else parent.children.add(statement_node);
                    parent = statement_node;
                }
                else{
                    statement_node.lines.add(flag);
                }
                if(lines.get(flag).contains("}") || ! isMultipleLine) return statement_node;
            }
            flag++;
        }
    }
    private void addParents(Node newNode , ArrayList<Node> leafNode) {

        for(int i=0; i<leafNode.size();i++){
            leafNode.get(i).children.add(newNode);
        }
    }
    private boolean blockScan(Node newNode){
        if(this.lines.get(flag).endsWith("{")) return true;
        flag++;
        while(true){
            if(this.lines.get(flag).endsWith("{")) return true;
            else if(this.lines.get(flag).endsWith(";")){
                flag--;
                return false;
            }
            else newNode.lines.add(flag);
            flag++;
        }
    }


    public void parse(String path) throws IOException {
        List<String> inputLines = new ArrayList<>();
        FileReader fileReader =  new FileReader(path);
        BufferedReader bufferedReader =  new BufferedReader(fileReader);
        String line;
        int i=0;
        System.out.println("Source Code: \n");
        while((line = bufferedReader.readLine()) != null) {
            inputLines.add(line);
            System.out.println(i+ ":" +line);
            i++;
        }
        bufferedReader.close();
        lines = inputLines;
        System.out.println();
        System.out.println("Resultant CFG: \n");
        generateCFG();
    }

    public static void main(String[] args) throws IOException {
        String path = "Input4.txt";
        new CFG().parse(path);
    }
}
