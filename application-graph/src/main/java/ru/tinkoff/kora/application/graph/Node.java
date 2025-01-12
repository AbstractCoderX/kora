package ru.tinkoff.kora.application.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ru.tinkoff.kora.application.graph.GraphImpl.EMPTY_NULLABLE_INDEX;
import static ru.tinkoff.kora.application.graph.GraphImpl.EMPTY_OPTIONAL_INDEX;

public final class Node<T> {
    final ApplicationGraphDraw graphDraw;
    final int index;
    final Graph.Factory<T> factory;
    // leaks for the test purposes
    private final Class<?>[] tags;
    private final List<Node<?>> dependencyNodes;
    private final List<Node<? extends GraphInterceptor<T>>> interceptors;
    private final List<Node<?>> intercepts;
    private final List<Node<?>> dependentNodes;
    private final boolean isValueOf;

    Node(ApplicationGraphDraw graphDraw, int index, Graph.Factory<T> factory, List<Node<?>> dependencyNodes, List<Node<? extends GraphInterceptor<T>>> interceptors, Class<?>[] tags) {
        this.graphDraw = graphDraw;
        this.index = index;
        this.factory = factory;
        this.dependencyNodes = List.copyOf(dependencyNodes);
        this.dependentNodes = new ArrayList<>();
        this.interceptors = List.copyOf(interceptors);
        this.intercepts = new ArrayList<>();
        this.isValueOf = false;
        this.tags = tags;
    }

    private Node(ApplicationGraphDraw graphDraw, int index, Graph.Factory<T> factory, List<Node<?>> dependencyNodes, List<Node<? extends GraphInterceptor<T>>> interceptors, List<Node<?>> dependentNodes, List<Node<?>> intercepts, boolean isValueOf, Class<?>[] tags) {
        this.graphDraw = graphDraw;
        this.index = index;
        this.factory = factory;
        this.dependencyNodes = List.copyOf(dependencyNodes);
        this.interceptors = List.copyOf(interceptors);
        this.dependentNodes = dependentNodes;
        this.intercepts = intercepts;
        this.isValueOf = isValueOf;
        this.tags = tags;
    }

    public Node<T> valueOf() {
        return new Node<>(this.graphDraw, this.index, this.factory, this.dependencyNodes, this.interceptors, this.dependentNodes, this.intercepts, true, this.tags);
    }

    void addDependentNode(Node<?> node) {
        this.dependentNodes.add(node);
    }

    void intercepts(Node<?> node) {
        this.intercepts.add(node);
    }

    public List<Node<?>> getDependentNodes() {
        return Collections.unmodifiableList(this.dependentNodes);
    }

    List<Node<?>> getDependencyNodes() {
        return this.dependencyNodes;
    }

    List<Node<? extends GraphInterceptor<T>>> getInterceptors() {
        return this.interceptors;
    }

    public List<Node<?>> getIntercepts() {
        return Collections.unmodifiableList(this.intercepts);
    }

    boolean isValueOf() {
        return this.isValueOf;
    }

    public Class<?>[] getTags() {
        return tags;
    }

    public static <T> Node<Optional<T>> emptyOptional() {
        return new Node<>(null, EMPTY_OPTIONAL_INDEX, g -> Optional.empty(), List.of(), List.of(), new Class<?>[0]);
    }

    public static <T> Node<T> emptyNullable() {
        return new Node<>(null, EMPTY_NULLABLE_INDEX, g -> null, List.of(), List.of(), new Class<?>[0]);
    }

}
