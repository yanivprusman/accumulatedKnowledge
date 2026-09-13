import "server-only";
import { q, exec } from "./db";

/** A finding as it travels over the wire. Timestamps are UTC "YYYY-MM-DD HH:MM:SS". */
export type Finding = {
  id: string;
  need: string;
  place: string;
  /** Both set or both null. Null is a finding with no spot — a supplier you phone. */
  lat: number | null;
  lon: number | null;
  accuracyM: number | null;
  verdict: "WORKS" | "AVOID";
  method: string;
  phone: string | null;
  foundAt: string;
  confirmedAt: string;
  confirmedN: number;
  updatedAt: string;
};

type Row = {
  id: string; need: string; place: string; lat: number | null; lon: number | null;
  accuracy_m: number | null; verdict: "WORKS" | "AVOID"; method: string; phone: string | null;
  found_at: string; confirmed_at: string; confirmed_n: number; updated_at: string;
};

const toFinding = (r: Row): Finding => ({
  id: r.id,
  need: r.need,
  place: r.place,
  lat: r.lat === null ? null : Number(r.lat),
  lon: r.lon === null ? null : Number(r.lon),
  accuracyM: r.accuracy_m === null ? null : Number(r.accuracy_m),
  verdict: r.verdict,
  method: r.method,
  phone: r.phone,
  foundAt: r.found_at,
  confirmedAt: r.confirmed_at,
  confirmedN: Number(r.confirmed_n),
  updatedAt: r.updated_at,
});

/**
 * Every finding, newest-confirmed first.
 *
 * The whole set goes to the phone in one call and is ordered there by where the
 * phone actually is. Ordering by distance on the server would need the server to
 * know where you are, which is both a worse answer (it changes as you walk) and
 * a position sent somewhere it does not need to go.
 */
export async function listFindings(): Promise<Finding[]> {
  const rows = await q<Row>("SELECT * FROM findings ORDER BY confirmed_at DESC");
  return rows.map(toFinding);
}

/** Create or replace. The client owns the id, so saving twice is not two rows. */
export async function saveFinding(f: Finding): Promise<void> {
  await exec(
    `INSERT INTO findings
       (id, need, place, lat, lon, accuracy_m, verdict, method, phone,
        found_at, confirmed_at, confirmed_n, updated_at)
     VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
     ON DUPLICATE KEY UPDATE
       need=VALUES(need), place=VALUES(place), lat=VALUES(lat), lon=VALUES(lon),
       accuracy_m=VALUES(accuracy_m), verdict=VALUES(verdict), method=VALUES(method),
       phone=VALUES(phone), found_at=VALUES(found_at), confirmed_at=VALUES(confirmed_at),
       confirmed_n=VALUES(confirmed_n), updated_at=VALUES(updated_at)`,
    [
      f.id, f.need, f.place, f.lat, f.lon, f.accuracyM, f.verdict, f.method, f.phone,
      f.foundAt, f.confirmedAt, f.confirmedN, f.updatedAt,
    ],
  );
}

/**
 * "I was here again and it still works."
 *
 * Bumps the count rather than overwriting the record, because age is the only
 * thing that tells you whether to trust a finding: a tent spot confirmed four
 * times over two years is a different claim from one written down once.
 */
export async function confirmFinding(id: string, at: string): Promise<boolean> {
  const res = await exec(
    "UPDATE findings SET confirmed_at=?, confirmed_n=confirmed_n+1, updated_at=? WHERE id=?",
    [at, at, id],
  );
  return res.affectedRows > 0;
}

export async function deleteFinding(id: string): Promise<boolean> {
  const res = await exec("DELETE FROM findings WHERE id=?", [id]);
  return res.affectedRows > 0;
}
