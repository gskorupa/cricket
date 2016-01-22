/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.servlet;

import com.gskorupa.cricket.services.EchoService;
import com.gskorupa.cricket.HttpAdapterHook;
import com.gskorupa.cricket.Kernel;
import com.gskorupa.cricket.RequestObject;
import com.gskorupa.cricket.in.Result;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author greg
 */
public class CricketServlet extends HttpServlet {

    private HashMap<String, String> hookMethodNames = new HashMap();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        //getServiceHooks();
    }

    private void getServiceHooks() {
        HttpAdapterHook ah;
        String requestMethod;
        // for every method of a Kernel instance (our service class extending Kernel)
        for (Method m : Kernel.getInstanceUsingResources(CricketServlet.class).getClass().getMethods()) {
            ah = (HttpAdapterHook) m.getAnnotation(HttpAdapterHook.class);
            // we search for annotated method
            if (ah != null) {
                requestMethod = ah.requestMethod();
                // 'this' is a handler class loaded according to configuration described in propertis
                // file
                // we need to find all names of implemented interfaces because
                // handler class is mapped by the interface name
                for (Class c : this.getClass().getInterfaces()) {
                    //
                    if (ah.handlerClassName().equals(c.getSimpleName())) {
                        //System.out.println(ah.handlerClassName() + " " + c.getSimpleName());
                        //setHookMethodName(m.getName());
                        addHookMethodNameForMethod(requestMethod, m.getName());
                        System.out.println("hook method for http method " + requestMethod + " : " + m.getName());
                        break;
                    }
                }
            }
        }
    }

    public void addHookMethodNameForMethod(String requestMethod, String hookMethodName) {
        hookMethodNames.put(requestMethod, hookMethodName);
    }

    public String getHookMethodNameForMethod(String requestMethod) {
        String result = null;
        result = hookMethodNames.get(requestMethod);
        if (null == result) {
            result = hookMethodNames.get("*");
        }
        return result;
    }

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

        String method = request.getMethod();
        String hookMethodName = getHookMethodNameForMethod(method);
        if (null == hookMethodName) {
            hookMethodName = getHookMethodNameForMethod("*");
        }
        HashMap<String, Object> parameters = new HashMap();
        parameters.put("name", "x");
        parameters.put("surname", "y");
        RequestObject requestObject = new RequestObject();
        requestObject.method = method;
        requestObject.parameters = parameters;
        requestObject.pathExt = request.getContextPath();
        System.out.println("XXXXXXXXXX");
        Result result = null;
        Kernel.getInstanceUsingResources(Kernel.class);
        System.out.println("YYYYYYYYYY");
        
        //result=(Result)((Kernel)Kernel.getInstanceUsingResources(Kernel.class))
        //        .doGet(requestObject);
        //try {
            //sendLogEvent("sending request to hook method " + getHookMethodNameForMethod(method));
            //Method m = Kernel.getInstance().getClass().getMethod(getHookMethodNameForMethod(method), RequestObject.class);
            //result = (Result) m.invoke(Kernel.getInstance(), parameters);
        //} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            //e.printStackTrace();
        //}
        String acceptType = request.getHeader("Accept");
        response.setStatus(result.getCode());
        response.setContentType("text/plain");
        response.getWriter().print(result.toString());
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

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
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

}
