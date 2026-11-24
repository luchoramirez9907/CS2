import { Component } from "react";
import Alert from "./Alert.jsx";

/** An unexpected failure of a page shows a message instead of a blank screen. */
export default class ErrorBoundary extends Component {
  state = { failed: false };

  static getDerivedStateFromError() {
    return { failed: true };
  }

  render() {
    if (!this.state.failed) return this.props.children;
    return (
      <>
        <Alert type="error">Esta página tuvo un error. Vuelve a intentarlo o entra a otra opción del menú.</Alert>
        <button type="button" className="secondary" onClick={() => this.setState({ failed: false })}>Reintentar</button>
      </>
    );
  }
}
