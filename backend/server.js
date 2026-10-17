const express = require('express');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const crypto = require('crypto');
const { Pool } = require('pg');

const app = express();
app.disable('x-powered-by');
app.use(helmet());
app.use(express.json({ limit: '4kb' }));
app.use('/api/', rateLimit({ windowMs: 60_000, limit: 30, standardHeaders: true, legacyHeaders: false }));

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.PGSSL === 'true' ? { rejectUnauthorized: true } : undefined
});

function hashKey(value) {
  return crypto.createHash('sha256').update(value, 'utf8').digest('hex');
}

app.get('/health', (_req, res) => res.json({ ok: true }));

app.post('/api/verify-key', async (req, res) => {
  const key = typeof req.body?.key === 'string' ? req.body.key.trim() : '';
  if (key.length < 4 || key.length > 200) {
    return res.status(400).json({ valid: false, message: 'Key không hợp lệ hoặc đã hết hạn' });
  }
  try {
    const result = await pool.query(
      `SELECT active, expires_at
       FROM activation_keys
       WHERE key_sha256 = $1
       LIMIT 1`,
      [hashKey(key)]
    );
    const row = result.rows[0];
    const valid = !!row && row.active === true &&
      (row.expires_at === null || new Date(row.expires_at).getTime() > Date.now());

    // Do not reveal whether a key exists, is disabled, or has expired.
    return res.status(200).json({
      valid,
      message: valid ? 'Key hợp lệ' : 'Key không hợp lệ hoặc đã hết hạn'
    });
  } catch (err) {
    console.error('Key verification database error:', err.message);
    return res.status(503).json({ valid: false, message: 'Máy chủ tạm thời bận, thử lại sau' });
  }
});

const port = Number(process.env.PORT || 3000);
app.listen(port, () => console.log(`QBao key API listening on ${port}`));
