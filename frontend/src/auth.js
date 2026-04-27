import { reactive } from "vue";
import { fetchCurrentUser, login as apiLogin, logout as apiLogout } from "./api/modules";

export const authState = reactive({
  user: null,
  ready: false
});

export const isAdmin = () => authState.user?.role === "ADMIN";

export const ensureAuthLoaded = async () => {
  if (authState.ready) {
    return authState.user;
  }

  try {
    authState.user = await fetchCurrentUser();
  } catch {
    authState.user = null;
  } finally {
    authState.ready = true;
  }
  return authState.user;
};

export const login = async (payload) => {
  const user = await apiLogin(payload);
  authState.user = user;
  authState.ready = true;
  return user;
};

export const logout = async () => {
  try {
    await apiLogout();
  } finally {
    authState.user = null;
    authState.ready = true;
  }
};
