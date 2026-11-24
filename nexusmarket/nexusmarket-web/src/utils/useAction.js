import { useCallback, useState } from "react";

/** Runs one action at a time and keeps its error; `run` returns the error, or null when it worked. */
export function useAction() {
  const [busy, setBusy] = useState(false);
  const [failure, setFailure] = useState("");

  const run = useCallback(async (action) => {
    setBusy(true);
    setFailure("");
    try {
      await action();
      return null;
    } catch (problem) {
      setFailure(problem.message);
      return problem;
    } finally {
      setBusy(false);
    }
  }, []);

  return { busy, failure, run };
}
