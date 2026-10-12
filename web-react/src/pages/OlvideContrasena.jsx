import { useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { styles } from './recuperacionStyles';

// HU-04: pedir el enlace de recuperacion. El backend responde igual exista o no el correo.
const API_URL = 'http://localhost:8080/api/auth/recuperar';

function OlvideContrasena() {
  const [correo, setCorreo] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMensaje('');

    if (!correo.trim()) {
      setError('Ingresa tu correo.');
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post(API_URL, { correo: correo.trim() });
      setMensaje(response.data.mensaje);
    } catch (err) {
      if (err.response && err.response.status === 400) {
        setError('El formato del correo es invalido.');
      } else {
        setError('No se pudo conectar con el servidor. Verifica que el backend este corriendo.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>GastroMind</h1>
        <h2 style={styles.subtitle}>Recuperar contrasena</h2>

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.field}>
            <label htmlFor="correo" style={styles.label}>
              Correo electronico
            </label>
            <input
              id="correo"
              type="email"
              placeholder="ejemplo@gastromind.com"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
              style={styles.input}
              autoComplete="email"
            />
          </div>

          {error && <p style={styles.error}>{error}</p>}
          {mensaje && <p style={styles.success}>{mensaje}</p>}

          <button
            type="submit"
            disabled={loading}
            style={{ ...styles.button, ...(loading ? styles.buttonDisabled : {}) }}
          >
            {loading ? 'Enviando...' : 'Enviar enlace'}
          </button>
        </form>

        <Link to="/" style={styles.link}>
          Volver a iniciar sesion
        </Link>
      </div>
    </div>
  );
}

export default OlvideContrasena;
