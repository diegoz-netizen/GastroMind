import { createContext, useState, useContext } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState({
    token: null,
    empleadoId: null,
    nombreCompleto: null,
    correo: null,
    rol: null,
    restauranteId: null,
  });

  const login = (data) => {
    setAuth({
      token: data.token,
      empleadoId: data.empleadoId,
      nombreCompleto: data.nombreCompleto,
      correo: data.correo,
      rol: data.rol,
      restauranteId: data.restauranteId,
    });
  };

  const logout = () => {
    setAuth({
      token: null,
      empleadoId: null,
      nombreCompleto: null,
      correo: null,
      rol: null,
      restauranteId: null,
    });
  };

  const isAuthenticated = () => {
    return !!auth.token;
  };

  return (
    <AuthContext.Provider value={{ auth, login, logout, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
