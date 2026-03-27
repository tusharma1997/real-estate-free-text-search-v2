package com.realestate.freetextsearch.service;

import com.realestate.freetextsearch.data.VocabularyData;
import com.realestate.freetextsearch.data.VocabularyData.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;

@Service
public class NlpParserService {

    // ─── LEVENSHTEIN ──────────────────────────────────────────────────────────
    private int levenshtein(String a, String b) {
        if (a.equals(b)) return 0;
        int m = a.length(), n = b.length();
        if (m == 0) return n;
        if (n == 0) return m;
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? dp[i - 1][j - 1]
                        : 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
        return dp[m][n];
    }

    private record FuzzyResult(String matched, String window, double score, int dist) {}

    private FuzzyResult fuzzyFind(String text, List<String> keys, double threshold) {
        String[] words = text.split("\\s+");
        FuzzyResult best = null;
        List<String> sorted = new ArrayList<>(keys);
        sorted.sort((a, b) -> b.length() - a.length());
        for (String key : sorted) {
            String[] keyWords = key.split("\\s+");
            for (int i = 0; i <= words.length - keyWords.length; i++) {
                String window = String.join(" ", Arrays.copyOfRange(words, i, i + keyWords.length));
                int dist = levenshtein(window, key);
                double score = 1.0 - (double) dist / Math.max(window.length(), key.length());
                if (score >= threshold && (best == null || score > best.score))
                    best = new FuzzyResult(key, window, score, dist);
            }
        }
        return best;
    }

    // ─── GEO N-GRAM SCORING ───────────────────────────────────────────────────
    private static final int L = 10, E = 7;

    private int scoreGeoMatch(int tokenCount, int editDistance) {
        return tokenCount * L - editDistance * E;
    }

    private record GeoCandidate(GeoEntity entity, int editDist, String ngram, int score) {}

    public Map<String, Object> resolveGeo(List<String> residualTokens, Integer cityOverride) {
        String text = String.join(" ", residualTokens).toLowerCase();
        String[] words = Arrays.stream(text.split("\\s+")).filter(w -> !w.isBlank()).toArray(String[]::new);

        List<GeoCandidate> candidates = new ArrayList<>();

        for (int len = 3; len >= 1; len--) {
            for (int i = 0; i <= words.length - len; i++) {
                String ngram = String.join(" ", Arrays.copyOfRange(words, i, i + len));
                List<GeoEntity> exactMatches = VocabularyData.GEO_ENTITIES.stream()
                        .filter(e -> e.name().toLowerCase().equals(ngram)).toList();
                for (GeoEntity m : exactMatches)
                    candidates.add(new GeoCandidate(m, 0, ngram, scoreGeoMatch(len, 0)));
                if (exactMatches.isEmpty() && len <= 2) {
                    for (GeoEntity entity : VocabularyData.GEO_ENTITIES) {
                        String eName = entity.name().toLowerCase();
                        if (Math.abs(eName.split(" ").length - len) > 1) continue;
                        int dist = levenshtein(ngram, eName);
                        int maxLen = Math.max(ngram.length(), eName.length());
                        double similarity = 1.0 - (double) dist / maxLen;
                        if (similarity >= 0.78 && dist <= 2)
                            candidates.add(new GeoCandidate(entity, dist, ngram, scoreGeoMatch(len, dist)));
                    }
                }
            }
        }

        if (candidates.isEmpty()) return null;

        candidates.sort((a, b) -> b.score != a.score ? b.score - a.score : b.entity.listingCount() - a.entity.listingCount());
        GeoCandidate winner = candidates.get(0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("winner", Map.of("entity", geoEntityToMap(winner.entity), "editDist", winner.editDist, "ngram", winner.ngram));
        result.put("allCandidates", candidates.stream().limit(5).map(c ->
                Map.of("entity", geoEntityToMap(c.entity), "editDist", c.editDist, "ngram", c.ngram, "score", c.score)).toList());

        // Ambiguity check
        List<GeoCandidate> sameScore = candidates.stream().filter(c ->
                c.entity.name().equalsIgnoreCase(winner.entity.name()) &&
                !Objects.equals(c.entity.parentCityId(), winner.entity.parentCityId()) &&
                Math.abs(c.score - winner.score) < 3).toList();

        if (!sameScore.isEmpty() && cityOverride == null) {
            result.put("ambiguous", true);
            List<Map<String, Object>> options = new ArrayList<>();
            options.add(geoEntityToMap(winner.entity));
            sameScore.forEach(c -> options.add(geoEntityToMap(c.entity)));
            result.put("options", options);
        }

        // Resolve city + locality
        if ("city".equals(winner.entity.type())) {
            result.put("cityId", winner.entity.id());
            result.put("cityName", winner.entity.name());
            List<String> remaining = Arrays.stream(words).filter(w -> !winner.ngram.contains(w)).toList();
            if (!remaining.isEmpty()) {
                List<GeoCandidate> localityCands = candidates.stream().filter(c ->
                        "locality".equals(c.entity.type()) &&
                        Objects.equals(c.entity.parentCityId(), winner.entity.id()) &&
                        c != winner).toList();
                if (!localityCands.isEmpty()) {
                    result.put("localityIds", localityCands.stream().map(c -> c.entity.id()).toList());
                    result.put("localityName", localityCands.get(0).entity.name());
                }
            }
        } else {
            result.put("localityIds", List.of(winner.entity.id()));
            result.put("localityName", winner.entity.name());
            if (winner.entity.parentCityId() != null) {
                VocabularyData.GEO_ENTITIES.stream()
                        .filter(e -> e.id() == winner.entity.parentCityId() && "city".equals(e.type()))
                        .findFirst().ifPresent(parentCity -> {
                    result.put("cityId", parentCity.id());
                    result.put("cityName", parentCity.name());
                });
            }
            List<Integer> siblings = VocabularyData.GEO_ENTITIES.stream()
                    .filter(e -> "locality".equals(e.type()) &&
                            Objects.equals(e.parentCityId(), winner.entity.parentCityId()) &&
                            e.name().toLowerCase().startsWith(winner.entity.name().toLowerCase().split(" ")[0]))
                    .map(GeoEntity::id).toList();
            if (siblings.size() > 1) result.put("localityIds", siblings);
        }

        if (winner.editDist > 0)
            result.put("fuzzyCorrection", Map.of("original", winner.ngram, "corrected", winner.entity.name(), "dist", winner.editDist));

        return result;
    }

    private Map<String, Object> geoEntityToMap(GeoEntity e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.id()); m.put("name", e.name()); m.put("type", e.type());
        m.put("parentCityId", e.parentCityId()); m.put("listingCount", e.listingCount());
        return m;
    }

    // ─── BUDGET TIER ──────────────────────────────────────────────────────────
    private BudgetTier getBudgetTier(long price) {
        return VocabularyData.BUDGET_TIERS.stream()
                .min(Comparator.comparingLong(t -> Math.abs(t.price() - price)))
                .orElse(VocabularyData.BUDGET_TIERS.get(0));
    }

    // ─── DICT FIND ────────────────────────────────────────────────────────────
    private record DictResult(String key, Object value, int dist, String original) {}

    private <V> DictResult dictFind(String text, Map<String, V> map, double threshold) {
        List<String> keys = new ArrayList<>(map.keySet());
        keys.sort((a, b) -> b.length() - a.length());
        for (String k : keys) if (text.contains(k)) return new DictResult(k, map.get(k), 0, null);
        FuzzyResult f = fuzzyFind(text, keys, threshold);
        if (f != null) return new DictResult(f.matched, map.get(f.matched), f.dist, f.window);
        return null;
    }

    // ─── MAIN PARSE ───────────────────────────────────────────────────────────
    public Map<String, Object> parseQuery(String raw, Integer geoOverride) {
        String text = raw.toLowerCase().trim();
        Map<String, Object> entities = new LinkedHashMap<>();
        List<Map<String, Object>> fuzzyMatches = new ArrayList<>();

        // 1. BEDROOM
        Matcher bedMatch = Pattern.compile("(\\d)\\s*(?:bhk|bhks|bedroom|bedrooms|bed room|bed\\b|br\\b)").matcher(text);
        if (bedMatch.find()) entities.put("bedroom", Integer.parseInt(bedMatch.group(1)));
        else if (Pattern.compile("\\b1\\s*rk\\b|1rk\\b").matcher(text).find()) {
            entities.put("bedroom", 1);
            entities.put("isRK", true);
        }

        // 2. PROPERTY TYPE
        List<String> ptKeys = new ArrayList<>(VocabularyData.PROPERTY_TYPE_MAP.keySet());
        ptKeys.sort((a, b) -> b.length() - a.length());
        boolean ptFound = false;
        for (String k : ptKeys) {
            if (text.contains(k)) {
                int val = VocabularyData.PROPERTY_TYPE_MAP.get(k);
                entities.put("propertyType", val);
                entities.put("propertyTypeLabel", VocabularyData.PROPERTY_TYPE_LABELS.get(val));
                ptFound = true; break;
            }
        }
        if (!ptFound) {
            FuzzyResult f = fuzzyFind(text, ptKeys, 0.80);
            if (f != null) {
                int val = VocabularyData.PROPERTY_TYPE_MAP.get(f.matched);
                entities.put("propertyType", val);
                entities.put("propertyTypeLabel", VocabularyData.PROPERTY_TYPE_LABELS.get(val));
                fuzzyMatches.add(Map.of("original", f.window, "corrected", f.matched, "entity", "propertyType"));
            }
        }

        // 3. PREFERENCE
        entities.put("preference", Pattern.compile("\\b(rent|renting|for rent|rental|on rent|lease|to let|pg|paying guest)\\b").matcher(text).find() ? "R" : "S");

        // 4. SALE TYPE
        if (Pattern.compile("\\bresale\\b|\\bre-sale\\b|\\bsecond hand\\b|\\bused property\\b").matcher(text).find())
            entities.put("saleType", "resale");
        else if (Pattern.compile("\\bnew (flat|project|property|construction)\\b|\\bnewly built\\b").matcher(text).find())
            entities.put("saleType", "new");

        // 5. PRICE
        Pattern priceRx = Pattern.compile("(\\b(?:under|below|upto|up to|less than|within|maximum|max|atmost|budget|above|more than|starting from|starting|minimum|min|at least|from)\\b)?\\s*(\\d+(?:\\.\\d+)?)\\s*(lakh|lakhs|lac|lacs|crore|crores|cr|l|k)?\\b", Pattern.CASE_INSENSITIVE);
        Matcher pm = priceRx.matcher(text);
        while (pm.find()) {
            String dir = pm.group(1) != null ? pm.group(1).trim().toLowerCase() : null;
            double num = Double.parseDouble(pm.group(2));
            String unit = pm.group(3) != null ? pm.group(3).toLowerCase() : null;
            if (unit == null && num > 1000) continue;
            if (unit == null) continue;
            long price;
            switch (unit) {
                case "lakh","lakhs","lac","lacs","l" -> price = Math.round(num * 100000);
                case "crore","crores","cr" -> price = Math.round(num * 10000000);
                case "k" -> price = Math.round(num * 1000);
                default -> { continue; }
            }
            if (price < 10000) continue;
            boolean isMax = dir == null || dir.matches("under|below|upto|up to|less than|within|maximum|max|atmost|budget");
            boolean isMin = dir != null && dir.matches("above|more than|starting.*|minimum|min|at least|from");
            if (isMin) {
                entities.put("minPrice", price);
                entities.put("minPriceTier", budgetTierToMap(getBudgetTier(price)));
            } else {
                entities.put("maxPrice", price);
                entities.put("maxPriceTier", budgetTierToMap(getBudgetTier(price)));
            }
        }

        // 6. POSSESSION
        List<String> possKeys = new ArrayList<>(VocabularyData.POSSESSION_MAP.keySet());
        possKeys.sort((a, b) -> b.length() - a.length());
        boolean possFound = false;
        for (String k : possKeys) {
            if (text.contains(k)) {
                entities.put("possession", VocabularyData.POSSESSION_MAP.get(k));
                entities.put("possessionLabel", VocabularyData.POSSESSION_LABELS.get(VocabularyData.POSSESSION_MAP.get(k)));
                possFound = true; break;
            }
        }
        if (!possFound) {
            FuzzyResult f = fuzzyFind(text, possKeys, 0.85);
            if (f != null) {
                entities.put("possession", VocabularyData.POSSESSION_MAP.get(f.matched));
                entities.put("possessionLabel", VocabularyData.POSSESSION_LABELS.get(VocabularyData.POSSESSION_MAP.get(f.matched)));
                fuzzyMatches.add(Map.of("original", f.window, "corrected", f.matched, "entity", "possession"));
            }
        }

        // 7. FURNISHING
        List<String> furnKeys = new ArrayList<>(VocabularyData.FURNISH_MAP.keySet());
        furnKeys.sort((a, b) -> b.length() - a.length());
        for (String k : furnKeys) {
            if (text.contains(k)) {
                entities.put("furnish", VocabularyData.FURNISH_MAP.get(k));
                entities.put("furnishLabel", VocabularyData.FURNISH_LABELS.get(VocabularyData.FURNISH_MAP.get(k)));
                break;
            }
        }

        // 8. AMENITIES
        List<Map<String, Object>> amenities = new ArrayList<>();
        List<String> amenKeys = new ArrayList<>(VocabularyData.AMENITY_MAP.keySet());
        amenKeys.sort((a, b) -> b.length() - a.length());
        for (String k : amenKeys) {
            if (text.contains(k)) {
                int id = VocabularyData.AMENITY_MAP.get(k);
                if (amenities.stream().noneMatch(a -> a.get("id").equals(id)))
                    amenities.add(Map.of("id", id, "label", VocabularyData.AMENITY_LABELS.getOrDefault(id, k)));
            }
        }
        if (!amenities.isEmpty()) entities.put("amenities", amenities);

        // 9. FACING
        List<String> facKeys = new ArrayList<>(VocabularyData.FACING_MAP.keySet());
        facKeys.sort((a, b) -> b.length() - a.length());
        for (String k : facKeys) {
            if (text.contains(k)) {
                entities.put("facing", VocabularyData.FACING_MAP.get(k));
                entities.put("facingLabel", VocabularyData.FACING_LABELS.get(VocabularyData.FACING_MAP.get(k)));
                break;
            }
        }

        // 10. PROPERTY FEATURES
        List<String> pfKeys = new ArrayList<>(VocabularyData.PROPERTY_FEATURE_MAP.keySet());
        pfKeys.sort((a, b) -> b.length() - a.length());
        for (String k : pfKeys) {
            if (text.contains(k)) {
                entities.put("propertyFeature", VocabularyData.PROPERTY_FEATURE_MAP.get(k));
                entities.put("propertyFeatureLabel", VocabularyData.PROPERTY_FEATURE_LABELS.get(VocabularyData.PROPERTY_FEATURE_MAP.get(k)));
                break;
            }
        }

        // 11. AREA
        Matcher areaM = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:sq\\.?\\s*ft|sqft|sft|square\\s*f(?:eet|oot)|sq\\.?\\s*yard|sqyard|gaj|gaz|sq\\.?\\s*m(?:eter|etre|tr)?|sqm|acre|marla)", Pattern.CASE_INSENSITIVE).matcher(text);
        if (areaM.find()) {
            double area = Double.parseDouble(areaM.group(1));
            String u = areaM.group(0).toLowerCase();
            if (u.matches(".*(?:yard|gaj|gaz).*")) area = Math.round(area * 9);
            else if (u.matches(".*(?:sq\\s*m|sqm).*")) area = Math.round(area * 10.764);
            else if (u.contains("acre")) area = Math.round(area * 43560);
            entities.put("minArea", (int) area);
        }

        // 12. POSTED BY
        if (Pattern.compile("\\b(owner|by owner|direct owner|no broker|no brokerage|without broker|zero brokerage)\\b").matcher(text).find())
            entities.put("postedBy", "O");
        else if (Pattern.compile("\\b(builder|developer|from builder)\\b").matcher(text).find())
            entities.put("postedBy", "B");

        // 13. RERA
        if (Pattern.compile("\\brera\\b").matcher(text).find()) entities.put("rera", true);

        // 14. BATHROOM
        Matcher bathM = Pattern.compile("(\\d)\\s*(?:bath(?:room)?s?|washroom|toilet)").matcher(text);
        if (bathM.find()) entities.put("bathrooms", Integer.parseInt(bathM.group(1)));

        // 15. GEOGRAPHIC — strip known patterns to get residual
        Set<String> stopwords = Set.of("in","at","near","for","with","and","or","the","a","an","of","to","from","by");

        // Build all synonyms for stripping
        List<String> allSynonyms = new ArrayList<>();
        allSynonyms.addAll(VocabularyData.PROPERTY_TYPE_MAP.keySet());
        allSynonyms.addAll(VocabularyData.POSSESSION_MAP.keySet());
        allSynonyms.addAll(VocabularyData.FURNISH_MAP.keySet());
        allSynonyms.addAll(VocabularyData.AMENITY_MAP.keySet());
        allSynonyms.addAll(VocabularyData.FACING_MAP.keySet());
        allSynonyms.addAll(VocabularyData.PROPERTY_FEATURE_MAP.keySet());
        allSynonyms.sort((a, b) -> b.length() - a.length());

        List<Pattern> knownPatterns = new ArrayList<>(List.of(
                Pattern.compile("\\d+\\s*(?:bhk|bhks|bedroom|bedrooms|bed\\b|br\\b)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\d+(?:\\.\\d+)?\\s*(?:lakh|lakhs|lac|lacs|crore|crores|cr|l\\b|k\\b)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:under|below|upto|above|more than|starting|minimum|maximum|budget)\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:sqft|sq\\s*ft|square\\s*feet|sq\\s*yard|gaj)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:ready\\s*to\\s*move|under\\s*construction|new\\s*launch|rtm)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:furnished|unfurnished|semi\\s*furnished)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:for\\s*(?:sale|rent)|buy|purchase|rent|lease)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\d+\\s*(?:bath(?:room)?|washroom)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:owner|by owner|direct owner|no broker|no brokerage|without broker|zero brokerage)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:builder|developer|from builder)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\brk\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\brera\\b", Pattern.CASE_INSENSITIVE)
        ));
        for (String syn : allSynonyms)
            knownPatterns.add(Pattern.compile("\\b" + Pattern.quote(syn) + "\\b", Pattern.CASE_INSENSITIVE));
        for (Map<String, Object> fm : fuzzyMatches)
            knownPatterns.add(Pattern.compile("\\b" + Pattern.quote((String) fm.get("original")) + "\\b", Pattern.CASE_INSENSITIVE));

        String residual = text;
        for (Pattern p : knownPatterns) residual = p.matcher(residual).replaceAll(" ");
        List<String> residualTokens = Arrays.stream(residual.split("\\s+"))
                .filter(t -> t.length() > 1 && !stopwords.contains(t)).toList();

        if (!residualTokens.isEmpty()) {
            Map<String, Object> geoResult = resolveGeo(residualTokens, geoOverride);
            if (geoResult != null) {
                entities.put("geoResult", geoResult);
                if (!Boolean.TRUE.equals(geoResult.get("ambiguous"))) {
                    if (geoResult.containsKey("cityId")) {
                        entities.put("cityId", geoResult.get("cityId"));
                        entities.put("cityName", geoResult.get("cityName"));
                    }
                    if (geoResult.containsKey("localityIds")) {
                        entities.put("localityIds", geoResult.get("localityIds"));
                        entities.put("localityName", geoResult.get("localityName"));
                    }
                    if (geoResult.containsKey("fuzzyCorrection")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fc = (Map<String, Object>) geoResult.get("fuzzyCorrection");
                        Map<String, Object> fm = new LinkedHashMap<>(fc);
                        fm.put("entity", "location");
                        fuzzyMatches.add(fm);
                    }
                }
            }
        }

        entities.put("fuzzyMatches", fuzzyMatches);

        // Build params
        Map<String, Object> params = buildParams(entities);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("entities", entities);
        response.put("params", params);
        response.put("raw", raw);
        response.put("residualTokens", residualTokens);
        return response;
    }

    private Map<String, Object> budgetTierToMap(BudgetTier t) {
        return Map.of("id", t.id(), "price", t.price(), "label", t.label());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildParams(Map<String, Object> e) {
        if (e.containsKey("geoResult")) {
            Map<String, Object> geo = (Map<String, Object>) e.get("geoResult");
            if (Boolean.TRUE.equals(geo.get("ambiguous")))
                return Map.of("status", "AWAITING_CITY_DISAMBIGUATION");
        }
        if (!e.containsKey("cityId") && !e.containsKey("localityIds"))
            return Map.of("status", "NO_LOCATION");

        Map<String, Object> p = new LinkedHashMap<>();
        if (e.containsKey("cityId")) p.put("city", e.get("cityId"));
        if (e.containsKey("localityIds")) {
            List<Integer> ids = (List<Integer>) e.get("localityIds");
            p.put("locality_array", ids.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""));
        }
        if (e.containsKey("bedroom")) p.put("bedroom_num", e.get("bedroom"));
        if (e.containsKey("propertyType")) p.put("property_type", e.get("propertyType"));
        p.put("preference", e.getOrDefault("preference", "S"));
        if (e.containsKey("maxPriceTier")) {
            Map<String, Object> tier = (Map<String, Object>) e.get("maxPriceTier");
            p.put("budget_max", tier.get("id"));
            p.put("maxPrice", e.get("maxPrice"));
        }
        if (e.containsKey("minPriceTier")) {
            Map<String, Object> tier = (Map<String, Object>) e.get("minPriceTier");
            p.put("budget_min", tier.get("id"));
            p.put("minPrice", e.get("minPrice"));
        }
        if (e.containsKey("possession")) p.put("availability", e.get("possession"));
        if (e.containsKey("furnish")) p.put("furnish", e.get("furnish"));
        if (e.containsKey("amenities")) {
            List<Map<String, Object>> amenities = (List<Map<String, Object>>) e.get("amenities");
            p.put("features", amenities.stream().map(a -> String.valueOf(a.get("id"))).reduce((a, b) -> a + "," + b).orElse(""));
        }
        if (e.containsKey("facing")) p.put("facing_direction", e.get("facing"));
        if (e.containsKey("propertyFeature")) p.put("property_feature", e.get("propertyFeature"));
        if (e.containsKey("minArea")) p.put("area_min", e.get("minArea"));
        if (e.containsKey("postedBy")) p.put("class", e.get("postedBy"));
        if (e.containsKey("rera")) p.put("rera", true);
        if (e.containsKey("bathrooms")) p.put("bathroom_num", e.get("bathrooms"));
        if (e.containsKey("saleType")) p.put("sale_type", e.get("saleType"));
        p.put("area_unit", 1); p.put("res_com", "R"); p.put("search_type", "QS"); p.put("moduleName", "FREE_TEXT");
        return p;
    }
}
