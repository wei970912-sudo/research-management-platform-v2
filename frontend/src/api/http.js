import axios from "axios";

const http = axios.create({
  baseURL: "/api",
  timeout: 10000,
  withCredentials: true
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    if (status === 401 && window.location.pathname !== "/login") {
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default http;
