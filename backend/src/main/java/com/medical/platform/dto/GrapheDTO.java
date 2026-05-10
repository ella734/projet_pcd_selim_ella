package com.medical.platform.dto;

import java.util.*;

public class GrapheDTO {

    private List<NodeDTO> nodes = new ArrayList<>();
    private List<EdgeDTO> edges = new ArrayList<>();

    public List<NodeDTO> getNodes() { return nodes; }
    public List<EdgeDTO> getEdges() { return edges; }

    public void addNode(String id, String label, String type) {
        addNode(id, label, type, null);
    }

    public void addNode(String id, String label, String type, String subLabel) {
        nodes.add(new NodeDTO(id, label, type, subLabel));
    }

    public void addEdge(String id, String source, String target, String type, String label) {
        edges.add(new EdgeDTO(id, source, target, type, label));
    }

    public static class NodeDTO {
        private String id;
        private String label;
        private String type;
        private String subLabel;

        public NodeDTO(String id, String label, String type, String subLabel) {
            this.id = id;
            this.label = label;
            this.type = type;
            this.subLabel = subLabel;
        }

        public String getId() { return id; }
        public String getLabel() { return label; }
        public String getType() { return type; }
        public String getSubLabel() { return subLabel; }
    }

    public static class EdgeDTO {
        private String id;
        private String source;
        private String target;
        private String type;
        private String label;

        public EdgeDTO(String id, String source, String target, String type, String label) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.type = type;
            this.label = label;
        }

        public String getId() { return id; }
        public String getSource() { return source; }
        public String getTarget() { return target; }
        public String getType() { return type; }
        public String getLabel() { return label; }
    }
}
