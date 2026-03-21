import './App.css'
import Menubar from "./components/Menubar.jsx";
import {Fragment} from "react";
import Home from "./pages/Home.jsx";
import Footer from "./components/Footer.jsx";
import { Routes, Route } from "react-router-dom";
import {Toaster} from "react-hot-toast";
import AppContextProvider from "./context/AppContextProvider.jsx";
import UserSyncHandler from "./components/UserSyncHandler.jsx";
import PaymentSuccess from "./pages/PaymentSuccess.jsx";
import PaymentCancel from "./pages/PaymentCancel.jsx";


function App() {

  return (
      <AppContextProvider>
         <Fragment>
             <UserSyncHandler/>
             <Menubar/>
             <Toaster/>
             <Routes>
                 <Route path="/" element={<Home/>}></Route>
                 <Route path="/payment/success" element={<PaymentSuccess/>}></Route>
                 <Route path="/payment/cancel" element={<PaymentCancel/>}></Route>
             </Routes>
             <Footer/>
         </Fragment>
      </AppContextProvider>
  )
}

export default App
