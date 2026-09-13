# ידע מקומי — localKnowledge

What works where, written down where you learned it.

## Why it exists

On 2026-09-09, close to midnight, the map said there was a free shower at
חוף בית ינאי. That was true. What the map could not say is that the beach is a
national park, that the gate locks at night, and that none of it matters because
you can park outside and walk 360 m to the showers. The drive was wasted once;
the sentence that stops it being wasted again had nowhere to live.

So this app does not store places. **It stores a need and the method that
actually worked, fixed to the spot where it was learned.** You search by the need
you have again; what you read back is the method. The place alone is what every
map already told you.

## The model

One table, one record type: a **finding**.

| field | what it is |
| :--- | :--- |
| `need` | what you were after — `מקלחת`, `אוהל`, `חנות`. A string, never an enum: the next need is one you have not had yet. |
| `method` | the payload. How to actually get the thing. |
| `place` | what you would call it out loud. |
| `lat` / `lon` | **where you stop the car**, not where the thing is. The seeded Beit Yanai record points at the gate, because that is what you navigate to. **Or neither**: a supplier you phone has no spot, and anchoring him to wherever you stood when you saved his number would navigate you there. |
| `phone` | someone to call about it — tap to dial or open WhatsApp. Null for most findings; a shower has no number. |
| `verdict` | `WORKS` or `AVOID`. |
| `confirmed_at` / `confirmed_n` | when it was last checked, and how many times. |

**`AVOID` is a first-class record, not a failure state.** "Looks good on the map,
gate locks at night" is worth as much as a place that works — more, really,
because the map will keep sending you there.

**Age is the basis for trust**, so a confirmation bumps a counter instead of
overwriting the record. Confirmed four times over two years is a different claim
from written down once, and the UI says which in words ("אושר לפני 2 ימים").

## Where state lives

- **The server is the record** (`local_knowledge` on the system MySQL, 3306). The
  device keeps a cache so the app opens useful with no signal; the cache is never
  a second place a finding can be edited.
- **Capture survives no coverage**, because a tent spot is learned exactly where
  there is none and cannot be re-learned later. An offline capture goes to a
  visible outbox ("N רישומים עוד לא נשלחו") and is retried. It is never silently
  merged.
- **Confirm and delete require a connection** and say so when there is none. They
  act on a record the server already holds and they can wait; queueing them would
  buy a second source of truth for nothing.

**The client owns the clock.** Nothing uses `CURRENT_TIMESTAMP` — MySQL writes
defaults in the server's zone (IDT), which read back as UTC put every row three
hours into the future. Every timestamp is passed explicitly, in UTC.

## Layout

| path | what |
| :--- | :--- |
| `db/schema.sql` | the one table. Re-runnable: `sudo mysql local_knowledge < db/schema.sql` |
| `lib/findings.ts` | the only place that reads or writes it |
| `app/api/findings/…` | list / save / confirm / delete, all bearer-guarded |
| `app/page.tsx` | the reading room — everything you know, on a big screen. Read-only on purpose: a finding edited from an armchair is a finding edited without being there. |
| `mobile/shared/src/commonMain` | **every screen.** Models, geo, store, UI. |
| `mobile/app` | the Android launcher: location, storage, and the hand-offs — `geo:` to a navigator, `tel:` to the dialler, `wa.me` to WhatsApp. iOS is a launcher away. |

## Auth

The service listens on `0.0.0.0` (WireGuard *and* the home LAN), so every route
needs `Authorization: Bearer $LOCALKNOWLEDGE_API_TOKEN`. The server reads it from
`.env.local`; the APK gets the same value baked in at build time from the
gitignored `mobile/.env`. A build with no token says so on screen.

The phone reaches the backend directly at `10.7.0.2:3153` over WireGuard — there
is no public URL and no nginx in the path.

## Build and install

```bash
cd mobile && ./gradlew assembleDevDebug      # always the dev flavour while developing
androidDeploy localKnowledge                 # builds + CHUNKED install + launch
```

Never `adb install -r` against a `:5555` serial — a raw install over WireGuard
stalls permanently once the ADB buffer fills. `androidDeploy` routes through
`utilities/chunked-adb-install.sh`, which is why it works.

Web side: `d startApp --app localKnowledge` (dev 3153, prod 3152).

## Tests

`./gradlew :shared:testDebugUnitTest` — the two things that cannot be checked by
looking at the screen: how old a finding reads as, and the geometry the list is
ordered by — including that a finding with no spot sorts after the ones you can drive to — and the phone digits a WhatsApp link needs. All fail silently. The bearing test is there because the first draft
of the seeded record said the showers were *west* of the gate; they are
south-west, and "west" is how you walk past them in the dark.
