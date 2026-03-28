export default function App() {
  return (
    <main className="shell">
      <section className="hero">
        <p className="eyebrow">Service Auth</p>
        <h1>Hello, World.</h1>
        <p className="lede">
          Admin Hub is online. This first sketch is a slim React and webpack shell
          for future client, user, and permission management.
        </p>
      </section>

      <section className="panel" aria-label="Frontend status">
        <div className="panel-row">
          <span className="label">Stack</span>
          <span className="value">ReactJS + webpack</span>
        </div>
        <div className="panel-row">
          <span className="label">Theme</span>
          <span className="value">Slim neutral CSS</span>
        </div>
        <div className="panel-row">
          <span className="label">Port</span>
          <span className="value">3000</span>
        </div>
      </section>
    </main>
  )
}
