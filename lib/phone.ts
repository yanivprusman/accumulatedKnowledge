/**
 * The digits WhatsApp's `wa.me` link wants: country code, no plus, no dashes.
 *
 * Same rule as the phone app's `internationalDigits` — numbers are written the way
 * they were read out ("050-2225880"), and the local trunk 0 means nothing to wa.me.
 */
export function whatsAppDigits(phone: string): string | null {
  const digits = phone.replace(/\D/g, "");
  if (!digits) return null;
  if (digits.startsWith("00")) return digits.slice(2);
  if (digits.startsWith("0")) return "972" + digits.slice(1);
  return digits;
}
