import type { Metadata } from "next";
import "./globals.css";
import FeedbackChatMount from "./FeedbackChatMount";

export const metadata: Metadata = {
  title: "ידע מצטבר",
  description: "מה עובד, איפה — מה שלמדת בשטח ושום מפה לא יודעת",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="he" dir="rtl">
      <body>{children}
        <FeedbackChatMount />
</body>
    </html>
  );
}
