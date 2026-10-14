# Démo export PDF (React + TipTap + Spring Boot)

- `backend/` : CSS/template du PDF dans `src/main/resources` ; `POST /api/pdf/preview` `{ header, contentHtml }` → PDF A4 (OpenHTMLToPDF + Thymeleaf).
- `frontend/` : formulaire d'en-tête + éditeur TipTap + bouton « Prévisualiser le PDF » (nouvel onglet).

## Lancer
    cd backend && mvn spring-boot:run            # :8080 (ou -Dspring-boot.run.arguments=--server.port=8089)
    cd frontend && npm install && npm run dev    # BACKEND_URL=http://localhost:8089 si port différent
# tiptap-export-pdf
