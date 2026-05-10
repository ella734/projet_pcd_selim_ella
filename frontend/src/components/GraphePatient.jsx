import { useState, useEffect, useCallback } from "react";
import ReactFlow, { Background, Controls, MiniMap } from "reactflow";
import "reactflow/dist/style.css";
import api from "../services/api";

const NODE_STYLES = {
  PATIENT: {
    background: "#1B4F8A",
    color: "white",
    border: "2px solid #0d2d52",
    borderRadius: "50%",
    padding: "14px 18px",
    fontWeight: "bold",
    fontSize: "0.85rem",
    minWidth: 120,
    textAlign: "center",
  },
  MEDICAMENT: {
    background: "#c0392b",
    color: "white",
    border: "2px solid #922b21",
    borderRadius: "8px",
    padding: "8px 14px",
    fontWeight: "600",
    fontSize: "0.8rem",
    textAlign: "center",
  },
  COMORBIDITE: {
    background: "#e67e22",
    color: "white",
    border: "2px solid #ca6f1e",
    borderRadius: "8px",
    padding: "8px 14px",
    fontWeight: "600",
    fontSize: "0.8rem",
    textAlign: "center",
  },
};

const EDGE_STYLES = {
  PREND: { stroke: "#2980b9", strokeWidth: 1.5, strokeDasharray: "5,3" },
  A_COMORBIDITE: { stroke: "#e67e22", strokeWidth: 1.5 },
  INTERAGIT_AVEC: { stroke: "#c0392b", strokeWidth: 2.5 },
};

function buildReactFlowGraph(grapheDTO) {
  const { nodes: rawNodes, edges: rawEdges } = grapheDTO;
  if (!rawNodes || rawNodes.length === 0) return { nodes: [], edges: [] };

  const patientNode = rawNodes.find((n) => n.type === "PATIENT");
  const medNodes = rawNodes.filter((n) => n.type === "MEDICAMENT");
  const comNodes = rawNodes.filter((n) => n.type === "COMORBIDITE");

  const positions = {};

  // Patient au centre
  if (patientNode) positions[patientNode.id] = { x: 400, y: 300 };

  // Médicaments : arc supérieur
  const medRadius = 220;
  medNodes.forEach((n, i) => {
    const total = medNodes.length;
    const angle = (Math.PI / (total + 1)) * (i + 1) - Math.PI / 2;
    positions[n.id] = {
      x: 400 + medRadius * Math.cos(angle),
      y: 300 + medRadius * Math.sin(angle),
    };
  });

  // Comorbidités : arc inférieur
  const comRadius = 200;
  comNodes.forEach((n, i) => {
    const total = comNodes.length;
    const angle = (Math.PI / (total + 1)) * (i + 1) + Math.PI / 2;
    positions[n.id] = {
      x: 400 + comRadius * Math.cos(angle),
      y: 300 + comRadius * Math.sin(angle),
    };
  });

  const nodes = rawNodes.map((n) => ({
    id: n.id,
    data: {
      label: (
        <div>
          <div>{n.label}</div>
          {n.subLabel && (
            <div style={{ fontSize: "0.7rem", opacity: 0.85, marginTop: 2 }}>
              {n.subLabel}
            </div>
          )}
        </div>
      ),
    },
    position: positions[n.id] || { x: 0, y: 0 },
    style: NODE_STYLES[n.type] || {},
  }));

  const edges = rawEdges.map((e) => ({
    id: e.id,
    source: e.source,
    target: e.target,
    label: e.label,
    style: EDGE_STYLES[e.type] || { stroke: "#888" },
    labelStyle: {
      fill: e.type === "INTERAGIT_AVEC" ? "#c0392b" : "#555",
      fontWeight: e.type === "INTERAGIT_AVEC" ? "bold" : "normal",
      fontSize: "0.72rem",
    },
    animated: e.type === "INTERAGIT_AVEC",
  }));

  return { nodes, edges };
}

export default function GraphePatient({ patientId }) {
  const [rfNodes, setRfNodes] = useState([]);
  const [rfEdges, setRfEdges] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [stats, setStats] = useState({ meds: 0, comorbidites: 0, interactions: 0 });

  const charger = useCallback(() => {
    if (!patientId) return;
    setLoading(true);
    setError(null);

    api.get(`/graphe/patient/${patientId}`)
      .then(({ data }) => {
        const { nodes, edges } = buildReactFlowGraph(data);
        setRfNodes(nodes);
        setRfEdges(edges);
        setStats({
          meds: (data.nodes || []).filter((n) => n.type === "MEDICAMENT").length,
          comorbidites: (data.nodes || []).filter((n) => n.type === "COMORBIDITE").length,
          interactions: (data.edges || []).filter((e) => e.type === "INTERAGIT_AVEC").length,
        });
      })
      .catch(() => setError("Graphe de connaissance Neo4j non disponible."))
      .finally(() => setLoading(false));
  }, [patientId]);

  useEffect(() => { charger(); }, [charger]);

  const legend = [
    { color: "#1B4F8A", label: "Patient" },
    { color: "#c0392b", label: "Médicament" },
    { color: "#e67e22", label: "Comorbidité" },
  ];

  if (loading) return <p style={{ color: "#888", padding: "1rem" }}>Chargement du graphe de connaissance…</p>;

  if (error) return (
    <div style={{ padding: "1rem", background: "#fff8e1", border: "1px solid #ffe082", borderRadius: 8, color: "#795548" }}>
      {error}
    </div>
  );

  if (rfNodes.length === 0) return (
    <div style={{ padding: "1rem", background: "#f9f9f9", border: "1px solid #eee", borderRadius: 8, color: "#888" }}>
      Aucune donnée dans le graphe pour ce patient. Synchronisez via <strong>Admin → Sync</strong>.
    </div>
  );

  return (
    <div>
      {/* Statistiques */}
      <div style={{ display: "flex", gap: "0.75rem", marginBottom: "0.75rem", flexWrap: "wrap" }}>
        {[
          { label: "Médicaments", value: stats.meds, color: "#c0392b" },
          { label: "Comorbidités", value: stats.comorbidites, color: "#e67e22" },
          { label: "Interactions", value: stats.interactions, color: "#8e44ad" },
        ].map((s) => (
          <div key={s.label} style={{
            padding: "0.4rem 1rem", background: "#f8f9fa",
            borderLeft: `4px solid ${s.color}`, borderRadius: 6,
            fontSize: "0.85rem", color: "#333"
          }}>
            <strong style={{ color: s.color }}>{s.value}</strong> {s.label}
          </div>
        ))}
      </div>

      {/* Graphe ReactFlow */}
      <div style={{ width: "100%", height: 480, border: "1px solid #e2e8f0", borderRadius: 10, overflow: "hidden" }}>
        <ReactFlow nodes={rfNodes} edges={rfEdges} fitView>
          <Background color="#f0f4f8" gap={20} />
          <Controls />
          <MiniMap
            nodeColor={(n) => {
              if (n.style?.background) return n.style.background;
              return "#ccc";
            }}
          />
        </ReactFlow>
      </div>

      {/* Légende */}
      <div style={{ display: "flex", gap: "1rem", marginTop: "0.6rem", flexWrap: "wrap" }}>
        {legend.map((l) => (
          <div key={l.label} style={{ display: "flex", alignItems: "center", gap: 6, fontSize: "0.8rem", color: "#555" }}>
            <div style={{ width: 12, height: 12, borderRadius: "50%", background: l.color }} />
            {l.label}
          </div>
        ))}
        <div style={{ display: "flex", alignItems: "center", gap: 6, fontSize: "0.8rem", color: "#c0392b" }}>
          <div style={{ width: 20, height: 2, background: "#c0392b" }} />
          Interaction ⚠
        </div>
      </div>
    </div>
  );
}
