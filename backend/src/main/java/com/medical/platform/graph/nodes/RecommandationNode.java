package com.medical.platform.graph.nodes;

import org.springframework.data.neo4j.core.schema.*;
import java.util.*;

@Node("Recommandation")
public class RecommandationNode {

    @Id
    private String id;
    private String condition;
    private String conseil;
    private String source;
    private String niveau;
    private String type;

    @Relationship(type = "S_APPLIQUE_A", direction = Relationship.Direction.OUTGOING)
    private List<ComorbiditeNode> comorbidites = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getConseil() { return conseil; }
    public void setConseil(String conseil) { this.conseil = conseil; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<ComorbiditeNode> getComorbidites() { return comorbidites; }
    public void setComorbidites(List<ComorbiditeNode> comorbidites) { this.comorbidites = comorbidites; }
}
