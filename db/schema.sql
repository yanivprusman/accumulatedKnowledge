-- localKnowledge schema (system MySQL 3306, database `local_knowledge`).
--
-- One table, because the app has exactly one kind of record: a FINDING.
-- A finding is not a pin and not a review. It is "I wanted X; here is what
-- actually works," fixed to the spot where it was learned.
--
-- The map already knows where the shower is. What it cannot say is that the
-- gate locks at night and that you can park outside and walk 360 m. That
-- sentence is the whole product, and it lives in `method`.
--
-- Re-runnable: apply with `sudo mysql local_knowledge < db/schema.sql`.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS findings (
  id           VARCHAR(40)  NOT NULL PRIMARY KEY,  -- client-generated, so a finding can be
                                                   -- written standing at the gate, offline
  need         VARCHAR(32)  NOT NULL,              -- what you wanted: shower, tent, shop…
                                                   -- deliberately a string, not an ENUM:
                                                   -- the next need is one you haven't had yet
  place        VARCHAR(96)  NOT NULL,              -- what you'd call it out loud
  lat          DOUBLE           NULL,              -- where you stop the car. Both set or both
  lon          DOUBLE           NULL,              -- NULL: NULL is a supplier you phone, and a
                                                   -- made-up coordinate would send you there
  accuracy_m   INT              NULL,              -- how good the fix was when recorded;
                                                   -- NULL = position was typed, not measured
  verdict      ENUM('WORKS','AVOID') NOT NULL DEFAULT 'WORKS',
                                                   -- AVOID is not a failure state. "Looks good
                                                   -- on the map, gate locks at night" is worth
                                                   -- exactly as much as a place that works.
  method       TEXT         NOT NULL,              -- the payload. How to actually get the thing.
  phone        VARCHAR(32)      NULL,              -- someone to call about it; a shower has none
  found_at     DATETIME     NOT NULL,              -- UTC, written by the client (see below)
  confirmed_at DATETIME     NOT NULL,              -- last time it was checked and still true
  confirmed_n  INT          NOT NULL DEFAULT 1,    -- confirmed once is a note; confirmed five
                                                   -- times over three years is knowledge
  updated_at   DATETIME     NOT NULL,
  KEY idx_need (need),
  KEY idx_confirmed (confirmed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Brings a table created before a finding could be a person rather than a spot up to
-- the definition above. MySQL has no ADD COLUMN IF NOT EXISTS, so the step checks first.
SET @has_phone := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'findings' AND COLUMN_NAME = 'phone'
);
SET @add_phone := IF(@has_phone = 0,
  'ALTER TABLE findings ADD COLUMN phone VARCHAR(32) NULL AFTER method',
  'DO 0');
PREPARE step FROM @add_phone;
EXECUTE step;
DEALLOCATE PREPARE step;

-- Re-declaring a column that is already NULL changes nothing, so this needs no guard.
ALTER TABLE findings MODIFY lat DOUBLE NULL, MODIFY lon DOUBLE NULL;

-- No column here defaults to CURRENT_TIMESTAMP on purpose. MySQL writes defaults in the
-- server's zone (IDT), which read back as UTC put every row three hours into the future —
-- a bug tally paid for already. Every timestamp is passed explicitly, in UTC, by whoever
-- is making the record.
