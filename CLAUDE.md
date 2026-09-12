@AGENTS.md

# localKnowledge

Read `README.md` first — it carries the model and the reasoning. The short version:

- A record is a **need + the method that worked**, fixed to a place. Not a pin,
  not a review. If a change makes the place more prominent than the method, it is
  the wrong change.
- **`AVOID` is first-class.** Never treat it as an error state or hide it.
- **Age is trust.** A confirmation bumps `confirmed_n`; it never overwrites.
- **Capture must work with no signal**; confirm and delete must not pretend to.
  Do not add a write queue for confirm/delete — see README, "Where state lives".
- **All UI lives in `mobile/shared/src/commonMain`.** The Android module is a
  launcher, a location source, a key-value store and a `geo:` hand-off. Putting a
  screen in `mobile/app` is what stops iOS being a launcher away.
- **The client owns the clock** — explicit UTC on every write, never
  `CURRENT_TIMESTAMP`.
- Install with `androidDeploy localKnowledge`, never `adb install -r` over
  WireGuard.
