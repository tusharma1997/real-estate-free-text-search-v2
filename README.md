# Real Estate Free Text Search v2 — Java API

Full-stack NLP query parser for real estate searches with a **Java Spring Boot** backend and **React** frontend.

## Architecture

```
free-text-search-2/
├── backend/          # Spring Boot 3.3.6, Java 21
│   ├── src/main/java/com/realestate/freetextsearch/
│   │   ├── FreeTextSearchApplication.java
│   │   ├── controller/ParseController.java    # POST /api/parse
│   │   ├── data/VocabularyData.java           # All vocabulary maps
│   │   └── service/NlpParserService.java      # NLP parser engine
│   └── pom.xml
└── frontend/         # React + Vite
    ├── src/App.jsx   # UI calling the Java API
    └── vite.config.js # Dev proxy to port 8080
```

## Running

### Backend (Java)
```bash
set JAVA_HOME=%USERPROFILE%\jdk\jdk-21.0.10+7
set PATH=%USERPROFILE%\maven\apache-maven-3.9.6\bin;%JAVA_HOME%\bin;%PATH%
mvn -f backend/pom.xml spring-boot:run
```
Backend starts on `http://localhost:8080`

### Frontend (React)
```bash
cd frontend
npm install
npm run dev
```
Frontend starts on `http://localhost:5174` with API proxy to backend.

## API

### POST /api/parse
```json
{
  "query": "3bhk flat in bandra under 1 crore ready to move",
  "geoOverride": null
}
```
Returns parsed entities, Solr params, residual tokens, and fuzzy corrections.
