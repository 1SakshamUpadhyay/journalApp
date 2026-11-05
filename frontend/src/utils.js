
export const errMsg = (e, fallback = 'Something went wrong') => {
  if (!e.response) {
    return 'Cannot reach the server. Is the backend running?';
  }

  const d = e.response.data;

  if (typeof d === 'string' && d.trim()) {
    return d;
  }

  if (d?.message) {
    return d.message;
  }

  const map = {
    400: 'Invalid request',
    403: 'Not allowed',
    404: 'Not found',
    409: 'Already exists',
    500: 'Server error'
  };

  return map[e.response.status] || fallback;
};

// Get MongoDB ObjectId safely
export const entryId = (e) => {
  const id = e?.id ?? e?._id;

  if (typeof id === 'string') {
    return id;
  }

  return id?.$oid || id?.hexString || null;
};

// Format journal entry date
export const fmtDate = (d) => {
  return d
    ? new Date(d).toLocaleString(undefined, {
        dateStyle: 'medium',
        timeStyle: 'short'
      })
    : '';
};

// Extract username from JWT
export const jwtSub = (t) => {
  try {
    return JSON.parse(
      atob(t.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
    ).sub;
  } catch {
    return '';
  }
};
