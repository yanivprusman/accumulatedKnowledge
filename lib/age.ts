/**
 * How old a record is, in Hebrew words.
 *
 * Age is the only honest basis for trusting a finding: a tent spot confirmed
 * last month is a different claim from one written down four years ago and never
 * revisited. Printing the raw date would leave the reader to do that arithmetic.
 */
export function ageLabel(utc: string, now: Date = new Date()): string {
  const then = new Date(utc.replace(" ", "T") + "Z");
  if (Number.isNaN(then.getTime())) return "בזמן לא ידוע";
  const days = Math.floor((now.getTime() - then.getTime()) / 86_400_000);
  if (days <= 0) return "היום";
  if (days === 1) return "אתמול";
  if (days < 7) return `לפני ${days} ימים`;
  if (days < 31) return `לפני ${Math.floor(days / 7)} שבועות`;
  if (days < 365) return `לפני ${Math.floor(days / 30)} חודשים`;
  if (days < 730) return "לפני שנה";
  return `לפני ${Math.floor(days / 365)} שנים`;
}
