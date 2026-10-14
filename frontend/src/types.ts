/** Miroir de fr.demo.pdf.PieceHeader (backend). */
export interface PieceHeader {
  typePiece: string
  sousTypePiece: string
  titre: string
  dateHorodatage: string
  numProcedure: string
  numIdj: string
  affaireContre: string
  qualification: string
  personne: string
  serviceNom: string
  serviceAdresse: string
}

export const defaultHeader: PieceHeader = {
  typePiece: 'PROCÈS VERBAL',
  sousTypePiece: 'Audition',
  titre: 'AUDITION GRANDE IDENTITÉ DE TANNEN Bifford',
  dateHorodatage: '24/09/2026 à 14h53',
  numProcedure: '12345/2019/000123',
  numIdj: '2405280000489201',
  affaireContre: 'Lucas RENAUD ; Personne 2 ; Personne 3 ; Personne 4',
  qualification: 'Vol Lorem ipsum dolor sit amet, consectetur adipiscing elit',
  personne: 'TANNEN Bifford',
  serviceNom: 'CSP CONFLANS SAINTE HONORINE',
  serviceAdresse: '1 Square de Ramsgate 78700',
}

export const headerLabels: Record<keyof PieceHeader, string> = {
  typePiece: 'Type de pièce',
  sousTypePiece: 'Sous-type',
  titre: 'Titre',
  dateHorodatage: 'Date / horodatage',
  numProcedure: 'N° procédure',
  numIdj: 'N° IDJ',
  affaireContre: 'Affaire contre',
  qualification: 'Qualification (infraction)',
  personne: 'Personne liée (p.2+)',
  serviceNom: 'Service',
  serviceAdresse: 'Adresse du service',
}
