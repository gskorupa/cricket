/*
 * Copyright 2015 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package com.gskorupa.cricket.example;

import java.io.IOException;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.gskorupa.cricket.Response;
import com.gskorupa.cricket.ServiceError;

/**
 *
 * @author greg
 */
public class HelloServlet extends HttpServlet {

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

        HelloService sService = (HelloService) HelloService.getInstanceUsingResources(HelloService.class);
        sService.getAdapters();
        HelloResult result = sService.getData();

        TreeMap<String, String> headers = new TreeMap();
        headers.put("service-name", "SimpleService");

        if (result.getCode() == 0) {
            Response.getResponse().send(
                    request,
                    response,
                    headers,
                    HttpServletResponse.SC_OK,
                    result);
        } else {
            ServiceError error = new ServiceError(sService, HttpServletResponse.SC_METHOD_NOT_ALLOWED, result.getCode(), "not implemented");
            error.sendResponse(request, response);
        }
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

        HelloService sService = (HelloService) HelloService.getInstanceUsingResources(HelloService.class);
        ServiceError error = new ServiceError(sService, HttpServletResponse.SC_METHOD_NOT_ALLOWED, -1, "not implemented");
        error.sendResponse(request, response);

    }

    /**
     * Handles the HTTP <code>PUT</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HelloService sService = (HelloService) HelloService.getInstanceUsingResources(HelloService.class);
        ServiceError error = new ServiceError(sService, HttpServletResponse.SC_METHOD_NOT_ALLOWED, -1, "not implemented");
        error.sendResponse(request, response);

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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HelloService sService = (HelloService) HelloService.getInstanceUsingResources(HelloService.class);
        ServiceError error = new ServiceError(sService, HttpServletResponse.SC_METHOD_NOT_ALLOWED, -1, "not implemented");
        error.sendResponse(request, response);

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

}
