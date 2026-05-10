// Sanitise le HTML avant injection pour prévenir XSS (données corrompues en base)
function sanitizeHTML(htmlString) {
  const parser = new DOMParser();
  const doc = parser.parseFromString(htmlString, 'text/html');

  const dangerous = ['script', 'iframe', 'object', 'embed', 'link', 'meta', 'base', 'form'];
  dangerous.forEach(tag => doc.querySelectorAll(tag).forEach(el => el.remove()));

  doc.querySelectorAll('*').forEach(el => {
    [...el.attributes].forEach(attr => {
      if (
        attr.name.startsWith('on') ||
        (attr.name === 'href' && attr.value.toLowerCase().startsWith('javascript:')) ||
        (attr.name === 'src' && attr.value.toLowerCase().startsWith('javascript:')) ||
        attr.name === 'action'
      ) {
        el.removeAttribute(attr.name);
      }
    });
  });

  return doc.body.innerHTML;
}

export function printSection({ title, element }) {
  if (!element) return;

  const printWindow = window.open('', '_blank', 'width=1200,height=900');
  if (!printWindow) return;

  const cloned = element.cloneNode(true);

  // Remove interactive / non-print elements from the clone
  cloned.querySelectorAll('.no-print, button, svg, input, select').forEach(el => el.remove());

  // Remove the Actions column (last th/td in every row)
  cloned.querySelectorAll('tr').forEach(row => {
    const cells = row.querySelectorAll('th, td');
    if (cells.length > 0) cells[cells.length - 1].remove();
  });

  const safeHTML = sanitizeHTML(cloned.innerHTML);

  const safeTitle = title.replace(/[<>"'&]/g, c =>
    ({ '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;', '&': '&amp;' }[c])
  );

  const printDate = new Date().toLocaleDateString('fr-FR', {
    year: 'numeric', month: 'long', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });

  printWindow.document.open();
  printWindow.document.write(`<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8" />
  <meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline';">
  <title>${safeTitle}</title>
  <style>
    @page {
      size: A4 landscape;
      margin: 15mm 12mm;
    }
    * { box-sizing: border-box; }
    body {
      font-family: Arial, sans-serif;
      font-size: 10px;
      color: #111827;
      margin: 0;
    }

    /* Print header */
    .print-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      border-bottom: 2px solid #1e40af;
      padding-bottom: 8px;
      margin-bottom: 12px;
    }
    .print-header h1 {
      margin: 0;
      font-size: 16px;
      color: #1e40af;
      font-weight: 700;
    }
    .print-header .meta {
      text-align: right;
      font-size: 8px;
      color: #6b7280;
      line-height: 1.5;
    }

    /* Card header (titre du tableau) */
    .card-header { margin-bottom: 6px; }
    .card-title  { font-size: 11px; font-weight: 600; color: #374151; }

    /* Table */
    .table-container { overflow: visible !important; }
    table {
      width: 100% !important;
      min-width: unset !important;
      border-collapse: collapse;
      font-size: 8.5px;
      table-layout: auto;
    }
    th, td {
      border: 1px solid #d1d5db;
      padding: 3px 5px;
      text-align: left;
      vertical-align: top;
      word-break: break-word;
    }
    th {
      background: #eff6ff;
      font-weight: 600;
      color: #1e40af;
    }
    tr:nth-child(even) td { background: #f9fafb; }

    /* Tags */
    .tag {
      display: inline-block;
      padding: 1px 4px;
      border-radius: 3px;
      border: 1px solid #cbd5e1;
      font-size: 7.5px;
    }

    /* Print footer with page numbers */
    @media print {
      .no-print, button, svg, input, select { display: none !important; }
      thead { display: table-header-group; }
      tr    { page-break-inside: avoid; }
    }
  </style>
</head>
<body>
  <div class="print-header">
    <h1>${safeTitle}</h1>
    <div class="meta">
      <div>Plateforme Médicale</div>
      <div>Imprimé le ${printDate}</div>
    </div>
  </div>
  ${safeHTML}
</body>
</html>`);
  printWindow.document.close();
  printWindow.focus();
  // Close only after the user finishes printing
  printWindow.onafterprint = () => printWindow.close();
  printWindow.print();
}
