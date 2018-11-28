package net.seancallahan.opus.util;

public class BinaryTree<T>
{
    private Node root;

    public BinaryTree(T rootValue)
    {
        this.root = new Node(rootValue);
    }

    public Node getRoot()
    {
        return root;
    }

    public class Node
    {
        private T value;
        private Node left;
        private Node right;

        public Node(T value)
        {
            this.value = value;
        }

        public T getValue()
        {
            return value;
        }

        public void setValue(T value)
        {
            this.value = value;
        }

        public Node getLeft()
        {
            return left;
        }

        public void setLeft(Node left)
        {
            this.left = left;
        }

        public Node getRight()
        {
            return right;
        }

        public void setRight(Node right)
        {
            this.right = right;
        }
    }
}
