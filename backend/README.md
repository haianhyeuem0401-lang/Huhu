# Key API deployment notes

1. Create a PostgreSQL database and run `schema.sql`.
2. Set `DATABASE_URL` and `PGSSL` in your host's environment variables.
3. Deploy this folder as a Node.js web service (`npm install`, `npm start`).
4. Use HTTPS and point Android `API_BASE_URL` to the service base URL.
5. For each activation key, calculate SHA-256 on a trusted admin system and insert only the 64-character lowercase hex digest into `activation_keys.key_sha256`. Never commit actual keys or database credentials.
6. Test the endpoint:
   `curl -X POST https://YOUR-API/api/verify-key -H 'Content-Type: application/json' -d '{"key":"test-key"}'`

This is a minimal example. Add authenticated admin key creation/revocation, audit logging, monitoring, and abuse controls before public release. Do not add an admin secret to the Android APK.
