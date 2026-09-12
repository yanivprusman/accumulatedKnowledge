import { guarded } from "@/lib/auth";
import { listFindings, saveFinding, type Finding } from "@/lib/findings";

export const GET = guarded(async () =>
  Response.json({ ok: true, findings: await listFindings() }),
);

/**
 * Create or replace one finding. The body carries its own id and its own
 * timestamps: a finding is written where it is learned, which is regularly
 * somewhere with no signal, so the phone cannot wait for the server to name it.
 */
export const POST = guarded(async (request) => {
  const body = (await request.json()) as Partial<Finding>;

  const missing = (["id", "need", "place", "method"] as const).filter(
    (k) => typeof body[k] !== "string" || !(body[k] as string).trim(),
  );
  if (missing.length)
    return Response.json({ ok: false, error: `missing: ${missing.join(", ")}` }, { status: 400 });
  if (typeof body.lat !== "number" || typeof body.lon !== "number")
    return Response.json({ ok: false, error: "lat and lon must be numbers" }, { status: 400 });

  const now = new Date().toISOString().slice(0, 19).replace("T", " ");
  await saveFinding({
    id: body.id!,
    need: body.need!.trim(),
    place: body.place!.trim(),
    lat: body.lat,
    lon: body.lon,
    accuracyM: typeof body.accuracyM === "number" ? Math.round(body.accuracyM) : null,
    verdict: body.verdict === "AVOID" ? "AVOID" : "WORKS",
    method: body.method!,
    foundAt: body.foundAt ?? now,
    confirmedAt: body.confirmedAt ?? body.foundAt ?? now,
    confirmedN: typeof body.confirmedN === "number" ? body.confirmedN : 1,
    updatedAt: now,
  });
  return Response.json({ ok: true });
});
