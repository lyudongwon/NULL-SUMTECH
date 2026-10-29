import { AuthForm } from "@/components/auth/AuthForm";
import { PageHead } from "@/components/layout/PageHead";

export const metadata = { title: "Log in | Can I Eat?" };

export default function LoginPage() {
  return (
    <div className="flex flex-1 flex-col gap-6">
      <PageHead title="Log in" />
      <AuthForm mode="login" />
    </div>
  );
}
