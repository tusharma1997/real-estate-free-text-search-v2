package com.realestate.freetextsearch.data;

import java.util.*;

/**
 * Vocabulary tables ported from the JavaScript NLP parser.
 * All maps and labels for property types, possession, furnishing,
 * amenities, facing, property features, budget tiers, and geo entities.
 */
public final class VocabularyData {

    private VocabularyData() {}

    // ─── PROPERTY TYPE ────────────────────────────────────────────────────────
    public static final Map<String, Integer> PROPERTY_TYPE_MAP = new LinkedHashMap<>();
    static {
        // ID 1 — Residential Apartment
        for (String k : new String[]{"multistorey apartment","multistorey","multi storey","multi-storey",
                "flat","flats","apartment","apartments","residential apartment","floor apartment"})
            PROPERTY_TYPE_MAP.put(k, 1);
        // ID 2 — House/Villa
        for (String k : new String[]{"independent house","villa","villas","house","houses",
                "bungalow","kothi","row house","rowhouse","townhouse","duplex","penthouse",
                "independent bungalow","bangla"})
            PROPERTY_TYPE_MAP.put(k, 2);
        // ID 3 — Plot/Land
        for (String k : new String[]{"residential plot","residential land","plot","plots","land",
                "open plot","bhookhanda","na plot","freehold plot"})
            PROPERTY_TYPE_MAP.put(k, 3);
        // ID 4 — Builder Floor
        for (String k : new String[]{"builder floor apartment","builder floor","builder-floor","bf apartment"})
            PROPERTY_TYPE_MAP.put(k, 4);
        // ID 5 — Farm House
        for (String k : new String[]{"farm house","farmhouse","farm-house","farm villa","weekend home"})
            PROPERTY_TYPE_MAP.put(k, 5);
        // ID 6 — Shop
        for (String k : new String[]{"commercial shop","retail shop","shop","shops","showroom","showrooms","kiosk"})
            PROPERTY_TYPE_MAP.put(k, 6);
        // ID 7 — Office
        for (String k : new String[]{"commercial office","office space","office","offices",
                "coworking","co-working","business centre","business center","workspace"})
            PROPERTY_TYPE_MAP.put(k, 7);
        // ID 8 — Commercial Land
        for (String k : new String[]{"commercial land","commercial plot","industrial land","industrial plot"})
            PROPERTY_TYPE_MAP.put(k, 8);
        // ID 90 — Studio/1RK
        for (String k : new String[]{"studio apartment","studio","1 rk","1rk",
                "bachelor flat","service apartment","serviced apartment","bachelor apartment"})
            PROPERTY_TYPE_MAP.put(k, 90);
    }

    public static final Map<Integer, String> PROPERTY_TYPE_LABELS = Map.of(
            1,"Residential Apartment", 2,"Independent House/Villa", 3,"Residential Plot",
            4,"Builder Floor", 5,"Farm House", 6,"Commercial Shop", 7,"Commercial Office",
            8,"Commercial Land", 90,"Studio/1RK"
    );

    // ─── POSSESSION ───────────────────────────────────────────────────────────
    public static final Map<String, Integer> POSSESSION_MAP = new LinkedHashMap<>();
    static {
        for (String k : new String[]{"ready to move","ready-to-move","rtm","immediate possession",
                "immediate","move in ready","possession ready","ready possession","available immediately","ready"})
            POSSESSION_MAP.put(k, 2);
        for (String k : new String[]{"under construction","under-construction","uc","ongoing",
                "upcoming","in construction","pre-launch","prelaunch","pre launch","booking open"})
            POSSESSION_MAP.put(k, 1);
        for (String k : new String[]{"new launch","new-launch","newly launched"})
            POSSESSION_MAP.put(k, 3);
    }

    public static final Map<Integer, String> POSSESSION_LABELS = Map.of(
            2,"Ready to Move", 1,"Under Construction", 3,"New Launch"
    );

    // ─── FURNISHING ───────────────────────────────────────────────────────────
    public static final Map<String, String> FURNISH_MAP = new LinkedHashMap<>();
    static {
        for (String k : new String[]{"fully furnished","fully-furnished","furnished","ff","with furniture"})
            FURNISH_MAP.put(k, "F");
        for (String k : new String[]{"semi furnished","semi-furnished","semifurnished",
                "partially furnished","part furnished","sf"})
            FURNISH_MAP.put(k, "S");
        for (String k : new String[]{"unfurnished","un-furnished","bare","without furniture","empty flat","bare shell"})
            FURNISH_MAP.put(k, "U");
    }

    public static final Map<String, String> FURNISH_LABELS = Map.of(
            "F","Furnished", "S","Semi-Furnished", "U","Unfurnished"
    );

    // ─── AMENITIES ────────────────────────────────────────────────────────────
    public static final Map<String, Integer> AMENITY_MAP = new LinkedHashMap<>();
    static {
        for (String k : new String[]{"swimming pool","swim pool","swimmingpool","pool"}) AMENITY_MAP.put(k, 1);
        for (String k : new String[]{"power backup","power-backup","generator","genset","dg set","inverter backup","inverter"}) AMENITY_MAP.put(k, 2);
        for (String k : new String[]{"club house","clubhouse","club-house","community hall","amenity hall","recreation room"}) AMENITY_MAP.put(k, 3);
        for (String k : new String[]{"reserved parking","covered parking","open parking","car parking","parking",
                "stilt parking","basement parking","garage"}) AMENITY_MAP.put(k, 4);
        for (String k : new String[]{"vaastu compliant","vastu compliant","vastu friendly","vastu shastra","vaastu","vastu"}) AMENITY_MAP.put(k, 5);
        for (String k : new String[]{"kids play area","children park","landscaped garden","kids park","play area",
                "park","garden","green area"}) AMENITY_MAP.put(k, 6);
        for (String k : new String[]{"24x7 security","gated community","security guard","cctv","intercom",
                "24 hour security","gated","security"}) AMENITY_MAP.put(k, 9);
        for (String k : new String[]{"atm","bank atm","nearby atm"}) AMENITY_MAP.put(k, 11);
        for (String k : new String[]{"fitness centre","fitness center","fitness club","workout room","gymnasium","gym"}) AMENITY_MAP.put(k, 12);
        for (String k : new String[]{"elevator","lifts","lift","elevators"}) AMENITY_MAP.put(k, 21);
        for (String k : new String[]{"waste disposal","garbage","waste management"}) AMENITY_MAP.put(k, 25);
        for (String k : new String[]{"piped gas","gas pipeline","png","cooking gas","piped cooking gas"}) AMENITY_MAP.put(k, 29);
        for (String k : new String[]{"wheelchair accessible","disabled access","wheelchair","handicap accessible"}) AMENITY_MAP.put(k, 34);
        for (String k : new String[]{"diesel generator","dg availability","dg","power generator"}) AMENITY_MAP.put(k, 35);
        for (String k : new String[]{"sea facing","sea-facing","sea view","ocean facing","waterfront"}) AMENITY_MAP.put(k, 100);
    }

    public static final Map<Integer, String> AMENITY_LABELS = Map.ofEntries(
            Map.entry(1,"Swimming Pool"), Map.entry(2,"Power Backup"), Map.entry(3,"Club House"),
            Map.entry(4,"Parking"), Map.entry(5,"Vastu Compliant"), Map.entry(6,"Park/Garden"),
            Map.entry(9,"Security"), Map.entry(11,"ATM"), Map.entry(12,"Gymnasium"),
            Map.entry(21,"Lift"), Map.entry(25,"Waste Disposal"), Map.entry(29,"Gas Pipeline"),
            Map.entry(34,"Wheelchair Access"), Map.entry(35,"DG Availability"), Map.entry(100,"Sea Facing")
    );

    // ─── FACING ───────────────────────────────────────────────────────────────
    public static final Map<String, Integer> FACING_MAP = new LinkedHashMap<>();
    static {
        FACING_MAP.put("north facing",1); FACING_MAP.put("north-facing",1); FACING_MAP.put("north face",1);
        FACING_MAP.put("south facing",2); FACING_MAP.put("south-facing",2);
        FACING_MAP.put("east facing",3); FACING_MAP.put("east-facing",3); FACING_MAP.put("east face",3);
        FACING_MAP.put("west facing",4); FACING_MAP.put("west-facing",4);
        FACING_MAP.put("north east facing",5); FACING_MAP.put("north-east facing",5); FACING_MAP.put("northeast facing",5); FACING_MAP.put("ne facing",5);
        FACING_MAP.put("north west facing",6); FACING_MAP.put("north-west facing",6); FACING_MAP.put("northwest facing",6); FACING_MAP.put("nw facing",6);
        FACING_MAP.put("south east facing",7); FACING_MAP.put("south-east facing",7); FACING_MAP.put("southeast facing",7); FACING_MAP.put("se facing",7);
        FACING_MAP.put("south west facing",8); FACING_MAP.put("south-west facing",8); FACING_MAP.put("southwest facing",8); FACING_MAP.put("sw facing",8);
    }

    public static final Map<Integer, String> FACING_LABELS = Map.of(
            1,"North", 2,"South", 3,"East", 4,"West",
            5,"North-East", 6,"North-West", 7,"South-East", 8,"South-West"
    );

    // ─── PROPERTY FEATURES ────────────────────────────────────────────────────
    public static final Map<String, Integer> PROPERTY_FEATURE_MAP = new LinkedHashMap<>();
    static {
        for (String k : new String[]{"corner property","corner plot","corner flat","corner unit","corner"}) PROPERTY_FEATURE_MAP.put(k, 1);
        for (String k : new String[]{"park facing","garden facing","overlooking park","overlooks garden"}) PROPERTY_FEATURE_MAP.put(k, 2);
        for (String k : new String[]{"road facing","main road facing","main road"}) PROPERTY_FEATURE_MAP.put(k, 3);
        for (String k : new String[]{"roof rights","terrace rights","with roof","roof access","terrace access"}) PROPERTY_FEATURE_MAP.put(k, 4);
    }

    public static final Map<Integer, String> PROPERTY_FEATURE_LABELS = Map.of(
            1,"Corner Property", 2,"Park Facing", 3,"Road Facing", 4,"Roof Rights"
    );



    // ─── GEOGRAPHIC DATA ──────────────────────────────────────────────────────
    public record GeoEntity(int id, String name, String type, Integer parentCityId, int listingCount) {}

    public static final List<GeoEntity> GEO_ENTITIES = List.of(
            // Cities
            new GeoEntity(12,"Mumbai","city",null,90000),
            new GeoEntity(15,"Navi Mumbai","city",null,25000),
            new GeoEntity(218,"Western Mumbai","city",null,30000),
            new GeoEntity(1,"Delhi","city",null,80000),
            new GeoEntity(7,"Noida","city",null,50000),
            new GeoEntity(222,"Greater Noida","city",null,30000),
            new GeoEntity(3,"Gurugram","city",null,40000),
            new GeoEntity(4,"Faridabad","city",null,15000),
            new GeoEntity(9,"Bangalore","city",null,70000),
            new GeoEntity(6,"Pune","city",null,45000),
            new GeoEntity(10,"Hyderabad","city",null,55000),
            new GeoEntity(11,"Chennai","city",null,35000),
            new GeoEntity(2,"Kolkata","city",null,30000),
            new GeoEntity(16,"Ahmedabad","city",null,25000),
            new GeoEntity(18,"Jaipur","city",null,20000),
            new GeoEntity(19,"Lucknow","city",null,18000),
            // Localities — Mumbai
            new GeoEntity(4931,"Bandra West","locality",12,8000),
            new GeoEntity(7913,"Bandra","locality",12,3000),
            new GeoEntity(4932,"Bandra East","locality",12,4000),
            new GeoEntity(4933,"BKC","locality",12,5000),
            new GeoEntity(4900,"Andheri","locality",12,12000),
            new GeoEntity(4901,"Andheri West","locality",12,7000),
            new GeoEntity(4902,"Andheri East","locality",12,6000),
            new GeoEntity(4850,"Powai","locality",12,9000),
            new GeoEntity(4851,"Juhu","locality",12,4000),
            new GeoEntity(4852,"Worli","locality",12,5000),
            new GeoEntity(4853,"Lower Parel","locality",12,6000),
            new GeoEntity(4857,"Borivali","locality",12,8000),
            new GeoEntity(4858,"Thane","locality",12,15000),
            // Localities — Delhi NCR
            new GeoEntity(3100,"Sector 62","locality",7,5000),
            new GeoEntity(2150,"Sector 62","locality",3,2000),
            new GeoEntity(2200,"Sector 62","locality",4,800),
            new GeoEntity(3103,"Noida Extension","locality",7,12000),
            new GeoEntity(2104,"Rohini","locality",1,7000),
            new GeoEntity(2106,"Vasant Kunj","locality",1,4000),
            new GeoEntity(2109,"Hauz Khas","locality",1,3000),
            new GeoEntity(2101,"Dwarka Expressway","locality",3,9000),
            new GeoEntity(2102,"Golf Course Road","locality",3,5000),
            // Localities — Bangalore
            new GeoEntity(5100,"Whitefield","locality",9,15000),
            new GeoEntity(5101,"Electronic City","locality",9,12000),
            new GeoEntity(5102,"Koramangala","locality",9,8000),
            new GeoEntity(5103,"HSR Layout","locality",9,6000),
            new GeoEntity(5104,"Indiranagar","locality",9,5000),
            new GeoEntity(5110,"Indira Nagar","locality",9,4000),
            // Localities — Pune
            new GeoEntity(6101,"Hinjewadi","locality",6,10000),
            new GeoEntity(6102,"Baner","locality",6,8000),
            new GeoEntity(6100,"Wakad","locality",6,7000),
            new GeoEntity(6103,"Kothrud","locality",6,5000),
            // Localities — Hyderabad
            new GeoEntity(7100,"Gachibowli","locality",10,12000),
            new GeoEntity(7101,"Hitech City","locality",10,10000),
            new GeoEntity(7102,"Kondapur","locality",10,7000),
            // Ambiguous localities
            new GeoEntity(2130,"Malviya Nagar","locality",1,4000),
            new GeoEntity(9100,"Malviya Nagar","locality",18,1500),
            new GeoEntity(9220,"Indira Nagar","locality",19,3000)
    );
}
