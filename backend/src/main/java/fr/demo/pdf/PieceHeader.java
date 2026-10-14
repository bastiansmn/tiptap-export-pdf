package fr.demo.pdf;

/** Données d'en-tête d'une pièce de procédure (objet de templating). */
public record PieceHeader(
        String typePiece,
        String sousTypePiece,
        String titre,
        String dateHorodatage,
        String numProcedure,
        String numIdj,
        String affaireContre,
        String qualification,
        String personne,
        String serviceNom,
        String serviceAdresse) {
}
