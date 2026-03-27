import { useState, useRef, useEffect, useCallback } from "react";

const EXAMPLES = [
  "3bhk flat in bandra under 1 crore ready to move",
  "2bhk apartment in greater noida under 80 lakhs",
  "villa in koramangala bangalore above 2 crore with gym",
  "furnished studio apartment for rent in delhi under 25k",
  "2bhk in grater noida under 80L",
  "4bhk penthouse worli sea facing north east facing",
  "plot in hinjewadi pune under 50 lakhs",
  "3bhk flat malviya nagar",
  "office space in bkc above 2 crore rera registered",
  "independent house vasant kunj no broker",
];

function formatPrice(p) {
  if (p >= 10000000) return `₹${(p / 10000000).toFixed(p % 10000000 === 0 ? 0 : 2)} Cr`;
  if (p >= 100000) return `₹${(p / 100000).toFixed(p % 100000 === 0 ? 0 : 1)} L`;
  return `₹${p.toLocaleString()}`;
}

export default function App() {
  const [query, setQuery] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [key, setKey] = useState(0);
  const inputRef = useRef(null);

  useEffect(() => { inputRef.current?.focus(); }, []);

  const parse = useCallback(async (q = query, geoOverride = null) => {
    if (!q.trim()) return;
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("/api/parse", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ query: q, geoOverride }),
      });
      if (!res.ok) throw new Error(`API error: ${res.status}`);
      const data = await res.json();
      setResult(data);
      setKey(k => k + 1);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [query]);

  const pickCity = (cityId) => parse(result?.raw, cityId);

  const e = result?.entities || {};
  const p = result?.params || {};
  const hasError = p.status === "NO_LOCATION" || p.status === "AWAITING_CITY_DISAMBIGUATION";
  const isAmbiguous = p.status === "AWAITING_CITY_DISAMBIGUATION";
  const isNoLocation = p.status === "NO_LOCATION";

  const Tag = ({ label, val, color = "#3b82f6" }) => (
    <span style={{
      display: "inline-flex", alignItems: "center", gap: 5,
      background: `${color}18`, border: `1px solid ${color}40`,
      borderRadius: 6, padding: "3px 10px", fontSize: 12, color, fontWeight: 600,
      fontFamily: "'DM Mono',monospace", whiteSpace: "nowrap",
    }}>
      {label && <span style={{ opacity: 0.6, fontSize: 11 }}>{label}:</span>}
      {val}
    </span>
  );

  const Section = ({ title, dot, children }) => (
    <div style={{
      background: "#0f1117", border: "1px solid #1e2433",
      borderRadius: 10, overflow: "hidden", marginBottom: 12,
    }}>
      <div style={{
        background: "#141824", borderBottom: "1px solid #1e2433",
        padding: "10px 18px", display: "flex", alignItems: "center", gap: 8,
      }}>
        <div style={{ width: 7, height: 7, borderRadius: "50%", background: dot }} />
        <span style={{ fontSize: 11, color: "#8892a4", letterSpacing: "0.8px", fontFamily: "'DM Mono',monospace" }}>{title}</span>
      </div>
      <div style={{ padding: "14px 18px" }}>{children}</div>
    </div>
  );

  return (
    <div style={{
      minHeight: "100vh", background: "#070b12",
      fontFamily: "'DM Mono', 'Fira Code', monospace", color: "#c9d1e0",
    }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=DM+Mono:wght@300;400;500&family=Syne:wght@700;800&display=swap');
        *{box-sizing:border-box}
        ::placeholder{color:#2e3a4e}
        ::-webkit-scrollbar{width:4px;height:4px}
        ::-webkit-scrollbar-track{background:#0a0e18}
        ::-webkit-scrollbar-thumb{background:#1e2a3a;border-radius:2px}
        @keyframes fadeUp{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:translateY(0)}}
        @keyframes pulse{0%,100%{opacity:1}50%{opacity:0.5}}
        .example-btn:hover{background:#1a2233!important;border-color:#3b82f6!important;color:#93c5fd!important}
        .parse-btn:hover{background:#2563eb!important}
      `}</style>

      {/* Header */}
      <div style={{
        borderBottom: "1px solid #111827", padding: "18px 28px",
        display: "flex", alignItems: "center", gap: 14,
        background: "linear-gradient(180deg,#0d1220 0%,#070b12 100%)",
      }}>
        <div style={{
          background: "linear-gradient(135deg,#3b82f6,#2563eb)",
          color: "#fff", fontSize: 11, fontWeight: 700,
          padding: "4px 10px", borderRadius: 4, letterSpacing: "1.5px",
        }}>REAL ESTATE</div>
        <div style={{ color: "#2e3a4e", fontSize: 12, letterSpacing: "0.5px" }}>Free Text Search — NLP Query Parser</div>
        <div style={{
          marginLeft: "auto", background: "#0f1a2e",
          border: "1px solid #1a2a40", borderRadius: 5,
          padding: "3px 12px", fontSize: 10, color: "#3b82f6", letterSpacing: "1px",
        }}>
          <span style={{ color: "#22c55e", marginRight: 6 }}>●</span>
          JAVA API
        </div>
      </div>

      <div style={{ padding: "24px 28px", maxWidth: 960, margin: "0 auto" }}>

        {/* Input */}
        <div style={{
          background: "#0f1117", border: "1px solid #1e2433",
          borderRadius: 10, padding: 4, display: "flex", gap: 6, marginBottom: 16,
          boxShadow: "0 0 0 1px rgba(59,130,246,0.08)",
        }}>
          <input ref={inputRef} value={query}
            onChange={e => setQuery(e.target.value)}
            onKeyDown={e => e.key === "Enter" && parse()}
            placeholder="3bhk flat in bandra under 1 crore ready to move..."
            style={{
              flex: 1, background: "transparent", border: "none", outline: "none",
              color: "#e2e8f0", fontSize: 15, padding: "12px 16px",
              fontFamily: "'DM Mono',monospace",
            }}
          />
          <button className="parse-btn" onClick={() => parse()}
            disabled={loading}
            style={{
              background: loading ? "#1e3a5f" : "#1d4ed8", border: "none", borderRadius: 7,
              color: "#fff", fontFamily: "'DM Mono',monospace",
              fontSize: 11, fontWeight: 700, padding: "10px 22px",
              cursor: loading ? "wait" : "pointer", letterSpacing: "1px", transition: "background 0.15s",
            }}>
            {loading ? "PARSING..." : "PARSE →"}
          </button>
        </div>

        {/* Error */}
        {error && (
          <div style={{
            background: "#1a0a0a", border: "1px solid #dc2626",
            borderRadius: 8, padding: "10px 16px", marginBottom: 12,
            color: "#fca5a5", fontSize: 12,
          }}>⚠ API Error: {error}</div>
        )}

        {/* Examples */}
        <div style={{ marginBottom: 28 }}>
          <div style={{ fontSize: 10, color: "#2e3a4e", letterSpacing: "1.2px", marginBottom: 8 }}>EXAMPLES:</div>
          <div style={{ display: "flex", flexWrap: "wrap", gap: 6 }}>
            {EXAMPLES.map((ex, i) => (
              <button key={i} className="example-btn"
                onClick={() => { setQuery(ex); parse(ex); }}
                style={{
                  background: "#0f1117", border: "1px solid #1e2433", borderRadius: 5,
                  color: "#4b5a6e", fontFamily: "'DM Mono',monospace", fontSize: 10,
                  padding: "5px 10px", cursor: "pointer", transition: "all 0.12s",
                }}>{ex}</button>
            ))}
          </div>
        </div>

        {/* Results */}
        {result && (
          <div key={key} style={{ animation: "fadeUp 0.25s ease" }}>

            {/* Fuzzy corrections */}
            {e.fuzzyMatches?.length > 0 && (
              <div style={{
                background: "#0c1a0f", border: "1px solid #166534",
                borderRadius: 8, padding: "10px 16px", marginBottom: 12,
                display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center",
              }}>
                <span style={{ fontSize: 10, color: "#4ade80", letterSpacing: "1px" }}>✏ TYPO CORRECTIONS:</span>
                {e.fuzzyMatches.map((m, i) => (
                  <span key={i} style={{ fontSize: 11, color: "#86efac" }}>
                    <span style={{ color: "#f87171", textDecoration: "line-through" }}>"{m.original}"</span>
                    <span style={{ color: "#4b5563", margin: "0 4px" }}>→</span>
                    <span style={{ color: "#4ade80", fontWeight: 600 }}>"{m.corrected}"</span>
                    {m.entity && <span style={{ color: "#374151", fontSize: 10, marginLeft: 4 }}>({m.entity})</span>}
                  </span>
                ))}
              </div>
            )}

            {/* Ambiguity */}
            {isAmbiguous && e.geoResult?.options && (
              <div style={{
                background: "#130e1f", border: "1px solid #7c3aed",
                borderRadius: 10, overflow: "hidden", marginBottom: 12,
              }}>
                <div style={{
                  background: "#1a1030", borderBottom: "1px solid #7c3aed",
                  padding: "12px 18px", display: "flex", alignItems: "center", gap: 8,
                }}>
                  <span style={{ fontSize: 16 }}>🔀</span>
                  <span style={{ color: "#c084fc", fontSize: 12, fontWeight: 600, letterSpacing: "0.5px" }}>
                    Ambiguous locality — which city?
                  </span>
                </div>
                <div style={{ padding: "16px 18px" }}>
                  <div style={{ color: "#a78bfa", fontSize: 12, marginBottom: 14 }}>
                    <span style={{ color: "#e879f9", fontWeight: 700 }}>"{e.geoResult.winner?.entity?.name}"</span>
                    {" "}exists in multiple cities:
                  </div>
                  <div style={{ display: "flex", flexWrap: "wrap", gap: 10 }}>
                    {e.geoResult.options.map((opt, i) => (
                      <button key={i} onClick={() => pickCity(opt.parentCityId || opt.id)}
                        style={{
                          background: "#1e1535", border: "1px solid #7c3aed",
                          borderRadius: 8, padding: "10px 18px", cursor: "pointer",
                          fontFamily: "'DM Mono',monospace", transition: "all 0.12s",
                          display: "flex", flexDirection: "column", gap: 3,
                        }}
                        onMouseEnter={e => { e.currentTarget.style.background = "#2d1f52"; e.currentTarget.style.borderColor = "#a855f7"; }}
                        onMouseLeave={e => { e.currentTarget.style.background = "#1e1535"; e.currentTarget.style.borderColor = "#7c3aed"; }}>
                        <span style={{ color: "#c084fc", fontSize: 13, fontWeight: 700 }}>{opt.name}</span>
                        <span style={{ color: "#6b7280", fontSize: 10 }}>id:{opt.id}</span>
                        {i === 0 && <span style={{ color: "#7c3aed", fontSize: 9, letterSpacing: "1px" }}>TOP MATCH</span>}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* No location */}
            {isNoLocation && (
              <div style={{
                background: "#1a0e0a", border: "1px solid #b45309",
                borderRadius: 8, padding: "12px 18px",
                display: "flex", alignItems: "center", gap: 10, marginBottom: 12,
              }}>
                <span style={{ fontSize: 18 }}>⚠️</span>
                <div>
                  <div style={{ color: "#fbbf24", fontSize: 12, fontWeight: 700 }}>Please specify the locality</div>
                  <div style={{ color: "#78350f", fontSize: 11, marginTop: 2 }}>
                    No city or locality detected — search blocked
                  </div>
                </div>
              </div>
            )}

            {/* Resolved Entities */}
            {!isAmbiguous && (
              <Section title="resolvedEntities" dot="#22c55e">
                {Object.keys(e).filter(k => !["geoResult", "fuzzyMatches", "isRK"].includes(k)).length === 0
                  ? <span style={{ color: "#374151", fontSize: 12 }}>No entities extracted</span>
                  : <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
                    {e.bedroom && <Tag label="bedroom_num" val={e.isRK ? "1 RK" : e.bedroom} color="#3b82f6" />}
                    {e.propertyType && <Tag label="property_type" val={`${e.propertyType} (${e.propertyTypeLabel})`} color="#8b5cf6" />}
                    {e.cityName && <Tag label="city" val={`${e.cityName} (${e.cityId})`} color="#ef4444" />}
                    {e.localityName && <Tag label="locality" val={e.localityName} color="#f97316" />}
                    {e.localityIds && <Tag label="locality_ids" val={e.localityIds.join(",")} color="#f97316" />}
                    {e.maxPrice && <Tag label="budget_max" val={formatPrice(e.maxPrice)} color="#eab308" />}
                    {e.minPrice && <Tag label="budget_min" val={formatPrice(e.minPrice)} color="#eab308" />}
                    {e.possessionLabel && <Tag label="possession" val={e.possessionLabel} color="#14b8a6" />}
                    {e.furnishLabel && <Tag label="furnishing" val={e.furnishLabel} color="#06b6d4" />}
                    {e.facingLabel && <Tag label="facing" val={e.facingLabel} color="#a855f7" />}
                    {e.propertyFeatureLabel && <Tag label="feature" val={e.propertyFeatureLabel} color="#ec4899" />}
                    {e.minArea && <Tag label="area_min" val={`${e.minArea} sqft`} color="#84cc16" />}
                    {e.postedBy && <Tag label="posted_by" val={e.postedBy === "O" ? "Owner" : "Builder"} color="#f43f5e" />}
                    {e.rera && <Tag label="rera" val="Yes" color="#22c55e" />}
                    {e.bathrooms && <Tag label="bathrooms" val={e.bathrooms} color="#6366f1" />}
                    {e.saleType && <Tag label="sale_type" val={e.saleType} color="#d946ef" />}
                    {e.preference && <Tag label="preference" val={e.preference === "R" ? "Rent" : "Sale"} color="#0ea5e9" />}
                    {e.amenities?.map(a => <Tag key={a.id} label="amenity" val={a.label} color="#22c55e" />)}
                  </div>
                }
              </Section>
            )}

            {/* Budget Tier */}
            {!isAmbiguous && (e.maxPriceTier || e.minPriceTier) && (
              <Section title="budgetTierMapping" dot="#f97316">
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  {e.maxPriceTier && (
                    <div style={{ fontSize: 12, color: "#c9d1e0" }}>
                      <span style={{ color: "#3b82f6" }}>budget_max</span>
                      <span style={{ color: "#4b5563", margin: "0 8px" }}>→</span>
                      <span style={{ color: "#f97316", fontWeight: 600 }}>tier id {e.maxPriceTier.id}</span>
                      <span style={{ color: "#4b5563", marginLeft: 8 }}>({e.maxPriceTier.label})</span>
                    </div>
                  )}
                  {e.minPriceTier && (
                    <div style={{ fontSize: 12, color: "#c9d1e0" }}>
                      <span style={{ color: "#3b82f6" }}>budget_min</span>
                      <span style={{ color: "#4b5563", margin: "0 8px" }}>→</span>
                      <span style={{ color: "#f97316", fontWeight: 600 }}>tier id {e.minPriceTier.id}</span>
                      <span style={{ color: "#4b5563", marginLeft: 8 }}>({e.minPriceTier.label})</span>
                    </div>
                  )}
                </div>
              </Section>
            )}

            {/* Solr Params */}
            {!isAmbiguous && (
              <Section title="searchParams → solr/es query" dot="#a855f7">
                <pre style={{
                  margin: 0, fontSize: 12, lineHeight: "1.7", color: "#e2e8f0",
                  whiteSpace: "pre-wrap", wordBreak: "break-all",
                }}>
                  {JSON.stringify(p, null, 2)}
                </pre>
              </Section>
            )}

            {/* Residual tokens */}
            {result.residualTokens?.length > 0 && (
              <Section title="residualTokens (geo input)" dot="#6366f1">
                <div style={{ display: "flex", flexWrap: "wrap", gap: 6 }}>
                  {result.residualTokens.map((t, i) => (
                    <span key={i} style={{
                      background: "#1e1b4b", border: "1px solid #4338ca",
                      borderRadius: 4, padding: "2px 8px", fontSize: 11, color: "#a5b4fc",
                    }}>{t}</span>
                  ))}
                </div>
              </Section>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
