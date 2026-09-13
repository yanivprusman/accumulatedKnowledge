import { listFindings, type Finding } from "@/lib/findings";
import { ageLabel } from "@/lib/age";
import { whatsAppDigits } from "@/lib/phone";

// Read from the database on every request: this page is for reading back what you
// know before a trip, and a cached copy of that is worse than useless.
export const dynamic = "force-dynamic";

/**
 * The reading room.
 *
 * Capture happens on the phone, because that is where you are when you learn
 * something. This screen is the other half: everything you know, on a screen big
 * enough to plan with. It is deliberately read-only — a finding edited from an
 * armchair is a finding edited without being there.
 */
export default async function Home() {
  const findings = await listFindings();
  const byNeed = new Map<string, Finding[]>();
  for (const f of findings) {
    const list = byNeed.get(f.need) ?? [];
    list.push(f);
    byNeed.set(f.need, list);
  }
  const needs = [...byNeed.entries()].sort((a, b) => b[1].length - a[1].length);

  return (
    <main className="mx-auto max-w-3xl px-5 py-10">
      <header className="mb-8">
        <h1 className="text-3xl font-semibold tracking-tight">ידע מצטבר</h1>
        <p className="mt-2 text-[15px] leading-relaxed text-(--color-muted)">
          מה שלמדת בשטח ושום מפה לא יודעת. {findings.length} רישומים
          {needs.length > 0 && ` · ${needs.length} סוגי צורך`}.
        </p>
      </header>

      {findings.length === 0 ? (
        <p className="rounded-xl border border-(--color-rule)/40 bg-(--color-card) p-6 text-(--color-muted)">
          עוד אין כאן כלום. הרישום הראשון נכתב מהטלפון, במקום שבו למדת אותו.
        </p>
      ) : (
        <>
          <nav className="mb-8 flex flex-wrap gap-2">
            {needs.map(([need, list]) => (
              <a
                key={need}
                href={`#need-${encodeURIComponent(need)}`}
                data-id={`jump-${need}`}
                className="rounded-full border border-(--color-rule)/50 px-3 py-1 text-sm transition-colors hover:border-(--color-pine) hover:text-(--color-pine)"
              >
                {need} <span className="text-(--color-muted)">{list.length}</span>
              </a>
            ))}
          </nav>

          <div className="space-y-10">
            {needs.map(([need, list]) => (
              <section key={need} id={`need-${encodeURIComponent(need)}`}>
                <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-(--color-muted)">
                  {need}
                </h2>
                <div className="space-y-3">
                  {list.map((f) => (
                    <FindingCard key={f.id} finding={f} />
                  ))}
                </div>
              </section>
            ))}
          </div>
        </>
      )}
    </main>
  );
}

const linkClass = "font-medium text-(--color-pine) underline-offset-2 hover:underline";

function FindingCard({ finding: f }: { finding: Finding }) {
  const works = f.verdict === "WORKS";
  const wa = f.phone ? whatsAppDigits(f.phone) : null;
  return (
    <article className="rounded-xl border border-(--color-rule)/40 bg-(--color-card) p-5">
      <div className="flex items-start justify-between gap-3">
        <h3 className="text-lg font-semibold">{f.place}</h3>
        <span
          className="shrink-0 rounded-md px-2 py-0.5 text-xs font-semibold"
          style={{
            color: works ? "var(--color-works)" : "var(--color-avoid)",
            background: works
              ? "color-mix(in srgb, var(--color-works) 14%, transparent)"
              : "color-mix(in srgb, var(--color-avoid) 14%, transparent)",
          }}
        >
          {works ? "עובד" : "לא שווה"}
        </span>
      </div>

      {/* The sentence the record exists to carry. Newlines are part of it. */}
      <p className="mt-3 whitespace-pre-line text-[17px] leading-relaxed">{f.method}</p>

      <div className="mt-4 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-(--color-muted)">
        <span>
          אושר {ageLabel(f.confirmedAt)}
          {f.confirmedN > 1 && ` · ${f.confirmedN} פעמים`}
        </span>
        {f.phone && (
          <a
            href={`tel:${f.phone.replace(/[^\d+]/g, "")}`}
            dir="ltr"
            data-id={`call-${f.id}`}
            className={linkClass}
          >
            {f.phone}
          </a>
        )}
        {wa && (
          <a
            href={`https://wa.me/${wa}`}
            target="_blank"
            rel="noreferrer"
            data-id={`whatsapp-${f.id}`}
            className={linkClass}
          >
            וואטסאפ
          </a>
        )}
        {f.lat !== null && f.lon !== null && (
          <a
            // waze.com/ul is Waze's own deep link, and it is an Android App Link —
            // tapping it on a phone opens Waze already navigating.
            href={`https://waze.com/ul?ll=${f.lat}%2C${f.lon}&navigate=yes`}
            target="_blank"
            rel="noreferrer"
            data-id={`waze-${f.id}`}
            className={linkClass}
          >
            נווט ב-Waze
          </a>
        )}
      </div>
    </article>
  );
}
