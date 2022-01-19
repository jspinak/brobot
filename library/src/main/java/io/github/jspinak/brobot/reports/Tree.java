package io.github.jspinak.brobot.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Aims to visualize the State hierarchy. Not yet implemented.
 */
public class Tree {

    private String name;
    private List<Tree> children = new ArrayList<>();

    public Tree(String name) {
        this.name = name;
    }

    public Tree(String name, List<Tree> children) {
        this.children = children;
    }

    public void addChild(Tree child) {
        children.add(child);
    }
}
