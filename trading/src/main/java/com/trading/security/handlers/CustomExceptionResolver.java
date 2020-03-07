package com.trading.security.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.InternalServerErrorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class CustomExceptionResolver extends SimpleMappingExceptionResolver {

    public CustomExceptionResolver() {
        // Turn logging on by default
        setWarnLogCategory(getClass().getName());
    }

    @Override
    public String buildLogMessage(Exception e, HttpServletRequest req) {
        return "MVC exception: " + e.getLocalizedMessage();
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response,
                                              Object handler, Exception ex) {

        // Log exception
        ex.printStackTrace();
        String exceptionType = ex.getClass().getCanonicalName();

        // Get the ModelAndView to use
        ModelAndView mav = super.doResolveException(request, response, handler, ex);

        // Make more information available to the view - note that SimpleMappingExceptionResolver adds the exception already
        mav.addObject("url", request.getRequestURL());
        mav.addObject("timestamp", new Date());

        ArrayList<String> exceptions404 = new ArrayList<>(
                Arrays.asList(
                        NoHandlerFoundException.class.getName()
                )
        );
        ArrayList<String> exceptions500 = new ArrayList<>(
                Arrays.asList(
                        InternalServerErrorException.class.getName(),
                        NullPointerException.class.getName()
                )
        );

        String userExceptionDetail;
        String errorHuman;
        String errorTech;

        if (exceptions404.contains(exceptionType)) {
            errorHuman = "We cannot find the page you are looking for";
            errorTech = "Page not found";
            userExceptionDetail = String.format("The page %s cannot be found", request.getRequestURL());
            mav.setViewName("customError");
            mav.addObject("status", HttpStatus.NOT_FOUND.value());
        } else if (exceptions500.contains(exceptionType)) {
            errorHuman = "We cannot currently serve the page you request";
            errorTech = "Internal error";
            userExceptionDetail = "The current page refuses to load due to an internal error";
            mav.setViewName("customError");
            mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        } else {
            errorHuman = "We cannot serve the current page";
            errorTech = "General error";
            userExceptionDetail = "A generic error prevents from serving the page";
            mav.setViewName("customError");
            mav.addObject("status", response.getStatus());
        }

        Exception userException = new Exception(userExceptionDetail);
        mav.addObject("error_human", errorHuman);
        mav.addObject("error_tech", errorTech);
        mav.addObject("exception", userException);
        return mav;
    }
}