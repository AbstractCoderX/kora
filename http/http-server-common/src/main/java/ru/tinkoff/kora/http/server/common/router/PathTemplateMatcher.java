package ru.tinkoff.kora.http.server.common.router;


import java.util.*;

public class PathTemplateMatcher<T> {
    /**
     * Map of path template stem to the path templates that share the same base.
     */
    private final Map<String, Set<PathTemplateHolder>> pathTemplateMap = new HashMap<>();

    /**
     * lengths of all registered paths
     */
    private volatile int[] lengths = {};

    /**
     * The result of a path template match.
     *
     * @author Stuart Douglas
     */
    public record PathTemplateMatch<T>(String matchedTemplate, Map<String, String> parameters, T value) {}


    public PathTemplateMatch<T> match(final String path) {
        String normalizedPath = "".equals(path) ? "/" : path;
        final Map<String, String> params = new LinkedHashMap<>();
        int length = normalizedPath.length();
        final int[] lengths = this.lengths;
        for (int i = 0; i < lengths.length; ++i) {
            int pathLength = lengths[i];
            if (pathLength == length) {
                var entry = pathTemplateMap.get(normalizedPath);
                if (entry != null) {
                    var res = handleStemMatch(entry, normalizedPath, params);
                    if (res != null) {
                        return res;
                    }
                }
            } else if (pathLength < length) {
                var part = normalizedPath.substring(0, pathLength);
                var entry = pathTemplateMap.get(part);
                if (entry != null) {
                    var res = handleStemMatch(entry, normalizedPath, params);
                    if (res != null) {
                        return res;
                    }
                }
            }
        }
        return null;
    }

    private PathTemplateMatch<T> handleStemMatch(Set<PathTemplateHolder> entry, final String path, final Map<String, String> params) {
        for (var val : entry) {
            if (val.template.matches(path, params)) {
                return new PathTemplateMatch<>(val.template.templateString(), params, val.value);
            } else {
                params.clear();
            }
        }
        return null;
    }


    public synchronized PathTemplateMatcher<T> add(final PathTemplate template, final T value) {
        var values = pathTemplateMap.get(trimBase(template));
        Set<PathTemplateHolder> newValues;
        if (values == null) {
            newValues = new TreeSet<>();
        } else {
            newValues = new TreeSet<>(values);
        }
        var holder = new PathTemplateHolder(value, template);
        if (newValues.contains(holder)) {
            PathTemplate equivalent = null;
            for (var item : newValues) {
                if (item.compareTo(holder) == 0) {
                    equivalent = item.template;
                    break;
                }
            }
            throw new IllegalStateException("Cannot add path template %s, matcher already contains an equivalent pattern %s".formatted(template.templateString(), equivalent.templateString()));
        }
        newValues.add(holder);
        pathTemplateMap.put(trimBase(template), newValues);
        buildLengths();
        return this;
    }

    private String trimBase(PathTemplate template) {
        String retval = template.base();

        if (template.base().endsWith("/") && !template.parameterNames().isEmpty()) {
            return retval.substring(0, retval.length() - 1);
        }
        if (retval.endsWith("*")) {
            return retval.substring(0, retval.length() - 1);
        }
        return retval;
    }

    private void buildLengths() {
        final var lengths = new TreeSet<Integer>((o1, o2) -> -o1.compareTo(o2));
        for (var p : pathTemplateMap.keySet()) {
            lengths.add(p.length());
        }

        int[] lengthArray = new int[lengths.size()];
        int pos = 0;
        for (int i : lengths) {
            lengthArray[pos++] = i; //-1 because the base paths end with a /
        }
        this.lengths = lengthArray;
    }

    public synchronized PathTemplateMatcher<T> add(final String pathTemplate, final T value) {
        final PathTemplate template = PathTemplate.create(pathTemplate);
        return add(template, value);
    }

    public synchronized PathTemplateMatcher<T> addAll(PathTemplateMatcher<T> pathTemplateMatcher) {
        for (var entry : pathTemplateMatcher.getPathTemplateMap().entrySet()) {
            for (var pathTemplateHolder : entry.getValue()) {
                add(pathTemplateHolder.template, pathTemplateHolder.value);
            }
        }
        return this;
    }

    Map<String, Set<PathTemplateHolder>> getPathTemplateMap() {
        return pathTemplateMap;
    }

    public Set<PathTemplate> getPathTemplates() {
        var templates = new HashSet<PathTemplate>();
        for (var holders : pathTemplateMap.values()) {
            for (var holder : holders) {
                templates.add(holder.template);
            }
        }
        return templates;
    }

    public synchronized PathTemplateMatcher<T> remove(final String pathTemplate) {
        final PathTemplate template = PathTemplate.create(pathTemplate);
        return remove(template);
    }

    private synchronized PathTemplateMatcher<T> remove(PathTemplate template) {
        var values = pathTemplateMap.get(trimBase(template));
        Set<PathTemplateHolder> newValues;
        if (values == null) {
            return this;
        } else {
            newValues = new TreeSet<>(values);
        }
        var it = newValues.iterator();
        while (it.hasNext()) {
            PathTemplateHolder next = it.next();
            if (next.template.templateString().equals(template.templateString())) {
                it.remove();
                break;
            }
        }
        if (newValues.size() == 0) {
            pathTemplateMap.remove(trimBase(template));
        } else {
            pathTemplateMap.put(trimBase(template), newValues);
        }
        buildLengths();
        return this;
    }


    public synchronized T get(String template) {
        var pathTemplate = PathTemplate.create(template);
        var values = pathTemplateMap.get(trimBase(pathTemplate));
        if (values == null) {
            return null;
        }
        for (var next : values) {
            if (next.template.equals(pathTemplate)) {
                return next.value;
            }
        }
        return null;
    }


    private final class PathTemplateHolder implements Comparable<PathTemplateHolder> {
        final T value;
        final PathTemplate template;

        private PathTemplateHolder(T value, PathTemplate template) {
            this.value = value;
            this.template = template;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof PathTemplateMatcher<?>.PathTemplateHolder that) {
                return template.equals(that.template);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return template.hashCode();
        }

        @Override
        public int compareTo(PathTemplateHolder o) {
            return template.compareTo(o.template);
        }
    }
}

