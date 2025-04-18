import { useState, useEffect } from "react";
import { Link, NavLink } from "react-router-dom";
import { getLoggedInUserRole } from "../apiRequest/AuthRequest";

export const Navbar = () => {
  const [role, setRole] = useState("");

  useEffect(() => {
    const role = getLoggedInUserRole();
    debugger;
    setRole(role);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('accessToken'); 
    localStorage.removeItem('refreshToken');
    setRole('');
    window.location.href="/login";
  };

  return (
    <div className="navbar bg-base-100">
      <div className="flex-1">
        <Link to="/" className="btn btn-ghost normal-case text-xl">
          Your Company
        </Link>
      </div>
      <div className="flex-none">
        <ul className="menu menu-horizontal px-1">
          <li>
            <Link to="/">Home</Link>
          </li>
          <li>
            <a href="#projects">Projects</a>
          </li>
          <li>
            <a href="#contact">Contact</a>
          </li>

          {
            role === "ROLE_ADMIN" ? (
              <li>
                <Link to={"/superadmin"}>Superadmin</Link>
              </li>
            ) : ""

          }

          <li>
            {(role === "" || role === undefined) ? (
              <li>
                <Link to={"/login"}>Login</Link>
              </li>
            ) : (
              <NavLink onClick={handleLogout}>Logout</NavLink>
            )}
          </li>
        </ul>
      </div>
    </div>
  );
};
