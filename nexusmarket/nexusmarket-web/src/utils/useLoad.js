import { useCallback, useEffect, useRef, useState } from "react";

/** Loads data when `load` changes; answers of an older request are ignored. */
export function useLoad(load) {
  const [state, setState] = useState({ data: undefined, error: "", loading: true });
  const current = useRef(0);

  const reload = useCallback(async () => {
    const turn = ++current.current;
    setState((previous) => ({ ...previous, error: "", loading: true }));
    try {
      const data = await load();
      if (turn === current.current) setState({ data, error: "", loading: false });
    } catch (failure) {
      if (turn === current.current) setState({ data: undefined, error: failure.message, loading: false });
    }
  }, [load]);

  useEffect(() => {
    reload();
    return () => {
      current.current++;
    };
  }, [reload]);

  const setData = useCallback((data) => {
    current.current++;
    setState({ data, error: "", loading: false });
  }, []);
  return { ...state, reload, setData };
}
