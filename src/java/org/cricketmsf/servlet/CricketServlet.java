/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cricketmsf.servlet;

import org.cricketmsf.ArgumentParser;
import org.cricketmsf.Kernel;
import org.cricketmsf.Runner;
import org.cricketmsf.config.ConfigSet;
import org.cricketmsf.config.Configuration;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author greg
 */
public class CricketServlet extends HttpServlet {
    
    static Kernel service;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet CricketServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet CricketServlet at " + request.getContextPath() + "</h1>");
            out.println(service.getClass().getName());
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public void init(){
        final ConfigSet configSet;
        configSet=new Runner().readConfig(new ArgumentParser());
        Class serviceClass = null;
        String serviceName;
        Configuration configuration = null;
        serviceName = configSet.getDefault().getId();
        configuration = configSet.getConfigurationById(serviceName);
        System.out.println("STARTING CRICKET SERVICE: "+serviceName);
        try {
            serviceClass = Class.forName(serviceName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            //System.exit(-1);
        }
        System.out.println("RUNNER");
        try {
            service = (Kernel) Kernel.getInstanceWithProperties(serviceClass, configuration);
            service.setConfigSet(configSet);
            //start
            service.getAdapters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void destroy(){
        service.shutdown();
        super.destroy();
    }
}
