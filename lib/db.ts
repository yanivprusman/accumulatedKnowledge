import mysql from "mysql2/promise";

// One pool against the system MySQL (3306), where `local_knowledge` lives — the
// same arrangement as the sibling apps (tally, veggieBox, govaBoard). Creds are
// overridable via env so the same code runs wherever the app ends up hosted.
const globalForDb = globalThis as typeof globalThis & { _lkPool?: mysql.Pool };

export const pool =
  globalForDb._lkPool ??
  mysql.createPool({
    host: process.env.DB_HOST ?? "127.0.0.1",
    port: Number(process.env.DB_PORT ?? 3306),
    user: process.env.DB_USER ?? "localknowledge",
    password: process.env.DB_PASSWORD ?? "",
    database: process.env.DB_NAME ?? "local_knowledge",
    waitForConnections: true,
    connectionLimit: 5,
    charset: "utf8mb4",
    dateStrings: true,
  });

if (process.env.NODE_ENV !== "production") globalForDb._lkPool = pool;

export async function q<T = Record<string, unknown>>(
  sql: string,
  params?: unknown[],
): Promise<T[]> {
  const [rows] = await pool.query(sql, params);
  return rows as T[];
}

export async function exec(sql: string, params?: unknown[]) {
  const [res] = await pool.query(sql, params);
  return res as mysql.ResultSetHeader;
}
