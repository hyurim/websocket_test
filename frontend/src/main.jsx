import React from "react";
import { createRoot } from "react-dom/client";
import { HashRouter } from "react-router-dom";
import App from "./App";
import { AuthProvider } from "./auth/AuthProvider";
import GlobalStyle from "./styles/GlobalStyle";

createRoot(document.getElementById("root")).render(
  <React.StrictMode>
	<HashRouter>
	  	<AuthProvider>
		  	<GlobalStyle />
			  <App />
		  </AuthProvider>
	</HashRouter>
  </React.StrictMode>
);