// SÉCURITÉ : Schémas de validation Zod pour tous les formulaires
// Valider AVANT d'envoyer à l'API — jamais de données brutes

// Zod n'est pas dans package.json, on utilise une validation manuelle légère
// compatible avec le projet (pas de dépendance supplémentaire)

export function validateLogin({ username, password }) {
  const errors = {};
  if (!username || username.trim().length < 3)
    errors.username = 'Minimum 3 caractères';
  if (username && username.length > 50)
    errors.username = 'Maximum 50 caractères';
  if (username && !/^[a-zA-Z0-9._@-]+$/.test(username))
    errors.username = 'Caractères invalides';
  if (!password || password.length < 6)
    errors.password = 'Minimum 6 caractères';
  if (password && password.length > 100)
    errors.password = 'Maximum 100 caractères';
  return errors;
}

export function validateUser({ loginU, motPasseU, role, isEditing }) {
  const errors = {};
  if (!loginU || loginU.trim().length < 3)
    errors.loginU = 'Minimum 3 caractères';
  if (loginU && loginU.length > 50)
    errors.loginU = 'Maximum 50 caractères';
  if (loginU && !/^[a-zA-Z0-9._@-]+$/.test(loginU))
    errors.loginU = 'Caractères invalides (lettres, chiffres, . _ @ -)';
  if (!isEditing && (!motPasseU || motPasseU.length < 8))
    errors.motPasseU = 'Minimum 8 caractères';
  if (motPasseU && motPasseU.length > 100)
    errors.motPasseU = 'Maximum 100 caractères';
  if (!role || !['ADMIN', 'MEDECIN', 'MEDECIN_INVESTIGATEUR', 'MEDECIN_SUIVI', 'AGENT_LABORATOIRE', 'AGENT_IMMUNO'].includes(role))
    errors.role = 'Rôle invalide';
  return errors;
}

export function validatePatient({ nomP, prenomP, sexeP, adressEmailP, telephoneP }) {
  const errors = {};
  if (!nomP || nomP.trim().length < 1)
    errors.nomP = 'Nom requis';
  if (nomP && nomP.length > 100)
    errors.nomP = 'Maximum 100 caractères';
  if (!prenomP || prenomP.trim().length < 1)
    errors.prenomP = 'Prénom requis';
  if (sexeP && !['M', 'F'].includes(sexeP))
    errors.sexeP = 'Sexe invalide';
  if (adressEmailP && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(adressEmailP))
    errors.adressEmailP = 'Email invalide';
  if (telephoneP && !/^[+\d\s()-]{6,20}$/.test(telephoneP))
    errors.telephoneP = 'Téléphone invalide';
  return errors;
}

export function validateMedecin({ nomM, prenomM, specialiteM, numTelM }) {
  const errors = {};
  if (!nomM || nomM.trim().length < 1) errors.nomM = 'Nom requis';
  if (nomM && nomM.length > 100) errors.nomM = 'Maximum 100 caractères';
  if (!prenomM || prenomM.trim().length < 1) errors.prenomM = 'Prénom requis';
  if (!specialiteM || specialiteM.trim().length < 1) errors.specialiteM = 'Spécialité requise';
  if (numTelM && !/^[+\d\s()-]{6,20}$/.test(numTelM))
    errors.numTelM = 'Téléphone invalide';
  return errors;
}

export function validateHopital({ libelleH }) {
  const errors = {};
  if (!libelleH || libelleH.trim().length < 2)
    errors.libelleH = 'Nom de l\'hôpital requis (min 2 caractères)';
  if (libelleH && libelleH.length > 200)
    errors.libelleH = 'Maximum 200 caractères';
  return errors;
}

export function validateService({ libelleS }) {
  const errors = {};
  if (!libelleS || libelleS.trim().length < 2)
    errors.libelleS = 'Nom du service requis (min 2 caractères)';
  if (libelleS && libelleS.length > 200)
    errors.libelleS = 'Maximum 200 caractères';
  return errors;
}

// SÉCURITÉ : Sanitisation basique des strings avant envoi à l'API
export function sanitize(str) {
  if (typeof str !== 'string') return str;
  return str.trim().replace(/[<>]/g, '');
}

export function hasErrors(errors) {
  return Object.keys(errors).length > 0;
}
