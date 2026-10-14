import { useState } from 'react'
import { Editor, type EditorContentValue } from './components/Editor'
import { defaultHeader, headerLabels, type PieceHeader } from './types'

export default function App() {
  const [header, setHeader] = useState<PieceHeader>(defaultHeader)
  const [content, setContent] = useState<EditorContentValue | null>(null)
  const [format, setFormat] = useState<'html' | 'json'>('json')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [pdfUrl, setPdfUrl] = useState<string | null>(null)

  async function openPdf(mode: 'tab' | 'modal', endpoint = '/api/pdf/preview') {
    setLoading(true)
    setError(null)
    // Onglet : à ouvrir de façon synchrone (sinon bloqué par l'anti-popup), puis à remplir.
    const tab = mode === 'tab' ? window.open('', '_blank') : null
    try {
      const res = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(
          format === 'json'
            ? { header, contentJson: content?.json }
            : { header, contentHtml: content?.html },
        ),
      })
      if (!res.ok) throw new Error(`Erreur ${res.status}`)
      const url = URL.createObjectURL(await res.blob())
      if (mode === 'tab') {
        if (tab) tab.location.href = url
        else window.open(url, '_blank')
      } else {
        setPdfUrl((old) => {
          if (old) URL.revokeObjectURL(old)
          return url
        })
      }
    } catch (e) {
      tab?.close()
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setLoading(false)
    }
  }

  function closePreview() {
    if (pdfUrl) URL.revokeObjectURL(pdfUrl)
    setPdfUrl(null)
  }

  return (
    <div className="panel">
      <h1>Pièce de procédure</h1>
      <h2>En-tête</h2>
      {(Object.keys(headerLabels) as (keyof PieceHeader)[]).map((k) => (
        <label key={k}>
          {headerLabels[k]}
          <input value={header[k]} onChange={(e) => setHeader({ ...header, [k]: e.target.value })} />
        </label>
      ))}
      <h2>Contenu</h2>
      <Editor onChange={setContent} />
      <label>
        Format envoyé au backend
        <select value={format} onChange={(e) => setFormat(e.target.value as 'html' | 'json')}>
          <option value="json">JSON (editor.getJSON())</option>
          <option value="html">HTML (editor.getHTML())</option>
        </select>
      </label>
      <div className="actions">
        <button className="primary" onClick={() => openPdf('tab')} disabled={loading}>
          {loading ? 'Génération…' : 'Ouvrir dans un nouvel onglet'}
        </button>
        <button className="primary" onClick={() => openPdf('modal')} disabled={loading}>
          Ouvrir dans une pop-up
        </button>
        {/* Exemple isolé : PDF/A-3 avec métadonnées XMP + XML joint (voir backend/.../pdfa3) */}
        <button onClick={() => openPdf('modal', '/api/pdf/preview-pdfa3')} disabled={loading}>PDF/A-3 (exemple)</button>
      </div>
      {error && <p className="error">{error}</p>}

      {pdfUrl && (
        <div className="modal-backdrop" onClick={closePreview}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={closePreview}>✕</button>
            <iframe src={pdfUrl} title="Aperçu PDF" />
          </div>
        </div>
      )}
    </div>
  )
}
