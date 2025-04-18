import axios from "axios";
import { jwtDecode } from "jwt-decode";

const BaseURL = "http://localhost:8082/api";

export const getBaseUrl = () => {
    return BaseURL;
}

export const loginUser = async (email, password) => {
  try {
    const url = `${BaseURL}/auth/login`;
    const response = await axios.post(url, {
      email,
      password,
    });

    return response.data;
  } catch (error) {
    console.error("API Error:", error.response || error.message);
    throw new Error(
      error.response?.data?.message || "Something went wrong during login."
    );
  }
};

export const getLoggedInUserRole = () => {
  const token = localStorage.getItem("accessToken");
  debugger;
  if (token) {
    const decodedToken = jwtDecode(token);
    return decodedToken.roles[0];
  }
  return "";
};

export async function registerUser(user) {
  try {
    let res = await axios.post(BaseURL + "/auth/register", user);
    debugger;
    if (res.status === 200) {
      return {
        success: true,
        message: "User Registration successful",
        data: res.data,
      };
    } else {
      return {
        success: false,
        message: res.data.message,
        data: res.data,
      };
    }
  } catch (error) {
    return { success: false, message: error.message };
  }
};

export async function googleLogin() {
    try {
      let res = await axios.post(BaseURL + "/oauth2/google/login");
      if (res.status === 200) {
        return {
          success: true,
          message: "User Registration successful",
          data: res.data,
        };
      } else {
        return {
          success: false,
          message: res.data.message,
          data: res.data,
        };
      }
    } catch (error) {
      return { success: false, message: error.message };
    }
  }
