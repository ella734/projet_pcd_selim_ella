import * as XLSX from 'xlsx';

function canExport() {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return !!user.role;
  } catch {
    return false;
  }
}

// Masks sensitive columns — supports both original field names and French display names
function filterSensitiveColumns(rows, role) {
  if (role === 'ADMIN') return rows;

  return rows.map(row => {
    const filtered = { ...row };

    // N° carnet — field name or display name
    const carnetKey = filtered.numCarnetP !== undefined ? 'numCarnetP' : 'N° carnet';
    if (filtered[carnetKey]) {
      filtered[carnetKey] = '****' + String(filtered[carnetKey]).slice(-4);
    }

    // Email — field name or display name
    const emailKey = filtered.adressEmailP !== undefined ? 'adressEmailP' : 'Email';
    if (filtered[emailKey]) {
      const [local, domain] = String(filtered[emailKey]).split('@');
      filtered[emailKey] = local.slice(0, 2) + '***@' + (domain || '');
    }

    return filtered;
  });
}

export function exportRowsToExcel({ rows, sheetName, fileName }) {
  if (!canExport()) {
    console.error('Export non autorisé');
    return;
  }

  const user = (() => {
    try { return JSON.parse(localStorage.getItem('user') || '{}'); }
    catch { return {}; }
  })();

  const filteredRows = filterSensitiveColumns(rows, user.role);

  const worksheet = XLSX.utils.json_to_sheet(filteredRows);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName);

  const safeFileName = String(fileName).replace(/[^a-zA-Z0-9_\-]/g, '_');
  XLSX.writeFile(workbook, `${safeFileName}.xlsx`);
}

export async function importRowsFromExcel(file) {
  const allowedTypes = [
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-excel',
  ];
  if (!allowedTypes.includes(file.type) && !file.name.match(/\.(xlsx|xls)$/i)) {
    throw new Error('Fichier invalide. Seuls les fichiers .xlsx et .xls sont acceptés.');
  }

  if (file.size > 10 * 1024 * 1024) {
    throw new Error('Fichier trop volumineux. Maximum 10 Mo.');
  }

  const buffer = await file.arrayBuffer();
  const workbook = XLSX.read(buffer, { type: 'array', cellDates: true });
  const sheetName = workbook.SheetNames[0];
  const worksheet = workbook.Sheets[sheetName];
  return XLSX.utils.sheet_to_json(worksheet, { defval: '', raw: false });
}

export function parseNumber(value) {
  if (value === null || value === undefined || value === '') return null;
  const normalized = Number(String(value).trim());
  return Number.isNaN(normalized) ? null : normalized;
}

export function parseBoolean(value) {
  if (typeof value === 'boolean') return value;
  const normalized = String(value ?? '').trim().toLowerCase();
  if (['true', 'vrai', 'oui', '1'].includes(normalized)) return true;
  if (['false', 'faux', 'non', '0'].includes(normalized)) return false;
  return false;
}

export function parseDate(value) {
  if (!value) return null;
  if (value instanceof Date && !Number.isNaN(value.getTime()))
    return value.toISOString().split('T')[0];
  const asDate = new Date(value);
  if (!Number.isNaN(asDate.getTime()))
    return asDate.toISOString().split('T')[0];
  return null;
}
