import React from "react";
import { Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Dashboard from "./pages/Dashboard";
import Profile from "./pages/Profile";
import Logout from "./pages/Logout";
import Protected from "../src/components/Protected";

const RoutesView = () => {
	return (
		<Routes>
		  <Route path="/" element={<Dashboard />} />
		  <Route path="/login" element={<Login />} />
		  <Route path="/signup" element={<Signup />} />
		  <Route path="/logout" element={<Logout />} />
		  <Route path="/dashboard" element={<Protected><Dashboard /></Protected>} />
		  <Route path="/profile" element={<Protected><Profile /></Protected>} />
		  <Route path="*" element={<div style={{padding:24}}>404 Not Found</div>} />
		</Routes>
	  );
}

export default RoutesView;