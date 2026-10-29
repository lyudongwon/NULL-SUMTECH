import { AuthForm } from "@/components/auth/AuthForm";
import { PageHead } from "@/components/layout/PageHead";

export const metadata = { title: "Sign up | Can I Eat?" };

export default function SignupPage() {
  return (
    <div className="flex flex-1 flex-col gap-6">
      <PageHead title="Sign up" />
      <AuthForm mode="signup" />
    </div>
  );
}
