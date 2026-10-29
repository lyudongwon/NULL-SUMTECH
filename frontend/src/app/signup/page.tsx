import Link from "next/link";
import { AuthForm } from "@/components/auth/AuthForm";

export const metadata = { title: "Sign up | Can I Eat?" };

export default function SignupPage() {
  return (
    <section className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">Sign up</h1>
        <Link href="/" className="text-sm text-neutral-500">
          Home
        </Link>
      </div>
      <AuthForm mode="signup" />
    </section>
  );
}
