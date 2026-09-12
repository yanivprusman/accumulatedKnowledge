import { guarded } from "@/lib/auth";
import { confirmFinding } from "@/lib/findings";

/** "Still true." Records that you checked, and when — the only basis for trusting it later. */
export const POST = guarded(async (request, ctx) => {
  const { id } = await ctx.params;
  const body = (await request.json().catch(() => ({}))) as { at?: string };
  const at = body.at ?? new Date().toISOString().slice(0, 19).replace("T", " ");
  const ok = await confirmFinding(id, at);
  return ok
    ? Response.json({ ok: true })
    : Response.json({ ok: false, error: "no such finding" }, { status: 404 });
});
