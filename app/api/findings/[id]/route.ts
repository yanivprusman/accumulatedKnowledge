import { guarded } from "@/lib/auth";
import { deleteFinding } from "@/lib/findings";

/** A delete is a real DELETE — undo is the client writing back what it still holds. */
export const DELETE = guarded(async (_request, ctx) => {
  const { id } = await ctx.params;
  const gone = await deleteFinding(id);
  return gone
    ? Response.json({ ok: true })
    : Response.json({ ok: false, error: "no such finding" }, { status: 404 });
});
