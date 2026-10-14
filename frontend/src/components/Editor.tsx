import { EditorContent, useEditor, type Editor as TiptapEditor, type JSONContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Table from '@tiptap/extension-table'
import TableRow from '@tiptap/extension-table-row'
import TableCell from '@tiptap/extension-table-cell'
import TableHeader from '@tiptap/extension-table-header'

const initialContent = `
<h2>Cadre juridique</h2>
<p>L'an deux mille vingt-six, le quatorze octobre, à quinze heures deux.</p>
<p>Nous, Didier DUPONT, <strong>Major de police</strong>, en fonction à la brigade d'atteinte à la personne.</p>
<p>Constatons que se présente Marie DUPONT qui nous informe avoir été victime de vol dans un local d'habitation.</p>
<ul><li>Premier point</li><li>Second point</li></ul>
`

export interface EditorContentValue {
  html: string
  json: JSONContent
}

export function Editor({ onChange }: { onChange: (value: EditorContentValue) => void }) {
  const emit = (editor: TiptapEditor) => {
    const value = { html: editor.getHTML(), json: editor.getJSON() }
    console.log('[TipTap] getJSON():', JSON.stringify(value.json, null, 2))
    console.log('[TipTap] getHTML():', value.html)
    onChange(value)
  }
  const editor = useEditor({
    extensions: [StarterKit, Image, Table.configure({ resizable: false }), TableRow, TableHeader, TableCell],
    content: initialContent,
    onCreate: ({ editor }) => emit(editor),
    onUpdate: ({ editor }) => emit(editor),
  })

  return (
    <div className="editor">
      <div className="toolbar">
        <button onClick={() => editor?.chain().focus().toggleBold().run()}>Gras</button>
        <button onClick={() => editor?.chain().focus().toggleItalic().run()}>Italique</button>
        <button onClick={() => editor?.chain().focus().toggleBulletList().run()}>Liste</button>
        <button onClick={() => editor?.chain().focus().insertTable({ rows: 2, cols: 2, withHeaderRow: true }).run()}>Tableau</button>
        <button onClick={() => editor?.chain().focus().insertContent('<p>Lorem ipsum dolor sit amet. </p>'.repeat(30)).run()}>
          + Texte long
        </button>
      </div>
      <EditorContent editor={editor} />
    </div>
  )
}
